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
package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.inria.peerunit.remote.GlobalVariables;

/**
 * 
 * @author Veronique Pelleau
 * @author sunye
 * @version 1.0
 * @since 1.0
 */
public class GlobalVariablesImpl implements GlobalVariables {

        private static final long serialVersionUID = 2L;


	/**
	 * Caching global variables
	 */
	private Map<Integer, Object> cacheMap = new ConcurrentHashMap<Integer, Object>();
		
	/**
	 * Clears all global variables for this object
	 * @throws java.rmi.RemoteException
	 */
	public void clearCollection() throws RemoteException {
		cacheMap.clear();
	}

	/**
	 * Returns true if this bootstrapper contains the specified variable.
	 * @param key Value whose presence is to be tested
	 * @return true if this bootstrapper contains the specified variable.
	 * @throws java.rmi.RemoteException
	 */
	public boolean containsKey(Integer key) throws RemoteException {
		return cacheMap.containsKey(key);
	}

	/**
	 * Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key) throws RemoteException {
		return cacheMap.get(key);
	}

	/**
	 * Used to retrieve all the variables of the testing global variables
	 * @return A map containing all registered variables for this Bootstrapper
	 * @throws RemoteException
	 */
	public Map<Integer, Object> getCollection() throws RemoteException {
		return cacheMap;
	}

	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key, Object object) throws RemoteException {
		cacheMap.put(key, object);
	}

}
