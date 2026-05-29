import type { EventType } from "./EventResponse";
import type { TicketTypeRequest } from "./TicketType";

export interface EventRequest {
    organizationId : number;
    venueId : number;
    auditoriumId : number | null;
    categoryId : number;
    title : string;
    description : string;
    performers : string[];
    images : string[];
    startDatetime : string;
    endDatetime : string;
    status : EventType;
    ticketTypes : TicketTypeRequest[];
    version? : number | null;
}
