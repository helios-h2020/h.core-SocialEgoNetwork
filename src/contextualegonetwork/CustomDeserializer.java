/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextualegonetwork;

/**
 * This abstract class is meant to be extended with a custom, programmer-defined
 * serializer class that best adapts to the needs of the program that exploits
 * the HELIOS library.
 */
public abstract class CustomDeserializer<T> {
    /**
     * Deserializes an object (in JSON format) in an object of type T
     * @param jsonString The json string to be deserialized
     * @return The object of type T obtained by the deserialization process
     */
    public abstract T fromJson(String jsonString);
    
    /**
     * @param obj The object that has to be serialized
     * @return A string, that is the JSON serialization of an object of type T
     */
    public abstract String toJson(T obj);
}
