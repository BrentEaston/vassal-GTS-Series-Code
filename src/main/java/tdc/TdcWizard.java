/*
 * $Id: TdcWizard 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2006-2012 by Brent Easton
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
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.KeyCommandSubMenu;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.PieceVisitor;
import VASSAL.counters.PieceVisitorDispatcher;
import VASSAL.counters.Stack;
import VASSAL.tools.NamedKeyStroke;
import VASSAL.tools.SequenceEncoder;

/**
 * Invoke the Attack Wizard
 */
public class TdcWizard extends Decorator implements EditablePiece {
  public static final String ID = "wizard;";
  public static final String ATTACK_COMMAND = "Attack";
  public static final String ATTACK_WITH_COMMAND = "Attack With";
  public static final String ASSAULT_COMMAND = "Assault";
  public static final String INFO_COMMAND = "Info";
  public static final String NO_UNITS = "No Units Available";
  public static final String NO_LONG_RANGE_UNITS = "No Long Range Units";
  public static final String NO_MEDIUM_RANGE_UNITS = "No Medium Range Units";
  public static final String NO_SHORT_RANGE_UNITS = "No Short Range Units";
  public static final String NO_LANDING_WAVE_UNITS = "No Landing Wave Units";
  public static final String LONG_RANGE = "Long Range (+30)";
  public static final String MEDIUM_RANGE = "Medium Range (+20)";
  public static final String SHORT_RANGE = "Short Range (+10)";
  public static final String LANDING_WAVE = "Landing Wave";

  protected KeyCommand attackCommand;
  protected KeyStroke attackKey;
  protected KeyCommand infoCommand;
  protected KeyStroke infoKey;
  protected KeyCommand assaultCommand;
  protected KeyStroke assaultKey;
  protected boolean peeking = false;

  protected BufferedImage infoImage;
  protected UnitInfo info;

  protected AttackWizard myWizard;
  protected AssaultWizard myAssaultWizard;

  protected final ArrayList<KeyCommand> keyCommands = new ArrayList<>();

  protected ArrayList<TargetReference> targets = new ArrayList<>();
  protected ArrayList<GamePiece> units = new ArrayList<>();

  protected KeyCommand beachCommand;
  protected KeyCommand nestCommand;
  protected KeyCommand obstacleCommand;
  protected KeyCommand seaStateCommand;
  protected boolean isAirPower = false;
  protected boolean isArtilleryContactMarker = false;
  protected boolean isEmplacedArtilleryContactMarker = false;
  protected GamePiece artilleryParkMarker;
  protected GamePiece artilleryUnit;
  protected boolean isNavalUnit = false;
  protected GamePiece assaultForceMarker;
  protected KeyCommand airDefenceCommand;

  public TdcWizard() {
    this(ID + "65,520;73,520", null);
  }

