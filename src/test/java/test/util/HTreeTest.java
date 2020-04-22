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
package test.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import fr.inria.peerunit.util.HTree;

/**
 *
 * @author sunye
 */
public class HTreeTest {

    public HTreeTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of insert method, of class HTree.
     */
    @Test
    public void testInsert() {
        HTree<Integer,String> tree = new HTree<Integer, String>(5);

        for (int i = 0; i < 1000; i++) {
            Integer e = new Integer(i);
            tree.put(e,e.toString());
            assertTrue(tree.containsKey(e));
        }
    }

    @Test
    public void testHead() {
         HTree<Integer, String> tree = new HTree<Integer,String>(5);

        for (int i = 0; i < 1000; i++) {
            Integer e = new Integer(i);
            tree.put(e,e.toString());
        }

        assertEquals(tree.head().key(), new Integer(0));
    }
}