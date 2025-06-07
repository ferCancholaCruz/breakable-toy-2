import React, { useState } from 'react';

interface StopDTO {
  airportCode: string;
  airportName: string;
  arrivalTime: string;
  departureTime: string;
}

interface FlightOfferDTO {
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

interface FlightResultsDisplayProps {
  results: FlightOfferDTO[];
  currency: string;
  initialSortFields: ('price' | 'duration')[];
  initialSortDirection: 'asc' | 'desc';
}

const parseDuration = (duration: string): number => {
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  const hours = match?.[1] ? parseInt(match[1]) : 0;
  const minutes = match?.[2] ? parseInt(match[2]) : 0;
  return hours * 60 + minutes;
};

const FlightResultsDisplay: React.FC<FlightResultsDisplayProps> = ({ results, currency, initialSortFields, initialSortDirection }) => {
  //keep the fields needed for sorting
  const [sortFields, setSortFields] = useState<("price" | "duration")[]>(initialSortFields);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>(initialSortDirection);

  //copy the results and sort by the rules inside the sort
  const sortedResults = results.slice().sort((a, b) => {
    for (let field of sortFields) {
      const valA = field === 'price' ? parseFloat(a.totalPrice) : parseDuration(a.totalFlightTime);
      const valB = field === 'price' ? parseFloat(b.totalPrice) : parseDuration(b.totalFlightTime);
      if (valA !== valB) {
        return sortDirection === 'asc' ? valA - valB : valB - valA; //which field goes first
      }
    }
    return 0;
  });

  const toggleSortField = (field: 'price' | 'duration') => {
    setSortFields((prev) => {
      return prev.includes(field) ? prev.filter(f => f !== field) : [...prev, field];
    });
  };

  const toggleSortDirection = () => {
    setSortDirection(prev => (prev === 'asc' ? 'desc' : 'asc'));
  };

  return (
    <div>
      <div>
        <strong>Sort by:</strong>
        <label>
          <input
            type="checkbox"
            checked={sortFields.includes('price')}
            onChange={() => toggleSortField('price')}
          /> Price
        </label>
        <label>
          <input
            type="checkbox"
            checked={sortFields.includes('duration')}
            onChange={() => toggleSortField('duration')}
          /> Duration
        </label>
        <button onClick={toggleSortDirection}>
          Order: {sortDirection === 'asc' ? 'Ascendente' : 'Descendente'}
        </button>
      </div>
      <ul>
        {sortedResults.map((flight, index) => (
          <li key={index}>
            {flight.departureDate} {flight.departureTime} - {flight.arrivalDate} {flight.arrivalTime} | 
            {flight.departureAirportName} ({flight.departureAirportCode}) → {flight.arrivalAirportName} ({flight.arrivalAirportCode}) | 
            {flight.airlineName} ({flight.airlineCode})
            {flight.airlineCode !== flight.operatingAirlineCode && ` by  ${flight.operatingAirlineName} (${flight.operatingAirlineCode})`} | 
            Total duration: {flight.totalFlightTime} | Total price: {flight.totalPrice} {currency} (for each adult: {flight.pricePerAdult})

            {flight.hasStops && (
              <ul>
                {flight.stops.map((stop, idx) => (
                  <li key={idx}>
                    Stop at {stop.airportName} ({stop.airportCode}) - arrival: {stop.arrivalTime}, departure: {stop.departureTime}
                  </li>
                ))}
              </ul>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FlightResultsDisplay;
