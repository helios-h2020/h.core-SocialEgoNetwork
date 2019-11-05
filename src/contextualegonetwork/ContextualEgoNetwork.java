package contextualegonetwork;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiFunction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * This class implements a Contextual Ego Network, that is the conceptual model of our Heterogeneous Social Graph.
 * It contains information about the various contexts, i.e. the different layers of the multi-layer network, and about the
 * nodes (users) that belong to the contexts. Moreover, it divides the contexts into a current one, some active ones (that are kept
 * in memory) and some inactive ones (that are serialized in dedicated files).
 */
public class ContextualEgoNetwork {
    /**
     * Path of the directory where contexts are serialized
     */
    private final Path DATA_PATH = Paths.get("ContextData");
    /**
     * Path of the directory where contexts are serialized for analysis
     */
    private final Path ANALYTICS_DATA_PATH = Paths.get("AnalyticsContextData");
    /**
     * Identifier (username) of the ego
     */
    private final String egoName;
    /**
     * Ego node
     */
    private final Node egoNode;
    /**
     * List of layers of the contextual ego network
     */
    private ArrayList<Layer> layers;
    /**
     * This hash map keeps a list of the contexts to which each node belongs.
     * the keys are the username of the nodes, while the values are ArrayLists of context identifiers.
     */
    private HashMap<String, ArrayList<String>> nodes;
    /**
     * This hash map keeps a list of the nodes that belong to each context.
     * the keys are the context identifiers, while the values are ArrayLists of node usernames
     */
    private HashMap<String, ArrayList<String>> contexts;
    /**
     * Active contexts (deserialized)
     */
    private HashMap<String, Context> activeContexts;
    /**
     * Used for serialization and deserialization of contexts
     */
    private ObjectMapper mapper;
    /**
     * Latest context that has been used (in order not to re-compute the hash value)
     */
    private Context currentContext = null;
    /**
     * For personalized serialization and deserialization
     */
    private HashMap<String, CustomDeserializer> deserializers;

    /**
     * Contexts are stored in ./ContextData directory.
     * Instances gets own contexts from that directory, but remember that egoname in these contexts needs to be equal to egoName param.(data persistence)
     * Snapshots of contexts for data analysis are stored in ./AnalyticsContextData.
     * If the two directories already existed, the application tries to retrieve pre-existing serialized contexts by deserialization.
     * @param egoName Name of ego for all contexts
     * @param nodeId The identifier of the ego
     * @throws NullPointerException if egoName or nodeId are null
     * @throws IllegalArgumentException if contextName is an empty string
     */
    public ContextualEgoNetwork(String egoName, Long nodeId) {

        if(egoName == null) throw new NullPointerException();
        if(egoName.equals("")) throw new IllegalArgumentException("Context name cannot be empty");
        this.egoNode = new Node(egoName, nodeId);
        this.egoName = egoName;
        this.nodes = new HashMap<>();
        this.contexts = new HashMap<>();
        this.activeContexts = new HashMap<>();
        this.deserializers= new HashMap<>();
        this.layers = new ArrayList<>();

        //Initialization of the object needed for serialization
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        //Creation of the directories where contexts will be serialized (if they don't exist already)
        try {
        if (!Files.exists(ANALYTICS_DATA_PATH)){
            Files.createDirectory(ANALYTICS_DATA_PATH);
        }

        if (!Files.exists(DATA_PATH))
        {
            Files.createDirectory(DATA_PATH);
        }
        //If they exist, context may exist for this CEN. They are deserialized
        else
        {
            File dir = new File(DATA_PATH.toString());
            String[] files = dir.list();
            for (String file : files) {
                Context context;
                try {
                    context = mapper.readValue(new File(DATA_PATH.toString() + "/" + file), Context.class);
                }catch (IOException ex) {
                    System.out.println("Error with loading the context"+file+ex.toString());
                    continue;
                }
                String[] nodeNames= context.getNodeNames();
                ArrayList<String> list = new ArrayList<>(Arrays.asList(nodeNames));
                contexts.put(context.getName(), list); //I add a context
                for (String nodeName : nodeNames) //I add the nodes
                {
                    ArrayList<String> cont = nodes.get(nodeName);
                    if (cont!=null) {
                        cont.add(context.getName());
                    } else {
                        cont = new ArrayList<>();
                        cont.add(context.getName());
                        nodes.put(nodeName, cont);
                    }
                }
            }
        }
        }
        catch(IOException ex){throw new RuntimeException("Error while creating directories",ex);}
    }

