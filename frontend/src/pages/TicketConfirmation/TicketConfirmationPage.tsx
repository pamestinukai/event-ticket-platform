import {
    Box,
    Button,
    Container,
    Divider,
    Paper,
    Stack,
    Typography,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import HomeIcon from '@mui/icons-material/Home';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useLocation, useNavigate, Navigate } from 'react-router-dom';
import { useEffect, useRef } from 'react';
import QRCode from 'qrcode';
import { Header } from '../../components/Header/Header';
import { Footer } from '../../components/Footer/Footer';
import type { TicketReservationResponse } from '../../types/TicketReservation';
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

function DetailRow({
                       icon,
                       label,
                       value,
                   }: {
    icon: React.ReactNode;
    label: string;
    value: string;
}) {
    return (
        <Stack direction='row' spacing={1.5} sx={{ alignItems: 'flex-start' }}>
            <Box sx={{ color: 'text.secondary', mt: 0.2, flexShrink: 0 }}>{icon}</Box>
            <Box>
                <Typography variant='caption' color='text.secondary' sx={{ display: 'block', lineHeight: 1.2 }}>
                    {label}
                </Typography>
                <Typography variant='body2' sx={{ fontWeight: 600 }}>
                    {value}
                </Typography>
            </Box>
        </Stack>
    );
}

