import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Box,
    Container,
    Divider,
    Stack,
    Typography,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import EmailIcon from '@mui/icons-material/Email';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import PaymentIcon from '@mui/icons-material/Payment';
import QrCodeIcon from '@mui/icons-material/QrCode2';
import LockIcon from '@mui/icons-material/Lock';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import { Header } from '../../components/Header/Header';
import { Footer } from '../../components/Footer/Footer';

const FAQ_ITEMS = [
    {
        icon: <EmailIcon fontSize="small" />,
        question: 'How will I receive my ticket?',
        answer:
            'After a successful purchase your ticket(s) are sent directly to the email address you provided at checkout. Each ticket contains a unique QR code that will be scanned at the entrance.',
    },
    {
        icon: <NotificationsActiveIcon fontSize="small" />,
        question: 'Will I be reminded about an upcoming event?',
        answer:
            'Yes. You will receive an automatic email reminder 24 hours before the event starts so you never miss something you have paid for.',
    },
    {
        icon: <PaymentIcon fontSize="small" />,
        question: 'How is payment handled?',
        answer:
            'All payments are processed securely through Stripe, a globally trusted payment provider. We never store your card details - they go directly to Stripe\'s encrypted servers.',
    },
    {
        icon: <QrCodeIcon fontSize="small" />,
        question: 'How does entrance verification work?',
        answer:
            'Every ticket carries a unique QR code. Event staff scan the code at the door using our built-in scanner tool. Each code can only be used once, preventing duplicate entry.',
    },
    {
        icon: <LockIcon fontSize="small" />,
        question: 'Is my personal data safe?',
        answer:
            'We collect only the information necessary to process your ticket purchase. Your data is stored securely and is never sold to third parties.',
    },
    {
        icon: <SupportAgentIcon fontSize="small" />,
        question: 'What if I have a problem with my ticket?',
        answer:
            'If you experience any issue with your ticket or purchase, please contact the event organiser directly. Their contact details can be found on the event page.',
    },
];

export function AboutPage() {
    return (
        <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <Header />

            <Container maxWidth="md" sx={{ py: 6, flex: 1 }}>
                {/* About section */}
                <Typography variant="h4" component="h1" sx={{ fontWeight: 700, mb: 1 }}>
                    About
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                    Why this platform exists and what it offers
                </Typography>

                <Divider sx={{ mb: 4 }} />

                <Stack spacing={2} sx={{ mb: 6 }}>
                    <Typography variant="body1" color="text.secondary" sx={{ lineHeight: 1.8 }}>
                        Currently, event organizers do not have a single tool to manage ticket sales. Some use different
                        platforms or social networks. Others sell tickets manually. Buyers also do not have one reliable
                        place to search for events and purchase tickets. As a result, organizers have to spend more time
                        on selling and managing tickets, and buyers face an inconvenient purchase process.
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ lineHeight: 1.8 }}>
                        The aim is to create a modern online ticket sales platform that meets the needs of both
                        organizers and buyers. Organizers can register and create events, set ticket prices, and monitor
                        sales in real time. Buyers can conveniently browse events, pay securely online, and receive a
                        digital ticket with a unique QR code for entrance verification.
                    </Typography>
                </Stack>

                {/* FAQ section */}
                <Typography variant="h5" component="h2" sx={{ fontWeight: 700, mb: 1 }}>
                    Frequently Asked Questions
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                    Everything you need to know before buying a ticket
                </Typography>

                <Divider sx={{ mb: 3 }} />

                <Stack spacing={1}>
                    {FAQ_ITEMS.map(({ icon, question, answer }) => (
                        <Accordion
                            key={question}
                            elevation={0}
                            disableGutters
                            sx={{
                                border: '1px solid',
                                borderColor: 'divider',
                                borderRadius: '8px !important',
                                '&:before': { display: 'none' },
                                '&.Mui-expanded': { borderColor: 'primary.main' },
                            }}
                        >
                            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                                <Stack direction="row" spacing={1.5} alignItems="center">
                                    <Box sx={{ color: 'primary.main', display: 'flex' }}>{icon}</Box>
                                    <Typography variant="body1" sx={{ fontWeight: 600 }}>
                                        {question}
                                    </Typography>
                                </Stack>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Typography variant="body2" color="text.secondary" sx={{ lineHeight: 1.8, pl: 3.5 }}>
                                    {answer}
                                </Typography>
                            </AccordionDetails>
                        </Accordion>
                    ))}
                </Stack>
            </Container>

            <Footer />
        </Box>
    );
}
