package freepastry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastImpl;
import rice.pastry.NodeHandle;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.routing.RouteSet;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.pastry.testing.Ping;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.PersistentStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManagerImpl;
import fr.inria.peerunit.util.TesterUtil;
public class Peer {

	private NodeIdFactory nidFactory;

	// construct the PastryNodeFactory, this is how we use rice.pastry.socket
	private PastryNodeFactory factory;

	// This will return null if we there is no node at that location
	private NodeHandle bootHandle;

	// construct a node, passing the null boothandle on the first loop will cause the node to start its own ring
	private PastryNode node;

	public Environment env;

	private Past app;

	public PastryIdFactory localFactory;

	private static Logger log ;
	
	private List<Object> resultSet=new ArrayList<Object>();
	
	private List<Id> nullResult=new ArrayList<Id>();
	
	private List<PastContent> insertedContent=new ArrayList<PastContent>();
	
	private List<PastContent> failedContent=new ArrayList<PastContent>();
	
	private boolean bootstrapper=false;
	
	public boolean join(int bindport, InetSocketAddress bootaddress, Environment environ,Logger log , boolean bootstrapper) throws InterruptedException,IOException {
		this.bootstrapper=bootstrapper;
		return join(bindport,  bootaddress,  environ, log);
	}

		

