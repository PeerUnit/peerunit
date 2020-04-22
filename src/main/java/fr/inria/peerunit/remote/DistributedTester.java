/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author sunye
 */
public interface DistributedTester extends Remote {// extends Tester, Coordinator {

    /**
     * Register several (child) testers.
     * @param testers
     * @throws RemoteException
     */
    void registerTesters(List<DistributedTester> testers)
            throws RemoteException;

    void setParent(DistributedTester parent) throws RemoteException;

    void start() throws RemoteException;

    void setCoordinator(Coordinator c) throws RemoteException;

    int getId() throws RemoteException;
    
    String getAddress() throws RemoteException;
}
