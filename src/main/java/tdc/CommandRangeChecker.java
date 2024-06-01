/*
 * $Id: CommandRangeChecker.java 9453 2020-06-07 22:45:00Z swampwallaby $
 *
 * Copyright (c) 2005-2017 by Brent Easton
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

 /* 10-Jul-17 German Leaders do not need to be in a Town/City/Fortified to ghave telephone access */

package tdc;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.PieceVisitor;
import VASSAL.counters.PieceVisitorDispatcher;
import VASSAL.counters.Properties;
import VASSAL.counters.PropertiesPieceFilter;
import VASSAL.counters.Stack;
import terrain.TerrainBasicPiece;

/**
 * Checks command ranges and finds leaders.
 *
 * @author Brent Easton
 *
 */
public class CommandRangeChecker {

  protected GamePiece piece;
  protected boolean isUnitIndependent = false; // Unit is part of an Independent
                                               // Formation
  protected boolean isUnitNonFormationCommand = false; // Unit has non-formation
                                                       // command capability
  protected boolean NonFormationCommand = false; // Unit is currently in command
                                                 // to Non-formaion leader
  protected boolean FormationCommand = false; // Unit is currently in command to
                                              // its own formation leader
  protected boolean canBeActivated; // Game Piece can be activated (i.e. is a
                                    // combat unit)
  protected boolean alwaysInCommand;
  protected boolean targetHasTelephoneCapability;

  protected String leaderFormationType = "";
  protected String leaderFormationValue = "";
  protected String leaderFormation2Value = "";
  protected String unitType;

  protected String pieceFormation = "";
  protected String pieceDivision = "";
  protected boolean isGermanInCrete = false;
  protected boolean isGermanInTGD = false;

  protected Map map;
  protected UnitInfo info;

  public CommandRangeChecker() {
    this(null, null);
  }

  public CommandRangeChecker(GamePiece p, UnitInfo i) {
    info = i;
    piece = p;
    setLeaderAttributes();
    map = p.getMap();
  }

  /**
   * Determine the Markers to be matched to find the leader for this unit. Units
   * of Independent formations need any leader of their Division. Other units
   * need a leader of their particular Formation. Units that are Hidden or
   * Obscured to me must be skipped Commandos in D-Day must be within 2 hexes of
   * another Commando unit
   *
   * In Crete, Greek units must be in command range of any allied leader
   *
   */
  protected void setLeaderAttributes() {
    unitType = (String) piece.getProperty(TdcProperties.TYPE);
    if (!TdcRatings.isUnitType(unitType)) {
      return;
    }

    // Commando handled separately
    if (info.isCommando()) {
      canBeActivated = true;
      return;
    }

    // Greek unit in Crete
    if (info.isGreek() && UnitInfo.isCreteRules()) {
      canBeActivated = true;
      alwaysInCommand = false;
      leaderFormationType = TdcProperties.ARMY;
      leaderFormationValue = TdcProperties.ARMY_ALLIED;
    }
    else {
      canBeActivated = Boolean.parseBoolean((String) piece.getProperty(TdcProperties.ACTIVE)) && !Boolean.TRUE.equals(piece.getProperty(Properties.INVISIBLE_TO_ME))
          && !Boolean.TRUE.equals(piece.getProperty(Properties.OBSCURED_TO_ME)) && !"Leader".equals(piece.getProperty("Type"));
      alwaysInCommand = isAlwaysInCommand();
      if (canBeActivated && !alwaysInCommand) {
        targetHasTelephoneCapability = hasTelephoneCapability(piece);

        String unitClass = (String) piece.getProperty(TdcProperties.CLASS);
        if (unitClass != null) {
          if (unitClass.equals(TdcProperties.INFANTRY) || unitClass.equals(TdcProperties.VEHICLE) || unitClass.equals(TdcProperties.GUN) || unitClass.equals(TdcProperties.CLASS_LANDING_CRAFT)) {
            leaderFormationType = TdcProperties.FORMATION;
            pieceFormation = piece.getProperty(TdcProperties.FORMATION) + "";
            pieceDivision = piece.getProperty(TdcProperties.DIVISION) + "";
            isGermanInCrete = UnitInfo.isCreteRules() && TdcProperties.ARMY_GERMAN.equals(piece.getProperty(TdcProperties.ARMY));
            isGermanInTGD = UnitInfo.isTgdRules() && TdcProperties.ARMY_GERMAN.equals(piece.getProperty(TdcProperties.ARMY));
            leaderFormationValue = pieceFormation;
            Object f = piece.getProperty(TdcProperties.FORMATION2);
            leaderFormation2Value = (f == null) ? "" : (String) f;
            // leaderFormation2Value = (String) piece.getProperty(TdcProperties.FORMATION2) + "";
            isUnitIndependent = Boolean.parseBoolean((String) piece.getProperty(TdcProperties.IS_INDEPENDENT));
            isUnitNonFormationCommand = Boolean.parseBoolean((String) piece.getProperty(TdcProperties.NON_FORMATION_COMMAND)) && !Boolean.parseBoolean((String) piece.getProperty(TdcProperties.COMMAND_BREAKDOWN));
            if (leaderFormationValue != null) {
              if (isUnitIndependent || isUnitNonFormationCommand) {
                leaderFormationType = TdcProperties.DIVISION;
                leaderFormationValue = piece.getProperty(leaderFormationType) + "";
                leaderFormation2Value = (f == null) ? "" : (String) f;
                //leaderFormation2Value = piece.getProperty(TdcProperties.DIVISION2) + "";
              }
            }
          }
        }
      }
    }
  }

