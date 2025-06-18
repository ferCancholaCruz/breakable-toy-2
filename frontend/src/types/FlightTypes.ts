export interface StopDTO {
    airportCode: string;
    airportName: string;
    arrivalTime: string;
    departureTime: string;
    arrivalDate: string;
    departureDate: string;
  }

  export interface FlightResultsDisplayProps {
    results: FlightOfferDTO[];
    currency: string;
    initialSortFields: ('price' | 'duration')[];
    initialSortDirection: 'asc' | 'desc';
    showDirect: boolean;
    showWithStops: boolean;
  }
  
  export interface AmenityDTO {
    name: string;
    chargeable: boolean;
  }
  
  export interface SegmentDetailsDTO {
    id?: string;
    departureDateTime: string;
    departureAirportName: string;
    arrivalAirportName: string;
    departureAirportCode: string;
    arrivalAirportCode: string;
    arrivalDateTime: string;
    carrierCode: string;
    carrierName:string;
    operatingCarrierName: string;
    operatingCarrierCode?: string;
    flightNumber: string;
    aircraft?: string;
    duration: string;
    cabin?: string;
    bookingClass?: string;
    amenities: AmenityDTO[];
  }
  
  export interface FeeDTO {
    type: string;
    amount: string;
  }
  
  export interface TravelerPriceDTO {
    travelerType: string;
    total: string;
  }
  
  export interface PriceDetailsDTO {
    base: string;
    total: string;
    currency: string;
    fees: FeeDTO[];
    travelerPrices: TravelerPriceDTO[];
  }
  
  export interface FlightOfferDTO {
    departureAirportCode: string;
    departureAirportName: string;
    departureDate: string;
    departureTime: string;
    id: string;
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
  
    segments: SegmentDetailsDTO[];
    priceDetails: PriceDetailsDTO;
  }
  