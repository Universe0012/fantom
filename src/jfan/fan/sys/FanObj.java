//
// Copyright (c) 2006, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   2 Dec 05  Brian Frank  Creation
//
package fan.sys;

import java.math.*;
import fanx.util.*;

/**
 * FanObj is the root class of all classes in Fan - it is the class
 * representation of Obj.
 */
public class FanObj
{

//////////////////////////////////////////////////////////////////////////
// Java
//////////////////////////////////////////////////////////////////////////

  public int hashCode()
  {
    long hash = hash();
    return (int)(hash ^ (hash >>> 32));
  }

  public final boolean equals(Object obj)
  {
    return _equals(obj);
  }

  public final String toString()
  {
    return toStr();
  }

//////////////////////////////////////////////////////////////////////////
// Identity
//////////////////////////////////////////////////////////////////////////

  public static boolean equals(Object self, Object x)
  {
    if (self instanceof FanObj)
      return ((FanObj)self)._equals(x);
    else
      return self.equals(x);
  }

  public boolean _equals(Object obj)
  {
    return this == obj;
  }

  public static Long compare(Object self, Object x)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).compare(x);
    else if (self instanceof Comparable)
      return Long.valueOf(((Comparable)self).compareTo(x));
    else
      return FanStr.compare(toStr(self), toStr(x));
  }

  public Long compare(Object obj)
  {
    return FanStr.compare(toStr(), toStr(obj));
  }

  public static Long hash(Object self)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).hash();
    else
      return Long.valueOf(self.hashCode());
  }

  public Long hash()
  {
    return Long.valueOf(super.hashCode());
  }

  public static String toStr(Object self)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).toStr();
    else
      return self.toString();
  }

  public String toStr()
  {
    return super.toString();
  }

  public static boolean isImmutable(Object self)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).isImmutable();
    else
      return FanUtil.isJavaImmutable(self.getClass());
  }

  public boolean isImmutable()
  {
    return type().isConst();
  }

  public static Type type(Object self)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).type();
    else
      return FanUtil.toFanType(self.getClass(), true);
  }

  public Type type()
  {
    return Sys.ObjType;
  }

  public static Object trap(Object self, String name, List args)
  {
    if (self instanceof FanObj)
      return ((FanObj)self).trap(name, args);
    else
      return doTrap(self, name, args, type(self));
  }

  public Object trap(String name, List args) { return doTrap(this, name, args, type()); }

  private static Object doTrap(Object self, String name, List args, Type type)
  {
    Slot slot = type.slot(name, true);
    if (slot instanceof Method)
    {
      Method m = (Method)slot;
      return m.func.callOn(self, args);
    }
    else
    {
      Field f = (Field)slot;
      int argSize = (args == null) ? 0 : args.sz();
      if (argSize == 0)
      {
        return f.get(self);
      }

      if (argSize == 1)
      {
        Object val = args.get(0);
        f.set(self, val);
        return val;
      }

      throw ArgErr.make("Invalid number of args to get or set field '" + name + "'").val;
    }
  }

//////////////////////////////////////////////////////////////////////////
// Utils
//////////////////////////////////////////////////////////////////////////

  public static void echo(Object obj)
  {
    System.out.println(obj);
  }

}
