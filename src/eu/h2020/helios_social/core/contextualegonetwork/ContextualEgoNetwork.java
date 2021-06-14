package eu.h2020.helios_social.core.contextualegonetwork;

import java.io.File;
import java.util.ArrayList;

/**
 * This class implements a Contextual Ego Network, which is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 * @author Barbara Guidi (guidi@di.unipi.it)
 * @author Andrea Michienzi (andrea.michienzi@di.unipi.it)
 */
public class ContextualEgoNetwork {
    private Node ego;
    private ArrayList<Context> contexts;
    private ArrayList<Node> alters;
    private Context currentContext;
    @Serializer.Serialization(enabled=false)
    private Serializer serializer;
    @Serializer.Serialization(enabled=false)
    private ArrayList<ContextualEgoNetworkListener> listeners = new ArrayList<ContextualEgoNetworkListener>();
    
    /**
     * Creates a ContextualEgoNetwork.
     * @param internalStoragePath The path to the internal storage location (can be any path)
     * @param ego The ego Node
     */
    protected ContextualEgoNetwork(Storage storage, Node ego) {
        if(ego == null) Utils.error(new NullPointerException());
        this.ego = ego;
        contexts = new ArrayList<Context>();
        alters = new ArrayList<Node>();
        serializer = Serializer.getInstance(storage);
        serializer.removePreviousSaved();
        serializer.registerId(this, "CEN");
        serializer.registerId(ego, ego.getId());
    }
    
    /**
     * Constructor used by the {@link Serializer} for deserialization into an initially empty network.
     */
    private ContextualEgoNetwork() {}

    /**
     * Attaches a {@link ContextualEgoNetworkListener} to the contextual ego network, which 
     * will be called on the respective events.
     * 
     * @param listener The lister to attach
     */
    public void addListener(ContextualEgoNetworkListener listener) {
    	listeners.add(listener);
    	listener.init(this);
    }

    /**
     * Obtain all listeners attached to the contextual ego network
     * 
     * @return A list of listeners
     * @see #addListener(ContextualEgoNetworkListener)
     */
    public ArrayList<ContextualEgoNetworkListener> getListeners() {
    	return new ArrayList<ContextualEgoNetworkListener>(listeners);
    }

    /**
     * Instantiates a ContextualEgoNetwork at the given storage path by creating a new ego node with the given data.
     * Loads a previously saved one if such a node exists. Since method version 1.1.0 this method depends on the storage 
     * management of {@link eu.h2020.helios_social.core.contextualegonetwork.storage.NativeStorage}.
     * @param internalStoragePath The path to the internal storage location (can be any path)
     * @param egoName The name of the ego network's ego.
     * @param egoData The data with which to create the network's node.
     * @return the created or loaded contextual ego network
     * @deprecated Prefer {@link #createOrLoad(Storage, String, Object)}, which this method wraps.
     * This method remains only to ensure compatibility with previous versions and may be removed in future versions.
     */
    public static ContextualEgoNetwork createOrLoad(String internalStoragePath, String egoName, Object egoData) {
    	if(!internalStoragePath.isEmpty() && !internalStoragePath.endsWith(File.separator) && !internalStoragePath.endsWith("\\") && !internalStoragePath.endsWith("/"))
				internalStoragePath += File.separator;
    	String path = internalStoragePath + egoName + File.separator;
    	Storage storage = Storage.getInstance(path, eu.h2020.helios_social.core.contextualegonetwork.storage.LegacyStorage.class);
    	return createOrLoad(storage, egoName, egoData);
    }
    
