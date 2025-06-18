import React from 'react';
import {
  FlightOfferDTO,
  SegmentDetailsDTO,
  AmenityDTO,
  TravelerPriceDTO,
  FeeDTO,
} from '../types/FlightTypes';
import './FlightResultsDisplay.css';

interface FlightResultsDetailProps {
  flight: FlightOfferDTO;
  currency: string;
  onBack: () => void;
}

//separate time and date and display them in AM/PM format
const formatTime = (dateTime: string) => {
  const [date, time] = dateTime.split('T');
  const [hour, minute] = time.split(':');
  const h = parseInt(hour, 10);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const formattedHour = ((h + 11) % 12 + 1).toString();
  return `${date} ${formattedHour}:${minute} ${ampm}`;
};

//calculate the wait time at each stop
const calculateLayover = (arrival: string, nextDeparture: string) => {
  const a = new Date(arrival);
  const b = new Date(nextDeparture);
  const diffMinutes = Math.round((b.getTime() - a.getTime()) / 60000);

  // avoid consider times over 24 hrs
  if (diffMinutes <= 0 || diffMinutes > 1440) return null;

  const hours = Math.floor(diffMinutes / 60);
  const mins = diffMinutes % 60;
  return `${hours}h ${mins}m layover`;
};

//create a react component tht displays the details 
const FlightResultsDetail: React.FC<FlightResultsDetailProps> = ({
  flight,
  currency,
  onBack,
}) => {
  console.log("Flight details loaded:", flight);
  console.log("First segment:", flight.segments[0]);
  return (
    <div className="flight-details-wrapper">
      <button onClick={onBack} className="back-button">← Back to results</button>
      <h2>Flight Details</h2>

      <div className="flight-detail-main">
      <div className="segments-container">
  {flight.segments.map((seg: SegmentDetailsDTO, index: number) => (
    <div key={index} className="segment-card">
      <h3>Segment {index + 1}</h3>
      <p><strong>From:</strong> {seg.departureAirportName} ({seg.departureAirportCode})</p>
      <p><strong>To:</strong> {seg.arrivalAirportName} ({seg.arrivalAirportCode})</p>
      <p><strong>Departure:</strong> {formatTime(seg.departureDateTime)}</p>
      <p><strong>Arrival:</strong> {formatTime(seg.arrivalDateTime)}</p>
      <p><strong>Airline:</strong> {seg.carrierName} ({seg.carrierCode})</p>
      
      <p><strong>Flight Number:</strong> {seg.flightNumber}</p>
      <p><strong>Aircraft:</strong> {seg.aircraft ?? 'N/A'}</p>
      {seg.operatingCarrierCode ? (
  seg.operatingCarrierCode !== seg.carrierCode ? (
    <p><strong>Operating Carrier:</strong> {seg.operatingCarrierName} ({seg.operatingCarrierCode})</p>
  ) : (
    <p><strong>Operating Carrier:</strong> Same as marketing carrier</p>
  )
) : (
  <p><strong>Operating Carrier:</strong> Not specified</p>
)}


      <div className="traveler-fare-box">
        <p><strong>Cabin:</strong> {seg.cabin ?? 'N/A'}</p>  
        <p><strong>Class:</strong> {seg.bookingClass ?? 'N/A'}</p>

        <h4>Amenities</h4>
        <ul>
          {seg.amenities.length > 0 ? (
            seg.amenities.map((a: AmenityDTO, idx: number) => (
              <li key={idx}>
                {a.name} — {a.chargeable ? 'Chargeable' : 'Included'}
              </li>
            ))
          ) : (
            <li>No amenities listed</li>
          )}
        </ul>
      </div>

          {index < flight.segments.length - 1 && (() => {
           const layoverText = calculateLayover(
             seg.arrivalDateTime,
             flight.segments[index + 1].departureDateTime
           );
           return layoverText ? (
             <p className="layover-info">{layoverText}</p>
              ) : null;
              })()}
           </div>
         ))}
        </div>

        <div className="price-breakdown">
          <h3>Price Breakdown</h3>
          <p><strong>Base Price:</strong> {flight.priceDetails.base} {currency}</p>
          <p><strong>Total Price:</strong> {flight.priceDetails.total} {currency}</p>

          <h4>Fees</h4>
          <ul>
            {flight.priceDetails.fees.map((fee: FeeDTO, idx: number) => (
              <li key={idx}>{fee.type}: {fee.amount} {currency}</li>
            ))}
            <li>
              BASE TAX: {(parseFloat(flight.priceDetails.total) - parseFloat(flight.priceDetails.base)).toFixed(2)} {currency}
            </li>
          </ul>

          <div className="traveler-prices">
            <h4>Traveler Prices</h4>
            <ul>
              {flight.priceDetails.travelerPrices.map((tp: TravelerPriceDTO, idx: number) => (
                <li key={idx}>{tp.travelerType}: {tp.total} {currency}</li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FlightResultsDetail;