    /**
     * Creates a new context and adds it to the active contexts. It also creates the correspondent layer.
     * Successive calls with the same ContextName, will replace the previous context value if the context has not been saved at least once.
     * @param contextName Identifier of the context
     * @param f The translation function of the context
     * @return The new context just created in the CEN
     * @throws NullPointerException If contextName is null
     * @throws IllegalArgumentException If the context already exists (is already serialized)
     */
    public Context newContext(String contextName, BiFunction f)
    {
        if(contextName == null) throw new NullPointerException();
        Path filePath = Paths.get(DATA_PATH.toString(), contextName+".json");
        
        if(Files.exists(filePath)) throw new IllegalArgumentException("The context already exists");
        
        if(contexts.putIfAbsent(contextName, new ArrayList<>())!= null)
            contexts.replace(contextName, new ArrayList<>());
        
        Context context = new Context(contextName, egoNode);
        context.saveTimeOfLoad();
        
        activeContexts.put(contextName, context);
        newLayer(context, f);
        return context;
    }

    /**
     * Creates a new layer in the CEN. This method is protected, and it is invoked by the CEN object whenever a context is created.
     * @param c The context encapsulated in the layer
     * @param f The function to be associated to the layer
     * @throws NullPointerException If c is null
     */
    private void newLayer(Context c, BiFunction f) {
        if(c == null) throw new NullPointerException();
        layers.add(new Layer(c,f));
    }

    /**
     * Deletes a layer identified by its encapsulated context name. Subsequently, the context is also deleted (if it hasn't been already deleted).
     * @param contextName The name of the context whose layer has to be deleted.
     * @return True if the elimination has been successful, false otherwise (if the context doesn't exist)
     * @throws NullPointerException if contextName is null
     */
    public boolean deleteLayer(String contextName) {
        Layer l = getLayer(contextName);
        if(getLayer(contextName) != null) {
            layers.remove(layers.indexOf(getLayer(contextName)));
            deleteContext(contextName);
            return true;
        }
        else return false;
    }

    /**
     * Gets a layer by the name of the context encapsulated by it
     * @param contextName The name of the context
     * @return The layer that encapsulates the context identified by contextName, null if it doesn't exist
     * @throws NullPointerException if contextName is null
     */
    public Layer getLayer(String contextName) {
        if(contextName == null) throw new NullPointerException();
        for(Layer l: layers) {
            if(l.getContext().getName().equals(contextName)) return l;
        }
        return null;
    }

    /**
     * Gets a shallow copy of the list of layers of the contextual ego network
     * @return An arrayList containing the layers
     */
    public ArrayList<Layer> getLayers() {
        return (ArrayList<Layer>) layers.clone();
    }

    /**
     * Adds the context to the active contexts set (deserializes it).
     * @param contextName Identifier of the context
     * @return True if the context is added to the active contexts, false if it doesn't exists
     * @throws NullPointerException if contextName is null
     */
    public boolean loadContext(String contextName) {
        if(contextName == null) throw new NullPointerException();
        // check if it's already loaded
        Context tempContext = activeContexts.get(contextName);
        if (tempContext != null)
            return true;

        // check if the context exists
        if (contexts.get(contextName) == null)
            return false;

        Context context;

        //deserialization
        try {
        context = mapper.readValue(new File(DATA_PATH.toString() + "/" + contextName+".json"), Context.class);
        }catch(IOException ex){throw new RuntimeException(ex);}
       
        activeContexts.put(contextName, context);
        context.saveTimeOfLoad();
        return true;
    }

    
    /**
     * Saves the context and removes it from active contexts.
     * NB: context needs to be Active
     * @param contextName Identifier of the context
     * @throws NullPointerException If contextName is null
     * @throws ContextException If contextName isn't an identifier of a valid context, or is an identifier of a non active context
     */
    public void saveContext(String contextName) 
    {
        if(contextName == null) throw new NullPointerException();
        if(contexts.get(contextName)==null)
            throw new ContextException(contextName+" does not exist");
        
        Context context = activeContexts.remove(contextName);
        if(context!=null)
        {
            context.saveTimeOfUnload();
            if(currentContext!=null){
            if(currentContext.getName().equals(contextName))
                currentContext = null; 
            }
            try{
            Path path = Paths.get(DATA_PATH.toString(), contextName+".json");
            Files.deleteIfExists(path);
            Files.createFile(path);
            mapper.writeValue(new File(path.toString()), context);
            }catch(IOException ex){throw new RuntimeException(ex);}
            return;
        }
        throw new ContextException(contextName+" isn't active");
            
    }
    
    /**
     * Removes the context from active contexts. NB: changes will not be saved!
     * @param contextName The name of the context to be set to inactive
     * @throws NullPointerException If contextName is null
     * @throws ContextException If contextName isn't an identifier of a valid context, or is an identifier of a non active context
     */
    public void setInactive(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        if(contexts.get(contextName)==null)
            throw new ContextException(contextName+" does not exist");
        
        Path path = Paths.get(DATA_PATH.toString(), contextName+".json");
        if(!Files.exists(path)){
            deleteContext(contextName);
            return;
        }    
        
        Context context = activeContexts.remove(contextName);
        if(context!=null)
        {
            context.saveTimeOfUnload();
            if(currentContext!=null){
            if(currentContext.getName().equals(contextName))
                currentContext = null; 
            }
            return;
        }
        throw new ContextException(contextName+" isn't active");
    }
    
