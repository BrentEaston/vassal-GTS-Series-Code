/*
 *
 * Copyright (c) 2005-2020 by Brent Easton
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
 *  5-Mar-20 BE Add AMD Ruleset
 */

package tdc;

/**
 * Define the Properties/Markers used in TDC
 * @author Brent Easton
 *
 */
public interface TdcProperties {

  boolean DEBUG = false;            // Debug Mode - Mouse over viewer shows ratings

  String PREF_TAB = "Grand Tactical Series";

  String STEP = "Step";            // Step Count Marker
  String ACTIVE = "Active";        // Unit can be Activated Marker
  String
  	IS_INDEPENDENT = "isIndependent";                  // Units belongs to Indepndent Formation Marker
  String BLACK_STRIPE_IND = "BlackStripeInd"; // Black Stripe Independent
  String
    ALWAYS_IN_COMMAND = "AlwaysInCommand";             // Unit is always in command
  String
    NON_FORMATION_COMMAND = "NonFormationCommand";     // Unit can be in command to leaders of other formations in division
  String
    HAS_TELEPHONE = "HasTelephone";                    // Leader with access to telephone
  String LAYER_TELEPHONE = "Telephone";

  String RANGE = "Range";          // Command or Fire Range Marker
  String RANGE_TYPE = "RangeType"; // Range Type Marker
  String ARTILLERY = "Artillery";  // Artillery Range Type
  String MORTAR = "Mortar";        // Mortar Range Type

  String ARMY = "Army";           // Owning Army Marker

  String FORMATION = "Formation"; // Owning Formation Marker
  String FORMATION2 = "Formation2"; // Secondary command Formation Marker
  String BASE_FORMATION = "MyFormation"; // Base Formation Marker
  String DIVISION = "Division";   // Owning Division Marker
  String DIVISION2 = "Division2";   // Secondary command Division Marker
  String DIVISION_INDEPENDENT = "ind"; // D-Day Independents with no onwing Division

  String TYPE = "Type";         // Unit Type Marker
  String BRIDGE = "Bridge";     // Bridge Type
  String CHIT = "Chit";         // Chit Type

  String CLASS = "Class";       // Unit Class Marker
  String LEADER = "Leader";     // Leader Class
  String INFANTRY = "Infantry"; // Infantry Class
  String VEHICLE = "Vehicle";   // Vehicle Class
  String GUN = "Gun";           // Gun Class

  String SUBCLASS = "Subclass";       // Unit Class Marker
  String ENGINEER = "Engineer"; // Engineer Type

  String MOVE = "Move";   // Movement type Marker
  String LEG = "Leg";     // Leg Movement
  String TRACK = "Track"; // Track Movement
  String WHEEL = "Wheel"; // Wheel Movement

  String ORGANIC = "Organic";

  String STRONGPOINT = "Strongpoint";
  String OP = "OP";
  String HILL = "Hill";
  String CITY = "City";
  String TOWN = "Town";
  String FORTIFIED = "Fortified";
  String IN_GERMANY = "InGermany";
  String RHINE = "Rhine";
  String RAISED_ROAD = "Raised Road";
  String RAISED_RAILROAD = "Raised Railroad";
  String EMBANKMENT_ROAD = "Embankment Road";
  String EMBANKMENT_RAILROAD = "Embankment Railroad";
  String SUNKEN_ROAD = "Sunken Road";
  String SUNKEN_RAILROAD = "Sunken Railroad";
  String SECTOR = "Sector";
  String OVERLAY = "Overlay";
  String TERRAIN_CREST = "Crest";
  String TERRAIN_RIDGE = "Ridge";
  String TERRAIN_STREAM = "Stream";
  String TERRAIN_RIVER = "River";
  String TERRAIN_CREST_STREAM = "CrestAndStream";
  String TERRAIN_CREST_RIVER = "CrestAndRiver";
  String TERRAIN_RIDGE_STREAM = "RidgeAndStream";
  String TERRAIN_RIDGE_RIVER = "RidgeAndRiver";

  String ARMY_GERMAN = "German";
  String ARMY_ALLIED = "Allied";

