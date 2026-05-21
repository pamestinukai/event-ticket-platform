import { Box, Container, Stack, Typography, Button } from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { EventForm } from "../../components/EventForm/EventForm";
import { createEvent } from "../../api/events";

export function EventCreate() {
  const { token } = useAuth();
  const navigate = useNavigate();

  return (
    <Box sx={{ minHeight: "100vh", py: 6 }}>
      <Container maxWidth="md">
        <Stack direction="row" spacing={1} sx={{ alignItems: "center", mb: 3 }}>
          <Button startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")} sx={{ textTransform: "none" }}>
            Back
          </Button>
        </Stack>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>Create event</Typography>
        <EventForm
          submitLabel="Create event"
          onSubmit={async (payload) => {
            if (!token) return;
            await createEvent(payload, token);
            navigate("/dashboard");
          }}
        />
      </Container>
    </Box>
  );
}
