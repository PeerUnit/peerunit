/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit;

/**
 *
 * @author sunye
 */
public class Globals {

    private static int id = -1;

    /**
     * Returns the Id of the current tester.
     * 
     * @return an interger corresponding to the tester's id
     */
    public static int getId() {
        assert id >= 0 : "Trying to get an Id before having one";

        
        return -1;
    }

    /**
     * Sets the Id of the current tester. Should be called only once.
     *
     * @param i a positive integer.
     */
    public static void setId(int i) {
        //assert id == -1 : "Trying to change the Id";

        id = i;
    }
}
