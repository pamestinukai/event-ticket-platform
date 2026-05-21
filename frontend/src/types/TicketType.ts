export interface TicketTypeRequest {
    name : string;
    price : number;
    totalQuantity : number;
}

export interface TicketTypeResponse {
    id : number;
    name : string;
    price : number;
    totalQuantity : number;
    availableQuantity : number;
}
