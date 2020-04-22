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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class TesterSetImpl implements TesterSet {

    private static final Logger LOG =
            Logger.getLogger(TesterSetImpl.class.getName());
    private CoordinatorImpl coordinator;
    private Map<String, MethodDescription> methods;

    public TesterSetImpl(CoordinatorImpl ci) {
        coordinator = ci;
    }

    public void execute(String... str) throws InterruptedException {        
        if (methods == null) {
            // Lazy initialization of methods Map.
            LOG.log(Level.FINE, "Method map initialization.");
            methods = new HashMap<String, MethodDescription>();
            for (MethodDescription each : coordinator.getSchedule().methods()) {
                methods.put(each.getName(), each);
            }
        }

        if (methods.containsKey(str[0])) {
            coordinator.execute(methods.get(str[0]));
        } else {
            LOG.log(Level.WARNING, "Method not found: {0}", str[0]);
        }
    }

    public void execute(MethodDescription... md) throws InterruptedException {
        coordinator.execute(md[0]);
    }

    public void dependencyExecute(MethodDescription md, TesterSet ts) throws InterruptedException {
        coordinator.dependencyExecute(md,ts);
    }

    public void hierarchicalExecute(Integer order) throws InterruptedException {
        coordinator.hierarchicalExecute(order);
    }

    public void globalExecute(Integer order, TesterSet ts) throws InterruptedException {
        coordinator.globalExecute(order, ts);
    }

    public Schedule getSchedule() {
        return coordinator.getSchedule();
    }
    
    public ResultSet getResult(MethodDescription md) {
        return coordinator.getResultFor(md);
    }

    public void setResult(MethodDescription md, ResultSet rs) throws InterruptedException {
            coordinator.setResult(md, rs);
    }
}
