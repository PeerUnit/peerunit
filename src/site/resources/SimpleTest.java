package freepastry.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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
import freepastry.Peer;
import freepastry.test.old.TestInsertLeaveB;

public class SimpleTest extends TesterImpl{
	// logger from jdk
	private static Logger log = Logger.getLogger(TestInsertLeaveB.class.getName());
	private static SimpleTest test;
	// Freepastry peer
	Peer peer = new Peer();

	public static void main(String[] str) {		
		test = new SimpleTest();
		// Export this class to be parsed
		test.export(test.getClass());

		// Log creation
		FileHandler handler;
		try {			
			handler = new FileHandler(TesterUtil.getLogfolder()+"/SimpleTest.log.peer"+test.getPeerName(),true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		// Execute the test
		test.run();
	}
	/**
	 * This method starts the test
	 */	
	@BeforeClass(place=-1,timeout=100)
	public void begin(){
		log.info("Starting the test ");
	}

        @TestStep(place=-1,timeout=1000000, name = "action1", step = 1)
	public void startingNetwork(){
		try {

			log.info("Joining in first");
			Network net= new Network();
			Thread.sleep(this.getPeerName()*1000);

			if(!net.joinNetwork(peer, null,false, log)){
				inconclusive("I couldn't join, sorry");
			}
			log.info("Getting bootstrapper "+net.getInetSocketAddress().toString());
			log.info("Running on port "+peer.getPort());
			log.info("Time to bootstrap");
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Stabilize the network. 
	 */
	@TestStep(place=-1,timeout=1000000, name = "action2", step = 0)
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
	/**
	 * Put some data and store in test variables.
	 */
	@TestStep(place=0,timeout=1000000, name = "action3", step = 0)
	public void put(){
		for(int i=0; i < 2 ; i++){
			// build the past content
			final String s = "test" + peer.env.getRandomSource().nextInt();
			final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
			peer.insert(myContent);		
		}

		// Wait until all the insert ends since it is asynchronous
		while((peer.getFailedContent().size()+peer.getInsertedContent().size())<2){
			log.info("Inserted so far : "+peer.getInsertedContent().size());
			log.info("Failed so far : "+peer.getFailedContent().size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}			

		List<PastContent> expecteds= new ArrayList<PastContent>();
		for (PastContent content : peer.getInsertedContent()) {
			log.info("Expected so far : "+content.toString());
			expecteds.add(content);
		}

		// Use a test variable to store the expected data
		test.put(1, expecteds);	
	}
	/**
	 * Get the data and the verdict.
	 */
	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void get(){
		// Lookup 
		List<PastContent> expectedContent=(List<PastContent>)test.get(1);			
		Id contentKey;			

		// Get the keys to lookup for data			
		for (PastContent key : expectedContent) {
			contentKey=key.getId();
			if(contentKey!=null){
				log.info("Lookup Expected "+contentKey.toString());
				peer.lookup(contentKey);				
			}
		}

		// Wait a little while for a response
		try {
			Thread.sleep(16000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// A list to store the retrieved data
		List<String> actuals= new ArrayList<String>();
		for (Object actual : peer.getResultSet()) {
			if(actual!=null){
				if(!actuals.contains(actual.toString())){
					log.info("[Local verdict] Actual "+actual.toString());
					actuals.add(actual.toString());
				}
			}		
		}										

		// Generating the expecteds list
		List<String> expecteds=new ArrayList<String>();
		for(PastContent expected : expectedContent){
			expecteds.add(expected.toString());
		}

		// Assigning a verdict
		log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
		Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);	
	}

	/**
	 * This method finishes the test 
	 *
	 */
	@AfterClass(timeout=100,place=-1)
	public void end() {		
		log.info("Peer bye bye");
	}
}

