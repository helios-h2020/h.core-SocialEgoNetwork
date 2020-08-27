package eu.h2020.helios_social.core.contextualegonetwork.examples;
import java.util.ArrayList;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Node;
public class ExampleSave {
	public static class ExampleData {
		public int testInt = 63;
		public float testFLoat = 61.f;
		public ArrayList<ArrayList<Float>> lists = new ArrayList<ArrayList<Float>>();;
		public ExampleData() {
		}
		public void init() {
			lists.add(new ArrayList<Float>());
			lists.get(0).add(1023.f);
			lists.get(0).add(1024.f);
		}
	}
	
	public static void main(String[] args) {
		//Use this example to demonstrate a simple management use case.
		//Utils.development = false;
		
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.createOrLoad("", "user-00001", new PersonData("RandomName1", "", "RandomSurname1", "1/1/00"));
		
		Node user1 = egoNetwork.getEgo();
		user1.getOrCreateInstance(ExampleData.class);
		System.out.println(user1.getOrCreateInstance(ExampleData.class).lists.get(0).size());
		user1.getOrCreateInstance(ExampleData.class).init();
		Node user2 = egoNetwork.getOrCreateNode("user-00002", null);//can set any object as node data 
		Node user3 = egoNetwork.getOrCreateNode("user-00003", new PersonData("RandomName3", "", "RandomSurname3", "3/3/00"));
		//all object data are recursively serialized, so pass an id if you don't want to serialize them
		Node user4 = egoNetwork.getOrCreateNode("user-00004", "RandomName4 (just a string)");
		
		
		//example context management
		Context context1 = egoNetwork.getOrCreateContext(new DefaultContextData("ContextName1", new DefaultContextData.Location(1,1)));
		context1.addNode(user2);
		context1.addNode(user4);
		context1.getOrAddEdge(user1, user2);//creates the edge if it doesn't exist
		context1.getOrAddEdge(user1, user4);
		for(int i=0;i<100;i++)
			context1.getOrAddEdge(user2, user4).addDetectedInteraction("HANDSHAKE");
		
		// the second way to create context
		Context context2 = egoNetwork.getOrCreateContext("ContextName2");
		System.out.println(context2.getNodes().size());
		context2.addNode(user3);
		context2.addNode(user4);
		context2.addEdge(user1, user3);
		context2.addEdge(user1, user4);
		//context2.cleanup();//remove context contents from memory
		
		egoNetwork.getContexts().get(1).addNode(user2);//this will reload the context in memory
		egoNetwork.getContexts().get(1).addEdge(user1, user2);
		
		egoNetwork.save(); //saves the ego network (also saves the contexts and nodes)
	}
}
