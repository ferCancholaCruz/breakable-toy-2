import React, { useState, useEffect, useRef } from 'react';
import FlightResultsDisplay from './FlightResultsDisplay';
import { FlightOfferDTO } from '../types/FlightTypes';
import { debounce } from 'lodash';
import './SearchForm.css';

interface AirportOption {
  iataCode: string;
  name: string;
  cityName: string;
}

const SearchForm: React.FC = () => {
  const [departureInput, setDepartureInput] = useState('');
  const [arrivalInput, setArrivalInput] = useState('');
  const [departureSuggestions, setDepartureSuggestions] = useState<AirportOption[]>([]);
  const [arrivalSuggestions, setArrivalSuggestions] = useState<AirportOption[]>([]);
  const [departureCode, setDepartureCode] = useState('');
  const [arrivalCode, setArrivalCode] = useState('');
  const [departureDate, setDepartureDate] = useState('');
  const [returnDate, setReturnDate] = useState('');
  const [adults, setAdults] = useState(1);
  const [currency, setCurrency] = useState('USD');
  const [nonStop, setNonStop] = useState(false);
  const [flightResults, setFlightResults] = useState<FlightOfferDTO[]>([]);

  const airportCache = useRef<{ [key: string]: AirportOption[] }>({});

  const fetchAirports = async (
    keyword: string,
    setSuggestions: (data: AirportOption[]) => void
  ) => {
    if (!keyword || keyword.length < 3 || keyword.includes('(')) return;

    if (airportCache.current[keyword]) {
      setSuggestions(airportCache.current[keyword]);
      return;
    }

    try {
      const response = await fetch(`/api/airports/search?keyword=${encodeURIComponent(keyword)}`);
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Backend error:', errorText);
        return;
      }
      const data = await response.json();
      airportCache.current[keyword] = data;
      setSuggestions(data);
    } catch (err) {
      console.error('Error fetching airport data:', err);
    }
  };

  const debouncedFetchAirports = useRef(
    debounce((keyword: string, setter: (data: AirportOption[]) => void) => {
      fetchAirports(keyword, setter);
    }, 500)
  ).current;

  useEffect(() => {
    debouncedFetchAirports(departureInput, setDepartureSuggestions);
  }, [departureInput]);

  useEffect(() => {
    debouncedFetchAirports(arrivalInput, setArrivalSuggestions);
  }, [arrivalInput]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!departureCode || !arrivalCode || !departureDate || adults < 1) {
      alert('Please fill in the required fields');
      return;
    }

    const payload = {
      departureCode,
      arrivalCode,
      departureDate,
      returnDate,
      numberAdults: adults,
      currency,
      nonStop,
    };

    try {
      const response = await fetch('/api/flights/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error(`Error: ${response.statusText}`);
      const data = await response.json();
      setFlightResults(data);
    } catch (err) {
      console.error('Error searching for flights:', err);
      alert('There was a problem looking for flights');
    }
  };

  return (
    <>
      <form className="search-form" onSubmit={handleSubmit}>
        {/* Departure Field */}
        <div className="form-group">
          <label>Departure:</label>
          <input
            type="text"
            value={departureInput}
            onChange={(e) => {
              setDepartureInput(e.target.value);
              setDepartureCode('');
            }}
            placeholder="City or airport"
            autoComplete="off"
          />
          {departureSuggestions.length > 0 && (
            <ul className="suggestions-list">
              {departureSuggestions.map((airport) => (
                <li
                  key={airport.iataCode}
                  onClick={() => {
                    setDepartureCode(airport.iataCode);
                    setDepartureInput(`${airport.name} (${airport.iataCode})`);
                    setDepartureSuggestions([]);
                  }}
                >
                  {airport.name} - {airport.cityName} ({airport.iataCode})
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Arrival Field */}
        <div className="form-group">
          <label>Arrival:</label>
          <input
            type="text"
            value={arrivalInput}
            onChange={(e) => {
              setArrivalInput(e.target.value);
              setArrivalCode('');
            }}
            placeholder="City or airport"
            autoComplete="off"
          />
          {arrivalSuggestions.length > 0 && (
            <ul className="suggestions-list">
              {arrivalSuggestions.map((airport) => (
                <li
                  key={airport.iataCode}
                  onClick={() => {
                    setArrivalCode(airport.iataCode);
                    setArrivalInput(`${airport.name} (${airport.iataCode})`);
                    setArrivalSuggestions([]);
                  }}
                >
                  {airport.name} - {airport.cityName} ({airport.iataCode})
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="form-group">
          <label>Departure Date:</label>
          <input type="date" value={departureDate} onChange={(e) => setDepartureDate(e.target.value)} />
        </div>

        <div className="form-group">
          <label>Return Date (optional):</label>
          <input type="date" value={returnDate} onChange={(e) => setReturnDate(e.target.value)} />
        </div>

        <div className="form-group">
          <label>Adults:</label>
          <input type="number" min={1} value={adults} onChange={(e) => setAdults(Number(e.target.value))} />
        </div>

        <div className="form-group">
          <label>Currency:</label>
          <select value={currency} onChange={(e) => setCurrency(e.target.value)}>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="MXN">MXN</option>
          </select>
        </div>

        <div className="form-group">
          <label>Non-stop:</label>
          <input type="checkbox" checked={nonStop} onChange={(e) => setNonStop(e.target.checked)} />
        </div>

        <button type="submit">Search Flights</button>
      </form>

      {flightResults.length > 0 && (
        <FlightResultsDisplay
          results={flightResults}
          currency={currency}
          initialSortFields={['price']}
          initialSortDirection="asc"
        />
      )}
    </>
  );
};

export default SearchForm;