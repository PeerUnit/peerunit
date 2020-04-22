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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eduardo Almeida.
 * @author sunye
 * @version 1.0
 * @since 1.0
 * @see java.lang.Runnable
 * @see java.lang.Thread
 */
public class Timeout implements Runnable {

    private static Logger LOG = Logger.getLogger(Timeout.class.getName());

    private long timeout;

    private Thread thread;

    public Timeout(Thread t, long millis) {
        timeout = millis;
        thread = t;
    }

    /**
     * measure the life time of an thread
     *
     * @param t the tester which will be registered
     * @param list the MethodDescription list
     * @see fr.inria.peerunit.remote.Coordinator#register(fr.inria.peerunit.Tester,fr.inria.peerunit.parser.MethodDescription)
     * @throws InterruptedException
     */
    public void run() {
        LOG.finest("Entering Timeout thread");
        try {
            thread.join(timeout);
            if (thread.isAlive()) {
                LOG.finest("I will interrupt execution thread");
                thread.interrupt();
            }
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "TimeoutThread Interrupted Exception", e);
        }
    }

}
