package eu.h2020.helios_social.core.contextualegonetwork;

import java.io.File;
import java.util.HashMap;

/**
 * This class abstracts file system operations that can be used by the {@link Serializer}.
 * To extend this class it is imperative to create a public constructor, but instantiation
 * should be preferred through the static {@link #getInstance(String, Class)} method of this
 * base class.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public abstract class Storage {
	private static HashMap<String, Storage> storagePaths = new HashMap<String, Storage>();
	public static Storage getInstance(String path, Class<? extends Storage> storageClass) {
		if(!path.isEmpty() && !path.endsWith(File.separator) && !path.endsWith("\\") && !path.endsWith("/"))
			path += File.separator;
		Storage existingStorage = storagePaths.get(path);
		if(existingStorage!=null) {
			if(existingStorage.getClass() != storageClass)
				return Utils.error("A different storage instance is using the given location", existingStorage);
			return existingStorage;
		}
		for(String existingPath : storagePaths.keySet()) 
			if(existingPath.startsWith(path) || path.startsWith(existingPath))
				return Utils.error("A different storage instance is indirectly using the given location", null);
		try {
			storagePaths.put(path, existingStorage = storageClass.getConstructor(String.class).newInstance(path));
		}
		catch (Exception e) {
			return Utils.error(e.toString(), null);
		}
		return existingStorage;
	}
	public abstract void saveToFile(String fileName, String contents) throws Exception;
	public abstract String loadFromFile(String fileName) throws Exception;
	public abstract void deleteFile(String fileName) throws Exception;
	public abstract void deleteAll() throws Exception;
	public abstract boolean fileExists(String fileName);
	public abstract String getSerializedFilePath(String fileName);
}
