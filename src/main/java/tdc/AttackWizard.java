/*
 * $Id: AttackWizard 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2006-2017 by Brent Easton
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

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.counters.*;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.UniqueIdManager;
import tdc.attack.AttackModel;
import tdc.attack.AttackView;
import tdc.attack.FireModifier;
import tdc.attack.FireResolver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AttackWizard extends AbstractConfigurable implements Drawable, MouseListener, GameComponent, CommandEncoder, UniqueIdManager.Identifyable, Wizard {

  public static final String ATTACK_WIZARD_COMMAND = "Awiz\t";
  public static final String PREF_TAB = "Grand Tactical Series";

  public static final String WIZARD_OPEN = "Open";
  public static final String WIZARD_BEACH = "Beach";
  public static final String WIZARD_SPECIAL = "Special";
  public static final String WIZARD_CLOSE = "Close";
  public static final String WIZARD_MODE = "Mode";
  public static final String WIZARD_MODIFIER = "Mod";
  public static final String WIZARD_REPORT = "Report";
  public static final String WIZARD_TARGET = "Target";

  public static final String AUTO_CLOSE = "AutoClose";

  public static boolean SHOW_WIZARD = true;

  public static int NO_SPECIAL_ATTACK = 0;
  public static int BEACH_SPECIAL_ATTACK = 1;
  public static int NEST_SPECIAL_ATTACK = 2;
  public static int OBSTACLE_SPECIAL_ATTACK = 3;
  public static int SEA_STATE_SPECIAL_ATTACK = 4;

  public static final String ATTACK_BEACH_DEFENCES = "Attack Beach Defences";
  public static final String RESISTANCE_NEST_ATTACK = "Resistance Nest Attack";
  public static final String BEACH_OBSTACLES_ATTACK = "Beach Obstacles Attack";
  public static final String SEA_STATE_ATTACK = "Sea State Attack";
  public static final String AIR_DEFENCE_ATTACK = "Air Defence Attack";

  public static final String[] SPECIAL_ATTACKS = new String[] { "", ATTACK_BEACH_DEFENCES, RESISTANCE_NEST_ATTACK, BEACH_OBSTACLES_ATTACK, SEA_STATE_ATTACK };

  public static final String[] SPECIAL_ATTACK_ICONS = new String[] { "", "marker-beach-defences.png", "marker-resistence-nests.png", "marker-beach-obstacles.png", "marker-sea-state.png" };
  public static final String[] SPECIAL_ATTACK_FIRE_COLORS = new String[] { "", "", TdcRatings.PINK, TdcRatings.WHITE, TdcRatings.BLUE };
  public static final String[] SPECIAL_ATTACK_GP_NAMES = new String[] { "", "", "Nests", "Obstacles", "Seastate" };
  public static final String[] SPECIAL_ATTACK_NAMES = new String[] { "", "Beach Defences", "Resistance Nests", "Beach Obstacles", "Sea State" };

  protected static UniqueIdManager idMgr = new UniqueIdManager("AttackWizard");

  protected GamePiece source;
  protected TdcMap map;
  protected WizardThread myThread;
  protected Point targetPoint;
  protected ArrayList<GamePiece> targetPieces = new ArrayList<>();
  protected UnitInfo sourceInfo;
  protected JDialog myDialog;
  protected JPanel dialogContent;
  protected AttackModel myModel;
  protected AttackView myView;
  protected FireResolver myResolver;
  protected HashMap<TdcWizard, InfoImage> activeInfo = new HashMap<>();
  protected String wizardID = "";
  protected BooleanConfigurer autoCloseConfig;
  protected boolean attackNotResolved;
  protected GamePiece artilleryPark;
  protected int specialAttackType = NO_SPECIAL_ATTACK;
  protected int addRange;
  protected boolean isAirDefence;

  public AttackWizard() {

  }

  protected GamePiece getAlternateSource( GamePiece source) {
    if ("true".equals(source.getProperty(TdcProperties.IS_ARTILLERY))) {
      // If artillery in park find the Park unit
      if (TdcProperties.TGD_ARTY_PARK.equals(source.getProperty(BasicPiece.CURRENT_ZONE))) {
        final String divisionId = (String) source.getProperty(TdcProperties.DIVISION);
        final String parkId = (String) source.getProperty(BasicPiece.LOCATION_NAME);
        if ((divisionId!=null)&(parkId!=null)) {
          // find the corresponding Artillery Park piece
          for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
            if (piece instanceof Stack) {
              final Stack stack = (Stack) piece;
              for (int i = 0; i < stack.getPieceCount(); i++) {
                final GamePiece p = stack.getPieceAt(i);
                if (TdcProperties.TYPE_ARTILLERY_PARK.equals(p.getProperty(TdcProperties.TYPE))) {
                  if (divisionId.equals(p.getProperty(TdcProperties.DIVISION)) && parkId.equals(p.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
                    return p;
                  }
                }
              }
            }
          }
        }
        // artillery park not found
        return null;
      }

      if (TdcProperties.DIVISION_NAVAL.equals(source.getProperty(TdcProperties.DIVISION)) &&
        !TdcProperties.CLASS_LANDING_CRAFT.equals(source.getProperty(TdcProperties.CLASS))) {
        final String parkId = (String) source.getProperty(TdcProperties.NAVAL_PARK);
        if (parkId != null && parkId.length() > 0) {
          for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
            if (piece instanceof Stack) {
              final Stack stack = (Stack) piece;
              for (int i = 0; i < stack.getPieceCount(); i++) {
                final GamePiece p = stack.getPieceAt(i);
                final String forceMarkerID = (String) p.getProperty(TdcProperties.ARTILLERY_PARK_ID);
                if (TdcProperties.DIVISION_NAVAL.equals(p.getProperty(TdcProperties.DIVISION))) {
                  if (forceMarkerID != null && forceMarkerID.equals(parkId)) {
                    return p;
                  }
                }
              }
            }
          }
          // artillery park not found
          return null;
        }
      }

      // If La Pernelle or Maisy, find Force marker
      final String parkId = (String) source.getProperty(TdcProperties.ARTILLERY_PARK_ID);
      if (TdcProperties.NAVAL_PERNELLE_RANGE.equals(parkId) || TdcProperties.NAVAL_MAISY_RANGE.equals(parkId)) {
        // find the corresponding Force piece
        for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
          if (piece instanceof Stack) {
            final Stack stack = (Stack) piece;
            for (int i = 0; i < stack.getPieceCount(); i++) {
              final GamePiece p = stack.getPieceAt(i);
              if (TdcProperties.TYPE_ARTILLERY_PARK.equals(p.getProperty(TdcProperties.TYPE))) {
                if ( parkId.equals(p.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
                  return p;
                }
              }
            }
          }
        }
        // artillery park not found
        return null;
      }

    }

    return null;
  }

  protected WizardThread getMyThread() {
    if (myThread == null) {
      myThread = new WizardThread(this);
    }
    return myThread;
  }

  // Draw Active Info Panels
  public void draw(Graphics g, Map map) {

    for (java.util.Map.Entry<TdcWizard, InfoImage> entry : activeInfo.entrySet()) {
      final InfoImage ii = entry.getValue();
      ii.draw(g, map);
    }
  }

  public boolean drawAboveCounters() {
    return true;
  }

  public void addActiveInfo(TdcWizard tw) {
    removeActiveInfo(tw);
    activeInfo.put(tw, new InfoImage(tw));
  }

  public void removeActiveInfo(TdcWizard tw) {
    activeInfo.remove(tw);
  }

  public FireResolver getResolver() {
    return myResolver;
  }

  // User has asked for an Attack Wizard. Use a modified LOS Thread to select a
  // target hex
  public void launch(GamePiece source) {

    launch(source, null);
  }

  public void launch(GamePiece source, GamePiece artilleryPark) {
    launch(source, artilleryPark, 0);
  }

  public void launch(GamePiece source, GamePiece artilleryPark, int addRange) {
    isAirDefence = false;
    this.addRange = addRange;
    specialAttackType = NO_SPECIAL_ATTACK;
    setSource(source);
    setArtilleryPark(artilleryPark);
    if (sourceInfo.isIndirectWithForceMarker()) {
      // Find assault force marker
      GamePiece assaultForceMarker = null;
      final String parkId = (String) source.getProperty(TdcProperties.NAVAL_PARK);
      if (parkId != null && parkId.length() > 0) {
        for (GamePiece un : source.getMap().getAllPieces()) {
          if (un instanceof Stack) {
            Stack s = (Stack) un;
            for (int i = 0; i < s.getPieceCount(); i++) {
              GamePiece u = s.getPieceAt(i);
              if (TdcProperties.TYPE_ARTILLERY_PARK.equals(u.getProperty(TdcProperties.TYPE))) {
                final String park = (String) u.getProperty(TdcProperties.ARTILLERY_PARK_ID);
                if (TdcProperties.DIVISION_NAVAL.equals(u.getProperty(TdcProperties.DIVISION))
                  || (UnitInfo.isTinianRules() && TdcProperties.DIVISION_4MARINES.equals(u.getProperty(TdcProperties.DIVISION)))
                  || TdcProperties.NAVAL_PERNELLE_RANGE.equals(park) || TdcProperties.NAVAL_MAISY_RANGE.equals(park)) {

                  if (park != null && park.equals(parkId)) {
                    assaultForceMarker = u;
                  }
                }
              }
            }
          }
          else {
            if (TdcProperties.TYPE_ARTILLERY_PARK.equals(un.getProperty(TdcProperties.TYPE))) {
              if (TdcProperties.DIVISION_NAVAL.equals(un.getProperty(TdcProperties.DIVISION))
                || (UnitInfo.isTinianRules() && TdcProperties.DIVISION_4MARINES.equals(un.getProperty(TdcProperties.DIVISION)))) {
                String park = (String) un.getProperty(TdcProperties.ARTILLERY_PARK_ID);
                if (park != null && park.equals(parkId)) {
                  assaultForceMarker = un;
                }
              }
            }
          }
        }
      }

      // Launch an attack with an indirect range
      if (assaultForceMarker == null) {
        getMyThread().launch(getOnMapPiece(), sourceInfo.getEffectiveRange(), sourceInfo.isNQOSArtinAP(), addRange);
      }
      else {
        getMyThread().launchIndirectNavalAttack(getOnMapPiece(), sourceInfo.getEffectiveRange(), assaultForceMarker, sourceInfo.getNavalRange());
      }
    }
    else {
      getMyThread().launch(getOnMapPiece(), sourceInfo.getEffectiveRange(), sourceInfo.isNQOSArtinAP(), addRange);
    }
  }




  public void launchBeachDefenceAttack(GamePiece piece) {
    specialAttackType = BEACH_SPECIAL_ATTACK;
    isAirDefence = false;
    setSource(piece);

    targetPieces.clear();
    addTargetPiece(new BeachPiece(BEACH_SPECIAL_ATTACK));

    buildBeachDialog();

    final Command wizCmd = new WizardBeachCommand(this, source.getId(), BEACH_SPECIAL_ATTACK);
    final Command reportCmd = getChatCommand("START ATTACK", true);
    reportCmd.append(getChatCommand(myModel.getAttackText(), false));
    reportCmd.execute();
    wizCmd.append(reportCmd);
    GameModule.getGameModule().sendAndLog(wizCmd);

    attackNotResolved = true;
    myDialog.setVisible(true);

  }

  // Launch a Special Attack - source unit does not physically exist
  public void launchSpecialAttack(GamePiece piece, int specialAttack) {
    specialAttackType = specialAttack;
    isAirDefence = false;

    source = new BeachPiece(specialAttack);

    final String overlay = (String) piece.getProperty(TdcProperties.DEFENCE_OVERLAY);
    sourceInfo = new AttackModel.BeachAttackUnitInfo(source, specialAttack, overlay);

    targetPieces.clear();
    addTargetPiece(piece);

    buildBeachDialog();

    final Command wizCmd = new WizardBeachCommand(this, piece.getId(), specialAttack);
    final Command reportCmd = getChatCommand("START ATTACK", true);
    reportCmd.append(getChatCommand(myModel.getAttackText(), false));
    reportCmd.execute();
    wizCmd.append(reportCmd);
    GameModule.getGameModule().sendAndLog(wizCmd);

    // Set it off
    myDialog.setVisible(true);

  }

  // Launch an Airpower attack against enemy units in the source's current hex,
  // no thread display.
  public void launchAirPowerAttack(GamePiece piece) {
    source = piece;
    sourceInfo = new UnitInfo(piece, true);
    isAirDefence = false;
    targetSelected(source.getPosition(), 0, null);
  }

  public void launchAirDefenceAttack(GamePiece piece) {
    launch(piece);
    getMyThread().setRange(8);
    isAirDefence = true;
  }

  public void setSource(GamePiece source) {
    this.source = source;
    sourceInfo = new UnitInfo(source, true);
  }

  public GamePiece getSource() {
    return source;
  }

  public void setArtilleryPark(GamePiece park) {
    artilleryPark = park;
  }

  protected GamePiece getOnMapPiece() {
    return artilleryPark == null ? source : artilleryPark;
  }

  public int getRangeToTarget() {
    return myModel == null ? 1 : myModel.getRange();
  }

  // Target has been changed by another client
  public void changeTarget(int t) {
    myView.changeTarget(t);
  }

  public void setSpecialAttackType(int type) {
    specialAttackType = type;
  }

  public void setAirDefence(boolean b) {
    isAirDefence = b;
  }

  public void clearTargetPieces() {
    targetPieces.clear();
  }

  public void addTargetPiece(GamePiece piece) {
    targetPieces.add(piece);
  }

  public boolean isAltArtResolutionActive() {
    return false;
  }
  public void filterAlternateArtilleryResolution() {
    //targetPiecesAltArtExcluded
    if (isAltArtResolutionActive()) {

    }
  }

  public List<GamePiece> getTargetPieces() {
    return targetPieces;
  }

  public void setSourceInfo(UnitInfo info) {
    sourceInfo = info;
  }

  public void setAttackNotResolved(boolean b) {
    attackNotResolved = b;
  }

  public void setDialogVisibility(boolean b) {
    myDialog.setVisible(b);
  }

  /*
   * User has selected a hex on the current map using the target selector. p is
   * the centre of the selected hex. Called back from WizardThread when user
   * selects a target stack
   */

  public void targetSelected(Point p, int rangeToTarget, WizardThread losThread) {

    if (SHOW_WIZARD) {
      try {
        setTarget(p, rangeToTarget);
      }
      catch (Exception e) {
        getMyThread().setVisible(false);
        return;
      }
      if (targetPieces.size() > 0) {
        final Command wizCmd = new WizardOpenCommand(this, source.getId(), targetPoint, rangeToTarget);
        final Command reportCmd = getChatCommand("START ATTACK", true);
        reportCmd.append(getChatCommand(myModel.getAttackText() + " - " + myModel.getSmlModeString(), false));
        if (losThread != null && losThread.isBlocked()) {
          reportCmd.append(getChatCommand("*** LOS May be blocked by terrain: " + losThread.getBlockingReasons() + " ***", false));
        }
        reportCmd.execute();
        wizCmd.append(reportCmd);
        GameModule.getGameModule().sendAndLog(wizCmd);
        attackNotResolved = true;
      }
      else {
        getMyThread().setVisible(false); // N target selected, just hide the thread
      }
    }
    else {
      wizardCloses();
    }
  }

  public Point getTargetPoint() {
    return targetPoint;
  }

  public void setTarget(Point p, int rangeToTarget) {
    boolean found;
    setArtilleryPark(null);
    targetPieces = new ArrayList<>();
    targetPoint = map.snapTo(p);

    // Locate the stack of units in the target Hex and adjacent enemy units
    GamePiece[] pieces = map.getPieces();
    for (GamePiece piece : pieces) {
      // If it is a stack, see if it is in the target hex and that it contains
      // at least one enemy unit. If its position does not change when snapped,
      // then it is in an offboard box, so check the bounds of the piece
      if (piece instanceof Stack) {
        final Point pp = map.snapTo(piece.getPosition());

        if (pp.equals(piece.getPosition()) && piece.getMap() != null) {
          // Use a standard counter size for the bounding box so we don't pick movement trails
          final Rectangle box = new Rectangle (-37, -37, 75, 75);
          found = targetPoint.x >= pp.x + box.x && targetPoint.x <= pp.x + box.x + box.width && targetPoint.y >= pp.y + box.y && targetPoint.y <= pp.y + box.y + box.height;
        }
        //
        else {
          found = pp.equals(targetPoint);
        }

        if (found) {
          final Stack stack = (Stack) piece;
          for (Iterator<GamePiece> i = stack.getPiecesIterator(); i.hasNext();) {
            final GamePiece gp = i.next();
            final String type = (String) gp.getProperty(TdcProperties.TYPE);
            final String unitClass = (String) gp.getProperty(TdcProperties.CLASS);
            if (TdcRatings.isUnitType(type) || TdcRatings.isBeachDefenceType(type)) {
              if (!sourceInfo.getArmy().equals(gp.getProperty(TdcProperties.ARMY))) {
                addTargetPiece(gp);
              }
            }
            else if (TdcRatings.isArtilleryParkType(type)) {
              if (!sourceInfo.getArmy().equals(gp.getProperty(TdcProperties.ARMY))) {
                // Counter-Battery fire v Artillery Park
                addArtilleryParkUnits(gp);
              }
            }
            // Air Defence Unit v Air Power counter
            else if (sourceInfo.hasAirDefence() && TdcProperties.CLASS_AIRPOWER.equals(unitClass)) {
              addTargetPiece(gp);
            }
          }
        }
      }
    }

    if (targetPieces.size() > 0) {
      buildDialog(rangeToTarget);
      myDialog.setVisible(true);
      // Send focus back to Main Frame
      GameModule.getGameModule().getPlayerWindow().requestFocus();
    }
  }

  public void buildDialog(int rangeToTarget) {
    initialiseDialog();


    myModel = new AttackModel(source, targetPieces, rangeToTarget, artilleryPark);
    myResolver = new FireResolver(this);
    myView = new AttackView(myModel, this);
    dialogContent = myView.getContent();
    myDialog.add(dialogContent);
    myView.changeTarget(0);
    myDialog.pack();

    positionDialog(artilleryPark == null ? source.getPosition() : artilleryPark.getPosition());

  }

  public void buildBeachDialog() {
    initialiseDialog();

    myModel = new AttackModel(source, targetPieces, 0, artilleryPark, specialAttackType);
    myResolver = new FireResolver(this);
    myView = new AttackView(myModel, this);
    dialogContent = myView.getContent();
    myDialog.add(dialogContent);
    myView.changeTarget(0);
    myDialog.pack();

    if (myModel.isBeachDefenceAttack()) {
      positionDialog(source.getPosition());
    }
    else {
      positionDialog(targetPieces.get(0).getPosition());
    }
  }

  public void initialiseDialog() {
    if (myDialog == null) {
      final Window w = SwingUtilities.getWindowAncestor(map.getView());
      if (w instanceof Dialog) {
        myDialog = new JDialog((Dialog) w);
      }
      else {
        myDialog = new JDialog((Frame) w);
      }

      myDialog.addWindowListener(new WindowListener() {
        public void windowActivated(WindowEvent arg0) {
        }

        public void windowClosed(WindowEvent arg0) {
        }

        public void windowClosing(WindowEvent arg0) {
          wizardCloses();
        }

        public void windowDeactivated(WindowEvent arg0) {
        }

        public void windowDeiconified(WindowEvent arg0) {
        }

        public void windowIconified(WindowEvent arg0) {
        }

        public void windowOpened(WindowEvent arg0) {
        }
      });
    }

    if (dialogContent != null) {
      myDialog.remove(dialogContent);
      dialogContent = null;
    }
    myDialog.setVisible(false);

  }

  protected void positionDialog(Point pos) {

    final Dimension dialogSize = myDialog.getSize();

    final Rectangle r = new Rectangle(pos.x, pos.y, dialogSize.width, dialogSize.height);
    final Rectangle viewRect = map.getView().getVisibleRect();
    Rectangle bounds = map.mapToComponent(r);
    int maxX = bounds.x + dialogSize.width;
    int maxV = viewRect.x + viewRect.width;
    if (maxX > maxV) {
      bounds.x -= (maxX - maxV);
      if (bounds.x < viewRect.x) {
        bounds.x = viewRect.x;
      }
    }
    int maxY = bounds.y + dialogSize.height;
    maxV = viewRect.y + viewRect.height;
    if (maxY > maxV) {
      bounds.y -= (maxY - maxV);
      if (bounds.y < viewRect.y) {
        bounds.y = viewRect.y;
      }
    }

    // Force the map open
    if (!map.getView().getTopLevelAncestor().isVisible()) {
      map.getView().getTopLevelAncestor().setVisible(true);
    }
    final Point cp = map.getView().getLocationOnScreen();
    int left = cp.x + bounds.x;
    int top = cp.y + bounds.y;
    if (left < 0)
      left = 0;
    if (top < 0)
      top = 0;
    myDialog.setLocation(left, top);
  }

  // User has selected a target hex containing an enemy Artillery Park, Add the
  // units from the Arty Park as targets (Ignore the Assault Force counters)
  public void addArtilleryParkUnits(GamePiece p) {
    if (TdcProperties.DIVISION_NAVAL.equals(p.getProperty(TdcProperties.DIVISION)) ||
      TdcProperties.NAVAL_PERNELLE_RANGE.equals(p.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
      return;
    }

    artilleryPark = p;
    final String divMapName = p.getProperty(TdcProperties.ARTILLERY_PARK_MAP) + "";
    final String parkId = p.getProperty(TdcProperties.ARTILLERY_PARK_ID) + "";
    final Map divMap = Map.getMapById(divMapName);
    GamePiece[] pieces = divMap.getPieces();
    for (GamePiece piece : pieces) {
      if (piece instanceof Stack) {
        final Stack stack = (Stack) piece;
        for (GamePiece gp : stack.asList()) {
          if (TdcProperties.TGD_ARTY_PARK.equals(gp.getProperty(BasicPiece.CURRENT_ZONE))) {
            if (parkId.equals(gp.getProperty(BasicPiece.LOCATION_NAME))) {
              if ("true".equals(gp.getProperty(TdcProperties.IS_ARTILLERY))) {
                addTargetPiece(gp);
              }
            }
          }
        }
      }
    }
  }

  public void pack() {
    myDialog.pack();
  }

  // Our mode has changed, let other clients know.
  public void modeChanged(int mode, int oldMode) {
    final Command modeCmd = new WizardModeCommand(this, mode, oldMode);
    final Command reportCmd = getChatCommand(myModel.getSmlModeString(), false);
    reportCmd.execute();
    modeCmd.append(reportCmd);
    GameModule.getGameModule().sendAndLog(modeCmd);
  }

  // Our Dialog is closing, inform other clients
  public void wizardCloses() {
    getMyThread().setVisible(false);
    final Command closeCmd = new WizardCloseCommand(this);
    if (attackNotResolved) {
      attackNotResolved = false;
      final Command cancelCmd = getChatCommand("ATTACK CANCELLED", true);
      cancelCmd.execute();
      closeCmd.append(cancelCmd);
    }
    GameModule.getGameModule().sendAndLog(closeCmd);

  }

  // A Modifier has changed, tell other clients
  public void modifierChanged(FireModifier modifier) {
    final Command modCmd = new WizardModifierCommand(this, modifier.getId(), modifier.getValue(), modifier.getOldValue());
    // Don't report a change to the Base Defence Rating, this only changes when the target changes, which is already reported
    if (! AttackModel.BASE_DEFENCE_RATING.equals(modifier.getDescription())) {
      final Command reportCmd = getChatCommand(modifier.toString(), false);
      reportCmd.execute();
      modCmd.append(reportCmd);
    }
    GameModule.getGameModule().sendAndLog(modCmd);
  }

  // Another client has changed mode
  public void changeMode(int mode) {
    myView.changeMode(mode);
  }

  public int getMode() {
    return myModel.getMode();
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  // Close Dialog on game close
  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      hideDialog();
    }
  }

  public void hideDialog() {
    if (myDialog != null) {
      myDialog.setVisible(false);
      getMyThread().setVisible(false);
    }
  }

  public Command getRestoreCommand() {
    return null;
  }

  public void addTo(Buildable b) {
    idMgr.add(this);
    map = (TdcMap) b;
    getMyThread().addTo(map);
    map.setAttackWizard(this);
    map.addDrawComponent(this);
    map.getView().addMouseListener(this);
    GameModule.getGameModule().getGameState().addGameComponent(this);
    GameModule.getGameModule().addCommandEncoder(this);
    myResolver = new FireResolver(this);

    autoCloseConfig = new BooleanConfigurer(AUTO_CLOSE, "Auto-close Attack Wizard?", true);
    GameModule.getGameModule().getPrefs().addOption(PREF_TAB, autoCloseConfig);
  }

  public void removeFrom(Buildable b) {
    idMgr.remove(this);
    getMyThread().removeFrom(map);
    map.removeDrawComponent(this);
  }

  public String[] getAttributeDescriptions() {
    return new String[0];
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[0];
  }

  public String[] getAttributeNames() {
    return new String[0];
  }

  public void setAttribute(String key, Object value) {

  }

  public String getAttributeValueString(String key) {
    return null;
  }

