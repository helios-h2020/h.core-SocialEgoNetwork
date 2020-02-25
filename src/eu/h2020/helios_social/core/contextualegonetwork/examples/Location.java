package eu.h2020.helios_social.core.contextualegonetwork.examples;

public class Location {
    private double latitude, longitude;
    
    public Location(double latitude, double longitude) {
    	this.latitude = latitude;
    	this.longitude = longitude;
    }
    
    public Location() {
    }
    
    public double getLatitude() {
    	return latitude;
    }
    
    public double getLongitude() {
    	return longitude;
    }
    
    @Override
    public String toString() {
    	return "@("+latitude+","+longitude+")";
    }
}
