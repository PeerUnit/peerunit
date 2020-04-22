package performance;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

import util.Delegator;

public class MonitoringSocketImpl extends SocketImpl {
  private final Delegator delegator;

  public MonitoringSocketImpl() throws IOException {
    this.delegator = new Delegator(this, SocketImpl.class,
        "java.net.SocksSocketImpl");
  }

  private Socket getSocket() throws IOException {
    try {
       Field socket = SocketImpl.class.getDeclaredField("socket");
       socket.setAccessible(true);
       return (Socket) socket.get(this);
    } catch (Exception e) {
      throw new IOException("Could not discover real socket");
    }
  }

  public InputStream getInputStream() throws IOException {
    InputStream real = delegator.invoke();
    return new SocketMonitoringInputStream(getSocket(), real);
  }

  public OutputStream getOutputStream() throws IOException {
    OutputStream real = delegator.invoke();
    return new SocketMonitoringOutputStream(getSocket(), real);
  }

  // the rest of the class is plain delegation to real SocketImpl
  public void create(boolean stream) throws IOException {
    delegator.invoke(stream);
  }

  public void connect(String host, int port)
      throws IOException {
    delegator.invoke(host, port);
  }

  // We specify the exact method to delegate to.  Not actually
  // necessary here, but just to show how you would do it.
  public void connect(InetAddress address, int port)
      throws IOException {
    delegator
        .delegateTo("connect", InetAddress.class, int.class)
        .invoke(address, port);
  }

  public void connect(SocketAddress address, int timeout)
      throws IOException {
    delegator.invoke(address, timeout);
  }

  public void bind(InetAddress host, int port)
      throws IOException {
    delegator.invoke(host, port);
  }

  public void listen(int backlog) throws IOException {
    delegator.invoke(backlog);
  }

  public void accept(SocketImpl s) throws IOException {
    delegator.invoke(s);
  }

  public int available() throws IOException {
    Integer result = delegator.invoke();
    return result;
  }

  public void close() throws IOException {
    delegator.invoke();
  }

  public void shutdownInput() throws IOException {
    delegator.invoke();
  }

  public void shutdownOutput() throws IOException {
    delegator.invoke();
  }

  public FileDescriptor getFileDescriptor() {
    return delegator.invoke();
  }

  public InetAddress getInetAddress() {
    return delegator.invoke();
  }

  public int getPort() {
    Integer result = delegator.invoke();
    return result;
  }

  public boolean supportsUrgentData() {
    Boolean result = delegator.invoke();
    return result;
  }

  public void sendUrgentData(int data) throws IOException {
    delegator.invoke(data);
  }

  public int getLocalPort() {
    Integer result = delegator.invoke();
    return result;
  }

  public String toString() {
    return delegator.invoke();
  }

  public void setPerformancePreferences(int connectionTime,
                                        int latency,
                                        int bandwidth) {
    delegator.invoke(connectionTime, latency, bandwidth);
  }

  public void setOption(int optID, Object value)
      throws SocketException {
    delegator.invoke(optID, value);
  }

  public Object getOption(int optID) throws SocketException {
    return delegator.invoke(optID);
  }
}