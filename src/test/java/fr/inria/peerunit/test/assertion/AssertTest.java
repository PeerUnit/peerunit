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
package fr.inria.peerunit.test.assertion;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.tester.ComparisonFailure;
import fr.inria.peerunit.tester.Failure;
import fr.inria.peerunit.tester.InconclusiveFailure;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class AssertTest {

    public AssertTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of assertTrue method, of class Assert.
     */
    @Test
    public void testAssertTrue() {
        try {
            Assert.assertTrue(false);
            fail("Exception not thrown");
        } catch(Failure e) {
            // OK
        }
    }

    /**
     * Test of fail method, of class Assert.
     */
    @Test
    public void testFail() {
        try {
            Assert.fail("ok");
            fail("Exception not thrown");
        } catch(Failure e) {
            assertEquals(e.getMessage(), "ok");
            //
        }
    }

    /**
     * Test of inconclusive method, of class Assert.
     */
    @Test
    public void testInconclusive() {

        try {
            Assert.inconclusive("ok");
            fail("Exception not thrown");
        } catch(InconclusiveFailure e) {
            assertEquals(e.getMessage(), "ok");
            //
        }
    }


    /**
     * Test of assertEquals method, of class Assert.
     */
    @Test
    public void testAssertEquals() {
        String actual = "AAAAA";
        String other = "BBBBB";
        try {
            Assert.assertEquals(null, null);
            Assert.assertEquals(actual, actual);
            Assert.assertEquals(other, "BBBBB");
        } catch (ComparisonFailure c) {
            fail("comparison error");
        }

        try {
            Assert.assertEquals(other, actual);
            fail("comparison error");
        } catch (ComparisonFailure c) {
            // OK
        }
        try {
            Assert.assertEquals(null, actual);
            fail("comparison error");
        } catch (ComparisonFailure c) {
            // OK
        }
        
        try {
            Assert.assertEquals(other, null);
            fail("comparison error");
        } catch (ComparisonFailure c) {
            // OK
        }


    }

    /**
     * Test of assertListEquals method, of class Assert.
     */
    @Test
    public void testAssertListEquals() {
        List<String> list = new ArrayList<String>(5);
        List<String> other = new ArrayList<String>(5);

        list.add("aaa");
        list.add("bbb");
        other.add("bbb");
        other.add("aaa");

        try {
            Assert.assertListEquals(other, other);
            Assert.assertListEquals(other, list);
        } catch (ComparisonFailure c) {
            fail("comparison error");
        }

        try {
            Assert.assertListEquals(null, other);
            fail("comparison error");
        } catch (ComparisonFailure c) {
            // OK
        }

        try {
            Assert.assertListEquals(other, null);
            fail("comparison error");
        } catch (ComparisonFailure c) {
            // OK
        }

        try {
            other.add("bbb");
            Assert.assertListEquals(list, other);
            fail("comparison error");
        } catch(ComparisonFailure c) {
            // OK
        }
    }
}
