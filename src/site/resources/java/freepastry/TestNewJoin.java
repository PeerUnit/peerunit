package freepastry.test;
import static fr.inria.peerunit.test.assertion.Assert.fail;
import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.pastry.Id;
import rice.pastry.NodeHandle;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Network;
import freepastry.Peer;

/**
 * Test routing table update in an expanding system
 * @author almeida
 *
 */
public class TestNewJoin extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestNewJoin.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	List<Id> firstSuccessors=new ArrayList<Id>();


	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}


	@TestStep(place=-1,timeout=1000000, name = "action2", step = 0)
	public void startingHalfNet(){

		try {

			if(this.getPeerName()%2!=0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(this.getPeerName()*1000);

				if(!net.joinNetwork(peer, null,false, log)){
					inconclusive("I couldn't join, sorry");
				}
				log.info("Getting cached boot "+net.getInetSocketAddress().toString());
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testFind(){
		try {
			Thread.sleep(sleep);
			if(this.getPeerName()%2!=0){
				log.info("My ID "+peer.getId());
				for(NodeHandle nd: peer.getRoutingTable()){
					log.info("Successor NodeId "+nd.getId());
					firstSuccessors.add(nd.getNodeId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@TestStep(place=-1,timeout=1000000, name = "action5", step = 0)
	public void startingOtherHalfNet(){

		try {

			if(this.getPeerName()%2==0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(this.getPeerName()*1000);

				if(!net.joinNetwork(peer, null,false, log)){
					inconclusive("I couldn't join, sorry");
				}
				log.info("Getting cached boot "+net.getInetSocketAddress().toString());
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action7", step = 0)
	public void testFindAgain(){
		try {
			if((this.getPeerName()%2!=0)){
				List<NodeHandle> actuals;

				//Iterations to clean the volatiles from the routing table
				int timeToUpdate=0;
				Id obj=null;
				boolean tableUpdated=false;
				while(!tableUpdated &&	timeToUpdate < TesterUtil.getLoopToFail()){
					log.info("Verifying the "+timeToUpdate+" time ");
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					actuals= peer.getRoutingTable();

					for(NodeHandle nd: actuals){
						obj=nd.getNodeId();
						log.info(" Successor NodeId "+obj);
						if(!firstSuccessors.contains(obj)){
							log.info("List updated, the verdict may be PASS ");
							tableUpdated=true;
						}
					}

					//Demanding the routing table update
					peer.pingNodes();

					timeToUpdate++;
				}
				if(!tableUpdated)
					fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
				else
					log.info("List updated, the verdict may be PASS. Table updated "+timeToUpdate+" times.");

			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}
}

