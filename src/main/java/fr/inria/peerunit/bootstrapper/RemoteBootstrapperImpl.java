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

import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.DistributedTester;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteBootstrapperImpl implements Bootstrapper {

    private static final Logger LOG = Logger.getLogger(RemoteBootstrapperImpl.class.getName());

    private final BlockingQueue<DistributedTester> registrations =
            new LinkedBlockingQueue<DistributedTester>();

    private final AtomicInteger idCounter = new AtomicInteger(0);



    private final AtomicBoolean shouldILeave = new AtomicBoolean(false);

    public int register(DistributedTester t) throws RemoteException {
        LOG.entering("RemoteBoostrapperImpl", "register()");
        registrations.offer(t);
        return idCounter.getAndIncrement();
    }

    public void quit() throws RemoteException {
        LOG.entering("RemoteBoostrapperImpl", "quit()");
        synchronized (shouldILeave) {
            shouldILeave.set(true);
            shouldILeave.notifyAll();
        }
        LOG.exiting("RemoteBoostrapperImpl", "quit()");
    }


    public void waitForTesterTermination() throws InterruptedException {
        LOG.entering("RemoteBoostrapperImpl", "waitForTesterTermination()");
        while (!shouldILeave.get()) {
            synchronized (shouldILeave) {
                shouldILeave.wait();
            }
        }
        LOG.exiting("RemoteBoostrapperImpl", "waitForTesterTermination()");
    }

    public DistributedTester takeTester() throws InterruptedException {
        return registrations.take();
    }

}
