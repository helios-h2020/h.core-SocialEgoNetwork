package eu.h2020.helios_social.core.contextualegonetwork.examples;

import eu.h2020.helios_social.core.contextualegonetwork.Serializer;
import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.storage.NativeStorage;

public class ExampleProfile {

	public static void main(String[] args) {
		// E.g. first set some user data values   (HeliosUserData   of Profile module)

        // Load username from profile data
        Serializer serializer = Serializer.getInstance(Storage.getInstance("CEN/user_id", NativeStorage.class));
        
		HeliosUserData.getInstance().setValue("worklat", "60.012");
		serializer.save("HeliosUserData", HeliosUserData.getInstance());
		HeliosUserData.getInstance().setValue("worklat", "0");
		serializer.reload("HeliosUserData", HeliosUserData.getInstance());
        System.out.println(HeliosUserData.getInstance().getValue("worklat")); // will print 60.012
	}

}
