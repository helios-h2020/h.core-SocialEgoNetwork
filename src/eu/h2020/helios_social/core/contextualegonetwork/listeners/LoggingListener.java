package eu.h2020.helios_social.core.contextualegonetwork.listeners;

import java.util.logging.Logger;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetworkListener;
import eu.h2020.helios_social.core.contextualegonetwork.Edge;
import eu.h2020.helios_social.core.contextualegonetwork.Interaction;
import eu.h2020.helios_social.core.contextualegonetwork.Node;

public class LoggingListener implements ContextualEgoNetworkListener {
	private static Logger logger = Logger.getGlobal();
	@Override
	public void onCreateNode(ContextualEgoNetwork contextualEgoNetwork, Node node) {
		logger.info("Created node: "+node);
	}
	@Override
	public void onCreateContext(Context context) {
		logger.info("Created context: "+context);
	}
	@Override
	public void onSaveContext(Context context) {
		logger.info("Saved context: "+context);
	}
	@Override
	public void onCreateEdge(Edge edge) {
		logger.info("Created edge: "+edge);
	}
	@Override
	public void onCreateInteraction(Interaction interaction) {
		logger.info("Created interaction: "+interaction);
	}
}
