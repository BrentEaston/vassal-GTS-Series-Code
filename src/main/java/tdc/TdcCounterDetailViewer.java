/*
 * $Id: TdcCounterDetailViewer.java 9453 2020-06-07 22:45:00Z swampwallaby $
 *
 * Copyright (c) 2003-2009 by David Sullivan, Rodney Kinney,
 * Brent Easton, and Joel Uckelman.
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import terrain.TerrainHexGrid;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.IllegalBuildException;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.CompoundPieceCollection;
import VASSAL.build.module.map.Drawable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.ColorConfigurer;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.FormattedStringConfigurer;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.IntConfigurer;
import VASSAL.configure.PropertyExpression;
import VASSAL.configure.SingleChildInstance;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.configure.StringEnum;
import VASSAL.configure.VisibilityCondition;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.ColoredBorder;
import VASSAL.counters.Deck;
import VASSAL.counters.DeckVisitorDispatcher;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceFilter;
import VASSAL.counters.PieceFinder;
import VASSAL.counters.PieceIterator;
import VASSAL.counters.Properties;
import VASSAL.counters.Stack;
import VASSAL.i18n.Resources;
import VASSAL.tools.FormattedString;
import VASSAL.tools.image.LabelUtils;

/**
 * This is a {@link Drawable} class that draws the counters horizontally when
 * the mouse is held over a stack with the control key down.
 * 
 * @author David Sullivan
 * @version 1.0
 */
public class TdcCounterDetailViewer extends AbstractConfigurable implements Drawable, DragSourceMotionListener, MouseMotionListener, MouseListener, KeyListener {

  public static final String LATEST_VERSION = "2";
  public static final String USE_KEYBOARD = "ShowCounterDetails";
  public static final String PREFERRED_DELAY = "PreferredDelay";

  public static final String DELAY = "delay";
  public static final String ALWAYS_SHOW_LOC = "alwaysshowloc";
  public static final String DRAW_PIECES = "showgraph";
  public static final String GRAPH_SINGLE_DEPRECATED = "showgraphsingle";
  public static final String MINIMUM_DISPLAYABLE = "minDisplayPieces";
  public static final String HOTKEY = "hotkey";

  public static final String SHOW_TEXT = "showtext";
  public static final String SHOW_TEXT_SINGLE_DEPRECATED = "showtextsingle";
  public static final String ZOOM_LEVEL = "zoomlevel";
  public static final String DRAW_PIECES_AT_ZOOM = "graphicsZoom";
  public static final String BORDER_WIDTH = "borderWidth";
  public static final String SHOW_NOSTACK = "showNoStack";
  public static final String SHOW_MOVE_SELECTED = "showMoveSelectde";
  public static final String SHOW_NON_MOVABLE = "showNonMovable";
  public static final String SHOW_DECK = "showDeck";
  public static final String UNROTATE_PIECES = "unrotatePieces";
  public static final String DISPLAY = "display";
  public static final String LAYER_LIST = "layerList";
  public static final String SUMMARY_REPORT_FORMAT = "summaryReportFormat";
  public static final String COUNTER_REPORT_FORMAT = "counterReportFormat";
  public static final String EMPTY_HEX_REPORT_FORMAT = "emptyHexReportForma";
  public static final String VERSION = "version";
  public static final String FG_COLOR = "fgColor";
  public static final String BG_COLOR = "bgColor";
  public static final String FONT_SIZE = "fontSize";
  public static final String PROPERTY_FILTER = "propertyFilter";

  public static final String TOP_LAYER = "from top-most layer only";
  public static final String ALL_LAYERS = "from all layers";
  public static final String INC_LAYERS = "from listed layers only";
  public static final String EXC_LAYERS = "from layers other than those listed";
  public static final String FILTER = "by using a property filter";

  public static final String SUM = "sum(propertyName)";

  protected KeyStroke hotkey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
  protected Map map;
  protected int delay = 700;
  protected Timer delayTimer;

  protected boolean graphicsVisible = false;
  protected boolean textVisible = false;
  protected MouseEvent currentMousePosition;

  protected int minimumDisplayablePieces = 2;
  protected boolean alwaysShowLoc = false;
  protected boolean drawPieces = true;
  protected boolean drawSingleDeprecated = false;
  protected boolean showText = false;
  protected boolean showTextSingleDeprecated = false;
  protected boolean unrotatePieces = false;
  protected boolean showDeck = false;
  protected double zoomLevel = 1.0;
  protected double graphicsZoomLevel = 1.0;
  protected int borderWidth = 0;
  protected boolean showNoStack = false;
  protected boolean showMoveSelected = false;
  protected boolean showNonMovable = false;
  protected String displayWhat = TOP_LAYER;
  protected String[] displayLayers = new String[0];
  protected FormattedString summaryReportFormat = new FormattedString("$" + BasicPiece.LOCATION_NAME + "$");
  protected FormattedString counterReportFormat = new FormattedString("");
  protected FormattedString emptyHexReportFormat = new FormattedString("$" + BasicPiece.LOCATION_NAME + "$");
  protected String version = "";
  protected Color fgColor = Color.black;
  protected Color bgColor;
  protected Color saveBgColor;
  protected int fontSize = 9;
  protected PropertyExpression propertyFilter = new PropertyExpression();

  protected Rectangle bounds;
  protected Rectangle dbounds;
  protected double os_scale;
  
  protected boolean mouseInView = true;
  protected List<GamePiece> displayablePieces = null;

  protected Color fg, bg;

  protected boolean rubble;

  /** the JComponent which is repainted when the detail viewer changes */
  protected JComponent view;

