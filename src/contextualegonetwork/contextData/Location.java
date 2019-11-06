package contextualegonetwork.contextData;

public class Location {
    private double latitude, longitude;
    
    public Location(double latitude, double longitude) {
    	this.latitude = latitude;
    	this.longitude = longitude;
    }
    
    public double getLatitude() {
    	return latitude;
    }
    
    public double getLongitude() {
    	return longitude;
    }
}
