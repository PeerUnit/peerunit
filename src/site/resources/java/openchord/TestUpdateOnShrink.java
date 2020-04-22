
package openchord.test;

import static fr.inria.peerunit.test.assertion.Assert.assertTrue;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import openchord.DbCallback;
import openchord.StringKey;
import util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

public class TestUpdateOnShrink extends TestCaseImpl{

	private static final long serialVersionUID = 1L;

	StringKey key=null;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static Logger log = Logger.getLogger(TestUpdateOnShrink.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	private static DbCallback callback= new DbCallback();

	int sleep=TesterUtil.getSleep();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	URL localURL = null;
	public TestUpdateOnShrink() {
		super();callback.setCallback(OBJECTS, log);
		// TODO Auto-generated constructor stub
	}

	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("Starting test DHT ");
	}

	@TestStep(name="action1",measure=true,step=1,timeout=10000000, place=-1)
	public void init() {
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Peer name "+this.getName());

		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS[URL.SOCKET_PROTOCOL];

		try {
			String address = InetAddress.getLocalHost().toString();
			address = address.substring(address.indexOf("/")+1,address.length());
			FreeLocalPort port= new FreeLocalPort();
			log.info("Address: "+address+" on port "+port.getPort());
			localURL = new URL(protocol + "://"+address+":"+port.getPort()+"/");
		} catch (MalformedURLException e){
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		URL bootstrapURL=null;
		try {
			bootstrapURL = new URL(protocol + "://"+TesterUtil.getBootstrap()+":"+TesterUtil.getBootstrapPort()+"/");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();
		try {
			Thread.sleep(100*this.getName());
			log.info("LocalURL: "+localURL.toString());
			chord.join(localURL,bootstrapURL);

			log.info("Joining Chord DHT: "+chord.toString());
		} catch (ServiceException e) {
			e.printStackTrace();
			log.severe("Peer init exception");
		} catch (Exception e){
			e.printStackTrace();
			log.severe("Peer init exception");
		}

		log.info("Peer init");
	}

	@TestStep(name="action2",measure=true,step=1,timeout=10000000, place=-1)
	public void find() {

		chordPrint=(ChordImpl)chord;
		try{
			Thread.sleep(sleep);
			log.info("My ID is "+chord.getID());
			String[] succ=chordPrint.printSuccessorList().split("\n");
			for (String succList : succ) {
				log.info("Successor List "+succList+" size "+succ.length );
			}

		}catch (RuntimeException e) {
			log.severe("Could not find !"+e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(name="action4",measure=true,step=1,timeout=10000000,place=-1)
	public void testLeave() {
		try {

			if(this.getName()%2==0){
				log.info("Leaving early");
				chord.leave();
				String insertValue=chord.getID().toString().substring(0,2)+" "+localURL.toString();
				this.put(this.getName(), insertValue);
				log.info("Cached "+ insertValue);
			}

			// little time to cache the variables
			Thread.sleep(sleep);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@TestStep(name="action5",measure=true,step=1,timeout=10000000,place=-1)
	public void testRetrieve() {

		if(this.getName()%2!=0){
			String[] immediateSuccessor=chordPrint.printSuccessorList().split("\n");
			String successor=null;
			for (int i = 0; i < immediateSuccessor.length; i++) {
				if(i==1)
					successor=immediateSuccessor[i].toString();
			}

			log.info("Immediate successor "+successor);

			List<String> listQuitPeers = new ArrayList<String>();
			String quitPeer;
			for(int i=0;i< TesterUtil.getExpectedPeers();i++){
				if(i%2==0){
					if(this.get(i)!=null){
						quitPeer=this.get(i).toString().trim();
						listQuitPeers.add(quitPeer);
						log.info("Quit peer "+quitPeer);
					}
				}
			}
			int timeToClean=0;
			boolean tableUpdated=false;
			while(timeToClean < TesterUtil.getLoopToFail()){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!listQuitPeers.contains(successor.trim())){
					tableUpdated=true;
					break;
				}
				timeToClean++;
			}

			if(tableUpdated){
				log.info("Contains in GV " + successor);
				assertTrue("Successor updated correctly ",true);
			}else{
				log.info("Not Contains in GV " + successor);
				assertTrue("Successor didn't updated correctly ",false);
			}
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		if(this.getName()%2!=0){
			try {
				Thread.sleep(sleep);
				chord.leave();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}

			log.info("Peer bye bye");
		}
	}

	private int getName(){
		int peerName=0;
		try {
			peerName= super.getPeerName();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return peerName;
	}

}


