import { Box, Button, Paper, Stack, Typography } from "@mui/material";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import LocalOfferIcon from "@mui/icons-material/LocalOffer";
import EditIcon from "@mui/icons-material/Edit";
import type { EventResponse } from "../../types/EventResponse";

interface EventCardProps {
  event: EventResponse;
  onEdit?: (event: EventResponse) => void;
}

export function EventCard({ event, onEdit }: EventCardProps) {
  const thumbnail = event.images?.[0];
  return (
    <Paper elevation={0} sx={{ display: "flex", height: 140, border: "1px solid", borderColor: "divider", borderRadius: 1, overflow: "hidden" }}>
      <Box sx={{ width: 180, bgcolor: "background.default", flexShrink: 0, display: "flex", alignItems: "center", justifyContent: "center", overflow: "hidden" }}>
        {thumbnail ? (
          <Box component="img" src={thumbnail} alt={event.title} sx={{ width: "100%", height: "100%", objectFit: "cover" }} />
        ) : (
          <Typography variant="caption" color="text.secondary">No image</Typography>
        )}
      </Box>
      <Box sx={{ p: 2, flex: 1, display: "flex", flexDirection: "column" }}>
        <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "flex-start", mb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>{event.title}</Typography>
          {onEdit && (
            <Button size="small" startIcon={<EditIcon />} onClick={() => onEdit(event)} sx={{ textTransform: "none" }}>
              Edit
            </Button>
          )}
        </Stack>
        <Stack spacing={0.5}>
          <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
            <CalendarTodayIcon fontSize="small" sx={{ color: "text.secondary" }} />
            <Typography variant="body2" color="text.secondary">{event.startDatetime.slice(0, 10)}</Typography>
          </Stack>
          <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
            <LocationOnIcon fontSize="small" sx={{ color: "text.secondary" }} />
            <Typography variant="body2" color="text.secondary">
              {event.venue ? `${event.venue.name}, ${event.venue.city}` : "—"}
            </Typography>
          </Stack>
          <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
            <LocalOfferIcon fontSize="small" sx={{ color: "text.secondary" }} />
            <Typography variant="body2" color="text.secondary">{event.categoryName ?? "—"}</Typography>
          </Stack>
        </Stack>
      </Box>
    </Paper>
  );
}
