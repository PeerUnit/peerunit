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
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;

import java.util.Collections;
import java.util.TreeMap;
import java.util.Map;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */
public class GlobalVerdict {

    private Verdicts globalVerdict = null;
    private int passVerdicts = 0;
    private int incVerdicts = 0;
    private int failVerdicts = 0;
    private int errors = 0;
    private int relaxIndex;
    private Map<MethodDescription, ResultSet> results;

    public GlobalVerdict(int i) {
        relaxIndex = i;
        results = Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());
    }

    /**
     * Calculates the global verdict of a test suit. </br>
     * Once verdict is <code>FAIL</code> it will not have another verdict.
     *
     * @param localVerdict is the current verdict.
     * @param index is the threshold of inclusive results accepted to get a correct (PASS) global verdict.
     * @since 1.0
     */
    public void addLocalVerdict(Verdicts localVerdict) {

        switch (localVerdict) {
            case FAIL:
                failVerdicts++;
                break;
            case PASS:
                passVerdicts++;
                break;
            case INCONCLUSIVE:
                incVerdicts++;
                break;
            default:
                System.err.println("Unknown verdict");
                break;
        }

    }

    /**
     * Returns the global verdict.
     *
     * @return the global verdict : <code>PASS</code>, <code>FAIL</code>, <code>INCONCLUSIVE</code>, <code>ERROR</code>.

     * @since 1.0
     *
     */
    public Verdicts getGlobalVerdict() {
        return globalVerdict;
    }

    /**
     * Returns the number of executed testers.
     *
     * @return the number of executed testers (passed, failed and inconclusive).
     * @since 1.0
     */
    public int getJudged() {
        return passVerdicts + incVerdicts + failVerdicts;
    }

    @Override
    public String toString() {
        long accumulatedDelay = 0;
        StringBuffer result = new StringBuffer();
        result.append("------------------------------\n");
        result.append("Test Case Verdict: \n");
        for (ResultSet each : results.values()) {
            result.append(each).append("\n");
            accumulatedDelay += each.getDelay() ;
        }
        result.append("Global Verdict with relax index " + relaxIndex + "% is " + getGlobalVerdict() + "\n");
        result.append("LocalVerdicts are (Pass:" + passVerdicts + ") (Inconc.: " + incVerdicts + ") (Fail: " + failVerdicts + ")\n");
        result.append("Accumulated Time Elapsed: ").append(accumulatedDelay).append(" msec\n");
        result.append("------------------------------\n");

        return result.toString();
    }

    public void calculateVerdict() {

        for (ResultSet each : results.values()) {
            passVerdicts += each.getPass();
            incVerdicts += each.getInconclusives();
            failVerdicts += each.getFailures();
            errors += each.getErrors();
        }
        if (failVerdicts > 0) {
            globalVerdict = Verdicts.FAIL;
        } else if ((((double) incVerdicts / (passVerdicts + incVerdicts)) * 100) <= relaxIndex) {
            globalVerdict = Verdicts.PASS;
        } else {
            globalVerdict = Verdicts.INCONCLUSIVE;
        }
    }

    public void putResult(MethodDescription md, ResultSet rs) {
        results.put(md, rs);
    }

    public ResultSet getResultFor(MethodDescription md) {
        return results.get(md);
    }

    public boolean containsMethod(MethodDescription md) {
        return results.containsKey(md);
    }
}