  /**
   * A unit is Always in Command if a) It has the property AlwaysInCommand set
   * to true or b) It's Formation, Division or Army is XXXXX and the Global
   * Property AIC-XXXXX is true
   *
   * @return Always in Command or c) It's in an Artillery Park on the Bir
   *         Hacheim board.
   *
   *         TGD New Rules :-
   *         Artillery in Artillery Parks always in command
   *         Red TQR units (except Commandos) always in command
   */
  protected boolean isAlwaysInCommand() {

    if (!TdcRatings.isUnitType(unitType)) {
      return true;
    }

    if (Boolean.parseBoolean((String) piece.getProperty(TdcProperties.ALWAYS_IN_COMMAND))) {
      return true;
    }

    if (info.isRedTqr()) {
      return true;
    }

    String gpName = "AIC-" + piece.getProperty(TdcProperties.FORMATION);
    MutableProperty mp = GameModule.getGameModule().getMutableProperty(gpName);
    if (mp != null) {
      if ("true".equals(mp.getPropertyValue())) {
        return true;
      }
    }

    gpName = "AIC-" + piece.getProperty(TdcProperties.DIVISION);
    mp = GameModule.getGameModule().getMutableProperty(gpName);
    if (mp != null) {
      if ("true".equals(mp.getPropertyValue())) {
        return true;
      }
    }

    gpName = "AIC-" + piece.getProperty(TdcProperties.ARMY);
    mp = GameModule.getGameModule().getMutableProperty(gpName);
    if (mp != null) {
      if ("true".equals(mp.getPropertyValue())) {
        return true;
      }
    }

    // NQOS Artillery units in Artillery Parks
    if (TdcProperties.BIR_HACHEIM.equals(piece.getProperty("CurrentBoard"))) {
      final String location = (String) piece.getProperty("LocationName");
      if (location != null && location.startsWith(TdcProperties.ARTILLERY_PARK)) {
        return true;
      }
    }

    // GTS 2 Artillery units in Artillery Parks
    if (UnitInfo.isGTS2Rules()) {
      if (TdcProperties.TGD_ARTY_PARK.equals(piece.getProperty(BasicPiece.CURRENT_ZONE))) {
        return true;
      }
    }

    // Tinian Artillery units in Artillery Parks
    if (UnitInfo.isTinianRules()) {
      final String zone = (String) piece.getProperty(BasicPiece.CURRENT_ZONE);
      if ("Saipan".equals(zone) || TdcProperties.TGD_ARTY_PARK.equals(zone)) {
        return true;
      }
    }

    // Units of the 736th Regiment are Always in command if the 736TelCmd option
    // is true and
    // Phone lines have not been cut
    if ("true".equals(piece.getProperty("736TelCom"))) {
      if (TdcProperties.FORMATION_716_736.equals(piece.getProperty(TdcProperties.FORMATION))) {
        return hasTelephoneCapability(piece);
      }
    }

    return false;
  }

