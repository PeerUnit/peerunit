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
package fr.inria.peerunit.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import fr.inria.peerunit.common.MethodDescription;

/**
 * This interface represent a <i>tester/i>. A <i>tester</i> is a component who control a peer 
 * to test and executes on him the <i>actions</i> of a <i>test case</i>.
 * @author Eduardo Almeida
 * @author Aboubakar Koita
 * @version 1.0
 * @since 1.0 
 * @see fr.inria.peerunit.tester.TesterImpl
 */
public interface Tester extends Remote {

    /**
     * Sets the coordinator for this tester.
     *
     * @param coord
     */
    public void setCoordinator(Coordinator coord) throws RemoteException;

    /**
     * Execute a <i>test case action</i> thanks to it description.
     *
     * @param m is a instance of <tt>MathodDescription</tt> class containing 
     * all informations allowing the correct execution of the <i>test case
     * action</i> that it describes.
     * 
     * @throws RemoteException because the method is distant
     */
    public void execute(MethodDescription m) throws RemoteException;

    /**
     * Return the <i>tester's</i> id.
     *
     * @throws RemoteException
     */
    public int getId() throws RemoteException;

    /**
     * Stop the <i>tester</i>.
     */
    public void kill() throws RemoteException;

    /**
     * Starts the tester.
     *
     * @throws RemoteException
     */
    public void start() throws RemoteException;

    /**
     * Asks the tester to leave the system.
     * 
     * @throws RemoteException
     */
    public void quit() throws RemoteException;

}