	/**
	 * This method sets up a PastryNode.  It will bootstrap to an 
	 * existing ring if it can find one at the specified location, otherwise
	 * it will start a new ring.
	 * 
	 * @param bindport the local port to bind to 
	 * @param bootaddress the IP:port of the node to boot from
	 * @param env the environment for these nodes
	 */	
	public boolean join(int bindport, InetSocketAddress bootaddress, Environment environ,Logger log ) throws InterruptedException,IOException {
		env=environ;
		this.log=log;
		// Generate the NodeIds Randomly
		nidFactory = new RandomNodeIdFactory(env);

		// construct the PastryNodeFactory, this is how we use rice.pastry.socket
		factory = new SocketPastryNodeFactory(nidFactory, bindport, env);

		if(bootstrapper){
			bootHandle=null;
			log.log(Level.INFO,"I will create a new network");
			//	 construct a node, passing the null boothandle on the first loop will cause the node to start its own ring
			node = factory.newNode(bootHandle);
		}else{
			// This will return null if we there is no node at that location
			bootHandle = ((SocketPastryNodeFactory)factory).getNodeHandle(bootaddress);			
			log.log(Level.INFO,"Trying to join a FreePastry ring.");
			
			// construct a node, passing the null boothandle on the first loop will cause the node to start its own ring
			node = factory.newNode(bootHandle);
			
			// the node may require sending several messages to fully boot into the ring
			synchronized(node) {			
				int tryes=0;
				while(!node.isReady() && !node.joinFailed()) {
					// delay so we don't busy-wait				
						node.wait(200);				
					
					// abort if can't join
					if (node.joinFailed()) {
						log.log(Level.SEVERE,"Could not join the FreePastry ring.  Reason:"+node.joinFailedReason());
						throw new IOException("Could not join the FreePastry ring.  Reason:"+node.joinFailedReason());					
					}else  if(tryes > 300){
						return false;
					}
					tryes++;
				}
			}
		}
		
		// used for generating PastContent object Ids.
		// this implements the "hash function" for our DHT
		PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(env);
		
		// Setting log(n) replicas		
		Double replica=Math.log(TesterUtil.getExpectedPeers())/Math.log(2);
		
		//	create a different storage root for each node
		String storageDirectory = "./storage"+node.getId().hashCode();

		// create the persistent part
		Storage stor	= new PersistentStorage(idf, storageDirectory, 4 * 1024 * 1024, node.getEnvironment());
		
		app = new PastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(
				new MemoryStorage(idf), 512 * 1024, node.getEnvironment())), replica.intValue(), "");

		// We could cache the idf from whichever app we use, but it doesn't matter
		localFactory = new rice.pastry.commonapi.PastryIdFactory(env);
		log.info("Started with node id : "+node.getLocalHandle().toString());
		return true;
	}
		
	public InetSocketAddress getInetSocketAddress(InetAddress add){		
		String workStr=node.getLocalHandle().toString().substring(node.getLocalHandle().toString().lastIndexOf(":")+1, node.getLocalHandle().toString().lastIndexOf("]"));
		int port=Integer.valueOf(workStr);		
		InetSocketAddress inet= new InetSocketAddress(add,port);
		return inet;	
	}	

	public int getPort(){
		int port=0;
		String workStr=node.getLocalHandle().toString().substring(node.getLocalHandle().toString().lastIndexOf(":")+1, node.getLocalHandle().toString().lastIndexOf("]"));
		port=Integer.valueOf(workStr);
		return port;
	}

	public void insert(PastContent content){
		
		log.log(Level.INFO,"Storing content "+content.toString());
		final PastContent myContent =content;
		
		// pick a random past appl on a random node
		log.log(Level.INFO,"Inserting " + myContent + " at node "+app.getLocalNodeHandle());

		app.insert(myContent, new Continuation() {
			// the result is an Array of Booleans for each insert
			public void receiveResult(Object result) {          
				Boolean[] results = ((Boolean[]) result);
				int numSuccessfulStores = 0;
				for (int ctr = 0; ctr < results.length; ctr++) {
					if (results[ctr].booleanValue()) 
						numSuccessfulStores++;
				}
				log.log(Level.INFO,myContent + " successfully stored at " + 
						numSuccessfulStores + " locations.");
				insertedContent.add(myContent);
			}

			public void receiveException(Exception result) {
				log.log(Level.SEVERE,"Error storing "+myContent,result);
				failedContent.add(myContent);
				result.printStackTrace();
			}
		});
		
	}
	
	public void lookup( Id key){
		// wait 5 seconds
		
		// let's do the "get" operation
		log.log(Level.INFO,"Looking up ...");

		final Id lookupKey = key;
		log.log(Level.INFO,"Looking up " + lookupKey + " at node "+app.getLocalNodeHandle());
		app.lookup(lookupKey,true, new Continuation() {
			public void receiveResult(Object result) {
				log.log(Level.INFO,"Successfully looked up " + result + " for key "+lookupKey+".");
				if(result==null){
					nullResult.add(lookupKey);
				}else{
					if(!resultSet.contains(result)){
						resultSet.add(result);
					}
				}
			}

			public void receiveException(Exception result) {
				log.log(Level.SEVERE,"Error looking up "+lookupKey,result);
				result.printStackTrace();
			}
		});
	}
	
	public List<Object> getResultSet(){
		return resultSet;
	}
	public int getSizeExpected(){
		return resultSet.size();
	}	
	
	public List<PastContent> getInsertedContent(){
		return insertedContent;
	}
	
	public List<PastContent> getFailedContent(){
		return failedContent;
	}
	
	
	public void leave(){
		node.destroy();		
		env.destroy();
	}
	
	public boolean isAlive(){
		return bootHandle.isAlive();
	}
	
	public List<NodeHandle> getRoutingTable(){
		List<NodeHandle> list=new ArrayList<NodeHandle>();
		RouteSet[] routeSetVector;
		RouteSet routeSet;
		for(int i=0;i<node.getRoutingTable().numRows();i++){
			routeSetVector=node.getRoutingTable().getRow(i);
			for(int j=0;j<routeSetVector.length;j++){				
				if(routeSetVector[j]!=null){
					routeSet=routeSetVector[j];					
					for(int k=0;k<routeSet.size();k++){
						if(!list.contains(routeSet.get(k))){
							list.add(routeSet.get(k));
						}
					}
				}				
			}
		}
		return list;
	}	
	
	public void pingNodes(){
		RouteSet[] routeSetVector;
		for(int i=0;i<node.getRoutingTable().numRows();i++){
			routeSetVector=node.getRoutingTable().getRow(i);
			for(int j=0;j<routeSetVector.length;j++){				
				if(routeSetVector[j]!=null){
					routeSetVector[j].pingAllNew();	
				}
			}
		}
	}
	
	public void ping(PastryNode nd){
		Ping pg = new Ping(nd);
	}
	
	public Id getId(){
		return node.getNodeId();
	}
	
	public boolean isReady(){
		if(node.joinFailed())
			return false;
		else if(node.isReady())
			return true;
		else
			return false;
	}
	
	public List<Id> getNullResulKeys(){
		return nullResult;
	}
}
