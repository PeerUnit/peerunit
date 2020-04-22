/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.base;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class RangeTest {

    @Test
    public void testStar() {
        Range star = Range.fromString("*");
        assertTrue(star.includes(0));
        assertTrue(star.includes(Integer.MAX_VALUE));
        assertTrue(star.includes(42));
        assertFalse(star.includes(-1));
        assertFalse(star.includes(Integer.MIN_VALUE));
    }

    @Test
    public void testSingle() {
        Range r = Range.fromString("33");
        int[] wrong = {0, -1, 32, 34, Integer.MAX_VALUE};

        assertTrue(r.includes(33));

        for (int each : wrong) {
            assertFalse(r.includes(each));
        }
    }

    @Test
    public void testInterval() {
        Range r = Range.fromString("13-32");
        int[] wrong = {0, -1, 12, 33, Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] right = {13, 32, 16, 20, 25, 29};

        for (int each : wrong) {
            assertFalse(r.includes(each));
        }

        for (int each : right) {
            assertTrue("Must include: " + each, r.includes(each));
        }

    }

    @Test
    public void testSimpleUnion() {
        Range r = Range.fromString("1, 5");
        int[] right = {1,5};

        for(int each : right) {
            assertTrue(r.includes(each));
        }
    }

    @Test
    public void testRangeUnion() {
        Range r = Range.fromString("1-5, 10-15");
        int[] right = {1, 5, 10, 13, 15};
        int[] wrong = {0, 6, 9, 16, Integer.MAX_VALUE, -1};

        for(int each : right) {
            assertTrue(r.includes(each));
        }

        for(int each : wrong) {
            assertFalse(r.includes(each));
        }
    }

    @Test
    public void testMixUnion() {
        Range r = Range.fromString("1-5, 15");
        int[] right = {1, 5, 15};
        int[] wrong = {0, 6, 9, 16, Integer.MAX_VALUE, -1};

        for(int each : right) {
            assertTrue(r.includes(each));
        }

        for(int each : wrong) {
            assertFalse(r.includes(each));
        }
    }

    @Test
    public void testAllUnion() {
        Range star = Range.fromString("5, 10-15, *");
        assertTrue(star.includes(0));
        assertTrue(star.includes(Integer.MAX_VALUE));
        assertTrue(star.includes(42));
        assertFalse(star.includes(-1));
        assertFalse(star.includes(Integer.MIN_VALUE));
    }

}
