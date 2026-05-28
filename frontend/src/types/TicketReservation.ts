export interface TicketReservationItem {
    ticketTypeId: number;
    quantity: number;
}

export interface TicketReservationRequest {
    eventId: number;
    tickets: TicketReservationItem[];
}

export interface ReservedTicketSummary {
    ticketTypeId: number;
    ticketTypeName: string;
    quantity: number;
    pricePerTicket: number;
}

export interface TicketReservationResponse {
    purchaseId: number;
    eventId: number;
    eventName: string;
    tickets: ReservedTicketSummary[];
    totalPrice: number;
    currency: string;
    expiresAt: string;
}
