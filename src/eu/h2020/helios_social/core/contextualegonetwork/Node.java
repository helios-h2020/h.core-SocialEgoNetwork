package eu.h2020.helios_social.core.contextualegonetwork;

import eu.h2020.helios_social.core.contextualegonetwork.Node;

/**
 * This class implements a node in the social graph. All contexts share the same instances of the same nodes.
 *  */
public final class Node extends CrossModuleComponent {
    /**
     * Global identifier of the node
     */
    private String id;
    /**
     * Data carried by the node
     */
    private Object data;

    /**
     * Call getOrCreateNode(id, data) of a ContextualEgoNetwork instance to create nodes.
     * @param id identifier of the node
     * @param data data to store inside the node (can be null)
     * @throws NullPointerException if id is null
     * @throws IllegalArgumentException if the id parameters is empty
     */
    protected Node(String id, Object data) {
        if(id == null) Utils.error(new NullPointerException());
        if(id.equals("")) Utils.error(new IllegalArgumentException("The node id or alias cannot be an empty string"));
        this.id = id;
        this.data = data;
    }

    /**
     * Used in deserialization
     */
    protected Node()
    {}
    
    /**
     * @return The data attached to the node
     */
    public Object getData()
    {
        return data;
    }
 
    /**
     *
     * @return The identifier of the node declared in its constructor
     */
    public String getId() {
        return this.id;
    }
}