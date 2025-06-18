import React, { useState, useEffect, useRef } from 'react';
import FlightResultsDisplay from './FlightResultsDisplay';
import { FlightOfferDTO } from '../types/FlightTypes';
import { searchAirports, searchFlights } from '../services/flightApi';
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
  const [isLoading, setIsLoading] = useState(false);


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
      const data = await searchAirports(keyword);
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
      nonStop: showDirect
    };

    try {
      setIsLoading(true);
      const data = await searchFlights(payload);
      setFlightResults(data);
      setShowForm(false);
    } catch (err) {
      console.error('Error searching for flights:', err);
      alert('There was a problem looking for flights');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      <button
      className="toggle-form-btn"
     onClick={() => setShowForm((prev) => !prev)}
      >
      {showForm ? 'Hide Search Form' : 'Start Searching for Flights'}
    </button>
  
      {showForm && (
        <>
          <form className="search-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="departureInput">Departure:</label>
              <input
                id="departureInput"
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
  
            <div className="form-group">
              <label htmlFor="arrivalInput">Arrival:</label>
              <input
                id="arrivalInput"
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
              <label htmlFor="departureDate">Departure Date:</label>
              <input
                id="departureDate"
                type="date"
                value={departureDate}
                onChange={(e) => setDepartureDate(e.target.value)}
              />
            </div>
  
            <div className="form-group">
              <label htmlFor="returnDate">Return Date (optional):</label>
              <input
                id="returnDate"
                type="date"
                value={returnDate}
                onChange={(e) => setReturnDate(e.target.value)}
              />
            </div>
  
            <div className="form-group">
              <label htmlFor="adults">Adults:</label>
              <input
                id="adults"
                type="number"
                min={1}
                value={adults}
                onChange={(e) => setAdults(Number(e.target.value))}
              />
            </div>
  
            <div className="form-group">
              <label htmlFor="currency">Currency:</label>
              <select
                id="currency"
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
              >
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="MXN">MXN</option>
              </select>
            </div>
  
            <div className="form-group">
  <label>
    <input
      type="checkbox"
      checked={showDirect}
      onChange={(e) => setShowDirect(e.target.checked)}
    /> Only show non-stop flights
  </label>
</div>

  
            <button type="submit">Search Flights</button>
          </form>
  
          {isLoading && (
  <div
    style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      backgroundColor: 'rgba(255, 255, 255, 0.8)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000,
    }}
  >
    <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#333' }}>
      Searching for flights...
    </p>
  </div>
)}
        </>
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
