/*
 * $Id: FireResolver 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2017 by Brent Easton
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
 * 
 *  7-Jul-17 BE Added Crete CRT values
 */

package tdc.attack;

import VASSAL.build.GameModule;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.tools.imageop.Op;
import net.miginfocom.swing.MigLayout;
import tdc.AttackWizard;
import tdc.TdcProperties;
import tdc.TdcRatings;
import tdc.UnitInfo;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

public class FireResolver {
  
  public static final String RES_C = "C";
  public static final String RES_S = "S";
  public static final String RES_P = "S?";
  public static final String RES_1 = "1";
  public static final String RES_E = "E";
  
  public static final String BAR_M = "Miss";
  public static final String BAR_A1 = "Reduce any by 1";
  public static final String BAR_A2 = "Reduce any by 2";
  public static final String BAR_N1 = "Reduce Nests by 1";
  public static final String BAR_N2 = "Reduce Nests by 2";
  public static final String BAR_O1 = "Reduce Obstacles or Gaps by 1";
  public static final String BAR_O2 = "Reduce Obstacles or Gaps by 2";
  
  public static final int DO_ROLL = 0;
  public static final int DO_USE = 1;
  public static final int DO_REPORT = 2;
  
  public static final String BUTTON_ROLL = "Roll Button Only";
  public static final String BUTTON_USE = "Use Button Only";
  public static final String BUTTON_REPORT = "Report Button Only";
  public static final String BUTTON_SELECT = "Selectable Button";  
  
  public static final String ROLL_BUTTON = "RollButton";
  
  public static HashMap<String, String[]> armoured;
  public static HashMap<String, String[]> unarmoured;
  public static HashMap<String, String> description;
  public static HashMap<String, String[]> barResults;
  
  protected AttackWizard myWizard;
  protected JPanel controls;
  protected JPanel autoPanel;
  protected JPanel semiPanel;
  protected JPanel manualPanel;
  protected JComboBox<String> number;
  
  protected Icon normalImage;
  protected Icon pressedImage;
  protected JLabel option;
  
  protected StringEnumConfigurer buttonConfig;
  protected static int mode;
  
