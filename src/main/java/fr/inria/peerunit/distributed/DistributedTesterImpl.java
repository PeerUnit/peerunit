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
package fr.inria.peerunit.distributed;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.coordinator.RemoteCoordinatorImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.PeerUnitLogger;
import java.io.IOException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.FileHandler;

/**
 * The DistributedTester is both, a Tester and a Coordinator.
 * As a Tester, it has a Coordinator, it registers a test case and executes 
 * test steps when requested by its Coordinator.
 * 
 * As a coordinator, it accepts the registration of several testers
 * and asks its testers to execute test steps.
 * 
 * @author sunye
 */
public class DistributedTesterImpl { //implements Serializable {
    // - Register with bootstrapper and get an Id
    // 1. Wait for coordinator;
    // 2. Register with coordinator;
    // 3. Wait for tester registration;
    // - IF root, start test case execution
    // - wait all testers to quit
    // - leave and cleanup
    // SD
    // this ----- id = register(this) -----> bootstrapper
    // bootstrapper -----registerTesters(testers) ----------> this
    // this -------- setCoordinator(this) ---------> testers
    // bootstrapper -----start() -----> this[root]

    /**
     *
     */
    private static final long serialVersionUID = 11L;
    private static final Logger LOG = Logger.getLogger(TesterImpl.class.getName());
    /**
     * Set of testers that are coordinated by this tester.
     */
    private transient List<DistributedTester> children;
    /**
     * The bootstrapper, that will help me to find my parent and my children.
     */
    private transient Bootstrapper bootstrapper;
    /**
     * The test case that whill be executed
     */
    private transient Class<?> testCaseClass;
    /**
     * Remote interface for distributed testers.
     * RMI implementation.
     */
    private RemoteDistributedTesterImpl remoteDistributedTester;
    /**
     * Thread for the distributed testers.
     */
    private Thread thread = new Thread(new DistributedTesterThread());
    /**
     * Default profperties.
     */
    private TesterUtil defaults;
    /**
     * GlobalVariables.
     */
    private GlobalVariables globals;

    public DistributedTesterImpl(Class<?> klass, Bootstrapper boot,
            GlobalVariables gv, TesterUtil tu) throws RemoteException {
        defaults = tu;
        bootstrapper = boot;
        testCaseClass = klass;
        remoteDistributedTester = new RemoteDistributedTesterImpl(tu);
        globals = gv;
    }

    public int getId() {
        return remoteDistributedTester.id();
    }

    /**
     *
     * @return The remote distributed tester interface.
     */
    public DistributedTester getRemoteDistributedTester() {
        return remoteDistributedTester;
    }

    public void join() throws InterruptedException {
        this.thread.join();
    }

    private DistributedTester getParent() {
        return remoteDistributedTester.getParent();
    }

   /**
     * Registers this distributed tester with the bootstrapper and
     * receives an id.
     */
    public void startThread() throws RemoteException {
        // registration cannot be done in the constructor because
        // this object must be exported first.
        // The export is done externally.

        int i = bootstrapper.register(remoteDistributedTester);
        remoteDistributedTester.setId(i);

        // Log only can be initialized when the tester
        // receives an ID (the file name depends on it)
        this.initializeLogger();
        LOG.fine("Log initialized for DistributedTester " + getId());
        thread.start();
        LOG.exiting("DistributedTesterImpl", "startThread()");
    }

    /**
     * Starts the distributed tester thread.
     * @throws InterruptedException
     */
    public void start() throws RemoteException, InterruptedException {
        LOG.entering("DistributedTester", "start()");

        // 1 - Build the tree.
        children = remoteDistributedTester.getChildren();
        for (DistributedTester each : children) {
            each.setParent(remoteDistributedTester);
        }

        // 2 - Start children
        for (DistributedTester each : children) {
            each.start();
        }

        // 3 - Start threads
        if (getParent() == null) {
            // This is root
            runRootTester();
        } else if (children.size() > 0) {
            // This is an intermediary node.
            runMiddleTester();
        } else {
            // This is a LeafTester.
            runLeafTester();
        }
        LOG.exiting("DistributedTester", "start()");
    }

