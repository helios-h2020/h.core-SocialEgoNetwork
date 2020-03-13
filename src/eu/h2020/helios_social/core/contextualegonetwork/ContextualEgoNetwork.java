package eu.h2020.helios_social.core.contextualegonetwork;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class implements a Contextual Ego Network, which is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 */
public class ContextualEgoNetwork {
    private Node ego;
    private ArrayList<Context> contexts;
    private Context currentContext;
    
    @Serializer.Serialization(enabled=false)
    private ArrayList<ContextualEgoNetworkListener> listeners = new ArrayList<ContextualEgoNetworkListener>();
    
    /**
     * Creates a ContextualEgoNetwork.
     * @param ego The ego Node
     * @deprecated This constructor will be made protected in future versions.
     * 	Call ContextualEgoNetwork.createOrLoad(egoName, egoData)
     * 	and NOT ContextualEgoNetwork(new Node(egoName, egoData)).
     */
    public ContextualEgoNetwork(Node ego) {
        if(ego == null) Utils.error(new NullPointerException());
        this.ego = ego;
        contexts = new ArrayList<Context>();
        getSerializer().removePreviousSaved();
        getSerializer().registerId(this, "CEN");
        getSerializer().registerId(ego, ego.getId());
    }
    
    private ContextualEgoNetwork() {
    }
    
    public void addListener(ContextualEgoNetworkListener listener) {
    	listeners.add(listener);
    	listener.init(this);
    }
    
    public ArrayList<ContextualEgoNetworkListener> getListeners() {
    	return new ArrayList<ContextualEgoNetworkListener>(listeners);
    }
    
    /**
     * Instantiates a ContextualEgoNetwork by creating a new ego node with the given data.
     * Loads a previously saved one if such a node exists.
     * @param egoName - The name of the ego network's ego.
     * @param egoData - The data with which to create the network's node.
     * @return the created or loaded contextual ego network
     */
    public static ContextualEgoNetwork createOrLoad(String egoName, Object egoData) {
    	//TODO: ensure that multiple instances of the same CEN cannot be loaded at the same time
    	String path = egoName + File.separator;
    	ContextualEgoNetwork contextualEgoNetwork;
    	Serializer serializer = Serializer.getSerializer(path);
    	if(Files.exists(Paths.get(path + "CEN"))) {
    		contextualEgoNetwork = new ContextualEgoNetwork();
	    	serializer.registerId(contextualEgoNetwork, "CEN");
	    	serializer.reload(contextualEgoNetwork);
	    	serializer.reload(contextualEgoNetwork.ego);
	    	// make contexts know where to look for the serializer
	    	for(Context context : contextualEgoNetwork.contexts)
	    		context.contextualEgoNetwork = contextualEgoNetwork;
    	}
    	else {
    		contextualEgoNetwork = new ContextualEgoNetwork(new Node(egoName, egoData));
    		contextualEgoNetwork.save();
    	}
    	return contextualEgoNetwork;
    }
    
    /**
     * Creates an empty contextual ego network that corresponds to the ego with the given name (effectively determines
     * the stored path) and uses a deserializer to parse it and its ego into memory.
     * This requires that a ContextualEgoNetwork had been created through its constructor and saved in the past.
     * @param egoName The name of the ego node
     * @return the created ego network
     * @deprecated This function will be removed in future versions.
     * 	Call ContextualEgoNetwork.createOrLoad(egoName, null) instead.
     */
    public static ContextualEgoNetwork load(String egoName) {
    	String path = egoName + File.separator;
    	ContextualEgoNetwork contextualEgoNetwork = new ContextualEgoNetwork();
    	Serializer serializer = Serializer.getSerializer(path);
    	serializer.registerId(contextualEgoNetwork, "CEN");
    	serializer.reload(contextualEgoNetwork);
    	serializer.reload(contextualEgoNetwork.ego);
    	// make contexts know where to look for the serializer
    	for(Context context : contextualEgoNetwork.contexts)
    		context.contextualEgoNetwork = contextualEgoNetwork;
    	return contextualEgoNetwork;
    }
    
