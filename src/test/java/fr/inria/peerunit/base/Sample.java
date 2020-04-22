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
package fr.inria.peerunit.base;

import fr.inria.peerunit.parser.TestStep;
import static fr.inria.peerunit.tester.Assert.*;

/**
 * @author sunye
 * 
 * This is not a test case !!!
 * 
 * This class is only used to test TesterImpl and CoordinatorImpl
 */
public class Sample {

    @TestStep(range = "*", timeout = 1000, order = 1)
    public void first() {
        System.setProperty("executed", "ok");
    }

    @TestStep(range = "*", timeout = 1000, order = 2)
    public void failure() {
        assertTrue(false);
    }

    @TestStep(range = "*", timeout = 1000, order = 3)
    public void failureBis() {
        assert false;
    }
}
