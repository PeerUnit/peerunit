/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.tester;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Coordinator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class RemoteTesterImplTest {

    private Coordinator coordinator;
    private RemoteTesterImpl remoteTester;

    public RemoteTesterImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        coordinator = mock(Coordinator.class);
        remoteTester = new RemoteTesterImpl();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setCoordinator method, of class RemoteTesterImpl.
     */
    @Test
    public void testSetCoordinator() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    remoteTester.takeCoordinator();
                } catch (InterruptedException ex) {
                    fail("Thread interrupted");
                }
            }
        };
        t.start();
        t.join(1000);
        assertTrue(t.isAlive());
        remoteTester.setCoordinator(coordinator);
        assertTrue(coordinator == remoteTester.takeCoordinator());
        t.join(500);
        assertFalse(t.isAlive());
    }


    /**
     * Test of start method, of class RemoteTesterImpl.
     */
    @Test
    public void testStart() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    remoteTester.waitForStart();
                } catch (InterruptedException ex) {
                    fail("Thread interrupted");
                }
            }
        };
        t.start();
        t.join(1000);
        assertTrue(t.isAlive());
        remoteTester.start();
        t.join(500);
        assertFalse(t.isAlive());
    }


    /**
     * Test of execute method, of class RemoteTesterImpl.
     */
    //@Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        MethodDescription m = null;
        RemoteTesterImpl instance = new RemoteTesterImpl();
        instance.execute(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getId method, of class RemoteTesterImpl.
     */
    //@Test
    public void testGetId() throws Exception {
        System.out.println("getId");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        int expResult = 0;
        int result = instance.getId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of take method, of class RemoteTesterImpl.
     */
    //@Test
    public void testTake() throws Exception {
        System.out.println("take");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        MethodDescription expResult = null;
        MethodDescription result = instance.takeMethodDescription();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of takeCoordinator method, of class RemoteTesterImpl.
     */
    //@Test
    public void testTakeCoordinator() throws Exception {
        System.out.println("takeCoordinator");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        Coordinator expResult = null;
        Coordinator result = instance.takeCoordinator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of waitForStart method, of class RemoteTesterImpl.
     */
    //@Test
    public void testWaitForStart() throws Exception {
        System.out.println("waitForStart");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        instance.waitForStart();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of kill method, of class RemoteTesterImpl.
     */
    //@Test
    public void testKill() throws Exception {
        System.out.println("kill");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        instance.kill();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of put method, of class RemoteTesterImpl.
     */
    //@Test
    public void testPut() throws Exception {
        System.out.println("put");
        Integer key = null;
        Object object = null;
        RemoteTesterImpl instance = new RemoteTesterImpl();
        instance.put(key, object);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCollection method, of class RemoteTesterImpl.
     */
    //@Test
    public void testGetCollection() throws Exception {
        System.out.println("getCollection");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        Map expResult = null;
        Map result = instance.getCollection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class RemoteTesterImpl.
     */
    //@Test
    public void testGet() throws Exception {
        System.out.println("get");
        Integer key = null;
        RemoteTesterImpl instance = new RemoteTesterImpl();
        Object expResult = null;
        Object result = instance.get(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of containsKey method, of class RemoteTesterImpl.
     */
    //@Test
    public void testContainsKey() throws Exception {
        System.out.println("containsKey");
        Integer key = null;
        RemoteTesterImpl instance = new RemoteTesterImpl();
        boolean expResult = false;
        boolean result = instance.containsKey(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class RemoteTesterImpl.
     */
    @Test
    public void testClear() throws Exception {
        System.out.println("clear");
        RemoteTesterImpl instance = new RemoteTesterImpl();
        //instance.clear();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

}