  public TdcWizard(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    attackKey = st.nextKeyStroke(KeyStroke.getKeyStroke('A', java.awt.event.InputEvent.ALT_MASK));
    infoKey = st.nextKeyStroke(KeyStroke.getKeyStroke('I', java.awt.event.InputEvent.ALT_MASK));
    assaultKey = st.nextKeyStroke(KeyStroke.getKeyStroke('X', java.awt.event.InputEvent.ALT_MASK));
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(attackKey);
    se.append(infoKey);
    se.append(assaultKey);
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    keyCommands.clear();
    units.clear();
    targets.clear();
    info = new UnitInfo(Decorator.getOutermost(this), true);

    // System.out.println(piece.getProperty(BasicPiece.BASIC_NAME)+" - "+piece.getProperty(TdcProperties.TYPE));
    
    // Air and Partisan attacks are special attacks on the same stack as the source, not LOS thread.
    if (TdcProperties.CLASS_AIRPOWER.equals(piece.getProperty(TdcProperties.CLASS)) || TdcProperties.CLASS_PARTISAN.equals(piece.getProperty(TdcProperties.CLASS)) ) {
     isAirPower = true;
     attackCommand = new KeyCommand(ATTACK_COMMAND, attackKey, Decorator.getOutermost(this));
     attackCommand.setEnabled(getMap() != null && getMap() instanceof TdcMap);
     keyCommands.add(attackCommand);
    }
    // Attacks using an Artillery Park, Artillery Force or Naval Target marker must build a list of units in their matching Artillery Park/Naval Box
    else if (TdcProperties.TYPE_ARTILLERY_PARK.equals(piece.getProperty(TdcProperties.TYPE))) {
      if (TdcProperties.DIVISION_NAVAL.equals(piece.getProperty(TdcProperties.DIVISION))) {
        buildAssaultForceCommands();
      }
      else if (TdcProperties.NAVAL_PERNELLE_RANGE.equals(piece.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
        buildArtilleryForceCommands();  
      }
      else {
        buildArtilleryParkCommands();
      }
    }
    // Both Artillery-Contact and EmplacedArtillery-Contact prototypes use the Marker trait Type = "ArtilleryMarker" to be identified
    // Attacking with an Artillery Contact marker, attack with the Matching unit located in  the Artillery Park marker
    // Attacking with an Emplaced Artillery Contact marker, attack with the Matching unit assumed to be on the map
    // Exceptions are Utah:Maisy and Utah:La Pernelle
    else if (TdcProperties.TYPE_ARTILLERY_MARKER.equals(piece.getProperty(TdcProperties.TYPE))) {

      findArtilleryPark();

      isArtilleryContactMarker = true;
      attackCommand = new KeyCommand(ATTACK_COMMAND, attackKey, Decorator.getOutermost(this));
      attackCommand.setEnabled((getMap() != null) && (getMap() instanceof TdcMap) && ((artilleryParkMarker != null) || isEmplacedArtilleryContactMarker));
      keyCommands.add(attackCommand);
    }
    // If naval unit, locate the assault force marker and start attack from there
    else if (info.isIndirectWithForceMarker()) {
      isNavalUnit = true;
      if (myWizard == null) {
        final Map map = getMap();
        if ((map != null) && (map instanceof TdcMap)) {
          myWizard = ((TdcMap) map).getAttackWizard();
        }
      }
      if (myWizard != null) {
        assaultForceMarker = myWizard.findAssaultForceMarker((String) piece.getProperty(TdcProperties.NAVAL_PARK));
        if (assaultForceMarker != null) {
          attackCommand = new KeyCommand(ATTACK_COMMAND, attackKey, Decorator.getOutermost(assaultForceMarker));
          attackCommand.setEnabled((getMap() != null) && (getMap() instanceof TdcMap) && (assaultForceMarker != null));
          keyCommands.add(attackCommand);
        }
      }
    }
    // Standard Attack
    else {
      attackCommand = new KeyCommand(ATTACK_COMMAND, attackKey, Decorator.getOutermost(this));
      attackCommand.setEnabled(getMap() != null && getMap() instanceof TdcMap);
      keyCommands.add(attackCommand);

      infoCommand = new KeyCommand(INFO_COMMAND, infoKey, Decorator.getOutermost(this));
      infoCommand.setEnabled(getMap() != null && getMap() instanceof TdcMap);
      keyCommands.add(infoCommand);
      
      assaultCommand = new KeyCommand(ASSAULT_COMMAND, assaultKey, Decorator.getOutermost(this));
      assaultCommand.setEnabled(getMap() != null && getMap() instanceof TdcMap && info.canAssault(false));
     //  keyCommands.add(assaultCommand);
    }

    // Naval Allied


    // Beach Defence Attack Commands
    final String terrain = (String) getProperty(TdcBasicPiece.CURRENT_TERRAIN);
    if (TdcProperties.TERRAIN_BEACH.equals(terrain) || info.isLCS()) {
      final KeyCommandSubMenu sub = new KeyCommandSubMenu("Beach", this, null);

      final String dd = (String) getProperty(TdcProperties.DD);

      String overlay = (String) getProperty(TdcProperties.OVERLAY);
      if (overlay != null && overlay.startsWith("Utah-")) overlay = "Utah";
      final GameModule gm = GameModule.getGameModule();
      final boolean nestsRemaining = !"0".equals(gm.getProperty(overlay + "-Nests"));
      final boolean obstaclesRemaining = !"0".equals(gm.getProperty(overlay + "-Obstacles"));
      final boolean gapsRemaining = !"0".equals(gm.getProperty(overlay + "-Gaps"));
      int beachCmdCount = 0;

      if (nestsRemaining || obstaclesRemaining || gapsRemaining) {
        beachCommand = new KeyCommand(AttackWizard.ATTACK_BEACH_DEFENCES, new NamedKeyStroke(KeyStroke.getKeyStroke('B', java.awt.event.InputEvent.ALT_MASK)),
            this);
        keyCommands.add(beachCommand);
        beachCmdCount++;
      }

      if ("true".equals(dd)) {
        seaStateCommand = new KeyCommand(AttackWizard.SEA_STATE_ATTACK, new NamedKeyStroke(KeyStroke.getKeyStroke('S', java.awt.event.InputEvent.ALT_MASK)),
            this);
        keyCommands.add(seaStateCommand);
        beachCmdCount++;
      }

      if (nestsRemaining && !info.isLCS()) {
        nestCommand = new KeyCommand(AttackWizard.RESISTANCE_NEST_ATTACK, new NamedKeyStroke(KeyStroke.getKeyStroke('N', java.awt.event.InputEvent.ALT_MASK)),
            this);
        keyCommands.add(nestCommand);
        beachCmdCount++;
      }

      if (info.isLandingCraft() && obstaclesRemaining) {
        obstacleCommand = new KeyCommand(AttackWizard.BEACH_OBSTACLES_ATTACK, new NamedKeyStroke(
            KeyStroke.getKeyStroke('O', java.awt.event.InputEvent.ALT_MASK)), this);
        keyCommands.add(obstacleCommand);
        beachCmdCount++;
      }

      if (beachCmdCount > 0) {
        sub.setCommands(new String[] { AttackWizard.ATTACK_BEACH_DEFENCES, AttackWizard.RESISTANCE_NEST_ATTACK, AttackWizard.BEACH_OBSTACLES_ATTACK,
            AttackWizard.SEA_STATE_ATTACK });
        keyCommands.add(sub);
      }
    }

    // Air Defence Attack
    if ("true".equals(Decorator.getOutermost(this).getProperty(TdcProperties.IS_AIR_DEFENCE))) {
      airDefenceCommand = new KeyCommand(AttackWizard.AIR_DEFENCE_ATTACK, new NamedKeyStroke(AttackWizard.AIR_DEFENCE_ATTACK),
          this);
      keyCommands.add(airDefenceCommand);
    }
    
    //
    return keyCommands.toArray(new KeyCommand[0]);
  }

  // Find the Artillery Park marker containing the Artillery unit that matches this Artillery Park Marker
  protected void findArtilleryPark() {
    artilleryParkMarker = null;
    artilleryUnit = null;
    String artilleryParkId = null;
    String artilleryParkDivision = null;
    
    // 1. Find the name of the matching Artillery Unit
    final String artilleryUnitName = (String) piece.getProperty(ArtilleryMarker.MARKER_NAME);
    if (artilleryUnitName == null || artilleryUnitName.length() == 0) return;
    
    // 2. Find the Artillery unit
    for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
      if (artilleryUnitName.equals(piece.getProperty(BasicPiece.BASIC_NAME))) {
        // is the Artillery unit in a Park?
        if ("Arty Park".equals(piece.getProperty(BasicPiece.CURRENT_ZONE))) {
          artilleryUnit = piece;
          artilleryParkId = (String) piece.getProperty(BasicPiece.LOCATION_NAME);
          artilleryParkDivision = (String) piece.getProperty(TdcProperties.MAP_DIVISION);
          break;
        }
        // the unit is not in a Park, is it an Emplaced Artillery? (should be, what else has contacts?)
        UnitInfo pieceInfo = new UnitInfo(piece, false);
        if (pieceInfo.isEmplacedArtillery()) {
            artilleryUnit = piece;
            isEmplacedArtilleryContactMarker = true;
            // return here, next section searches for the Park unit on map
            // Exception. Utah. La Pernelle I and II are Emplaced , are in a "Naval Box" but have a Force Marker
            // (that acts as a Park Counter + range mod)
            if (TdcProperties.NAVAL_PERNELLE_RANGE.equals(piece.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
              artilleryParkId = TdcProperties.NAVAL_PERNELLE_RANGE;
              artilleryParkDivision = "709"; //(String) piece.getProperty(TdcProperties.MAP_DIVISION);
              break;
            }
            return;
        }
      }
    }

      if (artilleryUnit == null || artilleryParkId == null || artilleryParkDivision == null || getMap() == null) return;
    
    // 3. Locate the matching Artillery Park counter
        for (GamePiece p : getMap().getAllPieces()) {
            if (p instanceof Stack) {
              for (int i = 0; i < ((Stack) p).getPieceCount(); i++) {
                final GamePiece unit = ((Stack) p).getPieceAt(i);

                if (TdcProperties.TYPE_ARTILLERY_PARK.equals(unit.getProperty(TdcProperties.TYPE))) {
            if (artilleryParkDivision.equals(unit.getProperty(TdcProperties.DIVISION))) {
              if (artilleryParkId.equals(unit.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
                artilleryParkMarker = unit;
                break;
              }
            }        
          }
        }
        if (artilleryParkMarker != null) {
          break;
        }
      }
    }

  }

  protected void buildArtilleryForceCommands() {
    final KeyCommandSubMenu sub = new KeyCommandSubMenu(ATTACK_WITH_COMMAND, this, null);
    sub.setEnabled(getMap() != null && getMap() instanceof TdcMap);
    
    final String parkId = (String) piece.getProperty(TdcProperties.ARTILLERY_PARK_ID);
    final Map map = piece.getMap();
    if (map == null) {
      return;
    }
    
    // Locate all Artillery Units in the matching Zone with the matching Artillery Park Id
    for (GamePiece p : map.getPieces()) {
      if (p instanceof Stack) {
        final Stack stack = (Stack) p;
        for (int i = 0; i < stack.getPieceCount(); i++) {
          final GamePiece unit = stack.getPieceAt(i);
          if (parkId.equals(unit.getProperty(TdcProperties.NAVAL_PARK))) {
             units.add(unit);
           }
        }
      }
    }

    // No Units, set up a disabled 'None available' command
    if (units.size() == 0) {
      KeyCommand c = new KeyCommand(NO_UNITS, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      sub.setCommands(new String[] { NO_UNITS });
      keyCommands.add(c);
      keyCommands.add(sub);
      return;
    }

    final String[] names = new String[units.size()];
    targets.clear();
    int i = 0;
    
    int addRange = 0;
    if (TdcProperties.NAVAL_PERNELLE_RANGE.equals(parkId)) {
      addRange = 10;
    }
    if (TdcProperties.NAVAL_MAISY_RANGE.equals(parkId)) {
      addRange = 1;
    }
    
    for (GamePiece unit : units) {
      final String name = (String) unit.getProperty(BasicPiece.BASIC_NAME);
      names[i] = name;
      final NamedKeyStroke key = new NamedKeyStroke(name);
      targets.add(new TargetReference(unit, key, addRange));
      keyCommands.add(new KeyCommand(name, key, this));
      i++;
    }

    sub.setCommands(names);
    keyCommands.add(sub);
  
  }
  
  protected void buildArtilleryParkCommands() {
    final KeyCommandSubMenu sub = new KeyCommandSubMenu(ATTACK_WITH_COMMAND, this, null);
    sub.setEnabled(getMap() != null && getMap() instanceof TdcMap);

    final String aparkMap = (String) piece.getProperty(TdcProperties.ARTILLERY_PARK_MAP);
    final String parkId = (String) piece.getProperty(TdcProperties.ARTILLERY_PARK_ID);
    final Map map = Map.getMapById(aparkMap);
    if (map == null) {
      return;
    }

    // Locate all Artillery Units in the matching Artillery Park on the matching
    // Division display
    for (GamePiece p : map.getPieces()) {
      if (p instanceof Stack) {
        final Stack stack = (Stack) p;
        for (int i = 0; i < stack.getPieceCount(); i++) {
          final GamePiece unit = stack.getPieceAt(i);
          if ("true".equals(unit.getProperty(TdcProperties.IS_ARTILLERY))) {
            final String zone = (String) unit.getProperty(BasicPiece.CURRENT_ZONE);
            final String location = (String) unit.getProperty(BasicPiece.LOCATION_NAME);
            if (zone != null && zone.contains(TdcProperties.TGD_ARTY_PARK) && location.endsWith(parkId)) {
              units.add(unit);
            }
          }
        }
      }
    }

    // No Units, set up a disabled 'None available' command
    if (units.size() == 0) {
      KeyCommand c = new KeyCommand(NO_UNITS, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      sub.setCommands(new String[] { NO_UNITS });
      keyCommands.add(c);
      keyCommands.add(sub);
      return;
    }

    final String[] names = new String[units.size()];
    targets.clear();
    int i = 0;
    
    for (GamePiece unit : units) {
      final String name = (String) unit.getProperty(BasicPiece.BASIC_NAME);
      names[i] = name;
      final NamedKeyStroke key = new NamedKeyStroke(name);
      targets.add(new TargetReference(unit, key, 0));
      keyCommands.add(new KeyCommand(name, key, this));
      i++;
    }

    sub.setCommands(names);
    keyCommands.add(sub);
  }

  protected void buildAssaultForceCommands() {
    KeyCommandSubMenu sub;
    KeyCommand c;
    String[] names;
    
    final String parkId = (String) getProperty(TdcProperties.ARTILLERY_PARK_ID);
    final NavalUnitVisitor visitor = new NavalUnitVisitor(parkId);
    final PieceVisitorDispatcher dispatcher = new PieceVisitorDispatcher(visitor);
    for (GamePiece p : GameModule.getGameModule().getGameState().getAllPieces()) {
      dispatcher.accept(p);    
    }
    
    units.clear();
    targets.clear();
       
    // Long Range Units
    ArrayList<GamePiece> u = visitor.getLongRangeUnits();
    if (u.size() == 0) {
      c = new KeyCommand(NO_LONG_RANGE_UNITS, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      keyCommands.add(c);
      names = new String[] {NO_LONG_RANGE_UNITS};
    }
    else {
      names = new String[u.size()];
      int i = 0;
      for (GamePiece unit : u) {
        final String baseName = (String) unit.getProperty(BasicPiece.BASIC_NAME);
        final NamedKeyStroke key = new NamedKeyStroke(baseName);
        c = new KeyCommand(baseName, key, this, null);
        keyCommands.add(c);
        targets.add(new TargetReference(unit, key, 30));
        names[i++] = baseName;
      }
    }
    
    sub = new KeyCommandSubMenu(LONG_RANGE, this, null);
    sub.setCommands(names);
    keyCommands.add(sub);
        
    // Medium Range Units
    u = visitor.getMediumRangeUnits();
    if (u.size() == 0) {
      c = new KeyCommand(NO_MEDIUM_RANGE_UNITS, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      keyCommands.add(c);
      names = new String[] {NO_MEDIUM_RANGE_UNITS};
    }
    else {
      names = new String[u.size()];
      int i = 0;
      for (GamePiece unit : u) {
        final String baseName = (String) unit.getProperty(BasicPiece.BASIC_NAME);
        final NamedKeyStroke key = new NamedKeyStroke(baseName);
        c = new KeyCommand(baseName, key, this, null);
        keyCommands.add(c);
        targets.add(new TargetReference(unit, key, 20));
        names[i++] = baseName;
      }
    }
    
    sub = new KeyCommandSubMenu(MEDIUM_RANGE, this, null);
    sub.setCommands(names);
    keyCommands.add(sub);

    // Short Range
    u = visitor.getShortRangeUnits();
    if (u.size() == 0) {
      c = new KeyCommand(NO_SHORT_RANGE_UNITS, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      keyCommands.add(c);
      names = new String[] {NO_SHORT_RANGE_UNITS};
    }
    else {
      names = new String[u.size()];
      int i = 0;
      for (GamePiece unit : u) {
        final String baseName = (String) unit.getProperty(BasicPiece.BASIC_NAME);
        final NamedKeyStroke key = new NamedKeyStroke(baseName);
        c = new KeyCommand(baseName, key, this, null);
        keyCommands.add(c);
        targets.add(new TargetReference(unit, key, 10));
        names[i++] = baseName;
      }
    }
    
    sub = new KeyCommandSubMenu(SHORT_RANGE, this, null);
    sub.setCommands(names);
    keyCommands.add(sub);
    
    // Top Level Attack Command.
    final KeyCommandSubMenu attackWith = new KeyCommandSubMenu(ATTACK_WITH_COMMAND, this, null);
    attackWith.setEnabled(getMap() != null && getMap() instanceof TdcMap);
    attackWith.setCommands(new String[] { LONG_RANGE, MEDIUM_RANGE, SHORT_RANGE });
    keyCommands.add(attackWith);

  }

  public String myGetState() {
    return "";
  }

  public Command myKeyEvent(KeyStroke stroke) {
    int addRange = 0;
    Command c = null;


    if ((getMap() == null) || !(getMap() instanceof TdcMap)) { return c; }
    myWizard = ((TdcMap) getMap()).getAttackWizard();
    myGetKeyCommands();

    // Attack Command?
    if (attackCommand != null && attackCommand.matches(stroke)) {
//      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        if (isAirPower) {
          myWizard.launchAirPowerAttack(Decorator.getOutermost(this));
        }
        else if (isArtilleryContactMarker) {
          //Some contacts need addRange (only one in fact, Utah Maisy)
          try {
            final String a = (String) Decorator.getOutermost(artilleryParkMarker).getProperty("AddRange");
            addRange = Integer.parseInt(a);
          }
          catch (Exception e) {
            addRange = 0;
          }
          myWizard.launch (artilleryUnit, artilleryParkMarker, addRange);
        }
        else if (isNavalUnit) {
          myWizard.launch (Decorator.getOutermost(this), assaultForceMarker, 0);
        }
        else {
          myWizard.launch(Decorator.getOutermost(this));
        }
      }
      return c;
    }
    
    // Air Defence Attack?
    if (airDefenceCommand != null && airDefenceCommand.matches(stroke)) {
//      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        myWizard.launchAirDefenceAttack(Decorator.getOutermost(this));
      }
      return c;
    }

    // Info Command?
    if (infoCommand != null && infoCommand.matches(stroke)) {
 //      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        info = new UnitInfo(Decorator.getOutermost(this), true);
        infoImage = info.getInfoImage();
        myWizard.addActiveInfo(this);
      }
      return c;
    }

    // An Attack With Unit command?
    for (TargetReference target : targets) {
      if (target.getKeyStroke().equals(stroke)) {
//        myWizard = ((TdcMap) getMap()).getAttackWizard();
        if (myWizard != null) {
          try {
            final String a = (String) Decorator.getOutermost(this).getProperty("AddRange");
            addRange = Integer.parseInt(a);
          }
          catch (Exception e) {
            addRange = 0;
          }
          myWizard.launch(target.getUnit(), Decorator.getOutermost(this), target.getRange() + addRange);
        }
      }
    }

    // Beach attacks?
    if (beachCommand != null && beachCommand.matches(stroke)) {
//      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        myWizard.launchBeachDefenceAttack(Decorator.getOutermost(this));
      }
      return c;
    }

    if (nestCommand != null && nestCommand.matches(stroke)) {
//      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        myWizard.launchSpecialAttack(Decorator.getOutermost(this), AttackWizard.NEST_SPECIAL_ATTACK);
      }
      return c;
    }
    if (obstacleCommand != null && obstacleCommand.matches(stroke)) {
//      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        myWizard.launchSpecialAttack(Decorator.getOutermost(this), AttackWizard.OBSTACLE_SPECIAL_ATTACK);
      }
      return c;
    }
    if (seaStateCommand != null && seaStateCommand.matches(stroke)) {
      myWizard = ((TdcMap) getMap()).getAttackWizard();
      if (myWizard != null) {
        myWizard.launchSpecialAttack(Decorator.getOutermost(this), AttackWizard.SEA_STATE_SPECIAL_ATTACK);
      }
      return c;
    }
    
    // Assault?
    if (assaultCommand != null && assaultCommand.matches(stroke)) {
      myAssaultWizard = ((TdcMap) getMap()).getAssaultWizard();
      if (myAssaultWizard != null) {
        myAssaultWizard.launch(Decorator.getOutermost(this));
      }      
      return c;
    }
    
    return c;
  }

