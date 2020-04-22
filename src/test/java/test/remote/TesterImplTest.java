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
package test.remote;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.base.Sample;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.tester.TestCaseWrapper;
import fr.inria.peerunit.tester.TesterImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import java.util.logging.FileHandler;

public class TesterImplTest {

    private static TestCaseWrapper wrapper;
    private static CoordinatorImpl coord;
    private static Coordinator remoteCoordinator;
    private static Bootstrapper bootstrapper;
    private static GlobalVariables globals;
    private static TesterImpl tester0, tester1, tester2;

    /**
     * 
     * @throws InterruptedException
     */
    @BeforeClass
    public static void inititalize() throws InterruptedException, IOException {
        Level l = Level.ALL;
        FileHandler handler = new FileHandler("TesterImplTest.log");
        handler.setFormatter(new LogFormat());
        handler.setLevel(l);

        Logger myLogger = Logger.getLogger("fr.inria");
        myLogger.setUseParentHandlers(false);
        myLogger.addHandler(handler);
        myLogger.setLevel(l);


        Properties properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("tester.log.dateformat", "yyyy-MM-dd");
        properties.setProperty("tester.log.timeformat", "HH:mm:ss.SSS");
        properties.setProperty("tester.log.level", "FINEST");
        properties.setProperty("tester.logfolder", "/tmp/");
        properties.setProperty("tester.log.delimiter", "|");
        properties.setProperty("tester.waitForMethod", "500");
        try {
            TesterUtil defaults = new TesterUtil(properties);
            //coord = new CoordinatorImpl(3, 1);
            coord = new CoordinatorImpl(defaults);

            globals = new GlobalVariablesImpl();
            tester0 = new TesterImpl(coord.getRemoteCoordinator(), globals);
            tester0.getRemoteTester().setCoordinator(coord.getRemoteCoordinator());
            //executor = new TestCaseWrapper(tester0);
            tester1 = new TesterImpl(coord.getRemoteCoordinator(), globals);
            tester1.getRemoteTester().setCoordinator(coord.getRemoteCoordinator());
            tester2 = new TesterImpl(coord.getRemoteCoordinator(), globals);
            tester2.getRemoteTester().setCoordinator(coord.getRemoteCoordinator());
            tester0.registerTestCase(Sample.class);
            tester1.registerTestCase(Sample.class);
            tester2.registerTestCase(Sample.class);


            // Start Threads
            System.out.println("Starting tester threads");
            tester0.startThread();
            tester1.startThread();
            tester2.startThread();

            System.out.println("Sending start message to testers");
            tester0.getRemoteTester().start();
            tester1.getRemoteTester().start();
            tester2.getRemoteTester().start();
            //Thread.yield();
            System.out.println("Coordinator is waiting for testers");
            //Thread.yield();
            coord.waitForTesterRegistration();
            System.out.println("Registration is finished");

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testTesterImpl() {
        fail("Not yet implemented");
    }

    //@Test
    public void testRun() {
        fail("Not yet implemented");
    }

    @Test
    public void testRegister() {
        MethodDescription md = new MethodDescription("first", 1, 1, "", "", 10);

        assertEquals(3, coord.getSchedule().size());

        assertTrue(coord.getSchedule().containsMethod(md));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester0.getRemoteTester()));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester1.getRemoteTester()));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester2.getRemoteTester()));

    }

    @Test
    public void testGetId() {
        assertEquals(0, tester0.getId());
        assertEquals(1, tester1.getId());
        assertEquals(2, tester2.getId());
    }

    @Test
    public void mysets() {
        Set<TesterImpl> myset = new HashSet<TesterImpl>();

        assertTrue(myset.add(tester0));
        assertTrue(!myset.add(tester0));
        assertTrue(!myset.add(tester0));

    }

    /**
     * FIXME
     */
    //@Test
    public void testExecute() {
        try {
            
            TesterImpl tester = new TesterImpl(remoteCoordinator, globals);
            tester.getRemoteTester().setCoordinator(remoteCoordinator);
            tester.registerTestCase(Sample.class);

            System.setProperty("executed", "nok");
            List<MethodDescription> methods = wrapper.register(Sample.class);
            tester.startThread();

            for (MethodDescription md : methods) {
                tester.getRemoteTester().execute(md);
            }
            Thread.sleep(1200);
            Thread.yield();
            tester.quit();
            assertEquals("ok", System.getProperty("executed"));
        } catch (RemoteException e) {
            fail("RemoteException");
        } catch (InterruptedException e) {
            fail("InterruptedException");
        }

    }

    //@Test
    public void testKill() {
        fail("Not yet implemented");
    }

    //@Test
    public void testPut() {
        try {
            tester0.put(0, "zero");
            tester1.put(1, "one");
            tester2.put(2, "two");
            assertEquals("zero", tester2.get(0));
            assertEquals("one", tester0.get(1));
            assertEquals("two", tester1.get(2));
        } catch (RemoteException ex) {
            fail("Communication error");
        }
    }

    //@Test
    public void testClear() {
        fail("Not yet implemented");
    }

    //@Test
    public void testGet() {
        fail("Not yet implemented");
    }

    //@Test
    public void testGetCollection() {
        fail("Not yet implemented");
    }

    //@Test
    public void testContainsKey() {

        fail("Not yet implemented");
    }
}