    /**
     * Instantiates a ContextualEgoNetwork at the given storage path  by creating a new ego node with the given data.
     * Loads a previously saved one if such a node exists.
     * @param internalStoragePath The path to the internal storage location (s)
     * @param egoName The name of the ego network's ego.
     * @param egoData The data with which to create the network's node.
     * @return the created or loaded contextual ego network
     */
    public static ContextualEgoNetwork createOrLoad(Storage storage, String egoName, Object egoData) {
    	if(storage==null)
    		Utils.error(new IllegalArgumentException("null storage"));
    	if(egoName==null)
    		Utils.error(new IllegalArgumentException("null ego name"));
    	//TODO: potentially ensure that multiple instances of the same CEN cannot be loaded at the same time
    	ContextualEgoNetwork contextualEgoNetwork;
    	Serializer serializer = Serializer.getInstance(storage);
    	if(storage.fileExists("CEN.json")) {
    		contextualEgoNetwork = new ContextualEgoNetwork();
    		contextualEgoNetwork.serializer = serializer;
	    	serializer.registerId(contextualEgoNetwork, "CEN");
	    	serializer.reload(contextualEgoNetwork);
	    	serializer.reload(contextualEgoNetwork.ego);
	    	for(Node alter : contextualEgoNetwork.alters)
	    		serializer.reload(alter);
	    	// make contexts know where to look for the serializer
	    	for(Context context : contextualEgoNetwork.contexts) {
	    		context.contextualEgoNetwork = contextualEgoNetwork;
	    		if(!context.isLoaded())
	    			serializer.setSavePermission(context, false);
	    	}
    	}
    	else {
    		Node ego = new Node(null, egoName, egoData);
    		contextualEgoNetwork = new ContextualEgoNetwork(storage, ego);
    		ego.contextualEgoNetwork = contextualEgoNetwork;
    		contextualEgoNetwork.save();
    	}
    	return contextualEgoNetwork;
    }
    
    /*
     * Creates an empty contextual ego network that corresponds to the ego with the given name (effectively determines
     * the stored path) and uses a deserializer to parse it and its ego into memory.
     * This requires that a ContextualEgoNetwork had been created through its constructor and saved in the past.
     * @param egoName The name of the ego node
     * @return the created ego network
     * @deprecated This function will be removed in future versions.
     * 	Call ContextualEgoNetwork.createOrLoad(egoName, null) instead.
     */
    /*public static ContextualEgoNetwork load(String egoName) {
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
    }*/
    
    /**
     * Retrieves the Serializer responsible for saving and loading the ego network and its entities.
     * @return The {@link Serializer} object used to save and load data in the
     * folder determined by {@link #getPath()}
     */
    public Serializer getSerializer() {
    	return serializer;
    }
    
    /**
     * Makes the {@link #getSerializer()} save the contextual ego network. This
     * includes all explicitly serializeable objects, namely the contexts and nodes.
     * Only {@link Context#isLoaded()} contexts are saved, since otherwise they would 
     * have already been saved with {@link Context#cleanup()}.
     */
	public void save() {
		Serializer serializer = getSerializer();
		serializer.saveAllRegistered();
		for(Context context : contexts)
			if(context.isLoaded())
				for(ContextualEgoNetworkListener listener : listeners)
					listener.onSaveContext(context);
	}
	
	/**
	 * Applies {@link Context#cleanup()} to all contexts. Non-saved changes are lost.
	 * @see #save()
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
    protected Context createContext(Object data) {
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
     * @return The found or newly created context.
     */
    public Context getOrCreateContext(Object data) {
    	for(Context context : contexts)
    		if(data.equals(context.getData())) 
    			return context;
    	return createContext(data);
    }
    
    /**
     * Searches all ContextualEgoNetwork contexts for one with the same serializationId
     * assigned to it during serialization
     * @param serializationId The serializationId of the context
     * @return The found context, null if no such context found.
     */
    public Context getContextBySerializationId(String serializationId) {
    	for(Context context : contexts)
    		if(context.getSerializationId().equals(serializationId))
    			return context;
    	return null;
    }
    
