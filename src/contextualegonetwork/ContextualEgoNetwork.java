package contextualegonetwork;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class implements a Contextual Ego Network, that is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 */
public class ContextualEgoNetwork {
    private Node ego;
    private ArrayList<Context> contexts;
    private Context currentContext;
    public String testattr = "testattr";
    
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
    
    /**
     * Creates a ContextualEgoNetwork or loads a previous one.
     * @param egoName
     * @param egoData
     * @return
     */
    public static ContextualEgoNetwork createOrLoad(String egoName, Object egoData) {
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
    	else
    		contextualEgoNetwork = new ContextualEgoNetwork(new Node(egoName, egoData));
    	return contextualEgoNetwork;
    }
    
    /**
     * Creates an empty contextual ego network that corresponds to the ego with the given name (effectively determines
     * the stored path) and uses a deserializer to parse it and its ego into memory.
     * This requires that a ContextualEgoNetwork had been created through its constructor and saved in the past.
     * @param egoName
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
    Serializer getSerializer() {
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
	 * Applies {@link Context#cleanup()} on all contexts
	 */
	public void cleanup() {
		for(Context context : contexts)
			context.cleanup();
	}
    
    /**
     * The default method to create a new context in this ContextualEgoNetwork.
     * @param Data the data that the context should hold.
     * @return The newly created context
     */
    public Context createContext(Object data) {
    	Context context = new Context(this, data);
    	contexts.add(context);
    	return context;
    }
    
    /**
     * Returns a context based that satisfies data.equals(context.getData()) .
     * Overriding the {@link Object#equals(Object)} function of the query data
     * can be used to affect the outcome of the obtained context.
     * If no such context exists, a new one is created.
     * @param data The query data that the context should hold.
     * @return The newly created context
     */
    public Context getOrCreateContext(Object data) {
    	for(Context context : contexts)
    		if(data.equals(context.getData()))
    			return context;
    	return createContext(data);
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
    }
    
    /**
     * Method to access the contexts
     * @return an ArrayList of Context objects
     */
    public ArrayList<Context> getContexts(){
    	return contexts;
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
     * @return the context labeled as current.
     */
    public Context getCurrentContext() {
    	return this.currentContext;
    }
    
    /**
     * Shortcut to get the node of the Ego
     * @return the Node object of the Ego
     */
    public Node getEgo() {
    	return ego;
    }
    
    /**
     * Creates a node for which all contexts are accessible.
     * @param nodeId
     * @param data
     * @return
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
    	}
    	return node;
    }
}

