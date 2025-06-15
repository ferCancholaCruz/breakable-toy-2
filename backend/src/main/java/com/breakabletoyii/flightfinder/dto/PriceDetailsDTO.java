package com.breakabletoyii.flightfinder.dto;
import java.util.List;

public class PriceDetailsDTO {
    private String base;
    private String total;
    private String currency;
    private List<FeeDTO> fees;
    private List<TravelerPriceDTO> travelerPrices;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public List<TravelerPriceDTO> getTravelerPrices() {
        return travelerPrices;
    }

    public void setTravelerPrices(List<TravelerPriceDTO> travelerPrices) {
        this.travelerPrices = travelerPrices;
    }

    public List<FeeDTO> getFees() {
        return fees;
    }

    public void setFees(List<FeeDTO> fees) {
        this.fees = fees;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
