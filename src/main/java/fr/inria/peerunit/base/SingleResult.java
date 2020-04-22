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

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.Verdicts;
import java.io.Serializable;

/**
 *
 * @author sunye
 */
public class SingleResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private MethodDescription md;
    private int testerId;
    private long start;
    private long stop;

    private Verdicts verdict = Verdicts.PASS;

    public SingleResult(int id, MethodDescription md) {
        testerId = id;
        this.md = md;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public void addError(Throwable t) {
        verdict = Verdicts.ERROR;
    }

    public void addFailure(AssertionError ae) {
        verdict = Verdicts.FAIL;
    }
    
    public void addInconclusive(Throwable t) {
        verdict = Verdicts.INCONCLUSIVE;
    }

    public long getDelay() {
        return stop - start;
    }
    
    public MethodDescription getMethodDescription() {
        return md;
    }

    public int getTesterId() {
        return testerId;
    }
    
    public Verdicts getVerdict() {
        return verdict;
    }

    public ResultSet asResultSet() {
        ResultSet rs = new ResultSet((md));
        rs.add(this);
        return rs;
    }
}
