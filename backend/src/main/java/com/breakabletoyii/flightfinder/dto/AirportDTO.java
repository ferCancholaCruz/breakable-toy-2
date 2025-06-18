package com.breakabletoyii.flightfinder.dto;

public class AirportDTO {
    private String iataCode;
    private String name;
    private String cityName;

    public AirportDTO(){}

    public AirportDTO(String iataCode, String name, String cityName) {
        this.iataCode = iataCode;
        this.name = name;
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
