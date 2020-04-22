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
package test.btree;

import fr.inria.peerunit.base.Data;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import fr.inria.peerunit.bootstrapper.BootstrapperImpl;
import fr.inria.peerunit.distributed.DistributedTesterImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImplTest {

    private final static int REGISTRY_PORT = 1100;

    private BootstrapperImpl bootstrapper;
    private Properties properties;
    private TesterUtil defaults;

    @Before
    public void setup() {
        properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("test.treeStrategy", "1");
        defaults = new TesterUtil(properties);
        bootstrapper = new BootstrapperImpl(defaults);
    }

    // @Test
    public void testBootstrapperIml() {
        /*
         * properties.setProperty("tester.peers", "1"); TesterUtil defaults =
         * new TesterUtil(properties); fr.inria.peerunit.Bootstrapper b = new
         * BootstrapperImpl(defaults); Node node = mock(Node.class); try {
         * b.register(node); } catch (RemoteException e) { fail(e.getMessage());
         * }
         *
         * assertTrue(b != null);
         */
    }
    //FIXME
//    @Test
//    public void testRegister() {
//        int id = 0;
//        try {
//            for (int i = 0; i < 5; i++) {
//                DistributedTester tester = mock(DistributedTester.class);
//                id = bootstrapper.register(tester);
//                assertTrue(id == i);
//            }
//
//        } catch (RemoteException e) {
//            fail(e.getMessage());
//        }
//    }

    @Test
    public void testRemoteRegister() throws Exception {
        Registry registry;
        registry = LocateRegistry.createRegistry(REGISTRY_PORT);
        Bootstrapper stub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper.getRemoteBootstrapper(), 0);
        registry.bind("BootTest", stub);
        Bootstrapper remoteBoot = (Bootstrapper) registry.lookup("BootTest");
        DistributedTesterImpl tester = new DistributedTesterImpl(Data.class, remoteBoot, null, defaults);
        tester.startThread();
        assertTrue(tester.getId() == 0);
    }

    @Test
    public void testGetRegistered() {
        // fail("Not yet implemented");
    }

    @Test
    public void testIsRoot() {
        // fail("Not yet implemented");
    }
}
