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
package fr.inria.peerunit.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 
 * This objects of this class format the logging messages in conformity with
 * the values of properties file.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see PeerUnitLogger
 */
public class LogFormat extends Formatter {

    /**
     * The delimiter used for the logging.
     */
    private static String delimiter = TesterUtil.instance.getDelimiter();
    /**
     * The date format used for the logging.
     */
    private static String dateformat = TesterUtil.instance.getDateformat();
    /**
     * The time format used for the logging.
     */
    private static String timeformat = TesterUtil.instance.getTimeformat();

    /**
     *  This method is overridden to apply our own formatting
     *
     *  @param rec  the content to format
     *  @return the string resulting from formatting
     */
    @Override
    public String format(LogRecord rec) {
        StringBuffer buf = new StringBuffer(1000);

        Timestamp time = new Timestamp(System.currentTimeMillis());

        SimpleDateFormat format = new SimpleDateFormat(dateformat + "' '" + timeformat);

        buf.append(format.format(time));
        buf.append(delimiter);
        buf.append(rec.getLevel());
        buf.append(delimiter);
        buf.append(rec.getSourceClassName());
        buf.append(delimiter);
        buf.append(rec.getSourceMethodName());
        buf.append(delimiter);
        buf.append(formatMessage(rec));
        buf.append('\n');
        return buf.toString();
    }
}