    /**
     * Saves the context current state without removing it from active contexts.
     * NB: context needs to be Active
     * @param contextName Identifier of the context
     * @throws NullPointerException If contextName is null
     * @throws ContextException If contextName isn't an identifier of a valid context, or is an identifier of a non active context
     */
    public void saveStateOfContext(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        if(contexts.get(contextName)==null)
            throw new ContextException(contextName+" doesn't exist");
        
        Context context = activeContexts.get(contextName);
        if(context!=null)
        { 
            try{
            Path path = Paths.get(DATA_PATH.toString(), contextName+".json");
            Files.deleteIfExists(path);
            Files.createFile(path);
            mapper.writeValue(new File(path.toString()), context);
            }catch(IOException ex){throw new RuntimeException(ex);}
            return;
        }
        throw new ContextException(contextName+" isn't active");
            
    }

    /**
     * Saves all active contexts in a .json file in ANALYTIC_CONTEXT_DATA directory.
     */
    public void saveContextsForAnalysis()   
    {
            long miliSec = System.currentTimeMillis();
            Path path = Paths.get(ANALYTICS_DATA_PATH.toString(),miliSec+".json");
            try{
            Files.deleteIfExists(path);
            Files.createFile(path);
            mapper.writeValue(new File(path.toString()), activeContexts);
            }catch(IOException ex){throw new RuntimeException(ex);}
    }
    
    /**
     * Saves all active contexts without removing them from active contexts
     */
    public void saveActiveContexts() 
    {
        Collection<Context> cont = activeContexts.values();
        cont.forEach((context) -> {
            Path path = Paths.get(DATA_PATH.toString(), context.getName()+".json");
            try{
                Files.deleteIfExists(path);
                Files.createFile(path);
                mapper.writeValue(new File(path.toString()), context);
            }catch(IOException ex){throw new RuntimeException(ex);}
        });
    }
    
    /**
     * Gets all the active contexts
     * @return A String array containing all active contexts' names
     */
    public String[] getActiveContexts()
    {
        Object[] r =activeContexts.keySet().toArray();
        String[] res = new String[r.length];
        for(int i=0; i<r.length;i++)
            res[i]=(String) r[i];
        return res;
    }
    
    /**
     * Gets the number of active contexts
     * @return The number of active contexts
     */
    public int getActiveContextsSize()
    {
        return activeContexts.size();
    }
    
    /**
     * Gets the name of the current context
     * @return The name of the current context, or null if there is not a current context
     */
    public String getCurrentContext()
    {
        if(currentContext==null)
            return null;
        return currentContext.getName();
    }
    