  static {
    armoured = new HashMap<>();
    unarmoured = new HashMap<>();
    description = new HashMap<>();
  
    armoured.put(TdcRatings.WHITE,  new String[] {RES_C, RES_S, RES_S, RES_C, RES_1, RES_1, RES_E, RES_E, RES_E});
    armoured.put(TdcRatings.PINK,   new String[] {RES_C, RES_P, RES_P, RES_S, RES_S, RES_C, RES_C, RES_1, RES_E});
    armoured.put(TdcRatings.PURPLE, new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_P, RES_P, RES_S, RES_S});
    armoured.put(TdcRatings.BROWN,  new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_C, RES_C, RES_1, RES_1});
    if (UnitInfo.isCreteRules()) {
      armoured.put(TdcRatings.BLUE,   new String[] {RES_C, RES_P, RES_P, RES_C, RES_1, RES_1, RES_E, RES_E, RES_E});
      armoured.put(TdcRatings.GREEN,  new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_S, RES_C, RES_1, RES_1});
    }
    else {
      armoured.put(TdcRatings.BLUE,   new String[] {RES_C, RES_S, RES_S, RES_C, RES_1, RES_1, RES_E, RES_E, RES_E});
      armoured.put(TdcRatings.GREEN,  new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_S, RES_C, RES_C, RES_1});
    }
    armoured.put(TdcRatings.BLACK,  new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_C, RES_C, RES_1, RES_1});
    
    armoured.put(TdcRatings.ORANGE, new String[] {RES_C, RES_P, RES_P, RES_P, RES_P, RES_C, RES_C, RES_1, RES_1});
    armoured.put(TdcRatings.YELLOW, new String[] {RES_C, RES_P, RES_P, RES_S, RES_S, RES_S, RES_C, RES_1, RES_1});
      
    unarmoured.put(TdcRatings.WHITE,  new String[] {RES_1, RES_S, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E, RES_E});
    unarmoured.put(TdcRatings.PINK,   new String[] {RES_C, RES_P, RES_P, RES_S, RES_C, RES_C, RES_1, RES_1, RES_E});
    if (UnitInfo.isCreteRules()) {
      unarmoured.put(TdcRatings.PURPLE, new String[] {RES_C, RES_P, RES_P, RES_P, RES_S, RES_C, RES_C, RES_E, RES_E});
    }
    else {
      unarmoured.put(TdcRatings.PURPLE, new String[] {RES_C, RES_P, RES_P, RES_P, RES_S, RES_C, RES_C, RES_1, RES_E});
    }
    unarmoured.put(TdcRatings.BROWN,  new String[] {RES_1, RES_P, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E, RES_E});
    unarmoured.put(TdcRatings.BLACK,  new String[] {RES_1, RES_P, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E, RES_E});
    unarmoured.put(TdcRatings.BLUE,   new String[] {RES_C, RES_P, RES_P, RES_P, RES_S, RES_S, RES_S, RES_C, RES_1});
    unarmoured.put(TdcRatings.GREEN,  new String[] {RES_1, RES_P, RES_S, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E});
    unarmoured.put(TdcRatings.ORANGE, new String[] {RES_1, RES_P, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E, RES_E});
    unarmoured.put(TdcRatings.YELLOW, new String[] {RES_1, RES_S, RES_S, RES_C, RES_C, RES_1, RES_E, RES_E, RES_E});
      
    description.put(RES_C, "Cohesion Hit");
    description.put(RES_S, "Suppression");
    description.put(RES_P, "Possible Supression");
    description.put(RES_1, "Step Loss");
    description.put(RES_E, "Elimination");   
    
    barResults = new HashMap<>();
    barResults.put (TdcProperties.BAC_79TH, new String[] {BAR_A2, BAR_A2, BAR_A2, BAR_A1, BAR_A1, BAR_A1, BAR_A1, BAR_M, BAR_M, BAR_M});
    barResults.put (TdcProperties.BAC_ENGINEER, new String[] {BAR_O2, BAR_O2, BAR_O2, BAR_O2, BAR_O1, BAR_O1, BAR_O1, BAR_O1, BAR_M, BAR_M});
    barResults.put (TdcProperties.BAC_TANK, new String[] {BAR_N2, BAR_N2, BAR_N2, BAR_N1, BAR_N1, BAR_N1, BAR_M, BAR_M, BAR_M, BAR_M});
    barResults.put (TdcProperties.BAC_LANDING_CRAFT, new String[] {BAR_N1, BAR_N1, BAR_N1, BAR_N1, BAR_M, BAR_M, BAR_M, BAR_M, BAR_M, BAR_M});
    barResults.put (TdcProperties.BAC_OTHER, new String[] {BAR_N1, BAR_N1, BAR_N1, BAR_M, BAR_M, BAR_M, BAR_M, BAR_M, BAR_M, BAR_M});

  }
  
  public static String resolve (String color, boolean armour, int roll) {
    if (armour) {
      return armoured.get(color)[roll]+" - "+description.get(armoured.get(color)[roll]);
    }
    else {
      return unarmoured.get(color)[roll]+" - "+description.get(unarmoured.get(color)[roll]);
    }
  }
  
  public static String resolveBeach (String attackClass, int roll) {
    return barResults.get(attackClass)[roll];
  }
  
  public FireResolver() {

  }
  
  public FireResolver(AttackWizard attackWizard) {
    myWizard = attackWizard;
    
    buttonConfig = new StringEnumConfigurer(ROLL_BUTTON, "Which Dice Roll button?", null);
    buttonConfig.setValidValues(new String[] {BUTTON_ROLL, BUTTON_USE, BUTTON_REPORT, BUTTON_SELECT});
    buttonConfig.setValue(BUTTON_SELECT);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, buttonConfig);

  }
  
  protected int getConfigMode() {
    final String m = (String) GameModule.getGameModule().getPrefs().getValue(ROLL_BUTTON);
    if (m.equals(BUTTON_ROLL)) {
      return 0;
    }
    else if (m.equals(BUTTON_USE)) {
      return 1;
    }
    else if (m.equals(BUTTON_REPORT)) {
      return 2;
    }
    return 0;
  }
  
  protected void setMode(int m) {
    mode = m;
  }

  protected void changeMode() {
    if (mode==0) {
      autoPanel.setVisible(false);
      semiPanel.setVisible(true);
      setMode(1);
    }
    else if (mode==1) {
      semiPanel.setVisible(false);
      manualPanel.setVisible(true);
      setMode(2);
    }
    else {
      manualPanel.setVisible(false);
      autoPanel.setVisible(true);
      setMode(0);      
    }
  }
  
  protected JComponent getControls() {
    JButton autoButton, semiButton, manualButton;

    if (controls == null) {
      controls = new JPanel(new MigLayout("hidemode 3","[grow]"));
      
      autoPanel = new JPanel(new MigLayout("insets 0","[grow]"));
      semiPanel = new JPanel(new MigLayout("insets 0","[grow]"));
      manualPanel = new JPanel(new MigLayout("insets 0","[grow]"));
      
      normalImage = new ImageIcon(Op.load("mm-extension-inactive.png").getImage());
      pressedImage = new ImageIcon(Op.load("mm-extension-active.png").getImage());

      option = new JLabel(normalImage);
      option.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          option.setIcon(pressedImage);
        }
        public void mouseReleased(MouseEvent e) {
          option.setIcon(normalImage);
          changeMode();
        }
      });

      autoButton = new JButton("Roll");
      autoButton.addActionListener(e -> doRoll());
      autoPanel.add(autoButton,"grow");
      controls.add(autoPanel,"w :80:,wrap");
       
      number = new JComboBox<>(new String[] {"0","1","2","3","4","5","6","7","8","9"});
      number.setMaximumRowCount(10);
      semiButton = new JButton("Use");
      semiButton.addActionListener(e -> doUse());
      semiPanel.add(number,"growy");
      semiPanel.add(semiButton,"growy");
      controls.add(semiPanel, "w :80:,wrap");
      
      manualButton = new JButton("Report");
      manualButton.addActionListener(e -> doReport());
      manualPanel.add(manualButton,"grow");
      controls.add(manualPanel,"w :80:,wrap");
      final String useButton = (String) GameModule.getGameModule().getPrefs().getValue(ROLL_BUTTON);
      if (BUTTON_SELECT.equals(useButton)) {
        controls.add(option, "center");    
      }
      else {
        setMode(getConfigMode());
      }
      autoPanel.setVisible(mode==0);
      semiPanel.setVisible(mode==1); 
      manualPanel.setVisible(mode==2);      
    }
    
    return controls;
  }
  
  protected void doRoll() {
    int roll = (int) (GameModule.getGameModule().getRNG().nextFloat() * 10);
    myWizard.doRoll(DO_ROLL, roll);
  }
  
  protected void doUse() {
    int roll = number.getSelectedIndex();
    myWizard.doRoll(DO_USE, roll);
  }
  
  protected void doReport() {
    myWizard.doRoll(DO_REPORT, 0);
  }
  
}