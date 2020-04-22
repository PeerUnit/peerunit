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

import java.io.IOException;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The application log class. This class is used for  log application runtime events.
 * This class's objects are parameterized by the values of properties of application properties file.
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 */
public class PeerUnitLogger {

    /**
     * A logging object
     */
    private static Logger LOG;

    /**
     * In the constructor, either a existing logger corresponding to the argument is cover,
     * or a new logger is created.
     *
     * @param name  the name of the logger to find or to create
     */
    public PeerUnitLogger(String name) {
        LOG = Logger.getLogger(name);
    }

    /**
     * This method set the logger parameters, a file handler with his, the level.
     *
     * @param pattern the path of the file must containing logging messages
     */
    public synchronized void createLogger(String pattern) {
        LogFormat format = new LogFormat();
        Level level = TesterUtil.instance.getLogLevel();
        FileHandler handler = null;
        try {
            handler = new FileHandler(pattern, true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handler.setFormatter(format);
        LOG.addHandler(handler);
        LOG.setLevel(level);
        LOG.log(level, "Logfile location: " + pattern);
    }

    /**
     * Log a message at a given level.
     *
     * @param level the level of logging
     * @param msg the message to log
     */
    public synchronized void log(Level level, String msg) {
        LOG.log(level, msg);
    }

    /**
     * Log a exception.
     *
     * @param e the exception to log
     */
    public synchronized void logStackTrace(Exception e) {
        /**
         * Logging the stack trace
         */
        StackTraceElement elements[] = e.getStackTrace();
        LOG.log(Level.SEVERE, e.toString());
        for (int i = 0, n = elements.length; i < n; i++) {
            LOG.log(Level.SEVERE, "at " + elements[i].toString());
        }

        LOG.log(Level.SEVERE, "Caused by: " + e.getCause());
    }
    
    public static void createLogger(TesterUtil defaults, String pattern) throws IOException {
        String folder = defaults.getLogfolder();
        File f = new File(folder);
        f.mkdirs();
        Level l = defaults.getLogLevel();
        FileHandler handler = new FileHandler(folder+"/"+pattern);
        
        handler.setFormatter(new LogFormat());
        handler.setLevel(l);

        Logger myLogger = Logger.getLogger("fr.inria");
        myLogger.setUseParentHandlers(false);
        myLogger.addHandler(handler);
        myLogger.setLevel(l);
    }
}