    /**
     * Deletes a context from the Contextual Ego Network, and removes the file it is serialized in (if it exists)
     * @param contextName Identifier of the context
     * @return True if it has been deleted correctly, false if it doesn't exists
     * @throws NullPointerException if contextName is null
     */
    public boolean deleteContext(String contextName)
    {
        if(contextName == null) throw new NullPointerException();

        if(contexts.remove(contextName)!=null)
        {
            activeContexts.remove(contextName);
            if(currentContext!=null){
            if(currentContext.getName().equals(contextName))
                currentContext = null; 
            }
            Collection<ArrayList<String>> col = nodes.values();
            col.forEach((list) -> {
                list.remove(contextName);
            });
            try {
                Files.deleteIfExists(Paths.get(DATA_PATH.toString(), contextName+".json"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            deleteLayer(contextName);
            return true;
        }       
        return false;
        
    }
    
    /**
     * Sets the Current context to a specific one identified by contextName. If non-active, it is loaded
     * @param contextName Identifier of the context
     * @throws NullPointerException if contextName is null
     * @throws ContextException if the context doesn't exist in this CEN
     */
    public void setCurrentContext(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        if(contexts.get(contextName)==null)
            throw new ContextException(contextName+" not exists");

        Context context = activeContexts.get(contextName);
        if(context==null)
        {
            try{context = mapper.readValue(new File(DATA_PATH.toString() + "/" + contextName+".json"), Context.class);}catch(IOException ex){throw new RuntimeException(ex);}
            activeContexts.put(contextName, context);
            context.saveTimeOfLoad();
        }
        currentContext = context;
        
    }
    /**
     * Adds an edge to the current context
     * @param src Identifier of the source node
     * @param dst Identifier of the dst node
     * @throws NullPointerException If src or dst is null
     * @throws ContextException If there is no current context
     * @return 0 if the edge has been added successfully, -1 otherwise (if Context.addEdge method fails)
     */
    public int addEdge(String src,String dst)
    {
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        if(src == null || dst == null) throw new NullPointerException();
        return currentContext.addEdge(src,dst);
    }

    /**
     * Adds an edge to a context identified by contextName
     * Context is set to current context and loaded if necessary.
     * @param src Identifier of the source node
     * @param dst Identifier of the dst node
     * @param contextName The name of the context
     * @throws NullPointerException If src, dst or contextName is null
     * @return 0 if the edge has been added su successfully, -1 otherwise (if Context.addEdge method fails)
     */
    public int addEdge(String contextName, String src, String dst)
    {
        if(src == null || dst == null || contextName == null) throw new NullPointerException();
        setCurrentContext(contextName);
        return addEdge(src, dst);
    }
    
    /**
     * Removes the edge from src to dst in the current context.
     * if the edge exists, but it is the only one that connects the ego to another node : the node is removed because it has no relationship with the ego(egonetwork rep invariant)
     * @param src Identifier of the source node
     * @param dst Identifier of the dst node
     * @return true if the edge is deleted correctly, false if also src or dst have been deleted
     * @throws NullPointerException If src or dst are null
     * @throws IllegalArgumentException If src is equal to dest
     */
    public boolean removeEdge(String src, String dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(src.equals(dst)) throw new IllegalArgumentException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        int resp=currentContext.removeEdge(src, dst);
        switch (resp) {
            case 0:
                return true;
            case 3:
                throw new NodeException("src || dst doesn't exists");
            case 4:
                throw new NodeException("src==dst");
        //the alter (the other node of the edge) is also removed
            default:
                String toRemove;
                if(src.equals(egoNode.getUsername()))
                    toRemove = dst;
                else
                    toRemove = src;
                contexts.get(currentContext.getName()).remove(toRemove);
                nodes.get(toRemove).remove(currentContext.getName());
                return false;
        }
    }

    /**
     * Removes the edge from src to dst in the context. If the edge exists,
     * but it is the only one that connects the ego to another node : the node is removed because it has no relationship with the ego(egonetwork rep invariant).
     * Context is set to current context and loaded if necessary.
     * @param contextName Identifier of the context
     * @param src Identifier of the source node
     * @param dst Identifier of the dst node
     * @return true if the edge is deleted correctly, false if also src or dst have been deleted.
     * @throws NullPointerException if src, dst or contextName are null
     */
    public boolean removeEdge(String contextName, String src, String dst)
    {
        if(src == null || dst == null || contextName == null) throw new NullPointerException();
        setCurrentContext(contextName);
        return removeEdge(src, dst);
    }

    /**
     * Adds the node to the context together with the arc that connects it to the ego (egonetwork rep invariant).
     * Context is set to current context and loaded if necessary.
     * @param contextName Identifier of the context
     * @param nodeName Username of the node
     * @param nodeId Identifier of the new node
     * @return True if the node is added successfully to the context, false if a node with the same username exists or the new node's username
     *         corresponds to ego's username
     * @throws NullPointerException if contextName or nodeName is null
     */
    public boolean addNode(String contextName, String nodeName, Long nodeId)
    {
        if(contextName == null || nodeName == null) throw new NullPointerException();
        setCurrentContext(contextName);     
        return addNode(nodeName, nodeId);
    }
    
    /**
     * Adds the node to the current context together with the arc that connects it to the ego (egonetwork rep invariant),
     * @param nodeName The username of the node
     * @param nodeId The identifier of the node
     * @return True if the node is added successfully to the context, false if a node with the same username exists or the new node's username
     *         corresponds to ego's username
     * @throws NullPointerException if nodeName is null
     * @throws ContextException if there is no active context
     */
    public boolean addNode(String nodeName, Long nodeId)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null)
           throw new ContextException(" there is no current context");
        String contextName = currentContext.getName();
        //If the node doesn't belong to any context I add it to the nodes list
        if(nodes.get(nodeName) == null)
        {
            nodes.put(nodeName, new ArrayList<>());
            nodes.get(nodeName).add(contextName);
        }
        //I add current context to the node's contexts
        contexts.get(contextName).add(nodeName);
        return currentContext.addNode(new Node(nodeName, nodeId));
        
    }


    /**
     * Adds a Person node to the current context
     * @param username Username of the node
     * @param identifier Numeric identifier of the node
     * @param firstName First name of the person
     * @param secondName Second name of the person
     * @param surname Surname of the person
     * @param birthDate Birth date of the user
     * @return True if the Person is added successfully, false otherwise (if the addNode method fails)
     * @throws NullPointerException if username, firstName, secondName or surName are null
     * @throws IllegalArgumentException if userName, firstName or surname are empty strings
     * @throws ContextException if there is no active context
     */
    public boolean addPerson(String username, int identifier, String firstName, String secondName, String surname, long birthDate) {
        if(firstName == null || secondName == null || surname == null) throw new NullPointerException();
        if(firstName.equals("") || surname.equals("") || username.equals("")) throw new IllegalArgumentException("First name, surname or username cannot be empty strings");
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        String contextName = currentContext.getName();
        //If the node doesn't belong to any context I add it to the nodes list
        if(nodes.get(username) == null)
        {
            nodes.put(username, new ArrayList<>());
            nodes.get(username).add(contextName);
        }
        Node persNode = new Person(username, identifier, firstName, secondName, surname, birthDate);
        return currentContext.addNode(persNode);
    }

    /**
     * Adds a Smart object node to the current context
     * @param username Username of the node
     * @param id Numeric identifier of the node
     * @param macAddr MAC Address of the devide
     * @param man Manufacturer of the device
     * @param own Name of the owner of the device
     * @return True if the Smart Object is added successfully, false otherwise (if the addNode method fails)
     * @throws NullPointerException if username, macAddr, man or own are null
     * @throws IllegalArgumentException if the length of macAddr is different than 12, or if man or own are empty strings
     * @throws ContextException if there is no active context
     */
    public boolean addSmartObject(String username, long id, String macAddr, String man, String own) {
        if(username == null || macAddr == null || man == null || own == null) throw new NullPointerException();
        if(macAddr.length() != 12) throw new IllegalArgumentException("The MAC Address must be made of 12 digits");
        if(man.equals("") || own.equals("")) throw new IllegalArgumentException("Owner and manufacturer cannot be empty strings");
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        String contextName = currentContext.getName();
        //If the node doesn't belong to any context I add it to the nodes list
        if(nodes.get(username) == null)
        {
            nodes.put(username, new ArrayList<>());
            nodes.get(username).add(contextName);
        }
        Node smObNode = new SmartObject(username, id, macAddr, man, own);
        return currentContext.addNode(smObNode);
    }
    
    /**
     * Removes the node from the Context, the ego node can't be removed (egonetwork rep invariant).
     * Context is set to current context and loaded if necessary.
     * @param contextName The name of the context from which the node has to be removed
     * @param nodeName The name of the node that has to be removed
     * @return true if the node has been removed correctly,false if node doesn't exists.
     * @throws NullPointerException if contextName or nodeName is null
     */
    public boolean removeNode(String contextName, String nodeName)
    {
        if(contextName == null || nodeName == null) throw new NullPointerException();
        setCurrentContext(contextName);
        return removeNode(nodeName);
    }

    /**
     * Removes the node from the current context, the ego node can't be removed (egonetwork rep invariant).
     * @param nodeName The name of the node to remove
     * @return true if the node has been removed correctly,false if node doesn't exist in current context.
     * @throws NullPointerException if nodeName is null
     * @throws ContextException If there is no current context
     * @throws NodeException If the node is the ego (the ego node can't be removed)
     */
    public boolean removeNode(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        
        String contextName = currentContext.getName();
        
        if(nodeName.equals(egoNode.getUsername()))
            throw new NodeException("Ego can't be removed");


        if(currentContext.removeNode(nodeName)) //The node belongs to current context
        {
            //I remove the node from current context's nodes
            contexts.get(contextName).remove(nodeName);
            //I remove current context from the node's contexts
            nodes.get(nodeName).remove(contextName);
            //If the node doesn't belong to any context
            if(nodes.get(nodeName).isEmpty()) nodes.remove(nodeName);
            return true;
        }
        return false; //The node doesn't belong to current context
    }

    /**
     * Removes all current context nodes with score less than minThreshold
     * @param minThreshold The score threshold under which a node is considered inactive
     * @return The number of removed nodes
     * @throws ContextException If there is no current context
     */
    public int removeInactive(double minThreshold)
    {
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        
        String contextName = currentContext.getName();
        int res=0;
        
        String[] nodenames= currentContext.getNodeNames();
        for (String nodename : nodenames) {
            if(currentContext.getScore(nodename)<minThreshold)
            {
                res++;
                currentContext.removeNode(nodename);
                removeNode(nodename);
            }
        }
        return res;
    }

    /**
     * Gets the activity score of a node
     * @param nodeName Identifier of the node
     * @return The node's activity score, or -1 if the node doesn't belong to current context
     * @throws NullPointerException if nodeName is null
     * @throws ContextException if there is no current context
     */
    public double getScore(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null){
            throw new ContextException("There is no current context");
        }
        double res = currentContext.getScore(nodeName);
        return res;
    }
    
        
    
