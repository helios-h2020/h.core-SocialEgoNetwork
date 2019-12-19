package contextualegonetwork;

import java.util.ArrayList;

import contextualegonetwork.Node;

/**
 * This class implements an edge of the Social Graph. An edge, in a context, can link the ego of the Contextual Ego Network to one of the alters or two of the alters.
 * Each edge is enriched by temporal information, particularly the one related to the creation timestamp, and with social information, that is the
 * interactions that take place between the source and the destination nodes in the life span of the edge (this information is stored on the source and destination nodes).
 * Moreover, the edge has a weight, which is its tie strenght, i.e. the number of interactions on the edge divided by the number of seconds passed since the creation date.
 */
public final class Edge {
    /**
     * UNIX timestamp of the creation time of the edge
     */
    private long timeCreated;
    /**
     * Source node
     */
    private Node src;
    /**
     * Destination node
     */
    private Node dst;
    /**
     * The context of the edge
     */
    private Context context;
    /**
     * The list of interactions
     */
    private ArrayList<Interaction> interactions;

    /**
     * Constructor method
     * @param src The source node of the edge
     * @param dst The destination node of the edge
     * @param identifier Id of the edge
     * @throws NullPointerException if src or dst are null
     */
    Edge(Node src, Node dst, Context context)
    {
        if(src == null || dst == null) Utils.error(new NullPointerException());
        this.timeCreated = Utils.getCurrentTimestamp();
        this.src = src;
        this.dst = dst;
        this.context = context;
        interactions = new ArrayList<Interaction>();
    }

    /**
     * Used in deserialization
     */
    protected Edge()
    {}

    /**
     * @return The source node of the edge
     */
    public Node getSrc()
    {
        return src;
    }

    /**
     * @return The destination node of the edge
     */
    public Node getDst()
    {
        return dst;
    }

    /**
     * @return The context
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * @return The timestamp of the edge
     */
    public long getCreationTime() {
        return this.timeCreated;
    }
    

    /**
     * Adds a new interaction with no duration on this edge
     * @param type The type of the interaction
     * @return the created interaction
     */
    public Interaction addDetectedInteraction(String type) {
    	return addInteraction(Utils.getCurrentTimestamp(), 0, type);
    }

    /**
     * Adds a new interaction on this edge
     * @param timestamp The start timestamp of the interaction
     * @param duration The duration of the interaction
     * @param type The type of the interaction
     * @return 
     */
    public Interaction addInteraction(long timestamp, int duration, String type) {
        if(type == null) Utils.error(new NullPointerException());
        if(timestamp < 0 || duration < 0) Utils.error(new IllegalArgumentException("Timestamp and duration cannot be negative"));
        if(type.equals("")) Utils.error(new IllegalArgumentException("Type cannot be empty"));
        Interaction interaction = new Interaction(timestamp, duration, type);
        interactions.add(interaction);
        return interaction;
    }

    /**
     * @return The weight of the edge. i.e. the tie strength between the source and the destination
     *          (the number of interactions that have taken place on this edge divided by the life span of the edge)
     */
    public double getTieStrength() {
        long now = Utils.getCurrentTimestamp();
        double elapsed = (double) (now - this.timeCreated);
        if(elapsed==0)
        	return 0;
        return (double) (this.interactions.size()) / (double) (now - this.timeCreated);
    }
    
}
