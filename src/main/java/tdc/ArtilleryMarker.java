/*
 * $Id: ArtilleryMarker.java 4157 2008-09-29 19:41:27Z uckelman $
 *
 * Copyright (c) 2000-20012 by Rodney Kinney, Brent Easton
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

import javax.swing.JPanel;
import javax.swing.KeyStroke;

import VASSAL.command.Command;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;

/**
 * A marker for use in Artillery Contact pieces that returns the name of the Basic Piece of
 * the associated Artillery piece for the Marker Name ArtilleryUnit
 */
public class ArtilleryMarker extends Decorator implements EditablePiece {
  public static final String ID = "Artmark;";
  public static final String MARKER_NAME = "ArtilleryUnit";

  protected String[] keys;
  protected String[] values;

  public ArtilleryMarker() {
    this(ID, null);
  }

  public ArtilleryMarker(String type, GamePiece p) {
    mySetType(type);
    setInner(p);
  }

  public String[] getKeys() {
    return keys;
  }

  public void mySetType(String s) {

  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  public String getName() {
    return piece.getName();
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public Object getProperty(Object key) {
    if (MARKER_NAME.equals(key)){
      return getArtilleryName();
    }
    else if (TdcProperties.TYPE.equals(key)) {
      return TdcProperties.TYPE_ARTILLERY_MARKER;
    }
    return super.getProperty(key);
  }
  
  public Object getLocalizedProperty(Object key) {
    if ( MARKER_NAME.equals(key) || TdcProperties.TYPE.equals(key)){
      return getProperty(key);
    }
    return super.getLocalizedProperty(key);
  }

  protected String getArtilleryName() {
    String basicName = (String) super.getProperty(BasicPiece.BASIC_NAME);
    if (basicName.endsWith(" -")) {
      return basicName.substring(0, basicName.length() - 2);
    }
    else if (basicName.endsWith(" - ")) {
      return basicName.substring(0, basicName.length() - 3);
    }
    else {
      return basicName.substring(0, basicName.length() - 10);
    }
  }
  
  public void setProperty(Object key, Object value) {
    super.setProperty(key, value);
  }

  public String myGetState() {
    return "";
  }

  public void mySetState(String state) {

  }

  public String myGetType() {
    return ID;
  }

  protected KeyCommand[] myGetKeyCommands() {
    return new KeyCommand[0];
  }

  public Command myKeyEvent(KeyStroke stroke) {
    return null;
  }

  public String getDescription() {
      return "Artillery Marker";
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    return null;
  }

  public PieceEditor getEditor() {
    return new Ed();
  }

  private static class Ed implements PieceEditor {

    public Component getControls() {
      return new JPanel();
    }

    public String getType() {
      return "";
    }

    public String getState() {
      return "";
    }
    
  }
}
