
package contextualegonetwork;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator=JSOGGenerator.class)

/**
 * This class implements a context of the Contextual Ego Network. The context stores all the information related
 * to the nodes, i.e. the actors (humans or smart objects) that engage in relationships inside it, and keeps track of its spatial and temporal
 * characteristics. The context can be in three possible states: it can be the current online context, it can be one
 * of the active contexts (saved in local memory), or it can be non-active (and thus serialized and stored on disk).
 */
class Context
{
    /**
     * Name (identifier) of the context
     */
    private String name;
    /**
     * Name of the ego of the Contextual Ego Network
     */
    private String egoName;
    /**
     * Spatial coordinates
     */
    private double latitude, longitude;
    /**
     * Total number of hours the context has been active
     */
    private long totalHourActive;
    /**
     * Counter of the times the context has been active during the week. It is useful
     * to determine the temporal slots in which the context is recurrently active
     * (i.e. reccurring days of the week, recurring hours during a specific day of the week)
     * It is updated every time the context is unloaded
     * N.B. timeCounter[0] represents sunday
     */
    private long[][] timeCounter;
    /**
     * Day of the week the context has been loaded
     */
    @JsonIgnore
    private int tempDayOfWeek;
    /**
     * Hour of the day the context has been laaded
     */
    @JsonIgnore
    private int tempHour;
    /**
     * Structure that contains all the alters of this context
     */
    private HashMap<String, Node> nodes;
    /**
     * Counter that keeps track of the last id assigned to an edge in this context
     */
    private Integer edgeIdCounter;
    /**
     * String data carried by the context
     */
    private String contextData;
    /**
     * Object data carried by the context
     */
    @JsonIgnore
    private Object dataObject;
    /**
     * Class of the object data carried by the context
     */
    private String contextDataClass;

    /**
     * Constructor method
     * @param contextName Name of the context
     * @param egoNode Ego node
     * @throws NullPointerException if contextName or egoNode are null
     * @throws IllegalArgumentException if contextName is an empty string
     */
    public  Context(String contextName, Node egoNode)
    {
        if(contextName == null || egoNode == null) throw new NullPointerException();
        if(contextName.equals("")) throw new IllegalArgumentException("Context name cannot be empty");
        this.egoName = egoNode.getUsername();
        this.name = contextName;
        this.edgeIdCounter = 0;
        this.nodes = new HashMap<>();
        nodes.put(egoNode.getUsername(), egoNode);
        timeCounter = new long[7][24];
    }

    /**
     *
     * @return The object data attached to the context
     */
    @JsonIgnore
    public Object getDataObject()
    {
        return this.dataObject;
    }

    /**
     * Attaches an object data to the context (if the field was not empty it gets overwritten)
     * @param obj The object data to attach
     * @throws NullPointerException if obj is null
     */
    @JsonIgnore
    public void setDataObject(Object obj)
    {
        if(obj == null) throw new NullPointerException();
        this.dataObject = obj;
    }

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Context()
    {}

    /**
     *
     * @return The string data attached to the context
     */
    public String getContextData()
    {
        return this.contextData;
    }

    /**
     * Sets the string data field of the context to a string passed as parameter (if the field was not empty it gets overwritten)
     * @param data The string data to be attached to the context
     * @throws NullPointerException if data is null
     */
    public void setContextData(String data)
    {
        if(data == null) throw new NullPointerException();
        this.contextData= data;
    }

    /**
     *
     * @return The class of dataObject
     */
    public String getContextDataClass()
    {
        return this.contextDataClass;
    }

    /**
     * Sets the field dataClass
     * @param data The class to be set, which is the class of dataObject
     * @throws NullPointerException if data is null
     */
    public void setContextDataClass(String data)
    {
        if(data == null) throw new NullPointerException();
        this.contextDataClass= data;
    }

