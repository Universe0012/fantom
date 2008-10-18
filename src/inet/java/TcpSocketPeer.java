//
// Copyright (c) 2007, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   9 Feb 07  Brian Frank  Creation
//
package fan.inet;

import java.io.*;
import java.net.*;
import fan.sys.*;
import fan.sys.Thread;

public class TcpSocketPeer
  extends Socket
{

//////////////////////////////////////////////////////////////////////////
// Peer Factory
//////////////////////////////////////////////////////////////////////////

  public static TcpSocketPeer make(TcpSocket fan)
  {
    return new TcpSocketPeer();
  }

  public TcpSocketPeer()
  {
    // turn off Nagle's algorithm since we should
    // always be doing buffering in the virtual machine
    try { setTcpNoDelay(true); } catch(Exception e) {}
  }

//////////////////////////////////////////////////////////////////////////
// State
//////////////////////////////////////////////////////////////////////////

  public boolean isBound(TcpSocket fan)
  {
    return isBound();
  }

  public boolean isConnected(TcpSocket fan)
  {
    return isConnected();
  }

  public boolean isClosed(TcpSocket fan)
  {
    return isClosed();
  }

//////////////////////////////////////////////////////////////////////////
// End Points
//////////////////////////////////////////////////////////////////////////

  public IpAddress localAddress(TcpSocket fan)
  {
    if (!isBound()) return null;
    InetAddress addr = getLocalAddress();
    if (addr == null) return null;
    return IpAddressPeer.make(addr);
  }

  public Long localPort(TcpSocket fan)
  {
    if (!isBound()) return null;
    int port = getLocalPort();
    if (port <= 0) return null;
    return Long.valueOf(port);
  }

  public IpAddress remoteAddress(TcpSocket fan)
  {
    if (!isConnected()) return null;
    return remoteAddr;
  }

  public Long remotePort(TcpSocket fan)
  {
    if (!isConnected()) return null;
    return Long.valueOf(remotePort);
  }

//////////////////////////////////////////////////////////////////////////
// Communication
//////////////////////////////////////////////////////////////////////////

  public TcpSocket bind(TcpSocket fan, IpAddress addr, Long port)
  {
    try
    {
      InetAddress javaAddr = (addr == null) ? null : addr.peer.java;
      int javaPort = (port == null) ? 0 : port.intValue();
      bind(new InetSocketAddress(javaAddr, javaPort));
      return fan;
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public TcpSocket connect(TcpSocket fan, IpAddress addr, Long port, Duration timeout)
  {
    try
    {
      // connect
      int javaTimeout = (timeout == null) ? 0 : (int)timeout.millis();
      connect(new InetSocketAddress(addr.peer.java, port.intValue()), javaTimeout);
      connected(fan);
      return fan;
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  void connected(TcpSocket fan)
    throws IOException
  {
    InetSocketAddress sockAddr = (InetSocketAddress)getRemoteSocketAddress();
    this.remoteAddr = IpAddressPeer.make(sockAddr.getAddress());
    this.remotePort = sockAddr.getPort();
    this.in  = SysInStream.make(getInputStream(), getInBufferSize(fan));
    this.out = SysOutStream.make(getOutputStream(), getOutBufferSize(fan));
  }

  public InStream in(TcpSocket fan)
  {
    if (in == null) throw IOErr.make("not connected").val;
    return in;
  }

  public OutStream out(TcpSocket fan)
  {
    if (out == null) throw IOErr.make("not connected").val;
    return out;
  }

  public boolean close(TcpSocket fan)
  {
    try
    {
      close();
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  public void close()
    throws IOException
  {
    super.close();
    this.in  = null;
    this.out = null;
  }

//////////////////////////////////////////////////////////////////////////
// Threading
//////////////////////////////////////////////////////////////////////////

/* TODO - remove when I make final decision to keep TcpSocket const

  **
  ** Fork this socket onto another thread.  A new thread is automatically
  ** created with the given name (pass null to auto-generate a name).
  ** The new thread is started using the specified run method and this
  ** socket as the argument.  The run method must be a const method (it
  ** cannot capture state from the calling thread), otherwise NotImmutableErr
  ** is thrown.  Once a socket is forked onto a new thread,  it is detached
  ** from the calling thread and all methods will throw UnsupportedErr.
  **
  native Thread fork(Str threadName, |TcpSocket s->Obj| run)

  public Thread fork(TcpSocket oldSock, Str name, final Method run)
  {
    // error checking
    checkDetached();
    if (!run.isConst().val)
      throw NotImmutableErr.make("Run method not const: " + run).val;

    // increment fork counter
    int n = -1;
    synchronized (topLock) { n = forkCount++; }

    // generate name if null
    if (name == null) name = Str.make("inet.TcpSocket" + n);

    // create new detached thread-safe socket
    final TcpSocket newSock = detach(oldSock);

    // create new thread
    Thread thread = new Thread(name)
    {
      public Obj run()
      {
        return run.call1(newSock);
      }
    };

    // start thread
    return thread.start();
  }

  private TcpSocket detach(TcpSocket oldSock)
  {
    // detach old TcpSocket from this peer
    oldSock.peer = new TcpSocketPeer();
    oldSock.peer.detached = true;

    // create new thread safe TcpSocket
    final TcpSocket newSock = new TcpSocket();
    newSock.peer = this;
    return newSock;
  }

  private void checkDetached()
  {
    if (detached)
      throw UnsupportedErr.make("TcpSocket forked onto new thread").val;
  }
*/

//////////////////////////////////////////////////////////////////////////
// Streaming Options
//////////////////////////////////////////////////////////////////////////

  public Long getInBufferSize(TcpSocket fan)
  {
    return (inBufSize <= 0) ? null : Long.valueOf(inBufSize);
  }

  public void setInBufferSize(TcpSocket fan, Long v)
  {
    if (in != null) throw Err.make("Must set inBufferSize before connection").val;
    inBufSize = (v == null) ? 0 : v.intValue();
  }

  public Long getOutBufferSize(TcpSocket fan)
  {
    return (outBufSize <= 0) ? null : Long.valueOf(outBufSize);
  }

  public void setOutBufferSize(TcpSocket fan, Long v)
  {
    if (in != null) throw Err.make("Must set outBufSize before connection").val;
    outBufSize = (v == null) ? 0 : v.intValue();
  }

//////////////////////////////////////////////////////////////////////////
// Socket Options
//////////////////////////////////////////////////////////////////////////

  public SocketOptions options(TcpSocket fan)
  {
    if (options == null) options = SocketOptions.make(fan);
    return options;
  }

  public boolean getKeepAlive(TcpSocket fan)
  {
    try
    {
      return getKeepAlive();
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setKeepAlive(TcpSocket fan, boolean v)
  {
    try
    {
      setKeepAlive(v);
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public Long getReceiveBufferSize(TcpSocket fan)
  {
    try
    {
      return Long.valueOf(getReceiveBufferSize());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setReceiveBufferSize(TcpSocket fan, Long v)
  {
    try
    {
      setReceiveBufferSize(v.intValue());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public Long getSendBufferSize(TcpSocket fan)
  {
    try
    {
      return Long.valueOf(getSendBufferSize());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setSendBufferSize(TcpSocket fan, Long v)
  {
    try
    {
      setSendBufferSize(v.intValue());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public boolean getReuseAddress(TcpSocket fan)
  {
    try
    {
      return getReuseAddress();
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setReuseAddress(TcpSocket fan, boolean v)
  {
    try
    {
      setReuseAddress(v);
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public Duration getLinger(TcpSocket fan)
  {
    try
    {
      int linger = getSoLinger();
      if (linger < 0) return null;
      return Duration.makeSec(linger);
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setLinger(TcpSocket fan, Duration v)
  {
    try
    {
      if (v == null)
        setSoLinger(false, 0);
      else
        setSoLinger(true, (int)(v.sec()));
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public Duration getReceiveTimeout(TcpSocket fan)
  {
    try
    {
      int timeout = getSoTimeout();
      if (timeout <= 0) return null;
      return Duration.makeMillis(timeout);
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setReceiveTimeout(TcpSocket fan, Duration v)
  {
    try
    {
      if (v == null)
        setSoTimeout(0);
      else
        setSoTimeout((int)(v.millis()));
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public boolean getNoDelay(TcpSocket fan)
  {
    try
    {
      return getTcpNoDelay();
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setNoDelay(TcpSocket fan, boolean v)
  {
    try
    {
      setTcpNoDelay(v);
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public Long getTrafficClass(TcpSocket fan)
  {
    try
    {
      return Long.valueOf(getTrafficClass());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

  public void setTrafficClass(TcpSocket fan, Long v)
  {
    try
    {
      setTrafficClass(v.intValue());
    }
    catch (IOException e)
    {
      throw IOErr.make(e).val;
    }
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  private int inBufSize = 4096;
  private int outBufSize = 4096;
  private IpAddress remoteAddr;
  private int remotePort;
  private SysInStream in;
  private SysOutStream out;
  private SocketOptions options;

}