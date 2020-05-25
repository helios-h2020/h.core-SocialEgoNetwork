package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;
import eu.h2020.helios_social.core.contextualegonetwork.Node;

/**
 * This class implements a {@link ContextualEgoNetworkListener} that logs contextual ego network events.
 * 
 * @author Emmanouil Krasanakis (maniospas@hotmail.com)
 */
public class LoggingListener implements ContextualEgoNetworkListener {
	//private static Logger logger = Logger.getGlobal();
	
	private void info(String text) {
		System.out.println(text);
	}
	public synchronized void init(ContextualEgoNetwork contextualEgoNetwork) {
		info("Started logging on CEN of node: "+contextualEgoNetwork.getEgo().getId());
	}
	public void onCreateNode(Node node) {
		info("Created node: "+node.getId());
	}
	public void onCreateContext(Context context) {
		info("Created context: "+context.getData().toString());
	}
	public void onLoadContext(Context context) {
		info("Loaded context: "+context.getData().toString());
	}
	public void onSaveContext(Context context) {
		info("Loaded context: "+context.getData().toString());
	}
	public void onRemoveContext(Context context) {
		info("Removed context: "+context.getData().toString());
	}
	public void onAddNode(Context context, Node node) {
		info("Added a node to context: "+context.getData().toString());
	}
	public void onRemoveNode(Context context, Node node) {
		info("Removed a node from context: "+context.getData().toString());
	}
	public void onCreateEdge(Edge edge) {
		info("Added an edge to context: "+edge.getContext().getData().toString());
	}
	public void onRemoveEdge(Edge edge) {
		info("Removed an edge from context: "+edge.getContext().getData().toString());
	}
	public void onCreateInteraction(Interaction interaction) {
		info("Added a "+toString(interaction.getData())+" interaction to context: "+interaction.getEdge().getContext().getData().toString());
	}
	protected static String toString(Object object) {
		if(object==null)
			return "null";
		else
			return object.toString();
	}
}
