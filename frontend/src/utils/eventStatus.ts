import type { ChipProps } from "@mui/material";
import type { EventType } from "../types/EventResponse";

/** Turns an event status enum (e.g. "SOLD_OUT") into a readable label (e.g. "Sold out"). */
export function formatStatus(status: EventType): string {
  const text = status.replace(/_/g, " ").toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

/** MUI Chip color for each event status. */
export function statusChipColor(status: EventType): ChipProps["color"] {
  switch (status) {
    case "PUBLISHED":
      return "success";
    case "CANCELED":
      return "error";
    case "RESCHEDULED":
      return "warning";
    case "SOLD_OUT":
      return "info";
    case "DRAFT":
    case "COMPLETED":
    default:
      return "default";
  }
}
