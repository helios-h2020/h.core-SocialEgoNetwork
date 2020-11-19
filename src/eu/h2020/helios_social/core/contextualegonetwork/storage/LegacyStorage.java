package eu.h2020.helios_social.core.contextualegonetwork.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This is a {@link Storage} implementation that uses native java operations
 * to save and load files.
 * To ensure compatibility with Android versions 26 or earlier,
 * it uses java.io instead of java.nio for file system operations.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class LegacyStorage extends Storage {
	private String path;
	public LegacyStorage(String path) {
		if(path==null || path.isEmpty())
			Utils.error("For safety reasons, cannot access a null or empty storage location");
    	if(!path.isEmpty() && !path.endsWith(File.separator))
    		Utils.error("LegacyStorage path should end with a '"+File.separator+"' character");
		this.path = path;
	}
	@Override
	public String getSerializedFilePath(String fileName) {
		return this.path + fileName;
	}
	@Override
	public void saveToFile(String fileName, String contents) throws Exception {
		String path = getSerializedFilePath(fileName);
		getSerializedFilePath(fileName);
		File dirPath = new File(path);
		if (dirPath.getParent() != null)
			dirPath.getParentFile().mkdirs();
		dirPath.delete();
		dirPath.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(dirPath);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
		outputStreamWriter.write(contents);
		outputStreamWriter.close();
		outputStream.close();
	}
	@Override
	public String loadFromFile(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(getSerializedFilePath(fileName)));
		StringBuilder builder = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			builder.append(line);
			builder.append(System.lineSeparator());
			line = br.readLine();
		}
		String read = builder.toString();
		br.close();
		return read;
	}
	@Override
	public boolean fileExists(String fileName) {
		return (new File(getSerializedFilePath(fileName))).exists();
	}
	@Override
	public void deleteFile(String fileName) throws Exception {
		(new File(getSerializedFilePath(fileName))).delete();
	}
	@Override
	public void deleteAll() throws Exception {
		File path = new File(this.path);
		if (path.exists())
			for (File file : path.listFiles())
				file.delete();
	}
}
