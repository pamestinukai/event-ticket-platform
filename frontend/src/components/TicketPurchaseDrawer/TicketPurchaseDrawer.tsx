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

type Step = 'select' | 'details' | 'confirming';

export function TicketPurchaseDrawer({
                                         open,
                                         onClose,
                                         event,
                                     }: TicketPurchaseDrawerProps) {
    const navigate = useNavigate();
    const eventId = Number(event.eventId);

    // Ticket types
    const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);
    const [loadingTypes, setLoadingTypes] = useState(false);
    const [typesError, setTypesError] = useState<string | null>(null);

    // Selection
    const [selectedTypeId, setSelectedTypeId] = useState<number | ''>('');
    const [quantity, setQuantity] = useState(1);

    // Buyer details
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');

    // Flow
    const [step, setStep] = useState<Step>('select');
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    // Load ticket types when drawer opens
    useEffect(() => {
        if (!open) return;
        setLoadingTypes(true);
        setTypesError(null);
        getTicketTypes(eventId)
            .then((types) => {
                setTicketTypes(types);
                if (types.length > 0) setSelectedTypeId(types[0].id);
            })
            .catch((err) => setTypesError(err.message))
            .finally(() => setLoadingTypes(false));
    }, [open, eventId]);

    // Reset when closed
    useEffect(() => {
        if (!open) {
            setStep('select');
            setQuantity(1);
            setName('');
            setEmail('');
            setSubmitError(null);
            setSelectedTypeId('');
        }
    }, [open]);

    const selectedType = ticketTypes.find((t) => t.id === selectedTypeId);
    const totalPrice = selectedType ? selectedType.price * quantity : 0;
    const maxQty = selectedType ? Math.min(selectedType.availableQuantity, 10) : 1;

    async function handleConfirmPurchase() {
        if (!selectedType || selectedTypeId === '') return;
        setSubmitting(true);
        setSubmitError(null);
        let reservation: TicketReservationResponse | null = null;
        try {
            reservation = await reserveTickets({
                eventId,
                tickets: [{ ticketTypeId: Number(selectedTypeId), quantity }],
            });
            await confirmReservation(reservation.purchaseId);
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
                        {/* Step 1: Ticket selection */}
                        <Box>
                            <Typography variant='body2' sx={{ fontWeight: 700, mb: 1.5, color: 'text.secondary', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                                Available ticket types
                            </Typography>
                            <Stack spacing={1.5}>
                                {ticketTypes.map((type) => (
                                    <Paper
                                        key={type.id}
                                        elevation={0}
                                        onClick={() => {
                                            if (type.availableQuantity > 0) {
                                                setSelectedTypeId(type.id);
                                                setQuantity(1);
                                            }
                                        }}
                                        sx={{
                                            border: '2px solid',
                                            borderColor: selectedTypeId === type.id ? 'primary.main' : 'divider',
                                            borderRadius: 2,
                                            p: 2,
                                            cursor: type.availableQuantity > 0 ? 'pointer' : 'not-allowed',
                                            opacity: type.availableQuantity === 0 ? 0.5 : 1,
                                            transition: 'border-color 0.15s, box-shadow 0.15s',
                                            boxShadow: selectedTypeId === type.id ? '0 0 0 4px rgba(37,99,235,0.1)' : 'none',
                                            '&:hover': type.availableQuantity > 0 ? { borderColor: 'primary.main' } : {},
                                        }}
                                    >
                                        <Stack direction='row' sx={{ justifyContent: 'space-between', alignItems: 'center' }}>
                                            <Box>
                                                <Typography variant='body1' sx={{ fontWeight: 600 }}>
                                                    {type.name}
                                                </Typography>
                                                <Typography variant='body2' color='text.secondary'>
                                                    {type.availableQuantity > 0
                                                        ? `${type.availableQuantity} left`
                                                        : 'Sold out'}
                                                </Typography>
                                            </Box>
                                            <Typography variant='h6' sx={{ fontWeight: 700, color: 'primary.main' }}>
                                                €{Number(type.price).toFixed(2)}
                                            </Typography>
                                        </Stack>
                                    </Paper>
                                ))}
                            </Stack>
                        </Box>

                        {/* Quantity selector */}
                        {selectedType && (
                            <Box>
                                <Typography variant='body2' sx={{ fontWeight: 700, mb: 1.5, color: 'text.secondary', textTransform: 'uppercase', fontSize: '0.7rem', letterSpacing: 1 }}>
                                    Quantity
                                </Typography>
                                <Stack direction='row' sx={{ alignItems: 'center', gap: 1 }}>
                                    <IconButton
                                        size='small'
                                        onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                                        disabled={quantity <= 1}
                                        sx={{ border: '1px solid', borderColor: 'divider' }}
                                    >
                                        <RemoveIcon fontSize='small' />
                                    </IconButton>
                                    <Typography variant='h6' sx={{ fontWeight: 700, minWidth: 32, textAlign: 'center' }}>
                                        {quantity}
                                    </Typography>
                                    <IconButton
                                        size='small'
                                        onClick={() => setQuantity((q) => Math.min(maxQty, q + 1))}
                                        disabled={quantity >= maxQty}
                                        sx={{ border: '1px solid', borderColor: 'divider' }}
                                    >
                                        <AddIcon fontSize='small' />
                                    </IconButton>
                                    <Typography variant='body2' color='text.secondary' sx={{ ml: 1 }}>
                                        max {maxQty}
                                    </Typography>
                                </Stack>
                            </Box>
                        )}

                        <Divider />

                        {/* Step 2: Buyer details */}
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

            {/* Footer: total + CTA */}
            {!loadingTypes && !typesError && selectedType && (
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
                                {quantity} × {selectedType.name}
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
                        disabled={submitting || !name.trim() || !email.trim()}
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
