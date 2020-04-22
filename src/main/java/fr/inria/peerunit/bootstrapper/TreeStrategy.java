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

import fr.inria.peerunit.remote.DistributedTester;
import java.rmi.RemoteException;

/**
 * The classes that implement a concrete strategy should implement this The
 * context class uses this to call the concrete strategy
 * 
 * @author Veronique Pelleau
 * @author Jeremy Masson
 * @author Aboubakar Koïta
 * 
 */
public interface TreeStrategy {

    void buildTree();

    int getNodesSize();

    void register(DistributedTester t);

    void setCommunication();

    int getRegistered();

    void startRoot() throws RemoteException;

    void cleanUp();
}
