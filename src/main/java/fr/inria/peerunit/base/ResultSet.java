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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sunye
 */
public class ResultSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SingleResult> results;

    private final MethodDescription method;

    private AtomicInteger errors;
    private AtomicInteger failures;
    private AtomicInteger inconclusives;
    private AtomicInteger passes;

    /**
     * Accumulated value of local execution delay.
     */
    private long accumulatedDelay = 0;
    private long start;
    private long stop;
    
    public ResultSet (MethodDescription md) {
        results = Collections.synchronizedList(new LinkedList<SingleResult>());
        method = md;
	errors = new AtomicInteger(0);
	failures = new AtomicInteger(0);
	inconclusives = new AtomicInteger(0);
	passes = new AtomicInteger(0);
    }

    public void add(ResultSet other) {
        assert method.equals(other.method) : "Adding incompatible ResultSet";

        errors.addAndGet(other.errors.intValue());
        failures.addAndGet(other.failures.intValue());
        inconclusives.addAndGet(other.inconclusives.intValue());
        passes.addAndGet(other.passes.intValue());
        accumulatedDelay += other.accumulatedDelay;

        results.addAll(other.results);
    }

    public void add(SingleResult r) {
        assert method.equals(r.getMethodDescription()) : "Adding incompatible SingleResult";

        results.add(r);
        accumulatedDelay += r.getDelay();
        switch(r.getVerdict()) {
            case PASS: 
                passes.incrementAndGet();
                break;
            case ERROR:
                errors.incrementAndGet();
                break;
            case FAIL:
                failures.incrementAndGet();
                break;
            case INCONCLUSIVE:
                inconclusives.incrementAndGet();
                break;
        }
    }

    // Michel
    public void addSimulatedError() {
        errors.incrementAndGet();
    }

    @Override
    public String toString() {
        long average = this.size() == 0 ? 0 : accumulatedDelay/size();
        return String.format("Level: %d. Pass: %d. Fails: %d. Errors: %d. " +
			     "Inconclusive: %d. Time elapsed: %d msec. Average: %d msec. \t Method: %s",
			     method.getOrder(), passes.intValue(), failures.intValue(), errors.intValue(), 
                             inconclusives.intValue(), getDelay(), average, method.getName());
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public long getDelay() {
        return stop - start;
    }

    public MethodDescription getMethodDescription() {
        return method;
    }

    public int size() {
        return results.size();
    }

    public int getErrors() {
        return errors.intValue();
    }

    public int getFailures() {
        return failures.intValue();
    }

    public int getInconclusives() {
        return inconclusives.intValue();
    }

    public int getPass() {
        return passes.intValue();
    }
}
