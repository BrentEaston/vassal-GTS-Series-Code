/*
 * $Id: TdcRatings 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2006-2015 by Brent Easton
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceCloner;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.Properties;
import VASSAL.tools.image.LabelUtils;
import VASSAL.tools.SequenceEncoder;

/**
 * Encode all ratings on a counter. Return them in a HashMap when requested.
 */
public class TdcRatings extends Decorator implements EditablePiece {

  public static final String WHITE = "White";
  public static final String PINK = "Pink";
  public static final String BLUE = "Blue";
  public static final String GREEN = "Green";
  public static final String ORANGE = "Orange";
  public static final String YELLOW = "Yellow";
  public static final String BLACK = "Black";
  public static final String PURPLE = "Purple";
  public static final String BROWN = "Brown";

  protected static final String[] WEAPON_CLASSES = new String[] { WHITE, PINK, PURPLE, BLUE, GREEN,
      ORANGE, YELLOW, BROWN, BLACK };
  protected static final String[] FIRE_RATINGS = new String[] { "No", "0", "1", "2", "3", "4", "5",
      "6", "7", "8", "9" };
  protected static final String[] TQR_RATINGS = new String[] { "0", "1", "2", "3", "4", "5", "6",
      "7", "8", "9" };
  protected static final String[] MOVEMENT_COLOURS = new String[] { "White", "Red", "Black" };
  protected static final String[] MOVE_RATINGS = new String[] { "No", "*", "1", "2", "3", "4", "5",
      "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21",
      "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36",
      "37", "38", "39", "40" };
  protected static final String[] RANGES = new String[] { "*", "1", "2", "3", "4", "5", "6", "7",
      "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23",
      "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38",
      "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53",
      "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68",
      "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83" };
  protected static final String[] DEF_RATINGS = new String[] { "-5", "-4", "-3", "-2", "-1", "0",
      "+1", "+2", "+3", "+4", "+5" };

  public static final String FIRE_RATING = "FireRating";
  public static final String FIRE_COLOUR = "FireColour";
  public static final String ASSAULT_RATING = "AssaultRating";
  public static final String ASSAULT_COLOUR = "AssaultColour";
  public static final String ASSAULT_DOUBLE = "AssaultDouble";
  public static final String MOVE_RATING = "MoveRating";
  public static final String MOVE_COLOUR = "MoveColour";
  public static final String TQR_RATING = "TqrRating";
  public static final String DEF_RATING = "DefRating";
  public static final String ARMOURED = "Armoured";
  public static final String RANGE = "Range";
  public static final String ORGANIC = "Organic";
  public static final String DIFF_STEP = "DiffStep";
  public static final String RED_TQR = "redTqr";
  public static final String FLAK_RATING = "flakRating";

  protected static final Color COLOR_WHITE = Color.white;
  protected static final Color COLOR_PINK = new Color(241, 145, 165);
  protected static final Color COLOR_PURPLE = new Color(153, 120, 182);
  protected static final Color COLOR_BROWN = new Color(145, 62, 18);
  protected static final Color COLOR_GREEN = new Color(34, 102, 55);
  protected static final Color COLOR_YELLOW = new Color(255, 213, 36);
  protected static final Color COLOR_ORANGE = new Color(247, 147, 42);
  protected static final Color COLOR_BLUE = new Color(70, 160, 220);
  protected static final Color COLOR_RED = Color.red;
  protected static final Color COLOR_BLACK = Color.black;

  protected static final Font FONT_LG = new Font("Dialog", Font.BOLD, 16);
  protected static final Font FONT_SML = new Font("Dialog", Font.BOLD, 12);

  public static final String ID = "ratings;";
  protected KeyCommand[] keyCommands;
  protected String fireRating, tqrRating, assaultRating, fireColour, assaultColour, moveColour,
      defRating, range, moveRating, flakRating;
  protected String organicFireRating, organicTqrRating, organicAssaultRating, organicFireColour,
      organicAssaultColour, organicMoveColour, organicDefRating, organicRange, organicMoveRating,
      organicFlakRating;
  protected boolean assaultDouble, armoured, organic, organicAssaultDouble, organicArmoured,
      diffStep, redTqr, organicRedTqr;

  protected HashMap<String, String> ratings, organicRatings;

  public static boolean isUnitType(String type) {
    return TdcProperties.TYPE_UNIT.equals(type) || TdcProperties.TYPE_FLAK.equals(type)
        || TdcProperties.TYPE_ENGINEER.equals(type) || TdcProperties.TYPE_REARGUARD.equals(type);
  }

  public static boolean isArtilleryParkType (String type) {
    return TdcProperties.TYPE_ARTILLERY_PARK.equals(type);
  }

  public static boolean isBeachDefenceType (String type) {
    return TdcProperties.TYPE_BEACH_DEFENCE.equals(type);
  }
  
  
  // Select a fg color for text
  public static Color getFireFg(String c) {
    if (GREEN.equals(c) || BLUE.equals(c) || BLACK.equals(c)) {
      return COLOR_WHITE;
    }
    else {
      return COLOR_BLACK;
    }
  }

