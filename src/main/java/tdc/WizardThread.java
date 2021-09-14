/*
 * $Id: WizardThread.java 3910 2008-07-28 18:03:22Z uckelman $
 *
 * Copyright (c) 2000-2012 by Rodney Kinney, Brent Easton
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.ColorConfigurer;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.PlayerIdFormattedStringConfigurer;
import VASSAL.configure.StringEnum;
import VASSAL.configure.VisibilityCondition;
import VASSAL.counters.GamePiece;
import VASSAL.i18n.Resources;
import VASSAL.i18n.TranslatableConfigurerFactory;
import VASSAL.tools.FormattedString;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.UniqueIdManager;
import VASSAL.tools.imageop.ScaledImagePainter;
  import terrain.HexRef;
import terrain.TerrainHexGrid;

/*
 * Modified LOS Thread to act as an Attack Selector.
 */

/**
 * A class that allows the user to draw a straight line on a Map (LOS = Line Of
 * Sight). No automatic detection of obstacles is performed; the user must
 * simply observe the thread against the image of the map. However, if the user
 * clicks on a board with a {@link Map Grid}, the thread may snap to the grid
 * and report the distance between endpoints of the line
 * */
public class WizardThread extends AbstractConfigurable implements MouseListener, MouseMotionListener, Drawable, Configurable, UniqueIdManager.Identifyable,
  CommandEncoder, KeyListener {

  public static final String LOS_THREAD_COMMAND = "LOS\t";

  public static final String NAME = "threadName";
  public static final String SNAP_LOS = "snapLOS";
  public static final String SNAP_START = "snapStart";
  public static final String SNAP_END = "snapEnd";
  public static final String REPORT = "report";
  public static final String PERSISTENCE = "persistence";
  public static final String PERSISTENT_ICON_NAME = "persistentIconName";
  public static final String GLOBAL = "global";
  public static final String LOS_COLOR = "threadColor";
  public static final String HOTKEY = "hotkey";
  public static final String TOOLTIP = "tooltip";
  public static final String ICON_NAME = "iconName";
  public static final String LABEL = "label";
  public static final String DRAW_RANGE = "drawRange";
  public static final String HIDE_COUNTERS = "hideCounters";
  public static final String HIDE_OPACITY = "hideOpacity";
  public static final String RANGE_BACKGROUND = "rangeBg";
  public static final String RANGE_FOREGROUND = "rangeFg";
  public static final String RANGE_SCALE = "scale";
  public static final String RANGE_ROUNDING = "round";
  public static final String ROUND_UP = "Up";
  public static final String ROUND_DOWN = "Down";
  public static final String ROUND_OFF = "Nearest whole number";
  public static Font RANGE_FONT = new Font(Font.DIALOG, Font.PLAIN, 11);
  public static final String DEFAULT_ICON = "/images/thread.gif";

  public static final String FROM_LOCATION = "FromLocation";
  public static final String TO_LOCATION = "ToLocation";
  public static final String CHECK_COUNT = "NumberOfLocationsChecked";
  public static final String CHECK_LIST = "AllLocationsChecked";
  public static final String RANGE = "Range";

  public static final String NEVER = "Never";
  public static final String ALWAYS = "Always";
  public static final String CTRL_CLICK = "Ctrl-Click & Drag";
  public static final String WHEN_PERSISTENT = "When Persisting";

  public static final String TARGET_IMAGE = "target.png";

  protected static UniqueIdManager idMgr = new UniqueIdManager("WizardThread");

  protected boolean retainAfterRelease = false;
  protected long lastRelease = 0;

  protected Map map;
  protected KeyStroke hotkey;
  protected Point anchor;
  protected Point arrow;
  protected boolean visible;
  protected boolean drawRange;
  protected int rangeScale;
  protected double rangeRounding = 0.5;
  protected boolean hideCounters;
  protected int hideOpacity = 0;
  protected boolean isArtilleryPark = false;
  protected boolean prevHideThread = false;
  protected String fireColor;
  protected String fixedColor;
  protected Color threadColor = Color.black, rangeFg = Color.white, rangeBg = Color.black;
  protected boolean snapStart;
  protected boolean snapEnd;
  protected Point lastAnchor = new Point();
  protected Point lastArrow = new Point();
  protected Point lastArrowSnap = new Point();
  protected Rectangle lastRangeRect = new Rectangle();
  protected String anchorLocation = "";
  protected String lastLocation = "";
  protected String lastRange = "";
  protected FormattedString reportFormat = new FormattedString("$playerId$ Checked LOS from $" + FROM_LOCATION + "$ to $" + CHECK_LIST + "$");
  protected List<String> checkList = new ArrayList<>();
  protected String persistence = CTRL_CLICK;
  protected String persistentIconName;
  protected String global = ALWAYS;
  protected String threadId = "";
  protected boolean persisting = false;
  protected boolean mirroring = false;
  protected String iconName;
  protected boolean ctrlWhenClick = false;
  protected boolean initializing;
  protected boolean firstTime;
  protected boolean active;
  protected boolean alwaysInRange = false;

  protected GamePiece source;
  protected Wizard myWizard;
  protected ScaledImagePainter imagePainter = new ScaledImagePainter();
  protected int sourceRange;
  protected static final Color IN_COLOR = Color.black;
  protected static final Color OUT_COLOR = Color.red;

  protected int addRange;
  protected GamePiece indirectSource = null;
  protected Point indirectAnchor;

  protected String currentNavalPark; // Id Of Naval Park (G/S/J/C) for Naval box
  // arrow is currently over, null otherwise
  protected String currentNavalBox; // Name of naval box arrow is currently over
  protected GamePiece currentAssaultForceMarker; // Assault Force marker
  // matching curentNavalpark;
  protected HashSet<BlockingTerrain> blockingTerrain = new HashSet<>();

  protected ColorConfigurer ridgeColorConfig;
  protected ColorConfigurer crestColorConfig;
  protected ColorConfigurer blockColorConfig;
  protected ColorConfigurer possibleColorConfig;
  protected BooleanConfigurer blocksStartShowingConfig;

  protected static final String RIDGE_COLOR = "ridgeColor";
  protected static final String CREST_COLOR = "crestColor";
  protected static final String BLOCK_COLOR = "blockColor";
  protected static final String POSSIBLE_COLOR = "possibleColor";
  protected static final String BLOCKS_START_SHOWING = "blocksStartShowing";

  protected boolean ctrlKeyDepressed = false;

  protected double os_scale;

  public WizardThread(Wizard w) {
    myWizard = w;

    anchor = new Point(0, 0);
    arrow = new Point(0, 0);
    visible = false;
    persisting = false;
    mirroring = false;

    //
    drawRange = true;
    rangeFg = Color.white;
    rangeBg = Color.black;
    setConfigureName("WizardThread");
    rangeScale = 0;
    rangeRounding = 0.5;
    hideCounters = false;
    threadColor = Color.black;
    snapStart = true;
    snapEnd = true;
    persistence = NEVER;
    global = NEVER;
    imagePainter.setImageName(TARGET_IMAGE);
  }

  // Launch a thread that the measures the range from an alternate piece
  public void launchIndirectNavalAttack(GamePiece p, int range, GamePiece assaultForceMarker, int addRange) {
    indirectSource = assaultForceMarker;
    indirectAnchor = assaultForceMarker.getPosition();
    launch(p, range, false, addRange);
  }

  public void launch(GamePiece p, int range, boolean alwaysInRange) {
    launch(p, range, alwaysInRange, 0);
  }

  public void launch(GamePiece p, int range, boolean alwaysInRange, int addRange) {
    if (active || visible)
      return;
    this.addRange = addRange;
    final UnitInfo sourceInfo = new UnitInfo(p, true);
    this.fireColor = sourceInfo.getFireColor();
    this.isArtilleryPark = TdcRatings.isArtilleryParkType((String) p.getProperty(TdcProperties.TYPE));
    active = true;
    source = p;
    sourceRange = range;
    this.alwaysInRange = alwaysInRange;
    launch();
    firstTime = true;

    anchor = source.getPosition();

    final Point loc = MouseInfo.getPointerInfo().getLocation();
    SwingUtilities.convertPointFromScreen(loc, p.getMap().getView());
    arrow = map.snapTo(map.componentToMap(loc));

    lastAnchor = new Point(anchor);
    lastArrow = new Point(arrow);
    lastArrowSnap = new Point(arrow);
    initLOSBlockingTerrain();
    findLOSBlockingTerrain();

  }

  public void setRange(int r) {
    sourceRange = r;
  }


  /**
   * @return whether the threadis for an indirect fire
   */
  public boolean isIndirectThread() {
    boolean hideThread = false;

    // if from Contact marker  Always hide
    if (isArtilleryPark) {
      if (calculateRange() > 3) {
        hideThread = true;
      }
    }

    // if from Naval unit Always hide
    if (indirectSource != null) {
      hideThread = true;
    }
    // If indirect fire color (orange, brown , black ) and range > 3 Hide
    if (TdcRatings.ORANGE.equals(fireColor) || TdcRatings.BLACK.equals(fireColor) || TdcRatings.BROWN.equals(fireColor) ) {
      if (calculateRange() > 3) {
        hideThread = true;
      }
    }
    // If mortar fire color Hide
    if (TdcRatings.GREEN.equals(fireColor)) {
      hideThread = true;
    }

    if (hideThread == true) {
      if (prevHideThread == false) {
        // Clear any blocking terrain on the map
        blockingTerrain.clear();
      }
      prevHideThread = true;
      return true;
    }
    prevHideThread = false;
    return false;
  }


  /**
   * @return whether the thread should be drawn
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * If true, draw the thread on the map
   */
  public void setVisible(boolean state) {
    visible = state;
    active = state;
    map.repaint();
  }

  public void setActive(boolean b) {
    active = b;
  }

  /**
   * Expects to be added to a {@link Map}. Adds a button to the map window's
   * toolbar. Pushing the button pushes a MouseListener onto the Map that draws
   * the thread. Adds some entries to preferences
   *
   * @see Map#pushMouseListener
   */
  public void addTo(Buildable b) {
    idMgr.add(this);
    map = (Map) b;
    map.getView().addMouseMotionListener(this);
    map.getView().addKeyListener(this);
    map.addDrawComponent(this);
    GameModule.getGameModule().addCommandEncoder(this);

    ridgeColorConfig = new ColorConfigurer(RIDGE_COLOR, "Blocking Ridge Color", Color.ORANGE);
    GameModule.getGameModule().getPrefs().addOption(AttackWizard.PREF_TAB, ridgeColorConfig);

    crestColorConfig = new ColorConfigurer(CREST_COLOR, "Blocking Crest Color", Color.YELLOW);
    GameModule.getGameModule().getPrefs().addOption(AttackWizard.PREF_TAB, crestColorConfig);

    blockColorConfig = new ColorConfigurer(BLOCK_COLOR, "Blocking Hex Color", Color.RED);
    GameModule.getGameModule().getPrefs().addOption(AttackWizard.PREF_TAB, blockColorConfig);

    possibleColorConfig = new ColorConfigurer(POSSIBLE_COLOR, "Possibly Blocking Hex Color", Color.PINK);
    GameModule.getGameModule().getPrefs().addOption(AttackWizard.PREF_TAB, possibleColorConfig);

    blocksStartShowingConfig = new BooleanConfigurer(BLOCKS_START_SHOWING, "Blocking Terrain starts showing (Ctrl to toggle)?", true);
    GameModule.getGameModule().getPrefs().addOption(AttackWizard.PREF_TAB, blocksStartShowingConfig);
  }

  public Color getRidgeColor() {
    return (Color) ridgeColorConfig.getValue();
  }

  public Color getCrestColor() {
    return (Color) crestColorConfig.getValue();
  }

  public Color getBlockColor() {
    return (Color) blockColorConfig.getValue();
  }

  public Color getPossibleColor() {
    return (Color) possibleColorConfig.getValue();
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.removeDrawComponent(this);
    GameModule.getGameModule().removeCommandEncoder(this);
    idMgr.remove(this);
  }

  /**
   * The attributes of an LOS_Thread are:
   *
   * <pre>
   * <code>NAME</code>:  the name of the Preferences tab
   * <code>LABEL</code>:  the label of the button
   * <code>HOTKEY</code>:  the hotkey equivalent of the button
   * <code>DRAW_RANGE</code>:  If true, draw the distance between endpoints of the thread
   * <code>RANGE_FOREGROUND</code>:  the color of the text when drawing the distance
   * <code>RANGE_BACKGROUND</code>:  the color of the background rectangle when drawing the distance
   * <code>HIDE_COUNTERS</code>:  If true, hide all {@link GamePiece}s on the map when drawing the thread
   * </pre>
   */
  public String[] getAttributeNames() {
    return new String[] { NAME, LABEL, TOOLTIP, ICON_NAME, HOTKEY, REPORT, PERSISTENCE, PERSISTENT_ICON_NAME, GLOBAL, SNAP_START, SNAP_END, DRAW_RANGE,
      RANGE_SCALE, RANGE_ROUNDING, HIDE_COUNTERS, HIDE_OPACITY, LOS_COLOR, RANGE_FOREGROUND, RANGE_BACKGROUND };
  }

  public void setAttribute(String key, Object value) {
    if (DRAW_RANGE.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      drawRange = (Boolean) value;
    }
    else if (NAME.equals(key)) {
      setConfigureName((String) value);
    }
    else if (RANGE_SCALE.equals(key)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      rangeScale = (Integer) value;
    }
    else if (RANGE_ROUNDING.equals(key)) {
      if (ROUND_UP.equals(value)) {
        rangeRounding = 1.0;
      }
      else if (ROUND_DOWN.equals(value)) {
        rangeRounding = 0.0;
      }
      else {
        rangeRounding = 0.5;
      }
    }
    else if (HIDE_COUNTERS.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      hideCounters = (Boolean) value;
    }
    else if (HIDE_OPACITY.equals(key)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      setTransparency((Integer) value);
    }
    else if (RANGE_FOREGROUND.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      rangeFg = (Color) value;
    }
    else if (RANGE_BACKGROUND.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      rangeBg = (Color) value;
    }
    else if (LOS_COLOR.equals(key)) {
      if (value instanceof Color) {
        value = ColorConfigurer.colorToString((Color) value);
      }
      fixedColor = (String) value;
      threadColor = ColorConfigurer.stringToColor(fixedColor);
    }
    else if (SNAP_START.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      snapStart = (Boolean) value;
    }
    else if (SNAP_END.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      snapEnd = (Boolean) value;
    }
    else if (REPORT.equals(key)) {
      reportFormat.setFormat((String) value);
    }
    else if (PERSISTENCE.equals(key)) {
      persistence = (String) value;
    }
    else if (PERSISTENT_ICON_NAME.equals(key)) {
      persistentIconName = (String) value;
    }
    else if (GLOBAL.equals(key)) {
      global = (String) value;
    }
    else if (ICON_NAME.equals(key)) {
      iconName = (String) value;
    }
    //else {
    // launch.setAttribute(key, value);
    //}
  }

  protected void setTransparency(int h) {
    if (h < 0) {
      hideOpacity = 0;
    }
    else
      hideOpacity = Math.min(h, 100);
  }

  public String getAttributeValueString(String key) {
    if (DRAW_RANGE.equals(key)) {
      return String.valueOf(drawRange);
    }
    else if (NAME.equals(key)) {
      return getConfigureName();
    }
    else if (RANGE_SCALE.equals(key)) {
      return String.valueOf(rangeScale);
    }
    else if (RANGE_ROUNDING.equals(key)) {
      if (rangeRounding == 1.0) {
        return ROUND_UP;
      }
      else if (rangeRounding == 0.0) {
        return ROUND_DOWN;
      }
      else {
        return ROUND_OFF;
      }
    }
    else if (HIDE_COUNTERS.equals(key)) {
      return String.valueOf(hideCounters);
    }
    else if (HIDE_OPACITY.equals(key)) {
      return String.valueOf(hideOpacity);
    }
    else if (RANGE_FOREGROUND.equals(key)) {
      return ColorConfigurer.colorToString(rangeFg);
    }
    else if (RANGE_BACKGROUND.equals(key)) {
      return ColorConfigurer.colorToString(rangeBg);
    }
    else if (LOS_COLOR.equals(key)) {
      return fixedColor;
    }
    else if (SNAP_START.equals(key)) {
      return String.valueOf(snapStart);
    }
    else if (SNAP_END.equals(key)) {
      return String.valueOf(snapEnd);
    }
    else if (REPORT.equals(key)) {
      return reportFormat.getFormat();
    }
    else if (PERSISTENCE.equals(key)) {
      return persistence;
    }
    else if (PERSISTENT_ICON_NAME.equals(key)) {
      return persistentIconName;
    }
    else if (GLOBAL.equals(key)) {
      return global;
    }
    else if (ICON_NAME.equals(key)) {
      return iconName;
    }
    else {
      return null;
    }
  }

  public void setup(boolean show) {
    // launch.setEnabled(show);
  }

  /**
   * With Global visibility, LOS_Thread now has a state that needs to be
   * communicated to clients on other machines
   */

  public String getState() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(anchor.x).append(anchor.y).append(arrow.x).append(arrow.y);
    se.append(persisting);
    se.append(mirroring);
    return se.getValue();
  }

  public void setState(String state) {
    SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(state, ';');
    anchor.x = sd.nextInt(anchor.x);
    anchor.y = sd.nextInt(anchor.y);
    arrow.x = sd.nextInt(arrow.x);
    arrow.y = sd.nextInt(arrow.y);
    setPersisting(sd.nextBoolean(false));
    setMirroring(sd.nextBoolean(false));
  }

  public Color getColor(int range) {
    return (range <= sourceRange || alwaysInRange) ? IN_COLOR : OUT_COLOR;
  }

  public void draw(java.awt.Graphics g, Map m) {
    if (!visible)
      return;

    os_scale = Info.getSystemScaling();

    int r = calculateRange();
    g.setColor(getColor(r));
    final Point mapAnchor = map.mapToComponent(anchor);
    final Point mapArrow = map.mapToComponent(arrow);

    // Draw LOS
    g.drawLine((int) (mapAnchor.x * os_scale), (int) (mapAnchor.y * os_scale), (int) (mapArrow.x * os_scale), (int) (mapArrow.y * os_scale));

    // Draw Target
    imagePainter.draw(g, (int) (os_scale * (mapArrow.x - 16)), (int) (os_scale * (mapArrow.y - 16)), 1.0d * os_scale, m.getView());

    final boolean blocksStartShowing = blocksStartShowingConfig.getValueBoolean();
    if ((blocksStartShowing && !ctrlKeyDepressed) || (!blocksStartShowing && ctrlKeyDepressed)) {

      // Draw Hex Blocking terrain
      for (BlockingTerrain bt : blockingTerrain) {
        if (bt instanceof HexBlockingTerrain) {
          bt.draw(g, this);
        }
      }

      // Draw Edge Blocking terrain
      for (BlockingTerrain bt : blockingTerrain) {
        if (bt instanceof EdgeBlockingTerrain) {
          bt.draw(g, this);
        }
      }
    }

    // Draw Range
    if (drawRange) {
      drawRange(g, r, getColor(r));
    }

    lastAnchor = mapAnchor;
    lastArrow = mapArrow;
  }

  // Calculate the range from the current anchor to the current arrow
  public int calculateRange() {
    final Point rangeStart = getEffectiveAnchor();
    Point rangeEnd;
    int r = 0;

    if (currentNavalPark == null) {
      rangeEnd = new Point(arrow);
    }
    else {
      if (currentAssaultForceMarker == null) {
        return 0;
      }
      rangeEnd = currentAssaultForceMarker.getPosition();
    }

    final Board b = map.findBoard(rangeStart);
    MapGrid grid = null;
    if (b != null) {
      grid = b.getGrid();
    }
    if (grid instanceof ZonedGrid) {
      Point bp = new Point(rangeStart);
      bp.translate(-b.bounds().x, -b.bounds().y);
      Zone z = ((ZonedGrid) b.getGrid()).findZone(bp);
      if (z != null) {
        grid = z.getGrid();
      }
    }

    if (grid != null) {
      r = grid.range(rangeStart, rangeEnd);
    }

    r += addRange;

    if (currentNavalPark != null) {
      r += UnitInfo.calculateNavalAddRange(currentNavalBox);
      // Apply +10 to all ranges
      if ("true".equals(GameModule.getGameModule().getProperty(TdcProperties.ADD_10_TO_SWORD_NAVAL_RANGE))) {
        r += 10;
      }
    }

    return r;
  }

  public boolean drawAboveCounters() {
    return true;
  }

  protected void launch() {
    if (!visible) {
      map.pushMouseListener(this);
      if (hideCounters) {
        map.setPieceOpacity(hideOpacity / 100.0f);
        map.repaint();
      }
      visible = true;
      anchor.move(0, 0);
      arrow.move(0, 0);
      retainAfterRelease = false;
      initializing = true;
    }
    else if (persisting) {
      setPersisting(false);
    }
  }

  /**
   * Commands controlling persistence are passed between players, so LOS Threads
   * must have a unique ID.
   */
  public void setId(String id) {
    threadId = id;
  }

  public String getId() {
    return threadId;
  }

  /**
   * Since we register ourselves as a MouseListener using
   * {@link Map#pushMouseListener}, these mouse events are received in map
   * coordinates
   */
  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    // initializing = false;
    // if (visible && !persisting && !mirroring) {
    // Point p = source.getPosition();
    // Point q = e.getPoint();
    //
    // p = map.snapTo(map.mapCoordinates(p));
    // q = map.snapTo(map.mapCoordinates(q));
    //
    // anchor = p;
    // arrow = q;
    // anchorLocation = map.localizedLocationName(anchor);
    // lastLocation = anchorLocation;
    // lastRange = "";
    // checkList.clear();
    // ctrlWhenClick = e.isControlDown();
    // map.repaint();
    // }
  }

  public void mouseReleased(MouseEvent e) {
    if (!persisting && !mirroring) {
      if (retainAfterRelease && !(ctrlWhenClick && persistence.equals(CTRL_CLICK))) {
        retainAfterRelease = false;
        // if (global.equals(ALWAYS)) {
        // Command com = new LOSCommand(this, getAnchor(), getArrow(), false,
        // true);
        // GameModule.getGameModule().sendAndLog(com);
        // }
      }
      else if (e.getWhen() != lastRelease) {
        // visible = false;
        // if (global.equals(ALWAYS) || global.equals(WHEN_PERSISTENT)) {
        // if (persistence.equals(ALWAYS) || (ctrlWhenClick &&
        // persistence.equals(CTRL_CLICK))) {
        // anchor = lastAnchor;
        // Command com = new LOSCommand(this, getAnchor(), getArrow(), true,
        // false);
        // GameModule.getGameModule().sendAndLog(com);
        // setPersisting(true);
        // }
        // else {
        // Command com = new LOSCommand(this, getAnchor(), getArrow(), false,
        // false);
        // GameModule.getGameModule().sendAndLog(com);
        // }
        // }
        map.setPieceOpacity(1.0f);
        map.popMouseListener();
        map.repaint();

        // Board b = map.findBoard(getEffectiveAnchor());
        // MapGrid grid = null;
        // if (b != null) {
        // grid = b.getGrid();
        // }
        // if (grid != null && grid instanceof ZonedGrid) {
        // Point bp = new Point(getEffectiveAnchor());
        // bp.translate(-b.bounds().x, -b.bounds().y);
        // Zone z = ((ZonedGrid) b.getGrid()).findZone(bp);
        // if (z != null) {
        // grid = z.getGrid();
        // }
        // }

        // int range = grid == null ? 0 : grid.range(getEffectiveAnchor(),
        // arrow);
        int range = calculateRange();
        active = false;
        indirectSource = null;
        indirectAnchor = null;
        addRange = 0;
        myWizard.targetSelected(arrow, range, this);
      }
      lastRelease = e.getWhen();

    }
    ctrlWhenClick = false;
  }

  protected void setPersisting(boolean b) {
    persisting = b;
    visible = b;
    setMirroring(false);

    // if (persisting) {
    // launch.setAttribute(ICON_NAME, persistentIconName);
    // }
    // else {
    // launch.setAttribute(ICON_NAME, iconName);
    // map.repaint();
    // }
  }

  protected boolean isPersisting() {
    return persisting;
  }

  protected void setMirroring(boolean b) {
    mirroring = b;
    if (mirroring) {
      visible = true;
    }
  }

  protected boolean isMirroring() {
    return mirroring;
  }

  protected Point getAnchor() {
    return new Point(anchor);
  }

  protected void setEndPoints(Point newAnchor, Point newArrow, boolean showBlocking) {
    anchor.x = newAnchor.x;
    anchor.y = newAnchor.y;
    arrow.x = newArrow.x;
    arrow.y = newArrow.y;
    if (showBlocking) {
      findLOSBlockingTerrain();
    }
    map.repaint();
  }

  protected Point getArrow() {
    return new Point(arrow);
  }

  protected Point getEffectiveAnchor() {
    return new Point(indirectSource == null ? anchor : indirectAnchor);
  }

  protected int getLosCheckCount() {
    return checkList.size();
  }

  protected String getLosCheckList() {
    // FIXME: should use StringBuilder?
    String list = "";
    for (String loc : checkList) {
      list += (list.length() > 0 ? ", " : "") + loc;
    }
    return list;
  }

  /**
   * Since we register ourselves as a MouseMotionListener directly, these mouse
   * events are received in component coordinates
   */
  public void mouseMoved(MouseEvent e) {
    mouseDragged(e);
  }

  public void mouseDragged(MouseEvent e) {
    if (active && visible && !persisting && !mirroring) {
      // retainAfterRelease = true;

      setCtrlKeyDepressed((e.getModifiersEx() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK);

      Point p = e.getPoint();

      map.scrollAtEdge(p, 15);

      if (Boolean.TRUE.equals(GameModule.getGameModule().getPrefs().getValue(SNAP_LOS)) || snapEnd) {
        arrow = map.snapTo(map.componentToMap(p));
      }
      else {
        arrow = map.componentToMap(p);
      }

      // LOS target has snapped to a new Point, recalculate any Ridge/Crest
      // crossings
      if (!arrow.equals(lastArrowSnap)) {
        findLOSBlockingTerrain();
        lastArrowSnap = new Point(arrow);
      }

      // Find the Zone the arrow is over and if it is a Naval Box
      final Zone z = map.findZone(new Point(arrow));
      if (z == null) {
        currentNavalPark = null;
        currentAssaultForceMarker = null;
        currentNavalBox = null;
      }
      else {
        final String navalPark = (String) z.getProperty(TdcProperties.NAVAL_PARK);
        if (navalPark == null) {
          currentNavalPark = null;
          currentAssaultForceMarker = null;
          currentNavalBox = null;
        }
        else {
          if (!navalPark.equals(currentNavalPark)) {
            currentNavalPark = navalPark;
            currentAssaultForceMarker = myWizard.findAssaultForceMarker(navalPark);
          }
          currentNavalBox = (String) z.getProperty(TdcProperties.NAVAL_BOX);
        }
      }

      Point mapAnchor = map.componentToMap(lastAnchor);
      Point mapArrow = map.componentToMap(lastArrow);
      int fudge = (int) (1.0 / map.getZoom() * 2);
      Rectangle r = new Rectangle(Math.min(mapAnchor.x, mapArrow.x) - fudge, Math.min(mapAnchor.y, mapArrow.y) - fudge, Math.abs(mapAnchor.x - mapArrow.x) + 1
        + fudge * 2, Math.abs(mapAnchor.y - mapArrow.y) + 1 + fudge * 2);

      map.repaint();

      if (drawRange) {
        r = new Rectangle(lastRangeRect);
        r.width += (int) (r.width / map.getZoom()) + 1;
        r.height += (int) (r.height / map.getZoom()) + 1;
        map.repaint(r);
      }
    }
  }

  protected void setCtrlKeyDepressed(boolean b) {
    ctrlKeyDepressed = b;
    map.repaint();
  }

  protected boolean isCtrlKeyDepressed() {
    return ctrlKeyDepressed;
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      setCtrlKeyDepressed(true);
    }
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      setCtrlKeyDepressed(false);
    }
  }

  public void keyTyped(KeyEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * Writes text showing the range
   *
   * @param range
   *          the range to display, in whatever units returned by the
   *          {@link MapGrid} containing the thread
   */
  public void drawRange(Graphics g, int range, Color color) {
    Point mapArrow = map.mapToComponent(arrow);
    Point mapAnchor = map.mapToComponent(anchor);
    g.setColor(color);
    g.setFont(RANGE_FONT);
    final FontMetrics fm = g.getFontMetrics();
    final StringBuilder buffer = new StringBuilder();
    int dummy = range;
    while (dummy >= 1) {
      dummy = dummy / 10;
      buffer.append("8");
    }
    if (buffer.length() == 0) {
      buffer.append("8");
    }
    String rangeMess = Resources.getString("LOS_Thread.range");
    int wid = fm.stringWidth(" " + rangeMess + "  " + buffer.toString());
    int hgt = fm.getAscent() + 2;
    int w = mapArrow.x - mapAnchor.x;
    int h = mapArrow.y - mapAnchor.y;
    int x0 = mapArrow.x + (int) ((wid / 2 + 20) * w / Math.sqrt(w * w + h * h));
    int y0 = mapArrow.y + (int) ((hgt / 2 + 20) * h / Math.sqrt(w * w + h * h));

    if (alwaysInRange) {
      wid = fm.stringWidth(" " + "In Range" + " ");
      x0 = mapArrow.x + (int) ((wid / 2 + 20) * w / Math.sqrt(w * w + h * h));
      y0 = mapArrow.y + (int) ((hgt / 2 + 20) * h / Math.sqrt(w * w + h * h));
      g.fillRect((int) (os_scale* (x0 - wid / 2)), (int) (os_scale* (y0 + hgt / 2 - fm.getAscent())), (int) (os_scale* (wid)), (int) (os_scale* (hgt)));
      g.setColor(Color.white);
      g.drawString("In Range", (int) (os_scale* (x0 - wid / 2 + fm.stringWidth(" "))), (int) (os_scale* (y0 + hgt / 2 - 1)));
    }
    else {
      g.fillRect((int) (os_scale* (x0 - wid / 2)), (int) (os_scale* (y0 + hgt / 2 - fm.getAscent())), (int) (os_scale* (wid)), (int) (os_scale* (hgt)));
      g.setColor(Color.white);
      g.drawString(rangeMess + " " + range, (int) (os_scale* (x0 - wid / 2 + fm.stringWidth(" "))), (int) (os_scale* (y0 + hgt / 2 - 1)));
    }
    lastRangeRect = new Rectangle(x0 - wid / 2, y0 + hgt / 2 - fm.getAscent(), wid + 1, hgt + 1);
    Point np = map.componentToMap(new Point(lastRangeRect.x, lastRangeRect.y));
    lastRangeRect.x = np.x;
    lastRangeRect.y = np.y;
    lastRange = String.valueOf(range);
  }

  protected void initLOSBlockingTerrain() {
    ((TdcMap) map).initBlockingTerrain();
  }

  protected void findLOSBlockingTerrain() {
    if (!isIndirectThread()) {
      ((TdcMap) map).findBlockingTerrain(anchor, arrow, blockingTerrain);
    }
  }
  protected String getBlockingReasons() {
    ArrayList<String> reasons = new ArrayList<>();
    for (BlockingTerrain bt : blockingTerrain) {
      for (Iterator<String> i = bt.getReasons(); i.hasNext();) {
        String reason = i.next();
        if (!reasons.contains(reason))
          reasons.add(reason);
      }
    }
    final StringBuilder s = new StringBuilder();
    for (String reason : reasons) {
      if (s.length() > 0)
        s.append(", ");
      s.append(reason);
    }

    return s.toString();
  }

  public boolean isBlocked() {
    return blockingTerrain.size() > 0;
  }

  public static String getConfigureTypeName() {
    return "Line of Sight Thread";
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("Map.htm", "LOS");
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Thread name:  ", "Button text:  ", "Tooltip text:  ", "Button Icon:  ", "Hotkey:  ", "Report Format:  ", "Persistence:  ",
      "Button Icon when LOS persisting:  ", "Visible to Opponent:  ", "Force start of thread to snap to grid?", "Force end of thread to snap to grid?",
      "Draw Range?", "Pixels per range unit (0 to use Grid calculation):  ", "Round fractions:  ", "Hide Pieces while drawing?",
      "Opacity of hidden pieces (0-100%):  ", "Thread color:  " };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] { String.class, String.class, String.class, IconConfig.class, KeyStroke.class, ReportFormatConfig.class, PersistenceOptions.class,
      IconConfig.class, GlobalOptions.class, Boolean.class, Boolean.class, Boolean.class, Integer.class, RoundingOptions.class, Boolean.class, Integer.class,
      Color.class };
  }

  public static class IconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, DEFAULT_ICON);
    }
  }

  public static class ReportFormatConfig implements TranslatableConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new PlayerIdFormattedStringConfigurer(key, name, new String[] { FROM_LOCATION, TO_LOCATION, RANGE, CHECK_COUNT, CHECK_LIST });
    }
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    VisibilityCondition cond = null;
    if (RANGE_SCALE.equals(name) || RANGE_ROUNDING.equals(name)) {
      cond = () -> drawRange;
    }
    else if (HIDE_OPACITY.equals(name)) {
      cond = () -> hideCounters;
    }
    else if (PERSISTENT_ICON_NAME.equals(name)) {
      cond = () -> persistence.equals(CTRL_CLICK) || persistence.equals(ALWAYS);
    }

    return cond;
  }

  public static class RoundingOptions extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { ROUND_UP, ROUND_DOWN, ROUND_OFF };
    }
  }

  public static class PersistenceOptions extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { CTRL_CLICK, NEVER, ALWAYS };
    }
  }

  public static class GlobalOptions extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { WHEN_PERSISTENT, NEVER, ALWAYS };
    }
  }

  public Configurable[] getConfigureComponents() {
    return new Configurable[0];
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public Command decode(String command) {
    SequenceEncoder.Decoder sd;
    if (command.startsWith(LOS_THREAD_COMMAND + getId())) {
      sd = new SequenceEncoder.Decoder(command, '\t');
      sd.nextToken();
      sd.nextToken();
      Point anchor = new Point(sd.nextInt(0), sd.nextInt(0));
      Point arrow = new Point(sd.nextInt(0), sd.nextInt(0));
      return new LOSCommand(this, anchor, arrow);
    }
    return null;
  }

  public String encode(Command c) {
    if (c instanceof LOSCommand) {
      LOSCommand com = (LOSCommand) c;
      SequenceEncoder se = new SequenceEncoder(com.target.getId(), '\t');
      se.append(com.newAnchor.x).append(com.newAnchor.y).append(com.newArrow.x).append(com.newArrow.y);
      return LOS_THREAD_COMMAND + se.getValue();
    }
    else {
      return null;
    }
  }

  public static class LOSCommand extends Command {
    protected WizardThread target;
    protected String oldState;
    protected Point newAnchor, oldAnchor;
    protected Point newArrow, oldArrow;

    public LOSCommand(WizardThread oTarget, Point anchor, Point arrow) {
      target = oTarget;
      oldAnchor = target.getAnchor();
      oldArrow = target.getArrow();
      newAnchor = anchor;
      newArrow = arrow;
    }

    protected void executeCommand() {
      target.setEndPoints(newAnchor, newArrow, true);
      target.setVisible(true);
    }

    protected Command myUndoCommand() {
      return new LOSCommand(target, oldAnchor, oldArrow);
    }
  }

}
