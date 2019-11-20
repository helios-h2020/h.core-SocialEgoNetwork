package contextualegonetwork;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import contextualegonetwork.Edge;
import contextualegonetwork.Node;
import contextualegonetwork.contextData.ContextData;

/**
 * This class implements a context of the Contextual Ego Network. The context stores all the information related
 * to the nodes, i.e. the actors (humans or smart objects) that engage in relationships inside it, and keeps track of its spatial and temporal
 * characteristics. The context can be in three possible states: it can be the current online context, it can be one
 * of the active contexts (saved in local memory), or it can be non-active (and thus serialized and stored on disk).
 */
public final class Context
{
    /**
     * The contextual ego network the context is part of
     */
    private ContextualEgoNetwork contextualEgoNetwork;
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
    private int tempDayOfWeek;
    /**
     * Hour of the day the context has been loaded
     */
    private int tempHour;
    /**
     * Object data carried by the context
     */
    private ContextData data;
    /**
     * Graph nodes (i.e. alters) and edges
     */
    private ArrayList<Node> nodes;
    private ArrayList<Edge> edges;
    private ArrayList<Edge> phantomEdges;

    /**
     * Constructor method
     * @param name the name of the context
     * @param contextualEgoNetwork the ContextualEgoNetwork within which the context resides
     * @throws NullPointerException if name, contextualEgoNetwork or data are null
     * @throws IllegalArgumentException if name is an empty string
     */
    Context(ContextualEgoNetwork contextualEgoNetwork, ContextData data)
    {
        if(data == null || contextualEgoNetwork == null || data==null) Utils.error(new NullPointerException());
        this.contextualEgoNetwork = contextualEgoNetwork;
        this.data = data;
        nodes = new ArrayList<Node>();
        edges = new ArrayList<Edge>();
        phantomEdges = new ArrayList<Edge>();
        nodes.add(contextualEgoNetwork.getEgo());
        timeCounter = new long[7][24];
        registerTimeOfLoad();
        contextualEgoNetwork.getSerializer().registerId(this);
    }
    
    /**
     * If the context is loaded, it is serialized to a file
     * @return Whether the context was saved.
     */
    public boolean save() {
    	if(nodes==null) 
    		return false;
    	for(Node node : nodes) 
        	contextualEgoNetwork.getSerializer().save(node);
    	return contextualEgoNetwork.getSerializer().save(this);
    }
    
    /**
     * Removes any local files used to store the context (but retains the context in memory)
     * These files will be re-created once the context is saved again.
     */
    public void removeFromStorage() {
    	contextualEgoNetwork.getSerializer().removeFromStorage(this);
    }
    
    /**
     * Saves the context to a fileRemoves stored context and removes it from memory 
     * @see #save()
     */
    public void cleanup() {
    	save();
    	contextualEgoNetwork.getSerializer().unregister(this);
    	registerTimeOfUnload();
    	nodes = null;
    	edges = null;
    	phantomEdges = null;
    	timeCounter = null;
    }
    
    /**
     * Loads the context from its default given file
     */
    public void load() {
    	contextualEgoNetwork.getSerializer().reload(this, 1);
    	registerTimeOfLoad();
    }
    
    /**
     * Is used to assert that the context is loaded, producing an error if it's not
     * @return Whether the context is loaded
     * @see #isLoaded()
     */
    protected boolean assertLoaded() {
    	if(!isLoaded())
    		load();//Utils.error("Context needs be loaded before use", false);
    	return true;
    }
    
    /**
     * @return Whether the context has been loaded in memory
     */
    public boolean isLoaded() { 
    	return nodes!=null;
    }

    /**
     * Used in deserialization
     */
    protected Context()
    {}

    /**
     * @return The data attached to the context
     */
    public ContextData getData()
    {
    	assertLoaded();
        return data;
    }

    /**
     * Saves the activation hour of the context
     */
    protected void registerTimeOfLoad()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        this.tempDayOfWeek = c.get(Calendar.DAY_OF_WEEK)-1;
        this.tempHour = c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Updates the recurrencies array with the days of the week and hours of the days the context has been active.
     */
    protected void registerTimeOfUnload()
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
     * @return A shallow copy of the context's node list
     */
    public ArrayList<Node> getNodes() {
    	return new ArrayList<Node>(nodes);
    }
    
    /**
     * @return A shallow copy of the context's non-phantom edge list
     */
    public ArrayList<Edge> getEdges() {
    	return getEdges(true);
    }
    
