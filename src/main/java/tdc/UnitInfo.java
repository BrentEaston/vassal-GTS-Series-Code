/*
 *
 * Copyright (c) 2012-2020 by Brent Easton
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
 *  5-Mar-20 BE Aded AMD Ruleset
 */

package tdc;

import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import net.miginfocom.swing.MigLayout;
import tdc.attack.FireModifier;
import terrain.TerrainBasicPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UnitInfo {

  protected static final String TRUE = "true";
  protected static final String CLOSED = "closed";

  protected static HashMap<String, TerrainInfo> terrainInfo, terrainInfoArmoured;
  protected static HashMap<String, TerrainInfo> terrainInfoRubble; // Use TerrainInfo to mark as -1 hexs subject to Rubble
  protected static String ruleset;

  protected GamePiece unit;
  protected HashMap<String, String> ratings;
  protected HashMap<String, String> baseRatings;
  protected boolean attacking;

  protected int range;
  protected int minRange = 99;
  protected String army;
  protected String division;
  protected String formation;
  protected String unitName;
  protected String terrain;
  protected String unitType;
  protected String hex;
  protected String weather;
  protected String date;
  protected String time;
  protected String unitClass;
  protected String unitSubClass;
  protected String organic;
  protected boolean raised;
  protected boolean embanked;
  protected boolean sunken;
  protected boolean inCommand;
  protected boolean nonFormationCommand;
  protected boolean rubbled;
  protected boolean activated;

  // Statuses
  protected boolean weatherAdjusted;
  protected boolean ip;
  protected boolean entrench;
  protected boolean suppressed;
  protected boolean stepLoss;
  protected boolean column;
  protected boolean heroic;

  protected int barrage;
  protected int cohit;
  protected int mass;
  protected int steps;
  protected boolean delayed;

  // Details
  protected ArrayList<String> fireDetails = new ArrayList<>();
  protected ArrayList<String> assaultDetails = new ArrayList<>();
  protected ArrayList<String> defenceDetails = new ArrayList<>();
  protected ArrayList<String> rangeDetails = new ArrayList<>();
  protected ArrayList<String> tqrDetails = new ArrayList<>();
  protected ArrayList<String> moveDetails = new ArrayList<>();

  // Adjustments due to Special Rules for TdcHighligher
  protected boolean tqrAdjusted;
  protected int tqrAdjustment;
  protected boolean defenceAdjusted;
  protected int defenceAdjustment;
  protected boolean fireAdjusted;
  protected int fireAdjustment;
  protected boolean assaultAdjusted;
  protected int assaultAdjustment;
  protected boolean moveAdjusted;
  protected String adjustedMove;

  protected InfoDialog infoDialog;

  protected ArrayList<FireModifier> attackModifiers;
  protected ArrayList<FireModifier> defenceModifiers;

  protected String beachAttackClass;
  protected boolean isAirPower;
  protected boolean isPartisanAttack;
  protected boolean ignoreNegativeCombatModifiers;
  protected boolean ignoreAllCombatModifiers;
  protected boolean hasAirDefence;

  protected int maxLOS = 99;
  protected String maxLOSReason = "";
  protected boolean hill = false;
  protected boolean op = false;

  protected boolean assault = false;
  protected String assaultReason = "May Assault";
  protected int stackMass = 0;
  protected boolean inArtilleryPark = false;

  public static String getRuleSet() {
    ruleset = (String) GameModule.getGameModule().getProperty(TdcProperties.RULESET);
    if (ruleset == null)
      ruleset = TdcProperties.RULES_WED;
    return ruleset;
  }

  public static boolean isNqosRules() {
    return TdcProperties.RULES_NQOS.equals(getRuleSet());
  }

  public static boolean isGTS2Rules() {
    return isTgdRules() || isTinianRules() || isCreteRules() || isAmdRules();
  }

  public static boolean isTgdRules() {
    return TdcProperties.RULES_TGD.equals(getRuleSet()) || TdcProperties.RULES_DDAY.equals(getRuleSet());
  }

  public static boolean isTdcRules() {
    return TdcProperties.RULES_TDC.equals(getRuleSet()) || TdcProperties.RULES_WED.equals(getRuleSet());
  }

  public static boolean isTinianRules() {
    return TdcProperties.RULES_TINIAN.equals(getRuleSet());
  }

  public static boolean isCreteRules() {
    return TdcProperties.RULES_CRETE.equals(getRuleSet());
  }

  public static boolean isAmdRules() {
    return TdcProperties.RULES_AMD.equals(getRuleSet());
  }

  public static int getArtilleryDirectRange() {
    return isNqosRules() ? 5 : 4;
  }

  public static int calculateNavalAddRange(String boxname) {
    int r = 0;

    if (TdcProperties.NAVAL_LONG_RANGE.equals(boxname)) {
      r = 30;
    }
    else if (TdcProperties.NAVAL_MEDIUM_RANGE.equals(boxname)) {
      r = 20;
    }
    else if (TdcProperties.NAVAL_SHORT_RANGE.equals(boxname) || TdcProperties.NAVAL_SAIPAN_RANGE.equals(boxname) || TdcProperties.NAVAL_PERNELLE_RANGE.equals(boxname)) {
      r = 10;
    }
    else if (TdcProperties.NAVAL_MAISY_RANGE.equals(boxname)) {
      r = 1;
    }

    return r;
  }

  @SuppressWarnings("unchecked")
  public UnitInfo(GamePiece unit, boolean attacking) {
    this.attacking = attacking;
    this.unit = unit;

    if (terrainInfo == null) {
      setupTerrainInfo();
    }

    if (attacking) {
      attackModifiers = new ArrayList<>();
      defenceModifiers = new ArrayList<>();
    }

    baseRatings = (HashMap<String, String>) unit.getProperty(TdcProperties.RATINGS);
    if (baseRatings == null) {
      baseRatings = new HashMap<>();
      ratings = new HashMap<>();
      inCommand = true;
      return; // No Ratings, no adjustments
    }
    else {
      ratings = new HashMap<>(baseRatings);
    }

    findBasicInfo();
    checkColumn();
    checkStack();
    checkDateTimeWeather();
    checkStepLoss();
    checkInCommand();
    checkSpecialRules();
    checkTerrain();
    checkArtilleryPark();
    if (isAttacking()) {
      checkAdjacentCounters();
      activated = "2".equals(unit.getProperty("Active_Level"));
    }
  }

  public boolean isAttacking() {
    return attacking;
  }

  public boolean isActivated() {
    return activated;
  }

  public String getFireDetails() {
    return getDetails(fireDetails);
  }

  public String getMoveDetails() {
    return getDetails(moveDetails);
  }

  public String getFireColor() {
    return ratings.get(TdcRatings.FIRE_COLOUR);
  }

  public String getAssaultColor() {
    return ratings.get(TdcRatings.ASSAULT_COLOUR);
  }

  public String getWeaponClass( String fireColor) {
    return TdcRatings.getWeaponClass(fireColor);
  }

  public boolean isFireWoodsReduced() {
    return TdcRatings.GREEN.equals(getFireColor()) || TdcRatings.ORANGE.equals(getFireColor()) || TdcRatings.YELLOW.equals(getFireColor()) || TdcRatings.BROWN.equals(getFireColor()) || TdcRatings.BLACK.equals(getFireColor());
  }

  public boolean isFireMarshReduced() {
    return false; // Rule removed
    // return TdcRatings.GREEN.equals(getFireColor()) ||
    // TdcRatings.ORANGE.equals(getFireColor())
    // || TdcRatings.PURPLE.equals(getFireColor()) ||
    // TdcRatings.BROWN.equals(getFireColor())
    // || TdcRatings.BLACK.equals(getFireColor());
  }

  public boolean isFireBocageReduced() {
    return false; // Rule removed
    // return TdcRatings.GREEN.equals(getFireColor()) ||
    // TdcRatings.ORANGE.equals(getFireColor())
    // || TdcRatings.PURPLE.equals(getFireColor()) ||
    // TdcRatings.BROWN.equals(getFireColor())
    // || TdcRatings.BLACK.equals(getFireColor());
  }

  // Is this unit currently a vehicle? Vehicle class or has activated organic
  // Transport
  public boolean isVehicle() {
    if (TdcProperties.VEHICLE.equals(unitClass)) {
      return true;
    }
    else {
      if (isOrganic()) {
        return "true".equals(unit.getProperty("Trn_Active"));
      }
      else {
        return false;
      }
    }
  }

  public boolean isInfantry() {
    return (!TdcProperties.GUN.equals(unitClass) && !isVehicle());
  }

  public boolean isOrganic() {
    return "true".equals(organic);
  }

  // Can this unit perform an Assault?
  // - Have Non-* MA
  // - Have non-blank Assault rating
  // - Not be in Entrenchment or IP
  // - Not be suppressed
  // - Have at least one step
  // - Not be under a Heavy barrage
  // - Not have a Green, Orange, Black or Brown fire rating
  public boolean canAssault() {
    checkAssault();
    return assault;
  }

  public String getAssaultReason() {
    checkAssault();
    return assaultReason;
  }

  protected void checkAssault() {

    assault = false;

    if (isDelayed()) {
      assaultReason = "No, Delay marker";
      return;
    }

    if ("*".equals(ratings.get(TdcRatings.MOVE_RATING))) {
      assaultReason = "No, * Movement rating";
      return;
    }

    if ("No".equals(ratings.get(TdcRatings.MOVE_RATING))) {
      assaultReason = "No, no Movement rating";
      return;
    }

    if (steps < 1) {
      assaultReason = "No, Zero Step unit";
      return;
    }

    if ("No".equals(ratings.get(TdcRatings.ASSAULT_RATING))) {
      assaultReason = "No, no Assault rating";
      return;
    }

    if (isEntrenched() || isInIP()) {
      assaultReason = "No, Entrenched or in IP";
      return;
    }

    if (isSuppressed()) {
      assaultReason = "No, Suppressed";
      return;
    }

    if (barrage == 2) {
      assaultReason = "No, under Heavy Barrage";
      return;
    }

    if (isArtillery() || isMortar()) {
      assaultReason = "No, Artillery or Mortar unit";
      return;
    }

    assault = true;
    assaultReason = "Yes";
  }

  public String getArmourString() {
    if (entrench) {
      return "Entrenched";
    }
    return ("true".equals(baseRatings.get(TdcRatings.ARMOURED))) ? "Armoured" : "Unarmoured";
  }

  public String getAssaultDetails() {
    return getDetails(assaultDetails);
  }

  public String getDefenceDetails() {
    return getDetails(defenceDetails);
  }

  public String getTqrDetails() {
    return getDetails(tqrDetails);
  }

  public String getRangeDetails() {
    return getDetails(rangeDetails);
  }

  public String getArmy() {
    return army == null ? "" : army;
  }

  public String getType() {
    return unitType == null ? "" : unitType;
  }

  public int getMass() {
    return mass;
  }

  public int getCoHits() {
    return cohit;
  }

  public int getSteps() {
    return steps;
  }

  public boolean isArmoured() {
    return TRUE.equals(baseRatings.get(TdcRatings.ARMOURED));
  }

  public boolean isEntrenched() {
    return entrench;
  }

  public boolean isInIP() {
    return ip;
  }

  public boolean isDelayed() {
    return delayed;
  }

  public boolean isSuppressed() {
    return suppressed;
  }

  public boolean isIndirectFireRating() {
    String fireColor = getFireColor();
    return TdcRatings.ORANGE.equals(fireColor) || TdcRatings.BLACK.equals(fireColor) || TdcRatings.BROWN.equals(fireColor) || TdcRatings.GREEN.equals(fireColor);
  }
  public boolean isArtillery() {
    String fireColor = getFireColor();
    return TdcRatings.ORANGE.equals(fireColor) || TdcRatings.BLACK.equals(fireColor) || TdcRatings.BROWN.equals(fireColor);
  }

  public boolean isEmplacedArtillery() {
    return TdcRatings.BLACK.equals(getFireColor());
  }

  public boolean isArtilleryPark() {
    return TdcProperties.TYPE_ARTILLERY_PARK.equals(unitType);
  }

  public boolean isBeachDefence() {
    return TdcProperties.TYPE_BEACH_DEFENCE.equals(unitType);
  }

  public boolean isAirPower() {
    return isAirPower;
  }

  public boolean isPartisanAttack() {
    return isPartisanAttack;
  }

  public boolean hasAirDefence() {
    return hasAirDefence;
  }

  public boolean isIgnoreNegativeCombatModifiers() {
    return ignoreNegativeCombatModifiers;
  }

  public boolean isIgnoreAllCombatModifiers() {
    return ignoreAllCombatModifiers;
  }

  public boolean isNoHighlighting() {
    return isAirPower();
  }

  public boolean isNaval() {
    return TdcProperties.DIVISION_NAVAL.equals(division) && !TdcProperties.CLASS_LANDING_CRAFT.equals(unitClass);
  }

  public boolean isIndirectWithForceMarker() {
    String zone = (String) unit.getProperty(BasicPiece.CURRENT_ZONE);
    String navalPark = (String) unit.getProperty(TdcProperties.NAVAL_PARK);
    zone = zone == null ? "" : zone;
    navalPark = navalPark == null ? "" : navalPark;
    return isNaval() || (isTinianRules() && isArtillery() && "Saipan".equals(zone)) || (isArtillery() && (TdcProperties.NAVAL_PERNELLE_RANGE.equals(navalPark) || TdcProperties.NAVAL_MAISY_RANGE.equals(navalPark)));
  }

  public int getNavalRange() {
    int r = 0;
    if (isIndirectWithForceMarker()) {
      r = UnitInfo.calculateNavalAddRange((String) unit.getProperty(TdcProperties.NAVAL_BOX));
    }
    return r;
  }

  public boolean isCoastal() {
    return TdcRatings.BLACK.equals(getFireColor());
  }

  public boolean isMortar() {
    return TdcRatings.GREEN.equals(getFireColor());
  }

  public boolean isNQOSArtinAP() {
    final String loc = (String) unit.getProperty(BasicPiece.LOCATION_NAME);
    return isNqosRules() && isArtillery() && loc != null && loc.startsWith("Artillery Park");
  }

  public boolean isWoodsTerrain() {
    return TdcProperties.TERRAIN_WOODS.equals(terrain) || TdcProperties.TERRAIN_BRUSHWOODS.equals(terrain);
  }

  public boolean isMarshTerrain() {
    return TdcProperties.TERRAIN_MARSH.equals(terrain);
  }

  public boolean isBocageTerrain() {
    return TdcProperties.TERRAIN_BOCAGE.equals(terrain);
  }

  public boolean isCommando() {
    return TdcProperties.DIVISION_CDO.equals(division);
  }

  public boolean isEngineer() {
    return TdcProperties.ENGINEER.equals(unitSubClass);
  }

  public boolean isGreek() {
    return TdcProperties.SUBCLASS_GREEK.equals(unitSubClass);
  }

  public boolean isTank() {
    return isVehicle() && (TdcRatings.WHITE.equals(getFireColor()) || TdcRatings.BROWN.equals(getFireColor())) && "Red".equals(ratings.get(TdcRatings.MOVE_COLOUR));
  }

  public boolean isLandingCraft() {
    return TdcProperties.CLASS_LANDING_CRAFT.equals(unitClass);
  }

  public boolean isLCS() {
    return TdcProperties.CLASS_LCS.equals(unitClass);
  }

  public boolean isRedTqr() {
    return "true".equals(ratings.get(TdcRatings.RED_TQR));
  }

  public boolean isHill() {
    return hill;
  }

  public boolean isOp() {
    return op && !rubbled;
  }

  public int getMaxLOS() {
    return maxLOS;
  }

  public String getMaxLOSReason() {
    return maxLOSReason;
  }

  public String getBeachAttackClass() {
    return beachAttackClass;
  }

  public String getTerrain() {
    return terrain;
  }

  public String getTerrainPrintable() {
    return terrain.equals(TdcProperties.TERRAIN_BOCAGE) ? TdcProperties.TERRAIN_HEDGEROW : terrain;
  }

  public int getTerrainModifier() {
    final TerrainInfo ti = terrainInfo.get(terrain);
    return (ti == null) ? 0 : ti.fireModifier;
  }

  public int getTerrainModifierArmoured() {
    final TerrainInfo ti = terrainInfoArmoured.get(terrain);
    return (ti == null) ? 0 : ti.fireModifier;
  }

  public int getTerrainCanBeRubbled() {
    final TerrainInfo ti = terrainInfoRubble.get(terrain);
    return (ti == null) ? 0 : ti.fireModifier;
  }

  public boolean isInColumn() {
    return column;
  }

  public String getSector() {
    String sector = (String) unit.getProperty(TdcProperties.SECTOR);
    if (sector != null)
      return sector;

    final String location = unit.getMap().locationName(unit.getPosition());
    if (location == null)
      return null;

    final String[] coords = location.split("\\.");
    if (coords.length != 2)
      return null;

    int row;
    try {
      row = Integer.parseInt(coords[1]);
    }
    catch (Exception e) {
      return null;
    }

    int col;
    try {
      col = Integer.parseInt(coords[0]);
    }
    catch (Exception e) {
      return null;
    }

    if (row > 35)
      return null;

    if (col <= 22)
      return TdcProperties.DIVISION_6AB;
    if (col <= 48)
      return TdcProperties.DIVISION_3RD;
    if (col <= 67)
      return TdcProperties.DIVISION_3CAN;
    return TdcProperties.DIVISION_50TH;

  }

  protected void setupTerrainInfo() {
    terrainInfo = new HashMap<>();
    terrainInfo.put(TdcProperties.TERRAIN_CLEAR, new TerrainInfo(TdcProperties.TERRAIN_CLEAR, 0));
    terrainInfo.put(TdcProperties.TERRAIN_POLDER, new TerrainInfo(TdcProperties.TERRAIN_POLDER, 0));
    terrainInfo.put(TdcProperties.TERRAIN_OOIJ_POLDER, new TerrainInfo(TdcProperties.TERRAIN_OOIJ_POLDER, 0));
    terrainInfo.put(TdcProperties.TERRAIN_ORCHARD, new TerrainInfo(TdcProperties.TERRAIN_ORCHARD, isTgdRules() ? -2 : -1));
    terrainInfo.put(TdcProperties.TERRAIN_SCRUB, new TerrainInfo(TdcProperties.TERRAIN_SCRUB, -1));
    terrainInfo.put(TdcProperties.TERRAIN_WOODS, new TerrainInfo(TdcProperties.TERRAIN_WOODS, -2));
    terrainInfo.put(TdcProperties.TERRAIN_BRUSHWOODS, new TerrainInfo(TdcProperties.TERRAIN_BRUSHWOODS, -2));
    terrainInfo.put(TdcProperties.TERRAIN_SWAMP, new TerrainInfo(TdcProperties.TERRAIN_SWAMP, 1));
    terrainInfo.put(TdcProperties.TERRAIN_SAND_DUNES, new TerrainInfo(TdcProperties.TERRAIN_SAND_DUNES, -1));
    terrainInfo.put(TdcProperties.TERRAIN_CITY, new TerrainInfo(TdcProperties.TERRAIN_CITY, -3));
    terrainInfo.put(TdcProperties.TERRAIN_FORTIFIED, new TerrainInfo(TdcProperties.TERRAIN_FORTIFIED, -4));
    terrainInfo.put(TdcProperties.TERRAIN_DESERT, new TerrainInfo(TdcProperties.TERRAIN_DESERT, 0));
    terrainInfo.put(TdcProperties.TERRAIN_BOCAGE, new TerrainInfo(TdcProperties.TERRAIN_BOCAGE, -2));
    terrainInfo.put(TdcProperties.TERRAIN_HEDGEROW, new TerrainInfo(TdcProperties.TERRAIN_HEDGEROW, -2));
    terrainInfo.put(TdcProperties.TERRAIN_HILL, new TerrainInfo(TdcProperties.TERRAIN_HILL, -2));
    terrainInfo.put(TdcProperties.TERRAIN_VILLAGE, new TerrainInfo(TdcProperties.TERRAIN_VILLAGE, -1));
    terrainInfo.put(TdcProperties.TERRAIN_FLOODED, new TerrainInfo(TdcProperties.TERRAIN_FLOODED, 0));

    if (isTinianRules()) {
      terrainInfo.put(TdcProperties.TERRAIN_BEACH, new TerrainInfo(TdcProperties.TERRAIN_BEACH, 0));
      terrainInfo.put(TdcProperties.TERRAIN_MARSH, new TerrainInfo(TdcProperties.TERRAIN_MARSH, -2));
      terrainInfo.put(TdcProperties.TERRAIN_TOWN, new TerrainInfo(TdcProperties.TERRAIN_TOWN, -2));
      terrainInfo.put(TdcProperties.TERRAIN_BUILDINGS, new TerrainInfo(TdcProperties.TERRAIN_BUILDINGS, -2));
      terrainInfo.put(TdcProperties.TERRAIN_RUNWAY, new TerrainInfo(TdcProperties.TERRAIN_RUNWAY, 1));
      terrainInfo.put(TdcProperties.TERRAIN_BRUSH, new TerrainInfo(TdcProperties.TERRAIN_BRUSH, -1));
      terrainInfo.put(TdcProperties.TERRAIN_SUGAR_CANE, new TerrainInfo(TdcProperties.TERRAIN_SUGAR_CANE, -1));
      terrainInfo.put(TdcProperties.TERRAIN_ROCKY, new TerrainInfo(TdcProperties.TERRAIN_ROCKY, -2));
      terrainInfo.put(TdcProperties.TERRAIN_CAVE, new TerrainInfo(TdcProperties.TERRAIN_CAVE, -3));
    }
    else {
      terrainInfo.put(TdcProperties.TERRAIN_BEACH, new TerrainInfo(TdcProperties.TERRAIN_BEACH, 1));
      terrainInfo.put(TdcProperties.TERRAIN_MARSH, new TerrainInfo(TdcProperties.TERRAIN_MARSH, 1));
      terrainInfo.put(TdcProperties.TERRAIN_TOWN, new TerrainInfo(TdcProperties.TERRAIN_TOWN, -2));
    }

    terrainInfoArmoured = new HashMap<>();
    terrainInfoArmoured.put(TdcProperties.TERRAIN_CLEAR, new TerrainInfo(TdcProperties.TERRAIN_CLEAR, 0));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_POLDER, new TerrainInfo(TdcProperties.TERRAIN_POLDER, 0));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_OOIJ_POLDER, new TerrainInfo(TdcProperties.TERRAIN_OOIJ_POLDER, 0));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_ORCHARD, new TerrainInfo(TdcProperties.TERRAIN_ORCHARD, -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_SCRUB, new TerrainInfo(TdcProperties.TERRAIN_SCRUB, -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_WOODS, new TerrainInfo(TdcProperties.TERRAIN_WOODS, -2));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_BRUSHWOODS, new TerrainInfo(TdcProperties.TERRAIN_BRUSHWOODS, -2));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_SWAMP, new TerrainInfo(TdcProperties.TERRAIN_SWAMP, 1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_SAND_DUNES, new TerrainInfo(TdcProperties.TERRAIN_SAND_DUNES, isTgdRules() ? 0 : -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_CITY, new TerrainInfo(TdcProperties.TERRAIN_CITY, -2));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_FORTIFIED, new TerrainInfo(TdcProperties.TERRAIN_FORTIFIED, -3));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_DESERT, new TerrainInfo(TdcProperties.TERRAIN_DESERT, 0));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_BOCAGE, new TerrainInfo(TdcProperties.TERRAIN_BOCAGE, -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_HEDGEROW, new TerrainInfo(TdcProperties.TERRAIN_HEDGEROW, -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_HILL, new TerrainInfo(TdcProperties.TERRAIN_HILL, -1));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_VILLAGE, new TerrainInfo(TdcProperties.TERRAIN_VILLAGE, 0));
    terrainInfoArmoured.put(TdcProperties.TERRAIN_FLOODED, new TerrainInfo(TdcProperties.TERRAIN_FLOODED, 0));

    if (isTinianRules()) {
      terrainInfoArmoured.put(TdcProperties.TERRAIN_BEACH, new TerrainInfo(TdcProperties.TERRAIN_BEACH, 0));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_MARSH, new TerrainInfo(TdcProperties.TERRAIN_MARSH, -2));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_TOWN, new TerrainInfo(TdcProperties.TERRAIN_TOWN, -2));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_BUILDINGS, new TerrainInfo(TdcProperties.TERRAIN_BUILDINGS, -2));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_RUNWAY, new TerrainInfo(TdcProperties.TERRAIN_RUNWAY, 1));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_BRUSH, new TerrainInfo(TdcProperties.TERRAIN_BRUSH, -1));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_SUGAR_CANE, new TerrainInfo(TdcProperties.TERRAIN_SUGAR_CANE, -1));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_ROCKY, new TerrainInfo(TdcProperties.TERRAIN_ROCKY, -2));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_CAVE, new TerrainInfo(TdcProperties.TERRAIN_CAVE, 0));
    }
    else {
      terrainInfoArmoured.put(TdcProperties.TERRAIN_TOWN, new TerrainInfo(TdcProperties.TERRAIN_TOWN, -1));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_BEACH, new TerrainInfo(TdcProperties.TERRAIN_BEACH, 0));
      terrainInfoArmoured.put(TdcProperties.TERRAIN_MARSH, new TerrainInfo(TdcProperties.TERRAIN_MARSH, 0));
    }
    terrainInfoRubble = new HashMap<>();
    terrainInfoRubble.put(TdcProperties.TERRAIN_CITY, new TerrainInfo(TdcProperties.TERRAIN_CITY, -1));
    terrainInfoRubble.put(TdcProperties.TERRAIN_FORTIFIED, new TerrainInfo(TdcProperties.TERRAIN_FORTIFIED, -1));
  }

  protected String getDetails(ArrayList<String> details) {
    String result = "";
    for (String detail : details) {
      if (result.length() > 0) {
        result += ", ";
      }
      result += detail;
    }
    return result;
  }

  protected void adjustRating(String key, int value) {
    final String currentRatingString = ratings.get(key);
    if (currentRatingString == null) {
      return;
    }
    if ("No".equals(currentRatingString)) {
      return;
    }

    int rating;
    try {
      if (currentRatingString.startsWith("+")) {
        rating = Integer.parseInt(currentRatingString.substring(1));
      }
      else {
        rating = Integer.parseInt(currentRatingString);
      }
    }
    catch (NumberFormatException e) {
      rating = 0;
    }

    rating += value;
    setRating(key, Integer.toString(rating));
  }

  protected String displayInt(int value) {
    return value < 0 ? Integer.toString(value) : ("+" + value);
  }

  protected void setFireRating(String reason, String value) {
    setRating(TdcRatings.FIRE_RATING, value);
    fireDetails.add(reason);
  }

  protected void setMoveRating(String reason, String value) {
    setRating(TdcRatings.MOVE_RATING, value);
    moveAdjusted = true;
    adjustedMove = value;
    moveDetails.add(reason);
  }

  protected void setRangeRating(String reason, String value) {
    setRating(TdcRatings.RANGE, value);
    rangeDetails.clear();
    rangeDetails.add(reason);
  }

  protected void adjustFireRating(String reason, int value) {
    adjustRating(TdcRatings.FIRE_RATING, value);
    fireDetails.add(displayInt(value) + " " + reason);
  }

  protected void adjustAssaultRating(String reason, int value) {
    adjustRating(TdcRatings.ASSAULT_RATING, value);
    assaultDetails.add(displayInt(value) + " " + reason);
  }

  protected void adjustDefenceRating(String reason, int value) {
    adjustRating(TdcRatings.DEF_RATING, value);
    defenceDetails.add(displayInt(value) + " " + reason);
  }

  protected void adjustTqrRating(String reason, int value) {
    adjustRating(TdcRatings.TQR_RATING, value);
    tqrDetails.add(displayInt(value) + " " + reason);
  }

  protected void setRating(String key, String value) {
    ratings.put(key, value);
  }

  protected void setMaxRange(String reason, int value) {
    if (value < range) {
      range = value;
      setRangeRating(reason, Integer.toString(value));
    }
  }

  // Don't apply night or weather range maximums to
  // units firing Indirect.
  protected void setMaxRangeDTW(String reason, int value) {
    if (!isMortar() && !isArtillery()) {
      setMaxRange(reason, value);
      setMaxLOS(reason, value);
    }
  }

  protected void setMaxLOS(String reason, int value) {
    if (value < maxLOS) {
      maxLOS = value;
      maxLOSReason = reason;
    }
  }

  protected void findBasicInfo() {

    army = (String) unit.getProperty(TdcProperties.ARMY);
    division = (String) unit.getProperty(TdcProperties.DIVISION);
    formation = (String) unit.getProperty(TdcProperties.FORMATION);
    unitName = (String) unit.getProperty("BasicName");
    unitType = (String) unit.getProperty(TdcProperties.TYPE);
    unitClass = (String) unit.getProperty(TdcProperties.CLASS);
    unitSubClass = (String) unit.getProperty(TdcProperties.SUBCLASS);
    organic = (String) unit.getProperty(TdcProperties.ORGANIC);

    String r = ratings.get(TdcRatings.RANGE);
    if (r == null)
      r = "1";
    else if (r.equals("*")) {
      range = -1;
    }
    else {
      range = Integer.parseInt(r);
    }

    time = (String) GameModule.getGameModule().getProperty(TdcProperties.TIME);
    if (time == null)
      time = "";
    weather = (String) GameModule.getGameModule().getProperty(TdcProperties.WEATHER);
    if (weather == null)
      weather = "";
    date = (String) GameModule.getGameModule().getProperty(TdcProperties.DATE);
    if (date == null)
      date = "";

    hex = (String) unit.getProperty("LocationName");
    if (hex == null)
      hex = "";

    terrain = (String) unit.getProperty(TerrainBasicPiece.CURRENT_TERRAIN);
    if (terrain == null)
      terrain = "";

    hill = "true".equals(TdcMap.getTerrainProperty(unit, TdcProperties.TERRAIN_HILL));
    op = "true".equals(TdcMap.getTerrainProperty(unit, TdcProperties.OP));

    String s = (String) unit.getProperty(TdcProperties.STEP);
    if ("2".equals(s)) {
      steps = 2;
    }
    else if ("1".equals(s)) {
      steps = 1;
    }
    else {
      steps = 0;
    }

    // Beach Attack Class
    beachAttackClass = TdcProperties.BAC_OTHER;
    if ("true".equals(unit.getProperty(TdcProperties.MARKER_79TH))) {
      beachAttackClass = TdcProperties.BAC_79TH;
    }
    else {
      if (isEngineer()) {
        beachAttackClass = TdcProperties.BAC_ENGINEER;
      }
      else {
        if (isLandingCraft() || TdcProperties.CLASS_LCS.equals(unitClass)) {
          beachAttackClass = TdcProperties.BAC_LANDING_CRAFT;
        }
        else {
          if (isTank()) {
            beachAttackClass = TdcProperties.BAC_TANK;
          }
        }
      }
    }

    isAirPower = TdcProperties.CLASS_AIRPOWER.equals(unit.getProperty(TdcProperties.CLASS));
    isPartisanAttack = TdcProperties.CLASS_PARTISAN.equals(unit.getProperty(TdcProperties.CLASS));
    // Utah  / GJS   AirPower use full modifiers

    ignoreNegativeCombatModifiers = "true".equals(unit.getProperty(TdcProperties.COMBAT_NO_NEG)) && ! isTgdRules();
    ignoreAllCombatModifiers = "true".equals(unit.getProperty(TdcProperties.COMBAT_NO_MOD));
    final String flakRating = baseRatings.get(TdcRatings.FLAK_RATING);
    hasAirDefence = flakRating != null && flakRating.length() > 0 && !flakRating.equals("No");

    inArtilleryPark = TdcProperties.TGD_ARTY_PARK.equals(unit.getProperty(BasicPiece.CURRENT_ZONE));

  }

  public GamePiece getArtilleryParkPiece () {
    if (inArtilleryPark==false) {
      return null;
    }
    String artilleryParkId = (String) unit.getProperty(BasicPiece.LOCATION_NAME);
    String artilleryParkDivision = (String) unit.getProperty(TdcProperties.MAP_DIVISION);

    for (GamePiece p : GameModule.getGameModule().getGameState().getAllPieces()) {
      if (p instanceof Stack) {
        for (int i = 0; i < ((Stack) p).getPieceCount(); i++) {
          final GamePiece unit = ((Stack) p).getPieceAt(i);

          if (TdcProperties.TYPE_ARTILLERY_PARK.equals(unit.getProperty(TdcProperties.TYPE))) {
            if (artilleryParkDivision.equals(unit.getProperty(TdcProperties.DIVISION))) {
              if (artilleryParkId.equals(unit.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
                return unit;
              }
            }
          }
        }
      }
    }
    return null;
  }

  protected void addAttackModifier(String desc, int val) {
    addAttackModifier(desc, val, false);
  }

  protected void addAttackModifier(String desc, int val, boolean doNotDisplay) {
    if (isAttacking()) {
      attackModifiers.add(new FireModifier(desc, val, doNotDisplay));
    }
  }

  protected void addDefenceModifier(String desc, int val, boolean assaultDefenceOnly) {
    addDefenceModifier(desc, val, null, true, true, true, true, true);
  }

  protected void addDefenceModifier(String desc, int val) {
    addDefenceModifier(desc, val, null);
  }

  protected void addDefenceModifier(String desc, int val, boolean dir, boolean ind, boolean opp) {
    addDefenceModifier(desc, val, dir, ind, opp, false);
  }

  protected void addDefenceModifier(String desc, int val, boolean dir, boolean ind, boolean opp, boolean assault) {
    addDefenceModifier(desc, val, null, dir, ind, opp);
  }

  protected void addDefenceModifier(String desc, int val, GamePiece owner) {
    addDefenceModifier(desc, val, owner, true, true, true);
  }

  protected void addDefenceModifier(String desc, int val, GamePiece owner, boolean assaultDefenceOnly) {
    if (isAttacking()) {
      final FireModifier f = new FireModifier(desc, val, owner, true, true, true, true);
      f.setAssaultDefenceOnly(assaultDefenceOnly);
      defenceModifiers.add(f);
    }
  }

  protected void addDefenceModifier(String desc, int val, GamePiece owner, boolean dir, boolean ind, boolean opp, boolean asslt, boolean assaultDefenceOnly){
    if (isAttacking()) {
      final FireModifier f = new FireModifier(desc, val, owner, dir, ind, opp, asslt);
      f.setAssaultDefenceOnly(assaultDefenceOnly);
      defenceModifiers.add(f);
    }
  }

  protected void addDefenceModifier(String desc, int val, GamePiece owner, boolean dir, boolean ind, boolean opp) {
    addDefenceModifier(desc, val, owner, dir, ind, opp, true);
  }

  protected void addDefenceModifier(String desc, int val, GamePiece owner, boolean dir, boolean ind, boolean opp, boolean asslt) {
    addDefenceModifier(desc, val, owner, dir, ind, opp, asslt, false);
  }

  public ArrayList<FireModifier> getAttackModifiers() {
    return attackModifiers;
  }

  public ArrayList<FireModifier> getDefenceModifiers() {
    return defenceModifiers;
  }

  protected void checkTerrain() {
    final boolean raisedRoad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.RAISED_ROAD));
    final boolean raisedRailroad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.RAISED_RAILROAD));
    final boolean embankRoad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.EMBANKMENT_ROAD));
    final boolean embankRailroad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.EMBANKMENT_RAILROAD));
    final boolean sunkenRoad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.SUNKEN_ROAD));
    final boolean sunkenRailroad = TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.SUNKEN_RAILROAD));

    raised = raisedRoad || raisedRailroad;
    embanked = embankRoad || embankRailroad;
    sunken = sunkenRoad || sunkenRailroad;

    // -ve Terrain modifier is not added when in Column

    TerrainInfo ti;

    // Only TGD has separate modifies for armored/unarmored
    if (isArmoured() && UnitInfo.isTgdRules()) {
      ti = terrainInfoArmoured.get(terrain);
    }
    else {
      ti = terrainInfo.get(terrain);
    }

    String terrainDesc = terrain;
    if (UnitInfo.isTgdRules()) {
      terrainDesc += " (" + (isArmoured() ? "Armoured" : "Unarmoured") + ")";
    }

    if (ti != null) {
      if ((!column && ti.fireModifier != 0) || ti.fireModifier > 0) {
        adjustSpecialDefence(terrainDesc, ti.fireModifier);
      }
    }

    if (raised) {
      adjustSpecialDefence("Raised Road/RR", 2);
      addDefenceModifier("Raised Road/RR", 2, true);
    }

    if (embanked) {
      adjustSpecialDefence("Embankment Road/RR", 2);
      addDefenceModifier("Embankment Road/RR", 2, true);
    }

    if (sunken) {
      final int modifier = isVehicle() ? -3 : -2;
      adjustSpecialDefence("Sunken Road/RR", modifier);
      addDefenceModifier("Sunken Road/RR", modifier, true);
    }

    final String hill = (String) TdcMap.getTerrainProperty(unit, TdcProperties.HILL);
    if ("true".equals(hill) & !column) {
      final int modifier = isArmoured() ? -2 : -1;
      adjustSpecialDefence("Hill", modifier);
      addDefenceModifier("Hill", modifier, true, false, true, true);
    }
  }

  // Any adjacent counters?
  protected void checkAdjacentCounters() {

    int distance;
    final Map map = unit.getMap();
    if (map == null) {
      return;
    }
    final Point unitPos = unit.getPosition();
    final Board board = map.findBoard(unitPos);
    if (board == null)
      return;

    final GamePiece[] pieces = unit.getMap().getPieces();
    for (GamePiece piece : pieces) {
      if (piece instanceof Stack) {
        final MapGrid grid = board.getGrid();
        if (grid != null) {
          distance = board.getGrid().range(unit.getPosition(), piece.getPosition());
          if (distance == 1) {
            for (Iterator<GamePiece> i = ((Stack) piece).getPiecesIterator(); i.hasNext();) {
              final GamePiece p = i.next();
              final String type = (String) p.getProperty(TdcProperties.TYPE);
              if (TdcRatings.isUnitType(type)) {
                if (army != null && !army.equals(p.getProperty(TdcProperties.ARMY))) {
                  setMaxRange("Adjacent Enemy", 1);
                  break;
                }
              }
            }
          }
        }
      }
    }
  }

  /*
   * Two Step unit on its One Step side?
   */
  protected void checkStepLoss() {
    if ("2".equals(unit.getProperty(TdcProperties.STEP))) {
      if ("true".equals(unit.getProperty("Step_Active"))) {
        adjustForStepLoss(false);
        stepLoss = true;
        steps = 1;
      }
    }
  }

  // Check for column mode
  protected void checkColumn() {
    if ("true".equals(unit.getProperty("Column_Active"))) {
      adjustSpecialFire("In Column", -1);
      addAttackModifier("In Column", -1);
      adjustSpecialAssault("In Column", -1);
      adjustSpecialDefence("In Column", 2);
      addDefenceModifier("In Column", 2, unit);
      column = true;
    }
  }

  /*
   * Check if unit is In Command
   */
  protected void checkInCommand() {
    final CommandRangeChecker crc = new CommandRangeChecker(unit, this);
    nonFormationCommand = crc.isNonFormationCommand();
    inCommand = crc.inCommand();
    if (!inCommand) {
      if (isCommando()) {
        adjustSpecialTqr("Commando OOC", -3);
        adjustSpecialFire("Commando OOC", -1);
        addAttackModifier("Commando OOC", -1);
        adjustSpecialAssault("Commando OOC", -1);
        // setMoveRating("Commando OOC", "No");
      }
      else {
        adjustSpecialTqr("Not In Command", -1);
      }
    }
  }

  /*
   * Check for mods due to the current Date, Time or Weather
   */
  protected void checkDateTimeWeather() {

    // Set Default Max LOS
    if (isHill() || isOp()) {
      if (isHill()) {
        setMaxLOS("Hill", 13);
      }

      if (isOp()) {
        setMaxLOS("OP", 13);
      }
    }
    else {
      setMaxLOS("Clear", 8);
    }

    if (TdcProperties.TIME_NIGHT.equals(time)) {
      weatherAdjusted = true;
      final int nightRange = (isNqosRules() || isTgdRules() || isCreteRules()) ? 3 : 2;
      adjustSpecialFire("Night", -2);
      addAttackModifier("Night", -2);
      adjustSpecialAssault("Night", -2);

      setMaxRangeDTW("Night", nightRange);
    }

    if (weather.startsWith(TdcProperties.WEATHER_FOG)) {
      weatherAdjusted = true;
      adjustSpecialFire("Fog", -1);
      addAttackModifier("Fog", -1);
      adjustSpecialAssault("Fog", -1);
      setMaxRangeDTW("Fog", 2);
    }
    else if (TdcProperties.WEATHER_MIST.equals(weather)) {
      weatherAdjusted = true;
      adjustSpecialFire("Mist", -1);
      addAttackModifier("Mist", -1);
      adjustSpecialAssault("Mist", -1);
      setMaxRangeDTW("Mist", 3);
    }
    else if (TdcProperties.WEATHER_RAIN.equals(weather)) {
      weatherAdjusted = true;
      adjustSpecialFire("Rain", -2);
      addAttackModifier("Rain", -2);
      adjustSpecialAssault("Rain", -2);
      setMaxRangeDTW("Rain", 2);
    }
    else if (TdcProperties.WEATHER_SANDSTORM.equals(weather)) {
      weatherAdjusted = true;
      adjustSpecialFire("Sand Storm", -2);
      addAttackModifier("Sand Storm", -2);
      adjustSpecialAssault("Sand Storm", -2);
      setMaxRange("Sand Storm", 2);
      setMaxLOS("Sand Storm", 2);
    }
    else if (TdcProperties.WEATHER_STORM.equals(weather)) {
      weatherAdjusted = true;
      if (TdcProperties.TIME_NIGHT.equals(time)) {
        setMaxRangeDTW("Night Storm", 2);
      }
      else {
        setMaxRangeDTW("Storm", 4);
      }
    }
  }

  /*
   * Run through the Stack the unit is in. Look for any Barrage Marker, and for
   * Co Hits, Supression and Step Loss markers immediately above this unit. IP,
   * Entrenchments, Para and Glider Markers. And Bocage markers
   */

  /*
   * If this counter is a Landing Craft, then do not count the mass of any units
   * underneath it. If this counter is not a Landing Craft, then do not count
   * the mass of a Landing Craft under it. Search is from the bottom up.
   */

  /*
   * Check for overstacking - GTS2 may have 8 steps not in column PLUS one unit in column.
   */
  protected void checkStack() {
    final Stack parent = unit.getParent();
    if (parent == null) {
      // Non-stacking Combat unit - should not happen
      return;
    }
    boolean sourceSeen = false;
    boolean finished = false;
    boolean landingCraftSeen = false;

    for (Iterator<GamePiece> i = parent.getPiecesIterator(); i.hasNext();) {
      final GamePiece piece = i.next();
      final String type = (String) piece.getProperty(TdcProperties.TYPE);
      final String unitClass = (String) piece.getProperty(TdcProperties.CLASS);

      // Check for Barrage Counter
      // Except for airpower attack
      if (!isAirPower()) {
        if (TdcProperties.TYPE_BARRAGE.equals(type)) {
          final String barrageLevel = (String) piece.getProperty("Barrage_Level");
          if ("1".equals(barrageLevel) && barrage == 0) {
            barrage = 1;
          } else if ("2".equals(barrageLevel) && barrage < 2) {
            barrage = 2;
          }
        }
      }

      // Check for Rubble Counter in City or Fortified hex
      if (!rubbled) {
        if (TdcProperties.TYPE_RUBBLE.equals(type)) {
          final String rubbleLevel = (String) piece.getProperty("Rubble_Level");

          if (TdcProperties.TERRAIN_CITY.equals(terrain) || TdcProperties.TERRAIN_FORTIFIED.equals(terrain)) {
            rubbled = true;
            if ("1".equals(rubbleLevel)) {
              adjustSpecialDefence("Rubble", -1);
              addDefenceModifier("Rubble", -1);
            }
          }
        }
      }

      // Count Steps
      String step = (String) piece.getProperty(TdcProperties.STEP);
      int thisMass = 0;
      if (step != null) {
        if (step.equals("-1")) {
          thisMass -= 1;
        }
        else if (step.equals("1")) {
          thisMass += 1;
        }
        else if (step.equals("2")) {
          String active = (String) piece.getProperty("Step_Active");
          thisMass += TRUE.equals(active) ? 1 : 2;
        }
      }

      if (!landingCraftSeen && TdcProperties.CLASS_LANDING_CRAFT.equals(unitClass)) {
        landingCraftSeen = true;
      }

      // If the counter is a landing craft and we have seen a landing craft in
      // the stack, the add the mass, as the Landing Craft is below us!!!!
      // ie. This stack item is the landing craft or above it.
      if (isLandingCraft()) {
        if (landingCraftSeen) {
          mass += thisMass;
        }
      }
      // Our counter is not a landing craft, so we ignore the mass any landing
      // craft in the stack above us
      else {
        if (!TdcProperties.CLASS_LANDING_CRAFT.equals(unitClass) || ! landingCraftSeen) {
          mass += thisMass;
        }
      }

      // Loop till we find our unit
      if (!sourceSeen) {
        if (piece == unit) {
          sourceSeen = true;
        }
        continue;
      }

      // Source unit has been sighted. A Different Unit type finishes the search
      if (TdcRatings.isUnitType(type)) {
        finished = true;
      }

      if (finished) {
        continue;
      }

      // Process any Adjustment counters
      final String adjust = (String) piece.getProperty(TdcProperties.ADJUST);
      if (TRUE.equals(adjust)) {
        if (TdcProperties.TYPE_COHESION_HIT.equals(type)) {
          if (cohit == 0) {
            final String cohitLevel = (String) piece.getProperty("Cohesion Hit_Level");
            if ("1".equals(cohitLevel)) {
              cohit = 1;
              adjustFireRating("Cohesion Hit", -1);
              addAttackModifier("Cohesion Hit", -1);
              adjustAssaultRating("Cohesion Hit", -1);
            }
            else if ("2".equals(cohitLevel)) {
              cohit = 2;
              adjustFireRating("Cohesion Hit x 2", -2);
              addAttackModifier("Cohesion Hit x 2", -2);
              adjustAssaultRating("Cohesion Hit x 2", -2);
              adjustTqrRating("Cohesion Hit x 2", -1);
            }
          }
        }
        // Entrenchment counter (no IP side)
        else if (TdcProperties.TYPE_ENTRENCH.equals(type)) {
          if (!entrench && !ip) {
            entrench = true;
            adjustFireRating("Entrenched", 1);
            addAttackModifier("Entrenched", 1);
            adjustAssaultRating("Entrenched", 1);
            adjustDefenceRating("Entrenched", -2);
            addDefenceModifier("Entrenched", -2, unit, true, true, false, true, true);
            adjustTqrRating("Entrenched", 2);
          }
        }

        // Parachute delay counter
        else if (TdcProperties.TYPE_DELAY_P.equals(type)) {
          delayed = true;
          final String delayLevel = (String) piece.getProperty("Delay_Level");
          if ("1".equals(delayLevel)) {
            adjustFireRating("Para Delay", -2);
            addAttackModifier("Para Delay", -2);
            adjustAssaultRating("Para Delay", -2);
            adjustDefenceRating("Para Delay", 2);
            addDefenceModifier("Para Delay", 2, unit);
          }
          else if ("2".equals(delayLevel)) {
            adjustFireRating("Para Delay x 2", -4);
            addAttackModifier("Para Delay x 2", -4);
            adjustAssaultRating("Para Delay x 2", -4);
            adjustDefenceRating("Para Delay x 2", 4);
            addDefenceModifier("Para Delay x 2", 4, unit);
          }
        }

        // Glider delay counter
        else if (TdcProperties.TYPE_DELAY_G.equals(type)) {
          delayed = true;
          final String delayLevel = (String) piece.getProperty("Delay_Level");
          if ("1".equals(delayLevel)) {
            adjustFireRating("Glider Delay", -1);
            addAttackModifier("Glider Delay", -1);
            adjustAssaultRating("Glider Delay", -1);
            adjustDefenceRating("Glider Delay", 3);
            addDefenceModifier("Glider Delay", 3, unit);
          }
          else if ("2".equals(delayLevel)) {
            adjustFireRating("Glider Delay x 2", -2);
            addAttackModifier("Glider Delay x 2", -2);
            adjustAssaultRating("Glider Delay x 2", -2);
            adjustDefenceRating("Glider Delay x 2", 6);
            addDefenceModifier("Glider Delay x 2", 6, unit);
          }
        }

        // Air Landing Delay (Crete)
        else if (TdcProperties.TYPE_DELAY_A.equals(type)) {
          delayed = true;
          adjustFireRating("Air Landing Delay", -2);
          addAttackModifier("Air Landing Delay", -2);
          adjustAssaultRating("Air Landing Delay", -2);
          adjustDefenceRating("Air Landing Delay", 2);
          addDefenceModifier("Air Landing Delay", 2, unit);
          adjustTqrRating("Air Landing Delay", -1);
          setMoveRating("Air Landing Delay", "No");
        }

        // IP/Entrenchement CCounter
        else if (TdcProperties.TYPE_IP.equals(type)) {
          if (!entrench && !ip) {
            final String ipLevel = (String) piece.getProperty("IP_Level");
            if ("2".equals(ipLevel)) {
              entrench = true;
              adjustFireRating("Entrenched", 1);
              addAttackModifier("Entrenched", 1);
              adjustAssaultRating("Entrenched", 1);
              adjustDefenceRating("Entrenched", -2);
              addDefenceModifier("Entrenched", -2, unit, true, true, false, true, true);
              adjustTqrRating("Entrenched", 2);

            }
            else {
              ip = true;
              adjustDefenceRating("IP", -1);
              addDefenceModifier("IP", -1, unit, true);
              adjustTqrRating("IP", 1);
            }
          }
        }

        // Step Loss Counter
        else if (TdcProperties.TYPE_STEP_LOSS.equals(type)) {
          if (!stepLoss && steps == 2) {
            stepLoss = true;
            steps = 1;
            adjustForStepLoss(true);
          }
        }
        // Supression Counter
        else if (TdcProperties.TYPE_SUPPRESSED.equals(type)) {
          if (!suppressed) {
            suppressed = true;
            setFireRating("Suppressed", "No");
            adjustAssaultRating("Suppressed", -2);
            adjustTqrRating("Suppressed", -1);
            setMoveRating("Suppressed", "No");
          }
        }

        // Bocage marker
        else if (TdcProperties.TYPE_BOCAGE.equals(type)) {
          adjustFireRating("In Bocage", 1);
          addAttackModifier("In Bocage", 1);
          adjustAssaultRating("In Bocage", 1);
          adjustTqrRating("In Bocage", 1);
          adjustDefenceRating("In Bocage", -1);
          addDefenceModifier("In Bocage", -1, unit, true);
          setMoveRating("In Bocage", "No");
        }

        // Column Marker
        else if (TdcProperties.TYPE_COLUMN.equals(type)) {
          if (!column) {
            column = true;
            adjustFireRating("In Column", -1);
            addAttackModifier("In Column", -1);
            adjustAssaultRating("In Column", -1);
            adjustDefenceRating("In Column", 2);
            addDefenceModifier("In Column", 2, unit);
          }
        }

        // Heroic Event
        else if (TdcProperties.CLASS_HEROIC.equals(unitClass)) {
          if (!heroic) {
            heroic = true;
            adjustSpecialTqr("Heroic", 1);
            adjustSpecialDefence("Heroic", -1);
            addDefenceModifier("Heroic", -1, unit);
          }
        }

        // Hero marker
        else if (TdcProperties.TYPE_HERO.equals(type)) {
          adjustFireRating("Hero", 2);
          addAttackModifier("Hero", 2);
          adjustAssaultRating("Hero", 2);
          adjustTqrRating("Hero", 1);
          adjustDefenceRating("Hero", -2);
          addDefenceModifier("Hero", -2, unit);
        }

        // Alone & Afraid Marker?
        else if (TdcProperties.TYPE_HERO.equals(type)) {
          adjustFireRating("Alone & Afraid", -1);
          addAttackModifier("Alone & Afraid", -1);
          adjustAssaultRating("Alone & Afraid", -1);
          adjustTqrRating("Alone & Afraid", -1);
          adjustDefenceRating("Alone & Afraid", +1);
          addDefenceModifier("Alone & Afraid", +1, unit);
        }

        // Cave (Tinian)
        else if (TdcProperties.TYPE_CAVE.equals(type)) {
          adjustDefenceRating("In Cave", -3);
          addDefenceModifier("In Cave", -3, unit);
        }

      }
    }

    if (barrage > 0) {
      setMaxRange("Barraged", 1);
      adjustFireRating("Barraged", -barrage);
      addAttackModifier("Barraged", -barrage);
      adjustAssaultRating("Barraged", -barrage);
      adjustTqrRating("Barraged", -barrage);
    }

    // Overstacked?
  }

  protected void adjustForStepLoss(boolean fromStepLossCounter) {

    // Does the counter have an alternate Step Loss side?
    boolean diffStep = "true".equals(ratings.get(TdcRatings.DIFF_STEP));

    if (diffStep) {

    }
    else {
      // Step Loss counter, standard -1 modifiers
      adjustFireRating("Step Loss", -1);
      addAttackModifier("Step Loss", -1, !fromStepLossCounter);
      adjustAssaultRating("Step Loss", -1);
      adjustTqrRating("Step Loss", -1);
    }

  }

  /*
   * Check for modifiers due to special rules
   */
  protected void checkSpecialRules() {

    // Germans in Germany
    if (TdcProperties.ARMY_GERMAN.equals(unit.getProperty(TdcProperties.ARMY))) {
      if (TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.IN_GERMANY))) {
        adjustSpecialTqr("In Germany", 1);
      }
    }

    // Artillery Units in an Artillery Park on the Bir Hacheim Map
    if (TdcProperties.BOARD_BIR_HACHEIM.equals(unit.getProperty("CurrentBoard"))) {
      if (TRUE.equals(unit.getProperty(TdcProperties.INDIRECT))) {
        final String location = (String) unit.getProperty("LocationName");
        if (location != null) {
          if (location.startsWith(TdcProperties.ARTILLERY_PARK)) {
            adjustSpecialDefence("In Artillery Park", -1);
            addDefenceModifier("In Artillery Park", -1, unit);
          }
        }
      }
    }

    // 1AB drop zone closed
    if (TdcProperties.DIVISION_1AB.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.SDZ_1AB))) {
        adjustSpecialTqr("Drop Zone Closed", -1);
      }
    }
    // 82AB Drop zone closed
    else if (TdcProperties.DIVISION_82AB.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.SDZ_82AB))) {
        adjustSpecialTqr("Drop Zone Closed", -1);
      }
    }
    // 101st Drop zone closed
    else if (TdcProperties.DIVISION_101AB.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.SDZ_101AB))) {
        adjustSpecialTqr("Drop Zone Closed", -1);
      }
    }
    // 52nd AL drop zone closed
    else if (TdcProperties.DIVISION_52AL.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.SDZ_52AL))) {
        adjustSpecialTqr("Drop Zone Closed", -1);
      }
    }
    // 43rd or Guards Armoured and Club Route closed
    else if (TdcProperties.DIVISION_43RD.equals(division) || TdcProperties.DIVISION_GDS.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.CLUB_ROUTE))) {
        adjustSpecialTqr("Club Route Closed", -2);
      }
    }
    // 9ss or 10ss south of closed Arnhem bridge
    else if (TdcProperties.DIVISION_HOH.equals(division) || TdcProperties.DIVISION_FR.equals(division)) {
      if (CLOSED.equals(unit.getProperty(TdcProperties.ARNHEM_BRIDGE))) {
        if (isSouthOfRhine(unit)) {
          adjustSpecialTqr("South of Closed Arnhem Bridge", -1);
        }
      }
    }
    // KG Chill TQ and Assault bonus
    else if (TdcProperties.DIVISION_59.equals(division) || TdcProperties.DIVISION_WALTHER.equals(division) || TdcProperties.DIVISION_EINDHOVEN.equals(division)) {
      if (isWEDMap(unit)) {
        if (TdcProperties.CHILL_IND.equals(unit.getProperty(TdcProperties.EINDHOVEN_CHILL))) {
          adjustSpecialTqr("KG Chill Bonus", 1);
          if (TdcRatings.WHITE.equals(ratings.get(TdcRatings.ASSAULT_COLOUR))) {
            adjustSpecialAssault("KG Chill Bonus", 1);
          }
        }
      }
    }
    // French in Bir Hachiem or reduced supply
    else if (TdcProperties.DIVISION_FRENCH.equals(division)) {
      if (TRUE.equals(TdcMap.getTerrainProperty(unit, TdcProperties.BIR_HACHEIM))) {
        adjustSpecialTqr("In Bir Hacheim", 1);
        adjustSpecialAssault("In Bir Hacheim", 1);
        adjustSpecialFire("In Bir Hacheim", 1);
        addAttackModifier("In Bir Hacheim", 1);
        adjustSpecialDefence("In Bir Hacheim", -2);
        addDefenceModifier("In Bir Hacheim", -2, true, true, false);
        entrench = true;
      }
      final String supply = (String) unit.getProperty(TdcProperties.FRENCH_SUPPLY);
      if (TdcProperties.DIMISHED_SUPPLY.equals(supply)) {
        adjustSpecialTqr("Diminished Supply", -1);
      }
      else if (TdcProperties.EMERGENCY_SUPPLY.equals(supply)) {
        adjustSpecialTqr("Emergency Supply", -2);
      }
    }

    // Italian Artillery when Italian Artillery Leader dead event in place
    else if (TdcProperties.DIVISION_ARIETE.equals(division) || TdcProperties.DIVISION_TRIESTE.equals(division)) {
      if ("true".equals(unit.getProperty(TdcProperties.INDIRECT))) {
        if (TdcProperties.ITALIAN_ARTY_LEADER_DEAD.equals(unit.getProperty(TdcProperties.ITALIAN_ARTY_LEADER))) {
          adjustSpecialTqr("Artillery Leader Dead", -2);
        }
      }
    }

    // Divisional TQR adjustment
    final String adjust = (String) unit.getProperty("TQA-" + division);
    int tqAdjust = 0;
    if ("2".equals(adjust)) {
      tqAdjust = 1;
    }
    else if ("1".equals(adjust)) {
      tqAdjust = 1;
    }
    else if ("-1".equals(adjust)) {
      tqAdjust = -1;
    }
    else if ("-2".equals(adjust)) {
      tqAdjust = -2;
    }
    if (tqAdjust != 0) {
      adjustSpecialTqr("Divisional TQR Adjustment", tqAdjust);
    }

    /* --- CRETE Supply Levels -- */
    final Map map = unit.getMap();
    if (map == null) {
      return;
    }

    // Work out which 'set' of maps, based on board name
    final String boardName = (String) unit.getProperty(BasicPiece.CURRENT_BOARD);
    if (boardName == null) {
      return;
    }

    if (TdcProperties.SUBCLASS_GREEK.equals(unitSubClass)) {
      return;
    }

    String supplyVar = null;
    if (boardName.startsWith(TdcProperties.BOARD_KASTELLI) || boardName.startsWith(TdcProperties.BOARD_MALEME) || boardName.startsWith(TdcProperties.BOARD_CANEA_SUDA) || boardName.startsWith(TdcProperties.BOARD_GEORGEOPOLIS)) {
      supplyVar = TdcProperties.SUPPLY_MALEME_SUDA;
    }
    else if (boardName.startsWith(TdcProperties.BOARD_RETHYMON)) {
      supplyVar = TdcProperties.SUPPLY_RETHYMON;
    }
    else if (boardName.startsWith(TdcProperties.BOARD_HERAKLION)) {
      supplyVar = TdcProperties.SUPPLY_HERAKLION;
    }

    supplyVar = "Supply-" + army + "-" + supplyVar;
    String supplyLevel = (String) GameModule.getGameModule().getProperty(supplyVar);

    if (TdcProperties.SUPPLY_DIMINISHED.equals(supplyLevel)) {
      adjustSpecialTqr("Diminished Supply", -1);
    }
    else if (TdcProperties.SUPPLY_EMERGENCY.equals(supplyLevel)) {
      adjustSpecialTqr("Emergency Supply", -2);
      adjustSpecialFire("Emergency Supply", -1);
      addAttackModifier("Emergency Supply", -1);
    }
  }

  // If unit is Artillery in an Artillery park, then find and check if the Artillery Park Marker
  // on the map is Barraged and apply the mods to this counter.
  protected void checkArtilleryPark() {
    if (!isArtillery() || !inArtilleryPark)
      return;

    GamePiece artilleryParkMarker = null;

    final String artilleryParkId = (String) unit.getProperty(BasicPiece.LOCATION_NAME);
    final String artilleryParkDivision = (String) unit.getProperty(TdcProperties.MAP_DIVISION);

    for (GamePiece piece : GameModule.getGameModule().getGameState().getAllPieces()) {
      if (piece instanceof Stack) {
        final Stack stack = (Stack) piece;
        for (int i = 0; i < stack.getPieceCount(); i++) {
          final GamePiece p = stack.getPieceAt(i);

          if (TdcProperties.TYPE_ARTILLERY_PARK.equals(p.getProperty(TdcProperties.TYPE))) {
            if (artilleryParkDivision.equals(p.getProperty(TdcProperties.DIVISION))) {
              if (artilleryParkId.equals(p.getProperty(TdcProperties.ARTILLERY_PARK_ID))) {
                artilleryParkMarker = p;
                break;
              }
            }
          }
        }
      }
    }

    if (artilleryParkMarker == null) return;

    final Stack stack = artilleryParkMarker.getParent();
    if (stack == null) return;

    for (int i = 0; i < stack.getPieceCount(); i++) {
      final GamePiece piece = stack.getPieceAt(i);

      if (TdcProperties.TYPE_BARRAGE.equals(piece.getProperty(TdcProperties.TYPE))) {
        final String barrageLevel = (String) piece.getProperty("Barrage_Level");
        if ("1".equals(barrageLevel) && barrage == 0) {
          barrage = 1;
        }
        else if ("2".equals(barrageLevel) && barrage < 2) {
          barrage = 2;
        }
        adjustFireRating("Barraged", -barrage);
        addAttackModifier("Barraged", -barrage);
        adjustAssaultRating("Barraged", -barrage);
        adjustTqrRating("Barraged", -barrage);
      }
    }
  }

  protected boolean isSouthOfRhine(GamePiece p) {
    String board = (String) p.getProperty("CurrentBoard");
    if (board == null)
      return false;

    if (board.equals("Deelan")) {
      return false;
    }

    final String southInd = (String) TdcMap.getTerrainProperty(p, TdcProperties.RHINE);

    if (board.startsWith("Red Devils") || board.equals("Ede 1") || board.equals("Arnhem 1") || board.startsWith("Pannerden")) {
      return "south".equals(southInd);
    }

    if (board.equals("The Island 1") || board.equals("Ede 2") || board.equals("Arnhem 2")) {
      return !"north".equals(southInd);
    }

    if (board.startsWith("All American") || board.startsWith("Groesbeek") || board.startsWith("Grave") || board.startsWith("The Island") || board.equals("Cuijk")) {
      return true;
    }

    return false;
  }

  protected boolean isWEDMap(GamePiece p) {
    String board = (String) p.getProperty("CurrentBoard");
    if (board == null)
      return false;

    return board.startsWith("WED");
  }

  private void adjustSpecialDefence(String reason, int i) {
    defenceAdjusted = true;
    defenceAdjustment += i;
    adjustDefenceRating(reason, i);
  }

  private void adjustSpecialFire(String reason, int i) {
    fireAdjusted = true;
    fireAdjustment += i;
    adjustFireRating(reason, i);
  }

  private void adjustSpecialAssault(String reason, int i) {
    assaultAdjusted = true;
    assaultAdjustment += i;
    adjustAssaultRating(reason, i);
  }

  private void adjustSpecialTqr(String reason, int i) {
    tqrAdjusted = true;
    tqrAdjustment += i;
    adjustTqrRating(reason, i);
  }

  public boolean isInCommand() {
    return inCommand;
  }

  public boolean isNonFormationCommand() {
    return nonFormationCommand;
  }

  public boolean isTqrAdjusted() {
    return tqrAdjusted;
  }

  public boolean isFireAdjusted() {
    return fireAdjusted;
  }

  public boolean isAssaultAdjusted() {
    return assaultAdjusted;
  }

  public boolean isDefenceAdjusted() {
    return defenceAdjusted;
  }

  public boolean isMoveAdjusted() {
    return moveAdjusted;
  }

  public String getAdjustedMove() {
    return adjustedMove;
  }

  public int getTqrAdjustment() {
    return tqrAdjustment;
  }

  public String getTqrAdjustmentString() {
    return ((tqrAdjustment > 0) ? "+" : "") + tqrAdjustment;
  }

  public String getFireAdjustmentString() {
    return ((fireAdjustment > 0) ? "+" : "") + fireAdjustment;
  }

  public String getAssaultAdjustmentString() {
    return ((assaultAdjustment > 0) ? "+" : "") + assaultAdjustment;
  }

  public String getDefenceAdjustmentString() {
    return ((defenceAdjustment > 0) ? "+" : "") + defenceAdjustment;
  }

  public int getEffectiveRange() {
    return range;
  }

  public int getBaseRange() {
    String r = baseRatings.get(TdcRatings.RANGE);
    if (r.equals("*")) {
      return 99;
    }

    try {
      return Integer.parseInt(baseRatings.get(TdcRatings.RANGE));
    }
    catch (Exception e) {
      return 99;
    }
  }

  public int getBaseFireRating() {
    return Integer.parseInt(baseRatings.get(TdcRatings.FIRE_RATING));
  }

  public int getBaseAssaultRating() {
    return Integer.parseInt(baseRatings.get(TdcRatings.ASSAULT_RATING));
  }

  public String getEffectiveFireRating() {
    return ratings.get(TdcRatings.FIRE_RATING);
  }

  public String getEffectiveAssaultRating() {
    return ratings.get(TdcRatings.ASSAULT_RATING);
  }

  public String getEffectiveDefenceRating() {
    return ratings.get(TdcRatings.DEF_RATING);
  }

  public boolean isEffectiveDoubleAssault() {
    return "true".equals(ratings.get(TdcRatings.ASSAULT_DOUBLE));
  }

  public boolean isRubblePossible() { return (getTerrainCanBeRubbled() == -1) && !rubbled; }

  public int getDefenceRating() {
    String dr = baseRatings.get(TdcRatings.DEF_RATING);
    if (dr == null)
      dr = "0";
    return dr.startsWith("+") ? Integer.parseInt(dr.substring(1)) : Integer.parseInt(dr);
  }

  public int getEffectiveTqr() {
    try {
      return Integer.parseInt(ratings.get(TdcRatings.TQR_RATING));
    }
    catch (Exception e) {
      return 0;
    }
  }

  public HashMap<String, String> getRatings() {
    return ratings;
  }

  public HashMap<String, String> getBaseRatings() {
    return baseRatings;
  }

  public GamePiece getUnit() {
    return unit;
  }

  /*
   * Display a Dialog box containing an Information Display about this counter
   */
  public void displayInfo() {
    if (infoDialog == null && unit.getMap() != null) {
      showInfoDialog();
    }
  }

  protected static class TerrainInfo {
    protected String name;
    protected int fireModifier;

    public TerrainInfo(String name, int fireModifier) {
      this.name = name;
      this.fireModifier = fireModifier;
    }
  }

  protected void showInfoDialog() {
    infoDialog = new InfoDialog(this);
    infoDialog.setVisible(true);
  }

  public BufferedImage getInfoImage() {
    infoDialog = new InfoDialog(this);
    final BufferedImage image = infoDialog.getImage();
    infoDialog.dispose();
    infoDialog = null;
    return image;
  }

  protected void infoClosed() {
    infoDialog = null;
  }

  class InfoDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    protected UnitInfo info;
    protected JPanel panel;

    public InfoDialog(UnitInfo ui) {
      super(GameModule.getGameModule().getPlayerWindow());
      info = ui;
      setLayout(new BorderLayout());
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          dispose();
          info.infoClosed();
        }
      });
      final Map map = info.unit.getMap();
      final Point pp = map.mapToComponent(info.unit.getPosition());
      final Point cp = map.getView().getLocationOnScreen();
      setLocation(cp.x + pp.x, cp.y + pp.y);

      panel = new JPanel();
      panel.setBorder(BorderFactory.createEtchedBorder());
      fillPanel();
      add(panel, BorderLayout.CENTER);
      pack();
    }

    public JPanel getPanel() {
      return panel;
    }

    public BufferedImage getImage() {
      final BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
      final Graphics2D imageGraphics = image.createGraphics();
      panel.paint(imageGraphics);
      return image;
    }

    protected void fillPanel() {
      JLabel label;

      @SuppressWarnings("unchecked")
      final HashMap<String, String> baseRatings = (HashMap<String, String>) info.unit.getProperty(TdcProperties.RATINGS);
      final HashMap<String, String> curRatings = info.getRatings();

      panel.setLayout(new MigLayout("", "[][center][center][]"));

      final Font headFont = new Font(Font.DIALOG, Font.BOLD, 12);
      final Font mainFont = new Font(Font.DIALOG, Font.BOLD, 14);

      label = new JLabel(unitName);
      label.setFont(mainFont);
      panel.add(label, "span 4,center,wrap");

      label = new JLabel("Rating");
      label.setFont(headFont);
      panel.add(label);
      label = new JLabel("Base");
      label.setFont(headFont);
      panel.add(label);
      label = new JLabel("Cur");
      label.setFont(headFont);
      panel.add(label);
      label = new JLabel("Notes");
      label.setFont(headFont);
      panel.add(label, "wrap");

      panel.add(new JLabel("Movement"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.MOVE_RATING)));
      panel.add(new JLabel(curRatings.get(TdcRatings.MOVE_RATING)));
      panel.add(new JLabel(info.getMoveDetails()), "wrap");

      panel.add(new JLabel("Fire"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.FIRE_RATING)));
      panel.add(new JLabel(curRatings.get(TdcRatings.FIRE_RATING)));
      panel.add(new JLabel(info.getFireDetails()), "wrap");

      panel.add(new JLabel("Assault"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.ASSAULT_RATING)));
      panel.add(new JLabel(curRatings.get(TdcRatings.ASSAULT_RATING)));
      panel.add(new JLabel(info.getAssaultDetails()), "wrap");

      panel.add(new JLabel("TQR"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.TQR_RATING)));
      panel.add(new JLabel(curRatings.get(TdcRatings.TQR_RATING)));
      panel.add(new JLabel(info.getTqrDetails()), "wrap");

      panel.add(new JLabel("Defence"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.DEF_RATING)));
      String def = curRatings.get(TdcRatings.DEF_RATING);
      if (def == null)
        def = "0";
      if (!def.equals("0") && !def.startsWith("-") && !def.startsWith("+")) {
        def = "+" + def;
      }
      panel.add(new JLabel(def));
      panel.add(new JLabel(info.getDefenceDetails()), "wrap");

      panel.add(new JLabel("Range"));
      panel.add(new JLabel(baseRatings.get(TdcRatings.RANGE)));
      panel.add(new JLabel(curRatings.get(TdcRatings.RANGE)));
      panel.add(new JLabel(info.getRangeDetails()), "wrap");

      panel.add(new JLabel("Max LOS"));
      panel.add(new JLabel(""));
      panel.add(new JLabel(String.valueOf(info.getMaxLOS())));
      panel.add(new JLabel(info.getMaxLOSReason()), "wrap");

      panel.add(new JLabel("May Assault?"));
      panel.add(new JLabel(""));
      panel.add(new JLabel(""));
      panel.add(new JLabel(info.getAssaultReason()), "wrap");
    }

  }

}