    /**
     * @param nodeName Name of the node
     * @return A map containing incoming edges of the node in the current context, where keys are the source nodes' names
     * and values are the edges' weights, i.e. the tie strengths.. Null is returned if node can't be retrieved.
     * @throws NullPointerException if nodeName is null
     * @throws ContextException If there is no current context
     */
    public HashMap<String,Double> getIncomingEdges(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        else
            return currentContext.getInEdges(nodeName);
        
    }

    /**
     * @param nodeName Name of the node
     * @return A map containing outgoing edges of the node in the current context, where keys are the destination nodes' names
     * and values are the edges' weights, i.e. the tie strengths.. Null is returned if node can't be retrieved.
     * @throws NullPointerException if nodeName is null
     * @throws ContextException If there is no current context
     */
    public HashMap<String,Double> getOutgoingEdges(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        else
            return currentContext.getOutEdges(nodeName);
    }

    /**
     * Gets the name of the ego in the network
     * @return Name of the ego in the network
     */
    public String getEgoName()
    {
        return this.egoName;
    }
    
    
    /**
     * Extracts the percentage of days of the week and hours of the day in which a specific context context has been active
     * N.B: The context needs to be active in order to retrieve this information
     * @param contextName Identifier of the context
     * @return A float matrix F where vec[dayOfWeek][hourOfDay] == percentage use of that time slot with respect to the total hours that the context has been active
     * N.B.: F[0] represents sunday
     * @throws NullPointerException if contextName is null
     * @throws ContextException if the context is not active
     */
    public float[][] getRecurrencyArray(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        
        Context context = activeContexts.get(contextName);
        if(context == null)
            throw new ContextException("The context needs to be active");
        return context.getRecurrencyArray();   
    }
    
