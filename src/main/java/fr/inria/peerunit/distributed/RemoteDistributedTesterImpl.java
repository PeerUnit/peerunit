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

import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteDistributedTesterImpl implements DistributedTester, Serializable {

    private static Logger LOG =
            Logger.getLogger(RemoteDistributedTesterImpl.class.getName());

    private int id;

    private List<DistributedTester> children;

    private DistributedTester parent;
    /**
     * The (remote) Coordinator for this tester
     */
    private Coordinator coordinator;
    private String  address;

    /**
     * Number of expected testers for this coordinator.
     * Only used for distributed testers.
     * TODO: refactoring needed.
     */
    //private int expectedTesters = 0;
    /**
     * Locks for blocking multi-threaded methods.
     */
    private final Lock lock = new ReentrantLock();
    private final Condition mayStart = lock.newCondition();
    private final Condition hasCoordinator = lock.newCondition();

    private transient AtomicBoolean start = new AtomicBoolean(false);

    public RemoteDistributedTesterImpl(TesterUtil tu) {
        this.children = new ArrayList<DistributedTester>(tu.getTreeOrder());
        try {
            address = InetAddress.getLocalHost().getHostAddress() ;
        } catch (UnknownHostException ex) {
            LOG.log(Level.SEVERE, "UnknownHost", ex);
        }

    }

    /**
     *
     */
    public void setCoordinator(Coordinator coord) throws RemoteException {
        LOG.entering("RemoteDistributedTesterImpl", "setCoordinator");
        lock.lock();
        try {
            this.coordinator = coord;
            hasCoordinator.signal();
        } finally {
            lock.unlock();
        }
        LOG.exiting("RemoteDistributedTesterImpl", "setCoordinator");
    }

    /**
     * Returns the coordinator for this tester.
     * Blocks until the coordinator is available.
     *
     * @return The Coordinator for this tester.
     * @throws InterruptedException
     */
    public Coordinator takeCoordinator() throws InterruptedException {
        lock.lock();
        try {
            while (coordinator == null) {
                hasCoordinator.await();
            }
        } finally {
            lock.unlock();
        }

        return coordinator;
    }
    
    /**
     * Tester interface.
     * @param coord
     * @throws RemoteException
     */
    public void registerTesters(List<DistributedTester> testers) throws RemoteException {
        LOG.finest("Registering "+testers.size() + " testers.");
        //expectedTesters = testers.size();
        this.children.addAll(testers);
    }

    /**
     *
     * @return The local IP address of this tester.
     * @throws RemoteException
     */
    public String getAddress() throws RemoteException {
        return address;
    }

    @Override
    public String toString() {
        return String.format("Distributed Tester [%d]", id);
    }

    void setId(int i) {
        id = i;
    }

    public int getId() throws RemoteException {
        return id;
    }

    List<DistributedTester> getChildren() {
        return this.children;
    }

    int id() {
        return id;
    }
    
    DistributedTester getParent() {
        return this.parent;
    }

    public void setParent(DistributedTester dt) throws RemoteException {
        this.parent = dt;
    }

    public void start() throws RemoteException {
        lock.lock();
        try {
            start.set(true);
            mayStart.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Waits for the arrival of a start message.
     * Blocks the current thread until the start attribute becomes true;
     *
     * @throws InterruptedException
     */
    void waitForStart() throws InterruptedException {
        lock.lock();
        try {
            while (!start.get()) {
                mayStart.await();
            }
        } finally {
            lock.unlock();
        }
    }
}
