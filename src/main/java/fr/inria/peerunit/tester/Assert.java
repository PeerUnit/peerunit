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

import java.util.List;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */
public class Assert {

    /**
     * Asserts that a condition is true.
     *
     */
    static public void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * Asserts that a condition is true. 
     *
     */
    static public void assertTrue(boolean condition) {
        assertTrue("", condition);
    }

    /**
     * Fails a test with the given message.
     *
     */
    static public void fail(String message) {
        throw new Failure(message);
    }

    /**
     * Fails a test with an inconclusive verdict.
     */
    static public void inconclusive(String message) {
        throw new InconclusiveFailure(message);
    }

    /**
     * Fails a test with no message.
     */
    static public void fail() {
        fail("");
    }

    /**
     * Asserts that two objects are equal. 
     * If they are not, an {@link ComparisonFailure} is thrown with the given message.
     * If <code>expected</code> and <code>actual</code>are <code>null</code>,
     * they are considered equal.
     * @param message the identifying message or <code>null</code> for the {@link ComparisonFailure}
     * @param expected expected value
     * @param actual actual value
     *
     */
    static public void assertEquals(String message, Object expected, Object actual) {
        if (expected != actual && (expected == null || ! expected.equals(actual))) {
            throw new ComparisonFailure(message, expected, actual);
        }
    }

    /**
     * Asserts that two objects are equal. 
     * @param expected expected value
     * @param actual actual value
     */
    static public void assertEquals(Object expected, Object actual) {
        assertEquals("", expected, actual);
    }

    /**
     * Asserts that two lists are the same. If they are not equal,
     * an {@link AssertionError} with the given message is thrown.
     * @param expected list of expected values.
     * @param actual list of actual values.
     * @param message is the message corresponding to the error.
     *
     * @since 1.0
     */
    public static void assertListEquals(String message, List<?> expected, List<?> actual) {
        if ((expected == null || actual == null) ||
                expected.size() != actual.size() ||
                !expected.containsAll(actual)) {
            throw new ComparisonFailure(message, expected, actual);
        }
    }

    public static void assertListEquals(List<?> expected, List<?> actual) {
        assertListEquals("", expected, actual);
    }
}
