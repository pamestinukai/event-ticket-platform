export interface TicketTypeAnalytics {
  ticketTypeId: number;
  ticketTypeName: string;
  ticketsSold: number;
  checkedIn: number;
  revenue: number;
}

export interface EventAnalyticsResponse {
  eventId: number;
  eventTitle: string;
  totalCapacity: number;
  ticketsSold: number;
  checkedIn: number;
  attendanceRate: number;
  revenue: number;
  ticketTypeAnalytics: TicketTypeAnalytics[];
}