    /**
     * @return The path folder in which the ego network is saved
     */
    public String getPath() {
    	return ego.getId()+File.separator;
    }
    
    /**
     * @return The {@link Serializer} object used to save and load data in the
     * folder determined by {@link #getPath()}
     */
    public Serializer getSerializer() {
    	return Serializer.getSerializer(getPath());
    }
    
    /**
     * Makes the {@link #getSerializer()} save the contextual ego network. This
     * includes all explicitly serializeable objects, namely the contexts and nodes.
     */
	public void save() {
		Serializer serializer = getSerializer();
		serializer.saveAllRegistered();
	}
	
	/**
	 * Applies {@link Context#cleanup()} on all contexts. Non-saved changes are lost.
	 */
	public void cleanup() {
		for(Context context : contexts)
			context.cleanup();
	}
    
    /**
     * The default method to create a new context in a ContextualEgoNetwork.
     * @param data The data that the context should hold.
     * @return The newly created context
     */
    public Context createContext(Object data) {
    	Context context = new Context(this, data);
    	contexts.add(context);
		for(ContextualEgoNetworkListener listener : listeners)
			listener.onCreateContext(context);
    	return context;
    }
    
    /**
     * Returns a context that satisfies data.equals(context.getData()) .
     * Overriding the {@link Object#equals(Object)} function of the query data
     * can be used to affect the outcome of the obtained context.
     * If no such context exists, a new one is created.
     * @param data The query data that the created context should hold.
     * @return The found or newly created context
     */
    public Context getOrCreateContext(Object data) {
    	for(Context context : contexts)
    		if(data.equals(context.getData())) 
    			return context;
    	return createContext(data);
    }
    
    public Context getContextBySerializationId(String serializationId) {
    	Serializer serializer = getSerializer();
    	for(Context context : contexts)
    		if(context.getSerializationId().equals(serializationId))
    			return context;
    	return null;
    }
    
    /**
     * Removes a given context from the ContextualEgoNetwork's contexts.
     * @param context The given context
     */
    public void removeContext(Context context) {
    	if(context==null) Utils.error(new NullPointerException());
    	if(!contexts.contains(context)) Utils.error("Context not found");
    	contexts.remove(context);
    	context.removeFromStorage();
		for(ContextualEgoNetworkListener listener : listeners)
			listener.onRemoveContext(context);
    }
    
    /**
     * Method to grant safe access to all contexts of the contextual ego networks
     * @return an ArrayList of contexts
     */
    public ArrayList<Context> getContexts(){
    	return new ArrayList<Context>(contexts);
    }
    
    /**
     * Method to set a context in state Current.
     * @param context the context to be set as active, or null if no context should be active at the present time.
     */
    public void setCurrent(Context context) {
        if(context==null) Utils.error(new NullPointerException());
    	this.currentContext = context;
    }
    
    /**
     * Method to return the current context.
     * @return The current context.
     * @see setCurrent
     */
    public Context getCurrentContext() {
    	return this.currentContext;
    }
    
    /**
     * Get the ego node
     * @return the {@link Node} that serves as the ego of the contextual ego network
     */
    public Node getEgo() {
    	return ego;
    }
    
    /**
     * Searches for a node with the given id and if no such node is found, a new one is created using the given data.
     * @param nodeId The node's id
     * @param data The node's data (only used if a new node is created)
     * @return The found or created node.
     */
    public Node getOrCreateNode(String nodeId, Object data) {
    	if(nodeId==null) Utils.error(new NullPointerException());
    	if(nodeId.isEmpty()) Utils.error(new IllegalArgumentException());
    	Serializer serializer = getSerializer();
    	Object object = serializer.getObject(nodeId);
    	Node node = object==null?null:(Node)object;
    	if(node==null) {
    		node = new Node(nodeId, data);
    		serializer.registerId(node, node.getId());
    		for(ContextualEgoNetworkListener listener : listeners)
    			listener.onCreateNode(this, node);
    	}
    	return node;
    }
}