  String DIVISION_1AB = "1AB";
  String DIVISION_82AB = "82AB";
  String DIVISION_101AB = "101AB";
  String DIVISION_52AL = "52nd";
  String DIVISION_43RD = "43rd";
  String DIVISION_GDS = "Gds";
  String DIVISION_HOH = "9ss";
  String DIVISION_FR = "Frundsberg";
  String DIVISION_VT = "von Tettau";
  String DIVISION_59 = "59th";
  String DIVISION_WALTHER = "Walther";
  String DIVISION_EINDHOVEN = "Eindhoven";
  String DIVISION_6AB = "6ab";
  String DIVISION_7TH = "7th";
  String DIVISION_3RD = "3rd";
  String DIVISION_3CAN = "3can";
  String DIVISION_50TH = "50th";
  String DIVISION_51ST = "51st";
  String DIVISION_12SS = "12ss";
  String DIVISION_716 = "716";
  String DIVISION_346 = "346";
  String DIVISION_352 = "352";
  String DIVISION_352U = "352u";   // 352 in Utah gameset
  String DIVISION_CDO = "cdo";
  String DIVISION_NAVAL = "Naval";
  String DIVISION_709 = "709";
  String BOARD_UTAH_BEACH1 = "Utah-ua";
  String BOARD_UTAH_BEACH2 = "Utah-up";

  String FORMATION_POLISH = "Polish Para Brigade";
  String FORMATION_HARDER = "KG Harder";
  String FORMATION_SPINDLER = "KG Spindler";
  String FORMATION_KRAFFT = "KG Krafft";
  String FORMATION_VONALLWOERDEN = "KG von Allwoerden";
  String FORMATION_HENKE = "KG Henke";
  String FORMATION_KNAUST = "KG Knaust";
  String FORMATION_REINHOLD = "KG Reinhold";
  String FORMATION_EULING = "KG Euling";
  String FORMATION_FRUNDSBERG = "KG Frundsberg";
  String FORMATION_716_736 = "716-736";

  String FORMATION_KEIL = "709-keil";

  String RULESET = "Ruleset";
  String RULES_TDC = "TDC";
  String RULES_WED = "WED";
  String RULES_NQOS = "NQOS";
  String RULES_TGD = "TGD";
  String RULES_DDAY = "DDAY"; // For compatibility with early versions of module
  String RULES_TINIAN = "TINIAN";
  String RULES_CRETE = "CRETE";
  String RULES_AMD = "AMD";
  
  String SUB_RULESET = "SubRuleset";
  String RULES_GJS = "1";
  String RULES_UTAH = "2";
  String RULES_OMAHA = "3";
  String RULES_COMBINED = "4";
  
  String SDZ_1AB = "SDZ-1AB";
  String SDZ_52AL = "SDZ-52AL";
  String SDZ_82AB = "SDZ-82AB";
  String SDZ_101AB = "SDZ-101AB";
  String SDZ_POLISH = "SDZ-PB";
  String CLUB_ROUTE = "Club-Route";
  String ARNHEM_BRIDGE = "Arnhem-Bridge";
  String COMMAND_BREAKDOWN = "Command-Breakdown";
  String EINDHOVEN_CHILL = "Eindhoven-Chill";
  String EINDHOVEN_IND = "1";
  String CHILL_IND = "2";

  // NQOS
  String FRENCH_SUPPLY = "French-Supply";
  String DIMISHED_SUPPLY = "Diminished Supply";
  String EMERGENCY_SUPPLY = "Emergency Supply";
  String BIR_HACHEIM = "Bir Hacheim";
  String MINEFIELD_DOOR = "Minefield Door";
  String ITALIAN_ARTY_LEADER = "Italian-Arty-Leader";
  String ITALIAN_ARTY_LEADER_DEAD = "2";
  String INDIRECT = "Indirect";
  String DIVISION_ARIETE = "Ar";
  String DIVISION_TRIESTE = "Tr";
  String DIVISION_FRENCH = "Fr";
  String DIVISION_AFRIKA_KORPS = "Ge";
  String BOARD_BIR_HACHEIM = "Bir Hacheim";
  String ARTILLERY_PARK = "Artillery Park";

