package contextualegonetwork;

import java.util.HashMap;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

/**
 * This class implements a node in the Social Graph, which is representative of a node in a particular
 * layer of the HSG. Nodes are mapped in-between layers through a mapping function, for which two nodes
 * in different layers correspond to each other if they have the identifier
 */
@JsonIdentityInfo(generator=JSOGGenerator.class)
class Node {

    /**
     * Numeric identifier of the node
     */
    private long id;
    /**
     * String data carried by the node
     */
    private String data;

    /**
     * Object data carried by the node
     */
    private Object dataObject;
    /**
     * Class of the object data carried by the node
     */
    private String dataClass;
    /**
     * Username of the person/smart object
     */
    private String username;
    /**
     * Status of the node (if false offline, if true online)
     */
    private boolean online;
    /**
     * Timestamp of creation of the node
     */
    private long creation_date;
    /**
     * Number of times the node's status is set to online
     */
    private long online_counter;

    /**
     * Out-going edges
     */
    HashMap<String, Edge> outEdges;
    /**
     * In-going edges
     */
    HashMap<String, Edge> inEdges;
    /**
     * Interactions on the edges of this node
     */
    HashMap<Integer, ArrayList<Interaction>> edgeInteractions;

    /**
     * Constructor method
     * @param usrname Username of the node
     * @param id Numeric identifier of the node
     * @throws NullPointerException if the username parameter is null
     * @throws IllegalArgumentException if the username parameter is empty
     */
    public Node(String usrname, long id)
    {
        if(usrname == null) throw new NullPointerException();
        if(usrname.equals("")) throw new IllegalArgumentException("The username cannot be an empty string");
        this.username = usrname;
        this.outEdges = new HashMap<>();
        this.inEdges = new HashMap<>();
        this.edgeInteractions = new HashMap<>();
        this.creation_date= System.currentTimeMillis();
        this.id = id;
    }

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Node()
    {}

    /**
     * @return The number of times the node's state has been online
     */
    public long getOnline_counter()
    {
        return this.online_counter;
    }

    /**
     * Sets the creation date of the node to a specific timestamp passed as argument
     * @param date The timestamp to be set to the creation date
     */
    public void setCreation_date(long date)
    {
        this.creation_date=date;  
    }

    /**
     * @return The timestamp of when the node was created
     */
    public long getCreation_date()
    {
        return this.creation_date;
    }

    /**
     * Sets the field dataClass
     * @param data The class to be set, which is the class of dataObject
     * @throws NullPointerException if data is null
     */
    public void setDataClass(String data)
    {
        if(data == null) throw new NullPointerException();
        this.dataClass= data;
    }

    /**
     * 
     * @return The class of dataObject
     */
    public String getDataClass()
    {
        return this.dataClass;
    }

    /**
     * Attaches an object data to the node (if the field was not empty it gets overwritten)
     * @param obj The object data to attach
     * @throws NullPointerException if obj is null
     */
    public void setDataObject(Object obj)
    {
        if(obj == null) throw new NullPointerException();
        this.dataObject = obj;
    }

    /**
     *
     * @return The object data attached to the node
     */
    public Object getDataObject()
    {
        return this.dataObject;
    }

    /**
     *
     * @return The string data attached to the node
     */
    public String getData()
    {
        return data;
    }

    /**
     * Sets the string data field of the node to a string passed as parameter (if the field was not empty it gets overwritten)
     * @param data The string data to be attached to the node
     * @throws NullPointerException if data is null
     */
    public void setData(String data)
    {
        if(data == null) throw new NullPointerException();
        this.data = data;
    }

    /**
     * Sets the out-going edges of the node
     * @param edges A hash map containing the out-going edges to be set
     * @throws NullPointerException if toedges is null
     */
    public void setOutEdges(HashMap<String, Edge> edges)
    {
        if(edges == null) throw new NullPointerException();
        this.outEdges = edges;
    }

