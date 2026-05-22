import type {OrganizationResponse} from "./OrganizationResponse.ts";
import type {VenueResponse} from "./VenueResponse.ts";

export type EventType = "DRAFT" | "PUBLISHED" | "CANCELED" | "RESCHEDULED" | "COMPLETED";

export interface EventResponse {
    eventId : bigint;
    title : string;
    description : string;
    performers : string[];
    images : string[];
    startDatetime : string;
    endDatetime : string;
    status : EventType;
    createdAt : string;
    updatedAt : string;
    startingTicketPrice: number;
    organization : OrganizationResponse;
    venue : VenueResponse;
    auditoriumName : string;
    categoryName : string;
}