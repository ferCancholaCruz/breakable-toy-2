package com.breakabletoyii.flightfinder.amadeus;

import com.breakabletoyii.flightfinder.dto.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExtendedFlightOfferMapper {

    private final FlightOfferMapper summaryMapper;

    public ExtendedFlightOfferMapper(FlightOfferMapper summaryMapper) {
        this.summaryMapper = summaryMapper;
    }

    public List<FlightOfferDTO> mapFromJson(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONArray data = obj.getJSONArray("data");

        List<FlightOfferDTO> resultList = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject offer = data.getJSONObject(i);

            FlightOfferDTO flight = summaryMapper.mapSingleOffer(offer);

            List<SegmentDetailsDTO> segmentList = new ArrayList<>();
            JSONArray itineraries = offer.getJSONArray("itineraries");

            for (int j = 0; j < itineraries.length(); j++) {
                JSONObject itinerary = itineraries.getJSONObject(j);
                JSONArray segments = itinerary.getJSONArray("segments");

                for (int k = 0; k < segments.length(); k++) {
                    JSONObject seg = segments.getJSONObject(k);
                    SegmentDetailsDTO segment = new SegmentDetailsDTO();

                    segment.setDepartureDateTime(seg.getJSONObject("departure").getString("at"));
                    segment.setArrivalDateTime(seg.getJSONObject("arrival").getString("at"));
                    segment.setCarrierCode(seg.getString("carrierCode"));
                    segment.setFlightNumber(seg.optString("number", ""));
                    segment.setDuration(seg.optString("duration", ""));

                    if (seg.has("aircraft")) {
                        segment.setAircraft(seg.getJSONObject("aircraft").optString("code", ""));
                    }

                    if (seg.has("operating")) {
                        segment.setOperatingCarrierCode(seg.getJSONObject("operating").optString("carrierCode", ""));
                    }

                    segment.setId(seg.optString("id", "SEG" + j + "_" + k));
                    segment.setAmenities(new ArrayList<>()); // will fill later
                    segmentList.add(segment);
                }
            }

            JSONObject priceObj = offer.getJSONObject("price");
            PriceDetailsDTO priceDetails = new PriceDetailsDTO();
            priceDetails.setBase(priceObj.optString("base"));
            priceDetails.setTotal(priceObj.optString("total"));
            priceDetails.setCurrency(priceObj.optString("currency"));

            List<FeeDTO> feeList = new ArrayList<>();
            JSONArray fees = priceObj.optJSONArray("fees");
            if (fees != null) {
                for (int f = 0; f < fees.length(); f++) {
                    JSONObject fee = fees.getJSONObject(f);
                    FeeDTO feeDTO = new FeeDTO();
                    feeDTO.setAmount(fee.optString("amount"));
                    feeDTO.setType(fee.optString("type"));
                    feeList.add(feeDTO);
                }
            }
            priceDetails.setFees(feeList);

            List<TravelerPriceDTO> travelerPrices = new ArrayList<>();
            JSONArray travelers = offer.getJSONArray("travelerPricings");
            for (int t = 0; t < travelers.length(); t++) {
                JSONObject traveler = travelers.getJSONObject(t);
                TravelerPriceDTO tp = new TravelerPriceDTO();
                tp.setTravelerType(traveler.optString("travelerType"));
                tp.setTotal(traveler.getJSONObject("price").optString("total"));
                travelerPrices.add(tp);

                JSONArray fareDetailsArray = traveler.getJSONArray("fareDetailsBySegment");
                for (int fd = 0; fd < fareDetailsArray.length(); fd++) {
                    JSONObject fareDetail = fareDetailsArray.getJSONObject(fd);
                    String segmentId = fareDetail.optString("segmentId");

                    for (SegmentDetailsDTO seg : segmentList) {
                        if (segmentId.equals(seg.getId())) {
                            // set cabin and booking class
                            seg.setCabin(fareDetail.optString("cabin", ""));
                            seg.setBookingClass(fareDetail.optString("class", ""));

                            // set amenities
                            JSONArray amenitiesJson = fareDetail.optJSONArray("amenities");
                            if (amenitiesJson != null) {
                                List<AmenityDTO> amenities = new ArrayList<>();
                                for (int a = 0; a < amenitiesJson.length(); a++) {
                                    JSONObject amenityJson = amenitiesJson.getJSONObject(a);
                                    AmenityDTO amenity = new AmenityDTO();
                                    amenity.setName(amenityJson.optString("description"));
                                    amenity.setChargeable(amenityJson.optBoolean("isChargeable"));
                                    amenities.add(amenity);
                                }
                                seg.getAmenities().addAll(amenities);
                            }

                            break;
                        }
                    }
                }
            }

            priceDetails.setTravelerPrices(travelerPrices);

            flight.setSegments(segmentList);
            flight.setPriceDetails(priceDetails);

            resultList.add(flight);
        }

        return resultList;
    }
}
