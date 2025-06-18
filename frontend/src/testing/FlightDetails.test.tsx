import React from 'react';
import { render, screen } from '@testing-library/react';
import FlightResultsDetail from '../components/FlightDetails';
import { FlightOfferDTO } from '../types/FlightTypes';

const mockFlight: FlightOfferDTO = {
  departureAirportCode: 'JFK',
  id: "mock-id-123",
  departureAirportName: 'John F. Kennedy International',
  departureDate: '2025-07-10',
  departureTime: '15:00',
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
      carrierName: 'American Airlines', 
      operatingCarrierCode: 'AA',
      operatingCarrierName: 'American Airlines', 
      flightNumber: '123',
      aircraft: '738',
      duration: 'PT6H',
      cabin: 'ECONOMY',
      bookingClass: 'Y',
      amenities: [{ name: 'Wi-Fi', chargeable: true }],
      id: '1'
    }
  ],
  
  priceDetails: {
    base: '200.00',
    total: '250.00',
    currency: 'USD',
    fees: [{ type: 'TAX', amount: '50.00' }],
    travelerPrices: [{ travelerType: 'ADULT', total: '250.00' }],
  }
};

describe('FlightResultsDetail', () => {
  it('renders segment and price information', () => {
    render(
      <FlightResultsDetail
        flight={mockFlight}
        currency="USD"
        onBack={() => {}}
      />
    );

    expect(screen.getByText(/Flight Details/i)).toBeInTheDocument();
    expect(screen.getByText(/Departure:/i)).toBeInTheDocument();
    expect(screen.getByText(/Wi-Fi/i)).toBeInTheDocument();
    expect(screen.getByText(/Total Price/i)).toBeInTheDocument();
    expect(screen.getAllByText(/250.00 USD/i)).toHaveLength(2);

  });
});
