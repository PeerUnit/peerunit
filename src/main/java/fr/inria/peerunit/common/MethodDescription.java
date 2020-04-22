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
package fr.inria.peerunit.common;

import java.io.Serializable;

import fr.inria.peerunit.tester.AfterClassMethod;
import fr.inria.peerunit.tester.BeforeClassMethod;
import fr.inria.peerunit.tester.TestStepMethod;
import java.util.ArrayList;

/**
 * @author sunye
 * @author jeugenio
 *
 */
public class MethodDescription implements Comparable<MethodDescription>,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Method name
     */
    private String name;
    /**
     * Hierarchy level
     */
    private int order;
    /**
     * Required answers
     */
    private int answers = -1;
    /**
     * Trigger for fault injection
     */
    private String when;
    /**
     * Dependency actions
     */
    private String depend;
    /**
     * Method execution timeout (in milliseconds).
     */
    private int timeout;

    /*
     * Create a method description
     */
    public MethodDescription(String name, int order, int answers, String when, 
            String depend, int timeout) {
        this.name = name;
        this.order = order;
        this.answers = answers;
        this.when = when;
        this.depend = depend;
        this.timeout = timeout;
    }

    public MethodDescription(TestStepMethod method) {
        this(method.method().getName(), method.getOrder(), method.getAnswers(), method.getWhen(), method.getDepend(), method.timeout());
    }

    public MethodDescription(BeforeClassMethod method) {
        this(method.method().getName(), Integer.MIN_VALUE, -1, "", "", method.timeout());
    }

    public MethodDescription(AfterClassMethod method) {
        this(method.method().getName(), Integer.MAX_VALUE, -1, "", "", method.timeout());
    }


    /*
     * Compares the order of this object with the order of the specified object for order.
     *
     * @param o - the MethodDescription to be compared.
     * @return int
     *  -1 if the order of this object is less than the order of the specified object
     *  0 if the order of this object is equal to the order of the specified object
     *  1 if the order of this object is greater to the order of the specified object
     */
    public int compareTo(MethodDescription other) {

        if (order < other.order) {
            return -1;
        }

        if (order > other.order) {
            return 1;
        }

        return name.compareTo(other.name);
    }

    /*
     * Returns a string representation of the method description . The toString
     * method returns a string consisting of the name, the test case and the
     * order of the class of which the object is an instance
     *
     * @return String a string representation of the method description
     */
    @Override
    public String toString() {
        return String.format("Method: %s", name);
        //return String.format("Method: %s Order: %d Depend: %s Answers: %d When: %s Timeout: %d", name, order, depend, answers, when, timeout);
    }

    /*
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o - the reference object with which to compare.
     * @return boolean - true if this object is the same as the object argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MethodDescription)) {
            return false;
        } else {
            MethodDescription other = (MethodDescription) o;
            return name.equals(other.name)
                    && order == other.order
                    && timeout == other.timeout;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + this.order;
        hash = 53 * hash + this.timeout;
        return hash;
    }

    /*
     * Returns the name associated to method
     * @return String
     */
    public String getName() {
        return name;
    }

    /*
     * Returns the method execution timeout (in milliseconds)
     * @return int
     */
    public int getTimeout() {
        return timeout;
    }

    public int getOrder() {
        return order;
    }

    public int getAnswers() {
        return answers;
    }
   
    public String getDepend() {
        return depend;
    }

    public ArrayList<String> getDepends() {
        ArrayList<String> depends = new ArrayList<String>();
        // depend is null in before and after class methods.
        if (depend != null) {
            String[] tmp = depend.split(",");
            //depends.addAll(Arrays.asList(tmp));
            for (String token : tmp) {
                depends.add(token.trim());
            }
        }
        return depends;
    }
    public String getWhen() {
        return when;
    }

    public void setAnswers(int nanswers) {
        answers = nanswers;
    }
   
}
