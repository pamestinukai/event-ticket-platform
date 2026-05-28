import { useEffect, useState } from "react";
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle,
  FormControl, IconButton, InputLabel, MenuItem, Paper, Select,
  Stack, TextField, Tooltip, Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import PushPinIcon from "@mui/icons-material/PushPin";
import StarIcon from "@mui/icons-material/Star";
import AddPhotoAlternateIcon from "@mui/icons-material/AddPhotoAlternate";
import AddIcon from "@mui/icons-material/Add";
import { useAuth } from "../../context/AuthContext";
import { getVenues } from "../../api/venues";
import { getCategories } from "../../api/categories";
import { getAuditoriums } from "../../api/auditoriums";
import { getMyOrganization } from "../../api/organizations";
import { getTicketTypes } from "../../api/ticketTypes";
import type { VenueResponse } from "../../types/VenueResponse";
import type { CategoryResponse } from "../../types/CategoryResponse";
import type { AuditoriumResponse } from "../../types/AuditoriumResponse";
import type { EventRequest } from "../../types/EventRequest";
import type { EventResponse, EventType } from "../../types/EventResponse";
import type { TicketTypeRequest } from "../../types/TicketType";
import { formatStatus } from "../../utils/eventStatus";

interface TicketTypeRow {
  name: string;
  price: string;
  totalQuantity: string;
}

const EMPTY_TICKET_TYPE: TicketTypeRow = { name: "", price: "", totalQuantity: "" };

// Status options available from the event's currently saved status.
// DRAFT can be published, PUBLISHED can be canceled, anything else is final.
function allowedStatuses(saved: EventType): EventType[] {
  switch (saved) {
    case "DRAFT":
      return ["DRAFT", "PUBLISHED"];
    case "PUBLISHED":
      return ["PUBLISHED", "CANCELED"];
    default:
      return [saved];
  }
}

// Transitions that require an "are you sure?" confirmation before applying.
const CONFIRMED_STATUSES: EventType[] = ["PUBLISHED", "CANCELED"];

function toLocalDatetime(iso: string | undefined): string {
  if (!iso) return "";
  return iso.length >= 16 ? iso.slice(0, 16) : iso;
}

interface EventFormProps {
  initial?: EventResponse;
  submitLabel: string;
  onSubmit: (payload: EventRequest) => Promise<void>;
}

