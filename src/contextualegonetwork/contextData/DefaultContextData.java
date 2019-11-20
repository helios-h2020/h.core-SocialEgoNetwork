package contextualegonetwork.contextData;

public class DefaultContextData extends ContextData {
	private String name;
	private Location location;
	public ContextStates status;
	
	public DefaultContextData(String name, Location location) {
		this.name = name;
		this.location = location;
		this.status = ContextStates.NOTACTIVE;
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
