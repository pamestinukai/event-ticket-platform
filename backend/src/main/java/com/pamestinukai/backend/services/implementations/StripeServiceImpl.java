package com.pamestinukai.backend.services.implementations;

import com.pamestinukai.backend.dtos.request.CreateCheckoutSessionRequestDTO;
import com.pamestinukai.backend.dtos.response.CreateCheckoutSessionResponseDTO;
import com.pamestinukai.backend.entities.Purchase;
import com.pamestinukai.backend.entities.Ticket;
import com.pamestinukai.backend.entities.TicketType;
import com.pamestinukai.backend.exceptions.ResourceNotFoundException;
import com.pamestinukai.backend.repositories.PurchaseRepository;
import com.pamestinukai.backend.repositories.TicketRepository;
import com.pamestinukai.backend.services.interfaces.IStripeService;
import com.pamestinukai.backend.services.interfaces.ITicketService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements IStripeService {

    private final PurchaseRepository purchaseRepository;
    private final TicketRepository ticketRepository;
    private final ITicketService ticketService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public CreateCheckoutSessionResponseDTO createCheckoutSession(CreateCheckoutSessionRequestDTO dto) throws StripeException {
        Purchase purchase = purchaseRepository.findById(dto.getPurchaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found"));

        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            throw new IllegalStateException("Purchase is not in PENDING status");
        }

        purchase.setBuyerName(dto.getBuyerName());
        purchase.setBuyerEmail(dto.getBuyerEmail());
        purchaseRepository.save(purchase);

        List<Ticket> tickets = ticketRepository.findAllByPurchase(purchase);

        // Group tickets by type to build line items
        Map<TicketType, Long> ticketsByType = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getTicketType, Collectors.counting()));

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (Map.Entry<TicketType, Long> entry : ticketsByType.entrySet()) {
            TicketType tt = entry.getKey();
            long quantity = entry.getValue();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(quantity)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(purchase.getCurrency() != null ? purchase.getCurrency().toLowerCase() : "eur")
                            .setUnitAmount(tt.getPrice().multiply(java.math.BigDecimal.valueOf(100)).longValue())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(tt.getName())
                                    .setDescription(tt.getEvent().getTitle())
                                    .build())
                            .build())
                    .build();

            lineItems.add(lineItem);
        }

        String successUrl = frontendUrl + "/ticket/confirmation?purchaseId=" + purchase.getPurchaseId() + "&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendUrl + "/event/" + tickets.get(0).getTicketType().getEvent().getEventId() + "?payment=cancelled";

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(dto.getBuyerEmail())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addAllLineItem(lineItems)
                .putMetadata("purchaseId", String.valueOf(purchase.getPurchaseId()))
                .build();

        Session session = Session.create(params);

        purchase.setStripeSessionId(session.getId());
        purchase.setPaymentProvider("stripe");
        purchaseRepository.save(purchase);

        log.info("Stripe checkout session created: {} for purchase: {}", session.getId(), purchase.getPurchaseId());

        return new CreateCheckoutSessionResponseDTO(session.getUrl());
    }

    @Override
    public void handleWebhookEvent(String payload, String sigHeader) {
        com.stripe.model.Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new SecurityException("Invalid webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new IllegalStateException("Could not deserialize Stripe session"));

            String purchaseIdStr = session.getMetadata().get("purchaseId");
            if (purchaseIdStr == null) {
                log.error("Stripe webhook: purchaseId missing in session metadata for session {}", session.getId());
                return;
            }

            Long purchaseId = Long.parseLong(purchaseIdStr);

            Purchase purchase = purchaseRepository.findById(purchaseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Purchase not found: " + purchaseId));

            if (purchase.getStatus() == Purchase.PurchaseStatus.COMPLETED) {
                log.info("Purchase {} already completed, skipping webhook", purchaseId);
                return;
            }

            purchase.setProviderTransactionId(session.getPaymentIntent());
            purchaseRepository.save(purchase);

            ticketService.confirmTicketReservation(purchaseId, purchase.getBuyerEmail(), purchase.getBuyerName());

            log.info("Purchase {} confirmed via Stripe webhook", purchaseId);
        }
    }
}
