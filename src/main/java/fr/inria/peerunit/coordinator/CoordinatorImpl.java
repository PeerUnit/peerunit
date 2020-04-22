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

import fr.inria.peerunit.base.ResultListenner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.util.TesterUtil;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author sunye
 *
 */
public class CoordinatorImpl implements Runnable {

    private static final Logger LOG = Logger.getLogger(CoordinatorImpl.class.getName());
    private static final long serialVersionUID = 1L;
    /**
     * Schedule: Methods X set of Testers
     */
    private Schedule schedule = new Schedule();
    /**
     * Testers registered to this coordinator
     */
    final private List<Tester> registeredTesters;
    /**
     * Number of expected testers.
     */
    final private AtomicInteger expectedTesters;
    /**
     * Number of testers running the current method (test step).
     */
    final private AtomicInteger runningTesters;
    /**
     * Pool of threads. Used to dispatch actions to testers.
     */
    private ExecutorService executor;
    /**
     * The global verdict for a test case.
     */
    private GlobalVerdict verdict;
    /**
     * The coordinator interface, RMI implementation.
     */
    private RemoteCoordinatorImpl remoteCoordinator;
    /**
     * List of result listenners.
     */
    private List<ResultListenner> listenners = new ArrayList<ResultListenner>();
    /**
     * Strategy for test step execution.
     */
    private CoordinationStrategy strategy;

    /*
     * List of tester for each order.
     */
    private ArrayList<Tester> compareTester = new ArrayList<Tester>();
    /**
     * List of error actions.
     */
    private ArrayList<String> errorActions = new ArrayList();

    /**
     * @param testerNbr Number of expected testers. The Coordinator will wait for
     * the connection of "i" testers before starting to dispatch actions
     * to Testers.
     */
    public CoordinatorImpl(int testerNbr) {
        this(testerNbr, 100, new RemoteCoordinatorImpl(testerNbr));
    }

    /**
     *
     * @param testerNbr Number of expected testers.
     * @param relaxIndex
     */
    public CoordinatorImpl(int testerNbr, TesterUtil tu) {
        this(testerNbr, tu.getRelaxIndex(), new RemoteCoordinatorImpl(testerNbr),
                tu.getCoordinationStrategyClass());
    }

    public CoordinatorImpl(int testerNbr, int relaxIndex, String strategyClass) {
        this(testerNbr, relaxIndex, new RemoteCoordinatorImpl(testerNbr), strategyClass);
    }

    public CoordinatorImpl(int testerNbr, int relaxIndex, RemoteCoordinatorImpl rci) {
        this(testerNbr, relaxIndex, rci, null);
    }

    /**
     *
     * @param testerNbr Number of expected testers.
     * @param relaxIndex
     * @param rci
     */
    private CoordinatorImpl(int testerNbr, int relaxIndex, RemoteCoordinatorImpl rci,
            String strategyClass) {

        LOG.log(Level.FINEST, "Creating a CoordinatorImpl for {0} testers.", testerNbr);


        expectedTesters = new AtomicInteger(testerNbr);
        registeredTesters = Collections.synchronizedList(new ArrayList<Tester>(testerNbr));
        executor = Executors.newFixedThreadPool(testerNbr > 10 ? 10 : testerNbr);
        runningTesters = new AtomicInteger(0);
        verdict = new GlobalVerdict(relaxIndex);
        remoteCoordinator = rci;
        
        LOG.log(Level.FINEST, "Coordination strategy: {0}", strategyClass);
        TesterSet ts = new TesterSetImpl(this);
        try {
            strategy = (CoordinationStrategy) Class.forName(strategyClass).newInstance();
            strategy.init(ts);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error while initializing class: {0}", strategyClass);
            LOG.log(Level.WARNING, null, e);
            strategy = new SequencialStrategy();
            strategy.init(ts);
            LOG.log(Level.WARNING, "Initialized class: {0}", strategy.toString());
        }
    }

    /**
     * 
     * @param tu Defaults.
     */
    public CoordinatorImpl(TesterUtil tu) {
        this(tu.getExpectedTesters(), tu.getRelaxIndex(), tu.getCoordinationStrategyClass());
    }

    /**
     * Main thread;
     */
    public void run() {
        LOG.entering(null, "run()");
        try {
            waitForTesterRegistration();
            testcaseExecution();
            quitAllTesters();
            waitAllTestersToQuit();
            printVerdict();
            cleanUp();
        } catch (InterruptedException ie) {
            LOG.warning(ie.getMessage());
        } finally {
            LOG.exiting(null, "run()");
        }
    }

