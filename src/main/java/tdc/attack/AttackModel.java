/*
 * $Id: AttackModel 7690 2011-07-07 23:44:55Z swampwallaby $
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
import VASSAL.build.module.Map;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceCloner;
import tdc.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttackModel {

  public static final int MODE_DIRECT = 0;
  public static final int MODE_INDIRECT = 1;
  public static final int MODE_OPPORTUNITY = 2;
  public static final String[] MODES = new String[] { "Direct", "Indirect", "Opportunity" };
  public static final String FZ_TO_FZ = "Target moving FZ to FZ";
  public static final String FZ_TO_NON_FZ = "Target moving FZ to Non-FZ";
  public static final String COUNTER_BATTERY_FIRE = "Counter-Battery Fire";
  public static final String AIR_DEFENCE = "Air Defence";
  public static final String AIR_STRIKE = "Air Strike";
  public static final String PARTISAN_ATTACK = "Partisan Attack";
  public static final String BASE_DEFENCE_RATING = "Base Defence Rating";
  public static final int maxDirect = UnitInfo.isNqosRules() ? 4 : 3;

  public static PieceSlot barragePieceSlot = null;
  protected GamePiece source;
  protected UnitInfo sourceInfo;
  protected List<GamePiece> targets;
  protected List<GamePiece> targetPiecesAltArtExcluded;
  protected int targetIndex;
  protected int oldTargetIndex;
  protected GamePiece target;
  protected UnitInfo targetInfo;
  protected List<UnitInfo> targetInfos = new ArrayList<>();
  protected int mode;
  protected int range;

  protected int curRating;
  protected int attackModifierTotal;
  protected int defenceModifierTotal;

  protected ArrayList<FireModifier> attackModifiers;
  protected ArrayList<FireModifier> defenceModifiers;

  protected FireModifier defenceModifier;

  protected AttackView changeListener;
  protected boolean[] modeEnabled;

  private String time;
  private String division;
  protected boolean isNight;
  protected boolean isRubblePossible;
  protected boolean isPhoneLinesCutPossible;
  protected boolean canCounterBatteryFire;
  protected boolean canIndirectFireScatter;
  protected boolean displayRealFireRating;
  protected boolean isAssaultRating;
  protected GamePiece targetArtilleryPark;
  protected int specialAttackType;
  protected int airDefenceRating;
  protected String airDefenceDescription;

  public AttackModel(GamePiece source, ArrayList<GamePiece> targets, int range, GamePiece artilleryPark, int specialAttackType) {
    this.targetArtilleryPark = artilleryPark;
    this.source = source;
    this.targets = targets;
    this.range = range;
    this.specialAttackType = specialAttackType;

    time = (String) GameModule.getGameModule().getProperty(TdcProperties.TIME);
    if (time == null)
      time = "";
    if (TdcProperties.TIME_NIGHT.equals(time)) {
      isNight = true;
    }


    for (GamePiece target : targets) {
      if (specialAttackType == AttackWizard.BEACH_SPECIAL_ATTACK) {
        targetInfos.add(new BeachDefenseUnitInfo(target));
      }
      else
        targetInfos.add(new UnitInfo(target, true));
    }
    FireModifier.initialiseCrossRef();
    if (specialAttackType > AttackWizard.BEACH_SPECIAL_ATTACK) {
      sourceInfo = new BeachAttackUnitInfo(source, specialAttackType, (String) targets.get(0).getProperty(TdcProperties.OVERLAY));
    }
    else {
      sourceInfo = new UnitInfo(source, true);
    }

    division = (String) sourceInfo.getUnit().getProperty(TdcProperties.DIVISION);

    determineMode();
    selectTarget(0);
    determineMode();

  }

  public AttackModel(GamePiece source, ArrayList<GamePiece> targets, int range, GamePiece artilleryPark) {
    this(source, targets, range, artilleryPark, AttackWizard.NO_SPECIAL_ATTACK);
  }

  public AttackModel(GamePiece source, ArrayList<GamePiece> targets, int range) {
    this(source, targets, range, null);
  }

  public void setChangeListener(AttackView listener) {
    changeListener = listener;
  }

  public GamePiece getSource() {
    return source;
  }

  public List<GamePiece> getTargets() {
    return targets;
  }

  public int getTargetCount() {
    return targets.size();
  }

  public GamePiece getTarget() {
    return target;
  }

  public void selectTarget(int index) {
    if (index >= 0 && index < targets.size() && (index != targetIndex || targetInfo == null)) {
      oldTargetIndex = targetIndex;
      targetIndex = index;
      target = targets.get(index);
      targetInfo = targetInfos.get(index);

      if (defenceModifier != null) {
        defenceModifier.setValue(targetInfo.getDefenceRating());
      }

      // It's counter battery fire if
      // a) The source and target units are both Artillery
      // b) The source and target units are on different maps to each other
      // c) Neither the source or target units are on the main map
      canCounterBatteryFire = sourceInfo.isArtillery() && targetInfo.isArtillery() && sourceInfo.getUnit().getMap() != targetInfo.getUnit().getMap() && !TdcMap.isOnMainMap(sourceInfo.getUnit()) && !TdcMap.isOnMainMap(targetInfo.getUnit());
      canIndirectFireScatter = ( "true".equals(source.getProperty("ArtilleryScatterRule")) && ((sourceInfo.isArtillery() && range > 3)|| sourceInfo.isMortar()));

      // Check if 0 result means rubble
      // Hex must be City or Fortified
      // Not be already rubbled
      // Firer must be artillery
      // Range 4+ (else direct fire)
      isRubblePossible = targetInfo.isRubblePossible() && sourceInfo.isArtillery() && range > 3;

      // Check if 0 results on a beach (Overlay) can cut the phone lines
      // only possible between 09h00 6 June and Night 7 June
      final String date = (String) GameModule.getGameModule().getProperty(TdcProperties.DATE);
      final String time = (String) GameModule.getGameModule().getProperty(TdcProperties.TIME);
      //     if (((date == "June 6th") && (GameModule.getGameModule().getProperty(TdcProperties.TIME)!="700")) || (date == "June 7th")) {
      if (("June 6th".equals(date) && !"700".equals(time)) || ("June 7th".equals(date))) {
        String phone_gp_name = (String) target.getProperty(TdcProperties.OVERLAY);
        if (phone_gp_name != null) {
          phone_gp_name = "Phone-" + phone_gp_name;
        }
        final MutableProperty phone_gp = GameModule.getGameModule().getMutableProperty(phone_gp_name);
        isPhoneLinesCutPossible =  (phone_gp != null) && ("1".equals(phone_gp.getPropertyValue()));
      } else {
        isPhoneLinesCutPossible = false;
      }


    }
    change();
  }

  public GamePiece getNewBarragePiece() {
    if (barragePieceSlot==null) {
      for (final PieceSlot pieceSlot : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)) {
        if (Decorator.getInnermost(pieceSlot.getPiece()).getName().equals("Barrage")) {
          barragePieceSlot = pieceSlot;
          break;
        }
      }
    }
  return PieceCloner.getInstance().clonePiece(barragePieceSlot.getPiece());
  }

  public boolean isBarragePieceHeavy(GamePiece theBarragePiece) {
    // returns false if the theBarragePiece is not a barrage
    return false;
  }

  public int getTargetIndex() {
    return targetIndex;
  }

  public int getOldTargetIndex() {
    return oldTargetIndex;
  }

  public String getTargetArmourString() {
    return targetInfo.getArmourString();
  }

  public boolean isTargetArmoured() {
    return targetInfo.isArmoured();
  }

  public boolean isIndirectFire() {
    final String fc = sourceInfo.getFireColor();
    if ( TdcRatings.ORANGE.equals(fc) || TdcRatings.BLACK.equals(fc) || TdcRatings.BROWN.equals(fc) ) {
      if (range > maxDirect) {
        return true;
      }
    }
    if ( TdcRatings.GREEN.equals(fc)) {
      return true;
    }
    return false;
  }

  public boolean isHeavyBarragePossible() {
    int hbLevel = sourceInfo.getBaseFireRating();

    int i = 0;
    while (i<attackModifiers.size()) {
      switch (attackModifiers.get(i).description)
      {
        case "Company Bonus":
        case "Cohesion Hit":
        case "Entrenched":
        case "Barraged":
        case "In Bocage":
          hbLevel = hbLevel + attackModifiers.get(i).value;
          break;
      }
      i++;
    }

    if (hbLevel>5) {
      return true;
    }
    return false;
  }

  public boolean isCompanyBonusOn() {
    int i = 0;
    while (i<attackModifiers.size()) {
      if (attackModifiers.get(i).description=="Company Bonus") {
        if (attackModifiers.get(i).value==2) {
          return true;
        }
      }
      i++;
    }
    return false;
  }

  public boolean isTargetEntrenched() {
    return targetInfo.isEntrenched();
  }

  public boolean canCounterBatteryFire() {
    return canCounterBatteryFire;
  }

  public boolean canIndirectFireScatter() {
    return canIndirectFireScatter;
  }

  public boolean isRubblePossible() {
    return isRubblePossible;
  }

  public boolean isPhoneLinesCutPossible() {
    return isPhoneLinesCutPossible;
  }

  public boolean isSpecialAttack() {
    return specialAttackType > AttackWizard.NO_SPECIAL_ATTACK;
  }

  public boolean isBeachDefenceAttack() {
    return specialAttackType == AttackWizard.BEACH_SPECIAL_ATTACK;
  }

  public boolean isSeaStateAttack() {
    return specialAttackType == AttackWizard.SEA_STATE_SPECIAL_ATTACK;
  }

  public boolean isObstaclesAttack() {
    return specialAttackType == AttackWizard.OBSTACLE_SPECIAL_ATTACK;
  }

  public boolean isIgnoreNegativeModifiers() {
    return sourceInfo.isIgnoreNegativeCombatModifiers();
  }

  public boolean isIgnoreAllModifiers() {
    return sourceInfo.isIgnoreAllCombatModifiers();
  }

  public boolean isAirPower() {
    return sourceInfo.isAirPower();
  }

  public boolean isPartisanAttack() {
    return sourceInfo.isPartisanAttack();
  }

  public static int[] xOffsets = new int[] { 0, 100, 100, 0, -100, -100 };
  public static int[] yOffsets = new int[] { -100, -50, 50, 100, -50, 50 };

  public int getAirDefenceRating() {
    airDefenceRating = 0;
    airDefenceDescription = "";

    // Determine the best Air Defence terrain in the targets current and surrounding hexes    
    if (isAirPower()) {
      final Map targetMap = target.getMap();
      if (targetMap instanceof TdcMap) {
        final TdcMap tdcMap = (TdcMap) targetMap;
        final Point targetPos = target.getPosition();
        checkAirDefenceTerrain(tdcMap.getTerrainName(targetPos));
        for (int i = 0; i < xOffsets.length; i++) {
          final Point testPos = new Point(targetPos.x + xOffsets[i], targetPos.y + yOffsets[i]);
          final String terrain = tdcMap.getTerrainName(testPos);
          checkAirDefenceTerrain(terrain);
        }
      }
    }
    return airDefenceRating;
  }

  protected void checkAirDefenceTerrain(String terrain) {
    int modifier = 0;
    if (TdcProperties.TERRAIN_FORTIFIED.equals(terrain)) {
      modifier = 5;
    }
    else if (TdcProperties.TERRAIN_CITY.equals(terrain)) {
      modifier = 4;
    }
    else if (TdcProperties.TERRAIN_TOWN.equals(terrain)) {
      modifier = 2;
    }
    if (modifier > airDefenceRating) {
      airDefenceRating = modifier;
      airDefenceDescription = terrain;
    }
  }

  public String getAirDefenceDescription() {
    return airDefenceDescription;
  }

  public boolean isAirDefence() {
    return targetInfo != null && targetInfo.isAirPower();
  }

  public String getTargetTerrain() {
    if (isSpecialAttack()) {
      return "Beach";
    }
    else {
      return targetInfo.getTerrainPrintable();
    }
  }

  public String getFireRatingColour() {
    String fc;

    if (isAirDefence()) {
      fc = TdcRatings.BLUE;
    };
    if (isAssaultRating()) {
      fc = sourceInfo.getAssaultColor();
    }
    else {
      fc = sourceInfo.getFireColor();
      //GTS rule for Direct fire of Indirect HE also applies to Brown and Black FR
      //NQOS does not have units with this color, so should work the same there.
      if ( TdcRatings.ORANGE.equals(fc) || TdcRatings.BLACK.equals(fc) || TdcRatings.BROWN.equals(fc) ) {
        if (range <= maxDirect) {
          return TdcRatings.YELLOW;
        }
      }
    }
    return fc;

  }

  public int getFireRating() {
    if (curRating < 0)
      return 0;
    return Math.min(curRating, 9);
  }

  public int getDisplayFireRating() {
    if (isDisplayRealFireRating()) {
      return curRating;
    }
    return getFireRating();
  }

  public boolean isAssaultRating() {
    return isAssaultRating;
  }

  public void setIsAssaultRating(boolean b) {
    isAssaultRating = b;
  }

  public boolean isDisplayRealFireRating() {
    return displayRealFireRating;
  }

  public void setDisplayRealFireRating(boolean b) {
    displayRealFireRating = b;
  }

  public int getBaseFireRating() {
    if (isAssaultRating()) {
      return sourceInfo.getBaseAssaultRating();
    }
    return sourceInfo.getBaseFireRating();
  }

  public int getAttackModifierTotal() {
    return attackModifierTotal;
  }

  public int getDefenceModifierTotal() {
    return defenceModifierTotal;
  }

  public int getRange() {
    return range;
  }

  public UnitInfo getSourceInfo() {
    return sourceInfo;
  }

  public String getSourceLocation() {
    if (isSpecialAttack() && !isBeachDefenceAttack()) {
      return AttackWizard.SPECIAL_ATTACK_NAMES[specialAttackType];
    }
    return (String) source.getProperty(BasicPiece.LOCATION_NAME);
  }

  public String getTargetLocation() {
    if (specialAttackType == AttackWizard.BEACH_SPECIAL_ATTACK) {
      return "Beach Defences";
    }
    return (String) target.getProperty(BasicPiece.LOCATION_NAME);
  }

  public int getMode() {
    return mode;
  }

  public String getModeString() {
    if (mode == MODE_DIRECT) {
      return "DIRECT";
    }
    else if (mode == MODE_INDIRECT) {
      return "INDIRECT";
    }
    return "OPPORTUNITY";
  }

  public String getSmlModeString() {
    if (canCounterBatteryFire) {
      return COUNTER_BATTERY_FIRE;
    }
    else if (isAirDefence()) {
      return AIR_DEFENCE;
    }
    else if (sourceInfo.isAirPower()) {
      return AIR_STRIKE;
    }
    else if (sourceInfo.isPartisanAttack()) {
      return PARTISAN_ATTACK;
    }
    else {
      return MODES[mode] + " Fire";
    }
  }

  public void changeMode(int newMode) {
    mode = newMode;
    update();
  }

  protected void determineMode() {
    modeEnabled = new boolean[3];
    for (int i = 0; i < 3; i++) {
      modeEnabled[i] = true;
    }

    if (isSpecialAttack() || isAirPower() || isAirDefence() || isPartisanAttack()) {
      mode = MODE_DIRECT;
      modeEnabled[MODE_INDIRECT] = false;
      modeEnabled[MODE_OPPORTUNITY] = false;
      return;
    }

    if (sourceInfo.isMortar()) {
      mode = MODE_INDIRECT;
      modeEnabled[MODE_DIRECT] = false;
      modeEnabled[MODE_OPPORTUNITY] = false;
    }
    else if (sourceInfo.isActivated()) {
      if (sourceInfo.isArtillery()) {
        if (range < UnitInfo.getArtilleryDirectRange()) {
          mode = MODE_DIRECT;
          modeEnabled[MODE_INDIRECT] = false;
        }
        else {
          mode = MODE_INDIRECT;
          modeEnabled[MODE_DIRECT] = false;
          modeEnabled[MODE_OPPORTUNITY] = false;
        }
      }
      else {
        mode = MODE_DIRECT;
        modeEnabled[MODE_INDIRECT] = false;
      }
    }
    else {
      if (sourceInfo.isArtillery()) {
        if (range < UnitInfo.getArtilleryDirectRange()) {
          mode = MODE_OPPORTUNITY;
          modeEnabled[MODE_INDIRECT] = false;
        }
        else {
          mode = MODE_INDIRECT;
          modeEnabled[MODE_DIRECT] = false;
          modeEnabled[MODE_OPPORTUNITY] = false;
        }
      }
      else {
        mode = MODE_OPPORTUNITY;
        modeEnabled[MODE_INDIRECT] = false;
      }
    }
  }

  public boolean isModeEnabled(int mode) {
    return modeEnabled[mode];
  }

  public boolean isModeIndirect() {
    return mode == MODE_INDIRECT;
  }


  public boolean isContactLossPossible() {
    // Should return true if the firer is an orange artillery in a Park
    // Or an emplaced artillery
    // But not necessary for naval units
    final String fc = sourceInfo.getFireColor();
    if(sourceInfo.isNaval()) {
      return false;
    }
    if (TdcRatings.ORANGE.equals(fc) || TdcRatings.BLACK.equals(fc)) {
      return true;
    }
    return false;
  }
  protected void update() {
    String r;

    if (isAirDefence()) {
      r = sourceInfo.getBaseRatings().get(TdcRatings.FLAK_RATING);
    };

    if (isAssaultRating()) {
      r = sourceInfo.getBaseRatings().get(TdcRatings.ASSAULT_RATING);
    }
    else {
      r = sourceInfo.getBaseRatings().get(TdcRatings.FIRE_RATING);
    }

    if ("No".equals(r)) {
      curRating = -99;
    }
    else {
      try {
        curRating = Integer.parseInt(r);
      }
      catch (NumberFormatException ignored) {
        curRating = 0;
      }
    }

    getAttackModifiers();
    getDefenceModifiers();

    attackModifierTotal = 0;
    defenceModifierTotal = 0;

    for (FireModifier mod : attackModifiers) {
      if (mod.isUsedInMode(mode)) {
        curRating += mod.getValue();
        attackModifierTotal += mod.getValue();
      }
    }

    for (FireModifier mod : defenceModifiers) {
      if (mod.isUsedInMode(mode) && mod.isOwner(target)) {
        curRating += mod.getValue();
        defenceModifierTotal += mod.getValue();
      }
    }
  }

  public void change() {
    update();
    if (changeListener != null) {
      changeListener.refresh();
    }
  }

  public void change(FireModifier modifier) {
    update();
    if (changeListener != null) {
      changeListener.refresh(modifier);
    }
  }

  public String getAttackText() {
    String s = getSourceText();
    if (s.endsWith("s")) {
      return s + " attack " + getTargetText();
    }
    else {
      return s + " attacks " + getTargetText();
    }
  }

  public String getSourceText() {
    if (isSpecialAttack() && !isBeachDefenceAttack()) {
      return AttackWizard.SPECIAL_ATTACK_NAMES[specialAttackType];
    }

    final StringBuilder s = new StringBuilder(sourceInfo.getArmy() + " unit " + source.getName());
    if (isBeachDefenceAttack()) {
      s.append(" (").append(sourceInfo.getBeachAttackClass()).append(")");
    }
    s.append(" [").append(source.getProperty(BasicPiece.LOCATION_NAME)).append("]");
    return s.toString();
  }

  public String getTargetText() {
    if (specialAttackType == AttackWizard.BEACH_SPECIAL_ATTACK) {
      return "Beach Defences";
    }
    else if (isAirDefence()) {
      return "Allied Fighter Bomber " + " [" + target.getProperty(BasicPiece.LOCATION_NAME) + "]";
    }
    else
      return targetInfo.getArmy() + " unit " + target.getName() + " [" + target.getProperty(BasicPiece.LOCATION_NAME) + "]";
  }

  public List<String> getReportText() {

    final ArrayList<String> text = new ArrayList<>();

    if (isAirDefence()) {
      text.add(sourceInfo.getArmy() + " unit " + source.getName() + " [" + source.getProperty(BasicPiece.LOCATION_NAME) + "] attacks " + targetInfo.getArmy() + " Fighter Bomber [" + target.getProperty(BasicPiece.LOCATION_NAME) + "]");
      text.add("Range: " + range + "/8");
      text.add("Fire Rating " + getFireRating() + " [No modifiers]");
    }
    else if (specialAttackType == AttackWizard.NO_SPECIAL_ATTACK) {
      text.add(sourceInfo.getArmy() + " unit " + source.getName() + " [" + source.getProperty(BasicPiece.LOCATION_NAME) + "] attacks " + targetInfo.getArmy() + " unit " + target.getName() + " ["
        + target.getProperty(BasicPiece.LOCATION_NAME) + "]");
      if (sourceInfo.getEffectiveRange() == -1) {
        text.add("Range: " + range + "/*");
      }
      else {
        text.add("Range " + range + "/" + sourceInfo.getEffectiveRange() + (sourceInfo.getRangeDetails().length() > 0 ? " (" + sourceInfo.getRangeDetails() + ")" : ""));
        if (range > sourceInfo.getEffectiveRange()) {
          text.add("*** OUT OF RANGE ***");
        }
      }
      text.add("Weapon Class - " + sourceInfo.getWeaponClass(getFireRatingColour()) + " (" + getFireRatingColour() + ") v " + getTargetArmourString());
      String s = "Fire Rating " + getFireRating() + " [Base " + getBaseFireRating();
      boolean first = true;
      for (FireModifier f : getAttackModifiers()) {
        if (f.getValue() != 0 && (f.getOwner() == null || f.getOwner() == source)) {
          s += (first ? ":" : ",") + " " + f.getValueString() + " " + f.getDescription();
          first = false;
        }
      }
      for (FireModifier f : getDefenceModifiers()) {
        if (f.getValue() != 0 && (f.getOwner() == null || f.getOwner() == target)) {
          s += (first ? ":" : ",") + " " + f.getValueString() + " " + f.getDescription();
          first = false;
        }
      }
      text.add(s + "]");

      if (isIgnoreNegativeModifiers()) {
        text.add("Ignoring negative defence modifiers");
      }
      if (isIgnoreAllModifiers()) {
        text.add("Ignoring all modifiers");
      }
    }
    else {
      if (specialAttackType == AttackWizard.BEACH_SPECIAL_ATTACK) {

        text.add(sourceInfo.getArmy() + " unit " + source.getName() + " [" + source.getProperty(BasicPiece.LOCATION_NAME) + "] attacks Beach Defences");
        String s = "";
        boolean first = true;
        int mods = 0;
        for (FireModifier f : getAttackModifiers()) {
          if (f.getValue() != 0 && (f.getOwner() == null || f.getOwner() == source)) {
            s += (first ? ":" : ",") + " " + f.getValueString() + " " + f.getDescription();
            mods += f.getValue();
            first = false;
          }
        }
        if (mods == 0) {
          s = "Modifiers: +0";
        }
        else {
          s = "Modifiers" + s;
        }
        text.add(s);
        text.add("Beach Attack Class: " + sourceInfo.getBeachAttackClass());
      }

      else {
        if (specialAttackType == AttackWizard.SEA_STATE_SPECIAL_ATTACK) {
          final String ss = (String) GameModule.getGameModule().getProperty(target.getProperty(TdcProperties.OVERLAY) + "-Seastate");
          text.add("Sea State attack (" + ss + ") against unit " + target.getName() + " [" + target.getProperty(BasicPiece.LOCATION_NAME) + "]");
        }
      }
    }
    return text;
  }

  /*
   * Determine all modifiers that apply to this combat and which combat modes
   * they apply to.
   */
  protected ArrayList<FireModifier> getAttackModifiers() {
    if (attackModifiers == null) {
      attackModifiers = new ArrayList<>();

      if (isSeaStateAttack() || isObstaclesAttack() || isAirDefence()) {
        return attackModifiers;
      }

      if (isBeachDefenceAttack()) {
        if (sourceInfo.getSteps() < 2) {
          addAttackModifier(new FireModifier("Single Step", +1, FireModifier.BASIC, true, false, false, this));
        }
        if (sourceInfo.getCoHits() == 1) {
          addAttackModifier(new FireModifier("Cohesion Hit", +1, FireModifier.BASIC, true, false, false, this));
        }
        if (sourceInfo.getCoHits() == 2) {
          addAttackModifier(new FireModifier("Cohesion Hit x 2", +2, FireModifier.BASIC, true, false, false, this));
        }
        addAttackModifier(new FireModifier("Other", 0, FireModifier.COUNT, true, true, true, this));
        return attackModifiers;
      }

      for (FireModifier mod : sourceInfo.getAttackModifiers()) {
        addAttackModifier(mod);
      }

      if (sourceInfo.getSteps() == 2) {
        addAttackModifier(new FireModifier("Company Bonus", 2, FireModifier.BONUS, true, true, false, this));
      }
      if (canCounterBatteryFire) {
        addAttackModifier(new FireModifier(COUNTER_BATTERY_FIRE, -2, FireModifier.ONOFF, false, true, false, this));
      }
      if (isNight) {
        if (TdcProperties.DIVISION_6AB.equals(division) || TdcProperties.DIVISION_12SS.equals(division)
          || TdcProperties.DIVISION_82AB.equals(division) || TdcProperties.DIVISION_101AB.equals(division)) {
          addAttackModifier(new FireModifier(division + " Night bonus", 3, FireModifier.ONOFF, true, true, true, this));
        }
      }

      addAttackModifier(new FireModifier("Other", 0, FireModifier.COUNT, true, true, true, this));
    }
    return attackModifiers;
  }

  protected ArrayList<FireModifier> getDefenceModifiers() {
    if (defenceModifiers == null) {
      defenceModifiers = new ArrayList<>();

      if (isBeachDefenceAttack() || isSeaStateAttack() | isObstaclesAttack() || isAirDefence()) {
        return defenceModifiers;
      }

      for (int i = 0; i < targets.size(); i++) {
        final UnitInfo info = new UnitInfo(targets.get(i), true);
        UnitInfo terrainInfo;
        if (targetArtilleryPark != null) {
          terrainInfo = new UnitInfo(targetArtilleryPark, true);
        }
        else {
          terrainInfo = info;
        }

        int terrainModifier;
        if (info.isArmoured() && UnitInfo.isTgdRules()) {
          terrainModifier = terrainInfo.getTerrainModifierArmoured();
        }
        else {
          terrainModifier = terrainInfo.getTerrainModifier();
        }

        if (terrainInfo.isWoodsTerrain() && sourceInfo.isFireWoodsReduced()) {
          if (!UnitInfo.isTgdRules() || !info.isArmoured()) {
            terrainModifier = 0;
          }
        }
        if (terrainInfo.isMarshTerrain() && sourceInfo.isFireMarshReduced()) {
          if (!UnitInfo.isTgdRules() || !info.isArmoured()) {
            terrainModifier = 0;
          }
        }

        if ((!info.isInColumn() && terrainModifier != 0) || terrainModifier > 0) {
          addDefenceModifier(new FireModifier(terrainInfo.getTerrainPrintable() + (UnitInfo.isTgdRules() ? " ( " + targetInfos.get(i).getArmourString() + ")" : ""), terrainModifier, targets.get(i)));
        }

        int rangeBonus = 0;
        int sourceRange = sourceInfo.getBaseRange();
        String desc = "Range";
        String armoured = "";
        if (!sourceInfo.isLCS()) { //LCS Units ranged fire has no negative modifiers.
          if (info.isArmoured()) {
            armoured = info.isEntrenched() ? "Entrenched" : "Armoured";
            if (range > 1) {
              if (range >= sourceRange) {
                rangeBonus = -2;
                desc += " max";
              } else {
                rangeBonus = -1;
                desc += " > 1, < max";
              }
            }
          } else {
            armoured = "Unarmoured";
            if (UnitInfo.isNqosRules()) {
              if (range >= 7) {
                rangeBonus = -4;
                desc += " 7+";
              } else if (range >= 5) {
                rangeBonus = -3;
                desc += " 5-6";
              } else if (range >= 4) {
                rangeBonus = -2;
                desc += " 4";
              } else if (range >= 2) {
                rangeBonus = -1;
                desc += " 2-3";
              }
            } else {
              if (range > 1) {
                rangeBonus = -1 * (range - 1);
                desc += " " + range;
              }
            }
          }
        }

        desc += " v " + armoured;
        if (rangeBonus != 0 && mode != MODE_INDIRECT) {
          final FireModifier rangeModifier = new FireModifier(desc, rangeBonus, targets.get(i));
          addDefenceModifier(rangeModifier);
        }

        for (FireModifier mod : info.getDefenceModifiers()) {
          if (mod.getOwner() != null || i == 0) {
            addDefenceModifier(mod);
          }
        }

        final int mass = targetInfos.get(i).getMass();
        int massBonus = 0;
        String massDesc = "";

        if (UnitInfo.isGTS2Rules()) {
          if (mass >= 8) {
            massBonus = 3;
            massDesc = "8+";
          }
          else if (mass >= 6) {
            massBonus = 2;
            massDesc = "6-7";
          }
          else if (mass >= 4) {
            massBonus = 1;
            massDesc = "4-5";
          }
          else if (mass == 0) {
            massBonus = -1;
            massDesc = "0";
          }
        }
        else {
          if (mass >= 9) {
            massBonus = 3;
            massDesc = "9+";
          }
          else if (mass >= 7) {
            massBonus = 2;
            massDesc = "7-8";
          }
          else if (mass >= 5) {
            massBonus = 1;
            massDesc = "5-6";
          }
        }
        if (massBonus != 0) {
          addDefenceModifier(new FireModifier("Mass " + massDesc + " steps", massBonus, FireModifier.BASIC, true, true, true, this, targets.get(i)));
        }
        // Yucko, but need to do this to keep the order the same now that non-adj Ridge option
        // if (i == targets.size()-1) {
        defenceModifier = new FireModifier(BASE_DEFENCE_RATING, targetInfos.get(i).getDefenceRating(), FireModifier.BASIC, true, true, true, this, targets.get(i));
        addDefenceModifier(defenceModifier);

        //}

        if (!isSpecialAttack() && !isAirPower() && !info.isInColumn() && UnitInfo.isTgdRules()) {
          addDefenceModifier(new FireModifier("Target behind Ridge hexside", (targetInfos.get(i).isArmoured() ? -2 : -1), FireModifier.ONOFF, true, false, true, this, targets.get(i)));
        }

      }
      final int fz_to_fz_modifier = UnitInfo.isGTS2Rules() ? 2 : 3;
      addDefenceModifier(new FireModifier(FZ_TO_FZ, fz_to_fz_modifier, FireModifier.ONOFF, false, false, true, this));
      addDefenceModifier(new FireModifier(FZ_TO_NON_FZ, -1, FireModifier.ONOFF, false, false, true, this));
      addDefenceModifier(new FireModifier("Running from Assault", 2, FireModifier.ONOFF, false, false, true, this));

    }
    return defenceModifiers;
  }

  protected void addAttackModifier(FireModifier modifier) {
    if (!isIgnoreAllModifiers() && (!isIgnoreNegativeModifiers() || modifier.getValue() >= 0)) {
      attackModifiers.add(modifier);
    }
  }

  protected void addDefenceModifier(FireModifier modifier) {
    if (!isIgnoreAllModifiers() && (!isIgnoreNegativeModifiers() || modifier.getValue() >= 0)) {
      defenceModifiers.add(modifier);
    }
  }

  // Return info for a Beach Defence Attack

  public static class BeachDefenseUnitInfo extends UnitInfo {

    public BeachDefenseUnitInfo(GamePiece unit) {
      super(unit, true);
      terrain = TdcProperties.TERRAIN_BEACH;
    }

  }

  public static class BeachAttackUnitInfo extends UnitInfo {

    protected int specialAttackType;
    protected String overlay;
    protected String fireColor;
    protected int firePower;

    public BeachAttackUnitInfo(GamePiece unit, int specialAttackType, String overlay) {
      super(unit, true);
      terrain = TdcProperties.TERRAIN_BEACH;
      this.specialAttackType = specialAttackType;
      this.overlay = overlay;

      fireColor = AttackWizard.SPECIAL_ATTACK_FIRE_COLORS[specialAttackType];

      final String gpname = overlay + "-" + AttackWizard.SPECIAL_ATTACK_GP_NAMES[specialAttackType];
      final String f = (String) GameModule.getGameModule().getProperty(gpname);

      try {
        firePower = Integer.parseInt(f);
      }
      catch (Exception e) {
        firePower = 0;
      }

      baseRatings = new HashMap<>();
      baseRatings.put(TdcRatings.FIRE_RATING, f);
    }

    public String getFireColor() {
      return fireColor;
    }

    public int getBaseFireRating() {
      return firePower;
    }

    public int getBaseRange() {
      return 0;
    }

  }
}