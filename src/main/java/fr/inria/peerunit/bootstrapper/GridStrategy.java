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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.HNode;
import fr.inria.peerunit.util.HTree;
import fr.inria.peerunit.util.TesterUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author sunye
 */
public class GridStrategy implements TreeStrategy {

    private static final Logger LOG = Logger.getLogger(GridStrategy.class.getName());
    /**
     * default values for global variables
     */
    private TesterUtil defaults;
    /**
     * Map containing IP Address X Tester
     */
    private final HTree<String, TesterNode> testerTree;
    private final TesterMap testerMap;


    public GridStrategy(TesterUtil tu) {
        defaults = tu;
        testerMap = new TesterMap();
        testerTree = new HTree<String, TesterNode>(defaults.getTreeOrder());
    }

    public void register(DistributedTester tester) {
        LOG.entering("GridStrategy", "register(Tester)");
        String address = "Unknown";
        try {
            address = tester.getAddress();
        } catch (RemoteException ex) {
        }
        LOG.info("New tester:" + address);

        testerMap.put(address, tester);

        synchronized (testerMap) {
            testerMap.notifyAll();
        }
    }

    public void buildTree() {
        LOG.fine("Ready to build the tester tree");
        LOG.fine(testerMap.toString());
        for (Map.Entry<String, Collection<DistributedTester>> each : testerMap.entrySet()) {
            DistributedTester[] aux  = each.getValue().toArray(new DistributedTester[]{});
            testerTree.put(each.getKey(), new TesterNode(aux));
        }
    }

    public int getNodesSize() {
        //return tree.getNodesSize();
        return 0;
    }

    public void setCommunication() {
        HNode<String, TesterNode> node = testerTree.head();
        try {
            this.setCommunication(node);
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void setCommunication(HNode<String, TesterNode> n)
            throws RemoteException {

        HNode<String, TesterNode>[] children = n.children();

        List<TesterNode> nodes = new ArrayList<TesterNode>(children.length);
        for (HNode<String, TesterNode> each : children) {
            nodes.add(each.value());
        }
        TesterNode c = n.value();
        this.register(c, nodes);

        for (HNode<String, TesterNode> each : children) {
            if (!each.isLeaf()) {
                this.setCommunication(each);
            } else {
                this.register(each.value());
            }
        }
    }

    private void register(TesterNode tn, List<TesterNode> children) 
            throws RemoteException {
        
        List<DistributedTester> list = new LinkedList<DistributedTester>();
        list.addAll(tn.dependents());
        for (TesterNode each : children) {
            list.add(each.head());
        }
        tn.head().registerTesters(list);
    }

    private void register(TesterNode tn) throws RemoteException {
        if (! tn.dependents().isEmpty()) {
            tn.head().registerTesters(tn.dependents());
        }
        
    }

    public int getRegistered() {
        return testerMap.size();
    }

    public void startRoot() throws RemoteException {
        DistributedTester root = testerTree.head().value().head();
        root.start();
    }

    public void cleanUp() {
        testerMap.clear();
    }

    class TesterMap {

        private AtomicInteger size = new AtomicInteger(0);
        private Map<String, Collection<DistributedTester>> testers;

        public TesterMap() {
            testers =  Collections.synchronizedMap(
                    new HashMap<String, Collection<DistributedTester>>());
        }
        
        void put(String address, DistributedTester tester) {
            if (!testers.containsKey(address)) {
                testers.put(address, Collections.synchronizedList(
                        new LinkedList<DistributedTester>()));
            }
            testers.get(address).add(tester);
            size.getAndIncrement();
        }

        int size() {
            return size.get();
        }

        void clear() {
            testers.clear();
        }

        Collection<Collection<DistributedTester>> values() {
            return testers.values();
        }

        Set<Map.Entry<String, Collection<DistributedTester>>> entrySet() {
            return testers.entrySet();
        }

        @Override
        public String toString() {
            StringBuffer result = new StringBuffer("TesterMap[size="+size+"]\n");
            for (Map.Entry<String, Collection<DistributedTester>> each : testers.entrySet()) {
                result.append("Key: " + each.getKey());
                result.append(" Nodes: " + each.getValue().size());
                result.append("\n");
            }
            return result.toString();
        }
    }
}