//  protected class Rating {
//    protected String color;
//    protected int strength;
//
//    public Rating(String color, int strength) {
//      this.color = color;
//      this.strength = strength;
//    }
//
//    public String getColor() {
//      return color;
//    }
//
//    public int getStrength() {
//      return strength;
//    }
//  }

  public void mouseClicked(MouseEvent e) {

  }

  public void mousePressed(MouseEvent e) {

  }

  public void mouseReleased(MouseEvent e) {
    activeInfo.clear();
  }

  public void mouseEntered(MouseEvent e) {

  }

  public void mouseExited(MouseEvent e) {

  }

  class InfoImage {
    TdcWizard source;
    BufferedImage image;
    Rectangle bounds;

    public InfoImage(TdcWizard wiz) {
      source = wiz;
      image = wiz.getInfoImage();
      if (image != null) {
        final Point pos = wiz.getPosition();
        final Rectangle r = new Rectangle(pos.x, pos.y, image.getWidth(), image.getHeight());
        Rectangle viewRect = map.getView().getVisibleRect();

        bounds = map.mapToComponent(r);
        int maxX = bounds.x + image.getWidth();
        int maxV = viewRect.x + viewRect.width;
        if (maxX > maxV) {
          bounds.x -= (maxX - maxV);
          if (bounds.x < viewRect.x) {
            bounds.x = viewRect.x;
          }
        }
        int maxY = bounds.y + image.getHeight();
        maxV = viewRect.y + viewRect.height;
        if (maxY > maxV) {
          bounds.y -= (maxY - maxV);
          if (bounds.y < viewRect.y) {
            bounds.y = viewRect.y;
          }
        }
      }
    }

    public void draw(Graphics g, Map map) {
      if (image != null) {
        final double os_scale = Info.getSystemScaling();
        g.drawImage(image, (int) (bounds.x * os_scale), (int) (bounds.y * os_scale), map.getView());
      }
    }

    public void move(int x, int y) {
      bounds.x = x;
      bounds.y = y;
    }
  }

  public Command decode(String com) {
    if (com.startsWith(ATTACK_WIZARD_COMMAND + getId())) {
      final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(com, '\t');
      sd.nextToken();
      sd.nextToken();
      final String command = sd.nextToken("");
      if (WIZARD_OPEN.equals(command)) {
        final String pieceId = sd.nextToken("");
        final int x = sd.nextInt(0);
        final int y = sd.nextInt(0);
        final int range = sd.nextInt(1);
        return new WizardOpenCommand(this, pieceId, new Point(x, y), range);
      }
      else if (WIZARD_BEACH.equals(command)) {
        final String pieceId = sd.nextToken("");
        final int specialType = sd.nextInt(0);
        return new WizardBeachCommand(this, pieceId, specialType);
      }
      else if (WIZARD_CLOSE.equals(command)) {
        return new WizardCloseCommand(this);
      }
      else if (WIZARD_MODE.equals(command)) {
        final int mode = sd.nextInt(0);
        return new WizardModeCommand(this, mode);
      }
      else if (WIZARD_TARGET.equals(command)) {
        final int target = sd.nextInt(0);
        return new WizardTargetCommand(this, target);
      }
      else if (WIZARD_MODIFIER.equals(command)) {
        final String id = sd.nextToken("");
        final int value = sd.nextInt(0);
        return new WizardModifierCommand(this, id, value);
      }

    }
    return null;
  }

  public String encode(Command c) {
    if (c instanceof AttackWizardCommand) {
      final AttackWizardCommand com = (AttackWizardCommand) c;
      SequenceEncoder se = new SequenceEncoder(com.wizard.getId(), '\t');
      se.append(com.command);
      if (c instanceof WizardBeachCommand) {
        final WizardBeachCommand wbc = (WizardBeachCommand) c;
        se.append(wbc.sourceId).append(wbc.specialAttackType);
      }
      else if (c instanceof WizardOpenCommand) {
        final WizardOpenCommand woc = (WizardOpenCommand) c;
        se.append(woc.sourceId).append(woc.target.x).append(woc.target.y).append(woc.range);
      }
      else if (c instanceof WizardCloseCommand) {
        // No further parameters
      }
      else if (c instanceof WizardModeCommand || c instanceof WizardTargetCommand) {
        se.append(com.getNewValue());
      }
      else if (c instanceof WizardModifierCommand) {
        final WizardModifierCommand wmc = (WizardModifierCommand) c;
        se.append(wmc.getModifierId()).append(com.getNewValue());
      }
      return ATTACK_WIZARD_COMMAND + se.getValue();
    }
    return null;
  }

  public void setId(String id) {
    wizardID = id;
  }

  public String getId() {
    return wizardID;
  }

  public Command getChatCommand(String s, boolean useId) {
    final String id = "<" + source.getProperty(GlobalOptions.PLAYER_ID) + ">";
    return new Chatter.DisplayText(GameModule.getGameModule().getChatter(), (useId ? "* " + id + " - " : "  " + id + " ") + s);
  }

  /*
   * User has clicked one of the roll buttons in the FireResolver
   */
  public void doRoll(int type, int roll) {
    attackNotResolved = false;
    final int fireRating = myModel.getFireRating();
    Command extras = null;

    List<String> text = myModel.getReportText();

    if (type != FireResolver.DO_REPORT) {
      text.add(">" + roll + "< Rolled!" + ((type == FireResolver.DO_USE) ? " (Manually entered)" : ""));

      if (specialAttackType == BEACH_SPECIAL_ATTACK) {
        int adjustedRoll = roll + myModel.getAttackModifierTotal();
        if (adjustedRoll < 0)
          adjustedRoll = 0;
        if (adjustedRoll > 9)
          adjustedRoll = 9;
        if (roll != adjustedRoll) {
          text.add("Roll adjusted to " + adjustedRoll);
        }
        text.add("Result = " + FireResolver.resolveBeach(sourceInfo.getBeachAttackClass(), adjustedRoll));
      }
      else {
        if (roll > fireRating || roll == 9) {
          text.add("Miss, no effect");
          if (roll == 9) {
            if (myModel.isContactLossPossible()) {
              text.add("*** 9 rolled, CONTACT Lost ***");
            }
            if (myModel.canIndirectFireScatter()) {
              Integer randomDirection = getRandomDirection(targetPoint);
              String directionText = getRandomDirectionText(randomDirection);
              final Point scatterPoint = map.returnAdjacentHex(targetPoint, randomDirection);
              if (!map.terrainMap.isHexBarraged(scatterPoint)) {
                GamePiece newBarragePiece = myModel.getNewBarragePiece();
                final Command c = ((Map) map).placeOrMerge(newBarragePiece, scatterPoint);
                GameModule.getGameModule().sendAndLog(c);
                text.add("*** 9 rolled, Artillery Scatter. Direction: " + randomDirection.toString() + " -  " + directionText + " - Placed barrage ***");
              }
              else {
                text.add("*** 9 rolled, Artillery Scatter. Direction: " + randomDirection.toString() + " -  " + directionText + " - Barrage already present. ***");
              }
            }
          }
        }
        else {
          if (myModel.isAirDefence()) {
            text.add("*HIT*, Result = Fighter Bomber aborts mission!");
          }
          else {
            text.add("*HIT*, Result = " + FireResolver.resolve(myModel.getFireRatingColour(), (myModel.isTargetArmoured() || myModel.isTargetEntrenched()), roll));
          }
        }
        // Place barrage if not 9 and indirect, unless one is already there
        // Flip the existing or new barrage to heavy if CompanyBonus is on
        if (roll!=9 && myModel.isIndirectFire()) {
          boolean reportAddBarrage = false;
          if (!map.terrainMap.isHexBarraged(targetPoint)) {
            GamePiece newBarragePiece = myModel.getNewBarragePiece();
            final Command c = ((Map) map).placeOrMerge(newBarragePiece, targetPoint);
            GameModule.getGameModule().sendAndLog(c);
            reportAddBarrage = true;
          }
          boolean reportHeavyBarrage = false;
          if (myModel.isHeavyBarragePossible() && map.terrainMap.getHexBarrageLevel(targetPoint)<2) {
            reportHeavyBarrage = true;
            final Stack s = (Stack) map.findPiece(targetPoint, PieceFinder.STACK_ONLY);
            for (final GamePiece gamePiece : s.asList()) {
              if (gamePiece.getName().equals("Light Barrage")) {
                GamePiece outer = gamePiece;
                if (outer!=null) {
                  Embellishment barrage = null;
                  while ((barrage == null) && !(outer instanceof BasicPiece)) {
                    if (outer instanceof Embellishment) {
                      barrage = (Embellishment) outer;
                      if (!barrage.getDescription().contains("Barrage")) {
                        barrage = null;
                      }
                    }
                    outer = ((Decorator) outer).getInner();
                  }
                  if (barrage != null) {
                    ChangeTracker trackerBarrage = new ChangeTracker(barrage);
                    barrage.setValue(1);
                    GameModule.getGameModule().sendAndLog(trackerBarrage.getChangeCommand());
                  }
                }
              }
            }
          }
          if (reportAddBarrage==true) {
            if (reportHeavyBarrage==true) {
              text.add("*** Placed Heavy Barrage ***");
            } else  {
              text.add("*** Placed Light Barrage ***");
            }
          } else {
            if (reportHeavyBarrage==true) {
              text.add("*** Flipped existing Barrage to Heavy ***");
            }
          }
        }
        if (myModel.isObstaclesAttack() && roll == 0) {
          text.add("*** 0 rolled on Beach Obstacle attack, Drift marker applied ***");
          extras = myModel.getTarget().keyEvent(KeyStroke.getKeyStroke('D', InputEvent.ALT_DOWN_MASK));
        }
        if (myModel.isRubblePossible() && roll == 0) {
          text.add("*** 0 rolled. Add rubble marker. OP or Strongpoint destroyed. ***");
          // something similar could place a Rubble marker? extras = myModel.getTarget().keyEvent(KeyStroke.getKeyStroke('D', InputEvent.ALT_DOWN_MASK));
        }
      }
      if (roll == 0 && sourceInfo.isNaval() && myModel.isPhoneLinesCutPossible()) {
        // Phones can be only be cut on Overlays and starting with the 0900 turn of June 6th.
        // But skip message if the phones are cut on the overlay
        text.add(" *** 0 rolled by Naval Unit, Phone lines on target overlay are Cut ***");
      }
    }

    if (this.sourceInfo.inArtilleryPark==true) {
      // Fire originated from an Artillery Park
      // Locate the park piece and trigger "fired".
      // Get "Fired" layer (Embellishment)
      GamePiece outer = this.sourceInfo.getArtilleryParkPiece();
      ChangeTracker tracker = new ChangeTracker(outer);
      if (outer!=null) {
        Embellishment fired = null;
        while ((fired == null) && !(outer instanceof BasicPiece)) {
          if (outer instanceof Embellishment) {
            fired = (Embellishment) outer;
            if (!fired.getDescription().contains("Fired")) {
              fired = null;
            }
          }
          outer = ((Decorator) outer).getInner();
        }
        if (fired != null) {
          fired.setActive(true);
          GameModule.getGameModule().sendAndLog(tracker.getChangeCommand());
        }
      }
    }

    String attackType = "";
    if (!myModel.isSpecialAttack()) {
      attackType = myModel.getModeString() + " FIRE ATTACK";
    }
    else if (myModel.isSpecialAttack()) {
      attackType = (SPECIAL_ATTACK_NAMES[specialAttackType] + " ATTACK").toUpperCase();
    }

    final Command c = getChatCommand(attackType, true);

    for (String s : text) {
      c.append(getChatCommand(s, false));
    }

    c.append(getChatCommand("END ATTACK", true));

    if (isAutoClose()) {
      hideDialog();
      c.append(new WizardCloseCommand(this));
    }

    c.execute();

    // Add any commands that have already been executed
    if (extras != null) {
      c.append(extras);
    }

    // If the Source Unit is Activated, then send a Ctrl-K to mark it as done
    if ("2".equals(source.getProperty("Active_Level")) && "1".equals(source.getProperty("Mark_Level"))) {
      final Command mark = source.keyEvent(KeyStroke.getKeyStroke('K', InputEvent.CTRL_DOWN_MASK));
      c.append(mark);
    }

    GameModule.getGameModule().sendAndLog(c);
  }

  public int getRandomDirection( Point p ) {
    // Look for a dubug trigger
    final Stack s = (Stack) map.findPiece(p, PieceFinder.STACK_ONLY);
    for (final GamePiece gamePiece : s.asList()) {
      final String name = gamePiece.getName();
      switch (name) {
        case "Debug1":
          return 1;
        case "Debug2":
          return 2;
        case "Debug3":
          return 3;
        case "Debug4":
          return 4;
        case "Debug5":
          return 5;
        case "Debug6":
          return 6;
      }
    }
    return (int) (GameModule.getGameModule().getRNG().nextFloat() * 6 + 1);
  }

  public String getRandomDirectionText(int randomDirection) {
    String directionText = "";
    switch (randomDirection) {
      case 1:
        directionText = "North";
        break;
      case 2:
        directionText = "Northeast";
        break;
      case 3:
        directionText = "Southeast";
        break;
      case 4:
        directionText = "South";
        break;
      case 5:
        directionText = "Southwest";
        break;
      case 6:
        directionText = "Northwest";
        break;
    }
    return directionText;
  }

  // Player has clicked the Air Defence roll button
  public void doAirDefenceRoll() {
    final int defence = myModel.getAirDefenceRating();
    final int roll = (int) (GameModule.getGameModule().getRNG().nextFloat() * 10);
    final Command reportCmd = getChatCommand("AIR DEFENSE ATTACK", true);
    reportCmd.append(getChatCommand("Air Defense Rating: " + myModel.getAirDefenceRating() + " (" + myModel.getAirDefenceDescription() + ")", false));
    reportCmd.append(getChatCommand(">" + roll + "< Rolled!", false));
    if (roll > defence || roll == 9) {
      reportCmd.append(getChatCommand("Miss, no effect", false));
    }
    else {
      reportCmd.append(getChatCommand("*HIT*, Result = Fighter Bomber aborts mission!", false));
    }
    reportCmd.execute();
    GameModule.getGameModule().sendAndLog(reportCmd);
  }

  protected boolean isAutoClose() {
    return (Boolean) GameModule.getGameModule().getPrefs().getValue(AUTO_CLOSE);
  }

  public GamePiece findAssaultForceMarker(String id) {
    if (id == null) {
      return null;
    }
    for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
      if (piece instanceof Stack) {
        final Stack stack = (Stack) piece;
        for (int i = 0; i < stack.getPieceCount(); i++) {
          final GamePiece p = stack.getPieceAt(i);
          if (TdcProperties.TYPE_ARTILLERY_PARK.equals(p.getProperty(TdcProperties.TYPE))) {
            if (id.equals(p.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
              return p;
            }
          }
        }
      }
    }

    return null;
  }

  public static abstract class AttackWizardCommand extends Command {

    protected AttackWizard wizard;
    protected String command;
    protected int newValue;
    protected int oldValue;
    protected int specialAttackType;


    protected AttackWizardCommand(AttackWizard wizard, String command, int specialAttackType) {
      this.wizard = wizard;
      this.command = command;
      this.specialAttackType = specialAttackType;
    }

    protected AttackWizardCommand(AttackWizard wizard, String command) {
      this(wizard, command, AttackWizard.NO_SPECIAL_ATTACK);
    }

    public int getOldValue() {
      return oldValue;
    }

    public void setOldValue(int oldValue) {
      this.oldValue = oldValue;
    }

    public int getNewValue() {
      return newValue;
    }

    public void setNewValue(int newValue) {
      this.newValue = newValue;
    }

    public int getSpecialAttackType() {
      return specialAttackType;
    }

    protected abstract void executeCommand();

    protected abstract Command myUndoCommand();

  }

  // ===== OPEN ===== //
  public static class WizardOpenCommand extends AttackWizardCommand {

    protected String sourceId;
    protected Point target;
    protected int range;

    public WizardOpenCommand(AttackWizard wizard, String source, Point targLoc, int range) {
      super(wizard, WIZARD_OPEN);
      this.sourceId = source;
      this.range = range;
      target = new Point(targLoc);
    }



    protected void executeCommand() {
      boolean showBlocking = true;
      Point sourcePosition;
      final GamePiece source = GameModule.getGameModule().getGameState().getPieceForId(sourceId);
      wizard.sourceInfo = new UnitInfo(source, true);
      wizard.hideDialog();
      wizard.setSource(source);
      wizard.setTarget(target, range);
      //aze If log replay and source is an artillery in a park or naval or Maisy / La Pernelle, need to redefine the source point
      GamePiece altSource = wizard.getAlternateSource(source);
      if (altSource == null) {
        sourcePosition = source.getPosition();
      } else {
        sourcePosition = altSource.getPosition();
        // Artillery/Naval fire. Treat as indirect if range > 3
        if (range > 3)
        {
          showBlocking = false;
        }
      }
      wizard.getMyThread().setRange(wizard.sourceInfo.getEffectiveRange());
      wizard.getMyThread().setEndPoints(sourcePosition, target, showBlocking);
      wizard.getMyThread().setVisible(true);
      wizard.getMyThread().setActive(false);

      // Re-center map if center on opponents move enabled
      if (GlobalOptions.getInstance().centerOnOpponentsMove()) {
        // Bug 13459. Prevent crash if no targets (Yes, should not happen).
        if (wizard.getTargetPieces().size() > 0) {
          final GamePiece piece = wizard.getTargetPieces().get(0);
          if (piece != null && piece.getMap() != null) {
            piece.getMap().ensureVisible(piece.getMap().selectionBoundsOf(piece));
          }
        }
      }
    }

    protected Command myUndoCommand() {
      return new WizardCloseCommand(wizard);
    }

  }

  public static class WizardBeachCommand extends AttackWizardCommand {

    protected String sourceId;

    public WizardBeachCommand(AttackWizard wizard, String source, int specialAttackType) {
      super(wizard, WIZARD_BEACH, specialAttackType);
      this.sourceId = source;
    }

    protected void executeCommand() {
      final GamePiece source = GameModule.getGameModule().getGameState().getPieceForId(sourceId);
      wizard.hideDialog();
      wizard.setAttackNotResolved(true);

      wizard.setSpecialAttackType(specialAttackType);
      wizard.setAirDefence(false);
      wizard.clearTargetPieces();

      if (specialAttackType == BEACH_SPECIAL_ATTACK) {
        wizard.setSource(source);
        wizard.addTargetPiece(new BeachPiece(BEACH_SPECIAL_ATTACK));
      }
      else {
        final BasicPiece dummyUnit = new BeachPiece(specialAttackType);
        wizard.setSource (dummyUnit);
        final String overlay = (String) source.getProperty(TdcProperties.DEFENCE_OVERLAY);
        wizard.setSourceInfo(new AttackModel.BeachAttackUnitInfo(source, specialAttackType, overlay));
        wizard.addTargetPiece(source);
      }

      wizard.buildBeachDialog();
      wizard.setDialogVisibility(true);
      // Send focus back to Main Frame so as not to stop a fast log replay
      GameModule.getGameModule().getPlayerWindow().requestFocus();

    }

    protected Command myUndoCommand() {
      return new WizardCloseCommand(wizard);
    }

  }

  // ===== CLOSE ===== //
  public static class WizardCloseCommand extends AttackWizardCommand {

    public WizardCloseCommand(AttackWizard wizard) {
      super(wizard, WIZARD_CLOSE);
    }

    protected void executeCommand() {
      wizard.hideDialog();
    }

    protected Command myUndoCommand() {
      if (wizard == null || wizard.getSource() == null || wizard.getTargetPoint() == null) {
        return null;
      }
      return new WizardOpenCommand(wizard, wizard.getSource().getId(), wizard.getTargetPoint(), wizard.getRangeToTarget());
    }

  }

  // ===== MODIFIER ===== //
  public static class WizardModifierCommand extends AttackWizardCommand {

    protected String modifierId;

    public WizardModifierCommand(AttackWizard wizard, String modifierId, int newValue, int oldValue) {
      super(wizard, WIZARD_MODIFIER);
      setOldValue(oldValue);
      setNewValue(newValue);
      setModifierId(modifierId);
    }

    public WizardModifierCommand(AttackWizard wizard, String modifierId, int newValue) {
      this(wizard, modifierId, newValue, 0);
    }

    public void setModifierId(String modifierId) {
      this.modifierId = modifierId;
    }

    public String getModifierId() {
      return modifierId;
    }

    protected void executeCommand() {
      FireModifier.find(getModifierId()).changeValue(getNewValue());
    }

    protected Command myUndoCommand() {
      return new WizardModifierCommand(wizard, getModifierId(), getOldValue(), getNewValue());
    }
  }

  // ===== TARGET ===== //
  public static class WizardTargetCommand extends AttackWizardCommand {

    public WizardTargetCommand(AttackWizard wizard, int newTarget, int oldTarget) {
      super(wizard, WIZARD_TARGET);
      setOldValue(oldTarget);
      setNewValue(newTarget);
    }

    public WizardTargetCommand(AttackWizard wizard, int newTarget) {
      this(wizard, newTarget, 0);
    }

    protected void executeCommand() {
      wizard.changeTarget(getNewValue());
    }

    protected Command myUndoCommand() {
      return new WizardTargetCommand(wizard, getOldValue(), getNewValue());
    }
  }

  // ===== MODE ===== //
  public static class WizardModeCommand extends AttackWizardCommand {

    public WizardModeCommand(AttackWizard wizard, int newMode, int oldMode) {
      super(wizard, WIZARD_MODE);
      setOldValue(oldMode);
      setNewValue(newMode);
    }

    public WizardModeCommand(AttackWizard wizard, int newMode) {
      this(wizard, newMode, 0);
    }

    protected void executeCommand() {
      wizard.changeMode(getNewValue());
    }

    protected Command myUndoCommand() {
      return new WizardModeCommand(wizard, getOldValue(), getNewValue());
    }
  }

  public static class BeachPiece extends BasicPiece {
    public BeachPiece(int specialAttackType) {
      super(BasicPiece.ID + ";;" + SPECIAL_ATTACK_ICONS[specialAttackType] + ";" + SPECIAL_ATTACK_ICONS[specialAttackType] + ";");
    }
  }

}