  // Convert a Fire Rating colour name to displayable colours
  public static Color getFireBg(String c) {
    if (PINK.equals(c)) {
      return COLOR_PINK;
    }
    else if (PURPLE.equals(c)) {
      return COLOR_PURPLE;
    }
    else if (BROWN.equals(c)) {
      return COLOR_BROWN;
    }
    else if (GREEN.equals(c)) {
      return COLOR_GREEN;
    }
    else if (YELLOW.equals(c)) {
      return COLOR_YELLOW;
    }
    else if (ORANGE.equals(c)) {
      return COLOR_ORANGE;
    }
    else if (BLUE.equals(c)) {
      return COLOR_BLUE;
    }
    else if (BLACK.equals(c)) {
      return COLOR_BLACK;
    }
    else {
      return COLOR_WHITE;
    }
  }

  public static String getWeaponClass(String c) {
    if (PINK.equals(c)) {
      return "Small Arms";
    }
    else if (PURPLE.equals(c)) {
      return "Small Mortar";
    }
    else if (BROWN.equals(c)) {
      return "Dedicated Artillery";
    }
    else if (BLACK.equals(c)) {
      return "Coastal Battery";
    }
    else if (GREEN.equals(c)) {
      return "Mortar";
    }
    else if (YELLOW.equals(c)) {
      return "Direct HE";
    }
    else if (ORANGE.equals(c)) {
      return "Indirect HE";
    }
    else if (BLUE.equals(c)) {
      return "Amour Piercing";
    }
    else if (WHITE.equals(c)) {
      return "Dual Purpose";
    }
    return "Unknown";
  }

  public TdcRatings() {
    this(ID + "", null);
  }

  public TdcRatings(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    fireRating = st.nextToken("0");
    fireColour = st.nextToken("");
    assaultRating = st.nextToken("0");
    assaultColour = st.nextToken("");
    assaultDouble = st.nextBoolean(false);
    range = st.nextToken("1");
    defRating = st.nextToken("0");
    armoured = st.nextBoolean(false);
    tqrRating = st.nextToken("4");
    moveRating = st.nextToken("6");
    moveColour = st.nextToken("White");
    organic = st.nextBoolean(false);
    organicFireRating = st.nextToken("No");
    organicFireColour = st.nextToken(WHITE);
    organicAssaultRating = st.nextToken("No");
    organicAssaultColour = st.nextToken("White");
    organicAssaultDouble = st.nextBoolean(false);
    organicRange = st.nextToken("1");
    organicDefRating = st.nextToken("0");
    organicArmoured = st.nextBoolean(false);
    organicTqrRating = st.nextToken("2");
    organicMoveRating = st.nextToken("18");
    organicMoveColour = st.nextToken(BLACK);
    diffStep = st.nextBoolean(false);
    flakRating = st.nextToken("No");
    redTqr = st.nextBoolean(false);
    organicFlakRating = st.nextToken("No");
    organicRedTqr = st.nextBoolean(false);
    keyCommands = null;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(fireRating).append(fireColour).append(assaultRating).append(assaultColour)
        .append(assaultDouble).append(range).append(defRating).append(armoured).append(tqrRating)
        .append(moveRating).append(moveColour).append(organic).append(organicFireRating)
        .append(organicFireColour).append(organicAssaultRating).append(organicAssaultColour)
        .append(organicAssaultDouble).append(organicRange).append(organicDefRating)
        .append(organicArmoured).append(organicTqrRating).append(organicMoveRating)
        .append(organicMoveColour).append(diffStep).append(flakRating).append(redTqr)
        .append(organicFlakRating).append(organicRedTqr);
    return ID + se.getValue();
  }

  protected KeyCommand[] myGetKeyCommands() {
    if (keyCommands == null) {
      keyCommands = new KeyCommand[0];
    }
    return keyCommands;
  }

  public String myGetState() {
    return "";
  }

  public Object getLocalizedProperty(Object key) {
    if (TdcProperties.RATINGS.equals(key) || TdcProperties.TDC_RANGE.equals(key)
        || TdcProperties.IS_ARTILLERY.equals(key)  || TdcProperties.IS_AIR_DEFENCE.equals(key) ) {
      return getProperty(key);
    }
    return super.getLocalizedProperty(key);
  }

