package eu.h2020.helios_social.core.contextualegonetwork.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import eu.h2020.helios_social.core.contextualegonetwork.Storage;
import eu.h2020.helios_social.core.contextualegonetwork.Utils;

/**
 * This is a {@link Storage} implementation that uses native java operations
 * to save and load files.
 * 
 * It is equivalent to {@link LegacyStorage} with the difference
 * that NativeStorage uses more recent java interfaces.
 * This makes it safer and future-compatible, but lacks compatibility with 
 * earlier versions of Android that lack java.nio support.
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class NativeStorage extends Storage {
	private String path;
	public NativeStorage(String path) {
		if(path==null || path.isEmpty())
			Utils.error("For safety reasons, cannot access a null or empty storage location");
    	if(!path.isEmpty() && !path.endsWith(File.separator))
    		Utils.error("NativeStorage path should end with a '"+File.separator+"' character");
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
		Path dirPath = Paths.get(path);
		if (dirPath.getParent() != null)
			Files.createDirectories(dirPath.getParent());
		Files.deleteIfExists(dirPath);
		Files.createFile(dirPath);
		FileOutputStream outputStream = new FileOutputStream(path);
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
		return Files.exists(Paths.get(getSerializedFilePath(fileName)));
	}
	@Override
	public void deleteFile(String fileName) throws Exception {
		Path path = Paths.get(getSerializedFilePath(fileName));
		Files.deleteIfExists(path);
	}
	@Override
	public void deleteAll() throws Exception {
		File path = new File(this.path);
		if (path.exists())
			for (File file : path.listFiles())
				file.delete();
	}
}
