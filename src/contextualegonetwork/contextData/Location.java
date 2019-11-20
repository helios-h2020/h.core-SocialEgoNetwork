package contextualegonetwork.contextData;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator=JSOGGenerator.class)
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
}
