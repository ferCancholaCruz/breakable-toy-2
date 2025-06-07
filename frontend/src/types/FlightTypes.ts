export interface StopDTO {
    airportCode: string;
    airportName: string;
    arrivalTime: string;
    departureTime: string;
  }
  
  export interface FlightOfferDTO {
    departureAirportCode: string;
    departureAirportName: string;
    departureDate: string;
    departureTime: string;
    arrivalAirportCode: string;
    arrivalAirportName: string;
    arrivalDate: string;
    arrivalTime: string;
    airlineCode: string;
    airlineName: string;
    operatingAirlineCode: string;
    operatingAirlineName: string;
    totalFlightTime: string;
    totalPrice: string;
    pricePerAdult: string;
    hasStops: boolean;
    stops: StopDTO[];
  }
  