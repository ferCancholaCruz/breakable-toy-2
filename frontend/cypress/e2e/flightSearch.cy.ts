/// <reference types="cypress" />

describe('Flight Search E2E (Mocked)', () => {
  beforeEach(() => {
    cy.intercept('GET', '/api/airports/search*', (req) => {
      const keyword = String(req.query.keyword || '').toLowerCase();
      if (keyword.includes('madrid')) {
        req.reply([
          { iataCode: 'MAD', name: 'Adolfo Suárez Barajas', cityName: 'Madrid' },
        ]);
      } else if (keyword.includes('barcelona')) {
        req.reply([
          { iataCode: 'BCN', name: 'El Prat', cityName: 'Barcelona' },
        ]);
      } else {
        req.reply([]);
      }
    });

    cy.intercept('POST', '/api/flights/search', {
      statusCode: 200,
      body: [
        {
          departureDate: '2025-06-20',
          departureTime: '10:00',
          arrivalDate: '2025-06-20',
          arrivalTime: '12:00',
          departureAirportCode: 'MAD',
          arrivalAirportCode: 'BCN',
          departureAirportName: 'Adolfo Suárez Barajas',
          arrivalAirportName: 'El Prat',
          airlineCode: 'IB',
          airlineName: 'Iberia',
          operatingAirlineCode: 'IB',
          operatingAirlineName: 'Iberia',
          totalFlightTime: 'PT2H0M',
          hasStops: false,
          stops: [],
          totalPrice: '150',
          pricePerAdult: '150',
        },
      ],
    });

    cy.visit('http://localhost:3000'); 
  });

  it('searches for flights successfully', () => {
    // Open the form
    cy.contains('Start Searching for Flights').click();

    // Search and select airport 
    cy.get('#departureInput').type('Madrid');
    cy.get('.suggestions-list li').contains('Madrid').click();

    // look for and select arrival airport
    cy.get('#arrivalInput').type('Barcelona');
    cy.get('.suggestions-list li').contains('Barcelona').click();

    // departure date
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const formattedDate = futureDate.toISOString().split('T')[0];
    cy.get('#departureDate').type(formattedDate);

    // adults
    cy.get('#adults').clear().type('1');

    // send form
    cy.contains('Search Flights').click();

    // get results
    cy.get('[data-cy=results-container]', { timeout: 10000 }).should('exist');
    cy.contains('Iberia');
    cy.contains('150 USD');
  });
});

describe('Flight Search E2E', () => {
  beforeEach(() => {
    cy.visit('http://localhost:3000');
  });

  it('shows an alert if return date is earlier than departure date', () => {
    // open form
    cy.contains('Start Searching for Flights').click();

    // departure airport
    cy.get('#departureInput').type('Madrid');
    cy.get('.suggestions-list li').contains(/Madrid/i).click();

    // arrival airport
    cy.get('#arrivalInput').type('Barcelona');
    cy.get('.suggestions-list li').contains(/Barcelona/i).click();

    // set dates
    const now = new Date();
    const departure = new Date(now);
    departure.setDate(now.getDate() + 2);
    const returnDate = new Date(now);
    returnDate.setDate(now.getDate() + 5);

    const formattedDeparture = departure.toISOString().split('T')[0];
    const formattedReturn = returnDate.toISOString().split('T')[0];

    cy.get('#departureDate').type(formattedDeparture);
    cy.get('#returnDate').type(formattedReturn);

    // set number of paseengers
    cy.get('#adults').clear().type('1');

    // validate alert
    cy.on('window:alert', (text) => {
      expect(text).to.include('Return date cannot be earlier than departure date');
    });

    // send form
    cy.contains('Search Flights').click();
  });
});

describe('Flight Search E2E', () => {
  beforeEach(() => {
    cy.visit('http://localhost:3000'); 
    cy.contains('Start Searching for Flights').click(); // click button to open form
  });

  it('does not allow submission with zero adults', () => {
    // select departure
    cy.get('#departureInput').type('Madrid');
    cy.wait(1000);
    cy.get('.suggestions-list li').contains(/Madrid/i).click();

    // select arrival
    cy.get('#arrivalInput').type('Barcelona');
    cy.wait(1000);
    cy.get('.suggestions-list li').contains(/Barcelona/i).click();

    // departure date
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const formattedDate = futureDate.toISOString().split('T')[0];
    cy.get('#departureDate').type(formattedDate);

    // number of adults to be set in 0
    cy.get('#adults').clear().type('0');

    // Try to submit
    cy.contains('Search Flights').click();

    // alert has to appear
    cy.on('window:alert', (str) => {
      expect(str).to.contain('Please fill in the required fields');
    });
  });

  it('does not allow submission if required fields are missing', () => {
    // leave everything empty, and submit
    cy.contains('Search Flights').click();

    // alert has to appear
    cy.on('window:alert', (str) => {
      expect(str).to.contain('Please fill in the required fields');
    });
  });
});

describe('Flight Search E2E', () => {
  beforeEach(() => {
    cy.visit('http://localhost:3000'); 
  });

  it('searches for flights successfully', () => {
    // Open the form
    cy.contains('Start Searching for Flights').click();

    // Search and select airport 
    cy.get('#departureInput').type('Madrid');
    cy.wait(1000); //wait for suggestions
    cy.get('.suggestions-list li').contains(/Madrid/i).first().click();

    // look for and select arrival airport
    cy.get('#arrivalInput').type('Barcelona');
    cy.wait(1000);
    cy.get('.suggestions-list li').contains(/Barcelona/i).first().click();

    // departure date
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const formattedDate = futureDate.toISOString().split('T')[0];
    cy.get('#departureDate').type(formattedDate);
  

    // send form
    cy.contains('Search Flights').click();

    // get results
    cy.get('[data-cy=results-container]', { timeout: 10000 }).should('exist');
  });

  it('sorts flights by price ascending', () => {
    
    cy.contains('Start Searching for Flights').click();

    cy.get('#departureInput').type('Madrid');
    cy.wait(1000);
    cy.get('.suggestions-list li').contains(/Madrid/i).first().click();

    cy.get('#arrivalInput').type('Barcelona');
    cy.wait(1000);
    cy.get('.suggestions-list li').contains(/Barcelona/i).first().click();

    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 5);
    const formattedDate = futureDate.toISOString().split('T')[0];
    cy.get('#departureDate').type(formattedDate);
    //cy.get('#adults').clear().type('1');
    cy.contains('Search Flights').click();

    cy.get('[data-cy=results-container]', { timeout: 10000 }).should('exist');

    // click to enable price sorting
    cy.get('.sort-controls label')
    .contains('Price')
    .find('button')
    .click();

    cy.wait(1000);

    // Verify sort by order
    cy.get('.flight-card').then(cards => {
      const prices = [...cards]
      .map(card => {
      const text = card.textContent ?? '';
      const match = text.match(/Total Price:\s*(\d+\.\d+)/);
      if (match && match[1]) {
        return parseFloat(match[1]);
      }
      return null;
    })
    .filter((p): p is number => p !== null);

      const sorted = [...prices].sort((a, b) => b - a);
      expect(prices).to.deep.equal(sorted);
    });
  });
});
