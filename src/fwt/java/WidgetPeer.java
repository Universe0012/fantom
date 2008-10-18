//
// Copyright (c) 2008, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   12 Jun 08  Brian Frank  Creation
//
package fan.fwt;

import java.lang.reflect.Field;
import java.util.HashMap;
import fan.sys.*;
import fan.sys.List;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;

/**
 * Native methods for Widget
 */
public class WidgetPeer
  implements PaintListener, KeyListener, FocusListener,
             MouseListener, MouseMoveListener, MouseTrackListener, MouseWheelListener,
             DisposeListener
{

//////////////////////////////////////////////////////////////////////////
// Constructor
//////////////////////////////////////////////////////////////////////////

  public static WidgetPeer make(fan.fwt.Widget self)
    throws Exception
  {
    WidgetPeer peer = new WidgetPeer();
    peer.self = self;
    return peer;
  }

//////////////////////////////////////////////////////////////////////////
// Accessors
//////////////////////////////////////////////////////////////////////////

  public fan.fwt.Widget parent()
  {
    return self.parent();
  }

  public Widget parentControl()
  {
    fan.fwt.Widget p = self.parent();
    return p == null ? null : p.peer.control;
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  // Bool enabled := true
  public boolean enabled(fan.fwt.Widget self) { return enabled.get(); }
  public void enabled(fan.fwt.Widget self, boolean v) { enabled.set(v); }
  public final Prop.BoolProp enabled = new Prop.BoolProp(this, true)
  {
    public boolean get(Widget w)
    {
      // pain in the ass due to SWT's class hierarchy
      if (control instanceof Control)  return ((Control)control).getEnabled();
      if (control instanceof MenuItem) return ((MenuItem)control).getEnabled();
      if (control instanceof ToolItem) return ((ToolItem)control).getEnabled();
      return true;
    }
    public void set(Widget w, boolean v)
    {
      // pain in the ass due to SWT's class hierarchy
      if (control instanceof Control)  ((Control)control).setEnabled(v);
      if (control instanceof MenuItem) ((MenuItem)control).setEnabled(v);
      if (control instanceof ToolItem) ((ToolItem)control).setEnabled(v);
    }
  };

  // Bool visible := true
  public boolean visible(fan.fwt.Widget self) { return visible.get(); }
  public void visible(fan.fwt.Widget self, boolean v) { visible.set(v); }
  public final Prop.BoolProp visible = new Prop.BoolProp(this, true)
  {
    public boolean get(Widget w)
    {
      return (w instanceof Control) ? ((Control)w).getVisible() : true;
    }
    public void set(Widget w, boolean v)
    {
      // shell always controls its own visibility via open/close
      if (w instanceof Control && !(w instanceof Shell))
        ((Control)w).setVisible(v);
    }
  };

  // Size size
  public fan.fwt.Point pos(fan.fwt.Widget self) { return pos.get(); }
  public void pos(fan.fwt.Widget self, fan.fwt.Point v) { pos.set(v); onPosChange(); }
  public final Prop.PosProp pos = new Prop.PosProp(this);

  // Size size
  public Size size(fan.fwt.Widget self) { return size.get(); }
  public void size(fan.fwt.Widget self, Size v) { size.set(v); onSizeChange(); }
  public final Prop.SizeProp size = new Prop.SizeProp(this);

  void onPosChange() {}
  void onSizeChange() {}

//////////////////////////////////////////////////////////////////////////
// Layout
//////////////////////////////////////////////////////////////////////////

  public Size prefSize(fan.fwt.Widget self, Hints hints)
  {
    if (!(control instanceof Control)) return Size.def;
    int w = (hints.w == null) ? SWT.DEFAULT : hints.w.intValue();
    int h = (hints.h == null) ? SWT.DEFAULT : hints.h.intValue();
    Point s = ((Control)control).computeSize(w, h, true);
    return size(s);
  }

  public fan.fwt.Point posOnDisplay(fan.fwt.Widget self)
  {
    if (!(control instanceof Control)) return null;
    Point pt = Env.get().display.map((Control)control, null, 0, 0);
    return point(pt);
  }

  public fan.fwt.Widget relayout(fan.fwt.Widget self)
  {
    if (control instanceof Composite) ((Composite)control).layout(true);
    //if (control instanceof Control) ((Control)control).redraw();
    return self;
  }

  public fan.fwt.Widget pack(fan.fwt.Widget self)
  {
    if (control instanceof Control)
      ((Control)control).pack();
    return self;
  }

  public void repaint(fan.fwt.Widget self, Rect r)
  {
    if (control instanceof Control)
    {
      Control c = (Control)control;
      if (r == null)
        c.redraw();
      else
        c.redraw(r.x.intValue(), r.y.intValue(), r.w.intValue(), r.h.intValue(), true);
    }
  }

//////////////////////////////////////////////////////////////////////////
// Focus Eventing
//////////////////////////////////////////////////////////////////////////

  public boolean hasFocus(fan.fwt.Widget self)
  {
    if (control instanceof Control)
      return ((Control)control).isFocusControl();
    else
      return false;
  }

  public void focus(fan.fwt.Widget self)
  {
    if (control instanceof Control)
      ((Control)control).setFocus();
  }

  public void checkFocusListeners(fan.fwt.Widget self)
  {
    // if we don't have any onFocus listeners, then I
    // shouldn't be actively registered as a focus listener
    if (!(control instanceof Control)) return;
    boolean now = self.onFocus().isEmpty() && self.onBlur().isEmpty();
    if (now != activeFocusListener) return;
    if (activeFocusListener)
    {
      ((Control)control).removeFocusListener(this);
      activeFocusListener  = false;
    }
    else
    {
      ((Control)control).addFocusListener(this);
      activeFocusListener = true;
    }
  }

  public void focusGained(FocusEvent se)
  {
    self.onFocus().fire(event(EventId.focus));
  }

  public void focusLost(FocusEvent se)
  {
    self.onBlur().fire(event(EventId.blur));
  }

//////////////////////////////////////////////////////////////////////////
// Key Eventing
//////////////////////////////////////////////////////////////////////////

  public void checkKeyListeners(fan.fwt.Widget self)
  {
    // if we don't have any onKey listeners, then I
    // shouldn't be actively registered as a key listener
    if (!(control instanceof Control)) return;
    boolean now = self.onKeyDown().isEmpty() && self.onKeyUp().isEmpty();
    if (now != activeKeyListener) return;
    if (activeKeyListener)
    {
      ((Control)control).removeKeyListener(this);
      activeKeyListener  = false;
    }
    else
    {
      ((Control)control).addKeyListener(this);
      activeKeyListener = true;
    }
  }

  public void keyPressed(KeyEvent se)
  {
    fireKeyEvent(self.onKeyDown(), EventId.keyDown, se);
  }

  public void keyReleased(KeyEvent se)
  {
    fireKeyEvent(self.onKeyUp(), EventId.keyUp, se);
  }

  void fireKeyEvent(EventListeners listeners, EventId id, KeyEvent se)
  {
    fan.fwt.Event fe = event(id);
    fe.keyChar = Long.valueOf(se.character);
    fe.key     = toKey(se.keyCode, se.stateMask);
    listeners.fire(fe);
    if (fe.consumed) se.doit = false;
  }

  static Key toKey(int keyCode, int stateMask)
  {
    Key key = Key.fromMask(Long.valueOf(keyCode));
    if ((stateMask & SWT.SHIFT) != 0)   key = key.plus(Key.shift);
    if ((stateMask & SWT.ALT) != 0)     key = key.plus(Key.alt);
    if ((stateMask & SWT.CTRL) != 0)    key = key.plus(Key.ctrl);
    if ((stateMask & SWT.COMMAND) != 0) key = key.plus(Key.command);
    return key;
  }

//////////////////////////////////////////////////////////////////////////
// Mouse Eventing
//////////////////////////////////////////////////////////////////////////

  public void mouseDoubleClick(MouseEvent se) {}

  public void mouseDown(MouseEvent se)
  {
    fireMouseEvent(self.onMouseDown(), EventId.mouseDown, se);
  }

  public void mouseUp(MouseEvent se)
  {
    fireMouseEvent(self.onMouseUp(), EventId.mouseUp, se);
  }

  public void mouseMove(MouseEvent se)
  {
    fireMouseEvent(self.onMouseMove(), EventId.mouseMove, se);
  }

  public void mouseEnter(MouseEvent se)
  {
    fireMouseEvent(self.onMouseEnter(), EventId.mouseEnter, se);
  }

  public void mouseExit(MouseEvent se)
  {
    fireMouseEvent(self.onMouseExit(), EventId.mouseExit, se);
  }

  public void mouseHover(MouseEvent se)
  {
    fireMouseEvent(self.onMouseHover(), EventId.mouseHover, se);
  }

  public void mouseScrolled(MouseEvent se)
  {
    fireMouseEvent(self.onMouseWheel(), EventId.mouseWheel, se);
  }

  private void fireMouseEvent(EventListeners listeners, EventId id, MouseEvent se)
  {
    // save modifiers on mouse events for future selection, action,
    // and popup events which might occur;  this allows us to check
    // for Ctrl down to handle newTab style of eventing
    int mask = se.stateMask & SWT.MODIFIER_MASK;
    Key key = toKey(0, mask);
    modifiers = mask == 0 ? null : key;

    // fire event
    fan.fwt.Event fe = event(id);
    fe.pos    = point(se.x, se.y);
    fe.count  = Long.valueOf(se.count);
    fe.button = Long.valueOf(se.button);
    fe.key    = key;
    listeners.fire(fe);
  }

//////////////////////////////////////////////////////////////////////////
// Attachment
//////////////////////////////////////////////////////////////////////////

  public final boolean attached(fan.fwt.Widget self)
  {
    return control != null;
  }

  public final void attach(fan.fwt.Widget self)
  {
    // short circuit if I'm already attached
    if (control != null) return;

    // short circuit if my parent isn't attached
    fan.fwt.Widget parentWidget = self.parent();
    if (parentWidget == null || parentWidget.peer.control == null) return;

    // create control and initialize
    // TODO: need to rework this cluster f**k
    Widget parentControl = parentWidget.peer.control;
    if (parentControl instanceof TabItem)
    {
      TabItem item = (TabItem)parentControl;
      attachTo(create(item.getParent()));
      item.setControl((Control)this.control);
    }
    else
    {
      attachTo(create(parentControl));
      if (parentControl instanceof ScrolledComposite)
        ((ScrolledComposite)parentControl).setContent((Control)control);
    }

    // callback on parent
    parentWidget.peer.childAdded(self);
  }

  void childAdded(fan.fwt.Widget child) {}

  void attachTo(Widget control)
  {
    // sync with native control
    this.control = control;
    checkFocusListeners(self);
    checkKeyListeners(self);
    if (control instanceof Control)
    {
      ((Control)control).addMouseListener(this);
      ((Control)control).addMouseMoveListener(this);
      ((Control)control).addMouseTrackListener(this);
      ((Control)control).addMouseWheelListener(this);
    }
    control.addDisposeListener(this);
    syncPropsToControl();

    // stick myself in data field
    control.setData(self);

    // recursively attach my children
    List kids = self.kids;
    for (int i=0; i<kids.sz(); ++i)
    {
      fan.fwt.Widget kid = (fan.fwt.Widget)kids.get(i);
      kid.peer.attach(kid);
    }
  }

  public Widget create(Widget parent)
  {
    Canvas c = new Canvas((Composite)parent, 0)
    {
      public void drawBackground(GC gc, int x, int y, int w, int h) {}
    };
    c.addPaintListener(this);
    return c;
  }

  public void detach(fan.fwt.Widget self)
  {
    // dipose the control which automatically disposes all the
    // children; we do cleanup in the widgetDisposed callback.
    if (control != null) control.dispose();
    control = null;
  }

  public void widgetDisposed(DisposeEvent e)
  {
    syncPropsFromControl();
    control = null;
  }

//////////////////////////////////////////////////////////////////////////
// Widget/Control synchronization
//////////////////////////////////////////////////////////////////////////

  public void syncPropsToControl() { syncProps(true); }
  public void syncPropsFromControl(){ syncProps(false); }

  private void syncProps(boolean to)
  {
    try
    {
      Field[] fields = getClass().getFields();
      for (int i=0; i<fields.length; ++i)
      {
        Field f = fields[i];
        try
        {
          if (Prop.class.isAssignableFrom(f.getType()))
          {
            Prop prop = (Prop)f.get(this);
            if (to)
              prop.syncToControl();
            else
              prop.syncFromControl();
          }
        }
        catch (Exception e)
        {
          System.out.println("ERROR: setting " + f);
          e.printStackTrace();
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

//////////////////////////////////////////////////////////////////////////
// Callbacks
//////////////////////////////////////////////////////////////////////////

  public void paintControl(PaintEvent e)
  {
    Graphics g = new Graphics();
    g.peer.gc = e.gc;
    self.onPaint(g);
  }

//////////////////////////////////////////////////////////////////////////
// Utils
//////////////////////////////////////////////////////////////////////////

  static fan.fwt.Widget toFanWidget(Widget w)
  {
    if (w != null)
    {
      Object data = w.getData();
      if (data instanceof fan.fwt.Widget)
        return (fan.fwt.Widget)data;
    }
    return null;
  }

  static fan.fwt.Point point(int x, int y)
  {
    return fan.fwt.Point.make(Long.valueOf(x), Long.valueOf(y));
  }

  static fan.fwt.Point point(Point pt)
  {
    return fan.fwt.Point.make(Long.valueOf(pt.x), Long.valueOf(pt.y));
  }

  static fan.fwt.Size size(int w, int h)
  {
    return fan.fwt.Size.make(Long.valueOf(w), Long.valueOf(h));
  }

  static fan.fwt.Size size(Point pt)
  {
    return fan.fwt.Size.make(Long.valueOf(pt.x), Long.valueOf(pt.y));
  }

  static fan.fwt.Rect rect(Rectangle r)
  {
    return fan.fwt.Rect.make(Long.valueOf(r.x), Long.valueOf(r.y), Long.valueOf(r.width), Long.valueOf(r.height));
  }

  static Rectangle rect(fan.fwt.Rect r)
  {
    return new Rectangle(r.x.intValue(), r.y.intValue(), r.w.intValue(), r.h.intValue());
  }

  static int style(Halign halign)
  {
    if (halign == Halign.left) return SWT.LEFT;
    if (halign == Halign.center) return SWT.CENTER;
    if (halign == Halign.right) return SWT.RIGHT;
    throw new IllegalStateException(halign.toString());
  }

  static int orientation(Orientation orientation)
  {
    if (orientation == Orientation.horizontal) return SWT.HORIZONTAL;
    if (orientation == Orientation.vertical) return SWT.VERTICAL;
    throw new IllegalStateException(orientation.toString());
  }

  static int accelerator(Key key)
  {
    if (key == null) return 0;
    return key.mask.intValue();
  }

//////////////////////////////////////////////////////////////////////////
// Event Utils
//////////////////////////////////////////////////////////////////////////

  fan.fwt.Event event(EventId id) { return event(id, null); }
  fan.fwt.Event event(EventId id, Object data)
  {
    fan.fwt.Event f = fan.fwt.Event.make();
    f.id(id);
    f.widget(self);
    f.data(data);
    f.key(modifiers);
    return f;
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  fan.fwt.Widget self;
  Widget control;
  Key modifiers;
  boolean activeKeyListener   = false;
  boolean activeFocusListener = false;
}