    /**
     * Removes a given context from the ContextualEgoNetwork's contexts. Note that removing 
	 * a context does <i>not</i> remove any nodes from the network, even if those are not currently
	 * referenced by other contexts (because future contexts may use those nodes). However,
	 * it <i>removes the context's saved file from storage</i>. If the removed context is current context,
	 * the latter is also set to null. To avoid catastrophic missing references due to neglected saving, the network is 
     * <b>forcefully saved</b> after removing the node by calling its {@link #save()} method.
     * 
     * @param context The context to remove.
     */
    public void removeContext(Context context) {
    	if(context==null) Utils.error(new NullPointerException());
    	if(!contexts.contains(context)) Utils.error("Context not found");
		for(ContextualEgoNetworkListener listener : listeners)
			listener.onRemoveContext(context);
		if(context==currentContext) 
			currentContext = null;
    	contexts.remove(context);
    	context.removeFromStorage();
    	serializer.unregister(context);
    	save();
    }
    
    /**
     * Method to grant safe access to all contexts of the contextual ego network.
     * @return An ArrayList of contexts.
     */
    public ArrayList<Context> getContexts(){
    	return new ArrayList<Context>(contexts);
    }
    
    /**
     * Method to set a context as the current context.
     * @param context The context to be set as the current one, or null if no context should be active at the present time.
     */
    public void setCurrent(Context context) {
        if(context==null) Utils.error(new NullPointerException());
        if(!contexts.contains(context)) {Utils.error("Context does not reside in the ego network (has been probably removed)"); return;}
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
     * Retrieves the ego node.
     * @return The {@link Node} that serves as the ego of the contextual ego network.
     * @see #getAlters()
     */
    public Node getEgo() {
    	return ego;
    }

    /**
     * Retrieves the alters (not including the ego).
     * @return A list of {@link Node} alters.
     * @see #getEgo()
     */
    public ArrayList<Node> getAlters() {
    	return new ArrayList<Node>(alters);
    }
    
    /**
     * Searches for a node with the given id and, if no such node is found, creates a new one.
     * @param nodeId The node's id
     * @return The found or created node.
     * @see #getOrCreateNode(String, Object)
     */
    public Node getOrCreateNode(String nodeId) {
    	return getOrCreateNode(nodeId, null);
    }
    
    /**
     * Searches for a node with the given id and, if no such node is found, creates a new one using the given data.
     * @param nodeId The node's id
     * @param data The node's data (only used if a new node is created - can be null).
     * @return The found or created node.
     */
    public Node getOrCreateNode(String nodeId, Object data) {
    	if(nodeId==null) Utils.error(new NullPointerException());
    	if(nodeId.isEmpty()) Utils.error(new IllegalArgumentException());
    	Serializer serializer = getSerializer();
    	Object object = serializer.getObjectOrNull(nodeId);
    	Node node = object==null?null:(Node)object;
    	if(node==null) {
    		node = new Node(this, nodeId, data);
    		serializer.registerId(node, node.getId());
    		alters.add(node);
    		for(ContextualEgoNetworkListener listener : listeners)
    			listener.onCreateNode(node);
    	}
    	return node;
    }
    
    /**
     * Removes a node from the contextual ego network given its serialization id, which is the same as {@link Node#getId()}.
     * This includes removing the node from both the list of alters, as well as potentially loading any unloaded all contexts
     * (if so, they are unloaded afterwards). To avoid catastrophic missing references due to neglected saving, the network is 
     * <b>forcefully saved</b> after removing the node by calling its {@link #save()} method.
     * If no node with the given id is part of the network, none of the above happens. 
     * @param nodeId The serialization id of the node to remove.
     */
    public void removeNodeIfExists(String nodeId) {
    	Serializer serializer = getSerializer();
    	Object object = serializer.getObjectOrNull(nodeId);
    	Node node = object==null?null:(Node)object;
    	if(node!=null) {
	    	for(Context context : contexts) {
	    		boolean isLoaded = context.isLoaded();
	    		context.removeNodeIfExists(node);
	    		if(!isLoaded)
	    			context.cleanup();
	    	}
    		for(ContextualEgoNetworkListener listener : listeners)
    			listener.onRemoveNode(node);
	    	alters.remove(node);
	    	serializer.removeFromStorage(node);
	    	serializer.unregister(node);
	    	save();
    	}
    }
}

