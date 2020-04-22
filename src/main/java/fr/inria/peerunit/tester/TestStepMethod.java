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

import java.lang.reflect.Method;

import fr.inria.peerunit.parser.TestStep;

/**
 *
 * @author sunye
 * @author jeugenio
 */
public class TestStepMethod extends TestMethod {

    /**
     * TestStep attributes
     */
    protected int order;
    protected String depend;
    protected int answers;
    protected String when;

    /**
     *
     * @param m
     */
    public TestStepMethod(final Method m) {
        TestStep ts = m.getAnnotation(fr.inria.peerunit.parser.TestStep.class);
        method = m;
        order = ts.order();
        depend = ts.depend();
        answers = ts.answers();
        range = this.newRange(ts.range());
        when = ts.when();
        timeout = ts.timeout();
    }
    public int getAnswers() {
        return answers;
    }
    public String getDepend() {
        return depend;
    }
    public String getWhen() {
        return when;
    }
    public final int getOrder() {
        return order;
    }
    
}
