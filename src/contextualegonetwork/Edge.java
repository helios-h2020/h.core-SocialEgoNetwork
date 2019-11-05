package contextualegonetwork;

import java.awt.*;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

/**
 * This class implements an edge of the Social Graph. An edge, in a context, can link the ego of the Contextual Ego Network to one of the alters or two of the alters.
 * Each edge is enriched by temporal information, particularly the one related to the creation timestamp, and with social information, that is the
 * interactions that take place between the source and the destination nodes in the life span of the edge (this information is stored on the source and destination nodes).
 * Moreover, the edge has a weight, which is its tie strenght, i.e. the number of interactions on the edge divided by the number of seconds passed since the creation date.
 */
@JsonIdentityInfo(generator=JSOGGenerator.class)
class Edge {

    /**
     * Numerical identifier of the edge
     */
    private int id;
    /**
     * UNIX timestamp of the creation time of the edge
     */
    private long timeCreated;
    /**
     * Source node
     */
    private Node src;
    /**
     * Destination node
     */
    private Node dst;

    /**
     * Constructor method
     * @param src The source node of the edge
     * @param dst The destination node of the edge
     * @param identifier Id of the edge
     * @throws NullPointerException if src or dst are null
     */
    public Edge(Node src, Node dst, int identifier)
    {
        if(src == null || dst == null) throw new NullPointerException();
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        this.timeCreated = timeStamp.getTime();
        this.id = identifier;
        this.src = src;
        this.dst = dst;
    }

    /**
     * Used in deserialization
     */
    @JsonCreator
    public Edge()
    {}

    /**
     * @return The source node of the edge
     */
    public Node getSrc()
    {
        return src;
    }

    /**
     * @return The destination node of the edge
     */
    public Node getDst()
    {
        return dst;
    }

    /**
     * @return The identifier of the node
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * @return The timestamp of the edge
     */
    public long getCreationTime()
    {
        return this.timeCreated;
    }

    /**
     * Adds a new interaction on this edge
     * @param timestamp The start timestamp of the interaction
     * @param duration The duration of the interaction
     * @param type The type of the interaction
     */
    public void addInteraction(long timestamp, int duration, String type) {
        if(type == null) throw new NullPointerException();
        if(timestamp < 0 || duration < 0) throw new IllegalArgumentException("Timestamp and duration cannot be negative");
        if(type.equals("")) throw new IllegalArgumentException("Type cannot be empty");
        src.addEdgeInteraction(id, timestamp, duration, type);
        dst.addEdgeInteraction(id, timestamp, duration, type);
    }

    /**
     * @return The weight of the edge. i.e. the tie strength between the source and the destination
     *          (the number of interactions that have taken place on this edge divided by the life span of the edge)
     */
    public double getTieStrength() {
        Timestamp t = new Timestamp(System.currentTimeMillis());
        long now = t.getTime();
        boolean divZ = false;
        double tieStrength = 0;
        //To prevent the application from computing the timestamp right after the edge has been created (denominator would be 0)
        while (!divZ) {
            try{
                tieStrength = (double) (src.getInteractions(id).size()) / (double) (now - this.timeCreated);
                //System.out.println(tieStrength);
            }
            catch(ArithmeticException e) {

            }
            divZ = true;
        }
        return tieStrength;
    }
    
}
