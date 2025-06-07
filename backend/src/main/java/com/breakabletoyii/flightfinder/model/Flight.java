package com.breakabletoyii.flightfinder.model;

public class Flight {
    private String departureCode;  // code for departure airport
    private String arrivalCode;    // code for arrival airport
    private String departureDate;  // departure date
    private String returnDate;     // return date (optional)
    private int numberAdults;      // number of adults
    private String currency;       // currency type
    private boolean nonStop;       // true = non-stop flight only

    // Empty constructor
    public Flight() {}

    // Constructor with all fields
    public Flight(String departureCode, String arrivalCode, String returnDate, String departureDate, int numberAdults, String currency, boolean nonStop) {
        this.departureCode = departureCode;
        this.arrivalCode = arrivalCode;
        this.returnDate = returnDate;
        this.departureDate = departureDate;
        this.numberAdults = numberAdults;
        this.currency = currency;
        this.nonStop = nonStop;
    }

    public String getDepartureCode() {
        return departureCode;
    }

    public void setDepartureCode(String departureCode) {
        this.departureCode = departureCode;
    }

    public String getArrivalCode() {
        return arrivalCode;
    }

    public void setArrivalCode(String arrivalCode) {
        this.arrivalCode = arrivalCode;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public int getNumberAdults() {
        return numberAdults;
    }

    public void setNumberAdults(int numberAdults) {
        this.numberAdults = numberAdults;
    }

    public boolean isNonStop() {
        return nonStop;
    }

    public void setNonStop(boolean nonStop) {
        this.nonStop = nonStop;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
