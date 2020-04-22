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

import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.common.MethodDescription;


import fr.inria.peerunit.remote.Tester;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteTesterImpl implements Tester, Serializable {
    private static Logger LOG = Logger.getLogger(RemoteTesterImpl.class.getName());

    private static final long serialVersionUID = 1L;
    /**
     * The tester id
     */
    private int id = -1;
    /**
     * The (remote) Coordinator for this tester
     */
    private transient Coordinator coordinator;
    /**
     * The next TestStep to be executed.
     */
    private transient MethodDescription action;
    private transient AtomicBoolean start = new AtomicBoolean(false);

    /**
     * Quit status. When set to true, makes the Tester leave the system.
     */
    private transient AtomicBoolean quit = new AtomicBoolean(false);
    /**
     * Locks for blocking multi-threaded methods.
     */
    private final Lock lock = new ReentrantLock();
    private final Condition newAction = lock.newCondition();
    private final Condition hasCoordinator = lock.newCondition();
    private final Condition mayStart = lock.newCondition();
    private final Condition mayQuit = lock.newCondition();

    /**
     * 
     */
    public void setCoordinator(Coordinator coord) throws RemoteException {
        lock.lock();
        try {
            this.coordinator = coord;
            hasCoordinator.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 
     */
    public void execute(MethodDescription m) throws RemoteException {
        LOG.entering("RemoteTesterImpl", "execute()");

        action = m;
        lock.lock();
        try {
            action = m;
            newAction.signal();
        } finally {
            lock.unlock();
            LOG.exiting("RemoteTesterImpl", "execute()");
        }
    }

    /**
     * 
     */
    public int getId() throws RemoteException {
        assert id != -1 : "Id not set";

        return id;
    }

    /**
     * 
     */
    public void quit() throws RemoteException {
        lock.lock();
        try {
            quit.set(true);
            mayQuit.signal();
        } finally {
            lock.unlock();
        } 
    }
    
    /**
     * @see 
     */
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
     * Returns the next TestStep to execute.
     * Blocks until a new TestStep is available.
     * 
     * @return The description of the TestStep to be executed
     * @throws InterruptedException
     */
    public MethodDescription takeMethodDescription() throws InterruptedException {
        MethodDescription md;
        lock.lock();
        try {
            while (action == null) {
                newAction.await();
            }
            md = action;
            action = null;
        } finally {
            lock.unlock();
        }

        return md;
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
     * Waits for the arrival of a start message.
     * Blocks the current thread until the start attribute becomes true;
     * 
     * @throws InterruptedException
     */
    public void waitForStart() throws InterruptedException {
        lock.lock();
        try {
            while (!start.get()) {
                mayStart.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true if the current tester should quit
     * and false for any other case.
     * 
     * @return teh quit status.
     */
    public void waitForQuit() throws InterruptedException {
        lock.lock();
        try {
            while (!quit.get()) {
                mayQuit.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void setId(int i) {
        id = i;
    }

    @Override
    public String toString() {
        return String.format("RemoteTesterImpl %d", id);
    }

    public void kill() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void put(Integer key, Object object) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Integer, Object> getCollection() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object get(Integer key) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsKey(Integer key) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
