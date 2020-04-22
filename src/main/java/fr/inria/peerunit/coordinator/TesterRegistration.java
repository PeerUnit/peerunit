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

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Tester;
import java.io.Serializable;
import java.util.Collection;

public class TesterRegistration implements Serializable {

    private final Tester tester;
    private final Collection<MethodDescription> methods;

    public TesterRegistration(Tester t, Collection<MethodDescription> coll) {
        tester = t;
        methods = coll;
    }

    public Tester tester() {
        return tester;
    }

    Collection<MethodDescription> methods() {
        return methods;
    }
}
