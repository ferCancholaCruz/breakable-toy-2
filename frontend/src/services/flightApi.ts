import { FlightOfferDTO } from '../types/FlightTypes';

export interface AirportOption {
  iataCode: string;
  name: string;
  cityName: string;
}

const API_URL = process.env.REACT_APP_API_URL ?? 'http://localhost:8080';

export async function searchAirports(keyword: string): Promise<AirportOption[]> {
  const response = await fetch(`${API_URL}/api/airports/search?keyword=${encodeURIComponent(keyword)}`);
  if (!response.ok) throw new Error(await response.text());
  return await response.json();
}

export async function searchFlights(payload: {
  departureCode: string;
  arrivalCode: string;
  departureDate: string;
  returnDate?: string;
  numberAdults: number;
  currency: string;
  nonStop: boolean;
}): Promise<FlightOfferDTO[]> {
  const response = await fetch(`${API_URL}/api/flights/search`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  if (!response.ok) throw new Error(await response.text());
  return await response.json();
}
