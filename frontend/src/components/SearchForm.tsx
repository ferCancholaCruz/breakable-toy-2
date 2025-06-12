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
  const [flightResults, setFlightResults] = useState<FlightOfferDTO[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [showDirect, setShowDirect] = useState(true);
  const [showWithStops, setShowWithStops] = useState(true);

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

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const departure = new Date(departureDate);
    const returnD = returnDate ? new Date(returnDate) : null;

    if (departure < today) {
      alert('Departure date cannot be earlier than today.');
      return;
    }

    if (returnD && returnD < departure) {
      alert('Return date cannot be earlier than departure date.');
      return;
    }

    const payload = {
      departureCode,
      arrivalCode,
      departureDate,
      returnDate,
      numberAdults: adults,
      currency,
      nonStop: false // no se usa ya, pero puede ser enviado como false
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
      setShowForm(false);
    } catch (err) {
      console.error('Error searching for flights:', err);
      alert('There was a problem looking for flights');
    }
  };

  return (
    <>
      <button
        onClick={() => setShowForm((prev) => !prev)}
        style={{ marginBottom: '1rem' }}
      >
        {showForm ? 'Hide Search Form' : 'Start Searching for Flights'}
      </button>

      {showForm && (
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
            <label>Show flights with:</label>
            <label style={{ marginLeft: '1rem' }}>
              <input
                type="checkbox"
                checked={showDirect}
                onChange={(e) => setShowDirect(e.target.checked)}
              /> Non-stops
            </label>
            <label style={{ marginLeft: '1rem' }}>
              <input
                type="checkbox"
                checked={showWithStops}
                onChange={(e) => setShowWithStops(e.target.checked)}
              /> Stops
            </label>
          </div>

          <button type="submit">Search Flights</button>
        </form>
      )}

      {flightResults.length > 0 && (
        <FlightResultsDisplay
          results={flightResults}
          currency={currency}
          initialSortFields={['price']}
          initialSortDirection="asc"
          showDirect={showDirect}
          showWithStops={showWithStops}
        />
      )}
    </>
  );
};

export default SearchForm;
