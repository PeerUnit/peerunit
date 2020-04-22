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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.bootstrapper.BootstrapperImpl;
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 * 
 */
public class CoordinatorRunner {

    private static final Logger LOG = Logger.getLogger(CoordinatorRunner.class.getName());

    private static final GlobalVariablesImpl globals = new GlobalVariablesImpl();

    private TesterUtil defaults;
    private static Registry registry;

    /**
     * @param defaults
     */
    public CoordinatorRunner(TesterUtil tu) {
        defaults = tu;
        try {
            this.initializeRegistry();
            this.initializeLogger();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param defaults
     * @throws IOException
     */
    private void initializeLogger() throws IOException {
        PeerUnitLogger.createLogger(defaults, "coordination.log");
    }

    private void initializeRegistry() throws RemoteException {
        try {
            registry = LocateRegistry.createRegistry(defaults.getRegistryPort());
            LOG.fine("Registry created in port: " + defaults.getRegistryPort());
        } catch (RemoteException e) {
            registry = LocateRegistry.getRegistry(defaults.getRegistryPort());
            LOG.fine("Registry found in port: " + defaults.getRegistryPort());
        }
        assert registry != null;
    }

    private void start() throws RemoteException, AlreadyBoundException,
            InterruptedException, NotBoundException {

        LOG.entering("CoordinatorRunner", "start()");
        this.bindGlobals();

        if (defaults.getCoordinationType() == 0) {
            this.startCoordinator();
        } else {
            this.startBootstrapper();
        }

        this.cleanAndUnbind();
        //Thread.sleep(3000);
        System.exit(0);
    }

    public void startCoordinator() throws RemoteException, AlreadyBoundException, InterruptedException {
        LOG.info("Using the centralized architecture");
        CoordinatorImpl cii = new CoordinatorImpl(defaults);
        Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(cii.getRemoteCoordinator(), 0);
        LOG.info("New Coordinator address is : " + defaults.getServerAddr());
        registry.bind("Coordinator", stub);

        Thread coordination = new Thread(cii, "Coordinator");
        coordination.start();
        coordination.join();
        LOG.info("Coordination thread finished");
    }

    public void startBootstrapper() throws RemoteException, AlreadyBoundException, InterruptedException {
        LOG.info("Using the distributed architecture");
        BootstrapperImpl bootstrapper = new BootstrapperImpl(defaults);

        Bootstrapper bootStub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper.getRemoteBootstrapper(), 0);
        registry.bind("Bootstrapper", bootStub);
        Thread boot = new Thread(bootstrapper, "Bootstrapper");
        boot.start();
        boot.join();
        LOG.info("Bootstrap thread finished");
    }

    /**
     *  Bind the remote object's stub in the registry
     *
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    public void bindGlobals() throws RemoteException, AlreadyBoundException {
        assert registry != null;

        GlobalVariables globalsStub = (GlobalVariables) UnicastRemoteObject.exportObject(globals);
        registry.bind("Globals", globalsStub);
    }

    public void cleanAndUnbind() throws RemoteException {
        try {
            registry.unbind("Bootstrapper");
            registry.unbind("Coordinator");
            registry.unbind("Globals");
        } catch (NotBoundException e) {
            // Do nothing
        } 
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TesterUtil defaults;

        try {
            String filename;
            if (args.length == 1) {
                filename = args[0];
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else if (new File("peerunit.properties").exists()) {
                filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else {
                defaults = TesterUtil.instance;
            }
            CoordinatorRunner cr = new CoordinatorRunner(defaults);
            cr.start();

        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (AlreadyBoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open properties file");
        } finally {
            System.exit(1);
        }

    }
}
