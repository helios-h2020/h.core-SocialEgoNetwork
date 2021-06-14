package eu.h2020.helios_social.core.contextualegonetwork.examples;

import eu.h2020.helios_social.core.contextualegonetwork.Context;
import eu.h2020.helios_social.core.contextualegonetwork.ContextualEgoNetwork;
import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;
import eu.h2020.helios_social.core.contextualegonetwork.listeners.RecoveryListener;
import eu.h2020.helios_social.core.contextualegonetwork.storage.NativeStorage;

public class ExampleRemoveContext {

	public static void main(String[] args) {
		//running this code two times should not create crashes
		Utils.development = true;
		ContextualEgoNetwork cen = ContextualEgoNetwork.createOrLoad(
				Storage.getInstance("CEN\\", NativeStorage.class), 
				"user-00001", null);
		cen.addListener(new RecoveryListener());
		Context context1 = cen.getOrCreateContext("home");
		context1.getOrAddEdge(cen.getEgo(), cen.getOrCreateNode("user-00002"));
		Context context2 = cen.getOrCreateContext("work");
		context2.getOrAddEdge(cen.getEgo(), cen.getOrCreateNode("user-00003"));
		cen.setCurrent(context2);
		cen.save();
		cen.removeContext(context2);
		Context context3 = cen.getOrCreateContext("work3");
		System.out.println(cen.getContexts().size());
	}

}
