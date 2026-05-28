import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Container, Paper, Stack, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import QrCodeScannerIcon from "@mui/icons-material/QrCodeScanner";
import { useAuth } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { getMyEvents } from "../../api/events";
import type { EventResponse } from "../../types/EventResponse";
import { EventCard } from "../../components/EventCard/EventCard";
import { QrScannerDialog } from "../../components/QrScannerDialog/QrScannerDialog";

export function Dashboard() {
  const { token, email, logout } = useAuth();
  const navigate = useNavigate();
  const [events, setEvents] = useState<EventResponse[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [scannerOpen, setScannerOpen] = useState(false);

  useEffect(() => {
    if (!token) return;
    getMyEvents(token)
      .then(setEvents)
      .catch((err) => setError(err.message));
  }, [token]);

  function handleLogout() {
    logout();
    navigate("/");
  }

  return (
    <Box sx={{ minHeight: "100vh", py: 6 }}>
      <Container maxWidth="md">
        <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 4 }}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>Dashboard</Typography>
            <Typography variant="body2" color="text.secondary">Logged in as <strong>{email}</strong></Typography>
          </Box>
          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              startIcon={<QrCodeScannerIcon />}
              onClick={() => setScannerOpen(true)}
              sx={{ textTransform: "none" }}
            >
              Scan ticket
            </Button>
            <Button variant="outlined" onClick={handleLogout}>Log out</Button>
          </Stack>
        </Stack>

        <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>My events</Typography>
          <Button
            variant="contained"
            disableElevation
            startIcon={<AddIcon />}
            onClick={() => navigate("/events/new")}
            sx={{ textTransform: "none", borderRadius: 1 }}
          >
            Create event
          </Button>
        </Stack>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {events === null && !error && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
            <CircularProgress />
          </Box>
        )}

        {events !== null && events.length === 0 && (
          <Paper elevation={0} sx={{ p: 4, textAlign: "center", border: "1px dashed", borderColor: "divider", borderRadius: 1 }}>
            <Typography variant="body1" color="text.secondary">You haven't created any events yet.</Typography>
          </Paper>
        )}

        {events !== null && events.length > 0 && (
          <Stack spacing={2}>
            {events.map((e) => {
              const eventId = (e as unknown as { eventId: number }).eventId;
              const isPastEvent =
                e.status === "COMPLETED" ||
                new Date(e.endDatetime).getTime() < Date.now();
              return (
                <EventCard
                  key={eventId}
                  event={e}
                  onEdit={() => navigate(`/events/${eventId}/edit`)}
                  onViewAnalytics={() => navigate(`/events/${eventId}/analytics`)}
                  analyticsDisabled={!isPastEvent}
                />
              );
            })}
          </Stack>
        )}
      </Container>

      <QrScannerDialog open={scannerOpen} onClose={() => setScannerOpen(false)} />
    </Box>
  );
}