  protected void attackWith(GamePiece unit) {

  }

  public BufferedImage getInfoImage() {
    return infoImage;
  }

  public void mySetState(String newState) {
  }

  public void setProperty(Object key, Object val) {
    super.setProperty(key, val);
  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);
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
    return "Attack Wizard";
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public static class Ed implements PieceEditor {
    protected HotKeyConfigurer attackKeyInput;
    protected HotKeyConfigurer infoKeyInput;
    protected JPanel controls;

    public Ed(TdcWizard p) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      attackKeyInput = new HotKeyConfigurer(null, "Attack Wizard Key:  ", p.attackKey);
      controls.add(attackKeyInput.getControls());

      infoKeyInput = new HotKeyConfigurer(null, "Info Key:  ", p.infoKey);
      controls.add(infoKeyInput.getControls());

    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append((KeyStroke) attackKeyInput.getValue()).append((KeyStroke) infoKeyInput.getValue());
      return ID + se.getValue();
    }

    public String getState() {
      return "";
    }
  }
  
    public static class NavalUnitVisitor implements PieceVisitor {

      protected String parkId;
      protected ArrayList<GamePiece> longRange = new ArrayList<>();
      protected ArrayList<GamePiece> mediumRange = new ArrayList<>();
      protected ArrayList<GamePiece> shortRange = new ArrayList<>();

      public NavalUnitVisitor(String overlay) {
        this.parkId = overlay;
      }

      public ArrayList<GamePiece> getLongRangeUnits() {
        return longRange;
      }

      public ArrayList<GamePiece> getMediumRangeUnits() {
        return mediumRange;
      }

      public ArrayList<GamePiece> getShortRangeUnits() {
        return shortRange;
      }

      public Object visitStack(Stack s) {
        for (int i = 0; i < s.getPieceCount(); i++) {
          apply(s.getPieceAt(i));
        }
        return null;
      }

      public Object visitDefault(GamePiece p) {
        apply(p);
        return null;
      }

      protected void apply(GamePiece p) {
        if (TdcProperties.DIVISION_NAVAL.equals(p.getProperty(TdcProperties.DIVISION))) {
          if (parkId.equals(p.getProperty(TdcProperties.NAVAL_PARK))) {
            final String navalBox = (String) p.getProperty(TdcProperties.NAVAL_BOX);
            if (navalBox != null) {
              if (TdcProperties.NAVAL_LONG_RANGE.equals(navalBox)) {
                if (! longRange.contains(p)) longRange.add(p);
              }
              else if (TdcProperties.NAVAL_MEDIUM_RANGE.equals(navalBox)) {
                if (! mediumRange.contains(p)) mediumRange.add(p);
              }
              else if (TdcProperties.NAVAL_SHORT_RANGE.equals(navalBox)) {
                if (! shortRange.contains(p)) shortRange.add(p);
              }
            }
          }
        }
      }


  }
    
  static class TargetReference {
    
   GamePiece unit;
   NamedKeyStroke keyStroke;
   int range;   
   
   public GamePiece getUnit() {
    return unit;
  }

  public void setUnit(GamePiece unit) {
    this.unit = unit;
  }

  public NamedKeyStroke getKeyStroke() {
    return keyStroke;
  }

  public void setKeyStroke(NamedKeyStroke keyStroke) {
    this.keyStroke = keyStroke;
  }

  public int getRange() {
    return range;
  }

  public void setRange(int range) {
    this.range = range;
  }

   
   public TargetReference (GamePiece unit, NamedKeyStroke keyStroke, int range) {
     this.unit = unit;
     this.keyStroke = keyStroke;
     this.range = range;
     
   }
  }
}