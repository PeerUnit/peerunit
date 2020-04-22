import passpartout.impl.TesterImpl;
import passpartout.parser.impl.AfterClass;
import passpartout.parser.impl.BeforeClass;
import passpartout.parser.impl.Test;

public class Example extends TesterImpl{
    static Example test;
   
    // Data to exercise a DHT
    String expected="fourteen";
    int expectedKey=14;
   
    String actual;

    public static void main(String[] args) {
       
        // Instantiate this class
        test = new Example();
       
        // Export in order to parse the test methods
        test.export(test.getClass());       
       
        // Run the test itself
        test.run();
    }
   
    @BeforeClass(place=-1,timeout=100)
    public void start(){       
        // Pseudocode to instantiate a peer 
        Peer peer=new Peer();
    }

    @TestStep(from=0,to=2,timeout=100, name = "action1", step = 0)
    public void join(){       
        // Let's join the system
        peer.join();
    }
   
    @TestStep(place=2,timeout=100, name = "action2", step = 0)
    public void put(){       
        // Put data
        peer.put(expectedKey,expected);
    }
   
    @TestStep(from=3,to=4,timeout=100, name = "action3", step = 0)
    public void joinOthers(){
        // The rest of the peers join the system
        peer.join();
    }
   
    @TestStep(from=3,to=4,timeout=100, name = "action4", step = 0)
    public void retrieve(){
        // Retrieving the inserted data
        actual=peer.get(expectedKey);       
    }
   
    @TestStep(from=3,to=4,timeout=100, name = "action5", step = 0)
    public void assertRetrieve(){
        // Let's see if we got the expected data
        assertEquals(expected, actual);
    }
   
    @AfterClass(place=-1,timeout=100)
    public void stop(){
        peer.leave();
    }
}
