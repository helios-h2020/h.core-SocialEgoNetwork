package eu.h2020.helios_social.core.contextualegonetwork;

import java.util.ArrayList;

/**
 * This class implements an edge of the Social Graph. An edge, in a context, can link the ego of the Contextual Ego Network to one of the alters.
 * Each edge comprises multiple interactions.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 * @author Barbara Guidi (guidi@di.unipi.it)
 * @author Andrea Michienzi (andrea.michienzi@di.unipi.it)
 * @author Laura Ricci (laura.ricci@unipi.it)
 * @author Fabrizio Baiardi (f.baiardi@unipi.it)
 */
public final class Edge extends CrossModuleComponent {
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
     * Constructor method.
     * @param src The source node of the edge
     * @param dst The destination node of the edge
     * @param context The context the edge appears in
     * @throws NullPointerException if src or dst are null
     */
    Edge(ContextualEgoNetwork contextualEgoNetwork, Node src, Node dst, Context context)
    {
    	super(contextualEgoNetwork);
        if(src == null || dst == null) Utils.error(new NullPointerException());
    	assertSameContextualEgoNetwork(src);
    	assertSameContextualEgoNetwork(dst);
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
     * Retrieves the source node of the edge.
     * @return The source node.
     */
    public Node getSrc() {
        return src;
    }

    /**
     * Retrieves the destination node of the edge.
     * @return The destination node.
     */
    public Node getDst() {
        return dst;
    }
    
    /**
     * Retrieves the ego node of the edge context's ego network if that ego a member of the edge.
     * @return The ego node of the edge, null otherwise.
     */
    public Node getEgo() {
    	Node ego = getContext().getContextualEgoNetwork().getEgo();
    	if(ego==src || ego==dst)
    		return ego;
    	return null;
    }
    
    /**
     * Retrieve the edge's endpoint that is not the ego of the edge context's ego network if getEgo() finds an ego endpoint.
     * @return The alter endpoint of the edge.
     * @exception RuntimeException if the edge doesn't contain the contextual ego network's ego.
     */
    public Node getAlter() {
    	Node ego = getContext().getContextualEgoNetwork().getEgo();
    	if(ego==src)
    		return dst;
    	if(ego==dst)
    		return src;
    	Utils.error("Cannot retrieve alter for an edge that doesn't contain the ego");
    	return null;
    }

    /**
     * Retrieves the context the edge belongs to.
     * @return The edge's context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Adds a new interaction with no duration on this edge at the current timestamp.
     * @param data The data to be stored in the interaction
     * @return the created interaction
     */
    public Interaction addDetectedInteraction(Object data) {
    	return addInteraction(Utils.getCurrentTimestamp(), 0, data);
    }

    /**
     * Creates and adds a new interaction on this edge.
     * @param timestamp The start timestamp of the interaction
     * @param duration The duration of the interaction
     * @param data The data stored in the interaction
     * @return the created interaction
     */
    public Interaction addInteraction(long timestamp, long duration, Object data) {
        if(timestamp < 0 || duration < 0) Utils.error(new IllegalArgumentException("Timestamp and duration cannot be negative"));
        Interaction interaction = new Interaction(this, timestamp, duration, data);
        interactions.add(interaction);
        for(ContextualEgoNetworkListener listener : getContext().getContextualEgoNetwork().getListeners())
        	listener.onCreateInteraction(interaction);
        return interaction;
    }

    /**
     * Retrieves a shallow copy of the edge's interaction list.
     * @return A list of interactions.
     */
    public ArrayList<Interaction> getInteractions() {
    	return new ArrayList<Interaction>(interactions);
    }
    
}
