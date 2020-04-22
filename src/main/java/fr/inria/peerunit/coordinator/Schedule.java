/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author sunye
 */
public class Schedule {

    private Map<MethodDescription, Set<Tester>> testerMap;

    // Michel
    private Map<Integer, Set<MethodDescription>> orderMethodsMap;

    public Schedule() {
        testerMap = Collections.synchronizedMap(new TreeMap<MethodDescription, Set<Tester>>());
        orderMethodsMap = Collections.synchronizedMap(new TreeMap<Integer, Set<MethodDescription>>());
    }

    public void put(MethodDescription md, Tester t) {
        if (!testerMap.containsKey(md)) {
            testerMap.put(md, new HashSet<Tester>());
        }
        testerMap.get(md).add(t);
        // Michel (Methods linked with a specific order)
        int order = md.getOrder();
        if (!orderMethodsMap.containsKey(order)) {
            orderMethodsMap.put(order, new HashSet<MethodDescription>());
        }
        orderMethodsMap.get(order).add(md);
    }

    public void put(TesterRegistration tr) {
        for(MethodDescription each : tr.methods()) {
            this.put(each,tr.tester());
        }
    }

    public void clear() {
        testerMap.clear();
    }

    public boolean containsMethod(MethodDescription md) {
        return testerMap.containsKey(md);
    }
    public int size() {
        return testerMap.size();
    }

    public Collection<MethodDescription> methods() {
        return new ArrayList<MethodDescription>(testerMap.keySet());
    }

    public Collection<Integer> orders() {
        return new ArrayList<Integer>(orderMethodsMap.keySet());
    }

    public Collection<Tester> testersFor(MethodDescription md) {
        return testerMap.get(md);
    }

    // Michel
    public Collection<MethodDescription> methodsFor(Integer order) {
        return orderMethodsMap.get(order);
    }

    // Michel
    public int sizeOrderMap() {
        return orderMethodsMap.size();
    }
}