  public Object getProperty(Object key) {
    if (TdcProperties.RATINGS.equals(key)) {
      final GamePiece outer = Decorator.getOutermost(this);
      if (Boolean.TRUE.equals(outer.getProperty(Properties.OBSCURED_TO_ME))
          || Boolean.TRUE.equals(outer.getProperty(Properties.INVISIBLE_TO_ME))) {
        return null;
      }
      buildRatings();
      if (isMounted() || (diffStep && isStepLoss())) {
        return organicRatings;
      }
      else {
        return ratings;
      }
    }
    else if (TdcProperties.TDC_RANGE.equals(key)) {
      final UnitInfo info = new UnitInfo(Decorator.getOutermost(this), true);
      return String.valueOf(info.getEffectiveRange());
    }
    else if (TdcProperties.IS_ARTILLERY.equals(key)) {
      final UnitInfo info = new UnitInfo(Decorator.getOutermost(this), true);
      return String.valueOf(info.isArtillery());
    }
    else if (TdcProperties.IS_AIR_DEFENCE.equals(key)) {
      final String flakRating = ratings == null ? null : ratings.get(FLAK_RATING);
      return String.valueOf(flakRating != null && flakRating.length() > 0 && ! flakRating.equals("No"));
    }
    
    return super.getProperty(key);
  }

  protected boolean isMounted() {
    return "true".equals(Decorator.getOutermost(this).getProperty("Trn_Active"));
  }

  protected boolean isStepLoss() {
    return "true".equals(Decorator.getOutermost(this).getProperty("Step_Active"));
  }

  public void buildRatings() {
    if (ratings == null) {
      ratings = new HashMap<>();
      organicRatings = new HashMap<>();

      ratings.put(ASSAULT_RATING, assaultRating);
      if (!("No".equals(assaultRating))) {
        ratings.put(ASSAULT_COLOUR, assaultColour);
        ratings.put(ASSAULT_DOUBLE, assaultDouble ? "true" : "false");
      }
      ratings.put(FIRE_RATING, fireRating);
      if ("No".equals(fireRating)) {
        ratings.put(RANGE, "0");
      }
      else {
        ratings.put(FIRE_COLOUR, fireColour);
        ratings.put(RANGE, range);
      }
      ratings.put(DEF_RATING, defRating);
      ratings.put(ARMOURED, armoured ? "true" : "false");
      ratings.put(TQR_RATING, tqrRating);
      ratings.put(MOVE_RATING, moveRating);
      if (!("No".equals(moveRating) || "*".equals(moveRating))) {
        ratings.put(MOVE_COLOUR, moveColour);
      }
      ratings.put(ORGANIC, organic ? "true" : "false");
      ratings.put(DIFF_STEP, diffStep ? "true" : "false");
      ratings.put(FLAK_RATING, flakRating);
      ratings.put(RED_TQR, redTqr ? "true" : "false");

      if (organic || diffStep) {

        organicRatings.put(ASSAULT_RATING, organicAssaultRating);
        if (!("No".equals(organicAssaultRating))) {
          organicRatings.put(ASSAULT_COLOUR, organicAssaultColour);
          organicRatings.put(ASSAULT_DOUBLE, organicAssaultDouble ? "true" : "false");
        }
        organicRatings.put(FIRE_RATING, organicFireRating);
        if ("No".equals(organicFireRating)) {
          organicRatings.put(RANGE, "0");
        }
        else {
          organicRatings.put(FIRE_COLOUR, organicFireColour);
          organicRatings.put(RANGE, organicRange);
        }
        organicRatings.put(DEF_RATING, organicDefRating);
        organicRatings.put(ARMOURED, organicArmoured ? "true" : "false");
        organicRatings.put(TQR_RATING, organicTqrRating);
        organicRatings.put(MOVE_RATING, organicMoveRating);
        if (!("No".equals(organicMoveRating) || "*".equals(organicMoveRating))) {
          organicRatings.put(MOVE_COLOUR, organicMoveColour);
        }
        organicRatings.put(ORGANIC, organic ? "true" : "false");
        organicRatings.put(DIFF_STEP, diffStep ? "true" : "false");
        organicRatings.put(FLAK_RATING, organicFlakRating);
        organicRatings.put(RED_TQR, organicRedTqr ? "true" : "false");
      }
    }
  }

