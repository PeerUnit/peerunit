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
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.Tester;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteCoordinatorImpl implements Coordinator, Serializable {

    private static final Logger LOG = Logger.getLogger(RemoteCoordinatorImpl.class.getName());
    /**
     * Stores arriving registrations.
     */
    private final BlockingQueue<TesterRegistration> registrations;
    /**
     * Stores method execution results.
     */
    private final BlockingQueue<ResultSet> results;
    /**
     * Stores testers that are leaving the system.
     */
    private final BlockingQueue<Tester> leaving;
    /**
     * Number of registered testers.
     */
    private final AtomicInteger registeredTesters = new AtomicInteger(0);
    /**
     * Counter used to increment tester identifications.
     */
    private final AtomicInteger idCounter = new AtomicInteger(0);
    /**
     * Number of expected testers.
     */
    private final int expectedTesters;

    /**
     *
     * @param expectedTesters Expected number of testers.
     */
    public RemoteCoordinatorImpl(int i) {
        expectedTesters = i;
        registrations = new ArrayBlockingQueue<TesterRegistration>(expectedTesters);
        results = new ArrayBlockingQueue<ResultSet>(expectedTesters);
        leaving = new ArrayBlockingQueue<Tester>(expectedTesters);
    }

    /**
     * Test case registration.
     *
     * @param t
     * @param coll
     * @throws RemoteException
     */
//    public void registerMethods(Tester t, Collection<MethodDescription> coll)
//            throws RemoteException {
//        LOG.finest(String.format("%s registering tester %s", this, t));
//        registrations.offer(new TesterRegistration(t, coll));
//
//
//    }

    public void registerMethods(TesterRegistration tr)
            throws RemoteException {

        registrations.offer(tr);
    }
    /**
     * Method execution end. Sent by testers after the execution of a
     * method (TestStep).
     * @param result
     * @throws RemoteException
     */
    public void methodExecutionFinished(ResultSet result) throws RemoteException {
        results.offer(result);
    }

    /**
     * Informs the coordinator that a tester is leaving the system.
     * 
     * @param t The tester.
     * @throws RemoteException
     */
    public void quit(Tester t) throws RemoteException {
        leaving.offer(t);
    }

    /**
     * Bootstrapper method.
     * Registers a tester.
     * @param t
     * @return An int, the ID for the registering tester.
     * @throws RemoteException
     */
    public int register(Tester t) throws RemoteException {
        LOG.entering("CoordinatorIml", "register(Tester)");
        int id = idCounter.getAndIncrement();
        return id;
    }

    /**
     * Forces the coordinator to quit.
     * @throws RemoteException
     */
    public void quit() throws RemoteException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        return String.format("Coordinator for %d testers.",
                this.idCounter.get());
    }

    /**
     * Pending registrations.
     *
     * @return
     */
    public BlockingQueue<TesterRegistration> registrations() {
        return registrations;
    }

    /**
     * Pending test results.
     * @return
     */
    public BlockingQueue<ResultSet> results() {
        return results;
    }

    /**
     * Testers leaving the system.
     * @return
     */
    public BlockingQueue<Tester> leaving() {
        return leaving;
    }
}
