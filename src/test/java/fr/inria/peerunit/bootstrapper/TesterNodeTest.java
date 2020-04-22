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
package fr.inria.peerunit.bootstrapper;

import fr.inria.peerunit.distributed.RemoteDistributedTesterImpl;
import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class TesterNodeTest {

    private DistributedTester[] testers = new DistributedTester[5];
    private TesterNode node;


    @Before
    public void setUp() {
        for (int i = 0; i < testers.length; i++) {
            testers[i] = new RemoteDistributedTesterImpl(TesterUtil.instance);
        }
        node = new TesterNode(testers);
    }

    @Test
    public void testHead() {
        assertTrue(node.head() == testers[0]);
    }

    @Test
    public void testDependents() {
        assertTrue(node.dependents().size() == (testers.length - 1));

        for (int i = 1; i < testers.length; i++) {
            node.dependents().contains(testers[i]);
        }
    }

    @Test
    public void non() {
        
    }
}
