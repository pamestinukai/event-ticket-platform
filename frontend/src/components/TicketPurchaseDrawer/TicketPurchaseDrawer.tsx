import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Divider,
    Drawer,
    IconButton,
    Paper,
    Stack,
    TextField,
    Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import ConfirmationNumberIcon from '@mui/icons-material/ConfirmationNumber';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getTicketTypes } from '../../api/ticketTypes';
import { reserveTickets, confirmReservation, cancelReservation } from '../../api/tickets';
import type { TicketTypeResponse } from '../../types/TicketType';
import type { EventResponse } from '../../types/EventResponse';
import type { TicketReservationResponse } from '../../types/TicketReservation';

interface TicketPurchaseDrawerProps {
    open: boolean;
    onClose: () => void;
    event: EventResponse;
}

// quantity per ticketTypeId
type QuantityMap = Record<number, number>;

export function TicketPurchaseDrawer({
                                         open,
                                         onClose,
                                         event,
                                     }: TicketPurchaseDrawerProps) {
    const navigate = useNavigate();
    const eventId = Number(event.eventId);

    const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);
    const [loadingTypes, setLoadingTypes] = useState(false);
    const [typesError, setTypesError] = useState<string | null>(null);

    const [quantities, setQuantities] = useState<QuantityMap>({});
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');

    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    useEffect(() => {
        if (!open) return;
        setLoadingTypes(true);
        setTypesError(null);
        getTicketTypes(eventId)
            .then((types) => {
                setTicketTypes(types);
                // initialise all quantities to 0
                const init: QuantityMap = {};
                types.forEach((t) => { init[t.id] = 0; });
                setQuantities(init);
            })
            .catch((err) => setTypesError(err.message))
            .finally(() => setLoadingTypes(false));
    }, [open, eventId]);

    useEffect(() => {
        if (!open) {
            setQuantities({});
            setName('');
            setEmail('');
            setSubmitError(null);
        }
    }, [open]);

    function setQty(typeId: number, delta: number, max: number) {
        setQuantities((prev) => {
            const next = (prev[typeId] ?? 0) + delta;
            return { ...prev, [typeId]: Math.max(0, Math.min(max, next)) };
        });
    }

    const selectedItems = ticketTypes
        .map((t) => ({ type: t, qty: quantities[t.id] ?? 0 }))
        .filter((x) => x.qty > 0);

    const totalPrice = selectedItems.reduce(
        (sum, { type, qty }) => sum + Number(type.price) * qty,
        0,
    );
    const totalTickets = selectedItems.reduce((sum, { qty }) => sum + qty, 0);
    const canConfirm = totalTickets > 0 && name.trim() !== '' && email.trim() !== '';

    async function handleConfirmPurchase() {
        if (!canConfirm) return;
        setSubmitting(true);
        setSubmitError(null);
        let reservation: TicketReservationResponse | null = null;
        try {
            reservation = await reserveTickets({
                eventId,
                tickets: selectedItems.map(({ type, qty }) => ({
                    ticketTypeId: type.id,
                    quantity: qty,
                })),
            });
            await confirmReservation(reservation.purchaseId, email, name);
            onClose();
            navigate('/ticket/confirmation', {
                state: {
                    reservation,
                    buyerName: name,
                    buyerEmail: email,
                    event: {
                        title: event.title,
                        startDatetime: event.startDatetime,
                        endDatetime: event.endDatetime,
                        venue: event.venue,
                        auditoriumName: event.auditoriumName,
                        images: event.images,
                    },
                },
            });
        } catch (err) {
            if (reservation) {
                cancelReservation(reservation.purchaseId).catch(() => {});
            }
            setSubmitError(err instanceof Error ? err.message : 'Purchase failed');
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <Drawer
            anchor='right'
            open={open}
            onClose={onClose}
            PaperProps={{
                sx: { width: { xs: '100vw', sm: 420 }, p: 0, display: 'flex', flexDirection: 'column' },
            }}
        >
            {/* Header */}
            <Stack
                direction='row'
                sx={{
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    px: 3,
                    py: 2.5,
                    borderBottom: '1px solid',
                    borderColor: 'divider',
                    flexShrink: 0,
                }}
            >
                <Stack direction='row' spacing={1} sx={{ alignItems: 'center' }}>
                    <ConfirmationNumberIcon sx={{ color: 'primary.main' }} />
                    <Typography variant='h6' sx={{ fontWeight: 700 }}>
                        Get Tickets
                    </Typography>
                </Stack>
                <IconButton onClick={onClose} size='small'>
                    <CloseIcon />
                </IconButton>
            </Stack>

            {/* Event summary */}
            <Box sx={{ px: 3, py: 2, bgcolor: 'background.default', flexShrink: 0 }}>
                <Typography variant='subtitle1' sx={{ fontWeight: 700, lineHeight: 1.3 }}>
                    {event.title}
                </Typography>
                <Typography variant='body2' color='text.secondary' sx={{ mt: 0.5 }}>
                    {new Date(event.startDatetime).toLocaleDateString('lt-LT', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                    })}
                </Typography>
                <Typography variant='body2' color='text.secondary'>
                    {event.venue.name}, {event.venue.city}
                </Typography>
            </Box>

            <Divider />

            {/* Body */}
            <Box sx={{ flex: 1, overflowY: 'auto', px: 3, py: 3 }}>
                {loadingTypes && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
                        <CircularProgress />
                    </Box>
                )}

                {typesError && <Alert severity='error'>{typesError}</Alert>}

                {!loadingTypes && !typesError && ticketTypes.length === 0 && (
                    <Typography color='text.secondary'>No tickets available for this event.</Typography>
                )}

                {!loadingTypes && !typesError && ticketTypes.length > 0 && (
                    <Stack spacing={3}>
                        {/* Ticket types with per-type quantity */}
                        <Box>
                            <Typography variant='body2' sx={{ fontWeight: 700, mb: 1.5, color: 'text.secondary', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                                Available ticket types
                            </Typography>
                            <Stack spacing={1.5}>
                                {ticketTypes.map((type) => {
                                    const qty = quantities[type.id] ?? 0;
                                    const maxQty = Math.min(type.availableQuantity, 10);
                                    const sold = type.availableQuantity === 0;
                                    return (
                                        <Paper
                                            key={type.id}
                                            elevation={0}
                                            sx={{
                                                border: '2px solid',
                                                borderColor: qty > 0 ? 'primary.main' : 'divider',
                                                borderRadius: 2,
                                                p: 2,
                                                opacity: sold ? 0.5 : 1,
                                                transition: 'border-color 0.15s',
                                                boxShadow: qty > 0 ? '0 0 0 4px rgba(37,99,235,0.1)' : 'none',
                                            }}
                                        >
                                            <Stack direction='row' sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
                                                <Box>
                                                    <Typography variant='body1' sx={{ fontWeight: 600 }}>
                                                        {type.name}
                                                    </Typography>
                                                    <Typography variant='body2' color='text.secondary'>
                                                        {sold ? 'Sold out' : `${type.availableQuantity} left`}
                                                    </Typography>
                                                </Box>
                                                <Stack direction='row' sx={{ alignItems: 'center', gap: 1.5 }}>
                                                    <Typography variant='body1' sx={{ fontWeight: 700, color: 'primary.main', minWidth: 60, textAlign: 'right' }}>
                                                        €{Number(type.price).toFixed(2)}
                                                    </Typography>
                                                    {!sold && (
                                                        <Stack direction='row' sx={{ alignItems: 'center', gap: 0.5 }}>
                                                            <IconButton
                                                                size='small'
                                                                onClick={() => setQty(type.id, -1, maxQty)}
                                                                disabled={qty <= 0}
                                                                sx={{ border: '1px solid', borderColor: 'divider', width: 28, height: 28 }}
                                                            >
                                                                <RemoveIcon sx={{ fontSize: 14 }} />
                                                            </IconButton>
                                                            <Typography variant='body1' sx={{ fontWeight: 700, minWidth: 20, textAlign: 'center' }}>
                                                                {qty}
                                                            </Typography>
                                                            <IconButton
                                                                size='small'
                                                                onClick={() => setQty(type.id, 1, maxQty)}
                                                                disabled={qty >= maxQty}
                                                                sx={{ border: '1px solid', borderColor: 'divider', width: 28, height: 28 }}
                                                            >
                                                                <AddIcon sx={{ fontSize: 14 }} />
                                                            </IconButton>
                                                        </Stack>
                                                    )}
                                                </Stack>
                                            </Stack>
                                        </Paper>
                                    );
                                })}
                            </Stack>
                        </Box>

                        <Divider />

                        {/* Buyer details */}
                        <Box>
                            <Typography variant='body2' sx={{ fontWeight: 700, mb: 1.5, color: 'text.secondary', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                                Your details
                            </Typography>
                            <Stack spacing={2}>
                                <TextField
                                    label='Full name'
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    fullWidth
                                    size='small'
                                    required
                                />
                                <TextField
                                    label='Email address'
                                    type='email'
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    fullWidth
                                    size='small'
                                    required
                                    helperText='Your ticket will be associated with this email'
                                />
                            </Stack>
                        </Box>

                        {submitError && <Alert severity='error'>{submitError}</Alert>}
                    </Stack>
                )}
            </Box>

            {/* Footer */}
            {!loadingTypes && !typesError && ticketTypes.length > 0 && (
                <Box
                    sx={{
                        px: 3,
                        py: 2.5,
                        borderTop: '1px solid',
                        borderColor: 'divider',
                        flexShrink: 0,
                        bgcolor: 'background.paper',
                    }}
                >
                    <Stack direction='row' sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Box>
                            <Typography variant='body2' color='text.secondary'>
                                {totalTickets > 0 ? `${totalTickets} ticket${totalTickets > 1 ? 's' : ''} selected` : 'No tickets selected'}
                            </Typography>
                            <Typography variant='h5' sx={{ fontWeight: 800 }}>
                                €{totalPrice.toFixed(2)}
                            </Typography>
                        </Box>
                    </Stack>
                    <Button
                        variant='contained'
                        fullWidth
                        size='large'
                        disabled={submitting || !canConfirm}
                        onClick={handleConfirmPurchase}
                        sx={{
                            bgcolor: '#06d373',
                            color: 'black',
                            fontWeight: 700,
                            fontSize: '1rem',
                            py: 1.5,
                            '&:hover': { bgcolor: '#04b862' },
                            '&:disabled': { bgcolor: '#ccc', color: '#888' },
                        }}
                    >
                        {submitting ? (
                            <CircularProgress size={22} sx={{ color: 'black' }} />
                        ) : (
                            `Confirm Purchase · €${totalPrice.toFixed(2)}`
                        )}
                    </Button>
                </Box>
            )}
        </Drawer>
    );
}
