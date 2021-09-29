/*
 * $Id: AttackView 7690 2011-07-07 23:44:55Z swampwallaby $
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

import VASSAL.build.GameModule;
import VASSAL.command.Command;
import VASSAL.counters.GamePiece;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.OpIcon;
import net.miginfocom.swing.MigLayout;
import tdc.AttackWizard;
import tdc.AttackWizard.WizardTargetCommand;
import tdc.UnitInfo;

import javax.swing.*;
import java.awt.*;

public class AttackView {
  protected static Font boldFont;
  protected static Font normalFont;
  protected static String WIDTH = "w 450:450";
  
  protected AttackModel myModel;
  protected JPanel mainPanel;
  protected CardLayout cardLayout;
  protected RatingDisplay rating;
  protected JButton[] modeButtons;
  protected JPanel[] attackPanels;
  protected JPanel[] defencePanels;
  protected TargetUnitSelector targetSelector;
  protected Color selectedColor;
  protected Color unselectedColor;
  protected AttackWizard myWizard;
  protected JPanel specialAttack = new JPanel();
  protected JCheckBox checkBoxAssaultFireRating = new JCheckBox();
  protected JCheckBox defendingAssault = new JCheckBox();

  public AttackView(AttackModel model, AttackWizard owner) {
    myModel = model;
    myWizard = owner;
    myModel.setChangeListener(this);
    if (boldFont == null) {
      boldFont = new Font("Dialog", Font.BOLD, 12);
      normalFont = new Font("Dialog", Font.PLAIN, 12);
    }
  }

  public JPanel getContent() {
    if (mainPanel == null) {
      buildContent();
      changeMode(myModel.getMode());
    }
    return mainPanel;
  }

  public GamePiece getSource() {
    return myModel.getSource();
  }

  public GamePiece getTarget() {
    return myModel.getTarget();
  }
  
  /*
   * The Unit Selector has selected a new target
   */
  public void selectTarget(int t) {
    changeTarget(t);
    final Command targetCmd = new WizardTargetCommand (myWizard, t, myModel.getOldTargetIndex());
    final Command reportCmd = myWizard.getChatCommand("New target "+myModel.getTargetText(), false);
    reportCmd.execute();
    targetCmd.append(reportCmd);
    GameModule.getGameModule().sendAndLog(targetCmd);
  }
  
  // Target changed by another client
  public void changeTarget(int t) {
    
    targetSelector.select(t);
    // New target on the Defence Panel may have different modifiers
    for (FireModifier mod : myModel.getDefenceModifiers()) {
      mod.getControls(myModel.getMode()).setVisible(mod.isOwner(myModel.getTarget()));
    }
    refresh();
    pack();    
  }
  /**
   * Something in the model has changed, refresh the display
   */
  
  public void refresh(FireModifier modifier) {
    refresh();
    myWizard.modifierChanged(modifier);
  }
  
  public void refresh() {
    if (rating != null) {
      rating.refresh();
    }
    specialAttack.setVisible(myModel.canCounterBatteryFire() || myModel.isAirPower() || myModel.isPartisanAttack());
    checkBoxAssaultFireRating.setVisible(myModel.isAssaultMode());
    defendingAssault.setVisible(myModel.isAssaultMode());
  }
  
  protected void pack() {
    myWizard.pack();
  }
  
  protected void buildContent() {

    mainPanel = new JPanel(new MigLayout("insets 0,hidemode 3"));
    final JPanel buttonPanel = new JPanel(new MigLayout("ins 0,hidemode 3", "[center]rel[center]rel[center]rel[center]"));
    final JPanel topPanel = new JPanel(new MigLayout("hidemode 3", "[center]push[center]push[center]", "[top]"));
    final JPanel botPanel = new JPanel(new MigLayout("hidemode 3", "[left]push[right]", "[top]"));
    
    modeButtons = new JButton[AttackModel.MODES.length];
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      final int mode = i;
      final JButton button = new JButton(AttackModel.MODES[i]);
      button.addActionListener(e -> {
        int oldMode = myModel.getMode();
        changeMode(mode);
        myWizard.modeChanged(mode, oldMode);
      });
      button.setEnabled(myModel.isModeEnabled(i));
      modeButtons[i] = button;
    }    
    unselectedColor = modeButtons[0].getBackground();
    selectedColor = new Color(144, 190, 217);
    
    JLabel heading = null;
    if (myModel.isAirPower()) {
      heading = new JLabel("Air Strike");
    }
    else if (myModel.isAirDefence()) {
      heading = new JLabel("Air Defence");
    }
    else if (myModel.isPartisanAttack()) {
      heading = new JLabel("Partisan Attack");
    }
    else if (myModel.specialAttackType > AttackWizard.NO_SPECIAL_ATTACK) {
      heading = new JLabel(AttackWizard.SPECIAL_ATTACKS[myModel.specialAttackType]);
    }    

    if (heading != null) {
      heading.setFont(boldFont);
      topPanel.add(heading, "span 3,wrap");
    }
    else {
      buttonPanel.add(modeButtons[0], "sg 1");
      buttonPanel.add(modeButtons[1], "sg 1");
      buttonPanel.add(modeButtons[2], "sg 1");
      buttonPanel.add(modeButtons[3], "sg 1, wrap");
      topPanel.add(buttonPanel, "span 3, center, wrap");
    }

    
    final JPanel sourcePanel = new SourceUnitSelector(this, myModel).getContent();    
    targetSelector = new TargetUnitSelector(this, myModel);
    final JPanel targetPanel = targetSelector.getContent();
    rating = new RatingDisplay(myModel);
    final JPanel ratingPanel = rating.getContent();

    // Fo future expansion if we want to make the 0-9 limit option visible
    final JCheckBox checkBoxRawFireRating = new JCheckBox();
    checkBoxRawFireRating.setEnabled(true);
    checkBoxRawFireRating.setSelected(true);
    checkBoxRawFireRating.setText("0-9");
    checkBoxRawFireRating.addActionListener(e -> {
        myModel.setDisplayRealFireRating(!myModel.isDisplayRealFireRating());
        refresh();
      }
    );


    checkBoxAssaultFireRating.setEnabled(true);
    checkBoxAssaultFireRating.setSelected(false);
    myModel.setIsAssaultRating(false);
    checkBoxAssaultFireRating.setText("Use Assault Rating?");
    checkBoxAssaultFireRating.addActionListener(e -> {
      myModel.setIsAssaultRating(checkBoxAssaultFireRating.isSelected());
      myModel.update();
      refresh();
      }
    );

    defendingAssault = new JCheckBox("Defending Assault?");
    defendingAssault.setSelected(false);
    defendingAssault.addActionListener( e -> {
      myModel.setDefendingAssault(defendingAssault.isSelected());
      myModel.update();
      refresh();
    });

    ratingPanel.add(myWizard.getResolver().getControls(), "wrap");

    final JPanel smallPanel = new JPanel(new MigLayout("ins 0, hidemode 3", "[]"));
    // smallPanel.add(checkBoxRawFireRating); Hide Raw Assault rating box for now
    smallPanel.add(checkBoxAssaultFireRating, "wrap");
    // GTS2 Rules require different handling of terrain mods for Assaulting units as opposed to defending units
    if (UnitInfo.isGTS2Rules()) {
      smallPanel.add(defendingAssault);
    }
    ratingPanel.add(smallPanel);
        
    topPanel.add(sourcePanel);
    topPanel.add(ratingPanel);
    topPanel.add(targetPanel, "wrap");

    mainPanel.add(topPanel, WIDTH+",wrap");
     // Central Panel for Counter Battery Fire - Counter Battery description