  public Command myKeyEvent(KeyStroke stroke) {
    return null;
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
    return "TDC Unit Ratings";
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public static class Ed implements PieceEditor {
    protected StringEnumConfigurer fireRatingInput, tqrRatingInput, assaultRatingInput,
        defRatingInput, fireColourInput, assaultColourInput, moveColourInput, rangeInput,
        moveRatingInput, flakRatingInput;
    protected StringEnumConfigurer organicFireRatingInput, organicTqrRatingInput,
        organicAssaultRatingInput, organicDefRatingInput, organicFireColourInput,
        organicAssaultColourInput, organicMoveColourInput, organicRangeInput,
        organicMoveRatingInput, organicFlakRatingInput;
    protected BooleanConfigurer assaultDoubleInput, armouredInput, organicInput,
        organicArmouredInput, organicAssaultDoubleInput, diffStepInput, redTqrInput,
        organicRedTqrInput;
    protected JPanel controls;

    protected TdcRatings ratings;

    protected JPanel iconPanel;

    protected JLabel front;
    protected JPanel xFront;
    protected JLabel transport;
    protected JPanel xTransport;
    protected String steps;
    protected Embellishment stepImage;

    public Ed(TdcRatings p) {

      final PropertyChangeListener fieldListener = e -> showHideFields();

      ratings = p;
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

      findImages();
      findSteps();

      final Dimension d = new Dimension(75, 75);
      final JPanel iPanel = new JPanel(new MigLayout("", "[]push"));

      iconPanel = new JPanel(new MigLayout());
      iconPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

      iconPanel.add(front);
      iconPanel.add(transport, "wrap");

      xFront = new JPanel() {
        private static final long serialVersionUID = 1L;

        public void paint(Graphics g) {
          drawFront(g);
        }
      };
      xFront.setPreferredSize(d);
      xFront.setMinimumSize(d);
      iconPanel.add(xFront);

      xTransport = new JPanel() {
        private static final long serialVersionUID = 1L;

        public void paint(Graphics g) {
          drawTransport(g);
        }
      };
      xTransport.setPreferredSize(d);
      xTransport.setMinimumSize(d);
      iconPanel.add(xTransport);

      iPanel.add(iconPanel);
      controls.add(iPanel);

      fireRatingInput = new StringEnumConfigurer(null, "Fire Rating:  ", FIRE_RATINGS);
      fireRatingInput.setValue(p.fireRating);
      controls.add(fireRatingInput.getControls());
      fireRatingInput.addPropertyChangeListener(fieldListener);

      fireColourInput = new StringEnumConfigurer(null, "Fire Rating Colour:  ", WEAPON_CLASSES);
      fireColourInput.setValue(p.fireColour);
      controls.add(fireColourInput.getControls());
      fireColourInput.addPropertyChangeListener(fieldListener);

      rangeInput = new StringEnumConfigurer(null, "Range:  ", RANGES);
      rangeInput.setValue(p.range);
      controls.add(rangeInput.getControls());
      rangeInput.addPropertyChangeListener(fieldListener);

      assaultRatingInput = new StringEnumConfigurer(null, "Assault Rating:  ", FIRE_RATINGS);
      assaultRatingInput.setValue(p.assaultRating);
      controls.add(assaultRatingInput.getControls());
      assaultRatingInput.addPropertyChangeListener(fieldListener);

      assaultColourInput = new StringEnumConfigurer(null, "Assault Rating Colour:  ",
          WEAPON_CLASSES);
      assaultColourInput.setValue(p.assaultColour);
      controls.add(assaultColourInput.getControls());
      assaultColourInput.addPropertyChangeListener(fieldListener);

      assaultDoubleInput = new BooleanConfigurer(null, "Use Assault Rating twice (Red text)?",
          p.assaultDouble);
      controls.add(assaultDoubleInput.getControls());
      assaultDoubleInput.addPropertyChangeListener(fieldListener);

      flakRatingInput = new StringEnumConfigurer(null, "Flak Rating:  ", FIRE_RATINGS);
      flakRatingInput.setValue(p.flakRating);
      controls.add(flakRatingInput.getControls());
      flakRatingInput.addPropertyChangeListener(fieldListener);

      defRatingInput = new StringEnumConfigurer(null, "Defence Rating:  ", DEF_RATINGS);
      defRatingInput.setValue(p.defRating);
      controls.add(defRatingInput.getControls());
      defRatingInput.addPropertyChangeListener(fieldListener);

      armouredInput = new BooleanConfigurer(null, "Armoured (Black box)?", p.armoured);
      controls.add(armouredInput.getControls());
      armouredInput.addPropertyChangeListener(fieldListener);

      tqrRatingInput = new StringEnumConfigurer(null, "TQR Rating:  ", TQR_RATINGS);
      tqrRatingInput.setValue(p.tqrRating);
      controls.add(tqrRatingInput.getControls());
      tqrRatingInput.addPropertyChangeListener(fieldListener);

      redTqrInput = new BooleanConfigurer(null, "TQR Rating is red?", p.redTqr);
      controls.add(redTqrInput.getControls());
      redTqrInput.addPropertyChangeListener(fieldListener);

      moveRatingInput = new StringEnumConfigurer(null, "Movement Rating:  ", MOVE_RATINGS);
      moveRatingInput.setValue(p.moveRating);
      controls.add(moveRatingInput.getControls());
      moveRatingInput.addPropertyChangeListener(fieldListener);

      moveColourInput = new StringEnumConfigurer(null, "Movement Rating Colour:  ",
          MOVEMENT_COLOURS);
      moveColourInput.setValue(p.moveColour);
      controls.add(moveColourInput.getControls());
      moveColourInput.addPropertyChangeListener(fieldListener);

      organicInput = new BooleanConfigurer(null, "Has Organic Transport?", p.organic);
      controls.add(organicInput.getControls());
      organicInput.addPropertyChangeListener(fieldListener);

      diffStepInput = new BooleanConfigurer(null, "Step side has non-standard ratings?", p.diffStep);
      controls.add(diffStepInput.getControls());
      diffStepInput.addPropertyChangeListener(fieldListener);

      organicFireRatingInput = new StringEnumConfigurer(null, "Alt Fire Rating:  ", FIRE_RATINGS);
      organicFireRatingInput.setValue(p.organicFireRating);
      controls.add(organicFireRatingInput.getControls());
      organicFireRatingInput.addPropertyChangeListener(fieldListener);

      organicFireColourInput = new StringEnumConfigurer(null, "Alt Fire Rating Colour:  ",
          WEAPON_CLASSES);
      organicFireColourInput.setValue(p.organicFireColour);
      controls.add(organicFireColourInput.getControls());
      organicFireColourInput.addPropertyChangeListener(fieldListener);

      organicRangeInput = new StringEnumConfigurer(null, "Alt Range:  ", RANGES);
      organicRangeInput.setValue(p.organicRange);
      controls.add(organicRangeInput.getControls());
      organicRangeInput.addPropertyChangeListener(fieldListener);

      organicAssaultRatingInput = new StringEnumConfigurer(null, "Alt Assault Rating:  ",
          FIRE_RATINGS);
      organicAssaultRatingInput.setValue(p.organicAssaultRating);
      controls.add(organicAssaultRatingInput.getControls());
      organicAssaultRatingInput.addPropertyChangeListener(fieldListener);

      organicAssaultColourInput = new StringEnumConfigurer(null, "Alt Assault Rating Colour:  ",
          WEAPON_CLASSES);
      organicAssaultColourInput.setValue(p.organicAssaultColour);
      controls.add(organicAssaultColourInput.getControls());
      organicAssaultColourInput.addPropertyChangeListener(fieldListener);

      organicAssaultDoubleInput = new BooleanConfigurer(null,
          "Use Alt Assault Rating twice (Red text)?", p.organicAssaultDouble);
      controls.add(organicAssaultDoubleInput.getControls());
      organicAssaultDoubleInput.addPropertyChangeListener(fieldListener);

      organicFlakRatingInput = new StringEnumConfigurer(null, "Alt Flak Rating:  ", FIRE_RATINGS);
      organicFlakRatingInput.setValue(p.organicFlakRating);
      controls.add(organicFlakRatingInput.getControls());
      organicFlakRatingInput.addPropertyChangeListener(fieldListener);

      organicDefRatingInput = new StringEnumConfigurer(null, "Alt Defence Rating:  ", DEF_RATINGS);
      organicDefRatingInput.setValue(p.organicDefRating);
      controls.add(organicDefRatingInput.getControls());
      organicDefRatingInput.addPropertyChangeListener(fieldListener);

      organicArmouredInput = new BooleanConfigurer(null, "Alt Armoured (Black box)?",
          p.organicArmoured);
      controls.add(organicArmouredInput.getControls());
      organicArmouredInput.addPropertyChangeListener(fieldListener);

      organicTqrRatingInput = new StringEnumConfigurer(null, "Alt TQR Rating:  ", TQR_RATINGS);
      organicTqrRatingInput.setValue(p.organicTqrRating);
      controls.add(organicTqrRatingInput.getControls());
      organicTqrRatingInput.addPropertyChangeListener(fieldListener);

      organicRedTqrInput = new BooleanConfigurer(null, "TQR Rating is red?", p.organicRedTqr);
      controls.add(organicRedTqrInput.getControls());
      organicRedTqrInput.addPropertyChangeListener(fieldListener);

      organicMoveRatingInput = new StringEnumConfigurer(null, "Alt Movement Rating:  ",
          MOVE_RATINGS);
      organicMoveRatingInput.setValue(p.organicMoveRating);
      controls.add(organicMoveRatingInput.getControls());
      organicMoveRatingInput.addPropertyChangeListener(fieldListener);

      organicMoveColourInput = new StringEnumConfigurer(null, "Alt Movement Rating Colour:  ",
          MOVEMENT_COLOURS);
      organicMoveColourInput.setValue(p.organicMoveColour);
      controls.add(organicMoveColourInput.getControls());
      organicMoveColourInput.addPropertyChangeListener(fieldListener);

      showHideFields();

    }

    public void showHideFields() {
      boolean showFire = !("No".equals(fireRatingInput.getValueString()));
      fireColourInput.getControls().setVisible(showFire);
      rangeInput.getControls().setVisible(showFire);

      boolean showAssault = !("No".equals(assaultRatingInput.getValueString()));
      assaultColourInput.getControls().setVisible(showAssault);
      assaultDoubleInput.getControls().setVisible(showAssault);

      boolean showMove = !("No".equals(moveRatingInput.getValueString()) || "*"
          .equals(moveRatingInput.getValueString()));
      moveColourInput.getControls().setVisible(showMove);

      boolean showOrganic = organicInput.getValueString().equals("true");
      boolean showDiff = diffStepInput.getValueString().equals("true");
      boolean showAlt = (showOrganic || showDiff);

      diffStepInput.getControls().setVisible(!showOrganic);
      organicInput.getControls().setVisible(!showDiff);

      boolean showOrganicFire = !("No".equals(organicFireRatingInput.getValueString()));
      boolean showOrganicAssault = !("No".equals(organicAssaultRatingInput.getValueString()));
      boolean showOrganicMove = !("No".equals(organicMoveRatingInput.getValueString()) || "*"
          .equals(organicMoveRatingInput.getValueString()));
      organicFireRatingInput.getControls().setVisible(showAlt);
      organicTqrRatingInput.getControls().setVisible(showAlt);
      organicAssaultRatingInput.getControls().setVisible(showAlt);
      organicDefRatingInput.getControls().setVisible(showAlt);
      organicFireColourInput.getControls().setVisible(showAlt && showOrganicFire);
      organicAssaultColourInput.getControls().setVisible(showAlt && showOrganicAssault);
      organicMoveColourInput.getControls().setVisible(showAlt && showOrganicMove);
      organicRangeInput.getControls().setVisible(showAlt && showOrganicFire);
      organicMoveRatingInput.getControls().setVisible(showAlt);
      organicArmouredInput.getControls().setVisible(showAlt);
      organicAssaultDoubleInput.getControls().setVisible(showAlt && showOrganicAssault);
      organicRedTqrInput.getControls().setVisible(showAlt);
      organicFlakRatingInput.getControls().setVisible(showAlt);
      Window w = SwingUtilities.getWindowAncestor(controls);

      xFront.repaint();
      xTransport.repaint();

      if (w != null) {
        w.pack();
      }
    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');

      se.append(fireRatingInput.getValueString()).append(fireColourInput.getValueString())
          .append(assaultRatingInput.getValueString()).append(assaultColourInput.getValueString())
          .append(assaultDoubleInput.getValueString()).append(rangeInput.getValueString())
          .append(defRatingInput.getValueString()).append(armouredInput.getValueString())
          .append(tqrRatingInput.getValueString()).append(moveRatingInput.getValueString())
          .append(moveColourInput.getValueString()).append(organicInput.getValueString())
          .append(organicFireRatingInput.getValueString())
          .append(organicFireColourInput.getValueString())
          .append(organicAssaultRatingInput.getValueString())
          .append(organicAssaultColourInput.getValueString())
          .append(organicAssaultDoubleInput.getValueString())
          .append(organicRangeInput.getValueString())
          .append(organicDefRatingInput.getValueString())
          .append(organicArmouredInput.getValueString())
          .append(organicTqrRatingInput.getValueString())
          .append(organicMoveRatingInput.getValueString())
          .append(organicMoveColourInput.getValueString()).append(diffStepInput.getValueString())
          .append(flakRatingInput.getValueString()).append(redTqrInput.getValueString())
          .append(organicFlakRatingInput.getValueString())
          .append(organicRedTqrInput.getValueString());
      return ID + se.getValue();
    }

    public String getState() {
      return "";
    }

    protected void findImages() {

      final BasicPiece bp = (BasicPiece) Decorator.getInnermost(ratings);
      final BufferedImage bi = new BufferedImage(75, 75, BufferedImage.TYPE_3BYTE_BGR);
      Graphics g = bi.getGraphics();
      bp.draw(g, 37, 37, null, 1.0);
      front = new JLabel(new ImageIcon(bi));
      final Dimension d = new Dimension(75, 75);
      front.setMinimumSize(d);
      front.setMaximumSize(d);
      front.setPreferredSize(d);

      Embellishment trn = null;
      stepImage = null;

      GamePiece outer = Decorator.getOutermost(ratings);
      while (!(outer instanceof BasicPiece) && (trn == null || stepImage == null)) {
        if (outer instanceof Embellishment) {
          trn = (Embellishment) outer;
          if (!trn.getDescription().contains("Trn")) {
            trn = null;
          }
          stepImage = (Embellishment) outer;
          if (!stepImage.getDescription().contains("Step")) {
            stepImage = null;
          }
        }
        outer = ((Decorator) outer).getInner();
      }

      final BufferedImage bi2 = new BufferedImage(75, 75, BufferedImage.TYPE_3BYTE_BGR);
      g = bi2.getGraphics();
      if (trn != null) {
        final boolean isActive = trn.isActive();
        trn.setActive(true);
        trn.draw(g, 37, 37, null, 1.0);
        trn.setActive(isActive);
      }
      else if (stepImage != null) {
        final boolean isActive = stepImage.isActive();
        stepImage.setActive(true);
        stepImage.draw(g, 37, 37, null, 1.0);
        stepImage.setActive(isActive);
      }
      else {
        g.setColor(Color.white);
        g.fillRect(0, 0, 75, 75);
      }

      transport = new JLabel(new ImageIcon(bi2));
      transport.setMinimumSize(d);
      transport.setMaximumSize(d);
      transport.setPreferredSize(d);
    }

    protected void findSteps() {
      final GamePiece outer = Decorator.getOutermost(ratings);
      final GamePiece expandedPiece = PieceCloner.getInstance().clonePiece(outer);
      steps = (String) expandedPiece.getProperty(TdcProperties.STEP);
    }

    protected void drawFront(Graphics g) {

      Color fg = null, bg = null;
      Rectangle bounds = new Rectangle(0, -75, 75, 75);

      final String fireRating = fireRatingInput.getValueString();
      final String fireColor = fireColourInput.getValueString();
      if ("No".equals(fireRating)) {
        LabelUtils.drawLabel(g, "No", bounds.x, bounds.y + 87, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, COLOR_RED, COLOR_BLACK, COLOR_RED);
      }
      else {
        fg = TdcRatings.getFireFg(fireColor);
        bg = TdcRatings.getFireBg(fireColor);
        LabelUtils.drawLabel(g, fireRating, bounds.x, bounds.y + 87, FONT_LG, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      final String range = rangeInput.getValueString();
      if (range != null && !range.equals("1")) {
        LabelUtils.drawLabel(g, range, bounds.x + 18, bounds.y + 83, FONT_LG, LabelUtils.RIGHT,
            LabelUtils.CENTER, Color.black, null, null);
      }

      final String assaultRating = assaultRatingInput.getValueString();
      final String assaultColor = assaultColourInput.getValueString();
      final String assaultDouble = assaultDoubleInput.getValueString();
      if (assaultRating != null && !assaultRating.equals("No")) {
        fg = TdcRatings.getFireFg(assaultColor);
        bg = TdcRatings.getFireBg(assaultColor);
        if ("true".equals(assaultDouble)) {
          fg = COLOR_RED;
        }
        LabelUtils.drawLabel(g, assaultRating, bounds.x + 20, bounds.y + 100, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      final String flakRating = flakRatingInput.getValueString();
      if (!("No".equals(flakRating))) {
        LabelUtils.drawLabel(g, flakRating, bounds.x + 2, bounds.y + 110, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, COLOR_BLACK, COLOR_BLUE, COLOR_BLACK);
      }

      final String defRating = defRatingInput.getValueString();
      final String armoured = armouredInput.getValueString();
      if (defRating != null) {
        if ("true".equals(armoured)) {
          bg = COLOR_BLACK;
          fg = COLOR_WHITE;
        }
        else {
          fg = COLOR_BLACK;
          bg = COLOR_WHITE;
        }
        LabelUtils.drawLabel(g, defRating, bounds.x, bounds.y + 130, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      final String tqrRating = tqrRatingInput.getValueString();
      final boolean isRed = "true".equals(redTqrInput.getValueString());
      LabelUtils.drawLabel(g, tqrRating, bounds.x + 75, bounds.y + 83, FONT_LG, LabelUtils.LEFT,
          LabelUtils.CENTER, isRed ? COLOR_RED : COLOR_BLACK, null, null);

      final String moveRating = moveRatingInput.getValueString();
      final String moveColour = moveColourInput.getValueString();
      if ("Black".equals(moveColour)) {
        bg = null;
        fg = COLOR_BLACK;
      }
      else if ("Red".equals(moveColour)) {
        bg = null;
        fg = COLOR_RED;
      }
      else if ("White".equals(moveColour)) {
        bg = Color.LIGHT_GRAY;
        fg = COLOR_WHITE;
      }
      LabelUtils.drawLabel(g, moveRating, bounds.x + 75, bounds.y + 103, FONT_LG, LabelUtils.LEFT,
          LabelUtils.CENTER, fg, bg, null);

      final String organic = organicInput.getValueString();
      if ("true".equals(organic)) {
        g.setColor(COLOR_BLACK);
        g.fillRect(bounds.x + 40, bounds.y + 78, 15, 9);
      }
      if ("1".equals(steps)) {
        g.setColor(COLOR_RED);
        g.fillOval(bounds.x + 46, bounds.y + 80, 5, 5);
      }
      else if ("2".equals(steps)) {
        g.setColor(COLOR_RED);
        g.fillOval(bounds.x + 42, bounds.y + 80, 5, 5);
        g.fillOval(bounds.x + 49, bounds.y + 80, 5, 5);
      }
    }

    protected void drawTransport(Graphics g) {

      // Do not draw anything if the counter is a one step non-organic unit

      boolean showOrganic = organicInput.getValueString().equals("true");
      boolean showDiff = diffStepInput.getValueString().equals("true");
      if (!showOrganic && !showDiff && stepImage == null) {
        return;
      }

      Color fg = null, bg = null;
      Rectangle bounds = new Rectangle(0, -75, 75, 75);

      String fireRating;
      String fireColor;
      String range;
      String assaultRating;
      String assaultColor;
      String assaultDouble;
      String defRating;
      String armoured;
      String tqrRating;
      String moveRating;
      String moveColour;
      String organic;
      String flakRating;
      String flakColor;
      String redTqr;

      if (showOrganic || showDiff) {
        fireRating = organicFireRatingInput.getValueString();
        fireColor = organicFireColourInput.getValueString();
        range = organicRangeInput.getValueString();
        assaultRating = organicAssaultRatingInput.getValueString();
        assaultColor = organicAssaultColourInput.getValueString();
        assaultDouble = organicAssaultDoubleInput.getValueString();
        defRating = organicDefRatingInput.getValueString();
        armoured = organicArmouredInput.getValueString();
        tqrRating = organicTqrRatingInput.getValueString();
        moveRating = organicMoveRatingInput.getValueString();
        moveColour = organicMoveColourInput.getValueString();
        flakRating = organicFlakRatingInput.getValueString();
        redTqr = organicRedTqrInput.getValueString();
        organic = organicInput.getValueString();
      }
      else {
        fireRating = fireRatingInput.getValueString();
        try {
          fireRating = String.valueOf(Integer.parseInt(fireRating) - 1);
        }
        catch (Exception ignored) {

        }
        fireColor = fireColourInput.getValueString();
        range = rangeInput.getValueString();
        assaultRating = assaultRatingInput.getValueString();
        try {
          assaultRating = String.valueOf(Integer.parseInt(assaultRating) - 1);
        }
        catch (Exception ignored) {

        }
        assaultColor = assaultColourInput.getValueString();
        assaultDouble = assaultDoubleInput.getValueString();

        flakRating = flakRatingInput.getValueString();
        try {
          flakRating = String.valueOf(Integer.parseInt(flakRating) - 1);
        }
        catch (Exception ignored) {

        }
        flakColor = BLUE;

        defRating = defRatingInput.getValueString();
        armoured = armouredInput.getValueString();
        tqrRating = tqrRatingInput.getValueString();
        try {
          tqrRating = String.valueOf(Integer.parseInt(tqrRating) - 1);
        }
        catch (Exception ignored) {

        }
        redTqr = redTqrInput.getValueString();
        moveRating = moveRatingInput.getValueString();
        moveColour = moveColourInput.getValueString();
        organic = "false";
      }

      if ("No".equals(fireRating)) {
        LabelUtils.drawLabel(g, "No", bounds.x, bounds.y + 87, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, COLOR_RED, COLOR_BLACK, COLOR_RED);
      }
      else {
        fg = TdcRatings.getFireFg(fireColor);
        bg = TdcRatings.getFireBg(fireColor);
        LabelUtils.drawLabel(g, fireRating, bounds.x, bounds.y + 87, FONT_LG, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      if (range != null && !range.equals("1")) {
        LabelUtils.drawLabel(g, range, bounds.x + 18, bounds.y + 83, FONT_LG, LabelUtils.RIGHT,
            LabelUtils.CENTER, Color.black, null, null);
      }

      if (assaultRating != null && !assaultRating.equals("No")) {
        fg = TdcRatings.getFireFg(assaultColor);
        bg = TdcRatings.getFireBg(assaultColor);
        if ("true".equals(assaultDouble)) {
          fg = COLOR_RED;
        }
        LabelUtils.drawLabel(g, assaultRating, bounds.x + 20, bounds.y + 100, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      if (!("No".equals(flakRating))) {
        LabelUtils.drawLabel(g, flakRating, bounds.x + 2, bounds.y + 110, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, COLOR_BLACK, COLOR_BLUE, COLOR_BLACK);
      }

      if (defRating != null) {
        if ("true".equals(armoured)) {
          bg = COLOR_BLACK;
          fg = COLOR_WHITE;
        }
        else {
          fg = COLOR_BLACK;
          bg = COLOR_WHITE;
        }
        LabelUtils.drawLabel(g, defRating, bounds.x, bounds.y + 130, FONT_SML, LabelUtils.RIGHT,
            LabelUtils.CENTER, fg, bg, COLOR_BLACK);
      }

      LabelUtils.drawLabel(g, tqrRating, bounds.x + 75, bounds.y + 83, FONT_LG, LabelUtils.LEFT,
          LabelUtils.CENTER, "true".equals(redTqr) ? COLOR_RED : COLOR_BLACK, null, null);

      if ("Black".equals(moveColour)) {
        bg = null;
        fg = COLOR_BLACK;
      }
      else if ("Red".equals(moveColour)) {
        bg = null;
        fg = COLOR_RED;
      }
      else if ("White".equals(moveColour)) {
        bg = Color.LIGHT_GRAY;
        fg = COLOR_WHITE;
      }
      LabelUtils.drawLabel(g, moveRating, bounds.x + 75, bounds.y + 103, FONT_LG, LabelUtils.LEFT,
          LabelUtils.CENTER, fg, bg, null);

      if ("true".equals(organic)) {
        g.setColor(COLOR_BLACK);
        g.fillRect(bounds.x + 40, bounds.y + 78, 15, 9);
      }
      if ("1".equals(steps)) {
        g.setColor(COLOR_RED);
        g.fillOval(bounds.x + 46, bounds.y + 80, 5, 5);
      }
      else if ("2".equals(steps)) {
        g.setColor(COLOR_RED);
        g.fillOval(bounds.x + 42, bounds.y + 80, 5, 5);
        g.fillOval(bounds.x + 49, bounds.y + 80, 5, 5);
      }
    }
  }

}