    /**
     * Sets the in-going edges of the node
     * @param edges A hash map containing the in-going edges to be set
     * @throws NullPointerException if fromedges is null
     */
    public void setInEdges(HashMap<String, Edge> edges)
    {
        if(edges == null) throw new NullPointerException();
        this.inEdges = edges;
    }
    //CONTROLLO SE L'EDGE ESISTE GIA'
    /**
     * Adds an out-going edge
     * @param edge The edge that is added to the out-going edges hash map
     * @throws NullPointerException if edge is null
     */
    public void addOutEdge(Edge edge)
    {
        if(edge == null) throw new NullPointerException();
        if(!outEdges.containsKey(edge.getDst().getUsername())) {
        outEdges.put(edge.getDst().getUsername(), edge);
        int edgeId = edge.getId();
        createEdgeList(edgeId);
        }

    }
    
    /**
     * Removes an out-going edge
     * @param dst The destination node of the edge that needs to be removed
     * @return An integer that tells the outcome of the operation: -1 if there wasn't an out-going edge directed towards
     *         dst, 0 if there was one and it has been removed successfully
     * @throws NullPointerException if dst is null
     */
    public int removeOutEdge(String dst) //ERA REMOVETOEDGE
    {
        if(dst == null) throw new NullPointerException();
        if(!outEdges.keySet().contains(dst)) return -1;
        else outEdges.remove(dst);
        return 0;
    }

    /**
     * Adds an in-going edge
     * @param edge The edge that is added to the in-going edges hash map
     * @throws NullPointerException if edge is null
     */
    public void addInEdge(Edge edge)
    {
        if(edge == null) throw new NullPointerException();
        if(!inEdges.containsKey(edge.getSrc().getUsername())) {
            inEdges.put(edge.getSrc().getUsername(), edge);
            int edgeId = edge.getId();
            createEdgeList(edgeId);
        }

    }

    /**
     * Removes an in-going edge
     * @param src The source node of the edge that needs to be removed
     * @return An integer that tells the outcome of the operation: -1 if there wasn't an in-going edge having src
     *         as source, 0 if there was one and it has been removed successfully
     * @throws NullPointerException if src is null
     */
    public int removeInEdge(String src)
    {
        if(src == null) throw new NullPointerException();
        if(!inEdges.keySet().contains(src)) return -1;
        else outEdges.remove(src);
        return 0;
    }
    

    /**
     * @return The edges whose destination node is this node
     */
    public HashMap<String, Edge> getInEdges()
    {
        return inEdges;
    }

    /**
     * @return The edges whose source node is the this node
     */
    public HashMap<String, Edge> getOutEdges()
    {
        return outEdges;
    }

    /**
     * @return The username of the node
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * Sets the status of the node to online
     */
    public void setOnline() {
        this.online = true;
    }

    /**
     * Sets the status of the node to offline
     */
    public void setOffline() {
        this.online = false;
    }

    /**
     * @return The status of the node (true if online, false if offline)
     */
    public boolean getOnlineStatus()
    {
        return this.online;
    }

    //LA USO SOLO DENTRO PER CREARE LA LISTA. SE USATA FUORI, TOGLIERE

    /**
     * Private method, only used inside the class. Creates an empty arraylist to store future interactions
     * happening on a newly-created edge.
     * @param edgeId The id of the edge for which the arraylist is created
     */
    private void createEdgeList(Integer edgeId) {
        edgeInteractions.put(edgeId, new ArrayList<>());
    }

    /**
     * Adds an interaction on an edge of this node. The edge is identified by its source and destination.
     * This node needs to be either the source or the destination of the edge.
     * @param src The source node of the edge
     * @param dst The destination node of the edge
     * @param timestamp The timestamp of when the interaction started
     * @param duration The duration of the interaction
     * @param type The type of the interaction
     * @return -1 if neither the source nor the destination of the edge is this node,
     *          -2 if the edge having this node as source or destination doesn't exist,
     *          0 if the operation was successful
     * @throws NullPointerException if src, dst or type are null
     * @throws IllegalArgumentException if timestamp is less than 0 or duration is less than 0 or type is an empty string
     */
    public int addEdgeInteraction(Node src, Node dst, long timestamp, int duration, String type) {
        if(src == null || dst == null || type == null) throw new NullPointerException();
        if(timestamp < 0 || duration < 0) throw new IllegalArgumentException("Timestamp and duration cannot be negative");
        if(type.equals("")) throw new IllegalArgumentException("Type cannot be empty");
        Integer edgeId;
        if(!(this == src || this == dst)) return -1;
        if(this == src) {
            if(outEdges.containsKey(dst.getUsername())) {
                edgeId = outEdges.get(dst.getUsername()).getId();
                edgeInteractions.get(edgeId).add(new Interaction(timestamp, duration, type));
            }
            else return -2;
        }
        else if(this == dst) {
            if(inEdges.containsKey(src.getUsername())) {
                edgeId = inEdges.get(src.getUsername()).getId();
                edgeInteractions.get(edgeId).add(new Interaction(timestamp, duration, type));
            }
            else return -2;
        }
        return 0;
    }