export function EventForm({ initial, submitLabel, onSubmit }: EventFormProps) {
  const { token } = useAuth();

  const [venues, setVenues] = useState<VenueResponse[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [auditoriums, setAuditoriums] = useState<AuditoriumResponse[]>([]);
  const [organizationId, setOrganizationId] = useState<number | null>(null);

  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [venueId, setVenueId] = useState<number | "">(initial?.venue?.id ?? "");
  const [categoryId, setCategoryId] = useState<number | "">("");
  const [auditoriumId, setAuditoriumId] = useState<number | "">("");
  const [startDatetime, setStartDatetime] = useState(toLocalDatetime(initial?.startDatetime));
  const [endDatetime, setEndDatetime] = useState(toLocalDatetime(initial?.endDatetime));
  const [status, setStatus] = useState<EventType>(initial?.status ?? "DRAFT");
  const [performers, setPerformers] = useState(initial?.performers?.join(", ") ?? "");
  const [imageDataUrls, setImageDataUrls] = useState<string[]>(initial?.images ?? []);
  const [ticketTypes, setTicketTypes] = useState<TicketTypeRow[]>([]);

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [pendingStatus, setPendingStatus] = useState<EventType | null>(null);

  useEffect(() => {
    if (!token) return;
    Promise.all([
      getVenues(token),
      getCategories(token),
      getAuditoriums(token),
      getMyOrganization(token),
    ])
      .then(([v, c, a, org]) => {
        setVenues(v);
        setCategories(c);
        setAuditoriums(a);
        setOrganizationId(Number(org.id));
        if (initial) {
          const cat = c.find((x) => x.name === initial.categoryName);
          if (cat) setCategoryId(cat.id);
          if (initial.venue) {
            const aud = a.find((x) => x.venueId === initial.venue.id && x.name === initial.auditoriumName);
            if (aud) setAuditoriumId(aud.id);
          }
        }
      })
      .catch((err) => setErrors({ form: err.message }));

    if (initial) {
      const eventId = (initial as unknown as { eventId: number }).eventId;
      if (eventId) {
        getTicketTypes(eventId, token)
          .then((existing) =>
            setTicketTypes(
              existing.map((t) => ({
                name: t.name,
                price: String(t.price),
                totalQuantity: String(t.totalQuantity),
              })),
            ),
          )
          .catch(() => { /* ignore — leave empty */ });
      }
    }
  }, [token, initial]);

  const filteredAuditoriums = venueId === "" ? [] : auditoriums.filter((a) => a.venueId === venueId);

  const savedStatus: EventType = initial?.status ?? "DRAFT";
  const statusOptions = allowedStatuses(savedStatus);
  const statusLocked = statusOptions.length === 1;

  function handleStatusChange(next: EventType) {
    if (next === status) return;
    if (CONFIRMED_STATUSES.includes(next)) {
      setPendingStatus(next);
    } else {
      setStatus(next);
    }
  }

  async function handleSubmit(e: { preventDefault(): void }) {
    e.preventDefault();
    const next: Record<string, string> = {};
    if (!title.trim()) next.title = "Title is required";
    if (venueId === "") next.venueId = "Venue is required";
    if (categoryId === "") next.categoryId = "Category is required";
    if (!startDatetime) next.startDatetime = "Start datetime is required";
    if (!endDatetime) next.endDatetime = "End datetime is required";
    if (startDatetime && endDatetime && startDatetime >= endDatetime) {
      next.endDatetime = "End must be after start";
    }
    if (organizationId === null) next.form = "Could not load your organization";
    if (ticketTypes.length === 0) next.ticketTypes = "Add at least one ticket type";
    ticketTypes.forEach((t, i) => {
      if (!t.name.trim()) next[`tt-name-${i}`] = "Name is required";
      if (t.price === "" || Number.isNaN(Number(t.price)) || Number(t.price) < 0) next[`tt-price-${i}`] = "Price must be ≥ 0";
      if (t.totalQuantity === "" || Number(t.totalQuantity) <= 0) next[`tt-qty-${i}`] = "Quantity must be > 0";
    });
    if (Object.keys(next).length > 0) {
      setErrors(next);
      return;
    }
    setErrors({});
    setLoading(true);
    try {
      const ticketTypePayload: TicketTypeRequest[] = ticketTypes.map((t) => ({
        name: t.name.trim(),
        price: Number(t.price),
        totalQuantity: Number(t.totalQuantity),
      }));
      await onSubmit({
        organizationId: organizationId!,
        venueId: venueId as number,
        auditoriumId: auditoriumId === "" ? null : (auditoriumId as number),
        categoryId: categoryId as number,
        title: title.trim(),
        description: description.trim(),
        performers: performers.split(",").map((p) => p.trim()).filter(Boolean),
        images: imageDataUrls,
        startDatetime,
        endDatetime,
        status,
        ticketTypes: ticketTypePayload,
      });
    } catch (err) {
      setErrors({ form: err instanceof Error ? err.message : "Something went wrong" });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Paper elevation={0} sx={{ p: 4, border: "1px solid", borderColor: "divider", borderRadius: 1 }}>
      {errors.form && <Alert severity="error" sx={{ mb: 2 }}>{errors.form}</Alert>}
      <Box component="form" onSubmit={handleSubmit}>
        <Stack spacing={2}>
          <TextField
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            error={!!errors.title}
            helperText={errors.title}
            required
            fullWidth
          />

          <TextField
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            multiline
            minRows={3}
            fullWidth
          />

          <FormControl fullWidth required error={!!errors.venueId}>
            <InputLabel>Venue</InputLabel>
            <Select
              label="Venue"
              value={venueId}
              onChange={(e) => { setVenueId(e.target.value as number); setAuditoriumId(""); }}
            >
              {venues.map((v) => (
                <MenuItem key={v.id} value={v.id}>{v.name} — {v.city}</MenuItem>
              ))}
            </Select>
            {errors.venueId && <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>{errors.venueId}</Typography>}
          </FormControl>

          <FormControl fullWidth disabled={venueId === ""}>
            <InputLabel>Auditorium (optional)</InputLabel>
            <Select
              label="Auditorium (optional)"
              value={auditoriumId}
              onChange={(e) => setAuditoriumId(e.target.value as number | "")}
            >
              <MenuItem value="">None</MenuItem>
              {filteredAuditoriums.map((a) => (
                <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl fullWidth required error={!!errors.categoryId}>
            <InputLabel>Category</InputLabel>
            <Select
              label="Category"
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value as number)}
            >
              {categories.map((c) => (
                <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
              ))}
            </Select>
            {errors.categoryId && <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>{errors.categoryId}</Typography>}
          </FormControl>

          <Stack direction="row" spacing={2}>
            <TextField
              label="Start"
              type="datetime-local"
              value={startDatetime}
              onChange={(e) => setStartDatetime(e.target.value)}
              error={!!errors.startDatetime}
              helperText={errors.startDatetime}
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="End"
              type="datetime-local"
              value={endDatetime}
              onChange={(e) => setEndDatetime(e.target.value)}
              error={!!errors.endDatetime}
              helperText={errors.endDatetime}
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>

          <FormControl fullWidth disabled={statusLocked}>
            <InputLabel>Status</InputLabel>
            <Select
              label="Status"
              value={status}
              onChange={(e) => handleStatusChange(e.target.value as EventType)}
            >
              {statusOptions.map((s) => (
                <MenuItem key={s} value={s}>{formatStatus(s)}</MenuItem>
              ))}
            </Select>
            {statusLocked && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.5 }}>
                {formatStatus(savedStatus)} events can no longer change status.
              </Typography>
            )}
          </FormControl>

          <Box>
            <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center", mb: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>Ticket types</Typography>
              <Button
                size="small"
                startIcon={<AddIcon />}
                onClick={() => setTicketTypes((prev) => [...prev, { ...EMPTY_TICKET_TYPE }])}
                sx={{ textTransform: "none" }}
              >
                Add ticket type
              </Button>
            </Stack>
            {ticketTypes.length === 0 ? (
              <Typography variant="caption" color={errors.ticketTypes ? "error" : "text.secondary"}>
                {errors.ticketTypes ?? "No ticket types yet. Add one so people can buy tickets."}
              </Typography>
            ) : (
              <Stack spacing={1.5}>
                {ticketTypes.map((tt, idx) => (
                  <Stack key={idx} direction="row" spacing={1} sx={{ alignItems: "flex-start" }}>
                    <TextField
                      label="Name"
                      value={tt.name}
                      onChange={(e) =>
                        setTicketTypes((prev) => prev.map((r, i) => (i === idx ? { ...r, name: e.target.value } : r)))
                      }
                      error={!!errors[`tt-name-${idx}`]}
                      helperText={errors[`tt-name-${idx}`]}
                      sx={{ flex: 2 }}
                    />
                    <TextField
                      label="Price (€)"
                      type="number"
                      value={tt.price}
                      onChange={(e) =>
                        setTicketTypes((prev) => prev.map((r, i) => (i === idx ? { ...r, price: e.target.value } : r)))
                      }
                      error={!!errors[`tt-price-${idx}`]}
                      helperText={errors[`tt-price-${idx}`]}
                      slotProps={{ htmlInput: { min: 0, step: "0.01" } }}
                      sx={{ flex: 1 }}
                    />
                    <TextField
                      label="Quantity"
                      type="number"
                      value={tt.totalQuantity}
                      onChange={(e) =>
                        setTicketTypes((prev) =>
                          prev.map((r, i) => (i === idx ? { ...r, totalQuantity: e.target.value } : r)),
                        )
                      }
                      error={!!errors[`tt-qty-${idx}`]}
                      helperText={errors[`tt-qty-${idx}`]}
                      slotProps={{ htmlInput: { min: 1, step: 1 } }}
                      sx={{ flex: 1 }}
                    />
                    <Tooltip title="Remove">
                      <IconButton
                        size="small"
                        onClick={() => setTicketTypes((prev) => prev.filter((_, i) => i !== idx))}
                        sx={{ mt: 1 }}
                      >
                        <CloseIcon />
                      </IconButton>
                    </Tooltip>
                  </Stack>
                ))}
              </Stack>
            )}
          </Box>

          <TextField
            label="Performers (comma-separated)"
            value={performers}
            onChange={(e) => setPerformers(e.target.value)}
            fullWidth
          />

          <Box>
            <Stack direction="row" spacing={1} sx={{ alignItems: "baseline", mb: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>Images</Typography>
              <Typography variant="caption" color="text.secondary">First image is the thumbnail</Typography>
            </Stack>
            <Button
              component="label"
              variant="outlined"
              startIcon={<AddPhotoAlternateIcon />}
              sx={{ textTransform: "none", borderRadius: 1, mb: 2, fontWeight: 600 }}
            >
              Add images
              <input
                type="file"
                accept="image/*"
                multiple
                hidden
                onChange={(e) => {
                  const files = Array.from(e.target.files ?? []);
                  if (files.length === 0) return;
                  Promise.all(
                    files.map(
                      (file) =>
                        new Promise<string>((resolve) => {
                          const reader = new FileReader();
                          reader.onload = () => resolve(typeof reader.result === "string" ? reader.result : "");
                          reader.readAsDataURL(file);
                        }),
                    ),
                  ).then((urls) => setImageDataUrls((prev) => [...prev, ...urls.filter(Boolean)]));
                  e.target.value = "";
                }}
              />
            </Button>
            {imageDataUrls.length > 0 && (
              <Box sx={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(140px, 1fr))", gap: 2, alignItems: "start" }}>
                {imageDataUrls.map((src, idx) => {
                  const isThumb = idx === 0;
                  const overlayBtn = {
                    bgcolor: "rgba(0,0,0,0.55)",
                    color: "#fff",
                    width: 28,
                    height: 28,
                    "&:hover": { bgcolor: "rgba(0,0,0,0.75)" },
                  };
                  return (
                    <Box
                      key={`${idx}-${src.slice(0, 32)}`}
                      sx={{
                        position: "relative",
                        borderRadius: 1,
                        overflow: "hidden",
                        border: "2px solid",
                        borderColor: isThumb ? "primary.main" : "divider",
                      }}
                    >
                      <Box
                        component="img"
                        src={src}
                        alt={`Image ${idx + 1}`}
                        sx={{ display: "block", width: "100%", height: 120, objectFit: "cover" }}
                      />

                      {isThumb && (
                        <Stack
                          direction="row"
                          spacing={0.5}
                          sx={{
                            position: "absolute",
                            top: 6,
                            left: 6,
                            alignItems: "center",
                            bgcolor: "primary.main",
                            color: "primary.contrastText",
                            px: 0.75,
                            py: 0.25,
                            borderRadius: 0.5,
                            fontSize: 11,
                            fontWeight: 700,
                          }}
                        >
                          <StarIcon sx={{ fontSize: 14 }} />
                          <span>Thumbnail</span>
                        </Stack>
                      )}

                      <Stack direction="row" spacing={0.5} sx={{ position: "absolute", top: 6, right: 6 }}>
                        {!isThumb && (
                          <Tooltip title="Set as thumbnail">
                            <IconButton
                              size="small"
                              sx={overlayBtn}
                              onClick={() =>
                                setImageDataUrls((prev) => {
                                  const next = [...prev];
                                  const [picked] = next.splice(idx, 1);
                                  next.unshift(picked);
                                  return next;
                                })
                              }
                            >
                              <PushPinIcon sx={{ fontSize: 16 }} />
                            </IconButton>
                          </Tooltip>
                        )}
                        <Tooltip title="Remove">
                          <IconButton
                            size="small"
                            sx={overlayBtn}
                            onClick={() => setImageDataUrls((prev) => prev.filter((_, i) => i !== idx))}
                          >
                            <CloseIcon sx={{ fontSize: 16 }} />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </Box>
                  );
                })}
              </Box>
            )}
          </Box>

          <Button
            type="submit"
            variant="contained"
            disableElevation
            disabled={loading}
            sx={{ py: 1.5, borderRadius: 1, fontWeight: 700, textTransform: "none" }}
          >
            {loading ? "Saving…" : submitLabel}
          </Button>
        </Stack>
      </Box>

      <Dialog open={pendingStatus !== null} onClose={() => setPendingStatus(null)}>
        <DialogTitle>
          {pendingStatus === "CANCELED" ? "Cancel this event?" : "Publish this event?"}
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            {pendingStatus === "CANCELED"
              ? "Canceling this event is permanent and can't be undone. Are you sure you want to continue?"
              : "Publishing makes this event visible to the public and available for ticket purchases. Are you sure?"}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPendingStatus(null)} sx={{ textTransform: "none" }}>
            Go back
          </Button>
          <Button
            variant="contained"
            disableElevation
            color={pendingStatus === "CANCELED" ? "error" : "primary"}
            onClick={() => {
              if (pendingStatus) setStatus(pendingStatus);
              setPendingStatus(null);
            }}
            sx={{ textTransform: "none", fontWeight: 700 }}
          >
            {pendingStatus === "CANCELED" ? "Cancel event" : "Publish"}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
