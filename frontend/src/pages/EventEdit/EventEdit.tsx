import {useEffect, useRef, useState} from "react";
import { Alert, Box, Button, CircularProgress, Container, Stack, Typography } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { EventForm } from "../../components/EventForm/EventForm";
import { getEventById, updateEvent } from "../../api/events";
import type { EventResponse } from "../../types/EventResponse";
import type {EventRequest} from "../../types/EventRequest.ts";
import {ConflictDialog} from "../../components/ConflictDialog/ConflictDialog.tsx";

export function EventEdit() {
  const { id } = useParams<{ id: string }>();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [event, setEvent] = useState<EventResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isConflict, setIsConflict] = useState(false);
  const lastPayloadRef = useRef<EventRequest | null>(null);

  useEffect(() => {
    if (!id || !token) return;
    getEventById(id, token)
      .then(setEvent)
      .catch((err) => setError(err.message));
  }, [id, token]);

  const handleSubmit = async (payload: EventRequest) => {
    if (!token || !id) return;
    lastPayloadRef.current = payload;
    try {
      await updateEvent(id, payload, token);
      navigate("/dashboard");
    } catch (err: any) {
      if (err.status === 409 || err.message?.toLowerCase().includes("conflict")) {
        setIsConflict(true);
      } else {
        setError(err.message);
      }
    }
  };

  const handleRetry = async () => {
    if (!lastPayloadRef.current || !token || !id) return;
    setIsConflict(false);
    try {
      const latest = await getEventById(id, token);
      await updateEvent(id, { ...lastPayloadRef.current, version: latest.version }, token);
      navigate("/dashboard");
    } catch (err: any) {
      if (err.status === 409 || err.message?.toLowerCase().includes("conflict")) {
        setIsConflict(true);
      } else {
        setError(err.message);
      }
    }
  };

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

        <ConflictDialog
            open={isConflict}
            onClose={() => setIsConflict(false)}
            onRetry={handleRetry}
        />

        {!event && !error && (
          <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
            <CircularProgress />
          </Box>
        )}

        {event && (
          <EventForm
            initial={event}
            submitLabel="Save changes"
            onSubmit={handleSubmit}
          />
        )}
      </Container>
    </Box>
  );
}
