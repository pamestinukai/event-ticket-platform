import { Box, CircularProgress, Container, Grid, Stack, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAvailableEvents } from "../../api/events";
import { EventCard } from "../../components/EventCard/EventCard";
import { Header } from "../../components/Header/Header";
import type { EventResponse } from "../../types/EventResponse";
import "./HomePage.css";

export function HomePage() {
  const navigate = useNavigate();
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getAvailableEvents()
      .then(data => setEvents(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 4, flex: 1 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 700, mb: 0.5 }}>
          Upcoming Events
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          Browse and buy tickets for the latest events
        </Typography>

        {loading && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Typography color="error">{error}</Typography>
        )}

        {!loading && !error && events.length === 0 && (
          <Typography color="text.secondary">No upcoming events found.</Typography>
        )}

        {!loading && !error && events.length > 0 && (
          <Grid container spacing={2}>
            {events.map(event => (
              <Grid size={12} key={String(event.id)}>
                <Stack
                  sx={{ cursor: "pointer" }}
                  onClick={() => navigate(`/event/${event.id}`)}
                >
                  <EventCard event={event} />
                </Stack>
              </Grid>
            ))}
          </Grid>
        )}
      </Container>
    </Box>
  );
}
