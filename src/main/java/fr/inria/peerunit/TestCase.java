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
package fr.inria.peerunit;

import fr.inria.peerunit.remote.Tester;

/**
 * The <i>test cases</i> interface. This interface must be implemented by the 
 * testing engineer  wants to write a <i>test case</i>, it allow to access the 
 * <i>tester</i> who will execute the  <i>test case</i>.
 *  
 * @author sunye
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.TestCaseImpl  
 **/
public interface TestCase {

//	public void setTester(TesterImpl ti);


	/**
	 * For set the reference to the <i>tester</i> which  will execute the  <i>test case</i>
	 * in distributed  architecture. 
	 * 
	 * @param t the  reference of the subjacent <i>tester</i>
	 */		
	public void setTester(Tester t);
	
}
