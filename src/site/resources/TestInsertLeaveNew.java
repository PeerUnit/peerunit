package freepastry.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import util.FreeLocalPort;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Network;
import freepastry.Peer;


/**
 * Test Insert/Retrieve in a Shrinking System 
 * @author almeida
 *
 */
public class TestInsertLeaveNew  extends TesterImpl{
	private static Logger log = Logger.getLogger(TestInsertLeaveNew.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	static TestInsertLeaveNew test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	List<Id> firstSuccessors=new ArrayList<Id>();

	int churnPercentage=TesterUtil.getChurnPercentage();

	Map<Integer,Object> objList=new HashMap<Integer, Object>();
	
	List<String> expecteds= new ArrayList<String>();
	
	List<PastContent> keySet;

	public static void main(String[] str) {		
		test = new TestInsertLeaveNew();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestInsertLeave.log.peer"+test.getPeerName(),true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		test.run();
	}
	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}

	@TestStep(place=-1,timeout=1000000, name = "action1", step = 0)
	public void startingNetwork(){
		try {	

			if(test.getPeerName()==0){
				Network net= new Network();
				if(!net.joinNetwork(peer, null, true, log)){
					inconclusive("I couldn't become a boostrapper, sorry");
				}
				
				test.put(-10,net.getInetSocketAddress());
				log.info("Net created");
				
				while(!peer.isReady())
					Thread.sleep(16000);
			}
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
			test.put(intObj.intValue()*100, intObj);
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action3", step = 0)
	public void startingInitNet(){	

		try {			
			// Wait a while due to the bootstrapper performance
			Thread.sleep(16000);
			if(test.getPeerName()!=0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(test.getPeerName()*1000);
				
				InetSocketAddress bootaddress= (InetSocketAddress)test.get(-10);
				log.info("Getting cached boot "+bootaddress.toString());
				
				if(!net.joinNetwork(peer, bootaddress, false, log)){
					inconclusive("I couldn't join, sorry");
				}
				
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (InterruptedException e) {			
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) { 			
				e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);		
			if(test.getPeerName()==0){
				List<PastContent> resultSet=new ArrayList<PastContent>();

				// these variables are final so that the continuation can access them
				for(int i=0; i < OBJECTS ; i++){
					final String s = "test" + peer.env.getRandomSource().nextInt();

					// build the past content
					final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);

					peer.insert(myContent);
					resultSet.add(myContent);

				}
				test.put(-1, resultSet);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testRetrieve(){		
		try {
			Thread.sleep(sleep);

			// Lookup first time
			keySet=(List<PastContent>)test.get(-1);				
			Id contentKey;
			for (PastContent key : keySet) {
				contentKey=key.getId();
				if(contentKey!=null){
					log.info("[PastryTest] Lookup Expected "+contentKey.toString());
					peer.lookup(contentKey);				
				}
			}

			// Sleep 
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
				e.printStackTrace();		
			}				
			
			log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

			for (Object expected : peer.getResultSet()) {			
				if(expected!=null){
					log.info("Retrieve before depart "+expected.toString());
					//expecteds.add(expected.toString());	
				}		
			}
			/*if(test.getPeerName()==0){
				test.put(2, expecteds);
			}*/

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action6", step = 0)
	public void leaving(){	
		try{
			if(chosenOne(test.getPeerName())){
				log.info("Leaving early ");
				test.kill();				
			}			
		} catch (RemoteException e) {			
			e.printStackTrace();
		}		
	}
	
	@TestStep(place=-1,timeout=1000000, name = "action7", step = 0)
	public void testInitialRetrieve(){		
		try {
			if(!chosenOne(test.getPeerName())){
				List<String> actuals=new ArrayList<String>();
				Thread.sleep(sleep);				
				Id contentKey;
				for (PastContent key : keySet) {
					contentKey=key.getId();
					if(contentKey!=null){
						log.info("[PastryTest] Lookup Expected "+contentKey.toString());
						peer.lookup(contentKey);				
					}
				}

				// Sleep 
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
					e.printStackTrace();		
				}				
				
				log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

				for (Object actual : peer.getResultSet()) {			
					if(actual!=null){
						log.info("Retrieve before depart "+actual.toString());
						actuals.add(actual.toString());			
						test.put(test.getPeerName(), actuals);
						
					}		
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@TestStep(place=-1,timeout=1000000, name = "action8", step = 0)
	public void buildExpecteds(){	
		try {
			Set<Integer> newKeySet=test.getCollection().keySet();
			List<String> cached=new ArrayList<String>();
			Object obj;
			for(Integer key: newKeySet){
				obj=test.get(key);
				if ((key.intValue() >= 0 )&&(key.intValue() < 64)) {
					cached=(List<String>) obj;					
				}
				
				for(String cachedObj:cached){					
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
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action9", step = 0)
	public void testRetrieveByOthers(){		
		try {			
			if(!chosenOne(test.getPeerName())){
				Thread.sleep(sleep);

				// Lookup first time

				Id contentKey;
				for (PastContent key : keySet) {
					contentKey=key.getId();
					if(contentKey!=null){
						log.info("[PastryTest] Lookup Expected "+contentKey.toString());
						peer.lookup(contentKey);				
					}
				}

				Thread.sleep(sleep);

				log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

				List<String> actuals= new ArrayList<String>();
				int timeToFind=0;			
				while(timeToFind < TesterUtil.getLoopToFail()){
					log.info("Retrieval "+timeToFind);
					for (Object actual : peer.getResultSet()) {
						if(actual!=null){
							log.info("[Local verdict] Actual "+actual.toString());

							if(!actuals.contains(actual.toString())){
								actuals.add(actual.toString());
							}
						}		
					}
					peer.pingNodes();
					Thread.sleep(sleep);
					timeToFind++;
				}

				//List<String> expecteds=(List<String>)test.get(2);		
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
		log.info("[PastryTest] Peer bye bye");
	}
	private boolean chosenOne(int name){		
		if(((name % 2) ==0)&&(name!=0)){
			return true;
		}
		else{
			return false;
		}
	}
}
