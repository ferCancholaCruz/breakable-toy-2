package com.breakabletoyii.flightfinder.model;

import java.util.Date;

public class Flight {
    public String departureCode;  //code for departure airport
    public String arrivalCode; //code for arrival airport
    public String departureDate; //departureDate
    public String arrivalDate; //arrivalDate
    public int numberAdults; //number of adults
    public String currency; //type of currency
    public boolean nonStop; //true = nonstop

    public String getDepartureCode() {
        return departureCode;
    }

    public Flight(String departureCode, String arrivalCode, String arrivalDate, String departureDate, int numberAdults, String currency, boolean nonStop) {
        this.departureCode = departureCode;
        this.arrivalCode = arrivalCode;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.numberAdults = numberAdults;
        this.currency = currency;
        this.nonStop = nonStop;
    }

    public Flight(){

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

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
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

