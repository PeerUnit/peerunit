package stats;

import performance.SocketMonitor;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SocketStats implements
    SocketStatsMBean, SocketMonitor {
  private final AtomicLong bytesRead = new AtomicLong(0);
  private final AtomicLong bytesWritten = new AtomicLong(0);
  private final ConcurrentMap<Socket, AtomicLong>
      individualBytesRead =
        new ConcurrentHashMap<Socket, AtomicLong>();
  private final ConcurrentMap<Socket, AtomicLong>
      individualBytesWritten =
        new ConcurrentHashMap<Socket, AtomicLong>();

  SocketStats() {
  }

  public void reset() {
    bytesRead.set(0);
    bytesWritten.set(0);
    individualBytesRead.clear();
    individualBytesWritten.clear();
  }

  public long getBytesRead() {
    return bytesRead.longValue();
  }

  public long getBytesWritten() {
    return bytesWritten.longValue();
  }

  public void write(Socket socket, int data) {
    bytesWritten.incrementAndGet();
    increment(socket, 1, individualBytesWritten);
  }

  public void write(Socket socket, byte[] data, int off, int len)
      throws IOException {
    bytesWritten.addAndGet(len);
    increment(socket, len, individualBytesWritten);
  }

  private void increment(Socket socket, int bytes,
                         ConcurrentMap<Socket, AtomicLong> map) {
    AtomicLong counter = map.get(socket);
    if (counter == null) {
      counter = new AtomicLong(0);
      AtomicLong temp = map.putIfAbsent(
          socket, counter);
      if (temp != null) {
        counter = temp;
      }
    }
    counter.addAndGet(bytes);
  }

  public void read(Socket s, int data) {
    bytesRead.incrementAndGet();
    increment(s, 1, individualBytesRead);
  }

  public void read(Socket s, byte[] data, int off, int len) {
    bytesRead.addAndGet(len);
    increment(s, len, individualBytesRead);
  }

  public String getIndividualBytesWritten() {
    return convertToString(individualBytesWritten);
  }

  public String getIndividualBytesRead() {
    return convertToString(individualBytesRead);
  }

  private String convertToString(Map<?, ?> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      sb.append("\n");
      sb.append("\t\t");
      sb.append(entry.getKey());
      sb.append(" -> ");
      sb.append(entry.getValue());
    }
    return sb.toString();
  }

  public String toString() {
    return "SocketStats:\n" +
        "\tbytes read    = " + getBytesRead() + "\n" +
        "\tbytes written = " + getBytesWritten() + "\n" +
        "\tindividual bytes read    = " +
          getIndividualBytesRead() + "\n" +
        "\tindividual bytes written = " +
          getIndividualBytesWritten();
  }

  public void printStats() {
    System.out.println(this);
  }
}