  protected boolean hasTelephoneCapability(GamePiece piece) {

    final String division = piece.getProperty(TdcProperties.DIVISION) + "";
    final boolean isLeader = TdcProperties.LEADER.equals(piece.getProperty(TdcProperties.TYPE));
    boolean hasTelephone = "true".equals(piece.getProperty(TdcProperties.HAS_TELEPHONE));


    // Only American units have telephone capability in WED and earlier
    // In WED only American Leaders have Telephone property, all units are
    // assumed to have it
    if (TdcProperties.DIVISION_82AB.equals(division) || TdcProperties.DIVISION_101AB.equals(division)) {
      // Leader must have the telephone symbol

      if (isLeader && !hasTelephone) {
        return false;
      }

      // Both Leader and Units must be in an OP, strongpoint or City, Town or
      // Fortified terrain
      final String terrain = (String) TdcMap.getTerrainProperty(piece, TerrainBasicPiece.CURRENT_TERRAIN);
      final String strongpoint = (String) TdcMap.getTerrainProperty(piece, TdcProperties.STRONGPOINT);
      final String op = (String) TdcMap.getTerrainProperty(piece, TdcProperties.OP);
      return "true".equals(op) || "true".equals(strongpoint) || TdcProperties.CITY.equals(terrain) || TdcProperties.TOWN.equals(terrain) || TdcProperties.FORTIFIED.equals(terrain);
    }
    // In GJS, Units of some Divisions can have telephone capability
    // Note, German Leaders do not need to be in any special terrain to have Telephone capability
    else if (TdcProperties.DIVISION_716.equals(division) || TdcProperties.DIVISION_352.equals(division)
      || TdcProperties.DIVISION_346.equals(division)) {
      if (isLeader) {
        return true;
      }

      // Unit must have Telephone capability OR be stacked with a unit that has Telephone capability.
      // + Phone must not be cut
      // + Must be before 8june 0700
      if (!hasTelephone) {
        // Scan stack to see if another unit has a phone
        Stack s = piece.getParent();
        if (s==null) {
          return false;
        }
        for (Iterator<GamePiece> i = s.getPiecesIterator(); i.hasNext();) {
          GamePiece p = i.next();
          if ("true".equals(p.getProperty(TdcProperties.HAS_TELEPHONE))) {
            // must be of same division
            if (piece.getProperty(TdcProperties.DIVISION).equals(p.getProperty(TdcProperties.DIVISION))) {
              //recursively check telephoneCapability on this piece (lazy check for cut and date/time)
              hasTelephone =  hasTelephoneCapability(p);
            }
          }
        }
      }
      if  (!hasTelephone) {
        return false;
      }

      // Check if Telephone has been cut or 8th june or later
      if (isTelephoneCut(piece)) {
        return false;
      }

      // Non-leader with telephone not cut has telephone regardless of terrain
        return true;

    }
    // In Utah:
    // Only units of the 709 Division with a Telephone symbol have Telephone Capability, as long as lines are not cut.
    // and Leader Keil is stacked with a telephone unit with uncut lines.
    // Units belonging to KG Keil also have Telephone if stacked with another unit with Telephone Capability (assuming Keil has telephone capability).
    // In the 2 Beach scenarios, 709 telephone units do not need Keil to be on the board.
    else if (TdcProperties.DIVISION_709.equals(division)) {

      boolean keilHasPhone = false;

      // If Keil isn't stacked with an uncut phone unit, no-one has phone capability

      // Special case, If its the 2 beach scenarios and Keil is not the map, then he is assumed to have phone capability
      final String board = (String) piece.getProperty(BasicPiece.CURRENT_BOARD);
      if (TdcProperties.BOARD_UTAH_BEACH1.equals(board) || TdcProperties.BOARD_UTAH_BEACH2.equals(board)) {
        keilHasPhone = !isKeilOnMap(piece.getMap());
      }

      // Keil is on map, so checked he is stacked with a unit with phone capability
      if (!keilHasPhone) {
        final String keilPhoneCount = (String) piece.getProperty(TdcProperties.KEIL_TELEPHONE_COUNT);
        keilHasPhone = (keilPhoneCount != null & !"0".equals(keilPhoneCount));
      }

      // If Keil has no phone capability, then effectively, no 709 units have phone capability
      if (!keilHasPhone) {
        return false;
      }

      // Units that actually have a Telephone, just report current state of lines.
      if (hasTelephone) {
        return !isTelephoneCut(piece);
      }

      // All other 709 pieces other than KG Keil have no telephone capability
      final String formation = piece.getProperty(TdcProperties.BASE_FORMATION) + "";
      if (!TdcProperties.FORMATION_KEIL.equals(formation)) {
        return false;
      }
      
      // KG Keil pieces have telephone capability if stacked with another unit that has active telephone capability
      final Stack stack = piece.getParent();
      for (Iterator<GamePiece> i = stack.getPiecesIterator(); i.hasNext();) {
        final GamePiece p = i.next();
        if (!TdcProperties.LEADER.equals(p.getProperty(TdcProperties.TYPE))) {
          if (division.equals(p.getProperty(TdcProperties.DIVISION))) {
            if ("true".equals(p.getProperty(TdcProperties.HAS_TELEPHONE))) {
              if (!"2".equals(p.getProperty(TdcProperties.LAYER_TELEPHONE + "_Level"))) {
                return true;
              }
            }
          }
        }
      }
      return false;
    }

    return false;
  }

