/*
 * $Id: HoverText.java 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2015 by Brent Easton
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.tools.SequenceEncoder;

public class HoverText extends Decorator implements EditablePiece {
  public static final String ID = "hover;";
  protected KeyCommand[] keyCommands;
  protected KeyCommand setHoverCommand;
  protected String setHoverCommandName;
  protected KeyStroke setHoverKey;

  // The hoveText has moved from Type to State so it refreshes properly.
  protected String hoverText;
  protected String oldHoverText;
  
  public HoverText() {
    this(ID + "Hover;P;", null);
  }

  public HoverText(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  
  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String getName() {
    return piece.getName();
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }
  
  public String getDescription() {
    return "Hover Text";
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    setHoverCommandName = st.nextToken();
    setHoverKey = st.nextKeyStroke('P');
    oldHoverText = st.nextToken("");
    keyCommands = null;
    
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public void mySetState(String newState) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(newState, ';');
    hoverText = st.nextToken("");
    if (hoverText.isEmpty() && !oldHoverText.isEmpty()) {
      hoverText = oldHoverText;
    }
  }

  public String myGetState() {
    final SequenceEncoder se = new SequenceEncoder(hoverText, ';');
    return se.getValue();
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(setHoverCommandName).append(setHoverKey).append("");
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    if (keyCommands == null) {
      setHoverCommand = new KeyCommand(setHoverCommandName, setHoverKey, Decorator.getOutermost(this));
      if (setHoverCommandName.length() > 0) {
        keyCommands = new KeyCommand[]{setHoverCommand};
      }
      else {
        keyCommands = new KeyCommand[0];
      }
    }
    return keyCommands;
  }

  public Command myKeyEvent(KeyStroke stroke) {
    myGetKeyCommands();
    Command c = null;
    if (setHoverCommand.matches(stroke)) {
      ChangeTracker tracker = new ChangeTracker(this);
      
      final String s = (String) JOptionPane.showInputDialog
          (getMap() == null ? null : getMap().getView().getTopLevelAncestor(),
              setHoverCommand.getName(),
           null,
           JOptionPane.QUESTION_MESSAGE,
           null,
           null,
           hoverText);
      
      if (s == null) {
        tracker = null;
      }
      else {
        hoverText = s;
      }
      c = tracker == null ? null : tracker.getChangeCommand();
    }
    return c;
  }
  
  public Object getProperty (Object key) {
    if (TdcProperties.HOVER_TEXT.equals(key)) {
      return hoverText;
    }
    else
      return super.getProperty(key);
  }
  
  public Object getLocalizedProperty (Object key) {
    if (TdcProperties.HOVER_TEXT.equals(key)) {
      return getProperty(key);
    }
    else
      return super.getLocalizedProperty(key);
  }
  
  
  public static class Ed implements PieceEditor {
    protected StringConfigurer nameInput;
    protected HotKeyConfigurer keyInput;
    protected JPanel controls;

    public Ed(HoverText p) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      nameInput = new StringConfigurer(null, "Command name:  ", p.setHoverCommandName);
      controls.add(nameInput.getControls());

      keyInput = new HotKeyConfigurer(null, "Keyboard Command:  ", p.setHoverKey);
      controls.add(keyInput.getControls());
    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(nameInput.getValueString())
        .append((KeyStroke) keyInput.getValue());
      return ID + se.getValue();
    }

    public String getState() {
      return "";
    }
  }

}
