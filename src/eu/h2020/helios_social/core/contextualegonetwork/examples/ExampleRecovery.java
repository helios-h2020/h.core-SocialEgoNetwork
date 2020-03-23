package eu.h2020.helios_social.core.contextualegonetwork.examples;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
import eu.h2020.helios_social.core.contextualegonetwork.Serializer;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.RecoveryListener;

public class ExampleRecovery {
	public static ContextualEgoNetwork createNetworkWithRecovery() {
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.createOrLoad("", "user-00001", null);
		
		egoNetwork.addListener(new RecoveryListener());//insure against failing operation
		
		Node user1 = egoNetwork.getEgo();
		Node user2 = egoNetwork.getOrCreateNode("user-00002", null);
		Node user3 = egoNetwork.getOrCreateNode("user-00003", null);
		Context context = egoNetwork.getOrCreateContext("Test Context");
		context.getOrAddEdge(user1, user2);
		context.getOrAddEdge(user1, user3);
		context.getOrAddEdge(user2, user3).addDetectedInteraction("HANDSHAKE");
		
		return egoNetwork;
	}
	
	public static ContextualEgoNetwork loadNetworkWithoutRecovery() {
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.createOrLoad("", "user-00001", null);
		return egoNetwork;
	}
	
	public static void main(String[] args) {
		Utils.development = true;
		ContextualEgoNetwork egoNetwork = createNetworkWithRecovery();
		System.out.println(egoNetwork.getOrCreateContext("Test Context").getEdges().size());//number of edges
		
		Serializer.clearSerializers();//cleanup loaded serializers
		egoNetwork = loadNetworkWithoutRecovery();
		System.out.println(egoNetwork.getOrCreateContext("Test Context").getEdges().size());//doesn't load edges, because no saved called previously

		egoNetwork.addListener(new RecoveryListener());//this will also load unsaved log file
		System.out.println(egoNetwork.getOrCreateContext("Test Context").getEdges().size());//RecoveryListener rediscovers unsaved data
		
	}
}
