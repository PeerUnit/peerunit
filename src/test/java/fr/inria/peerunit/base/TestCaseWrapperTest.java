/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.base;

import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.tester.ComparisonFailure;
import fr.inria.peerunit.tester.Failure;
import fr.inria.peerunit.tester.TestCaseWrapper;
import fr.inria.peerunit.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 * @author jeugenio
 */
public class TestCaseWrapperTest {

    private static Data testcase;
    private static TestCaseWrapper wrapper;
    private static CoordinatorImpl coord;
    private static GlobalVariables globals;
    private static TesterImpl tester;

    public TestCaseWrapperTest() {
    }

    @BeforeClass
    public static void inititalize() {
        Properties properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("tester.log.dateformat", "yyyy-MM-dd");
        properties.setProperty("tester.log.timeformat", "HH:mm:ss.SSS");
        properties.setProperty("tester.log.level", "FINEST");
        properties.setProperty("tester.logfolder", "/tmp/");
        properties.setProperty("tester.log.delimiter", "|");
        properties.setProperty("tester.waitForMethod", "500");
        try {
            TesterUtil defaults = new TesterUtil(properties);
            coord = new CoordinatorImpl(defaults);
            globals = new GlobalVariablesImpl();
            new Thread(coord, "Coordinator").start();
            tester = new TesterImpl(coord.getRemoteCoordinator(), globals);
            tester.getRemoteTester().setCoordinator(coord.getRemoteCoordinator());
            wrapper = new TestCaseWrapper(tester);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Tests the TestCaseWrapper.register() method.
     */
    @Test
    public void testRegister() {
        Method[] methods =  Data.class.getMethods();
        List<String> names = new ArrayList<String>(methods.length);
        List<MethodDescription> l = wrapper.register(fr.inria.peerunit.base.Data.class);

        // Only 8 methods should be registered for tester 0
        assertTrue("Only "+l.size()+" methods found.", l.size() == 8);

        for(Method each : methods) {
            names.add(each.getName());
        }

        // All registered methods must belong to the test case.
        for (MethodDescription each : l) {
            assertTrue(names.contains(each.getName()));
        }
    }

    /**
     * Test of getMethod method, of class TestCaseWrapper.
     */
    @Test
    public void testGetMethod() {
        List<MethodDescription> listMethodDesc = wrapper.register(Data.class);
        List<Method> methods =  java.util.Arrays.asList(Data.class.getMethods());

        for (MethodDescription each : listMethodDesc) {
            Method m = wrapper.getMethod(each);
            assertTrue(methods.contains(m));
        }
    }

    /**
     * Test of invoke method, of class TestCaseWrapper.
     */
    @Test
    public void testInvokeFailure() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("failure", 2, 1, "", "", 1000);

        assertNotNull("Method not found", wrapper.getMethod(md));

        try {
            wrapper.invoke(md);
            fail("Exception not thrown");
        } catch (Failure ex) {

        } catch (Throwable t) {
            fail("Wrong exception thrown");
        }
    }
    /**
     * Test of invoke method, of class TestCaseWrapper.
     */

    @Test
    public void testInvokeFailureBis() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("failureBis", 3, 1, "", "", 1000);

        assertNotNull(wrapper.getMethod(md));

        try {
            wrapper.invoke(md);
            fail("Exception not thrown");
        } catch (AssertionError ex) {

        } catch (Throwable t) {
            fail("Wrong exception thrown");
        }
    }

     /**
     * Test of invoke method, of class TestCaseWrapper.
     */
    @Test
    public void testInvokePass() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("first", 1, 1, "", "", 1000);
        
        assertNotNull(wrapper.getMethod(md));
        try {
            wrapper.invoke(md);
            
        } catch (Throwable t) {
            fail("Exception thrown");
        }
    }


    /**
     * Test of isLastMethod method, of class TestCaseWrapper.
     */
//    @Test
//    public void testIsLastMethod() {
//        wrapper.register(Sample.class);
//
//        assertFalse(wrapper.isLastMethod());
//        for(MethodDescription each : wrapper.getMethods().keySet()) {
//            try {
//                wrapper.invoke(each);
//            } catch (Throwable ex) {}
//        }
//        assertTrue(wrapper.isLastMethod());
//    }

    @Test
    public void testSetId() {
        wrapper.register(fr.inria.peerunit.base.Data.class);
        Data d = (Data) wrapper.getTestcase();
        assertTrue (d.getId() == 0) ;
    }

    @Test
    public void testSetGlobals() {
        wrapper.register(fr.inria.peerunit.base.Data.class);
        Data d = (Data) wrapper.getTestcase();
        assertTrue (d.getGlobals() == globals) ;
    }
}