  public TdcCounterDetailViewer() {
    // Set up the timer; this isn't the real delay---we always check the
    // preferences for that.
    delayTimer = new Timer(delay, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mouseInView)
          showDetails();
      }
    });

    delayTimer.setRepeats(false);
  }

  public void addTo(Buildable b) {
    map = (Map) b;
    view = map.getView();
    validator = new SingleChildInstance(map, getClass());
    map.addDrawComponent(this);
    String keyDesc = hotkey == null ? "" : "(" + HotKeyConfigurer.getString(hotkey) + ")";
    GameModule.getGameModule().getPrefs().addOption(Resources.getString("Prefs.general_tab"), new BooleanConfigurer(USE_KEYBOARD, Resources.getString("CounterDetailViewer.use_prompt", keyDesc), Boolean.FALSE));
    GameModule.getGameModule().getPrefs().addOption(Resources.getString("Prefs.general_tab"), new IntConfigurer(PREFERRED_DELAY, Resources.getString("CounterDetailViewer.delay_prompt"), new Integer(delay)));

    view.addMouseMotionListener(this);
    view.addMouseListener(this);
    view.addKeyListener(this);
    DragSource.getDefaultDragSource().addDragSourceMotionListener(this);

    setAttributeTranslatable(VERSION, false);
    setAttributeTranslatable(SUMMARY_REPORT_FORMAT, true);
    setAttributeTranslatable(COUNTER_REPORT_FORMAT, true);
  }

  public void draw(Graphics g, Map map) {
    if (currentMousePosition != null && view.getVisibleRect().contains(currentMousePosition.getPoint())) {
      draw(g, currentMousePosition.getPoint(), view);
    }
  }

  public boolean drawAboveCounters() {
    return true;
  }

  public void draw(Graphics g, Point pt, JComponent comp) {

    if (!graphicsVisible && !textVisible) {
      return;
    }

    bounds = new Rectangle(pt.x, pt.y, 0, 0);
    os_scale = Info.getSystemScaling();

    if (graphicsVisible) {
      drawGraphics(g, pt, comp, displayablePieces);
    }

    if (textVisible) {
      drawText(g, pt, comp, displayablePieces);
    }
  }

  @Deprecated
  // Required for backward compatibility
  protected void drawGraphics(Graphics g, Point pt, JComponent comp, PieceIterator pi) {
    ArrayList<GamePiece> a = new ArrayList<>();
    while (pi.hasMoreElements()) {
      a.add(pi.nextPiece());
    }
    drawGraphics(g, pt, comp, a);
  }

  protected String convertRating(String rating, boolean hasStepLoss) {
    if (hasStepLoss) {
      int irating;
      try {
        irating = Integer.parseInt(rating);
      }
      catch (Exception e) {
        return rating;
      }
      irating -= 1;
      if (irating < 0)
        irating = 0;
      return String.valueOf(irating);
    }
    return rating;
  }

  protected void drawGraphics(Graphics g, Point pt, JComponent comp, List<GamePiece> pieces) {

    fixBounds(pieces);
    if (TdcProperties.DEBUG) {
      bounds.height *= 2; // Double height for ratings display
    }

    if (bounds.width <= 0) {
      return;
    }

    final boolean hasHoverText = pieces.get(0).getProperty(TdcProperties.HOVER_TEXT) != null;

    dbounds = new Rectangle(bounds);
    dbounds.x *= os_scale;
    dbounds.y *= os_scale;
    dbounds.width *= os_scale;
    dbounds.height *= os_scale;

    Rectangle visibleRect = comp.getVisibleRect();
    visibleRect.x *= os_scale;
    visibleRect.y *= os_scale;
    visibleRect.width *= os_scale;
    visibleRect.height *= os_scale;
    
    dbounds.x = Math.min(dbounds.x, visibleRect.x + visibleRect.width - dbounds.width);
    if (dbounds.x < visibleRect.x)
      dbounds.x = visibleRect.x;
    dbounds.x += 10;
    dbounds.y = Math.min(dbounds.y, visibleRect.y + visibleRect.height - dbounds.height) - (isTextUnderCounters() ? 15 : 0);
    int minY = visibleRect.y + (textVisible ? g.getFontMetrics().getHeight() + 6 : 0);
    if (dbounds.y < minY)
      dbounds.y = minY;

    if (!hasHoverText) {
      if (bgColor != null) {
        g.setColor(bgColor);
        g.fillRect(dbounds.x, dbounds.y, dbounds.width, dbounds.height);
      }
      if (fgColor != null) {
        g.setColor(fgColor);
        g.drawRect(dbounds.x - 1, dbounds.y - 1, dbounds.width + 1, dbounds.height + 1);
        g.drawRect(dbounds.x - 2, dbounds.y - 2, dbounds.width + 3, dbounds.height + 3);
      }
    }
    Shape oldClip = g.getClip();

    rubble = false;
    int borderOffset = borderWidth;
    double graphicsZoom = graphicsZoomLevel;
    for (final GamePiece piece : pieces) {

      final Rectangle piecebounds = getBounds(piece);
      final String hover = (String) piece.getProperty(TdcProperties.HOVER_TEXT);

      // Draw the next piece
      // pt is the location of the left edge of the piece

      if (!hasHoverText) {
        String type = (String) piece.getProperty(TdcProperties.TYPE);
        if (TdcProperties.TYPE_RUBBLE.equals(type)) {
          rubble = true;
        }

        if (unrotatePieces)
          piece.setProperty(Properties.USE_UNROTATED_SHAPE, Boolean.TRUE);
        g.setClip(dbounds.x - 3, dbounds.y - 3, dbounds.width + 5, dbounds.height + 5);
        piece.draw(g, dbounds.x - (int) (piecebounds.x * graphicsZoom * os_scale) + borderOffset, dbounds.y - (int) (piecebounds.y * graphicsZoom * os_scale) + borderWidth, comp, graphicsZoom * os_scale);
        if (unrotatePieces)
          piece.setProperty(Properties.USE_UNROTATED_SHAPE, Boolean.FALSE);
        g.setClip(oldClip);

        if (isTextUnderCounters()) {
          String text = counterReportFormat.getLocalizedText(piece);
          if (text.length() > 0) {
            int x = dbounds.x - (int) (piecebounds.x * graphicsZoom * os_scale) + borderOffset;
            int y = dbounds.y + dbounds.height + 10;
            drawLabel(g, new Point(x, y), text, LabelUtils.CENTER, LabelUtils.CENTER);
          }

        }
      }
      else {
        // Hover Text Display
        if (hover != null) {
          final JTextArea text = new JTextArea();
          final int textWidth = 250;
          final int textHeight = 100;
          text.setText(hover);
          text.setLineWrap(true);
          text.setWrapStyleWord(true);
          text.setForeground(Color.BLACK);
          text.setBackground(Color.WHITE);
          text.setSize(textWidth, textHeight);
          text.setBorder(BorderFactory.createEtchedBorder());

          final BufferedImage image = new BufferedImage(text.getWidth(), text.getHeight(), BufferedImage.TYPE_INT_RGB);
          final Graphics2D imageGraphics = image.createGraphics();
          // panel.paint(imageGraphics);
          text.paint(imageGraphics);
          int x = Math.min(dbounds.x, visibleRect.x + visibleRect.width - textWidth);
          if (x < visibleRect.x)
            x = visibleRect.x;
          int y = Math.min(dbounds.y, visibleRect.y + visibleRect.height - textHeight);
          if (y < visibleRect.y)
            y = visibleRect.y;
          if (x == 0) {
            x = 1;
          }
          g.drawImage(image, x, y, null);
        }
      }

      // Ratings Display
      if (TdcProperties.DEBUG) {
        @SuppressWarnings("unchecked") final HashMap<String, String> ratings = (HashMap<String, String>) piece.getProperty(TdcProperties.RATINGS);
        if (ratings != null) {
          boolean hasStepLoss = "true".equals(piece.getProperty("Step_Active"));

          final String fireRating = convertRating(ratings.get(TdcRatings.FIRE_RATING), hasStepLoss);

          final String fireColor = ratings.get(TdcRatings.FIRE_COLOUR);
          if ("No".equals(fireRating)) {
            LabelUtils.drawLabel(g, "No", dbounds.x, dbounds.y + 87, FONT_SML, LabelUtils.RIGHT, LabelUtils.CENTER, COLOR_RED, COLOR_BLACK, COLOR_RED);
          }
          else {
            fg = TdcRatings.getFireFg(fireColor);
            bg = TdcRatings.getFireBg(fireColor);
            LabelUtils.drawLabel(g, fireRating, dbounds.x, dbounds.y + 87, FONT_LG, LabelUtils.RIGHT, LabelUtils.CENTER, fg, bg, COLOR_BLACK);
          }

          final String range = ratings.get(TdcRatings.RANGE);
          if (range != null && !range.equals("1")) {
            LabelUtils.drawLabel(g, range, dbounds.x + 18, dbounds.y + 83, FONT_LG, LabelUtils.RIGHT, LabelUtils.CENTER, Color.black, null, null);
          }

          final String assaultRating = convertRating(ratings.get(TdcRatings.ASSAULT_RATING), hasStepLoss);
          final String assaultColor = ratings.get(TdcRatings.ASSAULT_COLOUR);
          final String assaultDouble = ratings.get(TdcRatings.ASSAULT_DOUBLE);
          if (assaultRating != null && !assaultRating.equals("No")) {
            fg = TdcRatings.getFireFg(assaultColor);
            bg = TdcRatings.getFireBg(assaultColor);
            if ("true".equals(assaultDouble)) {
              fg = COLOR_RED;
            }
            LabelUtils.drawLabel(g, assaultRating, dbounds.x + 20, dbounds.y + 100, FONT_SML, LabelUtils.RIGHT, LabelUtils.CENTER, fg, bg, COLOR_BLACK);
          }

          final String flakRating = ratings.get(TdcRatings.FLAK_RATING);
          if (!("No".equals(flakRating))) {
            LabelUtils.drawLabel(g, flakRating, dbounds.x + 2, dbounds.y + 110, FONT_SML, LabelUtils.RIGHT, LabelUtils.CENTER, COLOR_BLACK, COLOR_BLUE, COLOR_BLACK);
          }
          final String defRating = ratings.get(TdcRatings.DEF_RATING);
          final String armoured = ratings.get(TdcRatings.ARMOURED);
          if (defRating != null) {
            if ("true".equals(armoured)) {
              bg = COLOR_BLACK;
              fg = COLOR_WHITE;
            }
            else {
              fg = COLOR_BLACK;
              bg = COLOR_WHITE;
            }
            LabelUtils.drawLabel(g, defRating, dbounds.x, dbounds.y + 130, FONT_SML, LabelUtils.RIGHT, LabelUtils.CENTER, fg, bg, COLOR_BLACK);
          }

          final String tqrRating = convertRating(ratings.get(TdcRatings.TQR_RATING), hasStepLoss);
          final boolean isRedTqr = "true".equals(ratings.get(TdcRatings.RED_TQR));
          LabelUtils.drawLabel(g, tqrRating, dbounds.x + 75, dbounds.y + 83, FONT_LG, LabelUtils.LEFT, LabelUtils.CENTER, isRedTqr ? COLOR_RED : COLOR_BLACK, null, null);

          final String moveRating = ratings.get(TdcRatings.MOVE_RATING);
          final String moveColour = ratings.get(TdcRatings.MOVE_COLOUR);
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
          LabelUtils.drawLabel(g, moveRating, dbounds.x + 75, dbounds.y + 103, ("No".equals(moveRating) ? FONT_SML : FONT_LG), LabelUtils.LEFT, LabelUtils.CENTER, fg, bg, null);

          String steps = (String) piece.getProperty(TdcProperties.STEP);
          final String organic = ratings.get(TdcRatings.ORGANIC);
          if ("true".equals(organic)) {
            g.setColor(COLOR_BLACK);
            g.fillRect(dbounds.x + 40, dbounds.y + 78, 15, 9);
          }
          if ("1".equals(steps)) {
            g.setColor(COLOR_RED);
            g.fillOval(dbounds.x + 46, dbounds.y + 80, 5, 5);
          }
          else if ("2".equals(steps)) {
            g.setColor(COLOR_RED);
            g.fillOval(dbounds.x + 42, dbounds.y + 80, 5, 5);
            g.fillOval(dbounds.x + 49, dbounds.y + 80, 5, 5);
          }
          // has Telephone?
          if ("true".equals(piece.getProperty(TdcProperties.HAS_TELEPHONE))) {
            LabelUtils.drawLabel(g, "P", dbounds.x, dbounds.y + 110, FONT_SML, LabelUtils.RIGHT, LabelUtils.CENTER, Color.white, Color.red, null);
          }
          // Independent?
          final boolean isBlackStripe = "true".equals(piece.getProperty(TdcProperties.BLACK_STRIPE_IND));
          if ("true".equals(piece.getProperty(TdcProperties.IS_INDEPENDENT))) {
            LabelUtils.drawLabel(g, "                     ", dbounds.x + 14, dbounds.y + 142, FONT_VSML, LabelUtils.RIGHT, LabelUtils.CENTER, isBlackStripe ? Color.black : Color.white, isBlackStripe ? Color.black : Color.white, COLOR_BLACK);
          }
        }
      }
      /*
       * ----------------------------------------------------------------------
       * -------------------------
       */

      dbounds.translate((int) (piecebounds.width * graphicsZoom * os_scale), 0);
      borderOffset += borderWidth;
    }

  }

  protected static final Font FONT_LG = new Font(Font.DIALOG, Font.BOLD, 16);
  protected static final Font FONT_SML = new Font(Font.DIALOG, Font.BOLD, 12);
  protected static final Font FONT_VSML = new Font(Font.DIALOG, Font.BOLD, 8);
  protected static final int SIZE_SML = 12;
  protected static final Color COLOR_WHITE = Color.white;
  protected static final Color COLOR_RED = Color.red;
  protected static final Color COLOR_BLACK = Color.black;
  protected static final Color COLOR_BLUE = new Color(70, 160, 220);

  /** Set the bounds field large enough to accommodate the given set of pieces */
  protected void fixBounds(List<GamePiece> pieces) {
    for (GamePiece piece : pieces) {
      final Dimension pieceBounds = getBounds(piece).getSize();
      bounds.width += (int) Math.round(pieceBounds.width * graphicsZoomLevel) + borderWidth;
      bounds.height = Math.max(bounds.height, (int) Math.round(pieceBounds.height * graphicsZoomLevel) + borderWidth * 2);
    }

    bounds.width += borderWidth;
    bounds.y -= bounds.height;
  }

  protected Rectangle getBounds(GamePiece piece) {
    if (unrotatePieces)
      piece.setProperty(Properties.USE_UNROTATED_SHAPE, Boolean.TRUE);
    Rectangle pieceBounds = piece.getShape().getBounds();
    if (unrotatePieces)
      piece.setProperty(Properties.USE_UNROTATED_SHAPE, Boolean.FALSE);
    return pieceBounds;
  }

  protected boolean isTextUnderCounters() {
    return textVisible && counterReportFormat.getFormat().length() > 0;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Deprecated
  // Required for backward compatibility
  protected void drawText(Graphics g, Point pt, JComponent comp, List pieces) {
    drawText(g, pt, comp, new PieceIterator(pieces.iterator()));
  }

  protected void drawText(Graphics g, Point pt, JComponent comp, PieceIterator pi) {
    /*
     * Label with the location If the counter viewer is being displayed, then
     * place the location name just above the left hand end of the counters. If
     * no counter viewer (i.e. single piece or expanded stack), then place the
     * location name above the centre of the first piece in the stack.
     */
    String locationName = null;
    String terrain = "";
    String weather = null;
    String time = null;
    int steps = 0;
    int x = (int) (bounds.x * os_scale - bounds.width * os_scale);
    int y = (int) (bounds.y * os_scale - 5 * os_scale);

    while (pi.hasMoreElements()) {
      GamePiece piece = pi.nextPiece();
      if (locationName == null) {
        locationName = map.locationName(piece.getPosition());
      }
      // if (terrain == null) {
      // terrain = (String) piece.getProperty("currentTerrain");
      // }
      String s = (String) piece.getProperty(TdcProperties.STEP);
      if (s != null) {
        int step = 0;
        try {
          step += Integer.parseInt(s);
        }
        catch (Exception ignored) {
        }
        if (step == 2) {
          String a = (String) piece.getProperty(TdcProperties.STEP + Embellishment.ACTIVE);
          if (a != null && a.equals("true")) {
            step = 1;
          }
        }
        steps += step;
      }
    }

    Point mapPt = map.componentToMap(currentMousePosition.getPoint());
    Point snapPt = map.snapTo(mapPt);

    Board board = map.findBoard(snapPt);
    if (board != null) {
      TerrainHexGrid hgrid = null;
      MapGrid grid = board.getGrid();
      if (grid instanceof ZonedGrid) {
        ZonedGrid zGrid = (ZonedGrid) grid;
        grid = zGrid.getBackgroundGrid();
        if (grid == null) {
          Zone zone = zGrid.findZone(snapPt);
          if (zone != null) {
            grid = zone.getGrid();
          }
        }
        if (grid instanceof TerrainHexGrid) {
          hgrid = (TerrainHexGrid) grid;
        }
      }
      else {
        hgrid = (TerrainHexGrid) grid;
      }
      if (hgrid != null) {
        boolean op = "true".equals(hgrid.getProperty(TdcProperties.OP, snapPt));
        boolean strongpoint = "true".equals(hgrid.getProperty(TdcProperties.STRONGPOINT, snapPt));
        boolean hill = "true".equals(hgrid.getProperty(TdcProperties.HILL, snapPt));
        boolean inGermany = "true".equals(hgrid.getProperty(TdcProperties.IN_GERMANY, snapPt));
        boolean inBirHachiem = "true".equals(hgrid.getProperty(TdcProperties.BIR_HACHEIM, snapPt));
        boolean minefieldDoor = "true".equals(hgrid.getProperty(TdcProperties.MINEFIELD_DOOR, snapPt));
        boolean raised = "true".equals(hgrid.getProperty(TdcProperties.RAISED_ROAD, snapPt)) || "true".equals(hgrid.getProperty(TdcProperties.RAISED_RAILROAD, snapPt));
        boolean embank = "true".equals(hgrid.getProperty(TdcProperties.EMBANKMENT_ROAD, snapPt)) || "true".equals(hgrid.getProperty(TdcProperties.EMBANKMENT_RAILROAD, snapPt));
        boolean sunken = "true".equals(hgrid.getProperty(TdcProperties.SUNKEN_ROAD, snapPt)) || "true".equals(hgrid.getProperty(TdcProperties.SUNKEN_RAILROAD, snapPt));
        terrain = hgrid.getTerrainName(snapPt);
        if (terrain.equals(TdcProperties.TERRAIN_BOCAGE)) {
          terrain = TdcProperties.TERRAIN_HEDGEROW;
        }

        // Convert to Beach terrain if hex is on overlay beach hex and the overlay is active        
        final String overlay = hgrid.getProperty(TdcProperties.OVERLAY, snapPt); // Is hex on an overlay?
        final boolean isBeach = "true".equals(hgrid.getProperty(TdcProperties.TERRAIN_BEACH, snapPt)); // Is it an overlay beach hex?     
        if (isBeach) {
//          if (overlay.startsWith("Utah")) {
//            final String overlay_level = (String) GameModule.getGameModule().getProperty("Overlay-Utah");
//            if (("2".equals(overlay_level) && "Utah-pl".equals(overlay)) || ("3".equals(overlay_level) && "Utah-act".equals(overlay))) { // Is overlay active?
//              terrain = TdcProperties.TERRAIN_BEACH; // Convert to Beach terrain
//            }
//          }
//          else {
            final String overlay_level = (String) GameModule.getGameModule().getProperty("Overlay-" + overlay);
            if ("2".equals(overlay_level)) { // Is overlay active?
              terrain = TdcProperties.TERRAIN_BEACH; // Convert to Beach terrain
            }
//          }
        }

        final String basicTerrain = terrain;

        if (terrain == null || terrain.equals("null")) {
          terrain = "";
        }
        else {
          terrain = " " + terrain;
        }
        if (op && (!rubble || (!TdcProperties.CITY.equals(basicTerrain) && !TdcProperties.FORTIFIED.equals(basicTerrain))))
          terrain += ", OP";
        if (strongpoint && (!rubble || !TdcProperties.FORTIFIED.equals(basicTerrain)))
          terrain += ", Strongpoint";
        if (hill)
          terrain += ", Hill";
        if (raised)
          terrain += ", Raised Road/RR";
        if (embank)
          terrain += ", Embankment Road/RR";
        if (sunken)
          terrain += ", Sunken Road/RR";
        if (minefieldDoor)
          terrain += ", Minefield Door";
        if (inGermany)
          terrain += ", In Germany";
        if (inBirHachiem)
          terrain += ", In Bir Hachiem";

        String poi = (String) hgrid.getProperty(TdcProperties.TERRAIN_POI, snapPt);
        if (poi != null) {
          terrain += "(" + poi + ")";
        }

        time = (String) GameModule.getGameModule().getProperty(TdcProperties.TIME);
        if (time == null)
          time = "";
        weather = (String) GameModule.getGameModule().getProperty(TdcProperties.WEATHER);
        if (weather == null)
          weather = "";

        if (weather.startsWith(TdcProperties.WEATHER_FOG)) {
          weather = "Fog";
        }

        // if (time.equals(TdcProperties.TIME_NIGHT)) {
        // terrain += ", Night";
        // }
        //
        // if (weather.length() > 0 &&
        // !TdcProperties.WEATHER_CLEAR.equals(weather)) {
        // terrain += ", " + weather;
        // }
      }
    }

    if (locationName == null) {
      locationName = map.locationName(snapPt) + terrain;
    }
    else {
      locationName += terrain + " - Mass: " + steps;
    }

    final Font f = new Font(Font.DIALOG, Font.PLAIN, fontSize);
    g.setFont(f);

    // If
    if (! displayablePieces.isEmpty()) {
      x += 10 + 75 * displayablePieces.size() * os_scale;  
    }
    
    drawLabel(g, new Point(x, y), locationName, LabelUtils.RIGHT, LabelUtils.BOTTOM);

    int xx = x;

    // Don't display weather in Crete, always the same
    xx += g.getFontMetrics().stringWidth(locationName + "  ") + 2;
    if (weather != null && !UnitInfo.isCreteRules()) {
      drawLabel(g, new Point(xx, y), weather, LabelUtils.RIGHT, LabelUtils.BOTTOM, getWeatherFg(weather), getWeatherBg(weather));
      xx += g.getFontMetrics().stringWidth(weather + "  ") + 2;
    }

    if (TdcProperties.TIME_NIGHT.equals(time)) {
      drawLabel(g, new Point(xx, y), time, LabelUtils.RIGHT, LabelUtils.BOTTOM, Color.white, Color.black);
    }

  }

  protected static Color getWeatherFg(String weather) {
    if (TdcProperties.WEATHER_RAIN.equals(weather) || TdcProperties.WEATHER_OVERCAST.equals(weather)) {
      return Color.white;
    }
    else {
      return Color.black;
    }
  }

  protected static Color getWeatherBg(String weather) {
    if (TdcProperties.WEATHER_CLEAR.equals(weather)) {
      return new Color(144, 190, 217);
    }
    else if (TdcProperties.WEATHER_FOG.equals(weather)) {
      return Color.lightGray;
    }
    else if (TdcProperties.WEATHER_MIST.equals(weather)) {
      return Color.lightGray;
    }
    else if (TdcProperties.WEATHER_RAIN.equals(weather)) {
      return Color.blue;
    }
    else if (TdcProperties.WEATHER_OVERCAST.equals(weather)) {
      return Color.darkGray;
    }
    else if (TdcProperties.WEATHER_SANDSTORM.equals(weather)) {
      return Color.orange;
    }
    else if (TdcProperties.WEATHER_STORM.equals(weather)) {
      return Color.darkGray;
    }
    else {
      return Color.white;
    }
  }

  @Deprecated
  // Required for backward compatibility
  protected void drawLabel(Graphics g, Point pt, String label) {
    drawLabel(g, pt, label, LabelUtils.RIGHT, LabelUtils.BOTTOM);
  }

  protected void drawLabel(Graphics g, Point pt, String label, int hAlign, int vAlign, Color fg, Color bg) {

    if (label != null) {
      Graphics2D g2d = ((Graphics2D) g);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
      LabelUtils.drawLabel(g, label, pt.x, pt.y, new Font(Font.DIALOG, Font.PLAIN, fontSize), hAlign, vAlign, fg, bg, Color.black);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

  }

  protected void drawLabel(Graphics g, Point pt, String label, int hAlign, int vAlign) {
    drawLabel(g, pt, label, hAlign, vAlign, fgColor, bgColor);
  }

  protected void showDetails() {

    displayablePieces = getDisplayablePieces();

    /*
     * Visibility Rules: Stack - Depends on setting of showGraphics/showText
     * Single Unit - Depends on setting of showGraphics/showText and
     * showGraphicsSingle/showTextSingle and stack must not be expanded. Empty
     * space - Depends on setting of
     */

    double zoom = getZoom();
    if (displayablePieces.size() < minimumDisplayablePieces) {
      if (displayablePieces.size() > 0) {
        graphicsVisible = zoom < zoomLevel;
        textVisible = zoom < zoomLevel && (summaryReportFormat.getFormat().length() > 0 || counterReportFormat.getFormat().length() > 0);
      }
      else {
        textVisible = (minimumDisplayablePieces == 0 && emptyHexReportFormat.getFormat().length() > 0);
        graphicsVisible = false;
      }
    }
    else {
      graphicsVisible = drawPieces;
      textVisible = showText && (summaryReportFormat.getFormat().length() > 0 || counterReportFormat.getFormat().length() > 0);
    }

    // For VP display, force text off, graphics on to get hover
    if (displayablePieces.size() > 0) {
      final GamePiece p = displayablePieces.get(0);
      if (p.getMap() != null && "VP".equals(p.getMap().getMapName())) {
        textVisible = false;
        graphicsVisible = true;
      }
    }

    map.repaint();
  }

  protected double getZoom() {
    return map.getZoom();
  }

  /**
   * Build an ArrayList of pieces to be displayed in order from bottom up, based
   * on selection criteria setup in config.
   */
  protected List<GamePiece> getDisplayablePieces() {
    rubble = false;
    GamePiece[] allPieces = map.getPieces(); // All pieces from bottom up

    Visitor visitor = new Visitor(new Filter(), map, map.componentToMap(currentMousePosition.getPoint()));
    DeckVisitorDispatcher dispatcher = new DeckVisitorDispatcher(visitor);

    /*
     * Process pieces from the top down to make it easier to check for top layer
     * only.
     */
    for (int i = allPieces.length - 1; i >= 0; i--) {
      dispatcher.accept(allPieces[i]);
    }

    return visitor.getPieces();
  }

  /**
   * Utility class to select the pieces we wish to view.
   */
  protected class Filter implements PieceFilter {

    protected int topLayer;

    public Filter() {
      topLayer = -1;
    }

    public boolean accept(GamePiece piece) {
      return accept(piece, 0, "");
    }

    public boolean accept(GamePiece piece, int layer, String layerName) {

      // Is it visible to us?
      if (Boolean.TRUE.equals(piece.getProperty(Properties.INVISIBLE_TO_ME))) {
        return false;
      }

      // If it Does Not Stack, do we want to see it?
      if (Boolean.TRUE.equals(piece.getProperty(Properties.NO_STACK)) && !showNoStack) {
        return false;
      }

      if (Boolean.TRUE.equals(piece.getProperty(Properties.NON_MOVABLE)) && !showNonMovable) {
        return false;
      }

      if (Boolean.TRUE.equals(piece.getProperty(Properties.TERRAIN)) && !showMoveSelected) {
        return false;
      }

      // Deck?
      if (piece.getParent() instanceof Deck && !showDeck) {
        return false;
      }

      // Select by property filter
      if (displayWhat.equals(FILTER)) {
        return propertyFilter.accept(piece);
      }

      // Looking at All Layers accepts anything.
      else if (displayWhat.equals(ALL_LAYERS)) {
        return true;
      }
      else {

        if (topLayer < 0) {
          topLayer = layer;
        }

        // Pieces are passed to us top down, so only display the top-most layer
        if (displayWhat.equals(TOP_LAYER)) {
          return layer == topLayer;
        }

        // Include pieces on named layers only
        else if (displayWhat.equals(INC_LAYERS)) {
          for (String displayLayer : displayLayers) {
            if (layerName.equals(displayLayer)) {
              return true;
            }
          }
        }

        // Exclude pieces from named layers.
        else if (displayWhat.equals(EXC_LAYERS)) {
          for (String displayLayer : displayLayers) {
            if (layerName.equals(displayLayer)) {
              return false;
            }
          }
          return true;
        }
      }

      // Ignore anything else
      return false;
    }

  }

  /*
   * Utility class to visit Map pieces, apply the filter and return a list of
   * pieces we are interested in.
   */
  protected static class Visitor extends PieceFinder.Movable {
    protected List<GamePiece> pieces;
    protected Filter filter;
    protected CompoundPieceCollection collection;
    protected int lastLayer = -1;
    protected int insertPos = 0;
    protected Point foundPieceAt;

    public Visitor(Filter filter, Map map, Point pt) {
      super(map, pt);
      if (map.getPieceCollection() instanceof CompoundPieceCollection) {
        collection = (CompoundPieceCollection) map.getPieceCollection();
      }
      pieces = new ArrayList<>();
      this.filter = filter;
    }

    public Object visitDeck(Deck d) {
      if (foundPieceAt == null) {
        GamePiece top = d.topPiece();
        if (top != null && !Boolean.TRUE.equals(top.getProperty(Properties.OBSCURED_TO_ME))) {
          Rectangle r = (Rectangle) d.getShape();
          r.x += d.getPosition().x;
          r.y += d.getPosition().y;
          if (r.contains(pt)) {
            apply(top);
          }
        }
      }
      return null;
    }

    public Object visitStack(Stack s) {
      boolean addContents = foundPieceAt == null ? super.visitStack(s) != null : foundPieceAt.equals(s.getPosition());
      if (addContents) {
        for (Iterator<GamePiece> i = s.getPiecesIterator(); i.hasNext();) {
          apply(i.next());
        }
      }
      return null;
    }

    public Object visitDefault(GamePiece p) {
      if (foundPieceAt == null ? super.visitDefault(p) != null : foundPieceAt.equals(p.getPosition())) {
        apply(p);
      }
      return null;
    }

    /*
     * Insert accepted pieces into the start of the array since we are being
     * passed pieces from the top down.
     */
    protected void apply(GamePiece p) {
      int layer;
      String layerName;

      layer = collection.getLayerForPiece(p);
      layerName = collection.getLayerNameForPiece(p);

      if (filter == null || filter.accept(p, layer, layerName)) {
        if (layer != lastLayer) {
          insertPos = 0;
          lastLayer = layer;
        }

        if (foundPieceAt == null) {
          foundPieceAt = p.getPosition();
        }

        pieces.add(insertPos++, p);
      }
    }

    public List<GamePiece> getPieces() {
      return pieces;
    }
  }

  public void mouseMoved(MouseEvent e) {
    // clear details when mouse moved
    if (graphicsVisible || textVisible) {
      hideDetails();
    }
    else {
      currentMousePosition = e;

      if (Boolean.FALSE.equals(GameModule.getGameModule().getPrefs().getValue(USE_KEYBOARD))) {

        // Restart timer
        if (delayTimer.isRunning())
          delayTimer.stop();
        delayTimer.setInitialDelay(getPreferredDelay());
        delayTimer.start();
      }
    }
  }

  protected int getPreferredDelay() {
    return (Integer) GameModule.getGameModule().getPrefs().getValue(PREFERRED_DELAY);
  }

  public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    mouseInView = true;
  }

  public void mouseExited(MouseEvent e) {
    mouseInView = false;
  }

  public void mousePressed(MouseEvent e) {
    if (delayTimer.isRunning())
      delayTimer.stop();
  }

  public void mouseReleased(MouseEvent e) {
    mouseInView = true;
    if (delayTimer.isRunning())
      delayTimer.stop();
  }

  public void dragMouseMoved(DragSourceDragEvent e) {
    // This prevents the viewer from popping up during piece drags.
    if (delayTimer.isRunning())
      delayTimer.stop();
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    if (hotkey != null && Boolean.TRUE.equals(GameModule.getGameModule().getPrefs().getValue(USE_KEYBOARD))) {
      if (hotkey.equals(KeyStroke.getKeyStrokeForEvent(e))) {
        showDetails();
      }
      else {
        hideDetails();
      }
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  protected void hideDetails() {
    graphicsVisible = false;
    textVisible = false;
    map.repaint();
  }

  /*
   * Compatibility. If this component has not yet been saved by this version of
   * vassal, convert the old-style options to new and update the version.
   */
  public Configurer getConfigurer() {

    // New version 2 viewer being created
    if (map == null) {
      version = LATEST_VERSION;
    }
    // Previous version needing upgrading?
    else if (!version.equals(LATEST_VERSION)) {
      upgrade();
    }
    return super.getConfigurer();
  }

  protected void upgrade() {

    if (!drawPieces && !showText) {
      minimumDisplayablePieces = Integer.MAX_VALUE;
    }
    else if (drawSingleDeprecated) {
      minimumDisplayablePieces = 1;
    }
    else {
      minimumDisplayablePieces = 2;
    }

    fgColor = map.getHighlighter() instanceof ColoredBorder ? ((ColoredBorder) map.getHighlighter()).getColor() : Color.black;

    bgColor = new Color(255 - fgColor.getRed(), 255 - fgColor.getGreen(), 255 - fgColor.getBlue());

    version = LATEST_VERSION;
  }

  public String[] getAttributeNames() {
    return new String[] { VERSION, DELAY, HOTKEY, BG_COLOR, FG_COLOR, MINIMUM_DISPLAYABLE, ZOOM_LEVEL, DRAW_PIECES, DRAW_PIECES_AT_ZOOM, GRAPH_SINGLE_DEPRECATED, BORDER_WIDTH, SHOW_TEXT, SHOW_TEXT_SINGLE_DEPRECATED, FONT_SIZE,
        SUMMARY_REPORT_FORMAT, COUNTER_REPORT_FORMAT, EMPTY_HEX_REPORT_FORMAT, DISPLAY, LAYER_LIST, PROPERTY_FILTER, SHOW_NOSTACK, SHOW_MOVE_SELECTED, SHOW_NON_MOVABLE, UNROTATE_PIECES, SHOW_DECK };
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Version", // Not displayed
        "Recommended Delay before display (ms):  ", "Keyboard shortcut to display:  ", "Background color:  ", "Border/text color:  ", "Display when at least this many pieces will be included:  ",
        "Always display when zoom level less than:  ", "Draw pieces?", "Draw pieces using zoom factor:  ", "Display unit graphics for single counter?", // Obsolete
        "Width of gap between pieces:  ", "Display text?", "Display text report for single counter?", // Obsolete
        "Font size:  ", "Summary text above pieces:  ", "Text below each piece:  ", "Text for empty location:  ", "Include individual pieces:  ", "Listed layers", "Piece selection property filter:  ", "Include non-stacking pieces?",
        "Include move-when-selected pieces?", "Include non-movable pieces?", "Show pieces in un-rotated state?", "Include top piece in Deck?" };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] { String.class, Integer.class, KeyStroke.class, Color.class, Color.class, MinConfig.class, Double.class, Boolean.class, Double.class, Boolean.class, Integer.class, Boolean.class, Boolean.class, Integer.class,
        ReportFormatConfig.class, CounterFormatConfig.class, EmptyFormatConfig.class, DisplayConfig.class, String[].class, PropertyExpression.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class };
  }

  public static class DisplayConfig extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { TOP_LAYER, ALL_LAYERS, INC_LAYERS, EXC_LAYERS, FILTER };
    }
  }

  public static class MinConfig extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { "0", "1", "2" };
    }
  }

  public static class EmptyFormatConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new FormattedStringConfigurer(key, name, new String[] { BasicPiece.LOCATION_NAME, BasicPiece.CURRENT_MAP, BasicPiece.CURRENT_BOARD, BasicPiece.CURRENT_ZONE });
    }
  }

  public static class ReportFormatConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new FormattedStringConfigurer(key, name, new String[] { BasicPiece.LOCATION_NAME, BasicPiece.CURRENT_MAP, BasicPiece.CURRENT_BOARD, BasicPiece.CURRENT_ZONE, SUM });
    }
  }

  public static class CounterFormatConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new FormattedStringConfigurer(key, name, new String[] { BasicPiece.PIECE_NAME });
    }
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("Map.htm", "StackViewer");
  }

  public void removeFrom(Buildable parent) {
    map.removeDrawComponent(this);
    view.removeMouseMotionListener(this);
  }

  public void setAttribute(String name, Object value) {
    if (DELAY.equals(name)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      if (value != null) {
        delay = (Integer) value;
      }
    }
    else if (HOTKEY.equals(name)) {
      if (value instanceof String) {
        hotkey = HotKeyConfigurer.decode((String) value);
      }
      else {
        hotkey = (KeyStroke) value;
      }
    }
    else if (DRAW_PIECES.equals(name)) {
      if (value instanceof Boolean) {
        drawPieces = (Boolean) value;
      }
      else if (value instanceof String) {
        drawPieces = "true".equals(value);
      }
    }
    else if (GRAPH_SINGLE_DEPRECATED.equals(name)) {
      if (value instanceof Boolean) {
        drawSingleDeprecated = (Boolean) value;
      }
      else if (value instanceof String) {
        drawSingleDeprecated = "true".equals(value);
      }
    }
    else if (SHOW_TEXT.equals(name)) {
      if (value instanceof Boolean) {
        showText = (Boolean) value;
      }
      else if (value instanceof String) {
        showText = "true".equals(value);
      }

    }
    else if (SHOW_TEXT_SINGLE_DEPRECATED.equals(name)) {
      if (value instanceof Boolean) {
        showTextSingleDeprecated = (Boolean) value;
      }
      else if (value instanceof String) {
        showTextSingleDeprecated = "true".equals(value);
      }
    }
    else if (ZOOM_LEVEL.equals(name)) {
      if (value instanceof String) {
        value =  Double.parseDouble((String) value);
      }
      zoomLevel = (Double) value;
    }
    else if (DRAW_PIECES_AT_ZOOM.equals(name)) {
      if (value instanceof String) {
        value = Double.parseDouble((String) value);
      }
      graphicsZoomLevel = (Double) value;
    }
    else if (BORDER_WIDTH.equals(name)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      borderWidth = (Integer) value;
    }
    else if (SHOW_NOSTACK.equals(name)) {
      if (value instanceof Boolean) {
        showNoStack = (Boolean) value;
      }
      else if (value instanceof String) {
        showNoStack = "true".equals(value);
      }
    }
    else if (SHOW_MOVE_SELECTED.equals(name)) {
      if (value instanceof Boolean) {
        showMoveSelected = (Boolean) value;
      }
      else if (value instanceof String) {
        showMoveSelected = "true".equals(value);
      }
    }
    else if (SHOW_NON_MOVABLE.equals(name)) {
      if (value instanceof Boolean) {
        showNonMovable = (Boolean) value;
      }
      else if (value instanceof String) {
        showNonMovable = "true".equals(value);
      }
    }
    else if (SHOW_DECK.equals(name)) {
      if (value instanceof Boolean) {
        showDeck = (Boolean) value;
      }
      else if (value instanceof String) {
        showDeck = "true".equals(value);
      }
    }
    else if (UNROTATE_PIECES.equals(name)) {
      if (value instanceof Boolean) {
        unrotatePieces = (Boolean) value;
      }
      else if (value instanceof String) {
        unrotatePieces = "true".equals(value);
      }
    }
    else if (DISPLAY.equals(name)) {
      displayWhat = (String) value;
    }
    else if (LAYER_LIST.equals(name)) {
      if (value instanceof String) {
        value = StringArrayConfigurer.stringToArray((String) value);
      }
      displayLayers = (String[]) value;
    }
    else if (EMPTY_HEX_REPORT_FORMAT.equals(name)) {
      emptyHexReportFormat.setFormat((String) value);
    }
    else if (SUMMARY_REPORT_FORMAT.equals(name)) {
      summaryReportFormat.setFormat((String) value);
    }
    else if (COUNTER_REPORT_FORMAT.equals(name)) {
      counterReportFormat.setFormat((String) value);
    }
    else if (MINIMUM_DISPLAYABLE.equals(name)) {
      try {
        minimumDisplayablePieces = Integer.parseInt((String) value);
      }
      catch (NumberFormatException e) {
        throw new IllegalBuildException(e);
      }
    }
    else if (VERSION.equals(name)) {
      version = (String) value;
    }
    else if (FG_COLOR.equals(name)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      fgColor = value == null ? Color.black : (Color) value;
    }
    else if (BG_COLOR.equals(name)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      bgColor = (Color) value;
    }
    else if (FONT_SIZE.equals(name)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      if (value != null) {
        fontSize = (Integer) value;
      }
    }
    else if (PROPERTY_FILTER.equals(name)) {
      propertyFilter.setExpression((String) value);
    }
  }

  public String getAttributeValueString(String name) {
    if (DELAY.equals(name)) {
      return String.valueOf(delay);
    }
    else if (HOTKEY.equals(name)) {
      return HotKeyConfigurer.encode(hotkey);
    }
    else if (DRAW_PIECES.equals(name)) {
      return String.valueOf(drawPieces);
    }
    else if (GRAPH_SINGLE_DEPRECATED.equals(name)) {
      return String.valueOf(drawSingleDeprecated);
    }
    else if (SHOW_TEXT.equals(name)) {
      return String.valueOf(showText);
    }
    else if (SHOW_TEXT_SINGLE_DEPRECATED.equals(name)) {
      return String.valueOf(showTextSingleDeprecated);
    }
    else if (ZOOM_LEVEL.equals(name)) {
      return String.valueOf(zoomLevel);
    }
    else if (DRAW_PIECES_AT_ZOOM.equals(name)) {
      return String.valueOf(graphicsZoomLevel);
    }
    else if (BORDER_WIDTH.equals(name)) {
      return String.valueOf(borderWidth);
    }
    else if (SHOW_NOSTACK.equals(name)) {
      return String.valueOf(showNoStack);
    }
    else if (SHOW_MOVE_SELECTED.equals(name)) {
      return String.valueOf(showMoveSelected);
    }
    else if (SHOW_NON_MOVABLE.equals(name)) {
      return String.valueOf(showNonMovable);
    }
    else if (SHOW_DECK.equals(name)) {
      return String.valueOf(showDeck);
    }
    else if (UNROTATE_PIECES.equals(name)) {
      return String.valueOf(unrotatePieces);
    }
    else if (DISPLAY.equals(name)) {
      return displayWhat;
    }
    else if (LAYER_LIST.equals(name)) {
      return StringArrayConfigurer.arrayToString(displayLayers);
    }
    else if (EMPTY_HEX_REPORT_FORMAT.equals(name)) {
      return emptyHexReportFormat.getFormat();
    }
    else if (SUMMARY_REPORT_FORMAT.equals(name)) {
      return summaryReportFormat.getFormat();
    }
    else if (COUNTER_REPORT_FORMAT.equals(name)) {
      return counterReportFormat.getFormat();
    }
    else if (MINIMUM_DISPLAYABLE.equals(name)) {
      return String.valueOf(minimumDisplayablePieces);
    }
    else if (VERSION.equals(name)) {
      return version;
    }
    else if (FG_COLOR.equals(name)) {
      return ColorConfigurer.colorToString(fgColor);
    }
    else if (BG_COLOR.equals(name)) {
      return ColorConfigurer.colorToString(bgColor);
    }
    else if (FONT_SIZE.equals(name)) {
      return String.valueOf(fontSize);
    }
    else if (PROPERTY_FILTER.equals(name)) {
      return propertyFilter.getExpression();
    }
    else
      return null;
  }

  public static String getConfigureTypeName() {
    return "Mouse-over Stack Viewer";
  }

  public VisibilityCondition getAttributeVisibility(String name) {
    if (BORDER_WIDTH.equals(name) || DRAW_PIECES_AT_ZOOM.equals(name)) {
      return () -> drawPieces;
    }
    else if (FONT_SIZE.equals(name) || SUMMARY_REPORT_FORMAT.equals(name) || COUNTER_REPORT_FORMAT.equals(name)) {
      return () -> showText;
    }
    else if (DRAW_PIECES.equals(name) || SHOW_TEXT.equals(name) || SHOW_NOSTACK.equals(name) || SHOW_DECK.equals(name) || DISPLAY.equals(name)) {
      return () -> true;
    }
    else if (LAYER_LIST.equals(name)) {
      return () -> (displayWhat.equals(INC_LAYERS) || displayWhat.equals(EXC_LAYERS));
    }
    else if (PROPERTY_FILTER.equals(name)) {
      return () -> displayWhat.equals(FILTER);
    }
    else if (EMPTY_HEX_REPORT_FORMAT.equals(name)) {
      return () -> showText && minimumDisplayablePieces == 0;
    }
    else if (SHOW_MOVE_SELECTED.equals(name) || SHOW_NON_MOVABLE.equals(name)) {
      return () -> showNoStack;
    }
    /*
     * The following fields are not to be displayed. They are either obsolete or
     * maintained for backward compatibility
     */
    else if (VERSION.equals(name) || SHOW_TEXT_SINGLE_DEPRECATED.equals(name) || GRAPH_SINGLE_DEPRECATED.equals(name)) {
      return () -> false;
    }
    return null;
  }

}