package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This class implements a {@link ContextualEgoNetworkListener} that assigns node, context and edge timestamps.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class CreationListener implements ContextualEgoNetworkListener {
	public static class CreationTimestamp {
		private long creationTimestamp;
		public CreationTimestamp() {
			creationTimestamp = Utils.getCurrentTimestamp();
		}
		public long value() {
			return creationTimestamp;
		}
	}

	public synchronized void init(ContextualEgoNetwork contextualEgoNetwork) {
		contextualEgoNetwork.getEgo().getOrCreateInstance(CreationTimestamp.class);
	}
	
	public void onCreateNode(Node node) {
		node.getOrCreateInstance(CreationTimestamp.class);
	}
	
	public void onCreateContext(Context context) {
		context.getOrCreateInstance(CreationTimestamp.class);
	}
	
	public void onCreateEdge(Edge edge) {
		edge.getOrCreateInstance(CreationTimestamp.class);
	}
	
	public void onCreateInteraction(Interaction interaction) {
		//timestamps are an organic part of interactions and don't need to be tracked by this listener
	}
}
