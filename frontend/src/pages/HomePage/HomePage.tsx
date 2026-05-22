import {
  Box,
  CircularProgress,
  Container,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPublicCategories } from '../../api/categories';
import { searchPublicEvents } from '../../api/events';
import { EventCard } from '../../components/EventCard/EventCard';
import {
  DEFAULT_FILTERS,
  EventFiltersPanel,
  type EventFilters,
} from '../../components/EventFilters/EventFilters';
import { EventSearchBar } from '../../components/EventSearchBar/EventSearchBar';
import { Header } from '../../components/Header/Header';
import type { CategoryResponse } from '../../types/CategoryResponse';
import type { EventResponse } from '../../types/EventResponse';
import './HomePage.css';

export function HomePage() {
  const navigate = useNavigate();

  // Backend-fetched results (keyword + category + date filters applied server-side)
  const [backendEvents, setBackendEvents] = useState<EventResponse[]>(
    [],
  );
  const [fetchedKey, setFetchedKey] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Search & filter state
  const [activeQuery, setActiveQuery] = useState('');
  const [filters, setFilters] =
    useState<EventFilters>(DEFAULT_FILTERS);

  // Categories for the filter dropdown
  const [categories, setCategories] = useState<CategoryResponse[]>(
    [],
  );

  // Key used to detect in-flight requests
  const fetchKey = JSON.stringify({
    q: activeQuery,
    cid: filters.categoryId,
    from: filters.dateFrom,
    to: filters.dateTo,
  });
  const loading = fetchedKey !== fetchKey;

  // Fetch categories once
  useEffect(() => {
    getPublicCategories()
      .then(setCategories)
      .catch(() => {});
  }, []);

  // Fetch events whenever search query or backend-supported filters change
  useEffect(() => {
    const key = fetchKey;
    let cancelled = false;
    searchPublicEvents({
      keyword: activeQuery,
      categoryId: filters.categoryId,
      dateFrom: filters.dateFrom,
      dateTo: filters.dateTo,
    })
      .then((data) => {
        if (!cancelled) {
          setBackendEvents(data);
          setFetchedKey(key);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message);
          setFetchedKey(key);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [
    activeQuery,
    filters.categoryId,
    filters.dateFrom,
    filters.dateTo,
    fetchKey,
  ]);

  // Cities available for the city filter (derived from current backend results)
  const availableCities = useMemo(
    () =>
      [
        ...new Set(
          backendEvents
            .map((e) => e.venue?.city)
            .filter(Boolean) as string[],
        ),
      ].sort(),
    [backendEvents],
  );

  // Client-side filtering for city and price
  const displayedEvents = useMemo(() => {
    let result = backendEvents;
    if (filters.cities.length > 0) {
      result = result.filter(
        (e) => e.venue?.city && filters.cities.includes(e.venue.city),
      );
    }
    if (filters.priceMin !== '') {
      result = result.filter(
        (e) =>
          e.startingTicketPrice == null ||
          Number(e.startingTicketPrice) >= Number(filters.priceMin),
      );
    }
    if (filters.priceMax !== '') {
      result = result.filter(
        (e) =>
          e.startingTicketPrice == null ||
          Number(e.startingTicketPrice) <= Number(filters.priceMax),
      );
    }
    return result;
  }, [
    backendEvents,
    filters.cities,
    filters.priceMin,
    filters.priceMax,
  ]);

  const hasQuery = activeQuery.trim().length > 0;
  const hasFilters =
    JSON.stringify(filters) !== JSON.stringify(DEFAULT_FILTERS);

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

        <EventFiltersPanel
          filters={filters}
          onChange={setFilters}
          categories={categories}
          availableCities={availableCities}
        />

        {loading && (
          <Box
            sx={{ display: 'flex', justifyContent: 'center', py: 8 }}
          >
            <CircularProgress />
          </Box>
        )}

        {error && <Typography color='error'>{error}</Typography>}

        {!loading && !error && displayedEvents.length === 0 && (
          <Typography color='text.secondary'>
            {hasQuery || hasFilters
              ? 'No events match your search or filters. Try adjusting your criteria.'
              : 'No upcoming events found.'}
          </Typography>
        )}

        {!loading && !error && displayedEvents.length > 0 && (
          <Grid container spacing={2}>
            {displayedEvents.map((event) => (
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
