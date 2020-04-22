package freepastry.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
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
 * Test routing table update in a shrinking system
 * @author almeida
 *
 */
public class TestUpdateOnShrink  extends TestCaseImpl {
	private static Logger log = Logger.getLogger(TestUpdateOnShrink.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}
	@TestStep(place=-1,timeout=1000000, name = "action1", step = 0)
	public void startingNetwork(){
		try {

			log.info("Joining in first");
			Network net= new Network();
			Thread.sleep(this.getPeerName()*1000);


			if(!net.joinNetwork(peer, null,false, log)){
				inconclusive("I couldn't join, sorry");
			}
			log.info("Getting cached boot "+net.getInetSocketAddress().toString());
			log.info("Running on port "+peer.getPort());
			log.info("Time to bootstrap");
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testFind(){
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("My ID "+peer.getId());
		for(NodeHandle nd: peer.getRoutingTable()){
			log.info("Successor NodeId "+nd.getId());
		}
	}
	@TestStep(name="action5",measure=true,step=0,timeout=10000000,place=-1)
	public void testLeave() {
		try {
			Thread.sleep(sleep);
			if(this.getPeerName()%2!=0){
				this.put(this.getPeerName(), peer.getId());
				log.info("Leaving early");
				this.kill();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action6", step = 0)
	public void testFindAgain(){
		try {
			if(super.getPeerName()%2==0){
				List<Id> volatiles=new ArrayList<Id>();

				Set<Integer> keySet=this.getCollection().keySet();
				Object gotValue;
				for(Integer i: keySet){
					gotValue=this.get(i);
					if(gotValue instanceof Id) {
						volatiles.add((Id)gotValue);
						log.info("Volatiles NodeId "+this.get(i));
					}
				}

				List<NodeHandle> actuals;

				//Lists to store the volatiles after the routing table update
				List<Id>  volatilesInTable= new ArrayList<Id>();
				List<Id>  previousVolatilesInTable= new ArrayList<Id>();

				//Iterations to clean the volatiles from the routing table
				int timeToClean=TesterUtil.getLoopToFail();
				Id obj=null;
				boolean tableUpdated=false;
				while((timeToClean > 0)&&(!tableUpdated)){
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					actuals= peer.getRoutingTable();

					for(NodeHandle nd: actuals){
						obj=nd.getNodeId();
						log.info(" Successor NodeId "+obj+" is volatile "+volatiles.contains(obj));
						if(volatiles.contains(obj)){
							volatilesInTable.add(obj);
						}
					}

					//Comparing both lists
					if(!previousVolatilesInTable.isEmpty()){
						for(Id id : previousVolatilesInTable){
							log.info("Previous NodeId "+id.toString());
							if(!volatilesInTable.contains(id)){
								log.info("Do not contains "+id.toString());
								tableUpdated=true;
							}
						}
					}

					//Charging the previous list
					previousVolatilesInTable.clear();
					for(Id id : volatilesInTable){
						previousVolatilesInTable.add(id);
					}

					log.info("In "+timeToClean+" contains "+volatilesInTable.size()+" on "+actuals.size());
					//	Cleaning the actual list
					volatilesInTable.clear();

					//Demanding the routing table update
					peer.pingNodes();

					timeToClean--;
				}//while
				if(!tableUpdated)
					inconclusive("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@TestStep(place=-1,timeout=1000000, name = "action7", step = 0)
	public void getHandle(){
		List<PastContent> cont=peer.getInsertedContent();
		PastContentHandle pch;
		for(PastContent pc: cont){
			pch=pc.getHandle(peer.getPast());
			System.out.println("NodeHandle "+pch.getNodeHandle());
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}
}