    /**
     * Computes the Jaccard similarity index between two contexts, i.e. the index related to their sets of users
     * @param firstContextName The name of the first context whose nodes we want to compute the index for
     * @param secondContextName The second context whose nodes we want to find the index for
     * @return Jaccard similarity index of context's nodes, -1 if one of the contexts don't have any nodes
     * @throws NullPointerException if firstContextName is null or secondContextName is null
     */
    public float NodesJaccardIndex(String firstContextName, String secondContextName)
    {
        if(firstContextName == null || secondContextName == null) throw new NullPointerException();
        ArrayList<String> firstActors = contexts.get(firstContextName);
        ArrayList<String> secondActors = contexts.get(secondContextName);
        if(firstActors == null || secondActors == null)
             return -1;
        
        ArrayList<String> longerList;
        ArrayList<String> shorterList;
        
        float firstLen = firstActors.size();
        float secondLen = secondActors.size();
        if(firstLen>=secondLen)
        {
            longerList = (ArrayList)firstActors.clone();
            shorterList = (ArrayList)secondActors.clone();
        }
        else
        {
            longerList = (ArrayList)secondActors.clone();
            shorterList = (ArrayList)firstActors.clone();
        }
        
        
        longerList.retainAll(shorterList); //longerList now contains the intersection of the actors of the two contexts
        float union = firstLen + secondLen - longerList.size();
        if(union == 0)
            return 0;
        
        return (longerList.size()/union);
        
    }
    
    /**
     * Computes the Jaccard similarity index between two actors of the CEN, i.e. the index related to the sets of contexts they belong to
     * @param firstNodeName The name of the first node whose contexts we want to compute the index for
     * @param secondNodeName The name of the second node whose contexts we want to compute the index for
     * @return Jaccard similarity index of the node's contexts, -1 if one of the two nodes doesn't exist
     */
    public float ContextsJaccardIndex(String firstNodeName, String secondNodeName)
    {
        if(firstNodeName == null || secondNodeName == null) throw new NullPointerException();
        ArrayList<String> firstContexts = nodes.get(firstNodeName);
        ArrayList<String> secondContexts = nodes.get(secondNodeName);
        if(firstContexts == null || secondContexts == null) return -1;
        
        ArrayList<String> longerList;
        ArrayList<String> shorterList;
        
        float firstLen = firstContexts.size();
        float secondLen = secondContexts.size();
        if(firstLen>=secondLen)
        {
            longerList = (ArrayList)firstContexts.clone();
            shorterList = (ArrayList)secondContexts.clone();
        }
        else
        {
            longerList = (ArrayList)secondContexts.clone();
            shorterList = (ArrayList)firstContexts.clone();
        }
        
        
        longerList.retainAll(shorterList); //longerList now contains the intersection of the contexts of the two actors
        float union = firstLen + secondLen- longerList.size();
        if(union==0)
            return 0;
        
        return (longerList.size()/union);
        
    }

    /**
     * Gets a context by its name
     * @param contextName The name of the context to be returned
     * @return The context identified by contextname (if active), null otherwise
     * @throws NullPointerException if contextName is null
     */
    public Context getContext(String contextName) {
        if(contextName == null) throw new NullPointerException();
        if(activeContexts.containsKey(contextName)) return activeContexts.get(contextName);
        else return null;
    }
    
    /**
     * Gets the nodes of a context
     * @param contextName The identifier of the context
     * @return A String vector of nodes contained in this context, null if the context doesn't exists
     * @throws NullPointerException if contextName is null
     */
    public String[] getNodes(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        
        ArrayList<String> nods = contexts.get(contextName);
        if(nods == null)
            return null;
        String[] res = new String[nods.size()];
        return  nods.toArray(res);
    }
    /**
     * Gets all contexts which a certain node belongs to
     * @param nodeName The identifier of the node
     * @return Null if node doesn't exists, a vector of all contexts if nodeName == null, a vector of the contexts where the node is present otherwise.
     */
    public String[] getContexts(String nodeName)
    {
        String[] res;
        if(nodeName==null)
        {
            Set<String> tmp = contexts.keySet();
            res = new String[tmp.size()];
            return tmp.toArray(res);
        }
        
        ArrayList<String> cont = nodes.get(nodeName);
        if(cont==null)
            return null;
        res = new String[cont.size()];
        return  cont.toArray(res);
    }
    /**
     * Return a boolean value that tells if a context is active or not.
     * @param contextName The identifier of the context
     * @return true if the context is active,false otherwise
     * @throws NullPointerException if contextName is null
     */
    public boolean isActive(String contextName)
    {
        if(contextName == null) throw new NullPointerException();
        Context context = activeContexts.get(contextName);
        return context != null;
    }

