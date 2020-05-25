package eu.h2020.helios_social.core.contextualegonetwork;

/**
 * An interface of listeners that can be added on a {@link ContextualEgoNetwork} to listen to structural changes.
 * Modeled callbacks are triggered by the respective components. Call {@link ContextualEgoNetwork#addListener(ContextualEgoNetworkListener)}
 * to add a listener to a network. Callbacks are not polymorphised to improve understanding when implementing new listeners.<br/>
 * 
 * <b>Be careful not to keep references to removed nodes and contexts</b> as these could potentially cause memory leaks (optimally, refrain
 * from keeping any references to such objects, but otherwise use the respective onRemove callbacks).
 * 
 * Listeners are <i>not</i> saved alongside the network and must be added upon load,
 * i.e. immediately after calling {@link ContextualEgoNetwork#createOrLoad(String, String, Object)}
 * 
 * @author Emmanouil Krasanakis (maniospas@hotmail.com)
 */
public interface ContextualEgoNetworkListener {
	/**
	 * Called when the listener is added to a contextual ego network. Checks for ensuring that the listener is assigned to only one network
	 * (if needed) must be implemented on the respective listener. However, it is not mandatory to keep the network the listener is being added
	 * to, as this can be retrieved from the {@link CrossModuleComponent#getContextualEgoNetwork()} method of nodes, contexts and edges.
	 * 
	 * @param contextualEgoNetwork The contextual ego network the listener has been added to.
	 */
	public default void init(ContextualEgoNetwork contextualEgoNetwork) {}
	
	/**
	 * Called when a new node is created by the {@link ContextualEgoNetwork#getOrCreateNode(String, Object)} method (after the created
	 * node is added to the network).
	 * @param node The created node.
	 */
	public default void onCreateNode(Node node) {}

	/**
	 * Called when a node is removed from the contextual ego network by the {@link ContextualEgoNetwork#removeNodeIfExists(String)}
	 * method. Contrary to creation callbacks, this is called <i>before</i> the node is removed from the network. This callback
	 * is different than {@link #onRemoveNode(Context, Node)}, although the latter will necessarily have been called before reaching
	 * this one, as nodes removed from the network are also removed from all contexts first.
	 * @param node The removed node.
	 */
	public default void onRemoveNode(Node node) {}
	
	/**
	 * Called when a new context is created by the {@link ContextualEgoNetwork#getOrCreateContext(Object)} method (after the created 
	 * context is added to the network).
	 * @param context The created context.
	 */
	public default void onCreateContext(Context context) {}
	
	/**
	 * Called when the {@link Context#load()} method of a context is called. Note that this method is often called by other methods
	 * of the {@link Context} class to retrieve the context's data if they have been removed from the memory through {@link Context#cleanup()}.
	 * The callback is called after all context data are retrieved to memory.
	 * @param context The loaded context.
	 */
	public default void onLoadContext(Context context) {}
	
	/**
	 * Called when the {@link Context#save()} method of a context is called (after the save has completed). This method is also implicitly
	 * called by {@link ContextualEgoNetwork#save()} and {@link Context#cleanup()} and hence a callback will also be triggered at those times.
	 * @param context The saved context.
	 */
	public default void onSaveContext(Context context) {}
	
	/**
	 * Called when a context is removed from the contextual ego network by the {@link ContextualEgoNetwork#removeContext(Context)}
	 * method. Contrary to creation callbacks, this is called <i>before</i> the context is removed from the network.
	 * @param node The context to be removed.
	 */
	public default void onRemoveContext(Context context) {}
	
	/**
	 * Called when a node is added to a context (after the node is added).
	 * @param context The context.
	 * @param node The node added to the context.
	 */
	public default void onAddNode(Context context, Node node) {}
	
	/**
	 * Called when a node is removed from a context using the {@link Context#removeNode(Node)}.
	 * That method is also called by the {@link Context#removeNodeIfExists(Node)} and {@link ContextualEgoNetwork#removeNodeIfExists(String)} 
	 * methods, so this callback occurs on those operations too. 
	 * Contrary to creation callbacks, this is called <i>before</i> the node is removed from the context. If the node has edges,
	 * {@link #onRemoveEdge(Edge)} callbacks of those edges will follow.
	 * @param context The context.
	 * @param node The node just removed from the context.
	 */
	public default void onRemoveNode(Context context, Node node) {}
	
	/**
	 * Called when an edge is created in a context using the {@link Context#addEdge(Node, Node)} method (after the edge is created).
	 * That method is also called by the {@link Context#getOrAddEdge(Node, Node)} method. Note that the context can be retrieved from
	 * the edge using the {@link Edge#getContext()} method.
	 * @param edge The created edge.
	 */
	public default void onCreateEdge(Edge edge) {}
	
	/**
	 * Called when an is removed from a context using the {@link Context#removeEdge(Node, Node)} method.
	 * That method is also called by the {@link Context#removeNodeIfExists(Node)} and {@link Context#removeNode(Node)}.
	 * Contrary to creation callbacks, this is called <i>before</i> the edge is removed from the context.
	 * @param edge The edge to be removed.
	 */
	public default void onRemoveEdge(Edge edge) {}
	
	/**
	 * Called when an interaction is created (after it has been added to an edge). To retrieve the interaction's edge use the
	 * {@link Interaction#getEdge()} method.
	 * @param interaction The created interaction.
	 */
	public default void onCreateInteraction(Interaction interaction) {}
}
