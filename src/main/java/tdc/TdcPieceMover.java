/*
 * $Id: TdcPieceMover.java 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2005-2010 by Brent Easton
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
 
package tdc;

import VASSAL.counters.PropertyExporter;
import java.awt.Point;
import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import VASSAL.build.module.map.PieceMover;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Properties;
import VASSAL.counters.Stack;

/**
 * 
 * @author Brent Easton
 *
 * Mark units done when they move, If they are Active and not yet marked.
 */
public class TdcPieceMover extends PieceMover {

  protected Command movedPiece(GamePiece p, Point loc) {
    // final Map m = p.getMap();
    // Rectangle r = m.componentRectangle(new Rectangle(0, 0, 10, 5));
    Command c = super.movedPiece(p, loc);
    if (c == null) {
      c = new NullCommand();
    }
    if (p.getMap() != null) {
      if (p instanceof Stack) {
        for (GamePiece piece : ((Stack) p).asList()) {
          c.append(activatePiece(piece));
        }
      }
      else {
        c.append(activatePiece(p));
      }
    }
    return c;
  }
  
  protected Command activatePiece(GamePiece p) {
    String markLevel = p.getProperty("Mark_Level") + "";
    String activeLevel = p.getProperty("Active_Level") + "";
    if (markLevel.equals("1") && activeLevel.equals("2")) {
      p.setProperty(Properties.SNAPSHOT, ((PropertyExporter) p).getProperties());
      return p.keyEvent(KeyStroke.getKeyStroke('K', InputEvent.CTRL_MASK));
    }
    else {
      return new NullCommand();
    }
  }
  
}
