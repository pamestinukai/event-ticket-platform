import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Container, Stack, Typography } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { EventForm } from "../../components/EventForm/EventForm";
import { getEventById, updateEvent } from "../../api/events";
import type { EventResponse } from "../../types/EventResponse";

export function EventEdit() {
  const { id } = useParams<{ id: string }>();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [event, setEvent] = useState<EventResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id || !token) return;
    getEventById(id, token)
      .then(setEvent)
      .catch((err) => setError(err.message));
  }, [id, token]);

  return (
    <Box sx={{ minHeight: "100vh", py: 6 }}>
      <Container maxWidth="md">
        <Stack direction="row" spacing={1} sx={{ alignItems: "center", mb: 3 }}>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")} sx={{ textTransform: "none" }}>
            Back
          </Button>
        </Stack>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>Edit event</Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        {!event && !error && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
            <CircularProgress />
          </Box>
        )}

        {event && (
          <EventForm
            initial={event}
            submitLabel="Save changes"
            onSubmit={async (payload) => {
              if (!token || !id) return;
              await updateEvent(id, payload, token);
              navigate("/dashboard");
            }}
          />
        )}
      </Container>
    </Box>
  );
}
