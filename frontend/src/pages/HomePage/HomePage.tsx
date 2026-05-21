import { Box, CircularProgress, Container, Grid, Stack, Typography } from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getAvailableEvents } from "../../api/events";
import { EventCard } from "../../components/EventCard/EventCard";
import { EventSearchBar } from "../../components/EventSearchBar/EventSearchBar";
import { Header } from "../../components/Header/Header";
import type { EventResponse } from "../../types/EventResponse";
import "./HomePage.css";

function matchesKeyword(event: EventResponse, keyword: string): boolean {
  const q = keyword.toLowerCase();
  return (
    event.title.toLowerCase().includes(q) ||
    event.description.toLowerCase().includes(q) ||
    event.performers.some(p => p.toLowerCase().includes(q))
  );
}

export function HomePage() {
  const navigate = useNavigate();
  const [allEvents, setAllEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeQuery, setActiveQuery] = useState("");

  useEffect(() => {
    getAvailableEvents()
      .then(data => setAllEvents(data))
      .catch(err => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const filteredEvents = useMemo(() => {
    if (!activeQuery.trim()) return allEvents;
    return allEvents.filter(e => matchesKeyword(e, activeQuery.trim()));
  }, [allEvents, activeQuery]);

  const hasQuery = activeQuery.trim().length > 0;

  return (
    <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
      <Header />
      <Container maxWidth="lg" sx={{ py: 4, flex: 1 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 700, mb: 0.5 }}>
          Upcoming Events
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          Browse and buy tickets for the latest events
        </Typography>

        <EventSearchBar onSearch={setActiveQuery} />

        {loading && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 8 }}>
            <CircularProgress />
          </Box>
        )}

        {error && (
          <Typography color="error">{error}</Typography>
        )}

        {!loading && !error && filteredEvents.length === 0 && (
          <Typography color="text.secondary">
            {hasQuery
              ? `No events found for "${activeQuery}". Try a different keyword.`
              : "No upcoming events found."}
          </Typography>
        )}

        {!loading && !error && filteredEvents.length > 0 && (
          <Grid container spacing={2}>
            {filteredEvents.map(event => (
              <Grid size={12} key={String(event.eventId)}>
                <Stack
                  sx={{ cursor: "pointer" }}
                  onClick={() => navigate(`/event/${event.eventId}`)}
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
