package contextualegonetwork;

import contextualegonetwork.Node;
import contextualegonetwork.nodeData.NodeData;

/**
 * This class implements a node in the Social Graph, which is representative of a node in a particular
 * layer of the HSG. Nodes are mapped in-between layers through a mapping function, for which two nodes
 * in different layers correspond to each other if they have the identifier
 */
public final class Node {
    /**
     * Global identifier of the node
     */
    private String id;
    /**
     * Data carried by the node
     */
    private NodeData data;
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
     * @throws NullPointerException if the alias, id or data parameters are null
     * @throws IllegalArgumentException if the alias or id parameters are empty
     */
    public Node(String id, String alias, NodeData data)
    {
        if(alias ==null || id == null || data==null) Utils.error(new NullPointerException());
        if(alias.equals("") || id.equals("")) Utils.error(new IllegalArgumentException("The node id cannot be an empty string"));
        this.alias = alias;
        this.creation_date= System.currentTimeMillis();
        this.id = id;
        this.data = data;
    }

    /**
     * Used in deserialization
     */
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
     * @return The data attached to the node
     */
    public NodeData getData()
    {
        return data;
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