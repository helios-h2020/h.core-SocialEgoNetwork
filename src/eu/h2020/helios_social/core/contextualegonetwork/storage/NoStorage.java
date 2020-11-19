package eu.h2020.helios_social.core.contextualegonetwork.storage;

import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This is a {@link Storage} implementation that does not perform
 * any actual storage operations. It can be used to facilitate
 * large-scale experiments (e.g. simulations) in dependency 
 * modules without evoking disk operations.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class NoStorage extends Storage {
	
	public NoStorage(String path) {
		if(!path.equals("NOFILESYSTEM\\"))
			Utils.error("NoStorage path should be NOFILESYSTEM\\ . This error is created to ensure that a NoStorage instance is not inadvertly created when data need to be actually saved");
	}
	
	@Override
	public void saveToFile(String fileName, String contents) throws Exception {
	}

	@Override
	public String loadFromFile(String fileName) throws Exception {
		Utils.error("Can not load from file with class NoStorage");
		return null;
	}

	@Override
	public void deleteFile(String fileName) throws Exception {
	}

	@Override
	public void deleteAll() throws Exception {
	}

	@Override
	public boolean fileExists(String fileName) {
		return false;
	}

	@Override
	public String getSerializedFilePath(String fileName) {
		Utils.error("Can not obtain a file path for class NoStorage");
		return null;
	}

}
