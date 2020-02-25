package eu.h2020.helios_social.core.contextualegonetwork.examples;

public class DefaultContextData {
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
	public String toString() {
		return name+" "+location.toString();
	}
}