    /**
     * Saves the activation hour of the context
     */
    public void saveTimeOfLoad()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        this.tempDayOfWeek = c.get(Calendar.DAY_OF_WEEK)-1;
        this.tempHour = c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Updates the recurrencies array with the days of the week and hours of the days the context has been active.
     */
    public void saveTimeOfUnload()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int day = c.get(Calendar.DAY_OF_WEEK)-1;
        int hour = c.get(Calendar.HOUR_OF_DAY);

        /*if the context has been loaded and unloaded during the same day (with at least 1 hour of difference)*/
        if(tempHour<hour && tempDayOfWeek==day)
        {
            for(int i = tempHour; i<=hour; i++)
            {
                timeCounter[day][i]++;
                totalHourActive++;
            }
        }
        /*if the context has been loaded and unloaded within the same hour of the same day*/
        else if(tempHour==hour && tempDayOfWeek==day)
        {
            timeCounter[day][hour]++;
            totalHourActive++;
        }
        /*if the context has been loaded and unloaded in two different days*/
        else
        {
            for(int i = tempHour; i<24; i++)
            {
                timeCounter[tempDayOfWeek][i]++;
                totalHourActive++;
            }
            for(int i = 0;i<hour+1;i++)
            {
                timeCounter[day][i]++;
                totalHourActive++;
            }

            int cont = (tempDayOfWeek+1)%7;
            while(cont != day)
            {
                for(int i=0;i<24;i++)
                {
                    timeCounter[cont][i]++;
                    totalHourActive++;
                }
                cont = (cont++)%7;
            }

        }
    }

    /**
     * Extracts the percentage of days of the week and hours of the day in which the context has been active
     * @return An array containing the weekly recurrencies rates
     */
    @JsonIgnore
    public float[][] getRecurrencyArray()
    {
        if(totalHourActive==0)
            return null;
        
        float[][] res = new float[7][24];
        for(int i=0;i<7;i++)
        {
            for(int j=0;j<24;j++)
            {
                res[i][j] = ((float)timeCounter[i][j]/(float)totalHourActive)*100;  //slot percentage
            }
        }
        return res;
    }

    /**
     * @return A shallow copy of the recurrencies array
     */
    public long[][] getTimeCounter()
    {
        return timeCounter.clone();
    }

    /**
     * Sets the recurrencies array to the value passed as argument of the method
     * @param temp The array timeCounter is set to
     * @throws IllegalArgumentException If temp doesn't have the right size
     */
    public void setTimeCounter(long[][] temp)
    {
        if(temp.length != 7) throw new IllegalArgumentException();
        for(int i = 0; i < temp.length; i++) {
            if(temp[i].length != 24) throw new IllegalArgumentException();
        }
        this.timeCounter = temp;
    }

    /**
     * @return The total number of hours the context has been active
     */
    public long getTotalHourActive()
    {
        return totalHourActive;
    }

    /**
     * Sets the context's nodes to the value passed as argument of the method
     * @param nodes The HashMap the field nodes is set to
     * @throws NullPointerException If nodes is null
     */
    public void setNodes(HashMap<String,Node> nodes)
    {
        if(nodes == null) throw new NullPointerException();
        this.nodes = nodes;
    }

    /**
     * Sets the context's latitude to the value passed as argument of the method
     * @param latitude The new value for the latitude field
     */
    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    /**
     * Sets the context's longitude to the value passed as argument of the method
     * @param longitude The new value for the longitude field
     */
    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    /**
     * @return A shallow copy of the context's nodes hash map
     */
    public HashMap<String,Node> getNodes() { return (HashMap<String, Node>) this.nodes.clone(); }
    
    /**
     * @return The name of the context
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return The username of the ego of the context
     */
    public String getEgoName()
    {
        return this.egoName;
    }

    /**
     * @return The current latitude of the context
     */
    public double getLatitude()
    {
        return latitude;
    }

    /**
     * @return The current longitude of the context
     */
    public double getLongitude()
    {
        return longitude;
    }

    /**
     * @return A string array which contains the usernames of the nodes of the context (excluding the ego)
     */
    @JsonIgnore
    public String[] getNodeNames()
    {
        Object[] r =nodes.keySet().toArray();
        /*the ego is excluded*/
        String[] res = new String[r.length-1];
        int j=0;
        int i=0;
        while(i<r.length)
        {
            if(!((r[i]).equals(egoName)))
            {
               res[j]=(String)r[i];
               i++;j++;
            }
            else
                i++;  
        }
        return res;
    }

    /**
     * Adds an edge between two nodes of the social graph
     * @param src The source node's username
     * @param dst The destination node's username
     * @return -1 if either src or dst isn't a valid user of the context
     *          0 if the operation is successful
     * @throws NullPointerException If src or dst are null
     * @throws IllegalArgumentException If src and dst are the same node
     */
    @JsonIgnore
    public int addEdge(String src,String dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(src.equals(dst)) throw new IllegalArgumentException("Src and dest cannot be the same node");
        Node srcNode = nodes.get(src);
        Node dstNode = nodes.get(dst);
        if(srcNode== null || dstNode==null) return -1;
        Edge edge = new Edge(srcNode,dstNode,edgeIdCounter);
        srcNode.addOutEdge(edge);
        dstNode.addInEdge(edge);
        edgeIdCounter++;
        return 0;
    }

    /**
     * Gets a node given its username
     * @param nodeName The username of the node
     * @return The node that has nodeName as its username (if it exists), null otherwise
     * @throws NullPointerException If nodeName is null
     */
    public Node getNode(String nodeName) {
       if(nodeName == null) throw new NullPointerException();
       if(!nodes.containsKey(nodeName)) return null;
       else return nodes.get(nodeName);
    }

    /**
     * Adds an interaction on an edge between two nodes of the context
     * @param src The source node of the edge
     * @param dst The destination node of the edge
     * @param timestamp The start time of the interaction
     * @param duration The duration of the interaction
     * @param type The interactipn type
     */
    public void addInteraction(Node src, Node dst, long timestamp, int duration, String type) {
        src.addEdgeInteraction(src, dst, timestamp, duration, type);
        dst.addEdgeInteraction(src, dst, timestamp, duration, type);
    }
    
    /**
     * Gets the weight of the edge (if it exists)  between two nodes of the social graph
     * @param src The source node's username
     * @param dst The destination node's username
     * @return The weight of the edge if the source and the destination nodes are in the social graph and are
     *         connected by an edge, null otherwise
     * @throws NullPointerException If src and dest are null
     * @throws IllegalArgumentException If src and dest are the same node
     */
    @JsonIgnore
    public Double getEdgeWeight(String src,String dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(src.equals(dst)) throw new IllegalArgumentException();
        Node srcNode = nodes.get(src);
        Node dstNode = nodes.get(dst);
        if(srcNode== null || dstNode==null)
            return null;
        Edge edg = srcNode.getOutEdges().get(dst);
        if(edg==null)
            return null;
        else
            return edg.getTieStrength();
    }
    
    /**
     * Gets the creation time of the edge (if it exists) between two nodes of the social graph
     * @param src The source node's username
     * @param dst The destination node's username
     * @return The creation date of the edge if the source and the destination nodes are in the social graph and are
     *         connected by an edge, -2 if either the source or the destination do not exist, -1 if the edge doesn't exist
     * @throws NullPointerException If src and dest are null
     * @throws IllegalArgumentException If src and dest are the same node
     */
    @JsonIgnore
    public long getEdgeTimeStamp(String src,String dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(src.equals(dst)) throw new IllegalArgumentException();
        Node srcNode = nodes.get(src);
        Node dstNode = nodes.get(dst);
        if(srcNode== null || dstNode==null)
            return -2;
        Edge edg = srcNode.getOutEdges().get(dst);
        if(edg==null)
            return -1;
        else
            return edg.getCreationTime();
    }

    /**
     * Removes an edge between a source and the destination node, and if either the source or the destination node is the ego
     *        the alter node is removed from the social graph too (because the condition for which a node belongs to the ego network, that is only if
     *        it is connected to the ego with an edge, isn't satisfied anymore)
     * @param src The source node
     * @param dst The destination node
     * @return 4 if the source and the destionation nodes are the same, 3 if either the source node or the destination node
     *         do not exist, 0 if the edge is removed but the alter is not, -1 if both the edge and the alter are removed
     * @throws NullPointerException If src and dest are null
     * @throws IllegalArgumentException If src and dest are the same node
     */
    @JsonIgnore
    public int removeEdge(String src,String dst)
    {
        if(src == null || dst == null) throw new NullPointerException();
        if(src.equals(dst)) throw new IllegalArgumentException();
        Node srcNode = nodes.get(src);
        Node dstNode = nodes.get(dst);
        boolean tmp =false;
        if(srcNode== null || dstNode==null)
            return 3;
        else if(src.equals(this.egoName))
        {
            if(!srcNode.getInEdges().keySet().contains(dst)) /*condition to remain in the ego network*/
            {
                removeNode(dst);
                tmp = true;
            }
        }
        else if(dst.equals(this.egoName))
        {
            if(!dstNode.getOutEdges().keySet().contains(src)) /*condition to remain in the ego network*/
            {
                removeNode(src);
                tmp = true;
            }
        }
        srcNode.removeOutEdge(dst);
        dstNode.removeInEdge(src);
        if(tmp)
            return -1; //if the alter has also been removed
        return 0;
    }

    /**
     * Adds a Person node to the context
     * @param username Username of the node
     * @param identifier Numeric identifier of the node
     * @param firstName First name of the person
     * @param secondName Second name of the person
     * @param surname Surname of the person
     * @param birthDate Birth date of the user
     * @return True if the Person is added successfully, false otherwise (if the addNode method fails)
     * @throws NullPointerException if username, firstName, secondName or surName are null
     * @throws IllegalArgumentException if userName, firstName or surname are empty strings
     */
    public boolean addPerson(String username, int identifier, String firstName, String secondName, String surname, long birthDate) {
        if(firstName == null || secondName == null || surname == null) throw new NullPointerException();
        if(firstName.equals("") || surname.equals("") || username.equals("")) throw new IllegalArgumentException("First name, surname or username cannot be empty strings");
        Node persNode = new Person(username, identifier, firstName, secondName, surname, birthDate);
        return addNode(persNode);
    }

    /**
     * Adds a Smart object node to the context
     * @param username Username of the node
     * @param id Numeric identifier of the node
     * @param macAddr MAC Address of the devide
     * @param man Manufacturer of the device
     * @param own Name of the owner of the device
     * @return True if the Smart Object is added successfully, false otherwise (if the addNode method fails)
     * @throws NullPointerException if username, macAddr, man or own are null
     * @throws IllegalArgumentException if the length of macAddr is different than 12, or if man or own are empty strings
     */
    public boolean addSmartObject(String username, long id, String macAddr, String man, String own) {
        if(username == null || macAddr == null || man == null || own == null) throw new NullPointerException();
        if(macAddr.length() != 12) throw new IllegalArgumentException("The MAC Address must be made of 12 digits");
        if(man.equals("") || own.equals("")) throw new IllegalArgumentException("Owner and manufacturer cannot be empty strings");
        Node smObNode = new SmartObject(username, id, macAddr, man, own);
        return addNode(smObNode);
    }

    /**
     * Adds a node to the context. A node is added because it has been detected by the application running on the
     * device of the ego. Two edge are thus added (from the ego to the new node and vice versa) to model the new relationship
     * between them.
     * @param newNode The new node to be added
     * @return True if the node is added successfully, false if a node with the same username exists or the new node's username
     *         corresponds to ego's username
     * @throws NullPointerException if newNode is null
     */
    public boolean addNode(Node newNode)
    {
        if(newNode == null) throw new NullPointerException();
        String nodeName = newNode.getUsername();
        if(nodes.containsKey(nodeName) || nodeName.equals(egoName)) return false;
        nodes.put(nodeName,newNode);
        //when a node is added, the subsequent relation to the ego is created (in-going and out-going edges)
        addEdge(egoName, nodeName);
        addEdge(nodeName, egoName);
        return true;
    }

    /**
     * Removes a node from the context's social graph
     * @param nodeName The username of the node that has to be removed
     * @return True if successfully removed, false if the node doesn't exist in the context
     * @throws NullPointerException If nodeName is null
     * @throws IllegalArgumentException If nodeName is equal to egoName (it is impossible to remove the ego node)
     */
    public boolean removeNode(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        if(nodeName.equals(egoName)) throw new IllegalArgumentException();
        Node node = nodes.remove(nodeName);
        if(node == null)
            return false;
        Set<String> fromEdges = node.getInEdges().keySet();
        Set<String> toEdges = node.getOutEdges().keySet();
        
        fromEdges.forEach((fromNode) -> {
            nodes.get(fromNode).removeOutEdge(nodeName);
        });
        
        toEdges.forEach((toNode) -> {
            nodes.get(toNode).removeInEdge(nodeName);
        });
        return true;
    }

    /**
     * Gets the in-going edges of a node
     * @param nodeName The username of a node
     * @return The out-going edges that have the parameter node as their source, enriched by their tie strength
     *         or null if the nodeName doesn't exist in the context
     * @throws NullPointerException If nodeName is null
     */
    public HashMap<String,Double> getInEdges(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return null;
        HashMap<String,Double> res = new HashMap<>();
        HashMap<String,Edge> tmp = node.getInEdges();
        Set<String> keys = tmp.keySet();
        keys.forEach((key) -> {
            res.put(key, tmp.get(key).getTieStrength());
        });     
        return res;
    }

    /**
     * Gets the out-going edges of a node
     * @param nodeName The username of a node
     * @return The in-going edges that have the parameter node as their destination, enriched by their tie strength
     *         or null if the nodeName doesn't exist in the context
     * @throws NullPointerException If nodeName is null
     */
    public HashMap<String,Double> getOutEdges(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node==null)
            return null;
        HashMap<String,Double> res = new HashMap<>();
        HashMap<String,Edge> tmp = node.getOutEdges();
        Set<String> keys = tmp.keySet();
        keys.forEach((key) -> {
            res.put(key, tmp.get(key).getTieStrength());
        });     
        return res;
    }

    /**
     * Gets the node that corresponds to a username
     * @param nodeName The username of a node
     * @return The data associated to that node, or null if it doesn't exist in the context
     * @throws NullPointerException If nodeName is null
     */
    public String getNodeData(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return null;
        return node.getData();
    }

    /**
     * Associates some String data to a user
     * @param nodeName The username of a node
     * @param data The data to be associated to the user
     * @return True if the operation is successful (the user exists in the context), false otherwise
     * @throws NullPointerException if nodeName or data is null
     */
    public boolean setNodeStringData(String nodeName, String data)
    {
        if(nodeName == null || data == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return false;
        node.setData(data);
        return true;
    }

    /**
     * Associates some Object data to a user
     * @param nodeName The username of a node
     * @param data The data to be associated to the user
     * @return True if the operation is successful (the user exists in the context), false otherwise
     * @throws NullPointerException if nodeName or data is null
     */
    public boolean setNodeObjectData(String nodeName, Object data)
    {
        if(nodeName == null || data == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return false;
        node.setDataObject(data);
        return true;
    }


    /**
     * Sets the class of a user's object data
     * @param nodeName The username of the node
     * @param dataClass The class to be set
     * @return True if the operation is successful (the user exists in the context), false otherwise
     * @throws NullPointerException if nodeName or data is null
     *
     */
    public boolean setNodeDataClass(String nodeName, String dataClass)
    {
        if(nodeName == null || dataClass == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node==null)
            return false;
        node.setDataClass(dataClass);
        return true;
    }

    /**
     * Gets the class of a user's object data
     * @param nodeName The username of a node
     * @return The class of a user's object data, or null if the node doesn't exist
     * @throws NullPointerException if nodeName is null
     */
    public String getNodeDataClass(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return null;
        return node.getDataClass();
    }

    /**
     * Gets the a user's object data
     * @param nodeName The username of a node
     * @return The user's object data, or null if the node doesn't exist
     * @throws NullPointerException if nodeName is null
     */
    public Object getNodeDataObject(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return null;
        return node.getDataObject();
    }

    /**
     * Gets the online status of a node
     * @param nodeName The username of the node
     * @return 0 if the user is online, 1 if it is online, -1 if the node doesn't exist in the context
     * @throws NullPointerException if nodeName is null
     */
    public int getNodeStatus(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        int retVal = -1;
        if(node == null) return retVal;
        retVal = node.getOnlineStatus() ? 1 : 0;
        return retVal;
    }

    /**
     * Updates a user's state to online/offline
     * @param nodeName The username of a node
     * @param value True if the user's state has to be set to online, false otherwise
     * @return 0 if the operation is successful, -1 if the user doesn't exist in the context
     * @throws NullPointerException if nodeName is null
     */
    public int setOnline(String nodeName, boolean value)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return -1;
        if(value == true) node.setOnline();
        else node.setOffline();
        return 0;
    }

    /**
     * Gets the score of a user (number of times online divided life span of the node)
     * @param nodeName The username of a node
     * @return -1 if the user doesn't exist in the context, the score of the user in the context otherwise
     * @throws NullPointerException if nodeName is null
     */
    @JsonIgnore
    public double getScore(String nodeName)
    {
        if(nodeName == null) throw new NullPointerException();
        Node node = nodes.get(nodeName);
        if(node == null)
            return -1;
        return node.getScore();
    }

    /**
     * Removes an edge of the context if its tie strength is smaller than a threshold. If the source or the destination of the
     *        edge was the ego, the alter node is also removed from the context.
     * @param src The username of the source node of the edge
     * @param dst The username of the destination node of the edge
     * @param threshold The value that is compared to the tie strength
     * @throws NullPointerException if src or dst is null, if there are no nodes having src/dst as their identifier,
     *                              or if there isn't an edge between the source and the destination node
     * @throws IllegalArgumentException if threshold is less than 0
     */
    public void removeWeakEdge(String src, String dst, double threshold) {
        if(src == null || dst == null) throw new NullPointerException("Source or destination string parameters are null");
        if(threshold < 0) throw new IllegalArgumentException("A less than 0 threshold is not valid");
        Node srcNode = nodes.get(src);
        Node dstNode = nodes.get(dst);
        if(srcNode == null || dstNode == null) throw new NullPointerException("Source or destination nodes don't exist");
        Edge edge = srcNode.getOutEdges().get(dst); /*edge that goes from srcNode to destNode, I look for it in the toEdges*/
        if(edge == null) throw new NullPointerException("The edge between src and dst doesn't exist");

        /*there is an edge between src and dst in this context, the tie strength is compared to the threshold*/
        double tieStrength = edge.getTieStrength();
        if(tieStrength < threshold) removeEdge(src, dst);
    }

    /**
     * Removes all the edges of the context whose tie strength is smaller than a threshold.
     * @param threshold The value that is compared to the tie strength
     * @throws IllegalArgumentException if threshold is less than 0
     */
    public void removeWeakEdges(double threshold) {
        if(threshold < 0) throw new NullPointerException("A less than 0 threshold is not valid");
        for(String srcKey : nodes.keySet()) {
            Node n = nodes.get(srcKey);
            for(Edge e : n.getOutEdges().values()) {
                String dstKey = e.getDst().getUsername();
                removeWeakEdge(srcKey, dstKey, threshold);
            }
        }
    }

    /**
     * Gets a node by its id
     * @param id The identifier to be looked for
     * @return The node which has such id, or null if no node has that id
     */
    public Node findNodeById(Integer id) {
        for(Node n: nodes.values()) {
            if(n.getId() == id) return n;
        }
        return null;
    }
}