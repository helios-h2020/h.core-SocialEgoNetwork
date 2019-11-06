package contextualegonetwork;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import contextualegonetwork.Node;

/**
 * This class implements a node in the Social Graph, which is representative of a node in a particular
 * layer of the HSG. Nodes are mapped in-between layers through a mapping function, for which two nodes
 * in different layers correspond to each other if they have the identifier
 */
@JsonIdentityInfo(generator=JSOGGenerator.class)
public class Node {

    /**
     * Global identifier of the node
     */
    private String id;
    /**
     * String data carried by the node
     */
    private String data;
    /**
     * Object data carried by the node
     */
    private Object dataObject;
    /**
     * Class of the object data carried by the node
     */
    private String dataClass;
    /**
     * Alias of the person/smart object
     */
    private String alias;
    /**
     * Status of the node (if false offline, if true online)
     */
    private boolean online;
    /**
     * Timestamp of creation of the node
     */
    private long creation_date;
    /**
     * Number of times the node's status is set to online
     */
    private long online_counter;

    /**
     * Constructor method
     * @param id identifier of the node
     * @param alias name of the node that appears on screen
     * @throws NullPointerException if the alias or id parameters are null
     * @throws IllegalArgumentException if the alias or id parameters are empty
     */
    public Node(String id, String alias)
    {
        if(alias ==null || id == null) Utils.error(new NullPointerException());
        if(alias.equals("") || id.equals("")) Utils.error(new IllegalArgumentException("The node id cannot be an empty string"));
        this.alias = alias;
        this.creation_date= System.currentTimeMillis();
        this.id = id;
    }

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Node()
    {}

    /**
     * @return The number of times the node's state has been online
     */
    public long getOnline_counter() {
        return this.online_counter;
    }

    /**
     * Sets the creation date of the node to a specific timestamp passed as argument
     * @param date The timestamp to be set to the creation date
     */
    public void setCreation_date(long date) {
        this.creation_date=date;  
    }

    /**
     * @return The timestamp of when the node was created
     */
    public long getCreation_date() {
        return this.creation_date;
    }

    /**
     * Sets the field dataClass
     * @param data The class to be set, which is the class of dataObject
     * @throws NullPointerException if data is null
     */
    public void setDataClass(String data) {
        if(data == null) throw new NullPointerException();
        this.dataClass= data;
    }

    /**
     * 
     * @return The class of dataObject
     */
    public String getDataClass()
    {
        return this.dataClass;
    }

    /**
     * Attaches an object data to the node (if the field was not empty it gets overwritten)
     * @param obj The object data to attach
     * @throws NullPointerException if obj is null
     */
    public void setDataObject(Object obj)
    {
        if(obj == null) throw new NullPointerException();
        this.dataObject = obj;
    }

    /**
     *
     * @return The object data attached to the node
     */
    public Object getDataObject()
    {
        return this.dataObject;
    }

    /**
     *
     * @return The string data attached to the node
     */
    public String getData()
    {
        return data;
    }

    /**
     * Sets the string data field of the node to a string passed as parameter (if the field was not empty it gets overwritten)
     * @param data The string data to be attached to the node
     * @throws NullPointerException if data is null
     */
    public void setData(String data)
    {
        if(data == null) throw new NullPointerException();
        this.data = data;
    }

    /**
     * @return The alias of the node
     */
    public String getAlias()
    {
        return this.alias;
    }

    /**
     * Sets the status to online or offline
     */
    public void setOnlineStatus(boolean online) {
        this.online = online;
    }
    
    /**
     * @return The status of the node (true if online, false if offline)
     */
    public boolean getOnlineStatus()
    {
        return this.online;
    }

    /**
     * @return A double, which is the score of the node (number of times online divided life span of the node)
     */
    @JsonIgnore
    public double getScore() {
        return ((double)online_counter)/((double)(System.currentTimeMillis()-creation_date));
    }

    /**
     *
     * @return The identifier of the node
     */
    public String getId() {
        return this.id;
    }
}