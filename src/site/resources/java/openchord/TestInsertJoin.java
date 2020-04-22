package openchord.test;

import java.net.InetAddress;
/**
 * Test Insert/Retrieve in an Expanding System
 * @author almeida
 *
 */
public class TestInsertLeave extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestInsertLeave.class.getName());
	private static final int OBJECTS=TesterUtil.getObjects();

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

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

	Map<Integer,Object> objList=new HashMap<Integer, Object>();

	List<String> expecteds=new ArrayList<String>();

	public TestInsertLeave() {
		super();callback.setCallback(OBJECTS, log);
	}


	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("Starting test DHT ");
	}

	@TestStep(name="action1",measure=true,step=1,timeout=10000000, place=-1)
	public void init() {
		try{


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

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=0,timeout=1000000, name = "action2", step = 0)
	public void chosingPeer(){
		Random rand=new Random();
		List<Integer> generated=new ArrayList<Integer>();
		int chosePeer;
		int netSize= (TesterUtil.getExpectedPeers()*TesterUtil.getChurnPercentage())/100;
		log.info("It will join "+netSize+" peers");
		boolean peerChose;
		while(netSize >0){
			peerChose=false;
			while(!peerChose){
				chosePeer=rand.nextInt(TesterUtil.getExpectedPeers());
				if(chosePeer!=0){
					Integer genInt=new Integer(chosePeer);
					if(!generated.contains(genInt)){
						generated.add(genInt);
						peerChose=true;
						log.info("Chose peer "+genInt);
					}
				}
			}
			netSize--;
		}
		for(Integer intObj: generated){
			this.put(intObj.intValue()*100, intObj);
		}
	}

	@TestStep(place=0,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < OBJECTS; i++) {
			data = "" +i;
			log.info("[TestDbpartout] Inserting data "+data);
			key=new StringKey(data);
			chord.insert(key,data,callback);
			insertedKeys.add(key);
		}

	}

	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){

		try {
			Thread.sleep(sleep);
			if(!chosenOne(this.getPeerName())){
				for (int i = 0; i < OBJECTS; i++) {
					data = ""+ i;
					key=new StringKey(data);
					chord.retrieve(key,callback);
				}
				callback.retr ++;
				Thread.sleep(sleep);
				for (String actual : callback.getResultSet()) {
					log.info("Retrieve before depart "+actual);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(name="action5",measure=true,step=1,timeout=10000000, place=-1)
	public void leaving() {
		try{
			if(chosenOne(this.getPeerName())){
				log.info("Leaving early ");
				this.kill();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action6", step = 0)
	public void testInitialRetrieve(){

		try {
			if(!chosenOne(this.getPeerName())){
				List<String> actuals=new ArrayList<String>();
				Thread.sleep(sleep);

				for (int i = 0; i < OBJECTS; i++) {
					data = ""+ i;
					key=new StringKey(data);
					chord.retrieve(key,callback);
				}
				callback.retr ++;
				Thread.sleep(sleep);
				for (String actual : callback.getResultSet()) {
					log.info("Retrieve after depart "+actual);
					actuals.add(actual);
					this.put(this.getPeerName(), actuals);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action7", step = 0)
	public void buildExpecteds(){
		try {
			Set<Integer> keySet=this.getCollection().keySet();
			List<String> cached=new ArrayList<String>();
			Object obj;
			for(Integer key: keySet){
				obj=this.get(key);
				if (!(obj instanceof Integer)) {
					cached=(List<String>) obj;
				}

				for(String cachedObj:cached){
					log.info("Cached "+cached);
					if(!expecteds.contains(cachedObj)){
						expecteds.add(cachedObj);
					}
				}
			}
			log.info("I may find "+expecteds.size()+" objects");
			for(String exp:expecteds){
				log.info("I may find "+exp);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action8", step = 0)
	public void testFinalRetrieve(){

		try {
			if(!chosenOne(this.getPeerName())){
				Thread.sleep(sleep);
				List<String> actuals=new ArrayList<String>();
				int timeToFind=0;
				while(timeToFind < TesterUtil.getLoopToFail()){
					for (int i = 0; i < OBJECTS; i++) {
						data = ""+ i;
						key=new StringKey(data);
						chord.retrieve(key,callback);
					}
					callback.retr ++;
					Thread.sleep(sleep);
					for (String actual : callback.getResultSet()) {
						log.info("Final retrieve "+timeToFind+" got "+actual);
						if(!actuals.contains(actual.toString())){
							actuals.add(actual);
						}else{
							log.info("Already have "+actual);
						}
					}
					timeToFind++;
				}
				//List<String> expecteds=(List<String>)test.get(0);
				log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
				Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
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
	private boolean chosenOne(int name){
		try {
			if(objList.isEmpty()){
				objList=this.getCollection();
			}
			Set<Integer> keySet=objList.keySet();
			Object nameChose;
			for(Integer key: keySet){
				nameChose=objList.get(key);
				if (nameChose instanceof Integer) {
					Integer new_name = (Integer) nameChose;
					if(new_name.intValue()==name){
						return true;
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
}

