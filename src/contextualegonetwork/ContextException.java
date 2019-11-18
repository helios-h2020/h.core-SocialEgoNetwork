/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contextualegonetwork;

import java.util.NoSuchElementException;

/**
 * This class implements a specific type of exception, related
 * to problems generated from code ran on objects having type Context
 */
public class ContextException extends NoSuchElementException {

    /**
	 * Automatically generated id for serialization
	 */
	private static final long serialVersionUID = 7727355205674669018L;

	/**
     * Creates a new instance of <code>ContextException</code> without detail
     * message.
     */
    public ContextException() {
    }

    /**
     * Constructs an instance of <code>ContextException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ContextException(String msg) {
        super(msg);
    }
}
