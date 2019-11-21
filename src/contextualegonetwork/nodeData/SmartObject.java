package contextualegonetwork.nodeData;

import contextualegonetwork.Node;
import contextualegonetwork.Utils;

public class SmartObject extends NodeData {

    /**
     * The MAC address of the device
     */
    private String MACAddress;
    /**
     * The manufacturer of the device
     */
    private String manufacturer;
    /**
     * Name of the owner of the device
     */
    private String owner;

    public SmartObject()
    {}

    /**
     * Constructor method
     * @param username Username of the node
     * @param id Unique identifier of the node
     * @param macAddr MAC Address of the devide
     * @param man Manufacturer of the device
     * @param own Name of the owner of the device
     * @throws NullPointerException if username, macAddr, man or own are null
     * @throws IllegalArgumentException if the length of macAddr is different than 12, or if man or own are empty strings
     */
    public SmartObject(String username, String id, String MACAddress, String manufacturer, String owner) {
    	super("Smart Object");
        if(username == null || MACAddress == null || manufacturer == null || owner == null) throw new NullPointerException();
        if(MACAddress.length() != 12) Utils.error(new IllegalArgumentException("The MAC Address must be made of 12 digits"));
        if(manufacturer.equals("") || owner.equals("")) Utils.error(new IllegalArgumentException("Owner and manufacturer cannot be empty strings"));
        this.MACAddress = MACAddress;
        this.manufacturer = manufacturer;
        this.owner = owner;
    }
    
    /**
     *
     * @return The MAC address of the smart object
     */
    public String getMACAddress() {
        return MACAddress;
    }

    /**
     *
     * @return The manufacturer of the smart object
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     *
     * @return The owner of the smart object
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the smart object
     * @param owner The owner to be set
     * @throws NullPointerException if owner is null
     * @throws IllegalArgumentException if owner is an empty string
     */
    public void setOwner(String owner) {

        this.owner = owner;
    }

    /**
     * Sets the MAC address of the smart object
     * @param macAddress The MAC address to be set
     * @throws Exception 
     * @throws NullPointerException if macAddress is null
     * @throws IllegalArgumentException if the length of macAddress is different than 12
     */
    public void setMACAddress(String macAddress) throws Exception {
        if(macAddress == null) Utils.error(new NullPointerException());
        if(macAddress.length() != 12) Utils.error(new IllegalArgumentException("The MAC Address must be made of 12 digits"));
        this.MACAddress = macAddress;
    }
}
