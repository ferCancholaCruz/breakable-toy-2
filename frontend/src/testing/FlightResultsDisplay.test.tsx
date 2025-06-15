import { render, screen } from '@testing-library/react';
import FlightResultsDisplay from '../components/FlightResultsDisplay';
import { FlightOfferDTO } from '../types/FlightTypes';

const sampleFlight = {
    departureDate: "2025-06-14",
    departureTime: "07:10",
    arrivalDate: "2025-06-14",
    arrivalTime: "10:00",
    airlineCode: "UA",
    airlineName: "United Airlines",
    operatingAirlineCode: "UA",
    operatingAirlineName: "United",
    departureAirportCode: "MAD",
    departureAirportName: "Madrid Barajas",
    arrivalAirportCode: "JFK",
    arrivalAirportName: "New York JFK",
    totalFlightTime: "PT8H30M",
    totalPrice: "450.00",
    pricePerAdult: "450.00",
    hasStops: false,
    stops: [],
    segments: [
        {
          departureDateTime: "2025-06-14T07:10:00",
          arrivalDateTime: "2025-06-14T10:00:00",
          departureAirportCode: "MAD",
          arrivalAirportCode: "JFK",
          carrierCode: "UA",
          airlineCode: "UA",
          flightNumber: "1234",
          duration: "PT8H30M",
          amenities: [
            { type: "wifi", name: "Wi-Fi", available: true, chargeable: false },
            { type: "entertainment", name: "In-Flight Entertainment", available: true, chargeable: false },
            { type: "powerOutlets", name: "Power Outlets", available: true, chargeable: false },
          ],
        },
      ],
      priceDetails: {
        total: "450.00",
        currency: "USD",
        taxes: "50.00",
        base: "400.00",
        fees: [
          {
            amount: "10.00",
            type: "SERVICE",
          },
        ],
        travelerPrices: [
            {
              travelerType: "ADULT",
              total: "450.00",
              price: {
                total: "450.00",
                base: "400.00",
                taxes: "50.00",
              },
            },
          ]
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

  expect(screen.getByText(/United Airlines/i)).toBeInTheDocument();
});
