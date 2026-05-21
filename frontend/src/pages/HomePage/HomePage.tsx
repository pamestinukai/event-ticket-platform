import {
  Box,
  CircularProgress,
  Container,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchPublicEvents } from '../../api/events';
import { EventCard } from '../../components/EventCard/EventCard';
import { EventSearchBar } from '../../components/EventSearchBar/EventSearchBar';
import { Header } from '../../components/Header/Header';
import type { EventResponse } from '../../types/EventResponse';
import './HomePage.css';

export function HomePage() {
  const navigate = useNavigate();
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [fetchedQuery, setFetchedQuery] = useState<string | null>(
    null,
  );
  const [error, setError] = useState<string | null>(null);
  const [activeQuery, setActiveQuery] = useState('');

  const loading = fetchedQuery !== activeQuery;

  useEffect(() => {
    let cancelled = false;
    searchPublicEvents(activeQuery)
      .then((data) => {
        if (!cancelled) {
          setEvents(data);
          setFetchedQuery(activeQuery);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message);
          setFetchedQuery(activeQuery);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [activeQuery]);

  const hasQuery = activeQuery.trim().length > 0;

  return (
    <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
      <Header />
      <Container maxWidth='lg' sx={{ py: 4, flex: 1 }}>
        <Typography
          variant='h4'
          component='h1'
          sx={{ fontWeight: 700, mb: 0.5 }}
        >
          Upcoming Events
        </Typography>
        <Typography
          variant='body1'
          color='text.secondary'
          sx={{ mb: 3 }}
        >
          Browse and buy tickets for the latest events
        </Typography>

        <EventSearchBar onSearch={setActiveQuery} />

        {loading && (
          <Box
            sx={{ display: 'flex', justifyContent: 'center', py: 8 }}
          >
            <CircularProgress />
          </Box>
        )}

        {error && <Typography color='error'>{error}</Typography>}

        {!loading && !error && events.length === 0 && (
          <Typography color='text.secondary'>
            {hasQuery
              ? `No events found for "${activeQuery}". Try a different keyword.`
              : 'No upcoming events found.'}
          </Typography>
        )}

        {!loading && !error && events.length > 0 && (
          <Grid container spacing={2}>
            {events.map((event) => (
              <Grid size={12} key={String(event.eventId)}>
                <Stack
                  sx={{ cursor: 'pointer' }}
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
