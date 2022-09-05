package com.catiger.taxi;

public class Place {
    private String placeName;
    private double latitude, longitude;
    private String placePosition;
    private String poiID;
    public Place(String placeName, String placePosition) {
        this.placeName = placeName;
        this.placePosition = placePosition;
    }

    public Place(String placeName, String placePosition, double latitude, double longitude) {
        this.placeName = placeName;
        this.placePosition = placePosition;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setPoiID(String poiID) {
        this.poiID = poiID;
    }

    public String getPoiID() {
        return poiID;
    }


    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlacePosition() {
        return placePosition;
    }

    public void setPlacePosition(String placePosition) {
        this.placePosition = placePosition;
    }

    @Override
    public String toString() {
        return "Place{" +
                "placeName='" + placeName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", placePosition='" + placePosition + '\'' +
                '}';
    }
}
