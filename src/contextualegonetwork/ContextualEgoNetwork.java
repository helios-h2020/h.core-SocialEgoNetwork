package contextualegonetwork;

import java.util.ArrayList;

import contextualegonetwork.contextData.ContextData;

/**
 * This class implements a Contextual Ego Network, that is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 */
public class ContextualEgoNetwork {
    private final Node ego;
    private ArrayList<Context> contexts;
    
    public ContextualEgoNetwork(Node ego) {
        if(ego == null) Utils.error(new NullPointerException());
        this.ego = ego;
        contexts = new ArrayList<Context>();
    }
    
    public Context createContext(ContextData data) {
    	Context context = new Context(this, data);
    	contexts.add(context);
    	return context;
    }
    
    public Node getEgo() {
    	return ego;
    }

}

