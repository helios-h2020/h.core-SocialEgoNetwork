package contextualegonetwork;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue; 

class Serializer {
	private HashMap<Object, String> objectIds;
	private HashMap<String, Object> idObjects;
	private String path;
	private static HashMap<String, Serializer> serializers = new HashMap<String, Serializer>(); 
	
	public synchronized static Serializer getSerializer(String path) {
		Serializer serializer = serializers.get(path);
		if(serializer==null)
			serializers.put(path, serializer = new Serializer(path));
		return serializer;
	}
	
	protected Serializer(String path) {
		objectIds = new HashMap<Object, String>();
		idObjects = new HashMap<String, Object>();
		this.path = path;
	}
	
	synchronized String registerId(Object object) {
		String id = objectIds.get(object);
		if(id!=null)
			return id;
		id = UUID.randomUUID().toString();
		while(idObjects.containsKey(id))
			id = UUID.randomUUID().toString();
		idObjects.put(id, object);
		objectIds.put(object, id);
		return id;
	}

	synchronized String registerSpecialId(Object object, String specialId) {
		String id = objectIds.get(object);
		if(id!=null)
			Utils.error("Explicitly defined ID already in use");
		idObjects.put(specialId, object);
		objectIds.put(object, specialId);
		return id;
	}
	
	public synchronized void removeFromStorage(Object object) {
        try{
            Path path = Paths.get(this.path+objectIds.get(object));
            Files.deleteIfExists(path);
        }
        catch(Exception e){
        	Utils.error("Failed to remove object: "+e.toString());
        }
	}
	