  // D-DAY
  String TERRAIN_CLEAR = "Clear";
  String TERRAIN_POLDER = "Polder";
  String TERRAIN_OOIJ_POLDER = "Ooij Polder";
  String TERRAIN_ORCHARD = "Orchard";
  String TERRAIN_WOODS = "Woods";
  String TERRAIN_BRUSHWOODS = "Brushwoods";
  String TERRAIN_SWAMP = "Swamp";
  String TERRAIN_SAND_DUNES = "Sand Dunes";
  String TERRAIN_VILLAGE = "Village";
  String TERRAIN_TOWN = "Town";
  String TERRAIN_CITY = "City";
  String TERRAIN_FORTIFIED = "Fortified";
  String TERRAIN_DESERT = "Desert";
  String TERRAIN_BEACH = "Beach";
  String TERRAIN_BOCAGE = "Bocage";
  String TERRAIN_HEDGEROW = "Hedgerow"; // Display name for Bocage
  String TERRAIN_MARSH = "Marsh";
  String TERRAIN_HILL = "Hill";
  String TERRAIN_FLOODED = "Flooded";
  String KEIL_TELEPHONE_COUNT = "KeilTelephoneCount"; // How many units with active telephones is Keil leader currently stacked with
  // TINIAN
  String TERRAIN_RUNWAY = "Runway";
  String TERRAIN_SUGAR_CANE = "Sugar Cane";
  String TERRAIN_BRUSH = "Brush";
  String TERRAIN_CAVE = "Cave";
  String TERRAIN_ROCKY = "Rocky";
  String TERRAIN_BUILDINGS = "Buildings";
  String TERRAIN_SLOPE = "Slope";
  String TERRAIN_STEEP_SLOPE = "Steep Slope";
  String TERRAIN_STACKING = "Stacking";
  String TERRAIN_TRAIL = "Trail";
  String TERRAIN_POI = "POI";
  String TERRAIN_OVERRIDE = "Terrain";   // Counter overriding hex terrain
  String TERRAIN_BLOCKING = "Blocking";  // Counter overriding terrain blocking
  String SUBCLASS_NAVAL_GUN = "NavalGun";
  String DIVISION_2MARINES = "2mar";
  String DIVISION_4MARINES = "4mar";
  String DIVISION_IMP_ARMY = "jp-army";
  String DIVISION_IMP_NAVY = "jp-navy";

  String RATINGS = "Ratings";
  String RATING_INDIRECT = "Rating-Indirect";

  String ADJUST = "Adjust";
  String TYPE_UNIT = "Unit";
  String TYPE_FLAK = "Flak";
  String TYPE_ENGINEER = "Engineer";
  String TYPE_REARGUARD = "Rearguard";
  String TYPE_IP = "IP";
  String TYPE_ENTRENCH = "Entrench";
  String TYPE_COHESION_HIT = "CoHit";
  String TYPE_SUPPRESSED = "Suppressed";
  String TYPE_STEP_LOSS = "Step Loss";
  String TYPE_BARRAGE = "Barrage";
  String TYPE_RUBBLE = "Rubble";
  String TYPE_DELAY_P = "Delay-P";
  String TYPE_DELAY_G = "Delay-G";
  String TYPE_DELAY_A = "Delay-A";
  String TYPE_COLUMN = "Column";
  String TYPE_EVENT = "Event";
  String TYPE_ARTILLERY_PARK = "APark";
  String TYPE_BOCAGE = "Bocage";
  String TYPE_HERO = "Hero";
  String TYPE_ALONE = "Alone";
  String TYPE_ARTILLERY_MARKER = "ArtilleryMarker";
  String TYPE_BEACH_DEFENCE = "BeachDefence";
  String TYPE_AIR = "Air";
  String TYPE_CAVE = "Cave";

  String CLASS_HEROIC = "Heroic";
  String CLASS_LANDING_CRAFT = "LandingCraft";
  String CLASS_LCS = "LCS";
  String CLASS_AIRPOWER = "Airpower";

  String DATE = "Date";
  String TIME = "Time";
  String WEATHER = "Weather";

