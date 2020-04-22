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
package fr.inria.peerunit.bootstrapper;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author Eduardo Almeida, Veronique PELLEAU
 * @version 1.0
 * @since 1.0
 */
public class BootstrapperImpl implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(BootstrapperImpl.class.getName());
    private TreeStrategy context;
    private TesterUtil defaults;
    private final int expectedTesters;
    private final RemoteBootstrapperImpl remoteBootstrapper;
    /**
     * Distributed Testers registered to this bootstrapper
     */
    final private List<DistributedTester> registeredTesters;

    public BootstrapperImpl(TesterUtil tu) {
        defaults = tu;
        remoteBootstrapper = new RemoteBootstrapperImpl();
        expectedTesters = defaults.getExpectedTesters();
        registeredTesters = Collections.synchronizedList(
                new ArrayList<DistributedTester>(expectedTesters));

        if (defaults.getCoordinationType() == 1) {
            LOG.fine("Using the HTree strategy");
            context = new ConcreteBtreeStrategy(defaults);
        } else {
            LOG.fine("Using the Grid strategy");
            context = new GridStrategy(defaults);
        }
    }

    public void run() {
        LOG.entering("BootstrapperImpl", "run()");
        LOG.info("Starting Bootstrapper");

        try {

            this.waitForTesterRegistration();
            context.buildTree();
            context.setCommunication();
            context.startRoot();
            remoteBootstrapper.waitForTesterTermination();
            context.cleanUp();
            LOG.info("[Bootstrapper] Finished !");
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, "Remote exception", ex);
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, "Wait interrupted", ex);
        }
    }

    /**
     * Returns the current number of registered nodes
     * @return the current number of registered nodes
     */
    public int getRegistered() {
        LOG.entering("BootstrapperImpl", "getRegistered()");
        return context.getRegistered();
    }

    /**
     * Waits for all expected testers to register.
     */
    private void waitForTesterRegistration() throws InterruptedException {
        LOG.entering("RemoteBoostrapperImpl", "waitForTesterRegistration()");
        LOG.fine("Waiting for registration. Expecting " + expectedTesters + " testers.");

        DistributedTester dt;

        while (registeredTesters.size() < expectedTesters) {
            dt = remoteBootstrapper.takeTester();
            registeredTesters.add(dt);
            context.register(dt);
            LOG.finest("Total tester registrations: " + registeredTesters.size());
        }
        LOG.exiting("RemoteBoostrapperImpl", "waitForTesterRegistration()");
    }

    public Bootstrapper getRemoteBootstrapper() {
        return remoteBootstrapper;
    }
}