    private void cleanUp() {
        LOG.entering("DistributedTesterImpl", "cleanup()");

        children.clear();
        bootstrapper = null;
        globals = null;
        LOG.exiting("DistributedTesterImpl", "cleanup()");
    }

    /**
     * Execution for leaf testers.
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void runLeafTester() throws RemoteException, InterruptedException {
        LOG.entering("DistributedTesterImpl", "runLeafTester()");

        // Local Tester
        TesterImpl localTester = new TesterImpl(globals, this.getId(), defaults);
        localTester.registerTestCase(testCaseClass);
        Tester remoteTester = localTester.getRemoteTester();
        UnicastRemoteObject.exportObject(remoteTester);
        //localTester.startThread();
        
        LOG.finest("Waiting for coordinator");
        Coordinator c = remoteDistributedTester.takeCoordinator();
        LOG.finest("DT got a coordinator and will set local tester");
        remoteTester.setCoordinator(c);
        remoteTester.start();
        //localTester.execute();
        localTester.startThread();
        localTester.join();

        LOG.exiting("DistributedTesterImpl", "runLeafTester()");
    }

    /**
     * Execution for root tester (coordinator)
     * @throws InterruptedException
     * @throws RemoteException
     */
    private void runRootTester() throws InterruptedException, RemoteException {
        LOG.entering("DistributedTesterImpl", "runRootTester()");

        // Coordinator
        CoordinatorImpl coordinator = new CoordinatorImpl(children.size() + 1,
                defaults);
        RemoteCoordinatorImpl remoteCoordinator = coordinator.getRemoteCoordinator();
        UnicastRemoteObject.exportObject(remoteCoordinator);
        for (DistributedTester each : children) {
            each.setCoordinator(remoteCoordinator);
        }

        // Local Tester
        TesterImpl localTester = new TesterImpl(globals, this.getId(), defaults);
        localTester.registerTestCase(testCaseClass);
        Tester remoteTester = localTester.getRemoteTester();
        remoteTester.setCoordinator(remoteCoordinator);
        localTester.startThread();
        remoteTester.start();

        coordinator.run();
        bootstrapper.quit();
        LOG.exiting("DistributedTesterImpl", "runRootTester()");
    }

    private void runMiddleTester() throws InterruptedException, RemoteException {
        LOG.entering("DistributedTesterImpl", "runMiddleTester()");

        // Middle Tester
        MiddleTester middle = new MiddleTester(children.size() + 1);
        Coordinator coordinator = middle.getCoordinator();
        Tester tester = middle.getTester();
        UnicastRemoteObject.exportObject(coordinator);
        UnicastRemoteObject.exportObject(tester);
        for (DistributedTester each : children) {
            each.setCoordinator(coordinator);
        }

        // Local Tester
        TesterImpl localTester = new TesterImpl(globals, this.getId(), defaults);
        localTester.registerTestCase(testCaseClass);
        Tester remoteTester = localTester.getRemoteTester();
        remoteTester.setCoordinator(coordinator);
        localTester.startThread();
        remoteTester.start();

        // Set parent tester to MiddleTester
        Coordinator c = remoteDistributedTester.takeCoordinator();
        tester.setCoordinator(c);

        //middle.execute();
        middle.startThread();
        middle.join();
        LOG.exiting("DistributedTesterImpl", "runRootTester()");
    }

    protected void initializeLogger() {

        FileHandler handler;
        try {
            PeerUnitLogger.createLogger(defaults,
                    String.format("Tester%d.log", getId()));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private class DistributedTesterThread implements Runnable {

        public void run() {
            try {
                remoteDistributedTester.waitForStart();
                start();
            } catch (RemoteException ex) {
                Logger.getLogger(DistributedTesterImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(DistributedTesterImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
