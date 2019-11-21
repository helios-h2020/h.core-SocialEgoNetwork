import contextualegonetwork.Context;
import contextualegonetwork.ContextualEgoNetwork;
import contextualegonetwork.Edge;
import contextualegonetwork.Utils;
import contextualegonetwork.contextData.DefaultContextData;

public class ExampleLoad {

	public static void main(String[] args) {
		Utils.development = true;//enable logging and make exceptions critically stop execution flow
		
		ContextualEgoNetwork egoNetwork = ContextualEgoNetwork.load("user-00001");
		System.out.println(egoNetwork.testattr);
		System.out.println(egoNetwork.getEgo().getAlias());
		//assert that deserialization finds contexts (these aren't loaded yet)
		System.out.println(egoNetwork.getContexts());
		//assert that deserialization identifies the same data objects
		System.out.println(egoNetwork+" == "+egoNetwork.getContexts().get(0).getContextualEgoNetwork());
		//assert that deserialization works for nested data types
		System.out.println(((DefaultContextData)egoNetwork.getContexts().get(0).getData()).getName());
		//assert that deserialization works for arrays (it would throw a nullptr exception if it didn't work)
		System.out.println(egoNetwork.getContexts().get(1).getTimeCounter()[6][23]);
		
		//print the edges of all contexts
		for(Context context : egoNetwork.getContexts()) {
			context.cleanup();
			System.out.println("Context: "+((DefaultContextData)context.getData()).getName());
			for(Edge edge : context.getEdges()) {
				System.out.println(edge.getSrc().getAlias()+" -> "+edge.getDst().getAlias());
			}
			System.out.println();
		}
	}
}
