**Description**
App for searching flight offers, using amadeus API for retrieving flight offers, airline name and airport codes and names
Consists of a form to introduce information for searching, a display for the flightoffers, and a detailed view of the offers

**Backend Package Structure Overview**
--amadeus/--
Contains services and mappers to interact with the Amadeus API:
1.- AuthService, HttpService: Handle authentication and HTTP communication.
2.- AirportSearchService, AirlineLookupService, FlightSearchService: Manage flight and airport queries.
3.- FlightOfferMapper, ExtendedFlightOfferMapper: Convert API responses into internal DTOs.

--config/--
Contains configuration classes:
1.- AmadeusApiConfig: Loads and configures Amadeus API keys and endpoints from environment variables or properties.

--controller/--
Defines the REST API endpoints:
1.- AirportController, FlightController: Expose endpoints for flight and airport search.
2.- GlobalExceptionHandler: Handles errors globally and returns consistent HTTP responses.

--dto/--
Holds data transfer objects used across layers:
DTOs like AirportDTO, FlightOfferDTO, and PriceDetailsDTO encapsulate request and response data structures passed between controllers and services.

**Frontend Code Structure Overview**

src/components/

--components/--

1.-SearchForm.tsx: Form UI for flight search inputs.
2.-FlightResultsDisplay.tsx, FlightDetails.tsx: Display results and detailed flight info.

--src/services/--

1.-flightApi.ts: Handles API requests to the backend.

--src/testing/--

Jest unit tests for frontend components:

Contains .test.tsx files for SearchForm, FlightDetails, etc.

src/types/

FlightTypes.ts: Shared TypeScript types used across components and services.

**How to Run the Project**

1.- Clone the repository and go to the project root:
git clone <repo-url>
cd flightfinder

2.- Create a .env file in the root of the project with the following content:

AMADEUS_API_KEY=your_amadeus_key_here
AMADEUS_API_SECRET=your_amadeus_secret_here

You can use the provided .env.example file as a reference.

Note: You must obtain your Amadeus API credentials by registering at the official Amadeus for Developers website.

3.- Build the backend (Spring Boot):

On Windows:
cd backend
gradlew clean build
cd ..

On Mac/Linux:
cd backend
./gradlew clean build
cd ..

4.- Run Docker Compose to build and start the containers:
docker compose down --volumes --remove-orphans
docker compose build --no-cache
docker compose up

5.- Open your browser and visit:

Frontend (React): http://localhost:3000

Backend (Spring Boot API): http://localhost:8080


**How to Run Automated Tests**

1.- Backend Unit Tests

2.- Open a terminal or console.

3.- Navigate to the backend directory where build.gradle is located.

Run the appropriate command based on your OS:

Windows (CMD or PowerShell):
gradlew test

Linux or macOS:
./gradlew test

This will run all tests located in src/test/java.

View Test Results
Once completed, open the following file in your browser to see the test report:

backend/build/reports/tests/test/index.html

This file provides a visual summary of which tests passed or failed.


**Frontend Unit Tests (React with Jest)**

1.- Install dependencies
From the root of the frontend project:

npm install


2.-Run all tests interactively:
npm test

Press the "a" key to run all tests when prompted.


3.- Run all tests once (CI mode):

npm test -- --watchAll=false


4.-Test file location:

Test files are located in:

src/testing/

and should use the .test.tsx extension.


**Cypress E2E Tests with Mocks**

This project includes E2E tests using Cypress with mocked backend responses for speed and reliability.

--Main Test File--

E2E tests are located in:
frontend/cypress/e2e/flightSearch.cy.ts

Includes test cases for:
Flight search with mocked results
Date validation: return date before departure date
Required field and adult count validation
Sorting by price (ascending)

**How to Run Cypress Tests**

From the frontend directory:

Visual Interface (recommended for development):

npx cypress open

Headless mode (terminal only):

npx cypress run

Make sure the frontend is running at:

http://localhost:3000

Mocked endpoints used:

GET /api/airports/search*

Simulated results for "Madrid" and "Barcelona".