    /**
     * Adds an interaction on an edge of this node. The edge is identified by its numerical identifier.
     * @param timestamp The timestamp of when the interaction started
     * @param duration The duration of the interaction
     * @param type The type of the interaction
     * @param edgeId The id of the edge
     * @return  -1 if the edge having this node as source or destination doesn't exist,
     *          0 if the operation was successful
     * @throws NullPointerException if src, dst or type are null
     * @throws IllegalArgumentException if timestamp is less than 0 or duration is less than 0 or type is an empty string
     */
    public int addEdgeInteraction(int edgeId, long timestamp, int duration, String type) {
        if(type == null) throw new NullPointerException();
        if(timestamp < 0 || duration < 0) throw new IllegalArgumentException("Timestamp and duration cannot be negative");
        if(type.equals("")) throw new IllegalArgumentException("Type cannot be empty");
        if(edgeId < 0) throw new IllegalArgumentException("The identifier cannot be negative");
        if(!edgeInteractions.containsKey(edgeId)) return -1;
        edgeInteractions.get(edgeId).add(new Interaction(timestamp, duration, type));
        return 0;
    }

    /**
     * Gets the list of interactions that have taken place on a specific edge
     * @param src The source of the edge
     * @param dst The destination edge of the edge
     * @return A shallow copy of the arraylist of interactions
     * @throws NullPointerException if src or dst are null
     * @throws IllegalArgumentException if the node is neither the source or the destination of the edge,
     *                                  or if the edge doesn't exist
     */
    public ArrayList<Interaction> getInteractions(Node src, Node dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(!(this == src || this == dst)) throw new IllegalArgumentException("The node must be either source or destination of the edge");
        int edgeId;
        ArrayList<Interaction> interactions = new ArrayList<Interaction>();
        if(this == src) {
            if(outEdges.containsKey(dst.getUsername())) {
                edgeId = outEdges.get(dst.getUsername()).getId();
                interactions = edgeInteractions.get(edgeId);
            }
            else throw new IllegalArgumentException("There is no edge having this node as its source");
        }
        else if(this == dst) {
            if(inEdges.containsKey(src.getUsername())) {
                edgeId = outEdges.get(dst.getUsername()).getId();
                interactions = edgeInteractions.get(edgeId);


            }
            else throw new IllegalArgumentException("There is no edge having this node as its destination");
        }
        return (ArrayList<Interaction>) interactions.clone();
    }

    /**
     * Gets the list of interactions that have taken place on a specific edge
     * @param edgeId The id of the edge
     * @return A shallow copy of the arrayList of interactions
     * @throws IllegalArgumentException if edgeId is less than 0,
     *                                  or if the edge doesn't exist
     */
    public ArrayList<Interaction> getInteractions(int edgeId)
    {
        if(edgeId < 0) throw new IllegalArgumentException("The identifier cannot be negative");
        if(!edgeInteractions.containsKey(edgeId)) throw new IllegalArgumentException("The edge having this id doesn't exist");
        return (ArrayList<Interaction>) edgeInteractions.get(edgeId).clone();
    }


    /**
     * @return A double, which is the score of the node (number of times online divided life span of the node)
     */
    @JsonIgnore
    public double getScore()
    {
        return ((double)online_counter)/((double)(System.currentTimeMillis()-creation_date));
    }

    /**
     *
     * @return The identifier of the node
     */
    public long getId() {
        return this.id;
    }
}