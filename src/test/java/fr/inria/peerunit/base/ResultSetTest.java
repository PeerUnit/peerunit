/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import fr.inria.peerunit.common.MethodDescription;

import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class ResultSetTest {

    private ResultSet result;
    private MethodDescription md;

    @Before
    public void before() {
        md = mock(MethodDescription.class);
        result = new ResultSet(md);
    }

    /**
     * Test of add method, of class ResultSet.
     */
    @Test
    public void testAdd_ResultSet() {
        ResultSet other = new ResultSet(md);
        ResultSet instance = new ResultSet(md);
        instance.add(other);

        assertTrue(instance.size() == 0);
    }

    /**
     * Test of add method, of class ResultSet.
     */
    @Test
    public void testAdd_SingleResult() {
        SingleResult sr1 = new SingleResult(1, md);
        SingleResult sr2 = new SingleResult(1, md);

        sr1.addError(new Exception());
        sr2.addFailure(null);

        result.add(sr1);
        result.add(sr2);

        assertTrue(result.getErrors() == 1);
        assertTrue(result.getFailures() == 1);
        assertTrue(result.getInconclusives() == 0);
        assertTrue(result.getPass() == 0);
        assertTrue(result.size() == 2);

        SingleResult sr3 = new SingleResult(1, md);
        sr3.addInconclusive(new Exception());

        result.add(sr3);
        assertTrue(result.getInconclusives() == 1);

        SingleResult sr4 = new SingleResult(1, md);

        result.add(sr4);
        assertTrue(result.getPass() == 1);

    }

    /**
     * Test of toString method, of class ResultSet.
     */
    @Test
    public void testToString() {
        when(md.getName()).thenReturn("name");
        SingleResult sr = new SingleResult(1, md);
        ResultSet rs = sr.asResultSet();

        String expected = String.format("Level: %d. Pass: %d. Fails: %d. Errors: %d. " +
                "Inconclusive: %d. Time elapsed: %d msec. Average: %d msec. \t Method: %s",
                0, 1, 0, 0, 0, 0, 0, "name");

        assertEquals(rs.toString(), expected);
    }

    /**
     * Test of the start(), stop() and getDelay() methods
     * of class SingleResult.
     */
    @Test
    public void testStartStop() {
        long start = System.currentTimeMillis();
        result.start();
        try {
            Thread.sleep((long) Math.random() * 1000);
        } catch (InterruptedException ex) {
            fail("Thread interrupted");
        }
        result.stop();
        long stop = System.currentTimeMillis();
        assertTrue(result.getDelay() >= 0);
        assertTrue(stop-start >= result.getDelay());
    }

    /**
     * Test of getMethodDescription method, of class ResultSet.
     */
    @Test
    public void testGetMethodDescription() {
        System.out.println("getMethodDescription");

    }

    /**
     * Test of size method, of class ResultSet.
     */
    @Test
    public void testSize() {
        System.out.println("size");

    }

    /**
     * Test of getErrors method, of class ResultSet.
     */
    @Test
    public void testGetErrors() {
        System.out.println("getErrors");
    }

    /**
     * Test of getfailures method, of class ResultSet.
     */
    @Test
    public void testGetFailures() {
        System.out.println("getFailures");

    }

    /**
     * Test of getInconclusives method, of class ResultSet.
     */
    @Test
    public void testGetInconclusives() {
        System.out.println("getInconclusives");

    }

}