    /**
     * @param real Whether the edges is real or a phantom one.
     * @return A shallow copy of the context's edge list
     */
    public ArrayList<Edge> getEdges(boolean real) {
    	if(real)
    		return new ArrayList<Edge>(edges);
    	else
    		return new ArrayList<Edge>(phantomEdges);
    }
    
    public Edge addEdge(Node src, Node dst)
    {
       return addEdge(src, dst, true);
    }

    /**
     * Adds an edge between two nodes of the social graph
     * @param src The source node
     * @param dst The destination node
     * @return The generated edge
     * @throws NullPointerException If src or dst are null
     * @throws IllegalArgumentException If src and dst are the same node or if they don't belong to the context
     */
    public Edge addEdge(Node src, Node dst, boolean isReal)
    {
        if(src == null || dst == null) return Utils.error(new NullPointerException(), null);
        if(src==dst) return Utils.error(new IllegalArgumentException("Src and dest cannot be the same node"), null);
        if(!nodes.contains(src) || !nodes.contains(dst)) return Utils.error(new IllegalArgumentException("Either source or destination nodes are not in context"), null);
        for(Edge edge : edges)
        	if(edge.getSrc()==src && edge.getDst()==dst)
        		return Utils.error(new IllegalArgumentException("Edge already exists in context"), edge);
        Edge edge = new Edge(src, dst, this);
        if(isReal)
        	edges.add(edge);
        else
        	phantomEdges.add(edge);
        return edge;
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
    public Edge getEdge(Node src, Node dst) {
        if(src == null || dst == null) return Utils.error(new NullPointerException(), null);
        if(src==dst) return Utils.error(new IllegalArgumentException("Src and dest cannot be the same node"), null);
    	for(Edge edge : edges)
        	if(edge.getSrc()==src && edge.getDst()==dst)
        		return edge;
    	return Utils.error("Edge does not exist", null);
    }

    /**
     * Removes an edge between a source and the destination node, and if either the source or the destination node is the ego
     *        the alter node is removed from the social graph too (because the condition for which a node belongs to the ego network, that is only if
     *        it is connected to the ego with an edge, isn't satisfied anymore)
     * @param src The source node
     * @param dst The destination node
     * @return The edge removed
     * @throws NullPointerException If src and dst are null
     * @throws IllegalArgumentException If src and dst are the same node
     */
    public Edge removeEdge(Node src, Node dst)
    {
    	Edge edge = getEdge(src, dst);
    	edges.remove(edge);
    	return edge;
    }

    /**
     * Adds a node to the context. A node is added because it has been detected by the application running on the
     * device of the ego. Two edge are thus added (from the ego to the new node and vice versa) to model the new relationship
     * between them.
     * @param newNode The new node to be added
     * @throws NullPointerException if newNode is null
     */
    public void addNode(Node node)
    {
        if(node == null) Utils.error(new NullPointerException());
        if(nodes.contains(node)) Utils.error("Node already in context");
        nodes.add(node);
        contextualEgoNetwork.getSerializer().registerId(node);
    }

    /**
     * Removes a node and its edges from the context
     * @param node The node to be removed
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is the ego of not part of the context
     */
    public void removeNode(Node node) {
        if(node == null) Utils.error(new NullPointerException());
        if(node == contextualEgoNetwork.getEgo()) Utils.error(new IllegalArgumentException());
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        edges.removeIf(edge -> edge.getSrc()==node);
        edges.removeIf(edge -> edge.getDst()==node);
        nodes.remove(node);
    }

    /**
     * Gets the in-going edges of a node
     * @param nodeName The given node
     * @return A stream of the in-going edges
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is not in the context
     */
    public Stream<Edge> getInEdges(Node node)
    {
        if(node == null) throw new NullPointerException();
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        return edges.stream().filter(edge -> edge.getDst()==node);
    }
    
    /**
     * Gets the out-going edges of a node
     * @param nodeName The given node
     * @return A stream of the out-going edges
     * @throws NullPointerException If the node is null
     * @throws IllegalArgumentException If the node is not in the context
     */
    public Stream<Edge> getOutEdges(Node node)
    {
        if(node == null) throw new NullPointerException();
        if(!nodes.contains(node)) Utils.error(new IllegalArgumentException());
        return edges.stream().filter(edge -> edge.getSrc()==node);
    }

    /**
     * Removes all edges from the context if the tie strength is less than a threshold.
     * @param threshold The value that is compared to the tie strength
     */
    public void removeWeakEdges(double threshold) {
    	edges.removeIf(edge -> edge.getTieStrength()<threshold);
    }
}