import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.contextData.DefaultContextData;
import contextualegonetwork.contextData.Location;
import contextualegonetwork.nodes.Person;

public class Excample {

	public static void main(String[] args) {
		//Use this example to demonstrate a simple management use-case.
		ContextualEgoNetwork egoNetwork = new ContextualEgoNetwork(new Person("user", "user#00001", "RandomName1", "", "RandomSurname1", 0));
		egoNetwork.createContext(new DefaultContextData("ContextName1", new Location(0,0)));
	}

}
