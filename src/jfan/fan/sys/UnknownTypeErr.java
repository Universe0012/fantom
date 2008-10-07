//
// Copyright (c) 2006, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   4 Jan 06  Brian Frank  Creation
//
package fan.sys;

/**
 * UnknownTypeErr
 */
public class UnknownTypeErr
  extends Err
{

//////////////////////////////////////////////////////////////////////////
// Fan Constructors
//////////////////////////////////////////////////////////////////////////

  public static UnknownTypeErr make() { return make((String)null, (Err)null); }
  public static UnknownTypeErr make(String msg) { return make(msg, (Err)null); }
  public static UnknownTypeErr make(String msg, Err cause)
  {
    UnknownTypeErr err = new UnknownTypeErr();
    make$(err, msg, cause);
    return err;
  }

  public static void make$(UnknownTypeErr self) { make$(self, null);  }
  public static void make$(UnknownTypeErr self, String msg) { make$(self, msg, null); }
  public static void make$(UnknownTypeErr self, String msg, Err cause) { Err.make$(self, msg, cause); }

//////////////////////////////////////////////////////////////////////////
// Java Constructors
//////////////////////////////////////////////////////////////////////////

  public UnknownTypeErr(Err.Val val) { super(val); }
  public UnknownTypeErr() { super(new UnknownTypeErr.Val()); }

//////////////////////////////////////////////////////////////////////////
// Identity
//////////////////////////////////////////////////////////////////////////

  public Type type() { return Sys.UnknownTypeErrType; }

//////////////////////////////////////////////////////////////////////////
// Val - Java Exception Type
//////////////////////////////////////////////////////////////////////////

  public static class Val extends Err.Val {}

}
