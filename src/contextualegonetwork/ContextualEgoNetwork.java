package contextualegonetwork;

import java.util.ArrayList;

import contextualegonetwork.contextData.ContextData;
import contextualegonetwork.contextData.ContextStates;
import contextualegonetwork.contextData.DefaultContextData;

/**
 * This class implements a Contextual Ego Network, that is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 */
public class ContextualEgoNetwork {
    private final Node ego;
    private ArrayList<Context> contexts;
    private Context currentContext;
    
    public ContextualEgoNetwork(Node ego) {
        if(ego == null) Utils.error(new NullPointerException());
        this.ego = ego;
        contexts = new ArrayList<Context>();
    }
    
    /**
     * The default method to create a new context in this ContextualEgoNetwork.
     * @param data the data that the context should hold.
     * @return the newly created context
     */
    public Context createContext(ContextData data) {
    	Context context = new Context(this, data);
    	contexts.add(context);
    	return context;
    }
    
    /**
     * Method to set the (current) contexts
     * @param contexts an ArrayList<Context> containing the (current) contexts
     */
    public void setContexts(ArrayList<Context> contexts){
    	if(contexts==null) Utils.error(new NullPointerException("Cannot pass null as the set of (active) contexts"));
    	this.contexts=contexts;
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
    public void setContextCurrent(Context context) {
    	this.currentContext=context;
    }
    
    /**
     * Method to return the current context.
     * @return the context labeled as current.
     */
    public Context getCurrentContext() {
    	return this.currentContext;
    }
    
    /**
     * Method to change the state of a context. For setting a context to the state current see {@link ContextualEgoNetwork#setContextCurrent(Context)}
     * @param context the context whose state is changed and the new state of the context.
     * @param state the new state of the context.
     */
    public void setContextState(Context context, ContextStates state) {
    	((DefaultContextData)context.getDataObject()).status=state;
    }
    
    /**
     * Shortcut to get the node of the Ego
     * @return the Node object of the Ego
     */
    public Node getEgo() {
    	return ego;
    }

}