//    if (myModel.canCounterBatteryFire()) {
//      specialAttack = new JPanel(new MigLayout("hidemode 3","[grow]"));
//      final JLabel specialLabel = new JLabel(myModel.getSmlModeString());
//      specialLabel.setFont(boldFont);
//      specialAttack.add(specialLabel, "align center");
//      mainPanel.add(specialAttack,WIDTH+",wrap");
//    }
    
    // Central Panel for Air Attack - Air Defence roll - D-Day rules only
    if (myModel.isAirPower() && UnitInfo.isTgdRules()) {
      specialAttack = new JPanel(new MigLayout("hidemode 3","[center,grow]"));
      final int airDefenceRating = myModel.getAirDefenceRating();
      final String airDefenceTerrain = myModel.getAirDefenceDescription();
      if (airDefenceRating == 0) {
        final JLabel label = new JLabel("No inherent Air Defense");
        label.setFont(boldFont);
        specialAttack.add(label);
      }
      else {
        final JLabel label = new JLabel("Air Defense Rating: "+airDefenceRating+" ("+airDefenceTerrain+")");
        label.setFont(boldFont);
        final JPanel panel = new JPanel();
        panel.add(label);
        
        final JButton button = new JButton(new OpIcon(Op.load("small_die.png")));
        final Dimension d = new Dimension(25, 25);
        button.setMinimumSize(d);
        button.setMinimumSize(d);
        button.setPreferredSize(d);
        button.addActionListener(e -> myWizard.doAirDefenceRoll());
        panel.add(button);
        specialAttack.add(panel);
        
      }
      mainPanel.add(specialAttack,WIDTH+",wrap");
    }
    
    attackPanels = new JPanel[AttackModel.MODES.length];
    defencePanels = new JPanel[AttackModel.MODES.length];
    
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      buildModePanels(i);
    }
    
    JLabel l = new JLabel("Attack Modifers");
    l.setFont(boldFont);
    botPanel.add(l, "align center");
    
    l = new JLabel("Defence Modifers");
    l.setFont(boldFont);
    botPanel.add(l, "align center, wrap");
    
    botPanel.add(attackPanels[0], "w :200:");
    botPanel.add(attackPanels[1], "w :200:");
    botPanel.add(attackPanels[2], "w :200:");
    botPanel.add(attackPanels[3], "w :200:");
    
    
    botPanel.add(defencePanels[0], "w :200:");
    botPanel.add(defencePanels[1], "w :200:");
    botPanel.add(defencePanels[2], "w :200:");
    botPanel.add(defencePanels[3], "w :200:");

    mainPanel.add(botPanel, WIDTH+",wrap");

    myModel.setDefendingAssault(defendingAssault.isSelected());
    myModel.update();
    refresh();
  }
  
  
  protected void buildModePanels(int mode) {
    attackPanels[mode] = new JPanel(new MigLayout("wrap 1, hidemode 3"));
    attackPanels[mode].setBorder(BorderFactory.createEtchedBorder());    
    
    defencePanels[mode] = new JPanel(new MigLayout("wrap 1, hidemode 3"));
    defencePanels[mode].setBorder(BorderFactory.createEtchedBorder());
    
         
    for (FireModifier mod : myModel.getAttackModifiers()) {
      if (mod.isUsedInMode(mode)) {
        attackPanels[mode].add(mod.getControls(mode));
      }
    }
    
    for (FireModifier mod : myModel.getDefenceModifiers()) {
      if (mod.isUsedInMode(mode)) {
        defencePanels[mode].add(mod.getControls(mode));
      }
    }
 
  }
  
  public void changeMode(int mode) {
    for (int i = 0; i < AttackModel.MODES.length; i++) {
      modeButtons[i].setBackground(mode == i ? selectedColor : unselectedColor);      
      modeButtons[i].setFont(i == mode ? boldFont : normalFont);     
      attackPanels[i].setVisible(i == mode);
      defencePanels[i].setVisible(i == mode);
    }
    myModel.changeMode(mode);
    changeTarget(myModel.getTargetIndex());
    refresh();
  }
  
  public void doRoll(int type, int roll) {
    myWizard.doRoll(type, roll);
  }

}