package eu.h2020.helios_social.core.contextualegonetwork;

/**
 * This class implements a generic interaction between two entities in a context. We assume that interactions
 * are not atomic, but rather events with a start time (modelled with a UNIX timestamp) and a duration, expressed in seconds.
 * This class is thought to be extended in order to model more specific events occurring between two entities,
 * e.g. messages, video-calls, commands (sent by a human entity to a smart object),...
 * 
 * @author Emmanouil Krasanakis (maniospas@iti.gr)
 * @author Barbara Guidi (guidi@di.unipi.it)
 * @author Andrea Michienzi (andrea.michienzi@di.unipi.it)
 */
public final class Interaction {
    /**
     * When the interaction started
     */
    private long startTimestamp;
    /**
     * The number of seconds that elapse between the start time and the end time of the interaction
     */
    private long duration;
    /**
     * The interaction's data
     */
    private Object data;
    /**
     * The interaction's parent edge
     */
    private Edge edge;

    /**
     * Constructor method
     * @param timestamp The start timestamp
     * @param duration The duration of the interaction
     * @throws IllegalArgumentException if timestamp or duration are less than 0
     */
    protected Interaction(Edge edge, long timestamp, long duration, Object data) {
        if(timestamp < 0 || duration < 0) throw new IllegalArgumentException();
        this.startTimestamp = timestamp;
        this.duration = duration;
        this.data = data;
        this.edge = edge;
    }

    /**
     * Used in deserialization
     */
    protected Interaction()
    {}

    /**
     * Retrieves the edge the interaction belongs to.
     * @return The edge.
     */
    public Edge getEdge() {
    	return edge;
    }

    /**
     * Retrieves the timestamp that orresponds to the start of the interaction.
     * @return The timestamp as a long.
     */
    public long getStartTime() {
        return startTimestamp;
    }
    
    /**
     * Retrieves the timestamp that corresponds to the end of the interaction.
     * @return The timestamp as a long.
     */
    public long getEndTime() {
        return startTimestamp + duration;
    }

    /**
     * Retrieves the duration of the interaction.
     * @return The duration of the interaction
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Retrieves the interaction's data.
     * @return This interaction's data
     */
    public Object getData() {
    	return data;
    }

    /**
     * Retrieves the type of the interaction's data as captured by the name of their class.
     * @return The type of this interaction's data, an empty string if no such data
     */
    public String getType() {
    	if(data==null)
    		return "";
        return data.getClass().toString();
    }
}