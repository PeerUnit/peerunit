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

import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.remote.GlobalVariables;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test the class ParserImpl
 */
public class Data  {

    private int id = -1;

    private GlobalVariables globals;

    @BeforeClass(range = "*", timeout = 100)
    public void begin() {
    }

    @TestStep(range = "*", timeout = 1000, order = 1)
    public void startingNetwork() {
    }

    @TestStep(range = "*", timeout = 1000000, order = 2)
    public void joinNet() {
    }

    @TestStep(range = "*", timeout = 1000000, order = 3)
    public void stabilize() {
    }

    @TestStep(range = "*", timeout = 1000000, order = 4)
    public void put() {
    }

    @TestStep(range = "*", timeout = 1000000, order = 5)
    public void get() {
    }

    @TestStep(range = "42", timeout = 1000000,  order = 6)
    public void notHere() {
    }

    @TestStep(range = "10-100", timeout = 1000000, order = 7)
    public void alsoNotHere() {
    }

    @TestStep(range = "0-100", timeout = 1000000, order = 8)
    public void here() {
    }

    @AfterClass(timeout = 100, range = "*")
    public void end() {
    }


    @SetId
    public void setId(int i) {
        id = i;
    }

    public int getId() {
        return id;
    }

    @SetGlobals
    public void setGlobals(GlobalVariables gv) {
        globals = gv;
    }

    public GlobalVariables getGlobals() {
        return globals;
    }
}
