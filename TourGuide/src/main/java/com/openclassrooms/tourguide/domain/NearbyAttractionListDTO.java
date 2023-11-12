package com.openclassrooms.tourguide.domain;

import gpsUtil.location.VisitedLocation;

import java.util.List;

public class NearbyAttractionListDTO {

    private VisitedLocation userLocation;
    private List<NearbyAttractionDTO> attractions;


    public VisitedLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(VisitedLocation userLocation) {
        this.userLocation = userLocation;
    }

    public List<NearbyAttractionDTO> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<NearbyAttractionDTO> attractions) {
        this.attractions = attractions;
    }
}