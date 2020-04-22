package freepastry;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.test.TestInsertJoin;

public class Bootstrap {
	private static Logger log = Logger.getLogger(TestInsertJoin.class.getName());


	
	public static void main(String[] str) {		

		// Log creation
		FileHandler handler;
		try {
			handler = new FileHandler(TesterUtil.getLogfolder()+"/bootstrap.log",true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		Peer peer=new Peer();
		Network net=new Network();
		if(!net.joinNetwork(peer, null, true, log)){
			throw new BootException("Can't bootstrap");
		}
	}
}
