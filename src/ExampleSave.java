import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Node;
import contextualegonetwork.dataExamples.DefaultContextData;
import contextualegonetwork.dataExamples.Location;
import contextualegonetwork.dataExamples.PersonData;

public class ExampleSave {

	public static void main(String[] args) {
		//Use this example to demonstrate a simple management use case.
		Node user1 = new Node("user-00001", new PersonData("RandomName1", "", "RandomSurname1", "1/1/00"));
		Node user2 = new Node("user-00002", null);//can set any object as node data 
		Node user3 = new Node("user-00003", new PersonData("RandomName3", "", "RandomSurname3", "3/3/00"));
		//all object data are recursively serialized, so pass an id if you don't want to serialize them
		Node user4 = new Node("user-00004", "RandomName4");
		
		ContextualEgoNetwork egoNetwork = new ContextualEgoNetwork(user1);
		//example context management
		Context context1 = egoNetwork.createContext(new DefaultContextData("ContextName1", new Location(1,1)));
		context1.addNode(user2);
		context1.addNode(user4);
		context1.getOrAddEdge(user1, user2);//creates the edge if it doesn't exist
		context1.getOrAddEdge(user1, user4);
		context1.getOrAddEdge(user2, user4).addDetectedInteraction("HANDSHAKE");
		
		// the second way to create context
		Context context2 = egoNetwork.getOrCreateContext("ContextName2");
		context2.addNode(user3);
		context2.addNode(user4);
		context2.addEdge(user1, user3);
		context2.addEdge(user1, user4);
		context2.cleanup();//remove context contents from memory
		
		egoNetwork.getContexts().get(1).addNode(user2);//this will reload the context in memory
		egoNetwork.getContexts().get(1).addEdge(user1, user2);
		
		egoNetwork.save(); //saves the ego network (also saves the contexts and nodes)
	}
}
