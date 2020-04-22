package freepastry.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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
 * Test Insert and retrieve on a stable system
 * @author almeida
 *
 */
public class TestInsertStableNew  extends TesterImpl{
	private static Logger log = Logger.getLogger(TestInsertStableNew.class.getName());

	static TestInsertStableNew test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();
	
	List<String> expecteds= new ArrayList<String>();

	public static void main(String[] str) {		
		test = new TestInsertStableNew();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestInsertStable.log.peer"+test.getPeerName(),true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
			Thread.sleep(2000);
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		test.run();
	}
	@BeforeClass(place=-1,timeout=1000000)
	public void start(){
		log.info("Starting test peer  ");
	}

	@TestStep(place=-1,timeout=1000000, name = "action1", step = 0)
	public void startingNetwork(){
		try {
			if(test.getPeerName()==0){
				Network net= new Network();
				if(!net.joinNetwork(peer, null, true, log)){
					inconclusive("I couldn't become a boostrapper, sorry");
				}
				
				test.put(-1,net.getInetSocketAddress());
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
	
	@TestStep(place=-1,timeout=1000000, name = "action2", step = 0)
	public void joiningNet(){	

		try {			
			// Wait a while due to the bootstrapper performance
			Thread.sleep(16000);
			if(test.getPeerName()!=0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(test.getPeerName()*1000);
				
				InetSocketAddress bootaddress= (InetSocketAddress)test.get(-1);
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
	
	/**
	 * Stabilize the network. 
	 */
	@TestStep(place=-1,timeout=1000000, name = "action3", step = 0)
	public void stabilize(){
		for (int i = 0; i < 4; i++) {
			try{	
				// Force the routing table update
				peer.pingNodes();
				Thread.sleep(16000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}
	}	
	
	@TestStep(place=-1,timeout=10000000, name = "action4", step = 0)
	public void testInsert(){
		try {			
			if(test.getPeerName()==0){
				log.info("I will insert");
				List<PastContent> resultSet=new ArrayList<PastContent>();
				// these variables are final so that the continuation can access them
				for(int i=0; i < TesterUtil.getObjects() ; i++){
					final String s = "test" + peer.env.getRandomSource().nextInt();
						// build the past content
					final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
	
					peer.insert(myContent);
					resultSet.add(myContent);
					Thread.sleep(150);		
				}
				test.put(0, resultSet);

				log.info("Inserted "+resultSet.size());
			}			
			
			Thread.sleep(sleep*3);		
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}


	@TestStep(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testRetrieve(){		
		// Get inserted data
		List<PastContent> cached=(List<PastContent>)test.get(0);
		
		// build the expecteds
		for(PastContent cachedObj:cached){					
			if(!expecteds.contains(cachedObj.toString())){
				expecteds.add(cachedObj.toString());
			}							
		}
		log.info("I may find "+expecteds.size()+" objects");
	
		// Lookup for data
		try {
			Thread.sleep(16000);
			String content;
			List<String> actuals= new ArrayList<String>();
			
			int timeToFind=0;			
			while(timeToFind < TesterUtil.getLoopToFail()){
				for(PastContent p: cached){					
					peer.lookup(p.getId());
				}	
				
				Thread.sleep(sleep);
				
				log.info("Retrieval "+timeToFind);
				for (Object actual : peer.getResultSet()) {
					if(actual!=null){
						if(!actuals.contains(actual.toString())){
							actuals.add(actual.toString());
						}
					}		
				}
				peer.pingNodes();
				Thread.sleep(sleep);
				timeToFind++;
			}
			log.info("Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
			Assert.assertListEquals("[Local verdict] ",expecteds, actuals);	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("Peer bye bye");
	}
}