    /**
     * Check if a certain node belongs to a certain context
     * @param contextName The identifier of the context
     * @param nodeName The identifier of the node
     * @return true if node is present in the context, false otherwise
     * @throws NullPointerException if contextName or nodeName is null
     */
    public boolean containsNode(String contextName,String nodeName)
    {
        if(contextName == null || nodeName == null) throw new NullPointerException();
        
        ArrayList<String> nods = contexts.get(contextName);
        if(nods==null)
            return false;
        else if(nodeName.equals(egoNode.getUsername()))
            return true;
        else
            return nods.contains(nodeName);
    }
    
    /**
     * Return a boolean that tells if a certain node is online or not in the current context.
     * @param nodeName The identifier of the node
     * @return true if the node is online in the current context, false if the node is not online or if it doesn't belong to current context.
     * @throws NullPointerException if nodeName is null
     * @throws ContextException if there is no current context
     */
    public boolean isOnline(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(currentContext==null)
             throw new ContextException("There is no current context");
        int resp = currentContext.getNodeStatus(nodeName);
        if(resp <= 0) return false;
        else return true;
    }
    
    /**
     * Gets the online status of a node in a specific context, identified by contextName.
     * The context is set to current context and loaded if necessary.
     * @param contextName The identifier of the context
     * @param nodeName The identifier of the node
     * @return true if the node is online in the current context, false if the node is not online or if it doesn't belong to that context.
     * @throws NullPointerException if contextName or nodeName is null
     */
    public boolean isOnline(String contextName,String nodeName) 
    {
        if(contextName == null || nodeName == null) throw new NullPointerException();
        setCurrentContext(contextName);
        return isOnline(nodeName);
    }
    
    /**
     * Sets a node online in a context, which is set to current context and loaded if necessary.
     * @param contextName The identifier of the context
     * @param nodeName The identifier of the node
     * @param value true to set online, false to set offline
     * @throws NullPointerException if contextName or nodeName is null
     */
    public void setOnline(String contextName, String nodeName, boolean value)
    {
        if(contextName == null || nodeName == null) throw new NullPointerException();
       setCurrentContext(contextName);
       setOnline(nodeName,value);
    }
    
    /**
     * Sets a node online in the current context
     * @param nodeName The identifier of the node
     * @param value true to set online, false to set offline
     * @throws NullPointerException if nodeName is null
     * @throws ContextException If there is no current context
     */
    public void setOnline(String nodeName, boolean value)
    {
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        
        currentContext.setOnline(nodeName,value);
    }
    
