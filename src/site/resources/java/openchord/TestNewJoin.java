package openchord.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

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

/**
 * Test routing table update in an expanding system
 * @author almeida
 *
 */
public class TestNewJoin extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestNewJoin.class.getName());
	private static final int OBJECTS=TesterUtil.getObjects();

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	List<String> routingTable=new ArrayList<String>();

	private static final long serialVersionUID = 1L;

	StringKey key=null;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static DbCallback callback= new DbCallback();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	URL localURL = null;


	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("Starting test DHT ");
	}

	@TestStep(name="action1",measure=true,step=1,timeout=10000000, place=-1)
	public void init() {
		try{
			if(this.getPeerName()%2!=0){

				Thread.sleep(sleep);
				log.info("Peer name "+this.getPeerName());


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
					Thread.sleep(100*this.getPeerName());
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(name="action2",measure=true,step=1,timeout=10000000, place=-1)
	public void routingTable() {

		try{
			if(this.getPeerName()%2!=0){
				chordPrint=(ChordImpl)chord;
				Thread.sleep(sleep);
				log.info("My ID is "+chord.getID());
				String[] succ=chordPrint.printSuccessorList().split("\n");
				String successor=null;
				for (int i = 0; i < succ.length; i++) {
					if(i>0){
						successor=succ[i].toString().trim();
						log.info("Successor List "+successor);
						routingTable.add(successor);
					}
				}
			}
		}catch (RuntimeException e) {
			log.severe("Could not find !"+e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(name="action3",measure=true,step=1,timeout=10000000, place=-1)
	public void initOtherHalf() {
		try{
			if(this.getPeerName()%2==0){

				Thread.sleep(sleep);
				log.info("Peer name "+this.getPeerName());


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
					Thread.sleep(100*this.getPeerName());
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@TestStep(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testFindAgain(){
		try {
			if(this.getPeerName()%2!=0){

				String[] succ=chordPrint.printSuccessorList().split("\n");

				String successor=null;
				int timeToUpdate=0;
				boolean tableUpdated=false;
				while(!tableUpdated &&	timeToUpdate < TesterUtil.getLoopToFail()){
					for (int i = 0; i < succ.length; i++) {
						if(i>0){
							successor=succ[i].toString().trim();
							log.info("New Successor List "+successor);
							if(!routingTable.contains(successor)){
								tableUpdated=true;
								break;
							}
						}
					}
					Thread.sleep(1000);
					timeToUpdate++;
				}
				if(!tableUpdated)
					inconclusive("Routing Table wasn't updated. Increase qty of loops.");
				else
					log.info("List updated, the verdict may be PASS. Table updated "+timeToUpdate+" times.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info(" Peer bye bye");
	}
}

