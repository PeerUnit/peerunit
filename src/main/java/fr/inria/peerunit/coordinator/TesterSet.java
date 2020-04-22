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

/**
 *
 * @author sunye
 * @author jeugenio
 */
public interface TesterSet {

    void execute(String... str) throws InterruptedException;

    void execute(MethodDescription... md) throws InterruptedException;

    void dependencyExecute(MethodDescription md, TesterSet ts) throws InterruptedException;

    void hierarchicalExecute(Integer order) throws InterruptedException;

    void globalExecute(Integer order, TesterSet ts) throws InterruptedException;

    Schedule getSchedule();

    public ResultSet getResult(MethodDescription md);

    public void setResult(MethodDescription md, ResultSet rs) throws InterruptedException;

}
