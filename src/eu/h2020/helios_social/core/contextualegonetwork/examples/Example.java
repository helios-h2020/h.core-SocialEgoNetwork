package eu.h2020.helios_social.core.contextualegonetwork.examples;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.CreationListener;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.LoggingListener;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.RecoveryListener;

public class Example {

	public static void main(String[] args) {
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.createOrLoad("", "user-00001", null);
		
		egoNetwork.addListener(new RecoveryListener());//automatic saving with minimal overhead
		egoNetwork.addListener(new CreationListener());//keep timestamps
		egoNetwork.addListener(new LoggingListener());//print events
		
		// common operations
		Node user1 = egoNetwork.getEgo();
		Node user2 = egoNetwork.getOrCreateNode("user-00002", null);
		Node user3 = egoNetwork.getOrCreateNode("user-00003", null);
		Context context = egoNetwork.getOrCreateContext("Test Context");
		context.getOrAddEdge(user1, user2);
		context.getOrAddEdge(user1, user3);
		context.getOrAddEdge(user2, user3).addDetectedInteraction(null);
		context.getOrAddEdge(user2, user3).addDetectedInteraction("HANDSHAKE2");
		egoNetwork.save();
	}

}
