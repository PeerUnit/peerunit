/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.Verdicts;

/**
 *
 * @author sunye
 * @author jeugenio
 */
public class SingleResultTest {
    private MethodDescription md = new MethodDescription("n", 1, 1, "", "", 0);

    private SingleResult result = new SingleResult(1, md);

    public SingleResultTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
     * Test of addError method, of class SingleResult.
     */
    @Test
    public void testAddError() {
        result.addError(new Exception("Exception"));
        assertTrue(result.getVerdict() == Verdicts.ERROR);
    }

    /**
     * Test of addFailure method, of class SingleResult.
     */
    @Test
    public void testAddFailure() {
        result.addFailure(new AssertionError("Error"));
        assertTrue(result.getVerdict() == Verdicts.FAIL);
    }

    /**
     * Test of addInconclusive method, of class SingleResult.
     */
    @Test
    public void testAddInconclusive() {
        result.addInconclusive(new Exception("Exception"));
        assertTrue(result.getVerdict() == Verdicts.INCONCLUSIVE);
    }

    /**
     * Test of getMethodDescription method, of class SingleResult.
     */
    @Test
    public void testGetMethodDescription() {
        MethodDescription m = new MethodDescription("name", 1, 1, "", "", 0);
        SingleResult r = new SingleResult(1, m);

        assertEquals(m, r.getMethodDescription());
    }

    /**
     * Test of getTesterId method, of class SingleResult.
     */
    @Test
    public void testGetTesterId() {
        SingleResult r = new SingleResult(42, md);

        assertTrue(42 == r.getTesterId());
    }


    /**
     * Test of asResultSet method, of class SingleResult.
     */
    @Test
    public void testAsResultSet() {
        result.addError(new Exception());

        ResultSet rs = result.asResultSet();

        assertTrue(rs.getMethodDescription() == md);
        assertTrue(rs.size() == 1);
        assertTrue(rs.getErrors() == 1);
        assertTrue(rs.getInconclusives() == 0);
        assertTrue(rs.getFailures() == 0);
    }

}