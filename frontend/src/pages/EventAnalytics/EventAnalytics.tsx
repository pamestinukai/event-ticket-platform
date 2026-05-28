import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate, useParams } from "react-router-dom";
import { getEventAnalytics } from "../../api/events";
import { useAuth } from "../../context/AuthContext";
import type { EventAnalyticsResponse } from "../../types/EventAnalyticsResponse";

export function EventAnalyticsPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [analytics, setAnalytics] = useState<EventAnalyticsResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id || !token) return;
    getEventAnalytics(id, token)
      .then(setAnalytics)
      .catch((err: Error) => setError(err.message));
  }, [id, token]);

  const attendance = useMemo(() => {
    if (!analytics) return "0.0";
    return analytics.attendanceRate.toFixed(1);
  }, [analytics]);

  if (error) {
    return (
      <Container maxWidth="md" sx={{ py: 6 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")}>Back to dashboard</Button>
      </Container>
    );
  }

  if (!analytics) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "60vh" }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ minHeight: "100vh", py: 6 }}>
      <Container maxWidth="md">
        <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 3 }}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700 }}>Event analytics</Typography>
            <Typography variant="body2" color="text.secondary">{analytics.eventTitle}</Typography>
          </Box>
          <Button variant="outlined" startIcon={<ArrowBackIcon />} onClick={() => navigate("/dashboard")}>Back</Button>
        </Stack>

        <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mb: 3 }}>
          <Paper elevation={0} sx={{ p: 2, border: "1px solid", borderColor: "divider", borderRadius: 1, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Revenue</Typography>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>EUR {Number(analytics.revenue).toFixed(2)}</Typography>
          </Paper>
          <Paper elevation={0} sx={{ p: 2, border: "1px solid", borderColor: "divider", borderRadius: 1, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Tickets sold</Typography>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>{analytics.ticketsSold} / {analytics.totalCapacity}</Typography>
          </Paper>
          <Paper elevation={0} sx={{ p: 2, border: "1px solid", borderColor: "divider", borderRadius: 1, flex: 1 }}>
            <Typography variant="body2" color="text.secondary">Attendance</Typography>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>{analytics.checkedIn} ({attendance}%)</Typography>
          </Paper>
        </Stack>

        <Paper elevation={0} sx={{ p: 3, border: "1px solid", borderColor: "divider", borderRadius: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>By ticket type</Typography>
          <Stack spacing={1.5}>
            {analytics.ticketTypeAnalytics.map((ticketType) => (
              <Box key={ticketType.ticketTypeId}>
                <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "baseline" }}>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>{ticketType.ticketTypeName}</Typography>
                  <Typography variant="body2" color="text.secondary">EUR {Number(ticketType.revenue).toFixed(2)}</Typography>
                </Stack>
                <Typography variant="body2" color="text.secondary">
                  Sold: {ticketType.ticketsSold} | Checked in: {ticketType.checkedIn}
                </Typography>
                <Divider sx={{ mt: 1.5 }} />
              </Box>
            ))}
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
