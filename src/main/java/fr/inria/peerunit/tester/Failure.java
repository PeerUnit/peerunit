/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.tester;


/**
 *
 * @author sunye
 */
public class Failure extends TestException {

    /**
     * Creates a new instance of <code>Failure</code> without detail message.
     */
    public Failure() {
    }


    /**
     * Constructs an instance of <code>Failure</code> with the specified detail message.
     * @param msg the detail message.
     */
    public Failure(String msg) {
        super(msg);
    }
}
