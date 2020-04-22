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

/*
 * Thrown to indicate that an annotation is not well formed.
 * The public constructor provided by this class ensure that the annotation error returned by the invocation: 
 * new AnnotationFailure("message")
 */
public class AnnotationFailure  extends AssertionError{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/*
	 * Constructs an AnnotationFailure with its detail message derived from the specified string
	 * @param message - value to be used in constructing detail message
	 */
	public AnnotationFailure(String message) {
		super (message);
	}
}
