import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.contextData.DefaultContextData;

public class ExampleLoad {

	public static void main(String[] args) {
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.create("user-00001/");
		System.out.println(egoNetwork.testattr);
		System.out.println(egoNetwork.getEgo().getAlias());
		System.out.println(((DefaultContextData)egoNetwork.getContexts().get(0).getData()).getName());
	}
}
