import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogContent,
    DialogTitle,
    Divider,
    IconButton,
    Stack,
    Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import WarningIcon from '@mui/icons-material/Warning';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import { useEffect, useRef, useState } from 'react';
import { BrowserQRCodeReader } from '@zxing/browser';
import { checkInTicket, validateTicket } from '../../api/tickets';
import type { TicketValidationResponse } from '../../api/tickets';

interface Props {
    open: boolean;
    onClose: () => void;
}

type ScanPhase = 'scanning' | 'preview' | 'checking-in' | 'result';

interface ScanResult {
    data: TicketValidationResponse;
    checkedIn: boolean;
    error?: string;
}

export function QrScannerDialog({ open, onClose }: Props) {
    const videoRef = useRef<HTMLVideoElement>(null);
    const readerRef = useRef<BrowserQRCodeReader | null>(null);
    const controlsRef = useRef<{ stop: () => void } | null>(null);

    const [phase, setPhase] = useState<ScanPhase>('scanning');
    const [scanError, setScanError] = useState<string | null>(null);
    const [preview, setPreview] = useState<TicketValidationResponse | null>(null);
    const [result, setResult] = useState<ScanResult | null>(null);
    const [cameraError, setCameraError] = useState<string | null>(null);

    // Start/stop camera when dialog opens/closes
    useEffect(() => {
        if (!open) {
            stopCamera();
            resetState();
            return;
        }
        startCamera();
        return () => stopCamera();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [open]);

    function stopCamera() {
        controlsRef.current?.stop();
        controlsRef.current = null;
        readerRef.current = null;
    }

    function resetState() {
        setPhase('scanning');
        setScanError(null);
        setPreview(null);
        setResult(null);
        setCameraError(null);
    }

    async function startCamera() {
        try {
            const reader = new BrowserQRCodeReader();
            readerRef.current = reader;

            const devices = await BrowserQRCodeReader.listVideoInputDevices();
            if (devices.length === 0) {
                setCameraError('No camera found on this device.');
                return;
            }

            // Prefer back camera on mobile
            const device =
                devices.find((d) => /back|rear|environment/i.test(d.label)) ?? devices[0];

            if (!videoRef.current) return;

            const controls = await reader.decodeFromVideoDevice(
                device.deviceId,
                videoRef.current,
                async (result, err) => {
                    if (!result) return;
                    // Stop scanning once we have a result
                    controls.stop();

                    const token = result.getText();
                    setScanError(null);

                    try {
                        const validation = await validateTicket(token);
                        setPreview(validation);
                        setPhase('preview');
                    } catch (e) {
                        setScanError(e instanceof Error ? e.message : 'Invalid ticket');
                        setPhase('scanning');
                        // Restart scanning after brief pause
                        setTimeout(() => startCamera(), 2000);
                    }
                },
            );
            controlsRef.current = controls;
        } catch (e) {
            if (e instanceof Error && e.name === 'NotAllowedError') {
                setCameraError('Camera access denied. Please allow camera permission.');
            } else {
                setCameraError('Could not start camera.');
            }
        }
    }

    async function handleCheckIn() {
        if (!preview) return;
        setPhase('checking-in');
        try {
            const data = await checkInTicket(preview.token);
            setResult({ data, checkedIn: true });
        } catch (e) {
            setResult({
                data: preview,
                checkedIn: false,
                error: e instanceof Error ? e.message : 'Check-in failed',
            });
        }
        setPhase('result');
    }

    function handleScanAnother() {
        resetState();
        startCamera();
    }

    const statusColor = (status: string) => {
        switch (status) {
            case 'VALID': return 'success.main';
            case 'CHECKED_IN': return 'warning.main';
            case 'CANCELED':
            case 'REFUNDED': return 'error.main';
            default: return 'text.secondary';
        }
    };

    const statusLabel = (status: string) => {
        switch (status) {
            case 'VALID': return 'Valid - not yet checked in';
            case 'CHECKED_IN': return 'Already checked in';
            case 'RESERVED': return 'Reserved - payment not completed';
            case 'CANCELED': return 'Canceled';
            case 'REFUNDED': return 'Refunded';
            default: return status;
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth='xs' fullWidth>
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
                <Stack direction='row' spacing={1} alignItems='center'>
                    <QrCodeScannerIcon color='primary' />
                    <span>Ticket Scanner</span>
                </Stack>
                <IconButton size='small' onClick={onClose}><CloseIcon /></IconButton>
            </DialogTitle>

            <DialogContent sx={{ px: 3, pb: 3, pt: 0 }}>

                {/* ── SCANNING PHASE ── */}
                {(phase === 'scanning') && (
                    <Stack spacing={2}>
                        {cameraError ? (
                            <Alert severity='error'>{cameraError}</Alert>
                        ) : (
                            <>
                                <Box
                                    sx={{
                                        position: 'relative',
                                        width: '100%',
                                        aspectRatio: '1',
                                        bgcolor: 'black',
                                        borderRadius: 2,
                                        overflow: 'hidden',
                                    }}
                                >
                                    <video
                                        ref={videoRef}
                                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                                        muted
                                        playsInline
                                    />
                                    {/* Scan frame overlay */}
                                    <Box
                                        sx={{
                                            position: 'absolute',
                                            inset: 0,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            pointerEvents: 'none',
                                        }}
                                    >
                                        <Box
                                            sx={{
                                                width: '60%',
                                                aspectRatio: '1',
                                                border: '3px solid',
                                                borderColor: 'primary.main',
                                                borderRadius: 2,
                                                boxShadow: '0 0 0 9999px rgba(0,0,0,0.45)',
                                            }}
                                        />
                                    </Box>
                                </Box>
                                <Typography variant='body2' color='text.secondary' textAlign='center'>
                                    Point the camera at a ticket QR code
                                </Typography>
                                {scanError && <Alert severity='error'>{scanError}</Alert>}
                            </>
                        )}
                    </Stack>
                )}

                {/* ── PREVIEW PHASE ── */}
                {phase === 'preview' && preview && (
                    <Stack spacing={2}>
                        <Stack direction='row' spacing={1} alignItems='center'>
                            {preview.status === 'VALID'
                                ? <CheckCircleIcon sx={{ color: 'success.main' }} />
                                : <WarningIcon sx={{ color: 'warning.main' }} />
                            }
                            <Typography variant='subtitle1' fontWeight={700}>
                                {statusLabel(preview.status)}
                            </Typography>
                        </Stack>

                        <Divider />

                        <TicketInfoRows ticket={preview} statusColor={statusColor(preview.status)} />

                        <Stack direction='row' spacing={1} pt={1}>
                            <Button variant='outlined' fullWidth onClick={handleScanAnother}>
                                Scan another
                            </Button>
                            <Button
                                variant='contained'
                                fullWidth
                                disabled={preview.status !== 'VALID'}
                                onClick={handleCheckIn}
                                color='success'
                            >
                                Check in
                            </Button>
                        </Stack>

                        {preview.status !== 'VALID' && (
                            <Alert severity='warning'>
                                This ticket cannot be checked in ({statusLabel(preview.status)}).
                            </Alert>
                        )}
                    </Stack>
                )}

                {/* ── CHECKING IN PHASE ── */}
                {phase === 'checking-in' && (
                    <Stack spacing={2} alignItems='center' py={3}>
                        <CircularProgress />
                        <Typography color='text.secondary'>Checking in ticket…</Typography>
                    </Stack>
                )}

                {/* ── RESULT PHASE ── */}
                {phase === 'result' && result && (
                    <Stack spacing={2}>
                        {result.checkedIn ? (
                            <Stack direction='row' spacing={1} alignItems='center'>
                                <CheckCircleIcon sx={{ color: 'success.main', fontSize: 32 }} />
                                <Box>
                                    <Typography variant='subtitle1' fontWeight={700} color='success.main'>
                                        Checked in successfully
                                    </Typography>
                                    <Typography variant='body2' color='text.secondary'>
                                        Ticket #{result.data.ticketId}
                                    </Typography>
                                </Box>
                            </Stack>
                        ) : (
                            <Stack direction='row' spacing={1} alignItems='center'>
                                <ErrorIcon sx={{ color: 'error.main', fontSize: 32 }} />
                                <Box>
                                    <Typography variant='subtitle1' fontWeight={700} color='error.main'>
                                        Check-in failed
                                    </Typography>
                                    <Typography variant='body2' color='text.secondary'>
                                        {result.error}
                                    </Typography>
                                </Box>
                            </Stack>
                        )}

                        <Divider />
                        <TicketInfoRows ticket={result.data} statusColor={statusColor(result.data.status)} />

                        <Button variant='contained' fullWidth onClick={handleScanAnother}>
                            Scan another ticket
                        </Button>
                    </Stack>
                )}

            </DialogContent>
        </Dialog>
    );
}

function TicketInfoRows({ ticket, statusColor }: { ticket: TicketValidationResponse; statusColor: string }) {
    return (
        <Stack spacing={1}>
            <InfoRow label='Event' value={ticket.eventTitle} />
            <InfoRow label='Date' value={ticket.eventDate} />
            <InfoRow label='Venue' value={ticket.venue} />
            <InfoRow label='Ticket type' value={ticket.ticketType} />
            <InfoRow label='Buyer' value={ticket.buyerName} />
            <InfoRow label='Status' value={ticket.status} valueColor={statusColor} />
            <InfoRow label='Ticket ID' value={`#${ticket.ticketId}`} />
        </Stack>
    );
}

function InfoRow({ label, value, valueColor }: { label: string; value: string; valueColor?: string }) {
    return (
        <Stack direction='row' justifyContent='space-between' alignItems='flex-start' spacing={1}>
            <Typography variant='body2' color='text.secondary' flexShrink={0}>
                {label}
            </Typography>
            <Typography variant='body2' fontWeight={600} textAlign='right' color={valueColor}>
                {value}
            </Typography>
        </Stack>
    );
}
