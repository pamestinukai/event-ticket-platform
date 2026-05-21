import { Box, Button, Card, CardActionArea, CardContent, CardMedia, Chip, CircularProgress, Container, Grid, Stack, Typography } from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAvailableEvents } from "../../api/events";
import { Header } from "../../components/Header/Header";
import { useAuth } from "../../context/AuthContext";
import type { EventResponse } from "../../types/EventResponse";
import "./Dashboard.css";

type LoadState = "idle" | "loading" | "ready" | "error";

function EventCardItem({ event, onOpen }: { event: EventResponse; onOpen: () => void }) {
  const [hasImageError, setHasImageError] = useState(false);
  const imageSrc = event.images?.[0];

  function formatDate(value: string) {
    return new Intl.DateTimeFormat("en-GB", {
      dateStyle: "medium",
      timeStyle: "short",
    }).format(new Date(value));
  }

  return (
    <Card className="eventCard">
      <CardActionArea onClick={onOpen}>
        {!hasImageError && imageSrc && (
          <CardMedia
            component="img"
            height="180"
            image={imageSrc}
            alt={event.title}
            onError={() => setHasImageError(true)}
          />
        )}
        {(hasImageError || !imageSrc) && (
          <Box className="eventCardFallback">
            <Typography variant="body2">No image available</Typography>
          </Box>
        )}
        <CardContent>
          <Stack direction="row" spacing={1} sx={{ mb: 1 }}>
            <Chip size="small" label={event.categoryName} />
            <Chip size="small" variant="outlined" label={event.venue.city} />
          </Stack>
          <Typography variant="h6" sx={{ fontWeight: 700 }} gutterBottom>
            {event.title}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {formatDate(event.startDatetime)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {event.venue.name}
          </Typography>
          <Box className="eventCardFooter">
            <Typography variant="body2" color="text.secondary">From</Typography>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              €{event.startingTicketPrice.toFixed(2)}
            </Typography>
          </Box>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}

export function Dashboard() {
  const { email, logout } = useAuth();
  const navigate = useNavigate();
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loadState, setLoadState] = useState<LoadState>("idle");

  useEffect(() => {
    let isActive = true;
    setLoadState("loading");
    getAvailableEvents()
      .then((data) => {
        if (!isActive) return;
        setEvents(data);
        setLoadState("ready");
      })
      .catch(() => {
        if (!isActive) return;
        setLoadState("error");
      });
    return () => {
      isActive = false;
    };
  }, []);

  const upcomingEvents = useMemo(() => {
    const now = Date.now();
    return events
      .filter((event) => event.status === "PUBLISHED" || event.status === "RESCHEDULED")
      .filter((event) => new Date(event.startDatetime).getTime() > now)
      .filter((event) => event.startingTicketPrice > 0)
      .sort((a, b) => new Date(a.startDatetime).getTime() - new Date(b.startDatetime).getTime());
  }, [events]);

  const isLoading = loadState === "loading";
  const hasError = loadState === "error";
  const isEmpty = loadState === "ready" && upcomingEvents.length === 0;

  function handleLogout() {
    logout();
    navigate("/");
  }

  return (
    <Box className="dashboardPageRoot">
      <Header />
      <Box className="dashboardHero">
        <Container maxWidth="lg">
          <Grid container spacing={4} sx={{ alignItems: "center" }}>
            <Grid size={{ xs: 12, md: 7 }}>
              <Typography variant="h2" component="h1" className="dashboardTitle">
                Welcome back{email ? `, ${email}` : ""}.
              </Typography>
              <Typography variant="h6" className="dashboardSubtitle">
                Track the next events your organization can promote and share.
              </Typography>
              <Stack direction={{ xs: "column", sm: "row" }} spacing={2} className="dashboardCta">
                <Button variant="outlined" size="large" onClick={handleLogout}>Log out</Button>
              </Stack>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Box className="dashboardHeroCard">
                <Typography variant="overline" className="dashboardHeroBadge">Upcoming</Typography>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>Live ticketing insights</Typography>
                <Typography variant="body1" className="dashboardHeroBody">
                  This catalog shows only future events with available ticket types.
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

      <Container maxWidth="lg" className="dashboardSection">
        <Box className="dashboardSectionHeader">
          <Typography variant="h4" className="sectionTitle">Event catalog</Typography>
          <Typography variant="body1" className="sectionSubtitle">
            Sorted by the soonest start date.
          </Typography>
        </Box>

        {isLoading && (
          <Box className="dashboardState">
            <CircularProgress size={48} />
            <Typography variant="body1">Loading upcoming events...</Typography>
          </Box>
        )}

        {hasError && (
          <Box className="dashboardState">
            <Typography variant="h6">Unable to load events right now.</Typography>
            <Typography variant="body2">Please refresh and try again.</Typography>
          </Box>
        )}

        {isEmpty && (
          <Box className="dashboardState">
            <Typography variant="h6">No upcoming events yet.</Typography>
            <Typography variant="body2">Check back soon for new listings.</Typography>
          </Box>
        )}

        {!isLoading && !hasError && upcomingEvents.length > 0 && (
          <Grid container spacing={3}>
            {upcomingEvents.map((event) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={event.id}>
                <EventCardItem event={event} onOpen={() => navigate(`/event/${event.id}`)} />
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </Box>
  );
}
