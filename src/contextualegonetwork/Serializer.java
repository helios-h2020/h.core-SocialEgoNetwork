package contextualegonetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue; 

/**
 * This class supports dynamic object serialization, with the capability of reloading
 * only parts of objects and saving only particular objects.
 * 
 * To declare an object as a dynamic object that is referenced in other serializations using only its id
 * use the function {@link #registerId(Object)} to automatically create a UUId or a 
 * {@link #registerSpecialId(Object, String)} to specify a given id.
 * 
 * Serialization supports non-enum primitive datatypes, lists, arrays and maps with String keys.
 * 
 * @author Emmanouil Krasanakis (maniospas@hotmail.com)
 */
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
	protected Object deserializeToNewObject(Object jsonValue, Type defaultClass, int levelsOfLoadingDemand) throws Exception {
		if(jsonValue==null)
			return null;
		if(jsonValue instanceof String) {
			if(defaultClass==String.class)
				return (String)jsonValue;
			if(defaultClass==Boolean.class || defaultClass==boolean.class)
				return (boolean)Boolean.parseBoolean((String)jsonValue);
			if(defaultClass==Long.class || defaultClass==long.class)
				return (long)Long.parseLong((String)jsonValue);
			if(defaultClass==Integer.class || defaultClass==int.class)
				return (int)Integer.parseInt((String)jsonValue);
			if(defaultClass==Double.class || defaultClass==double.class)
				return (double)Double.parseDouble((String)jsonValue);
			return Utils.error("Unknown primitive datatype: "+defaultClass.toString(), null);
		}
		if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")
				&& idObjects.containsKey((String)((JSONObject)jsonValue).get("@id"))) {
			return idObjects.get((String)((JSONObject)jsonValue).get("@id"));
		}
		if(jsonValue instanceof JSONArray) {
			if(defaultClass instanceof Class) {
				JSONArray array = (JSONArray)jsonValue;
				Class<?> componentType = ((Class<?>) defaultClass).getComponentType();
				Object list = Array.newInstance(componentType, array.size());
				for(int i=0;i<array.size();i++) 
					Array.set(list, i, deserializeToNewObject(array.get(i), componentType, levelsOfLoadingDemand));
				return list;
			}
			else {
				List<?> list = (List<?>) ((Class<?>)((java.lang.reflect.ParameterizedType) defaultClass).getRawType()).newInstance();
				Class<?> defaultListType = (Class<?>)((java.lang.reflect.ParameterizedType) defaultClass).getActualTypeArguments()[0];
				JSONArray array = (JSONArray)jsonValue;
				for(int i=0;i<array.size();i++) 
					((List<Object>)list).add(deserializeToNewObject(array.get(i), defaultListType, levelsOfLoadingDemand));
				return list;
			}
		}
		if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@class")) {
			Class<?> valueType = Class.forName((String)((JSONObject)jsonValue).get("@class"));
			Constructor<?> constructor = valueType.getDeclaredConstructor();
			boolean prevConstructorAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			Object value = constructor.newInstance();
			constructor.setAccessible(prevConstructorAccessible);
			if(jsonValue instanceof JSONObject && ((JSONObject)jsonValue).containsKey("@id")) {
				if(!idObjects.containsKey(value)) {
					registerSpecialId(value, (String)((JSONObject)jsonValue).get("@id"));
					if(levelsOfLoadingDemand>0)
						reload(value, levelsOfLoadingDemand-1);
				}
			}
			else 
				deserializeInstantiatedObject(jsonValue, value, levelsOfLoadingDemand);
			return value;
		}
		
		throw new RuntimeException("Unclear deserialization ");
	}
	
	protected void deserializeInstantiatedObject(Object json, Object object, int levelsOfLoadingDemand) {
		if(json==null || object==null)
			return;
		if(!(json instanceof JSONObject)) {
			Utils.error("Can only deserialize a JSONObject");
			return;
		}
		JSONObject classObject = (JSONObject)json;
		for(Field field : object.getClass().getDeclaredFields()) {
			String fieldName = field.getName();
			if(classObject.containsKey(fieldName)) {
				boolean prevAccessible = field.isAccessible();
				field.setAccessible(true);
				try {
					field.set(object, deserializeToNewObject(classObject.get(fieldName), field.getGenericType(), levelsOfLoadingDemand));
				}
				catch(Exception e) {
					Utils.error("Deserialization error for field "+object.getClass().toString()+"."+field.getName()+" : "+e.toString());
				}
				field.setAccessible(prevAccessible);
			}
		}
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
		if(object.getClass().isArray()) {
			JSONArray list = new JSONArray();
			for(int i=0;i<Array.getLength(object);i++)
				list.add(serialize(Array.get(object, i), true));
			return list;
		}
		else if(object instanceof Map) {
			JSONObject map = new JSONObject();
			for(String key : ((Map<String,?>)object).keySet()) 
				map.put(key.toString(), serialize(((Map<String,?>)object).get(key), true));
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
			return Utils.error("Failed to save: "+e.toString(), false);
		}
	}
	
	public synchronized boolean reload(Object object) {
		return reload(object, 0);
	}
	
	public synchronized boolean reload(Object object, int levelsOfLoadingDemand) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path+objectIds.get(object)));
			Utils.log("Loaded "+objectIds.get(object)+" "+object.getClass().getName());
		    StringBuilder builder = new StringBuilder();
		    String line = br.readLine();
		    while (line != null) {
		    	builder.append(line);
		    	builder.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String read = builder.toString();
		    JSONObject jsonObject = (JSONObject) JSONValue.parse(read);
		    deserializeInstantiatedObject(jsonObject, object, levelsOfLoadingDemand);
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

	public void removePreviousSaved() {
		File path = new File(this.path);
		for(File file : path.listFiles()) 
			file.delete();
	}
}
