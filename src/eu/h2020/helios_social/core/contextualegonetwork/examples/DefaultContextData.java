package eu.h2020.helios_social.core.contextualegonetwork.examples;

public class DefaultContextData {
	public static class Location {
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
	
	
	private String name;
	private Location location;
	
	public DefaultContextData(String name, Location location) {
		this.name = name;
		this.location = location;
	}
	
	public DefaultContextData() {
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLocation() {
		return location;
	}
	
	@Override
	public boolean equals(Object compareTo) {
		return toString().equals(compareTo.toString());
	}
	
	@Override
	public String toString() {
		return name+" "+location.toString();
	}
}
