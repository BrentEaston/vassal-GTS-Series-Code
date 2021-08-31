/*
 * $Id: TdcIndependent 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2014 by Brent Easton
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.KeyStroke;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;

/**
 * Handle support for attachable independent units in D-Day
 * This trait is for the Independent Units. See also TdcLeader
 */
public class TdcIndependent extends Decorator implements EditablePiece {

  public static final String ID = "independent;";
  
  public TdcIndependent() {
    this(ID + "", null);
  }
  
  public TdcIndependent(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }
  
  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    // TODO Auto-generated method stub

  }

  public Rectangle boundingBox() {
    // TODO Auto-generated method stub
    return null;
  }

  public Shape getShape() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public void mySetState(String newState) {
    // TODO Auto-generated method stub

  }

  public String myGetState() {
    return "";
  }

  public String myGetType() {
    return ID;
  }

  protected KeyCommand[] myGetKeyCommands() {
    // TODO Auto-generated method stub
    return null;
  }

  public Command myKeyEvent(KeyStroke stroke) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  public void mySetType(String type) {
    // TODO Auto-generated method stub
    
  }
  
  public static class Ed implements PieceEditor {

    public Ed (TdcIndependent p) {
      
    }
    
    public Component getControls() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getType() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getState() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  public HelpFile getHelpFile() {
    // TODO Auto-generated method stub
    return null;
  }

}