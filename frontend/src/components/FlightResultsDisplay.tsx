import React, { useState } from 'react';
import { FlightOfferDTO, FlightResultsDisplayProps } from '../types/FlightTypes';
import FlightResultsDetail from './FlightDetails';
import './FlightResultsDisplay.css';

const parseDuration = (duration: string): number => {
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  const hours = match?.[1] ? parseInt(match[1]) : 0;
  const minutes = match?.[2] ? parseInt(match[2]) : 0;
  return hours * 60 + minutes;
};

const formatDuration = (duration: string): string => {
  const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?/);
  const hours = match?.[1] ? `${match[1]}h` : '';
  const minutes = match?.[2] ? `${match[2]}m` : '';
  return [hours, minutes].filter(Boolean).join(' ');
};

const formatTime = (timeStr: string): string => {
  const [hour, minute] = timeStr.split(':');
  const h = parseInt(hour, 10);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const formattedHour = ((h + 11) % 12 + 1).toString();
  return `${formattedHour}:${minute} ${ampm}`;
};

const formatStopWithDayOffset = (arrivalDateTime: string, departureDateTime: string): string => {
  const arrival = new Date(arrivalDateTime);
  const departure = new Date(departureDateTime);
  const dayDiff = Math.floor((departure.getTime() - arrival.getTime()) / (1000 * 60 * 60 * 24));
  const formatTime = (dt: Date) => `${dt.getHours().toString().padStart(2, '0')}:${dt.getMinutes().toString().padStart(2, '0')}`;
  const offsetStr = dayDiff > 0 ? ` (+${dayDiff})` : '';
  return `${formatTime(arrival)} → ${formatTime(departure)}${offsetStr}`;
};

const calculateLayover = (arrivalDate: string, arrivalTime: string, departureDate: string, departureTime: string): string | null => {
  const arrival = new Date(`${arrivalDate}T${arrivalTime}`);
  const departure = new Date(`${departureDate}T${departureTime}`);
  const diffMinutes = Math.round((departure.getTime() - arrival.getTime()) / 60000);
  if (diffMinutes <= 0 || diffMinutes > 1440) return null;
  const hours = Math.floor(diffMinutes / 60);
  const mins = diffMinutes % 60;
  return `${hours}h ${mins}m layover`;
};

const FlightResultsDisplay: React.FC<FlightResultsDisplayProps> = ({
  results,
  currency,
  initialSortFields,
  initialSortDirection,
  showDirect,
  showWithStops,
}) => {
  const [sortFields, setSortFields] = useState(initialSortFields);
  const [sortOrders, setSortOrders] = useState<Record<'price' | 'duration', 'asc' | 'desc'>>({
    price: initialSortDirection,
    duration: initialSortDirection,
  });
  const [selectedFlight, setSelectedFlight] = useState<FlightOfferDTO | null>(null);

  if (selectedFlight) {
    return (
      <FlightResultsDetail
        flight={selectedFlight}
        currency={currency}
        onBack={() => setSelectedFlight(null)}
      />
    );
  }

  const sortedResults = results.slice().sort((a, b) => {
    for (let field of sortFields) {
      const valA = field === 'price' ? parseFloat(a.totalPrice) : parseDuration(a.totalFlightTime);
      const valB = field === 'price' ? parseFloat(b.totalPrice) : parseDuration(b.totalFlightTime);
      if (valA !== valB) {
        return sortOrders[field] === 'asc' ? valA - valB : valB - valA;
      }
    }
    return 0;
  });

  const filteredResults = sortedResults.filter(flight => {
    return (
      (flight.hasStops && showWithStops) ||
      (!flight.hasStops && showDirect)
    );
  });
  

  return (
    <div className="flight-results-container" data-cy="results-container">
      <div className="sort-controls">
        <strong>Sort by:</strong>
        {(['price', 'duration'] as const).map(field => (
          <label key={field} style={{ marginRight: '1rem' }}>
            <input
              type="checkbox"
              checked={sortFields.includes(field)}
              onChange={() =>
                setSortFields(prev =>
                  prev.includes(field) ? prev.filter(f => f !== field) : [...prev, field]
                )
              }
            />
            {field.charAt(0).toUpperCase() + field.slice(1)}
            <button
              type="button"
              onClick={() =>
                setSortOrders(prev => ({
                  ...prev,
                  [field]: prev[field] === 'asc' ? 'desc' : 'asc',
                }))
              }
              style={{ marginLeft: '0.5rem' }}
            >
              {sortOrders[field] === 'asc' ? '↑' : '↓'}
            </button>
          </label>
        ))}
      </div>

      <div className="flight-cards">
        {filteredResults.map((flight, index) => (
          <div
            key={index}
            className="flight-card"
            onClick={async () => {
              try {
                const response = await fetch(`/api/flights/details/${flight.id}`);
                if (!response.ok) throw new Error('Error loading flight details');
                const fullFlight = await response.json();
                setSelectedFlight(fullFlight);
              } catch (error) {
                console.error("Failed to load flight details", error);
                alert("Could not load flight details. Please try again.");
              }
            }}
            style={{ cursor: 'pointer' }}
          >
            <div className="flight-info">
              <p>
                <strong>{flight.departureDate} {formatTime(flight.departureTime)}</strong> →
                <strong> {flight.arrivalDate} {formatTime(flight.arrivalTime)}</strong>
              </p>
              <p>
                {flight.departureAirportName} ({flight.departureAirportCode}) →
                {flight.arrivalAirportName} ({flight.arrivalAirportCode})
              </p>
              <p>
                Airline: {flight.airlineName} ({flight.airlineCode})
                {flight.airlineCode !== flight.operatingAirlineCode &&
                  ` operated by ${flight.operatingAirlineName} (${flight.operatingAirlineCode})`}
              </p>
              <p>Duration: {formatDuration(flight.totalFlightTime)}</p>
              {flight.hasStops && flight.stops.length > 0 && (
                <>
                  <p>Stops:</p>
                  <ul style={{ margin: '0.25rem 0' }}>
                    {flight.stops
                      .filter(stop => {
                        const arrival = new Date(`${stop.arrivalDate}T${stop.arrivalTime}`);
                        const departure = new Date(`${stop.departureDate}T${stop.departureTime}`);
                        const diffMinutes = Math.round((departure.getTime() - arrival.getTime()) / 60000);
                        return diffMinutes > 0 && diffMinutes <= 1440;
                      })
                      .map((stop, idx) => {
                        const arrivalDateTime = `${stop.arrivalDate}T${stop.arrivalTime}`;
                        const departureDateTime = `${stop.departureDate}T${stop.departureTime}`;
                        const layover = calculateLayover(stop.arrivalDate, stop.arrivalTime, stop.departureDate, stop.departureTime);
                        return (
                          <li key={idx}>
                            {stop.airportName} ({stop.airportCode}) —&nbsp;
                            {formatStopWithDayOffset(arrivalDateTime, departureDateTime)}
                            {layover && <span style={{ marginLeft: '0.5rem' }}>({layover})</span>}
                          </li>
                        );
                      })}
                  </ul>
                </>
              )}
              {!flight.hasStops && <p>No stops (direct flight)</p>}
              <p><strong>Total Price:</strong> {flight.totalPrice} {currency}</p>
              <p><strong>Price per Adult:</strong> {flight.pricePerAdult} {currency}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default FlightResultsDisplay;
