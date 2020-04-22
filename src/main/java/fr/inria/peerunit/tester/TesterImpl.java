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

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.base.SingleResult;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.TesterRegistration;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.util.TesterUtil;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Almeida
 * @author Jeremy Masson
 * @author sunye
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.remote.peerUnit.Tester
 * @see fr.inria.peerunit.VolatileTester
 * @see fr.inria.peerunit.remote.StorageTester
 * @see fr.inria.peerunit.remote.Coordinator
 * @see java.util.concurrent.BlockingQueue<Object>
 */
public class TesterImpl extends AbstractTester implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(TesterImpl.class.getName());
    /**
     * The tester remote interface, RMI implementation
     */
    private final RemoteTesterImpl remoteTester;
    private Coordinator coord;
    private TestCaseWrapper testCase;
    private Class<?> testCaseClass;
    private List<MethodDescription> remainingMethods =
            new ArrayList<MethodDescription>(20);
    /**
     * Thread used to invoke @TestStep methods
     */
    private Thread invocationThread;
    /**
     * Thread used to run this tester.
     */
    private Thread testerThread;
    /**
     * Thread used to quit this tester.
     */
    private Thread quitThread;

    /**
     * Used to give the identifier of the tester.
     *
     * @param klass the coordinator which give the tester's identifier.
     * @throws RemoteException
     */
    public TesterImpl(Coordinator boot, GlobalVariables gv) throws RemoteException {
        super(gv);
        remoteTester = new RemoteTesterImpl ();
        coord = boot;
        int i = coord.register(remoteTester);
        this.setId(i);
        remoteTester.setId(i);
        testCase = new TestCaseWrapper(this);
        this.initializeLogger();
    }

    public TesterImpl(Coordinator boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        this(boot, gv);
        defaults = tu;
    }

    public TesterImpl(GlobalVariables gv, int i, TesterUtil tu) {
        this(gv, i, tu, new RemoteTesterImpl());
    }

    public TesterImpl(GlobalVariables gv, int i, TesterUtil tu, RemoteTesterImpl remote) {
        super(gv);
        remoteTester = remote;
        defaults = tu;
        this.setId(i);
        remoteTester.setId(i);
        testCase = new TestCaseWrapper(this);
    }

    /**
     * Starts the thread for this tester
     */
    public void startThread() {
        LOG.entering("TesterImpl", "startThread()");
        testerThread = new Thread(new TesterThread());
        testerThread.start();

        quitThread = new Thread(new QuitThread());
        quitThread.start();
    }


    public void execute() throws InterruptedException, RemoteException {
        LOG.entering("TesterImpl", "execute()");

        // 1 - Get Coordinator;
        coord = remoteTester.takeCoordinator();
        LOG.finest("Got a coordinator");
        assert coord != null : "Null coordinator";

        // 2 - Wait for start message;
        remoteTester.waitForStart();
        LOG.finest("Start message received");

        // 3 - Parse test case class (need id);
        remainingMethods.addAll(testCase.register(testCaseClass));
        LOG.log(Level.FINEST, "Tester will register {0} methods.",
                remainingMethods.size());

        // 4 - Register my test steps.
        coord.registerMethods(new TesterRegistration(remoteTester, remainingMethods));
        // 5 - Execute all test steps.
        this.testCaseExecution();
        // 6 - Leave;
        LOG.fine("Waiting for invokation thread");

        invocationThread.join();

    }

    private void testCaseExecution() {
        LOG.entering("TesterImpl", "testCaseExecution()");

        try {
            while (true) {
                MethodDescription md = null;
                md = remoteTester.takeMethodDescription();
                runInvocationThread(md);
                remainingMethods.remove(md);
            }
        } catch (InterruptedException e) {
            LOG.exiting("TesterImpl", "testCaseExecution()");
        }
    }

    private void runInvocationThread(MethodDescription md)
            throws InterruptedException {

        invocationThread = new Thread(new Invoke(md));
        invocationThread.start();
        if (md.getTimeout() > 0) {
            invocationThread.join(md.getTimeout());
            if (invocationThread.isAlive()) {
                invocationThread.interrupt();
            }
        }
    }

    /**
     * Creates the peer and the test testCase.
     * Sends the actions to be executed to the testCase.
     *
     * @param klass the Test Case Class.
     * @throws RemoteException
     * @throws SecurityException
     */
    public void registerTestCase(Class<?> klass) {
        LOG.entering("TesterImpl", "registerTestCase(CLass)");
        testCaseClass = klass;
    }

    /**
     *  Used to signal the finish of an method execution. If the method is
     *  the last action of the test case, the execution of this test case
     *  is interrupted.
     *  @param methodAnnotation the method which was executed
     */
    private void executionFinished(ResultSet r) {
        assert coord != null : "Null coordinator";

        try {
            coord.methodExecutionFinished(r);
//            if (testCase.isLastMethod()) {
//                LOG.log(Level.FINEST, "Test Case finished");
//                quit();
//            }
        } catch (RemoteException e) {
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }

        }
    }

    /**
     *  Used to interrupt actions's execution. 
     *  Cleans the action list and asks coordinator to quit.
     */
    public void quit() throws RemoteException {
        LOG.entering("TesterImpl", "quit()");
        assert coord != null : "Null coordinator";

        coord.quit(remoteTester);
        this.cleanUp();

        LOG.exiting("TesterImpl", "quit()");
    }

    /**
     *  Used to invoke an action
     * @param md the action will be invoked
     */
    private synchronized void invoke(MethodDescription md) {
        assert testCase != null : "Null executor";

        SingleResult result = new SingleResult(id, md);
        try {
            result.start();
            testCase.invoke(md);
            if (Thread.interrupted()) {
                result.addInconclusive(null);
                LOG.finest("Thread was interrupted.");
            }
        } catch (InconclusiveFailure e) {
            LOG.log(Level.WARNING, "InconclusiveFailure: " + e.getMessage(), e);
            result.addInconclusive(e);
        } catch (TestException e) {
            LOG.log(Level.WARNING, "TestException: " + e.getMessage(), e);
            result.addFailure(e);
        } catch (AssertionError e) {
            LOG.log(Level.WARNING, "AssertionError: " + e.getMessage(), e);
            result.addFailure(e);
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "InterruptedException: " + e.getMessage(), e);
            result.addInconclusive(e);
        } catch (ClosedByInterruptException e) {
            LOG.log(Level.WARNING, "ClosedByInterruptException: " + e.getMessage(), e);
            result.addInconclusive(null);
        } catch (Throwable e) {
            StringWriter writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            pw.flush();
            writer.flush();
            LOG.log(Level.WARNING, writer.toString());
            result.addError(e);
        } finally {
            result.stop();
        }

        LOG.log(Level.FINEST, "Tester [{0}] Executed {1}", new Object[]{id, md});
        this.executionFinished(result.asResultSet());
    }

    public void cleanUp() {
        LOG.fine("Tester cleaning up.");
        globals = null;
        coord = null;
    }

    /**
     *
     * @return The Tester remote implementation.
     */
    public Tester getRemoteTester() {
        return remoteTester;
    }

    public void join() throws InterruptedException {
        testerThread.join();
        quitThread.join();
    }

    /**
     * @author Eduardo Almeida.
     * @version 1.0
     * @since 1.0
     * @see java.lang.Runnable
     */
    private class Invoke implements Runnable {

        MethodDescription md;

        public Invoke(MethodDescription md) {
            this.md = md;
        }

        public void run() {
            invoke(md);
        }
    }

    private class TesterThread implements Runnable {

        public void run() {
            try {
                LOG.entering("TesterThread", "run()");
                execute();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "TesterThread interrupted exception", ex);
            } catch (RemoteException ex) {
                LOG.log(Level.SEVERE, "TesterThread remote exception", ex);
            }
        }
    }

    private class QuitThread implements Runnable {

        public void run() {
            // Waits for the quit() signal and then for the
            // current invocation thread.
            // Then, interrupts the tester Thread.

            LOG.entering("QuitThread", "run()");
            try {
                remoteTester.waitForQuit();
                LOG.fine("QuitThread -- quit message received;");
                invocationThread.join();
                if (testerThread.isAlive()) {
                    testerThread.interrupt();
                }
                quit();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "QuitThread interrupted exception", ex);
            } catch(RemoteException re) {
                LOG.log(Level.SEVERE, "QuitThread remote exception", re);
            }finally {
                LOG.exiting("QuitThread", "run()");
            }
        }
    }
}
