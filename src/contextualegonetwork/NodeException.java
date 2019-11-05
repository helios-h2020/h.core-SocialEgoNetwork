/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextualegonetwork;

/**
 * This class implements a specific type of exception, related
 * to problems generated from code ran on objects of having type Node
 */
public class NodeException extends RuntimeException {

    /**
     * Creates a new instance of <code>NodeException</code> without detail
     * message.
     */
    public NodeException() {
    }

    /**
     * Constructs an instance of <code>NodeException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public NodeException(String msg) {
        super(msg);
    }
}