  public boolean isKeilOnMap(Map map) {
    for (final GamePiece piece : map.getPieces()) {
      if (piece instanceof Stack) {
        for (final GamePiece p : ((Stack) piece).asList()) {
          if (TdcProperties.LEADER.equals(p.getProperty(TdcProperties.TYPE)) && TdcProperties.FORMATION_KEIL.equals(p.getProperty(TdcProperties.FORMATION))) {
            return true;
          }
        }
      }
    }
    return false;
  }
  public boolean isTelephoneCut(GamePiece piece) {
    // Return true if Phone cut set in piece or 8th June or later Check if the specific piece has the phone cut or
    // if it is 8jun 0700 or later
    if ("2".equals(piece.getProperty(TdcProperties.LAYER_TELEPHONE + "_Level"))) {
      return true;
    }
    final String date = (String) GameModule.getGameModule().getProperty(TdcProperties.DATE);
    if (("June 5th".equals(date)) || ("June 6th".equals(date)) || ("June 7th".equals(date))) {
      return false;
    }
    return true;
  }

  public boolean inCommand() {

    /*
     * Units that cannot be Activated, or are always in command are never marked
     * as out of command.
     */
    if (!canBeActivated || alwaysInCommand) {
      return true;
    }

    // Units with phone capability or stacked with such units are active if phones are not cut
    if (hasTelephoneCapability(piece)) {
      return true;
    }

    if (piece.getMap() != null && info.isCommando()) {
      final CommandoVisitor visitor = new CommandoVisitor(piece);
      final PieceVisitorDispatcher dispatcher = new PieceVisitorDispatcher(visitor);
      final GamePiece[] p = piece.getMap().getPieces();
      for (GamePiece gamePiece : p) {
        dispatcher.accept(gamePiece);
      }
      return visitor.isInCommand();
    }
    else {
      if (leaderFormationValue != null && leaderFormationValue.length() > 0 && piece.getMap() != null) {

        String s = TdcProperties.CLASS + "=" + TdcProperties.LEADER + " && " + leaderFormationType;
        if (leaderFormation2Value.length() > 0) {
          s += " =~ " + leaderFormationValue + "|" + leaderFormation2Value;
        }
        else {
          s += " = " + leaderFormationValue;
        }

        // German Units in Crete module can also be in command to a KG Leader from the same divsion
        if (isGermanInCrete) {
          s += " || " + TdcProperties.CLASS + "=" + TdcProperties.LEADER + " && Division = " + pieceDivision;
        }

        // German ind units not in formations ( mostly corps flaks) are in command if in range of any german leader
        //    Formation = ind-ind
        if ( isGermanInTGD && (pieceFormation.equals("ind-ind"))) {
          s = TdcProperties.CLASS + "=" + TdcProperties.LEADER + "&& Army = German";
        }
        final PieceFilter filter = PropertiesPieceFilter.parse(s);
        final LeaderVisitor visitor = new LeaderVisitor(filter, piece);
        final PieceVisitorDispatcher dispatcher = new PieceVisitorDispatcher(visitor);
        final GamePiece[] p = piece.getMap().getPieces();
        for (GamePiece gamePiece : p) {
          dispatcher.accept(gamePiece);
        }
        return visitor.isInCommand();
      }
    }

    return false;
  }