    /**
     * Calculates and show the global verdict for a test case.
     * @param chrono
     */
    public void printVerdict() {
        verdict.calculateVerdict();
        System.out.println(verdict);
        LOG.fine(verdict.toString());
    }

    /**
     * Dispatches test steps to testers:
     *
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("CoordinatorImpl", "testCaseExecution()");
        LOG.finer(String.format("RegistredMethods: %d", schedule.size()));

        strategy.testcaseExecution();

    }

    /**
     * Dispatches a given action to a given set of testers.
     * Waits (blocks) until all tester have executed the action.
     * @param testers
     * @param md
     * @throws InterruptedException
     */
    public void execute(MethodDescription md) throws InterruptedException {
        //assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "execute(MethodDescription)", md);

        ResultSet result = new ResultSet(md);
        verdict.putResult(md, result);
        result.start();

        Collection<Tester> testers = schedule.testersFor(md);
        // update the answer number if it did not be assigned
        if (md.getAnswers() == -1){
            md.setAnswers(testers.size());
        }
        String message = String.format("Method %s will be executed by %d testers", md, testers.size());
        LOG.fine(message);
        //System.out.println(message);
        runningTesters.set(testers.size());
        for (Tester each : testers) {
            //LOG.finest("Dispatching " + md + " to tester " + each);
            executor.submit(new MethodExecute(each, md));
        }
        waitForExecutionFinished();
        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " msec");
    }

    /**
     * Verifies the correctness of dependent actions, then execute(action)
     * 
     * @param md
     * @param testers
     * @throws InterruptedException
     */
    public void dependencyExecute(MethodDescription md, TesterSet testers) throws InterruptedException {
        boolean error = false;
        ResultSet rs;
        if (!hasDependencyError(md)) {
            testers.execute(md);
            rs = testers.getResult(md);
            if (hasResultError(rs)) {
                errorActions.add(md.getName());
            }
        }
        else {
            setDependencyError(md, testers);
        }
    }

    /**
     * Dispatches the actions of a given hierarchical level(order).
     * Waits until all tester have executed the actions.
     *
     * @param level
     * @throws InterruptedException
     */
    public void hierarchicalExecute(Integer level) throws InterruptedException {
        LOG.entering("CoordinatorImpl", "hierarchicalExecute(Integer)", level);

        Map<MethodDescription, AtomicInteger> actionsXntesters =
                Collections.synchronizedMap(new TreeMap<MethodDescription, AtomicInteger>());

        Map<MethodDescription, ResultSet> actionsXresult =
                Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());

        for (MethodDescription action : schedule.methodsFor(level)) {
            ResultSet result = new ResultSet(action);
            verdict.putResult(action, result);
            result.start();

            Collection<Tester> testers = schedule.testersFor(action);
            // update the answer number if it did not be assigned
            if (action.getAnswers() == -1){
                action.setAnswers(testers.size());
            }
            LOG.log(Level.FINE, "Method {0} will be executed by {1} testers", new Object[]{action, testers.size()});
            runningTesters.set(runningTesters.get() + testers.size());
            for (Tester tester : testers) {
                executor.submit(new MethodExecute(tester, action));
            }
            actionsXntesters.put(action, new AtomicInteger(testers.size()));
            actionsXresult.put(action, result);
        }

        while (runningTesters.intValue() > 0) {
            ResultSet rs = remoteCoordinator.results().take();
            verdict.getResultFor(rs.getMethodDescription()).add(rs);
            runningTesters.decrementAndGet();
            if (actionsXntesters.get(rs.getMethodDescription()).decrementAndGet() == 0) {
                ResultSet result = actionsXresult.get(rs.getMethodDescription());
                result.stop();
                LOG.fine("Method " + result.getMethodDescription() + " executed in "
                        + result.getDelay() + " msec");
            }
        }
    }

    /**
     * Dispatches the actions of a given hierarchical level(order) verifying their dependencies
     * Waits until all tester have executed the actions.
     *
     * @param level
     * @throws InterruptedException
     */
    public void globalExecuteOld(Integer level, TesterSet ts) throws InterruptedException {
        LOG.entering("CoordinatorImpl", "globalExecuteOld(Integer,TesterSet)", level);

        Map<MethodDescription, AtomicInteger> actionsXntesters =
                Collections.synchronizedMap(new TreeMap<MethodDescription, AtomicInteger>());

        Map<MethodDescription, ResultSet> actionsXresult =
                Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());

        for (MethodDescription action : schedule.methodsFor(level)) {
            if (!hasDependencyError(action)) {
                ResultSet result = new ResultSet(action);
                verdict.putResult(action, result);
                result.start();

                Collection<Tester> testers = schedule.testersFor(action);
                // update the answer number if it did not be assigned
                if (action.getAnswers() == -1){
                    action.setAnswers(testers.size());
                }
                LOG.log(Level.FINE, "Method {0} will be executed by {1} testers", new Object[]{action, testers.size()});
                runningTesters.set(runningTesters.get() + testers.size());
                for (Tester tester : testers) {
                    executor.submit(new MethodExecute(tester, action));
                }
                actionsXntesters.put(action, new AtomicInteger(testers.size()));
                actionsXresult.put(action, result);
            } else {
                setDependencyError(action, ts);
            }
        }
        while (runningTesters.intValue() > 0) {
            ResultSet rs = remoteCoordinator.results().take();
            verdict.getResultFor(rs.getMethodDescription()).add(rs);
            runningTesters.decrementAndGet();
            if (actionsXntesters.get(rs.getMethodDescription()).decrementAndGet() == 0) {
                ResultSet result = actionsXresult.get(rs.getMethodDescription());
                result.stop();
                LOG.fine("Method " + result.getMethodDescription() + " executed in "
                        + result.getDelay() + " msec");
                if (hasResultError(result)) {
                    errorActions.add(result.getMethodDescription().getName());
                }
            }
        }
    }

    /**
     * Dispatches the actions of a given hierarchical level(order) verifying their dependencies
     * Waits until all necessary tester have executed the actions.
     *
     * @param level
     * @throws InterruptedException
     */
    public void globalExecute(Integer level, TesterSet ts) throws InterruptedException {
        LOG.entering("CoordinatorImpl", "globalExecute(Integer,TesterSet)", level);

        Map<MethodDescription, AtomicInteger> actionsXntesters =
                Collections.synchronizedMap(new TreeMap<MethodDescription, AtomicInteger>());

        Map<MethodDescription, ResultSet> actionsXresult =
                Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());

        runningTesters.set(0); // the number of required answers (n_r)
        
        for (MethodDescription action : schedule.methodsFor(level)) {
            if (!hasDependencyError(action)) {
                ResultSet result = new ResultSet(action);
                verdict.putResult(action, result);
                result.start();
                
                Collection<Tester> testers = schedule.testersFor(action);
                // update the answer number if it did not be assigned
                if (action.getAnswers() == -1){
                    action.setAnswers(testers.size());
                }
                LOG.log(Level.FINE, "Method {0} will be executed by {1} testers", 
                        new Object[]{action.getName(), action.getAnswers()});
                for (Tester tester : testers) {
                    executor.submit(new MethodExecute(tester, action));
                }
                runningTesters.set(runningTesters.get() + action.getAnswers());
                //actionsXntesters.put(action, new AtomicInteger(testers.size()));
                actionsXresult.put(action, result);
            } else {
                setDependencyError(action, ts);
            }
        }
        while ((runningTesters.intValue() > 0) ) {  // || actionsXresult.get(action).getDelay() 
            ResultSet rs = remoteCoordinator.results().take();
            MethodDescription action;
            action= rs.getMethodDescription();
            if ((action.getAnswers() > 0) 
                    && (action.getAnswers()< schedule.testersFor(action).size())) {
                if (rs.getPass() > 0) {
                    runningTesters.decrementAndGet();
                    action.setAnswers(action.getAnswers()-1);
                    verdict.getResultFor(action).add(rs);
                }
            }
            else {
                runningTesters.decrementAndGet();
                verdict.getResultFor(action).add(rs);
            }
        }
        for (MethodDescription action : schedule.methodsFor(level)) {
            // if the action has a different number of required answers
            if (action.getAnswers() != schedule.testersFor(action).size()) {
                if (action.getAnswers() != 0){
                    verdict.getResultFor(action).addSimulatedError();
                    errorActions.add(action.getName());
                }
            }
            else {
                if (verdict.getResultFor(action).getPass() != action.getAnswers()) {
                    errorActions.add(action.getName());
                }               
            }
            verdict.getResultFor(action).stop();
            LOG.fine("Method " + action.getName() + " executed in "
                        + verdict.getResultFor(action).getDelay() + " msec");
        }
    }

    public void setResult(MethodDescription md, ResultSet rs) throws InterruptedException {
        //assert (status = RUNNING) == RUNNING;
        assert md != null : "Null MethodDescription";

        LOG.entering("CoordinatorImpl", "setResult()", md);

        ResultSet result = rs;
        verdict.putResult(md, result);
        result.start();

        result.stop();
        LOG.fine("Method " + md + " executed in " + result.getDelay() + " msec");
    }

    public ResultSet getResultFor(MethodDescription md) {
        return verdict.getResultFor(md);
    }

    /**
     * @return The schedule, a read-only map of (Methods X Testers).
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Waits for all expected testers to register their methods (TestSteps).
     */
    public void waitForTesterRegistration() throws InterruptedException {
        LOG.log(Level.FINE, "Waiting for registration. Expecting {0} testers.", expectedTesters);

        TesterRegistration reg;

        while (registeredTesters.size() < expectedTesters.intValue()) {
            reg = remoteCoordinator.registrations().take();
            for (MethodDescription m : reg.methods()) {
                schedule.put(m, reg.tester());
            }
            registeredTesters.add(reg.tester());
            LOG.log(Level.FINEST, "Total tester registrations: {0}", registeredTesters.size());
        }
        LOG.exiting("CoordinatorImpl", "waitForTesterRegistration()");
    }

    /**
     * Waits for all testers to finish the execution of a method.
     *
     * TODO This code is weird. Verdict needs some refactorings.
     * TODO Timeout needed.
     *
     */
    private void waitForExecutionFinished() throws InterruptedException {
        LOG.fine("Waiting for the end of the execution.");
        while (runningTesters.intValue() > 0) {
            ResultSet rs = remoteCoordinator.results().take();
            ResultSet result = verdict.getResultFor(rs.getMethodDescription());
            result.add(rs);
            for (ResultListenner each : listenners) {
                each.newResult(rs);
            }
            runningTesters.decrementAndGet();
        }
    }

    private void quitAllTesters() {
        LOG.entering("CoordinatorImpl", "quitAllTesters()");
        for (Tester each : registeredTesters) {
            executor.submit(new TesterQuit(each));
        }
        LOG.exiting("CoordinatorImpl", "quitAllTesters()");
    }

    /**
     * Waits for all testers to quit the system.
     *
     * @throws InterruptedException
     *
     * TODO Timeout needed.
     */
    public void waitAllTestersToQuit() throws InterruptedException {
        LOG.fine("Waiting all testers to quit.");
        while (registeredTesters.size() > 0) {
            Tester t = remoteCoordinator.leaving().take();
            registeredTesters.remove(t);
            LOG.fine(String.format("Waiting for %d testers to quit.", registeredTesters.size()));
        }
        LOG.fine("All testers quit.");

    }

    /**
     * Clears references to testers.
     */
    public void cleanUp() {
        LOG.fine("Coordinator cleaning up.");
        schedule.clear();
        runningTesters.set(0);
        registeredTesters.clear();
        executor.shutdown();
    }

    @Override
    public String toString() {
        return String.format("Coordinator(expected:%s,registered:%s,running:%s)",
                this.expectedTesters, Integer.valueOf(this.registeredTesters.size()),
                this.runningTesters);
    }

    /**
     * 
     * @return The RemoteCoordinator implementation
     */
    public RemoteCoordinatorImpl getRemoteCoordinator() {
        return remoteCoordinator;
    }

    public void registerResultListenner(ResultListenner listenner) {
        this.listenners.add(listenner);
    }

    private boolean hasDependencyError(MethodDescription md) {
        for (String depend : md.getDepends()) {
            if (errorActions.contains(depend)) {
                return true;
            }
        }
        return false;
    }

    private void setDependencyError(MethodDescription md, TesterSet ts) throws InterruptedException {
        ResultSet rs = new ResultSet(md);
        rs.addSimulatedError();
        ts.setResult(md, rs);
        errorActions.add(md.getName());
        LOG.log(Level.FINEST, "Action {0} was not executed due to its dependence!", md.getName());
    }

    private boolean hasResultError(ResultSet rs) {
        if (rs.getErrors() > 0
                || rs.getInconclusives() > 0
                || rs.getFailures() > 0) {
            return true;
        }
        return false;
    }
    
}
