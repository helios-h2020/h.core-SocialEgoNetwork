package contextualegonetwork;

import com.fasterxml.jackson.annotation.JsonCreator;

class SmartObject extends Node {

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

    /**
     * Used in deserialization
     */
    @JsonCreator
    public SmartObject()
    {}

    /**
     * Constructor method
     * @param username Username of the node
     * @param id Numeric identifier of the node
     * @param macAddr MAC Address of the devide
     * @param man Manufacturer of the device
     * @param own Name of the owner of the device
     * @throws NullPointerException if username, macAddr, man or own are null
     * @throws IllegalArgumentException if the length of macAddr is different than 12, or if man or own are empty strings
     */
    public SmartObject(String username, long id, String macAddr, String man, String own) {
        super(username, id);
        if(username == null || macAddr == null || man == null || own == null) throw new NullPointerException();
        if(macAddr.length() != 12) throw new IllegalArgumentException("The MAC Address must be made of 12 digits");
        if(man.equals("") || own.equals("")) throw new IllegalArgumentException("Owner and manufacturer cannot be empty strings");
        this.MACAddress = macAddr;
        this.manufacturer = man;
        this.owner = own;
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
     * @throws NullPointerException if macAddress is null
     * @throws IllegalArgumentException if the length of macAddress is different than 12
     */
    public void setMACAddress(String macAddress) {
        if(macAddress == null) throw new NullPointerException();
        if(macAddress.length() != 12) throw new IllegalArgumentException("The MAC Address must be made of 12 digits");
        this.MACAddress = macAddress;
    }
}