  public boolean isNonFormationCommand() {
    return NonFormationCommand && !FormationCommand;
  }

  protected class LeaderVisitor implements PieceVisitor {
    protected GamePiece sourcePiece;
    protected PieceFilter filter;
    protected boolean inCommand = false;
    protected boolean formationCommand = false;
    protected ArrayList<GamePiece> matches = new ArrayList<>();

    public LeaderVisitor(PieceFilter filter, GamePiece source) {
      this.filter = filter;
      sourcePiece = source;
    }

    /* Is Unit In command? */
    public boolean isInCommand() {
      return inCommand;
    }

    /* Is Unit In command by leader of same formation */
    public boolean isFormationCommand() {
      return formationCommand;
    }

    public ArrayList<GamePiece> getMatches() {
      return matches;
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
      if (p.getMap() != null && (filter == null || filter.accept(p))) {

        /*
         * Leaders in a Landing Wave box do not have a command range
         */
        final String zone = (String) p.getProperty(BasicPiece.CURRENT_ZONE);
        if (zone != null) {
          if (zone.startsWith(TdcProperties.ZONE_LANDING_WAVE)) {
            inCommand = false;
            return;
          }
        }

        /*
         * Regardless of command range, American units in certain hexes are in
         * command if their leader is also in a telephone hex
         */
        if (targetHasTelephoneCapability && hasTelephoneCapability(p)) {
          inCommand = true;
          matches.add(p);
        }
        /*
         * Otherwise check range
         */
        else {
          int commandRange = 0;
          try {
            commandRange = Integer.parseInt((String) p.getProperty(TdcProperties.RANGE));
          }
          catch (Exception ignored) {

          }

          if (commandRange > 0) {
            Point pos = p.getPosition();
            Board b = p.getMap().findBoard(pos);
            if (b != null) {
              MapGrid m = b.getGrid();
              if (m != null) {
                int range = m.range(pos, sourcePiece.getPosition());
                if (range <= commandRange) {
                  inCommand = true;
                  matches.add(p);

                  if (isUnitNonFormationCommand && !isUnitIndependent) {
                    String leaderFormation = (String) p.getProperty(TdcProperties.FORMATION);
                    if (leaderFormation.equals(pieceFormation)) {
                      FormationCommand = true;
                    }
                    else {
                      NonFormationCommand = true;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  protected static class CommandoVisitor implements PieceVisitor {

    GamePiece piece;
    String formation;
    boolean inCommand;

    public CommandoVisitor(GamePiece piece) {
      this.piece = piece;
      formation = (String) piece.getProperty(TdcProperties.FORMATION);
      inCommand = false;
    }

    /* Is Unit In command? */
    public boolean isInCommand() {
      return inCommand;
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
      // On the map and not the same piece
      if (p.getMap() != null && p != piece) {
        // It's another commando
        if (TdcProperties.DIVISION_CDO.equals(p.getProperty(TdcProperties.DIVISION))) {
          // From the same formation
          if (formation.equals(p.getProperty(TdcProperties.FORMATION))) {
            final Point pos = p.getPosition();
            final Board b = p.getMap().findBoard(pos);
            if (b != null) {
              final MapGrid m = b.getGrid();
              if (m != null) {
                final int range = m.range(pos, piece.getPosition());
                if (range <= 2) {
                  inCommand = true;
                }
              }
            }
          }
        }
      }
    }
  }
}
