package contextualegonetwork.contextData;

public class DefaultContextData extends ContextData {
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
	
}
