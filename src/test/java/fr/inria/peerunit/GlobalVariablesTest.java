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
package fr.inria.peerunit;

import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.util.TesterUtil;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class GlobalVariablesTest {

    private GlobalVariables globals;
    private static CoordinatorRunner runner;
    private Registry registry;

    public GlobalVariablesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        runner = new CoordinatorRunner(TesterUtil.instance);
        runner.bindGlobals();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        runner.cleanAndUnbind();
    }

    @Before
    public void setUp() throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry(TesterUtil.instance.getRegistryPort());
        globals = (GlobalVariables) registry.lookup("Globals");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of put method, of class GlobalVariables.
     */
    @Test
    public void testPut() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of get method, of class GlobalVariables.
     */
    @Test
    public void testGet() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of getCollection method, of class GlobalVariables.
     */
    @Test
    public void testGetCollection() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of containsKey method, of class GlobalVariables.
     */
    @Test
    public void testContainsKey() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of clearCollection method, of class GlobalVariables.
     */
    @Test
    public void testClearCollection() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }
}
