package stats;

import performance.SocketMonitor;
import java.net.*;

public interface SocketStatsMBean {
  void printStats();
  void reset();
  long getBytesRead();
  long getBytesWritten();
  String getIndividualBytesWritten();
  String getIndividualBytesRead();
}
  