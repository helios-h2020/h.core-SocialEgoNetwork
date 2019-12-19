package contextualegonetwork;

import contextualegonetwork.Node;

/**
 * This class implements a node in the social graph. Multiple instances of the same nodes can be found in
 * different Contexts.
 *  */
public final class Node {
    /**
     * Global identifier of the node
     */
    private String id;
    /**
     * Data carried by the node
     */
    private Object data;
    /**
     * Status of the node (if false offline, if true online)
     */
    private boolean online;
    /**
     * Timestamp of creation of the node
     */
    private long creationTime;
    /**
     * Number of times the node's status has been set to online
     */
    private long onlineCounter;

    /**
     * Constructor method
     * @param id identifier of the node
     * @param alias name of the node that appears on screen
     * @throws NullPointerException if id is null
     * @throws IllegalArgumentException if the id parameters is empty
     * @deprecated This constructor will be protected in future versions.
     * 	Call getOrCreateNode(id, data) of a ContextualEgoNetwork instance instead.
     */
    public Node(String id, Object data) {
        if(id == null) Utils.error(new NullPointerException());
        if(id.equals("")) Utils.error(new IllegalArgumentException("The node id or alias cannot be an empty string"));
        this.creationTime = Utils.getCurrentTimestamp();
        this.id = id;
        this.data = data;
    }

    /**
     * Used in deserialization
     */
    protected Node()
    {}

    /**
     * @return The number of times the node's state has been online
     */
    public long getOnlineCounter() {
        return this.onlineCounter;
    }

    /**
     * @return The timestamp of when the node was created
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * @return The data attached to the node
     */
    public Object getData()
    {
        return data;
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
        return ((double)onlineCounter)/((double)(Utils.getCurrentTimestamp()-creationTime));
    }

    /**
     *
     * @return The identifier of the node declared in its constructor
     */
    public String getId() {
        return this.id;
    }
}