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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.remote.GlobalVariables;

/**
 *
 * @author sunye
 */
public class TestCaseWrapper {

    private static Logger LOG = Logger.getLogger(TestCaseWrapper.class.getName());
    private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
    private List<MethodDescription> remainingMethods;
    private Object testcase;
    private int testerId = -1;
    private TesterImpl tester;

    public TestCaseWrapper(TesterImpl t) {
        assert t != null;

        this.tester = t;
        this.testerId = tester.getId();

    }

    /**
     * Get the method following methodDescription
     * @param md Method Description
     * @return the method
     */
    public Method getMethod(MethodDescription md) {
        return methods.get(md);
    }

    /**
     * Execute the given method description
     *
     * @param md : method description to execute
     * @throws Throwable if any exception is thrown
     */
    public void invoke(MethodDescription md) throws Throwable {

        assert methods.containsKey(md) : "Method should be registered";
        //assert remainingMethods.contains(md) : "Method already executed";
        assert testcase != null : "Test Case instance should not be null";

        Method m = methods.get(md);
        try {
            m.invoke(testcase, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.fillInStackTrace();
            throw e.getCause();
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getCause();
        } finally {
            //remainingMethods.remove(md);
        }
    }

    /**
     * Verifies if no more methods remain.
     * 
     * @return true if the method is the last one to be executed
     */
    @Deprecated
    private boolean isLastMethod() {

        return remainingMethods.isEmpty();
    }

    /**
     * Parse the test case to extract the methods to be executed
     * @param class
     * @return List of methods to be executed
     */
    public List<MethodDescription> register(Class<?> klass) {

        ClassFilter filter = new ClassFilter(klass);
        methods.clear();

        //TestStep
        for (TestStepMethod each : filter.stepMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        //AfterClass
        //TODO: At most 1 AfterClass method.
        for (AfterClassMethod each : filter.afterMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        //BeforeClass
        //TODO: At most 1 AfterClass method.
        for (BeforeClassMethod each : filter.beforeMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        //TestCase instantiation
        try {
            testcase = klass.newInstance();
//            if (TestCase.class.isAssignableFrom(klass)) {
//                ((TestCase) testcase).setTester(tester);
//            }

            if (filter.setId() != null) {
                filter.setId().invoke(testcase, testerId);
            }

            if (filter.setGlobals() != null) {
                filter.setGlobals().invoke(testcase, tester.globalTable());
            }

        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, "Instantiation Exception", e);
        } catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, "Illegal Access Exception", e);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Illegal Argument Exception", ex);
        } catch (InvocationTargetException ex) {
            LOG.log(Level.SEVERE, "Invocation Target Exception", ex);
        }

        remainingMethods = new ArrayList<MethodDescription>(methods.keySet());
        return remainingMethods;
    }

    public Object getTestcase() {
        return testcase;
    }

    public Map<MethodDescription, Method> getMethods() {
        return methods;
    }
}

class ClassFilter {

    private List<TestStepMethod> stepMethods = new ArrayList<TestStepMethod>();
    private List<BeforeClassMethod> beforeMethods = new ArrayList<BeforeClassMethod>();
    private List<AfterClassMethod> afterMethods = new ArrayList<AfterClassMethod>();
    private Method setId;
    private Method setGlobals;

    public ClassFilter(Class<?> c) {

        Method[] methods = c.getMethods();
        for (Method each : methods) {
            if (each.isAnnotationPresent(TestStep.class)) {
                stepMethods.add(new TestStepMethod(each));
            } else if (each.isAnnotationPresent(BeforeClass.class)) {
                beforeMethods.add(new BeforeClassMethod(each));
            } else if (each.isAnnotationPresent(AfterClass.class)) {
                afterMethods.add(new AfterClassMethod(each));
            } else if (each.isAnnotationPresent(SetId.class)
                    && each.getParameterTypes().length == 1
                    && each.getParameterTypes()[0] == int.class) {
                setId = each;
            } else if (each.isAnnotationPresent(SetGlobals.class)
                    && each.getParameterTypes().length == 1
                    && each.getParameterTypes()[0] == GlobalVariables.class) {
                setGlobals = each;
            }
        }
    }

    public List<TestStepMethod> stepMethods() {
        return stepMethods;
    }

    public List<BeforeClassMethod> beforeMethods() {
        return beforeMethods;
    }

    public List<AfterClassMethod> afterMethods() {
        return afterMethods;
    }

    public Method setId() {
        return setId;
    }

    public Method setGlobals() {
        return setGlobals;
    }
}


