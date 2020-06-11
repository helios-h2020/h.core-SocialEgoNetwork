package eu.h2020.helios_social.core.contextualegonetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class supports dynamic object serialization, with the capability of reloading
 * only parts of objects and saving only particular objects.
 * <p>
 * To declare an object as dynamic so that it is referenced by others with its id after serialization
 * use the function {@link #registerId(Object)} to automatically create a UUId or a
 * {@link #registerId(Object, String)} to specify a given id.
 * <p>
 * Serialization supports non-enum primitive datatypes, lists, arrays and maps with String keys.
 * <p>
 * To declare that a class field should not be saved, put the annotation
 * <code>@Serializer.Serialization(enabled=false)</code> over it.
 *
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 */
public class Serializer {
	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Serialization {
		boolean enabled() default true;
	}

	private HashMap<Object, Boolean> enableSaving;
	private HashMap<Object, String> objectIds;
	private HashMap<String, Object> idObjects;
	private String path;
	private static HashMap<String, Serializer> serializers =
			new HashMap<String, Serializer>();

	protected static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		for (Class<?> c = type; c != null; c = c.getSuperclass())
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		return fields;
	}

	/**
	 * This function removes all serializers from memory. This effectively unloads every contextual ego network wiythout saving.
	 *
	 * @deprecated Only use for testing.
	 */
	public synchronized static void clearSerializers() {
		serializers.clear();
	}

	/**
	 * Obtains a serializer that stores objects at a specific path.
	 * Serializers are 1-1 mapped to paths.
	 *
	 * @param path The path in which to store objects
	 * @return The serializer's instance
	 */
	public synchronized static Serializer getSerializer(String path) {
		Serializer serializer = serializers.get(path);
		if (serializer == null)
			serializers.put(path, serializer = new Serializer(path));
		return serializer;
	}

	protected Serializer(String path) {
		enableSaving = new HashMap<Object, Boolean>();
		objectIds = new HashMap<Object, String>();
		idObjects = new HashMap<String, Object>();
		this.path = path;
	}

	/**
	 * @return The path the serializer is stored in
	 */
	public String getPath() {
		return path;
	}

	public synchronized String getRegisteredId(Object object) {
		if (object == null) Utils.error(new NullPointerException());
		String id = objectIds.get(object);
		if (id != null)
			return id;
		return Utils.error(object.toString() + " has no id", null);
	}

	synchronized String registerId(Object object) {
		if (object == null) Utils.error(new NullPointerException());
		String id = objectIds.get(object);
		if (id != null)
			return id;
		id = UUID.randomUUID().toString();
		while (objectIds.containsKey(id))
			id = UUID.randomUUID().toString();
		idObjects.put(id, object);
		objectIds.put(object, id);
		Utils.log("Registered for monitoring " + id + " " +
				object.getClass().getName());
		return id;
	}

	synchronized String registerId(Object object, String specificId) {
		if (object == null) Utils.error(new NullPointerException());
		if (objectIds.get(object) != null &&
				idObjects.get(specificId) != object)
			Utils.error(
					"Explicitly defined ID already in use by a differet object (" +
							specificId + ")");
		idObjects.put(specificId, object);
		objectIds.put(object, specificId);
		Utils.log("Registered for monitoring " + specificId + " " +
				object.getClass().getName());
		return specificId;
	}
	
	public synchronized void removeFromStorage(Object object) {
		try {
			Path path = Paths.get(this.path + objectIds.get(object) + ".json");
			Files.deleteIfExists(path);
		} catch (Exception e) {
			Utils.error("Failed to remove object: " + e.toString());
		}
	}

	@SuppressWarnings("unchecked")
	protected Object deserializeToNewObject(Object jsonValue, Type defaultClass,
			int levelsOfLoadingDemand, ArrayList<Object> parents)
			throws Exception {
		if (jsonValue == null)
			return null;

		if (jsonValue instanceof JSONObject &&
				((JSONObject) jsonValue).has("@value")) {
			Class<?> primitiveType = Class.forName(
					(String) ((JSONObject) jsonValue).get("@class"));
			return deserializeToNewObject(
					((JSONObject) jsonValue).get("@value"), primitiveType,
					levelsOfLoadingDemand, parents);
		}
		if (jsonValue instanceof String) {
			if (defaultClass == String.class)
				return (String) jsonValue;
			if (defaultClass == Boolean.class || defaultClass == boolean.class)
				return new Boolean(Boolean.parseBoolean((String) jsonValue));
			if (defaultClass == Long.class || defaultClass == long.class)
				return new Long(Long.parseLong((String) jsonValue));
			if (defaultClass == Integer.class || defaultClass == int.class)
				return new Integer(Integer.parseInt((String) jsonValue));
			if (defaultClass == Double.class || defaultClass == double.class)
				return new Double(Double.parseDouble((String) jsonValue));
			return Utils
					.error("Unknown primitive datatype: " + defaultClass, null);
		}
		if (jsonValue instanceof JSONObject &&
				((JSONObject) jsonValue).has("@id")
				&& idObjects
				.containsKey((String) ((JSONObject) jsonValue).get("@id"))) {
			return idObjects.get((String) ((JSONObject) jsonValue).get("@id"));
		}
		if (jsonValue instanceof JSONArray) {
			if (defaultClass instanceof Class) {
				JSONArray array = (JSONArray) jsonValue;
				Class<?> componentType =
						((Class<?>) defaultClass).getComponentType();
				Object list = Array.newInstance(componentType, array.length());
				parents.add(list);
				for (int i = 0; i < array.length(); i++)
					Array.set(list, i,
							deserializeToNewObject(array.get(i), componentType,
									levelsOfLoadingDemand, parents));
				parents.remove(list);
				return list;
			} else {
				List<?> list =
						(List<?>) ((Class<?>) ((java.lang.reflect.ParameterizedType) defaultClass)
								.getRawType()).newInstance();
				Class<?> defaultListType =
						(Class<?>) ((java.lang.reflect.ParameterizedType) defaultClass)
								.getActualTypeArguments()[0];
				JSONArray array = (JSONArray) jsonValue;
				parents.add(list);
				for (int i = 0; i < array.length(); i++)
					((List<Object>) list)
							.add(deserializeToNewObject(array.get(i),
									defaultListType, levelsOfLoadingDemand,
									parents));
				parents.remove(list);
				return list;
			}
		}
		if (jsonValue instanceof JSONObject &&
				((JSONObject) jsonValue).has("@par")) {
			//for(Object obj : parents)
			//	System.out.println(obj.getClass().toString());
			//System.out.println(parents.get(parents.size()-Integer.parseInt((String) ((JSONObject)jsonValue).get("@par"))).getClass().toString());
			return parents.get(parents.size() - Integer.parseInt(
					(String) ((JSONObject) jsonValue).get("@par")));
		}
		if (jsonValue instanceof JSONObject &&
				((JSONObject) jsonValue).has("@class")) {
			Class<?> valueType = Class.forName(
					(String) ((JSONObject) jsonValue).get("@class"));
			Constructor<?> constructor = valueType.getDeclaredConstructor();
			if (constructor == null)
				throw new RuntimeException(
						"Cannot deserialize class with no default constructor: " +
								valueType.toString());
			boolean prevConstructorAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			Object value = constructor.newInstance();
			constructor.setAccessible(prevConstructorAccessible);
			if (jsonValue instanceof JSONObject &&
					((JSONObject) jsonValue).has("@id")) {
				if (!idObjects.containsKey(value)) {
					registerId(value,
							(String) ((JSONObject) jsonValue).get("@id"));
					if (levelsOfLoadingDemand > 0)
						reload(value, levelsOfLoadingDemand - 1);
				}
			} else {
				//donnot add to parents here (this is done in the called function)
				deserializeInstantiatedObject(jsonValue, value,
						levelsOfLoadingDemand, parents);
			}
			return value;
		}
		if (!(defaultClass instanceof Class) &&
				((java.lang.reflect.ParameterizedType) defaultClass)
						.getRawType() == HashMap.class) {
			HashMap<?, ?> map =
					(HashMap<?, ?>) ((Class<?>) ((java.lang.reflect.ParameterizedType) defaultClass)
							.getRawType()).newInstance();
			Class<?> defaultMapType =
					(Class<?>) ((java.lang.reflect.ParameterizedType) defaultClass)
							.getActualTypeArguments()[1];
			parents.add(map);
			Iterator<String> keys = ((JSONObject) jsonValue).keys();
			while (keys.hasNext()) {
				String entry = keys.next();
				((HashMap<String, Object>) map).put(entry,
						deserializeToNewObject(
								((JSONObject) jsonValue).get(entry),
								defaultMapType, levelsOfLoadingDemand,
								parents));
			}
			parents.remove(map);
			return map;
		}
		//System.out.println(defaultClass.toString());
		throw new RuntimeException("Unclear deserialization " + jsonValue);
	}

	protected void deserializeInstantiatedObject(Object json, Object object,
			int levelsOfLoadingDemand, ArrayList<Object> parents) {
		if (json == null || object == null)
			return;
		if (!(json instanceof JSONObject)) {
			Utils.error("Can only deserialize a JSONObject");
			return;
		}
		JSONObject classObject = (JSONObject) json;
		for (Field field : getAllFields(object.getClass())) {
			String fieldName = field.getName();
			if (classObject.has(fieldName)) {
				boolean prevAccessible = field.isAccessible();
				field.setAccessible(true);
				parents.add(object);
				try {
					field.set(object,
							deserializeToNewObject(classObject.get(fieldName),
									field.getGenericType(),
									levelsOfLoadingDemand, parents));
				} catch (Exception e) {
					Utils.error("Deserialization error for field " +
							object.getClass().toString() + "." +
							field.getName() + " : " + e.toString());
				}
				parents.remove(parents.size() - 1);
				field.setAccessible(prevAccessible);
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected Object serialize(Object object, Class<?> wouldHaveSuggestedClass,
			boolean convertToIdIfPossible,
			HashSet<Object> objectsWithKnownClasses, ArrayList<Object> parents)
			throws Exception {
		if (object == null)
			return null;
		if (object.getClass().isPrimitive()
				|| object instanceof String
				|| object instanceof Boolean
				|| object instanceof Long
				|| object instanceof Integer
				|| object instanceof Double
				|| object instanceof Enum) {
			if (wouldHaveSuggestedClass == null ||
					!wouldHaveSuggestedClass.getSimpleName()
							.replace("int", "Integer").equalsIgnoreCase(
									object.getClass().getSimpleName())) {
				JSONObject classObject = new JSONObject();
				classObject.put("@class",
						object.getClass().getTypeName().toString());
				classObject.put("@value", object.toString());
				return classObject;
			}
			return object.toString();
		}
		if (object instanceof List) {
			parents.add(object);
			JSONArray list = new JSONArray();
			for (Object element : ((List<?>) object))
				list.put(serialize(element, null, true, objectsWithKnownClasses,
						parents));
			parents.remove(object);
			return list;
		}
		if (object.getClass().isArray()) {
			JSONArray list = new JSONArray();
			parents.add(object);
			for (int i = 0; i < Array.getLength(object); i++)
				list.put(serialize(Array.get(object, i),
						object.getClass().getComponentType(), true,
						objectsWithKnownClasses, parents));
			parents.remove(object);
			return list;
		} else if (object instanceof Map) {
			parents.add(object);
			JSONObject map = new JSONObject();
			for (String key : ((Map<String, ?>) object).keySet())
				map.put(key.toString(),
						serialize(((Map<String, ?>) object).get(key), null,
								true, objectsWithKnownClasses, parents));
			parents.remove(object);
			return map;
		}

		JSONObject classObject = new JSONObject();
		String id = objectIds.get(object);
		if (id != null) {
			classObject.put("@id", id);
			if (!objectsWithKnownClasses.contains(id)) {
				classObject.put("@class",
						object.getClass().getTypeName().toString());
				objectsWithKnownClasses.add(id);
			}
		}
		if (id == null && parents.contains(object)) {
			classObject.put("@par",
					"" + (parents.size() - parents.indexOf(object)));
		} else if (id == null || !convertToIdIfPossible) {
			parents.add(object);
			if (id == null && !objectsWithKnownClasses.contains(object))
				classObject.put("@class",
						object.getClass().getTypeName().toString());
			for (Field field : getAllFields(object.getClass())) {
				Serialization serializeable =
						field.getAnnotation(Serialization.class);
				if (serializeable != null && !serializeable.enabled())
					continue;
				boolean prevAccessible = field.isAccessible();
				field.setAccessible(true);
				classObject.put(field.getName(),
						serialize(field.get(object), field.getType(), true,
								objectsWithKnownClasses, parents));
				field.setAccessible(prevAccessible);
			}
			parents.remove(object);
		}
		return classObject;
	}

	public synchronized void saveAllRegistered() {
		for (Object object : objectIds.keySet())
			if (enableSaving.getOrDefault(object, true))
				save(object);
	}

	public synchronized boolean save(Object object) {
		if (!enableSaving.getOrDefault(object, true))
			return Utils.error("Not allowed to save: " + objectIds.get(object) +
					" " + object.getClass().getName(), false);
		try {
			long tic = System.nanoTime();
			registerId(object);
			String path = this.path + objectIds.get(object) + ".json";
			Path dirPath = Paths.get(path);
			if (dirPath.getParent() != null)
				Files.createDirectories(dirPath.getParent());
			Files.deleteIfExists(dirPath);
			Files.createFile(dirPath);
			FileOutputStream outputStream = new FileOutputStream(path);
			OutputStreamWriter outputStreamWriter =
					new OutputStreamWriter(outputStream);
			JSONObject jsonSerialized =
					(JSONObject) serialize(object, null, false,
							new HashSet<Object>(), new ArrayList<Object>());
			outputStreamWriter.write(jsonSerialized.toString());
			outputStreamWriter.close();
			Utils.log("Saved " + objectIds.get(object) + " " +
					object.getClass().getName() + " (" +
					(System.nanoTime() - tic) / 1000.0 / 1000.0 + " ms)");
			return true;
		} catch (Exception e) {
			return Utils.error("Failed to save: " + e.toString(), false);
		}
	}

	public synchronized Object deserializeFromString(String serializedObject) {
		if (serializedObject.isEmpty())
			return null;
		try {
			JSONObject jsonObject =
					new JSONObject(serializedObject);
			return deserializeToNewObject(jsonObject, null, 0,
					new ArrayList<Object>());
		} catch (Exception e) {
			Utils.error(e);
			return null;
		}
	}

	public synchronized String serializeToString(Object object) {
		try {
			if (object == null)
				return "";
			return serialize(object, null, false, new HashSet<Object>(),
					new ArrayList<Object>()).toString();
		} catch (Exception e) {
			return Utils.error(e, null);
		}
	}

	public synchronized boolean reload(Object object) {
		return reload(object, 0);
	}

	public synchronized boolean reload(Object object,
			int levelsOfLoadingDemand) {//zero levels to NOT iteratively reload
		try {
			long tic = System.nanoTime();
			BufferedReader br = new BufferedReader(
					new FileReader(path + objectIds.get(object) + ".json"));
			StringBuilder builder = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
				line = br.readLine();
			}
			String read = builder.toString();
			JSONObject jsonObject = new JSONObject(read);
			deserializeInstantiatedObject(jsonObject, object,
					levelsOfLoadingDemand, new ArrayList<Object>());
			br.close();
			Utils.log("Loaded " + objectIds.get(object) + " " +
					object.getClass().getName() + " (" +
					(System.nanoTime() - tic) / 1000.0 / 1000.0 + " ms)");
			return true;
		} catch (Exception e) {
			return Utils.error(e, false);
		}
	}

	/**
	 * Enables or disables saving for the registered object for the serializer. When enabled (default behavior)
	 * this will save the given object to a file determined by the path and registered object's serialization
	 * identifier.
	 *
	 * @param object    The given object
	 * @param allowSave Whether the serializer is allowed to save the given object
	 * @throws Exception if the given object is not registered, for example with {@link #registerId(Object)}
	 * @see #saveAllRegistered()
	 */
	public synchronized void setSavePermission(Object object, boolean allowSave) {
		if (!objectIds.containsKey(object))
			Utils.error(new IllegalArgumentException());
		else
			enableSaving.put(object, allowSave);
	}

	/**
	 * Removes an object from the serializer. This object will no longer have a unique id
	 * and hence won't be stored in its own file.
	 *
	 * @param object The object to remove
	 */
	public synchronized void unregister(Object object) {
		String id = objectIds.get(object);
		objectIds.remove(object);
		idObjects.remove(id);
		Utils.log("Unregistered " + id + " " + object.getClass().getName());
	}

	/**
	 * Empties the folder the serializer is initialized in from any previously saved data.
	 */
	public void removePreviousSaved() {
		File path = new File(this.path);
		if (path.exists())
			for (File file : path.listFiles())
				file.delete();
	}

	Object getObject(String specificId) {
		return idObjects.get(specificId);
	}
}