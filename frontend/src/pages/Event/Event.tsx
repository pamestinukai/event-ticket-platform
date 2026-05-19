import {
    Avatar,
    Box,
    Button,
    CircularProgress,
    Container,
    Divider,
    Grid,
    Paper,
    Stack,
    Typography
} from "@mui/material";
import {useParams} from "react-router-dom";
import {getEventById} from "../../api/events.ts";
import {useEffect, useState} from "react";
import {EventThumbnail} from "../../components/EventThumbnail/EventThumbnail.tsx";
import {Footer} from "../../components/Footer/Footer.tsx";
import {Header} from "../../components/Header/Header.tsx";

import LocationOnIcon from '@mui/icons-material/LocationOn';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import MicIcon from '@mui/icons-material/Mic';
import BusinessIcon from '@mui/icons-material/Business';
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import {EventDetail} from "../../components/EventDetail/EventDetail.tsx";
import type {EventResponse} from "../../types/EventResponse.ts";
import {InfoCard} from "../../components/InfoCard/InfoCard.tsx";
import {ErrorScreen} from "../../components/ErrorScreen/ErrorScreen.tsx";
import {EventDescription} from "../../components/EventDescription/EventDescription.tsx";


function formatDate(date: string){
    return new Date(date).toLocaleDateString("lt-LT", {
        year: "numeric",
        day: "numeric",
        month: "numeric",
        hour: "numeric",
        minute: "numeric"
    })
}

function formatTime(date: string){
    return new Date(date).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
    });
}

export function EventPage(){
    const { id } = useParams();
    const [event, setEvent] = useState<EventResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const borderRadius = 10;

    useEffect(() => {
       if(!id) return;

       getEventById((id))
           .then(data => setEvent(data))
           .catch(err => setError(err.message));
    }, [id]);

    if (error) return <ErrorScreen error={error} />;

    if (!event) return (
            <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
                <CircularProgress size={100} aria-label="Loading…" />
            </Box>
        )

    return(
        <Box>
            <Header/>
            <Container maxWidth="lg" >
                <EventThumbnail
                    src={event.images[0]}/>
                <Grid container spacing={3}>
                    <Grid size={9} sx={{ display: "flex", flexDirection: "column", gap: 2 }}>

                        <Paper elevation={0} sx={{ mt: 2, border: "1px solid", borderColor: "divider", borderRadius: {borderRadius}, px: 3, pb: 2 }}>
                            <Typography variant="h4" sx={{ pt: 4, pb: 2, fontWeight: 800 }}>{event.title}</Typography>

                            <Stack spacing={1} sx={{ mb: 4 }}>
                                <EventDetail icon={<CalendarTodayIcon />} text={`${formatDate(event.startDatetime)} - ${formatTime(event.endDatetime)}`} />
                                <EventDetail icon={<LocationOnIcon />}    text={`${event.venue.name}, ${event.venue.city} · ${event.auditoriumName}`} />
                                <EventDetail icon={<MicIcon />}           text={event.performers.join(", ")} />
                                <EventDetail icon={<LocalOfferIcon />}    text={event.categoryName} />
                            </Stack>

                            <EventDescription description={event.description}/>
                        </Paper>

                        <InfoCard title="Event location">
                            <EventDetail icon={<BusinessIcon />}   text={event.venue.name} />
                            <EventDetail icon={<LocationOnIcon />} text={`${event.venue.address}, ${event.venue.city}, ${event.venue.country}`} />
                        </InfoCard>

                        <InfoCard title="Organizer">
                            <Stack direction="row" spacing={1.5} sx={{ mb: 2, alignItems:"center" }}>
                                <Avatar sx={{ backgroundColor: "primary.light", color: "primary.dark", width: 40, height: 40, fontSize: 14 }}>
                                    {event.organization.companyName.slice(0, 2).toUpperCase()}
                                </Avatar>
                                <Typography variant="body1" sx={{fontWeight:500}} >{event.organization.companyName}</Typography>
                            </Stack>
                            <Divider sx={{ mb: 2 }} />
                            <Stack spacing={1}>
                                <EventDetail icon={<EmailIcon />} text={event.organization.ownerEmail} href={`mailto:${event.organization.ownerEmail}`} />
                                <EventDetail icon={<PhoneIcon />} text={event.organization.ownerPhone} href={`tel:${event.organization.ownerPhone}`} />
                            </Stack>
                        </InfoCard>

                    </Grid>

                    <Grid size={3}>
                        <Paper elevation={4}  sx={{ mt: 2, padding: 3, borderRadius: {borderRadius} }}>
                            <Typography variant="h6">Tickets</Typography>
                            <Divider sx={{my: 1}} />
                            <Box>
                                <Typography variant="caption" color="text.secondary">Starting from</Typography>
                                <Typography variant="h5" sx={{ fontWeight: 700 }}>€{event.startingTicketPrice}</Typography>
                            </Box>
                            <Button
                                variant="contained"
                                fullWidth
                                sx={{backgroundColor: "#06d373", color: "black", fontWeight: 700, mt: 1}}>
                                Select tickets
                            </Button>
                        </Paper>
                    </Grid>
                </Grid>
            </Container>
            <Footer/>
        </Box>
    );
}