  String TIME_NIGHT = "Night";
  String WEATHER_CLEAR = "Clear";
  String WEATHER_FOG = "Fog";
  String WEATHER_OVERCAST = "Overcast";
  String WEATHER_RAIN = "Rain";
  String WEATHER_MIST = "Mist";
  String WEATHER_SANDSTORM = "Sand Storm";
  String WEATHER_STORM = "Storm";

  String TDC_RANGE = "TdcRange";
  String IS_ARTILLERY = "isArtillery";
  String IS_AIR_DEFENCE = "isAirDefence";
  String HOVER_TEXT = "hoverText";
  String ACTIVATION_COUNT = "activationCount";
  String ACTIVATION_LIMIT = "activationLimit";

  int COMMAND_COMMAND_RANGE = 2;
  String TGD_ARTY_PARK = "Arty Park"; // Name of the Zone containing the Artillery Parks on a division card
  String ARTILLERY_PARK_MAP = "AParkMap"; // Property containing the name of the Division Card map for a unit
  String ARTILLERY_PARK_ID = "Park"; // Property containing the Artillery Park Id
  String GERMAN_BEACH_DEFENCE = "German Beach Defence"; // Name of the zone containing the Beach Defence Track
  String DD = "DD"; // True for DD units
  String BEACH_LANDING = "BeachLanding"; // True for units that may land on beach
  String DEFENCE_OVERLAY = "DefenceOverlay"; // Zone Property of Beach Defence Track containing overlay name
  String NAVAL_BOX = "NavalBox";   // Name of zone property on Naval cards indicating location of token
  String NAVAL_PARK = "NavalPark"; // Name of zone property on Naval cards indicating which Assault Force
  String NAVAL_LONG_RANGE = "Long";
  String NAVAL_MEDIUM_RANGE = "Medium";
  String NAVAL_SHORT_RANGE = "Short";
  String NAVAL_SAIPAN_RANGE = "Saipan";
  String NAVAL_EN_ROUTE = "En Route";
  String NAVAL_LOADING = "Loading";
  String NAVAL_LANDING = "Landing";
  String COMBAT_NO_NEG = "CombatNoNeg"; // true if a unit attracts no negative modifiers on combat
  String COMBAT_NO_MOD = "CombatNoMod"; // true if a unit attracts no modifiers at all on combat
  String ADD_10_TO_SWORD_NAVAL_RANGE = "Add10ToSwordNavalRange"; // Global value to add to all Naval ranges on map
  String NAVAL_PERNELLE_RANGE = "Pernelle";
  String NAVAL_MAISY_RANGE = "Maisy";
  
  // Beach Attack Classes (BAC)
  String BAC_79TH = "79th Armoured";
  String BAC_ENGINEER = "Engineer";
  String BAC_TANK = "Tank";
  String BAC_LANDING_CRAFT = "Landing Craft";
  String BAC_OTHER = "Other";
  String MARKER_79TH = "79th"; // True if unit belong to 79th Armoured division

  String ZONE_LANDING_WAVE = "Landing Wave";

  // Global properties belonging to Maps
  String MAP_DIVISION = "Map-Division";

  // Crete
  String TERRAIN_SCRUB = "Scrub";

  String SUPPLY_FULL = "Full";
  String SUPPLY_REDUCED = "Reduced";
  String SUPPLY_DIMINISHED = "Diminished";
  String SUPPLY_EMERGENCY = "Emergency";

  String SUPPLY_MALEME_SUDA = "Maleme-Suda";
  String SUPPLY_RETHYMON = "Rethymon";
  String SUPPLY_HERAKLION = "Heraklion";

  String BOARD_KASTELLI = "Kastelli";
  String BOARD_MALEME = "Maleme";
  String BOARD_CANEA_SUDA = "Canea Suda";
  String BOARD_GEORGEOPOLIS = "Georgeopolis";
  String BOARD_RETHYMON = "Rethymon";
  String BOARD_HERAKLION = "Heraklion";

  String CLASS_PARTISAN = "Partisan";
  String SUBCLASS_GREEK = "Greek";
  String SUBCLASS_KGR_LEADER = "KgrLeader";


}