    /**
     * Gets the weight of an edge, i.e. the tie strength
     * @param srcName The identifier of the source node
     * @param dstName The identifier of the destination node
     * @return Weight of the edge in current context (if src, dst and edge exist in current context), null otherwise
     * @throws NullPointerException if srcName or dstName are null
     * @throws ContextException If there is no current context
     */
    public Double getEdgeWeight(String srcName, String dstName)
    {
        if(srcName == null || dstName == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        if(srcName.equals(dstName))
            throw new NodeException("src == dst");
        return currentContext.getEdgeWeight(srcName, dstName);
    }
    
    /**
     * Gets the timestamp of an edge
     * @param srcName The identifier of the source node
     * @param dstName The identifier of the destination node
     * @return timeStamp of the edge (if exist src,dst and edge)in the current context. -2 if a node doesn't exists, -1 if the edge doesn't exists.
     * @throws NullPointerException if srcName or dstName are null
     * @throws ContextException If there is no current context
     */
    public long getEdgeTimestamp(String srcName,String dstName)
    {
        if(srcName == null || dstName == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        if(srcName.equals(dstName))
            throw new NodeException("src == dst");
        return currentContext.getEdgeTimeStamp(srcName, dstName);
    }
    
    
   /**
    * Resets this instance (all context files will be deleted)
    */
    public void Reset()
    {
        cleanContextData();
        cleanAnalyticsData();
        
        this.nodes = new HashMap<>();
        this.contexts = new HashMap<>();
        this.activeContexts = new HashMap<>();
        this.deserializers= new HashMap<>();
        this.currentContext = null;
        
        try{
        if (!Files.exists(ANALYTICS_DATA_PATH)){
            Files.createDirectory(ANALYTICS_DATA_PATH);
        }
        if (!Files.exists(DATA_PATH))
        {
            Files.createDirectory(DATA_PATH);
        }
        }catch(IOException ex){}
        
        
    }
    
    /**
     * Deletes the analytics context data directory
     */
    public void cleanAnalyticsData()
    {
        try{
        Files.walk(ANALYTICS_DATA_PATH)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .peek(System.out::println)
            .forEach(File::delete);
        }catch(IOException ex){}
    }
    
    /**
     * Deletes the context data directory
     */
    public void cleanContextData()
    {
        try{
        Files.walk(DATA_PATH)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .peek(System.out::println)
            .forEach(File::delete);
        
        }
        catch(IOException ex){}
    }
    /**
     * Adds a custom deserializer for the clazz's type. 
     * registered customSerializers are not persistent, for subsequent instances they will have to be registered again.
     * @param <V> The class of the deserializer
     * @param myClass The class for which we want to add a deserializer
     * @param deserializer myClass custom deserializer
     */
    public  <V extends CustomDeserializer> void addCustomDeserializer(Class<?> myClass, V deserializer)
    {
        deserializers.put(myClass.getName(), deserializer);
    }

    /**
     * Removes the serializer registered for myClass
     * @param myClass The class for which we want to remove a deserializer
     */
    public void removeCustomDeserializer(Class<?> myClass)
    {
        deserializers.remove(myClass.getName());
    }

    /**
     * Gets all the class names that are registered for serialization
     * @return A vector of class names registered for serialization
     */
    public String[] getCustomSerializableClassNames()
    {
        Set<String> kset = deserializers.keySet();
        String[] res = new String[kset.size()];
        return kset.toArray(res);
    }

    /**
     * Gets the custom deserializer registered for the class myClass
     * @param myClass The class for which we want to get the deserializer
     * @return A custom serializer for the class myClass
     */
    public Object getCustomDeserializer(Class<?> myClass)
    {
        return deserializers.get(myClass.getName());
    }
    /**
     * Sets an attachment on the current context
     * @param <T> The type of the attachment
     * @param data The attachment to be set
     * @throws ClassNotFoundException if there is no custom deserializer registered for the attachment's type
     */
    public <T> void setDataOfContext(T data) throws ClassNotFoundException
    {
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        String classname = data.getClass().getName();
        CustomDeserializer des = deserializers.get(classname);
        if(des==null)
            throw new ClassNotFoundException("can't find "+classname+" Customdeserializer");
        currentContext.setContextData(des.toJson(data));
        currentContext.setContextDataClass(classname);
        currentContext.setDataObject(data);
    }
    /**
     * Gets the object data of current context
     * @return the current context attachment
     * @throws ClassNotFoundException if there is no custom deserializer registered for the attachment's type
     */
    public Object getDataOfContext() throws ClassNotFoundException
    {   
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        Object res = currentContext.getDataObject();
        if(res!=null)
            return res;
        String contextData = currentContext.getContextData();
        if(contextData==null)
            return null;
        String contextdataclass = currentContext.getContextDataClass();
        CustomDeserializer des = deserializers.get(contextdataclass);
        if(des==null)
            throw new ClassNotFoundException("can't find "+contextdataclass+" Customdeserializer");
        return des.fromJson(contextData);
    }
    
    /**
     * Gets the object data of a specific node in current context
     * @param nodeName The node we want to get the data of
     * @return node's attachment in the current context.
     * @throws ClassNotFoundException if there is no custom deserializer registered for the attachment's type
     * @throws NullPointerException if nodeName is null
     * @throws ContextException if there is no current context
     */
    public Object getDataOfNode(String nodeName) throws ClassNotFoundException
    {
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        if(nodeName == null) throw new NullPointerException();
        if(!containsNode(currentContext.getName(), nodeName))
            throw new NodeException(nodeName+" doesn't exists");
        Object res = currentContext.getNodeDataObject(nodeName);
        if(res!=null)
            return res;
        String nodedata = currentContext.getNodeData(nodeName);
        if(nodedata==null)
            return null;
        String nodedataclass = currentContext.getNodeDataClass(nodeName);
        CustomDeserializer des = deserializers.get(nodedataclass);
        if(des==null)
            throw new ClassNotFoundException("can't find "+nodedataclass+" Customdeserializer");
        return des.fromJson(nodedata);
        
    }
    
    /**
     * Sets an attachment on a node in the current context.
     * @param <T> The type of the object attachment to be set
     * @param nodeName The identifier of a node
     * @param data The attachment to be set
     * @throws ClassNotFoundException if there is no custom deserializer registered for the attachment's type
     * @throws NullPointerException if nodeName or data is null
     * @throws ContextException if there is no current context
     */
    public <T> void setDataOfNode(String nodeName,T data) throws ClassNotFoundException
    {
        if(nodeName == null || data == null) throw new NullPointerException();
        if(currentContext==null)
            throw new ContextException(" there is no current context");
        if(!containsNode(currentContext.getName(), nodeName))
            throw new NodeException(nodeName+" doesn't exists");
        String classname = data.getClass().getName();
        CustomDeserializer des = deserializers.get(classname);
        if(des==null)
            throw new ClassNotFoundException("can't find "+classname+" Customdeserializer");
        currentContext.setNodeObjectData(nodeName,des.toJson(data));
        currentContext.setNodeDataClass(nodeName,classname);
        currentContext.setNodeObjectData(nodeName,data);
    }

}

