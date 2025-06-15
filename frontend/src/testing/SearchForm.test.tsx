import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import SearchForm from '../components/SearchForm';

beforeEach(() => {
  jest.useFakeTimers();

  global.fetch = jest.fn((url) => {
    if (url.toString().includes('/api/airports/search?keyword=mad')) {
      return Promise.resolve(
        new Response(JSON.stringify([{ iataCode: 'MAD', name: 'Madrid Barajas', cityName: 'Madrid' }]), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      );
    }
    if (url.toString().includes('/api/airports/search?keyword=cdg')) {
      return Promise.resolve(
        new Response(JSON.stringify([{ iataCode: 'CDG', name: 'Charles de Gaulle', cityName: 'Paris' }]), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      );
    }
    if (url.toString().includes('/api/flights/search')) {
      return Promise.resolve(
        new Response(JSON.stringify([]), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      );
    }
  
    return Promise.reject(new Error('Unknown URL'));
  });

  jest.spyOn(window, 'alert').mockImplementation(() => {});
});

afterEach(() => {
  jest.runOnlyPendingTimers();
  jest.useRealTimers();
  jest.resetAllMocks();
});

test('should show form when clicking toggle button', () => {
  render(<SearchForm />);
  expect(screen.queryByRole('form')).not.toBeInTheDocument();

  fireEvent.click(screen.getByText(/start searching/i));
  expect(screen.getByLabelText('Departure:')).toBeInTheDocument();

});

test('shows suggestions and selects departure and arrival', async () => {
  render(<SearchForm />);
  fireEvent.click(screen.getByText(/start searching/i));

  const [depInput, arrInput] = screen.getAllByPlaceholderText(/city or airport/i);

  fireEvent.change(depInput, { target: { value: 'mad' } });
  act(() => jest.advanceTimersByTime(500));
  await waitFor(() => expect(screen.getByText(/Madrid Barajas/i)).toBeInTheDocument());
  fireEvent.click(screen.getByText(/Madrid Barajas/i));

  fireEvent.change(arrInput, { target: { value: 'cdg' } });
  act(() => jest.advanceTimersByTime(500));
  await waitFor(() => expect(screen.getByText(/Charles de Gaulle/i)).toBeInTheDocument());
  fireEvent.click(screen.getByText(/Charles de Gaulle/i));
});

test('alerts if required fields are missing', () => {
  render(<SearchForm />);
  fireEvent.click(screen.getByText(/start searching/i));

  fireEvent.click(screen.getByRole('button', { name: /search flights/i }));

  expect(window.alert).toHaveBeenCalledWith('Please fill in the required fields');
});

test('alerts if return date is before departure date', async () => {
    render(<SearchForm />);
    fireEvent.click(screen.getByText(/start searching/i));
  
    const [depInput, arrInput] = screen.getAllByPlaceholderText(/city or airport/i);
  
    fireEvent.change(depInput, { target: { value: 'mad' } });
    act(() => jest.advanceTimersByTime(500));
    await waitFor(() => expect(screen.getByText(/Madrid Barajas/i)).toBeInTheDocument());
    fireEvent.click(screen.getByText(/Madrid Barajas/i)); // <- setDepartureCode
  
    fireEvent.change(arrInput, { target: { value: 'cdg' } });
    act(() => jest.advanceTimersByTime(500));
    await waitFor(() => expect(screen.getByText(/Charles de Gaulle/i)).toBeInTheDocument());
    fireEvent.click(screen.getByText(/Charles de Gaulle/i)); // <- setArrivalCode
  
    const departureDateInput = screen.getByLabelText(/departure date/i);
    const returnDateInput = screen.getByLabelText(/return date/i);
  
    const today = new Date();
    const departure = new Date(today);
    const returnDate = new Date(today);
    departure.setDate(today.getDate() + 1);
    returnDate.setDate(today.getDate());
  
    fireEvent.change(departureDateInput, {
      target: { value: departure.toISOString().split('T')[0] },
    });
  
    fireEvent.change(returnDateInput, {
      target: { value: returnDate.toISOString().split('T')[0] },
    });
  
    fireEvent.click(screen.getByRole('button', { name: /search flights/i }));
  
    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Return date cannot be earlier than departure date.');
    });
  });

  test('submits valid form and hides form', async () => {
    render(<SearchForm />);
    fireEvent.click(screen.getByText(/start searching/i));
  
    const [depInput, arrInput] = screen.getAllByPlaceholderText(/city or airport/i);
    const depDate = screen.getByLabelText('Departure Date:');
  
    fireEvent.change(depInput, { target: { value: 'mad' } });
    act(() => jest.advanceTimersByTime(500));
    await waitFor(() => expect(screen.getByText(/Madrid Barajas/i)).toBeInTheDocument());
    fireEvent.click(screen.getByText(/Madrid Barajas/i));
  
    fireEvent.change(arrInput, { target: { value: 'cdg' } });
    act(() => jest.advanceTimersByTime(500));
    await waitFor(() => expect(screen.getByText(/Charles de Gaulle/i)).toBeInTheDocument());
    fireEvent.click(screen.getByText(/Charles de Gaulle/i));
  
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    fireEvent.change(depDate, {
      target: { value: tomorrow.toISOString().split('T')[0] },
    });
  
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /search flights/i }));
    });
  
    await waitFor(() =>
      expect(global.fetch).toHaveBeenCalledWith(
        '/api/flights/search',
        expect.objectContaining({
          method: 'POST',
          headers: expect.any(Object),
          body: expect.stringContaining('"departureCode":"MAD"'),
        })
      )
    );
  });