	@SuppressWarnings("unchecked")
	protected void deserialize(Object json, Object object, int levelsOfLoadingDemand) {
		if(json==null || object==null)
			return;
		if(!(json instanceof JSONObject)) {
			Utils.error("Can only deserialize a JSONObject");
			return;
		}
		ArrayList<Object> pendingReload = new ArrayList<Object>();
		JSONObject classObject = (JSONObject)json;
		for(Field field : object.getClass().getDeclaredFields()) {
			String fieldName = field.getName();
			if(classObject.containsKey(fieldName)) {
				boolean prevAccessible = field.isAccessible();
				field.setAccessible(true);
				try {
					Object jsonValue = classObject.get(fieldName);
					if(jsonValue==null)
						field.set(object, null);
					else if(jsonValue instanceof String) {
						if(field.getType()==String.class)
							field.set(object, (String)jsonValue);
						else if(field.getType()==Boolean.class)
							field.set(object, (boolean)Boolean.parseBoolean((String)jsonValue));
						else if(field.getType()==Long.class)
							field.set(object, (long)Long.parseLong((String)jsonValue));
						else if(field.getType()==Integer.class)
							field.set(object, (int)Integer.parseInt((String)jsonValue));
						else if(field.getType()==Double.class)
							field.set(object, (double)Double.parseDouble((String)jsonValue));
					}
					else if(jsonValue instanceof JSONArray) {
						List<?> list = (List<?>) field.getType().newInstance();
						Class<?> defaultValueType = (Class<?>)((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
						JSONArray array = (JSONArray)jsonValue;
						for(int i=0;i<array.size();i++) {
							Class<?> valueType = defaultValueType;
							jsonValue = array.get(i);
							if(defaultValueType==String.class)
								((List<String>)list).add((String)jsonValue);
							else if(defaultValueType==Boolean.class)
								((List<Boolean>)list).add((boolean)Boolean.parseBoolean((String)jsonValue));
							else if(defaultValueType==Long.class)
								((List<Long>)list).add((long)Long.parseLong((String)jsonValue));
							else if(defaultValueType==Integer.class)
								((List<Integer>)list).add((int)Integer.parseInt((String)jsonValue));
							else if(defaultValueType==Double.class)
								((List<Double>)list).add((double)Double.parseDouble((String)jsonValue));
							else if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")
									&& idObjects.containsKey((String)((JSONObject)jsonValue).get("@id"))) {
								((List<Object>)list).add(idObjects.get((String)((JSONObject)jsonValue).get("@id")));
							}
							else {
								if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@class"))
									valueType = Class.forName((String)((JSONObject)jsonValue).get("@class"));
								Constructor<?> constructor = valueType.getDeclaredConstructor();
								boolean prevConstructorAccessible = constructor.isAccessible();
								constructor.setAccessible(true);
								Object value = constructor.newInstance();
								constructor.setAccessible(prevConstructorAccessible);
								((List<Object>)list).add(value);
								if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")) {
									if(levelsOfLoadingDemand>0 && !idObjects.containsKey(value)) {
										registerSpecialId(value, (String)((JSONObject)jsonValue).get("@id"));
										pendingReload.add(value);
									}
								}
								else
									deserialize(jsonValue, value, levelsOfLoadingDemand);
							}
						}
						field.set(object, list);
					}
					else if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")
							&& idObjects.containsKey((String)((JSONObject)jsonValue).get("@id"))) {
						field.set(object, idObjects.get((String)((JSONObject)jsonValue).get("@id")));
					}
					else {
						Class<?> valueType = field.getType();
						if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@class"))
							valueType = Class.forName((String)((JSONObject)jsonValue).get("@class"));
						Constructor<?> constructor = valueType.getDeclaredConstructor();
						boolean prevConstructorAccessible = constructor.isAccessible();
						constructor.setAccessible(true);
						Object value = constructor.newInstance();
						constructor.setAccessible(prevConstructorAccessible);
						field.set(object, value);
						if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")) {
							if(levelsOfLoadingDemand>0 && !idObjects.containsKey(value)) {
								registerSpecialId(value, (String)((JSONObject)jsonValue).get("@id"));
								pendingReload.add(value);
							}
						}
						else
							deserialize(jsonValue, value, levelsOfLoadingDemand);
					}
				}
				catch(Exception e) {
					Utils.error("Deserialization error: "+e.toString());
				}
				field.setAccessible(prevAccessible);
			}
		}
		for(Object objectToReload : pendingReload) 
			reload(objectToReload, levelsOfLoadingDemand-1);
	}
	
	@SuppressWarnings("unchecked")
	protected Object serialize(Object object, boolean convertToIdIfPossible) throws Exception {
		if(object==null)
			return null;
		if(object.getClass().isPrimitive()
				|| object instanceof String
				|| object instanceof Boolean
				|| object instanceof Long
				|| object instanceof Integer
				|| object instanceof Double
				|| object instanceof Enum)
			return object.toString();
		if(object instanceof List) {
			JSONArray list = new JSONArray();
			for(Object element : ((List<?>)object))
				list.add(serialize(element, true));
			return list;
		}
		else if(object instanceof Map) {
			JSONObject map = new JSONObject();
			for(Object key : ((Map<?,?>)object).keySet()) 
				map.put(key.toString(), serialize(((Map<?,?>)object).get(key), true));
			return map;
		}
		
        JSONObject classObject = new JSONObject(); 
        String id = objectIds.get(object);
        if(id!=null) {
        	classObject.put("@id", id);
        	classObject.put("@class", object.getClass().getTypeName().toString());
        }
        if(id==null || !convertToIdIfPossible){
        	if(id==null)
        		classObject.put("@class", object.getClass().getTypeName().toString());
    		for(Field field : object.getClass().getDeclaredFields()) {
    			boolean prevAccessible = field.isAccessible();
    			field.setAccessible(true);
    			//System.out.println(field.getName());
				classObject.put(field.getName(), serialize(field.get(object), true));
    			field.setAccessible(prevAccessible);
    		}
        }
		return classObject;
	}
	
	public synchronized void saveAllRegistered() {
		for(Object object : objectIds.keySet())
			save(object);
	}
	
	public synchronized boolean save(Object object) {
		try {
	        registerId(object);
	    	String path = this.path+objectIds.get(object);
	        Path dirPath = Paths.get(path);
	        if(dirPath.getParent()!=null)
        		Files.createDirectories(dirPath.getParent());
            Files.deleteIfExists(dirPath);
            Files.createFile(dirPath);
			FileOutputStream outputStream = new FileOutputStream(path);
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
	        outputStreamWriter.write(serialize(object, false).toString());
	        outputStreamWriter.close();
	        return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return Utils.error("Failed to save: "+e.toString(), false);
		}
	}
	
	public synchronized boolean reload(Object object, int levelsOfLoadingDemand) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path+objectIds.get(object)));
		    StringBuilder builder = new StringBuilder();
		    String line = br.readLine();
		    while (line != null) {
		    	builder.append(line);
		    	builder.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String read = builder.toString();
		    JSONObject jsonObject = (JSONObject) JSONValue.parse(read);
		    deserialize(jsonObject, object, levelsOfLoadingDemand);
		    br.close();
		    return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return Utils.error("Failed to load: "+e.toString(), false);
		}
	}
	

	public synchronized void unregister(Object object) {
		String id = objectIds.get(object);
		objectIds.remove(object);
		idObjects.remove(id);
	}
}
