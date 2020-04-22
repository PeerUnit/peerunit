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


/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */
public class InconclusiveFailure extends TestException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs an inclusive failure.
     *
     * @since 1.0
     */
    public InconclusiveFailure() {
        super();
    }

    /**
     * Constructs an inclusive failure.
     *
     * @since 1.0
     * @param message is the message corresponding to the failure.
     */
    public InconclusiveFailure(String message) {
        super(message);
    }
}
