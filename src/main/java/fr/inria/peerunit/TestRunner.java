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
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.distributed.DistributedTesterImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.tester.TesterImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketImplFactory;
import performance.MonitoringSocketFactory;
import performance.SocketMonitoringSystem;
import stats.StatsManager;

/**
 * A <i>test</i> on a peer is launched by a instance of the <tt>TestRunner</tt> class. Its role consists
 * to launch the right implementation of <i>Tester</i> interface, i.e centralized or distributed (the type
 * is set in the framework property file ), passing it as argument the <tt>Class</tt> instance corresponding
 * to the <i>test case</i> to perform.
 * 
 * @author sunye
 * @author Aboubakar Koita
 * @version 1.0
 * @since 1.0
 */
public class TestRunner {

    /**
     * The test case that will be excuted and those name was
     * passed at the command line.
     */
    private Class<?> testcase;
    private TesterUtil defaults;
    private Registry registry;
    private static final Logger LOG = Logger.getLogger(TestRunner.class.getName());

    /**
     * Launch the right implementation of <i>tester</i> passing it as argument the <tt>Class</tt>
     * instance corresponding to the <i>test case</i> to execute.
     *
     * @param klass the<tt>Class</tt> instance corresponding to the <i>test case</i> to execute
     */
    public TestRunner(Class<?> klass, TesterUtil tu) {

        defaults = tu;
        testcase = klass;

        Bootstrapper boot = null;
        Coordinator coordinator = null;

        registry = null;
        int times = 0;
        boolean centralized = true;

        this.initializeLogger();
        try {
            registry = LocateRegistry.getRegistry(defaults.getServerAddr(),
                    defaults.getRegistryPort());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        /**
        try {
        registry = LocateRegistry.getRegistry(defaults.getRegistryPort());
        } catch (RemoteException ex) {
        ex.printStackTrace();
        }*/
        while (times < 5 && boot == null) {
            try {
                boot = (Bootstrapper) registry.lookup("Bootstrapper");
                centralized = false;

            } catch (NotBoundException ex) {
                try {
                    coordinator = (Coordinator) registry.lookup("Coordinator");
                } catch (Exception e) {
                }
            } catch (AccessException ex) {
            } catch (RemoteException ex) {
            }
            times++;
            if (boot == null) {
                try {
                    Thread.sleep(300 * times);
                } catch (InterruptedException e) {
                }
            }

        }

        if (boot == null && coordinator == null) {
            LOG.severe("Unable to bind server: " + defaults.getServerAddr()
                    + " at port " + defaults.getRegistryPort());
            System.exit(1);
        }

        try {
            GlobalVariables globals = (GlobalVariables) registry.lookup("Globals");
            if (centralized) {
                LOG.fine("Coordinator found, using the centralized architecture.");
                TesterImpl tester = new TesterImpl(coordinator, globals, defaults);
                Tester remoteTester = tester.getRemoteTester();
                UnicastRemoteObject.exportObject(remoteTester);
                remoteTester.setCoordinator(coordinator);

                tester.registerTestCase(testcase);
                tester.startThread();
                remoteTester.start();
                tester.join();
                LOG.fine("Tester thread joined");
                UnicastRemoteObject.unexportObject(remoteTester, true);
                System.exit(0);

            } else {
                LOG.fine("Bootstrapper found, using the distributed architecture.");
                DistributedTesterImpl tester = new DistributedTesterImpl(testcase, boot, globals, defaults);
                DistributedTester remoteTester = tester.getRemoteDistributedTester();
                UnicastRemoteObject.exportObject(remoteTester);
                tester.startThread();
                tester.join();
                LOG.fine("DistributedTester thread joined");
                UnicastRemoteObject.unexportObject(remoteTester, true);
                System.exit(0);
            }
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
            e.printStackTrace(System.err);
        }

    }

    private void initializeLogger() {
        try {
            Level level = defaults.getLogLevel();
            Formatter formatter = new LogFormat();
            Logger.getLogger("fr.inria").setLevel(level);
            Logger.getLogger("").getHandlers()[0].setLevel(level);
            Logger.getLogger("").getHandlers()[0].setFormatter(formatter);
            
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * In the main method, we get the only argument corresponding to class name of
     * <i>test case</i> to perform. We load the <tt>Class</tt> corresponding and we
     * create a <code>TestRunner</tt> instance.
     *
     * @param args The only argument should be a class name of <i>test case</i> to execute
     */
    public static void main(String[] args) {
        TesterUtil defaults;
        String filename;

        /*
        try {
            SocketImplFactory socketImplFactory = new MonitoringSocketFactory();
            Socket.setSocketImplFactory(socketImplFactory);
            //ServerSocket.setSocketFactory(socketImplFactory);
            SocketMonitoringSystem.initForDelegator();
            //SocketMonitoringSystem.getInstance().add(StatsManager.getSocketStats());
        } catch (Exception e) {
        }
        */



        if (args.length < 1) {
            System.out.println("Usage: java TestRunner TestCaseClass [Properties File]");
        } else {
            String name = args[0];
            try {
                Class<?> klass = Class.forName(name);

                if (args.length > 1) {
                    filename = args[1];
                    FileInputStream fs = new FileInputStream(filename);
                    defaults = new TesterUtil(fs);
                } else if (new File("peerunit.properties").exists()) {
                    filename = "peerunit.properties";
                    FileInputStream fs = new FileInputStream(filename);
                    defaults = new TesterUtil(fs);
                } else {
                    defaults = TesterUtil.instance;
                }

                new TestRunner(klass, defaults);

            } catch (FileNotFoundException e) {
                System.err.println("Error: Unable to open properties file");
                System.exit(1);
            } catch (ClassCastException e) {
                System.out.println("Error: Class " + name + " does not implement TestCase interface.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error: Class " + name + " not found.");
            }
        }
    }
}
