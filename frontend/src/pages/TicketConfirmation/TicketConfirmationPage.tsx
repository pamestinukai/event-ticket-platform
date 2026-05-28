import {
    Box,
    Button,
    CircularProgress,
    Container,
    Divider,
    Paper,
    Stack,
    Typography,
    Alert,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import HomeIcon from '@mui/icons-material/Home';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import QRCode from 'qrcode';
import { jsPDF } from 'jspdf';
import { Header } from '../../components/Header/Header';
import { Footer } from '../../components/Footer/Footer';
import { getTicketsByPurchase } from '../../api/tickets';
import type { TicketReservationResponse } from '../../types/TicketReservation';
import type { TicketResponse } from '../../types/TicketResponse';
import type { VenueResponse } from '../../types/VenueResponse';

interface ConfirmationState {
    reservation: TicketReservationResponse;
    buyerName: string;
    buyerEmail: string;
    event: {
        title: string;
        startDatetime: string;
        endDatetime: string;
        venue: VenueResponse;
        auditoriumName: string;
        images: string[];
    };
}

function formatDate(date: string) {
    return new Date(date).toLocaleDateString('lt-LT', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function DetailRow({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
    return (
        <Stack direction='row' spacing={1.5} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ color: 'text.secondary', mt: 0.2, flexShrink: 0 }}>{icon}</Box>
            <Box>
                <Typography variant='caption' color='text.secondary' sx={{ display: 'block', lineHeight: 1.2 }}>
                    {label}
                </Typography>
                <Typography variant='body2' sx={{ fontWeight: 600 }}>{value}</Typography>
            </Box>
        </Stack>
    );
}

// Individual ticket card with its own QR code and download button
function TicketCard({
                        ticket,
                        index,
                        event,
                        buyerName,
                        buyerEmail,
                    }: {
    ticket: TicketResponse;
    index: number;
    event: ConfirmationState['event'];
    buyerName: string;
    buyerEmail: string;
}) {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [downloading, setDownloading] = useState(false);

    useEffect(() => {
        if (!canvasRef.current) return;
        QRCode.toCanvas(canvasRef.current, ticket.qrToken, {
            width: 160,
            margin: 1,
            color: { dark: '#111827', light: '#ffffff' },
        });
    }, [ticket.qrToken]);

    async function handleDownload() {
        setDownloading(true);
        try {
            const qrDataUrl = await QRCode.toDataURL(ticket.qrToken, {
                width: 300,
                margin: 1,
                color: { dark: '#111827', light: '#ffffff' },
            });

            const doc = new jsPDF({ unit: 'mm', format: 'a4' });
            const pageW = doc.internal.pageSize.getWidth();
            let y = 20;

            // Header
            doc.setFillColor(37, 99, 235);
            doc.rect(0, 0, pageW, 30, 'F');
            doc.setTextColor(255, 255, 255);
            doc.setFontSize(18);
            doc.setFont('helvetica', 'bold');
            doc.text(event.title, 15, 20);

            y = 45;
            doc.setTextColor(30, 30, 30);

            // Ticket info
            doc.setFontSize(10);
            doc.setFont('helvetica', 'bold');
            doc.setTextColor(100, 100, 100);
            doc.text(`TICKET ${index + 1} — ${ticket.ticketTypeName.toUpperCase()}`, 15, y);
            y += 8;

            doc.setFontSize(11);
            const rows: [string, string][] = [
                ['Date & Time', formatDate(event.startDatetime)],
                ['Venue', `${event.venue.name}, ${event.venue.city}`],
                ['Ticket Holder', buyerName],
                ['Email', buyerEmail],
                ['Ticket Type', ticket.ticketTypeName],
                ['Ticket ID', String(ticket.ticketId)],
            ];
            for (const [label, value] of rows) {
                doc.setFont('helvetica', 'bold');
                doc.setTextColor(30, 30, 30);
                doc.text(`${label}:`, 15, y);
                doc.setFont('helvetica', 'normal');
                doc.text(value, 65, y);
                y += 7;
            }

            y += 6;
            doc.setDrawColor(200, 200, 200);
            doc.setLineDashPattern([3, 3], 0);
            doc.line(15, y, pageW - 15, y);
            doc.setLineDashPattern([], 0);
            y += 8;

            doc.setFontSize(10);
            doc.setFont('helvetica', 'bold');
            doc.setTextColor(100, 100, 100);
            doc.text('SCAN AT ENTRANCE', 15, y);
            y += 4;
            doc.addImage(qrDataUrl, 'PNG', 15, y, 55, 55);

            doc.setTextColor(150, 150, 150);
            doc.setFont('helvetica', 'normal');
            doc.setFontSize(8);
            doc.text(ticket.qrToken, 15, y + 59);

            doc.save(`ticket-${ticket.ticketId}.pdf`);
        } finally {
            setDownloading(false);
        }
    }

    return (
        <Paper
            elevation={0}
            sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 2, overflow: 'hidden' }}
        >
            {/* Mini header */}
            <Box sx={{ bgcolor: 'primary.main', px: 2, py: 1.5 }}>
                <Typography variant='body2' sx={{ color: 'white', fontWeight: 700 }}>
                    Ticket {index + 1} — {ticket.ticketTypeName}
                </Typography>
            </Box>

            <Stack direction={{ xs: 'column', sm: 'row' }} sx={{ p: 2, gap: 2, alignItems: 'center' }}>
                {/* QR code */}
                <Box sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1.5, p: 1, bgcolor: 'white', flexShrink: 0 }}>
                    <canvas ref={canvasRef} />
                </Box>

                {/* Details + download */}
                <Stack sx={{ flex: 1, gap: 1 }}>
                    <Typography variant='caption' color='text.secondary'>
                        Ticket ID: #{ticket.ticketId}
                    </Typography>
                    <Typography variant='caption' color='text.secondary' sx={{ wordBreak: 'break-all' }}>
                        Token: {ticket.qrToken}
                    </Typography>
                    <Button
                        variant='outlined'
                        size='small'
                        startIcon={downloading ? <CircularProgress size={14} /> : <DownloadIcon />}
                        onClick={handleDownload}
                        disabled={downloading}
                        sx={{ mt: 1, textTransform: 'none', fontWeight: 600, alignSelf: 'flex-start' }}
                    >
                        {downloading ? 'Preparing…' : 'Download PDF'}
                    </Button>
                </Stack>
            </Stack>
        </Paper>
    );
}

