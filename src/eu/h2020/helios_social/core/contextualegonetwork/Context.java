package eu.h2020.helios_social.core.contextualegonetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * This class implements a context of the Contextual Ego Network. The context stores all the information related
 * to the nodes, i.e. the actors that engage in interactions inside it,
 * and keeps track of its spatial and temporal characteristics.
 * The context can be unloaded from memory using the {@link #cleanup()} function and can be forced to be written anew
 * in the device's storage by calling the {@link #removeFromStorage()} method. If the context is unloaded from memory,
 * references to it remain and its data are reloaded on the first demand or by calling {@link #load()}.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 * @author Barbara Guidi (guidi@di.unipi.it)
 * @author Andrea Michienzi (andrea.michienzi@di.unipi.it)
 * @author Laura Ricci (laura.ricci@unipi.it)
 * @author Fabrizio Baiardi (f.baiardi@unipi.it)
 */
public final class Context extends CrossModuleComponent
{
    /**
     * Object data carried by the context
     */
    private Object data;
    /**
     * Graph nodes (i.e. alters) and edges
     */
    private ArrayList<Node> nodes;
    private HashMap<String, Edge> edges;

    /**
     * Constructor method.
     * @param name the name of the context
     * @param contextualEgoNetwork the ContextualEgoNetwork in which the context resides
     * @throws NullPointerException if name or contextualEgoNetwork are null
     * @throws IllegalArgumentException if name is an empty string
     */
    Context(ContextualEgoNetwork contextualEgoNetwork, Object data)
    {
        super(contextualEgoNetwork);
        if(data == null || data==null) Utils.error(new NullPointerException());
        this.data = data;
        nodes = new ArrayList<Node>();
        edges = new HashMap<String, Edge>();
        nodes.add(contextualEgoNetwork.getEgo());
        contextualEgoNetwork.getSerializer().registerId(this);
    }
    
    /**
     * Retrieves the id assigned to this context during serialization.
     * @return The context's unique string identifier.
     */
    public String getSerializationId() {
    	return getContextualEgoNetwork().getSerializer().getRegisteredId(this);
    }
    
    /**
     * If the context is loaded, it is serialized to a file. The context's nodes
     * are not serialized though
     * @return Whether the context was saved.
     */
    public boolean save() {
    	if(nodes==null) 
    		return false;
    	//for(Node node : nodes) 
        	//contextualEgoNetwork.getSerializer().save(node);
    	boolean succesfull = getContextualEgoNetwork().getSerializer().save(this);
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onSaveContext(this);
    	return succesfull;
    }
    
    /**
     * Removes any local files used to store the context (but retains the context in memory)
     * These files will be re-created once the context is saved again.
     */
    protected void removeFromStorage() {
    	getContextualEgoNetwork().getSerializer().removeFromStorage(this);
    }
    
    /**
     * Saves the context to a file and removes its data memory and it from the dynamic serializer
     * (so that universal save does not save it anymore)
     * @see #save()
     */
    public void cleanup() {
    	save();
    	getContextualEgoNetwork().getSerializer().setSavePermission(this, false);
    	nodes = null;
    	edges = null;
    }
    
    /**
     * Loads the context from the given serializer in memory.
     * This operation is automatically performed on-demand by other context access operations.
     */
    public void load() {
    	getContextualEgoNetwork().getSerializer().reload(this, 1);//loads all of its nodes too
    	getContextualEgoNetwork().getSerializer().setSavePermission(this, true);
    	Utils.log("Loaded context "+data.toString()+" with "+nodes.size()+" nodes, "+edges.size()+" edges");
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onLoadContext(this);
    }
    
    /**
     * Is used to assert that the context is loaded, producing an error if it's not
     * @return Whether the context is loaded
     * @see #isLoaded()
     */
    protected boolean assertLoaded() {
    	if(!isLoaded()) 
    		load();
    	return true;
    }
    
    /**
     * Checks whether the context has been loaded in memory.
     * @return <code>true</code> if the context has has been loaded, <code>false</code> otherwise.
     */
    public boolean isLoaded() { 
    	return nodes!=null;
    }

    /**
     * Used in deserialization
     */
    protected Context()
    {}

    /**
     * @return The data attached to the context
     */
    public Object getData()
    {
    	assertLoaded();
        return data;
    }

    /**
     * Retrieves a shallow copy of the context's node list.
     * @return A list of nods.
     */
    public ArrayList<Node> getNodes() {
    	assertLoaded();
    	return new ArrayList<Node>(nodes);
    }
    
    /**
     * Retrieves a shallow immutable copy of the context's edge list.
     * @return An edge list.
     */
    public ArrayList<Edge> getEdges() {
    	assertLoaded();
    	return new ArrayList<Edge>(edges.values());
    }

    /**
     * Creates an edge between two nodes of the social graph.
     * @param src The source node
     * @param dst The destination node
     * @return The created {@link Edge}
     * @throws NullPointerException If src or dst are null
     * @throws IllegalArgumentException If src and dst are the same node or if they don't belong to the context
     * @throws IllegalArgumentException If the edge already exists in the context
     */
    public Edge addEdge(Node src, Node dst)  {
    	assertLoaded();
        if(src == null || dst == null) return Utils.error(new NullPointerException(), null);
        if(src==dst) return Utils.error(new IllegalArgumentException("Src and dest cannot be the same node"), null);
        if(!nodes.contains(src) || !nodes.contains(dst)) return Utils.error(new IllegalArgumentException("Either source or destination nodes are not in context"), null);
        if(getEdge(src, dst)!=null)
        	return Utils.error(new IllegalArgumentException("Edge already exists in context (maybe you meant to add a new interaction on that edge instead)"), getEdge(src, dst));
        Edge edge = new Edge(getContextualEgoNetwork(), src, dst, this);
        edges.put(edge.getSrc().getId()+"@"+edge.getDst().getId(), edge);
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onCreateEdge(edge);
        return edge;
    }
    
    /**
     * Retrieves the edge (if it exists) between two nodes in the context.
     * @param src The source node
     * @param dst The destination node
     * @return The edge if the source and the destination nodes are in the social graph and are
     *         connected by an edge, null otherwise
     * @throws NullPointerException If src and dest are null
     * @throws IllegalArgumentException If src and dest are the same node
     */
    public Edge getEdge(Node src, Node dst) {
    	assertLoaded();
        if(src == null || dst == null) return Utils.error(new NullPointerException(), null);
        if(src==dst) return Utils.error(new IllegalArgumentException("Src and dest cannot be the same node"), null);
        return edges.get(src.getId()+"@"+dst.getId());
    }
    
    /**
     * Retrieves the edge (if it exists) between two nodes of the social graph or creates it if it doesn't exist.
     * If the nodes are not part of the context, then they are also added.
     * @param src The source node
     * @param dst The destination node
     * @return The found or created {@link Edge}
     * @see #getEdge(Node, Node)
     * @see #addEdge(Node, Node)
     */
    public Edge getOrAddEdge(Node src, Node dst) {
    	addNodeIfNecessary(src);
    	addNodeIfNecessary(dst);
    	Edge edge = getEdge(src, dst);
    	if(edge==null)
    		edge = addEdge(src, dst);
    	return edge;
    }

    /**
     * Removes an edge between a source and a destination node.
     * @param src The source node
     * @param dst The destination node
     * @return The removed edge, null if no such edge found
     * @throws NullPointerException If src and dst are null
     * @throws IllegalArgumentException If src and dst are the same node
     */
    public Edge removeEdge(Node src, Node dst) {
    	assertLoaded();
    	Edge edge = getEdge(src, dst);
    	if(edge==null)
    		return null;
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onRemoveEdge(edge);
    	edges.remove(edge.getSrc().getId()+"@"+edge.getDst().getId());
    	return edge;
    }

    /**
     * Adds a new node to the context.
     * @param node The node to be added
     * @throws NullPointerException if newNode is null
     */
    public void addNode(Node node) {
    	assertLoaded();
        if(node == null) Utils.error(new NullPointerException());
        if(nodes.contains(node)) {Utils.error("Node already in context"); return;}
        nodes.add(node);
        getContextualEgoNetwork().getSerializer().registerId(node, node.getId());
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onAddNode(this, node);
    }

    /**
     * Adds a node to the context if it's not already part of it. Does nothing if the node already exists in the context.
     * Node can be created through the {@link ContextualEgoNetwork#getOrCreateNode} function.
     * @param node The node to be added
     * @throws NullPointerException if newNode is null
     */
    public void addNodeIfNecessary(Node node) {
    	assertLoaded();
        if(node == null) Utils.error(new NullPointerException());
        if(!nodes.contains(node)) {
        	assertSameContextualEgoNetwork(node);
            nodes.add(node);
            getContextualEgoNetwork().getSerializer().registerId(node, node.getId());
            for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
            	listener.onAddNode(this, node);
        }
    }

    /**
     * Removes a node and its edges from the context
     * @param node The node to be removed
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is the ego of not part of the context
     */
    public void removeNode(Node node) {
    	assertLoaded();
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        removeNodeIfExists(node);
    }
    

    /**
     * Removes a node and its edges from the context if its part of it
     * @param node The node to be removed
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is the ego
     */
    public void removeNodeIfExists(Node node) {
    	assertLoaded();
        if(node == null) Utils.error(new NullPointerException());
        if(node == getContextualEgoNetwork().getEgo()) Utils.error(new IllegalArgumentException());
        if(!nodes.contains(node)) 
        	return;
        for(ContextualEgoNetworkListener listener : getContextualEgoNetwork().getListeners())
        	listener.onRemoveNode(this, node);
        for(Edge edge : getEdges())
        	if(edge.getSrc()==node || edge.getDst()==node)
        		edges.remove(edge.getSrc().getId()+"@"+edge.getDst().getId());
        nodes.remove(node);
    }

    /**
     * Retrieves the incoming edges of a given node.
     * @param node The given node
     * @return A stream of the incoming edges
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is not in the context
     */
    public Stream<Edge> getInEdges(Node node) {
    	assertLoaded();
        if(node == null) throw new NullPointerException();
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        return getEdges().stream().filter(edge -> edge.getDst()==node);
    }
    
    /**
     * Retrieves the outgoing edges of a given node.
     * @param node The given node
     * @return A stream of the outgoing edges
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is not in the context
     */
    public Stream<Edge> getOutEdges(Node node) {
    	assertLoaded();
        if(node == null) throw new NullPointerException();
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        return getEdges().stream().filter(edge -> edge.getSrc()==node);
    }
}