export function TicketConfirmationPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const state = location.state as ConfirmationState | null;
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const ticketRef = useRef<HTMLDivElement>(null);

    // Guard — if navigated directly without state, redirect home
    if (!state) return <Navigate to='/' replace />;

    const { reservation, buyerName, buyerEmail, event } = state;

    // QR code encodes a unique string: purchaseId + eventId
    const qrValue = `TICKET:${reservation.purchaseId}:${reservation.eventId}`;

    useEffect(() => {
        if (!canvasRef.current) return;
        QRCode.toCanvas(canvasRef.current, qrValue, {
            width: 180,
            margin: 1,
            color: { dark: '#111827', light: '#ffffff' },
        });
    }, [qrValue]);

    async function handleDownload() {
        // Dynamically import html2canvas only when needed
        const { default: html2canvas } = await import('html2canvas');
        if (!ticketRef.current) return;
        const canvas = await html2canvas(ticketRef.current, {
            scale: 2,
            backgroundColor: '#ffffff',
            useCORS: true,
        });
        const link = document.createElement('a');
        link.download = `ticket-${reservation.purchaseId}.png`;
        link.href = canvas.toDataURL('image/png');
        link.click();
    }

    return (
        <Box>
            <Header />
            <Container maxWidth='sm' sx={{ py: 6 }}>
                {/* Success banner */}
                <Stack spacing={1} sx={{ alignItems: 'center', mb: 4, textAlign: 'center' }}>
                    <CheckCircleIcon sx={{ fontSize: 52, color: '#06d373' }} />
                    <Typography variant='h4' sx={{ fontWeight: 800 }}>
                        You're going!
                    </Typography>
                    <Typography color='text.secondary'>
                        Your ticket has been confirmed. Show the QR code at the entrance.
                    </Typography>
                </Stack>

                {/* The ticket card (captured for download) */}
                <Paper
                    ref={ticketRef}
                    elevation={0}
                    sx={{
                        border: '1px solid',
                        borderColor: 'divider',
                        borderRadius: 3,
                        overflow: 'hidden',
                        mb: 3,
                    }}
                >
                    {/* Ticket header with event image or color band */}
                    <Box
                        sx={{
                            height: 120,
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
                        <Box>
                            <Typography variant='h5' sx={{ fontWeight: 800, color: 'white', lineHeight: 1.2 }}>
                                {event.title}
                            </Typography>
                        </Box>
                    </Box>

                    {/* Dashed cut-line */}
                    <Box
                        sx={{
                            borderTop: '2px dashed',
                            borderColor: 'divider',
                            mx: 0,
                            position: 'relative',
                            '&::before': {
                                content: '""',
                                position: 'absolute',
                                left: -14,
                                top: -10,
                                width: 20,
                                height: 20,
                                borderRadius: '50%',
                                bgcolor: 'background.default',
                                border: '1px solid',
                                borderColor: 'divider',
                            },
                            '&::after': {
                                content: '""',
                                position: 'absolute',
                                right: -14,
                                top: -10,
                                width: 20,
                                height: 20,
                                borderRadius: '50%',
                                bgcolor: 'background.default',
                                border: '1px solid',
                                borderColor: 'divider',
                            },
                        }}
                    />

                    {/* Ticket body */}
                    <Stack direction={{ xs: 'column', sm: 'row' }} sx={{ p: 3, gap: 3 }}>
                        {/* Left: event details */}
                        <Box sx={{ flex: 1 }}>
                            <Stack spacing={2}>
                                <DetailRow
                                    icon={<CalendarTodayIcon fontSize='small' />}
                                    label='Date & Time'
                                    value={formatDate(event.startDatetime)}
                                />
                                <DetailRow
                                    icon={<LocationOnIcon fontSize='small' />}
                                    label='Venue'
                                    value={`${event.venue.name}, ${event.venue.city}`}
                                />
                                <DetailRow
                                    icon={<PersonIcon fontSize='small' />}
                                    label='Ticket holder'
                                    value={buyerName}
                                />
                                <DetailRow
                                    icon={<EmailIcon fontSize='small' />}
                                    label='Email'
                                    value={buyerEmail}
                                />
                            </Stack>

                            <Divider sx={{ my: 2 }} />

                            {/* Ticket breakdown */}
                            <Stack spacing={1}>
                                {reservation.tickets.map((t) => (
                                    <Stack
                                        key={t.ticketTypeId}
                                        direction='row'
                                        sx={{ justifyContent: 'space-between', alignItems: 'center' }}
                                    >
                                        <Stack direction='row' spacing={1} sx={{ alignItems: 'center' }}>
                                            <ConfirmationNumberIcon fontSize='small' sx={{ color: 'text.secondary' }} />
                                            <Typography variant='body2'>
                                                {t.quantity} × {t.ticketTypeName}
                                            </Typography>
                                        </Stack>
                                        <Typography variant='body2' sx={{ fontWeight: 600 }}>
                                            €{(Number(t.pricePerTicket) * t.quantity).toFixed(2)}
                                        </Typography>
                                    </Stack>
                                ))}
                                <Divider />
                                <Stack direction='row' sx={{ justifyContent: 'space-between' }}>
                                    <Typography variant='body1' sx={{ fontWeight: 700 }}>
                                        Total
                                    </Typography>
                                    <Typography variant='body1' sx={{ fontWeight: 700, color: 'primary.main' }}>
                                        €{Number(reservation.totalPrice).toFixed(2)}
                                    </Typography>
                                </Stack>
                            </Stack>
                        </Box>

                        {/* Right: QR code */}
                        <Stack
                            sx={{
                                alignItems: 'center',
                                justifyContent: 'center',
                                flexShrink: 0,
                                gap: 1,
                            }}
                        >
                            <Box
                                sx={{
                                    border: '1px solid',
                                    borderColor: 'divider',
                                    borderRadius: 2,
                                    p: 1.5,
                                    bgcolor: 'white',
                                }}
                            >
                                <canvas ref={canvasRef} />
                            </Box>
                            <Typography variant='caption' color='text.secondary' sx={{ textAlign: 'center' }}>
                                #{reservation.purchaseId}
                            </Typography>
                        </Stack>
                    </Stack>
                </Paper>

                {/* Actions */}
                <Stack spacing={1.5}>
                    <Button
                        variant='contained'
                        fullWidth
                        size='large'
                        startIcon={<DownloadIcon />}
                        onClick={handleDownload}
                        sx={{ fontWeight: 700, py: 1.5 }}
                    >
                        Download Ticket
                    </Button>
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
                </Stack>
            </Container>
            <Footer />
        </Box>
    );
}