export function TicketConfirmationPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const state = location.state as ConfirmationState | null;

    const [tickets, setTickets] = useState<TicketResponse[]>([]);
    const [loadingTickets, setLoadingTickets] = useState(true);
    const [ticketsError, setTicketsError] = useState<string | null>(null);

    if (!state) return <Navigate to='/' replace />;
    const { reservation, buyerName, buyerEmail, event } = state;

    useEffect(() => {
        getTicketsByPurchase(reservation.purchaseId)
            .then(setTickets)
            .catch((err) => setTicketsError(err.message))
            .finally(() => setLoadingTickets(false));
    }, [reservation.purchaseId]);

    return (
        <Box>
            <Header />
            <Container maxWidth='sm' sx={{ py: 6 }}>
                {/* Success banner */}
                <Stack spacing={1} sx={{ alignItems: 'center', mb: 4, textAlign: 'center' }}>
                    <CheckCircleIcon sx={{ fontSize: 52, color: '#06d373' }} />
                    <Typography variant='h4' sx={{ fontWeight: 800 }}>You're going!</Typography>
                    <Typography color='text.secondary'>
                        Your tickets have been confirmed. Show the QR code at the entrance.
                    </Typography>
                </Stack>

                {/* Order summary */}
                <Paper elevation={0} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 3, overflow: 'hidden', mb: 3 }}>
                    <Box
                        sx={{
                            height: 100,
                            bgcolor: 'primary.main',
                            backgroundImage: event.images?.[0]
                                ? `linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.55)), url(${event.images[0]})`
                                : undefined,
                            backgroundSize: 'cover',
                            backgroundPosition: 'center',
                            display: 'flex',
                            alignItems: 'flex-end',
                            px: 3,
                            pb: 2,
                        }}
                    >
                        <Typography variant='h5' sx={{ fontWeight: 800, color: 'white' }}>{event.title}</Typography>
                    </Box>

                    <Box sx={{ p: 3 }}>
                        <Stack spacing={1.5} sx={{ mb: 2 }}>
                            <DetailRow icon={<CalendarTodayIcon fontSize='small' />} label='Date & Time' value={formatDate(event.startDatetime)} />
                            <DetailRow icon={<LocationOnIcon fontSize='small' />} label='Venue' value={`${event.venue.name}, ${event.venue.city}`} />
                            <DetailRow icon={<PersonIcon fontSize='small' />} label='Ticket holder' value={buyerName} />
                            <DetailRow icon={<EmailIcon fontSize='small' />} label='Email' value={buyerEmail} />
                        </Stack>

                        <Divider sx={{ my: 2 }} />

                        <Stack spacing={1}>
                            {reservation.tickets.map((t) => (
                                <Stack key={t.ticketTypeId} direction='row' sx={{ justifyContent: 'space-between' }}>
                                    <Stack direction='row' spacing={1} sx={{ alignItems: 'center' }}>
                                        <ConfirmationNumberIcon fontSize='small' sx={{ color: 'text.secondary' }} />
                                        <Typography variant='body2'>{t.quantity} × {t.ticketTypeName}</Typography>
                                    </Stack>
                                    <Typography variant='body2' sx={{ fontWeight: 600 }}>
                                        €{(Number(t.pricePerTicket) * t.quantity).toFixed(2)}
                                    </Typography>
                                </Stack>
                            ))}
                            <Divider />
                            <Stack direction='row' sx={{ justifyContent: 'space-between' }}>
                                <Typography variant='body1' sx={{ fontWeight: 700 }}>Total</Typography>
                                <Typography variant='body1' sx={{ fontWeight: 700, color: 'primary.main' }}>
                                    €{Number(reservation.totalPrice).toFixed(2)}
                                </Typography>
                            </Stack>
                        </Stack>
                    </Box>
                </Paper>

                {/* Individual tickets */}
                <Typography variant='h6' sx={{ fontWeight: 700, mb: 2 }}>Your Tickets</Typography>

                {loadingTickets && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                        <CircularProgress />
                    </Box>
                )}

                {ticketsError && <Alert severity='error' sx={{ mb: 2 }}>{ticketsError}</Alert>}

                {!loadingTickets && !ticketsError && (
                    <Stack spacing={2} sx={{ mb: 3 }}>
                        {tickets.map((ticket, i) => (
                            <TicketCard
                                key={ticket.ticketId}
                                ticket={ticket}
                                index={i}
                                event={event}
                                buyerName={buyerName}
                                buyerEmail={buyerEmail}
                            />
                        ))}
                    </Stack>
                )}

                <Button
                    variant='outlined'
                    fullWidth
                    size='large'
                    startIcon={<HomeIcon />}
                    onClick={() => navigate('/')}
                    sx={{ fontWeight: 600, py: 1.5 }}
                >
                    Back to Events
                </Button>
            </Container>
            <Footer />
        </Box>
    );
}
