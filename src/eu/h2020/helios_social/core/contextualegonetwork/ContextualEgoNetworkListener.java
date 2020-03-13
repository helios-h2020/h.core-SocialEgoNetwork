package eu.h2020.helios_social.core.contextualegonetwork;

public interface ContextualEgoNetworkListener {
	public default void init(ContextualEgoNetwork contextualEgoNetwork) {}
	public default void onCreateNode(ContextualEgoNetwork contextualEgoNetwork, Node node) {}
	public default void onCreateContext(Context context) {}
	public default void onLoadContext(Context context) {}
	public default void onSaveContext(Context context) {}
	public default void onRemoveContext(Context context) {}
	public default void onAddNode(Context context, Node node) {}
	public default void onRemoveNode(Context context, Node node) {}
	public default void onCreateEdge(Edge edge) {}
	public default void onRemoveEdge(Edge edge) {}
	public default void onCreateInteraction(Interaction interaction) {}
}
