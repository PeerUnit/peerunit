/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.inria.peerunit.common.MethodDescription;

/**
 *
 * @author sunye
 * @author jeugenio
 */
public class MethodDescriptionTest {

    private MethodDescription m1;
    private MethodDescription m2;
    private MethodDescription m3;

    @Before
    public void setUp() {
        m1 = new MethodDescription("setup", 1, 1, "", "", 10);
        m2 = new MethodDescription("calculate", 2, 1, "", "", 20);
        m3 = new MethodDescription("terdown", 3, 1, "", "", 30);
    }


    /**
     * Test of compareTo method, of class MethodDescription.
     */
    @Test
    public void testCompareTo() {
        assertTrue(m1.compareTo(m2) == -1);
        assertTrue(m2.compareTo(m3) == -1);
        assertTrue(m3.compareTo(m1) == 1);
        assertTrue(m2.compareTo(m2) == 0);
    }

    /**
     * Test of equals method, of class MethodDescription.
     */
    @Test
    public void testEquals() {
        MethodDescription clone = new MethodDescription("setup", 1, 1, "", "", 10);
        assertEquals(m1, m1);
        assertEquals(m1, clone);
        assertFalse(m1.equals(m2));
    }

    /**
     * Test of getName method, of class MethodDescription.
     */
    @Test
    public void testGetName() {
       assertEquals(m1.getName(), "setup");
    }

    /**
     * Test of getTimeout method, of class MethodDescription.
     */
    @Test
    public void testGetTimeout() {
        assertTrue(m1.getTimeout() == 10);
    }

}