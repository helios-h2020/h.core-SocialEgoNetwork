import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Node;
import contextualegonetwork.contextData.DefaultContextData;
import contextualegonetwork.contextData.Location;
import contextualegonetwork.nodeData.PersonData;

public class ExampleSave {

	public static void main(String[] args) {
		//Use this example to demonstrate a simple management use case.
		Node user1 = new Node("user-00001", "user1", new PersonData("RandomName1", "", "RandomSurname1", "1/1/11"));
		Node user2 = new Node("user-00002", "user2", new PersonData("RandomName2", "", "RandomSurname2", "1/1/11"));
		Node user3 = new Node("user-00003", "user3", new PersonData("RandomName3", "", "RandomSurname3", "1/1/11"));
		Node user4 = new Node("user-00004", "user4", new PersonData("RandomName4", "", "RandomSurname4", "1/1/11"));
		ContextualEgoNetwork egoNetwork = new ContextualEgoNetwork(user1);
		egoNetwork.createContext(new DefaultContextData("ContextName1", new Location(0,0)));
		egoNetwork.getContexts().get(0).addNode(user2);
		egoNetwork.getContexts().get(0).addNode(user4);
		egoNetwork.getContexts().get(0).addEdge(user1, user2);
		egoNetwork.getContexts().get(0).addEdge(user1, user4);
		
		egoNetwork.createContext(new DefaultContextData("ContextName2", new Location(1,1)));
		egoNetwork.getContexts().get(1).addNode(user3);
		egoNetwork.getContexts().get(1).addNode(user4);
		egoNetwork.getContexts().get(1).addEdge(user1, user3);
		egoNetwork.getContexts().get(1).addEdge(user1, user4);
		egoNetwork.getContexts().get(1).cleanup();//remove context from memory
		egoNetwork.getContexts().get(1).addNode(user2);//this will reload the context
		egoNetwork.getContexts().get(1).addEdge(user1, user2);
		// Some larger-scale testing
		/*for(int i=5;i<1000;i++) {
			Node user = new Node("user-"+i, "user"+i, new PersonData("RandomName", "", "RandomSurname", "1/1/11"));
			egoNetwork.getContexts().get(1).addNode(user);
			egoNetwork.getContexts().get(1).addEdge(user1, user);
		}*/
		egoNetwork.save();
	}
}
