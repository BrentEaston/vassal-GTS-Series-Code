/*
 * $Id: PeekAtLayer.java 9193 2015-05-18 00:27:45Z swampwallaby $
 *
 * Copyright (c) 2006-2010 by Brent Easton
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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.Properties;
import VASSAL.tools.SequenceEncoder;

/**
 * This trait allows you to peek at a layer
 */
public class PeekAtLayer extends Decorator implements EditablePiece {
  public static final String ID = "peek;";
  protected KeyCommand[] keyCommands;
  protected KeyCommand peekCommand;
  protected String commandName;
  protected KeyStroke key;
  protected String layerName;
  protected boolean peeking = false;

  public PeekAtLayer() {
    this(ID + "Peek;P;", null);
  }

  public PeekAtLayer(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    commandName = st.nextToken();
    key = st.nextKeyStroke('P');
    layerName = st.nextToken("");
    keyCommands = null;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(commandName).append(key).append(layerName);
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    if (keyCommands == null) {
      peekCommand = new KeyCommand(commandName, key, Decorator.getOutermost(this));
      if (commandName.length() > 0) {
        keyCommands = new KeyCommand[]{peekCommand};
      }
      else {
        keyCommands = new KeyCommand[0];
      }
    }
    //peekCommand.setEnabled(getMap() != null);
    return keyCommands;
  }

  public String myGetState() {
    return "";
  }

  public Command myKeyEvent(KeyStroke stroke) {
    Command c = null;
    myGetKeyCommands();
    if (peekCommand.matches(stroke)) {
      peeking = true;
    }
    return c;
  }

  public void mySetState(String newState) {
  }

  public void setProperty(Object key, Object val) {
    if (Properties.SELECTED.equals(key)) {
      if (!Boolean.TRUE.equals(val)) {
        peeking = false;
      }
      super.setProperty(key, val);
    }
    else {
      super.setProperty(key, val);
    }
  }
  
  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    if (peeking && Boolean.TRUE.equals(getProperty(Properties.SELECTED))) {
      GamePiece outer = Decorator.getOutermost(this);
      if ("true".equals(outer.getProperty(layerName + Embellishment.ACTIVE))) {
        Decorator.getInnermost(this).draw(g, x, y, obs, zoom);
      }
      else {
        drawLayer(this, g, x, y, obs, zoom);
      }
    }
    else {
      piece.draw(g, x, y, obs, zoom);
    }
  }

  protected void drawLayer(GamePiece p, Graphics g, int x, int y, Component obs, double zoom) {
    Embellishment e = (Embellishment) Decorator.getDecorator(p, Embellishment.class);
    if (e == null) {
      piece.draw(g, x, y, obs, zoom);
    }
    else if (getLayerName(e).equals(layerName)) {
      e.setActive(true);
      e.draw(g, x, y, obs, zoom);
      e.setActive(false);
    }
    else {
      drawLayer(e.getInner(), g, x, y, obs, zoom);
    }
  }

  protected String getLayerName(Embellishment e) {
    SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(e.myGetType(), ';');
    for (int i=0; i < 19; i++) {
      sd.nextToken();
    }
    return sd.nextToken("");
  }
  
  public String getName() {
    return piece.getName();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  public String getDescription() {
    return "Peek at Layer";
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public static class Ed implements PieceEditor {
    protected StringConfigurer nameInput;
    protected HotKeyConfigurer keyInput;
    protected StringConfigurer layerInput;
    protected JPanel controls;

    public Ed(PeekAtLayer p) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      nameInput = new StringConfigurer(null, "Command name:  ", p.commandName);
      controls.add(nameInput.getControls());

      keyInput = new HotKeyConfigurer(null, "Keyboard Command:  ", p.key);
      controls.add(keyInput.getControls());

      layerInput = new StringConfigurer(null, "Layer name to Peek at:  ", p.layerName);
      controls.add(layerInput.getControls());
      
    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(nameInput.getValueString())
        .append((KeyStroke) keyInput.getValue())
        .append(layerInput.getValueString());
      return ID + se.getValue();
    }

    public String getState() {
      return "";
    }
  }
}
