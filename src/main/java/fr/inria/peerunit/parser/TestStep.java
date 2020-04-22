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
package fr.inria.peerunit.parser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Meta-annotation Retention indicate that this annotation must be retained
 * in runtime
 * Meta-annotation Target indicates that this annotation type can be used
 * to annotate only method declarations.
 * @author 
 * @author jeugenio
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestStep {

    int order() default -1;

    int answers() default -1;
    
    /**
     * Range of peers where the test step should be executed.
     * 
     * @return a String in the form: "*", "54" or "4-17".
     */
    String range() default "*";
    
    String when() default "";

    String depend() default "";

    int timeout() default -1;

}
