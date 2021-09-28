/*
 * $Id: FireModifier 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2012 by Brent Easton
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
package tdc.attack;

import java.awt.Dimension;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import tdc.TdcProperties;
import tdc.UnitInfo;
import VASSAL.build.GameModule;
import VASSAL.counters.GamePiece;
import VASSAL.tools.IconButton;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.OpIcon;

public class FireModifier {

  protected static final int BASIC = 0;
  protected static final int ONOFF = 1;
  protected static final int COUNT = 2;
  protected static final int BONUS = 3; // Special Company Bonus version of ONOFF
  protected static Font labelFont = new Font("Dialog", Font.BOLD, 12);
  
  protected static HashMap<String, FireModifier> crossRef;

  protected String description;
  protected int value;
  protected int oldValue;
  protected int modifier;
  protected boolean[] modes;
  protected JPanel[] controls;
  protected JLabel[] labels;
  protected JCheckBox[] checkBoxes;
  protected int type;
  protected AttackModel listener;
  protected GamePiece owner;
  protected String id;
  protected boolean doNotDisplay;
  protected String bonusMessage = null;
  protected boolean assaultDefenceOnly;

  public boolean isAssaultDefenceOnly() {
    return assaultDefenceOnly;
  }

  public void setAssaultDefenceOnly(boolean assaultDefenceOnly) {
    this.assaultDefenceOnly = assaultDefenceOnly;
  }

  protected static void initialiseCrossRef() {
    getCrossRef().clear();
  }
  
  protected static HashMap<String, FireModifier> getCrossRef() {
    if (crossRef == null) {
      crossRef = new HashMap<>();
    }
    return crossRef;
  }
  
  public static FireModifier find (String id) {
    return getCrossRef().get(id);
  }
  
  public FireModifier(String description, int value) {
    this (description, value, null);
  }

  public FireModifier(String description, int value, boolean doNotDisplay) {
    this(description, value);
    this.doNotDisplay = doNotDisplay;
  }
  
  public FireModifier(String description, int value, GamePiece owner) {
    this(description, value, BASIC, true, true, true, true);
    this.owner = owner;
  }

  public FireModifier(String description, int value, boolean dir, boolean ind, boolean opp) {
    this(description, value, BASIC, dir, ind, opp, null);
  }

  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp, boolean assault) {
    this(description, value, type, dir, ind, opp, assault, null);
  }

  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp) {
    this(description, value, type, dir, ind, opp, null);
  }

  public FireModifier(String description, int value, GamePiece owner, boolean dir, boolean ind, boolean opp) {
    this(description, value, owner, dir, ind, opp, false);
  }

  public FireModifier(String description, int value, GamePiece owner, boolean dir, boolean ind, boolean opp, boolean assault) {
    this(description, value, BASIC, dir, ind, opp, assault);
    this.owner = owner;
  }
  
  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp, AttackModel model, GamePiece owner) {
    this(description, value, type, dir, ind, opp, false, model, owner);
  }

  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp, boolean assault, AttackModel model, GamePiece owner) {
    this(description, value, type, dir, ind, opp, assault, model);
    this.owner = owner;
  }
  
  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp, AttackModel model) {
    this(description, value, type, dir, ind, opp, false, model);
  }

  public FireModifier(String description, int value, int type, boolean dir, boolean ind, boolean opp, boolean assault, AttackModel model) {
    this.description = description;
    this.modifier = value;
    this.type = type;
    this.value = type == BASIC ? value : 0;
    this.listener = model;

    modes = new boolean[AttackModel.MODES.length];
    modes[AttackModel.MODE_DIRECT] = dir;
    modes[AttackModel.MODE_INDIRECT] = ind;
    modes[AttackModel.MODE_OPPORTUNITY] = opp;
    modes[AttackModel.MODE_ASSAULT] = assault;
    controls = new JPanel[AttackModel.MODES.length];
    labels = new JLabel[AttackModel.MODES.length];
    checkBoxes = new JCheckBox[AttackModel.MODES.length];
    
    id = Integer.toString(getCrossRef().size());
    crossRef.put(id, this);
  }

  public void setOwner(GamePiece owner) {
    this.owner = owner;
  }
  
  public boolean isUsedInMode(int mode) {
    return modes[mode];
  }

  public boolean isHill() {
    return TdcProperties.HILL.equals(description);
  }
  
  public String getId() {
    return id;
  }
  
  public String getDescription() {
    return description;
  }

  public int getValue() {
    return value;
  }
  
  public int getOldValue() {
    return oldValue;
  }

  public String getValueString() {
    final String s = Integer.toString(value);
    return value < 1 ? s : ("+" + s);
  }

  public String toString() {
    if (type == ONOFF || type == BONUS) {
      String message = bonusMessage;
      if (message == null) {
        message = description + " " + (value == 0 ? "No" : "Yes");
      }
      bonusMessage = null;
      return message;
    }
    else if (type == COUNT) {
      return description + " " + getValueString();
    }
    else {
      return description;
    }
  }
  
  // The value has been changed by this client
  public void setValue(int v) {
    modifier = v;
    if (type == BASIC) value = v;
    updateLabels();
  }

  // The value has been changed by another client
  public void changeValue(int v) {
    //modifier = v;
    value = v;
    if (type == ONOFF || type == BONUS) {
      for (int i = 0; i < AttackModel.MODES.length; i++) {
        if (checkBoxes[i] != null) {
          checkBoxes[i].setSelected(value!=0);                
        }
      }
    }    
    refreshLabels();
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      if (controls[i] != null) {
        controls[i].repaint();
      }
    }
  }
  
  public GamePiece getOwner() {
    return owner;
  }
  
  public boolean isOwner(GamePiece piece) {
    return owner == null || (piece == owner);
  }
  
  public void setState(String state) {
    
  }
  
  protected void updateLabels() {
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      if (labels[i] != null) {
        labels[i].setText(getValueString());
      }
    }
    if (listener != null) {
      listener.change(this);
    }
  }
  
  protected void refreshLabels() {
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      if (labels[i] != null) {
        labels[i].setText(getValueString());
      }
    }
    if (listener != null) {
      listener.change();
    }
  }
  
  public JPanel getControls(int mode) {
    if (controls[mode] == null) {

      controls[mode] = new JPanel(new MigLayout("insets 0"));

      final JPanel modPanel = new JPanel(new MigLayout("insets 5", "push[]push"));
      modPanel.setBorder(BorderFactory.createEtchedBorder());
      labels[mode] = new JLabel(getValueString());
      labels[mode].setFont(labelFont);

      modPanel.add(labels[mode]);
      controls[mode].add(modPanel, "w 26!");
      controls[mode].add(new JLabel(description));

      if (type == ONOFF || type == BONUS) {
        checkBoxes[mode] = new JCheckBox();
        checkBoxes[mode].addActionListener(e -> {
          oldValue = value;
          value = value == 0 ? modifier : 0;
          updateLabels();
          for (int i = 0; i < AttackModel.MODES.length; i++) {
            if (checkBoxes[i] != null) {
              checkBoxes[i].setSelected(value!=0);
            }
          }
        });
        controls[mode].add(checkBoxes[mode]);
        if (type == BONUS) {
          final JButton button = new JButton(new OpIcon(Op.load("small_die.png")));
          final Dimension d = new Dimension(25, 25);
          button.setMinimumSize(d);
          button.setMinimumSize(d);
          button.setPreferredSize(d);
          button.addActionListener(e -> rollCompanyBonus());
          controls[mode].add(button);
        }
 
      }
      else if (type == COUNT) {
        final JButton minusButton = new IconButton(IconButton.MINUS_ICON, 20);
        minusButton.addActionListener(e -> {
          if (value > -9) {
            oldValue = value;
            value -= 1;
          }
          updateLabels();
        });
        final JButton plusButton = new IconButton(IconButton.PLUS_ICON, 20);
        plusButton.addActionListener(e -> {
          if (value < 9) {
            oldValue = value;
            value += 1;
          }
          updateLabels();
        });
        controls[mode].add(minusButton);
        controls[mode].add(plusButton);
      }
    }
    if (doNotDisplay) {
      controls[mode].setVisible(!doNotDisplay);
    }
    return controls[mode];
  }
  
  protected void rollCompanyBonus() {
    final int roll = (int) (GameModule.getGameModule().getRNG().nextFloat() * 10);
    final UnitInfo info = listener.getSourceInfo();
    final int tqr = info.getEffectiveTqr();    
    
    final boolean bonus = roll != 9 && (roll == 0 || roll <= tqr);
    
    bonusMessage = "Company Bonus Roll: TQR="+tqr+(info.isTqrAdjusted() ? "("+info.getTqrDetails()+")" : "")+", roll="+roll+" ";
    oldValue = value;
    
    if (bonus) {
      bonusMessage += "Success!";
      if (oldValue == 0) {
        value = modifier;
      }
    }
    else {
      bonusMessage += "Failed!";
      if (oldValue != 0) {
        value = 0;
      }
    }
 
    updateLabels();
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      if (checkBoxes[i] != null) {
        checkBoxes[i].setSelected(value!=0);                
      }
    }
  }

}