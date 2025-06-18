import { render, screen } from '@testing-library/react';
import FlightResultsDisplay from '../components/FlightResultsDisplay';
import { FlightOfferDTO } from '../types/FlightTypes';

const sampleFlight: FlightOfferDTO = {
  departureAirportCode: 'JFK',
  departureAirportName: 'John F. Kennedy International',
  departureDate: '2025-07-10',
  departureTime: '15:00',
  id: "mock-id-123",
  arrivalAirportCode: 'LAX',
  arrivalAirportName: 'Los Angeles International',
  arrivalDate: '2025-07-10',
  arrivalTime: '18:00',
  airlineCode: 'AA',
  airlineName: 'American Airlines',
  operatingAirlineCode: 'AA',
  operatingAirlineName: 'American Airlines',
  totalFlightTime: 'PT6H',
  totalPrice: '250.00',
  pricePerAdult: '250.00',
  hasStops: false,
  stops: [],
  segments: [
    {
      departureDateTime: '2025-07-10T15:00:00',
      arrivalDateTime: '2025-07-10T18:00:00',
      departureAirportCode: 'JFK',
      departureAirportName: 'John F. Kennedy International',
      arrivalAirportCode: 'LAX',
      arrivalAirportName: 'Los Angeles International',
      carrierCode: 'AA',
      id: "mock-id-123",
      carrierName: 'American Airlines', 
      operatingCarrierCode: 'AA',
      operatingCarrierName: 'American Airlines', 
      flightNumber: '123',
      aircraft: '738',
      duration: 'PT6H',
      cabin: 'ECONOMY',
      bookingClass: 'Y',
      amenities: [{ name: 'Wi-Fi', chargeable: true }],
    }
  ],
  priceDetails: {
    base: '200.00',
    total: '250.00',
    currency: 'USD',
    fees: [{ type: 'TAX', amount: '50.00' }],
    travelerPrices: [{ travelerType: 'ADULT', total: '250.00' }],
  },
};

test('renders flight card with airline name', () => {
  render(
    <FlightResultsDisplay
      results={[sampleFlight]}
      currency="USD"
      initialSortFields={['price']}
      initialSortDirection="asc"
      showDirect={true}
      showWithStops={true}
    />
  );

  expect(screen.getByText(/American Airlines/i)).toBeInTheDocument();

});
