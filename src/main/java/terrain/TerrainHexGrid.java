/*
 * $Id: TerrainHexGrid.java 9302 2020-03-05 11:59:32Z swampwallaby $
 *
 * Copyright (c) 2000-2008 by Brent Easton
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
package terrain;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.round;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.HexGrid;
import VASSAL.configure.AutoConfigurer;
import VASSAL.preferences.Prefs;
import tdc.TdcProperties;

/**
 * Subclass of HexGrid supporting Terrain
 */
// FIXME: Merge into HexGrid
public class TerrainHexGrid extends HexGrid {

  protected TerrainHexGridEditor gridEditor;
  protected TerrainMap terrainMap = null;
  protected HashMap<Point, Area> shapeCache = new HashMap<>();

  protected boolean gridEditingInProgress = false;
  public TerrainHexGrid() {
    super();
  }

  public boolean isGridEditingInProgress() {
    return gridEditingInProgress;
  }

  public void setGridEditingInProgress(boolean gridEditingInProgress) {
    this.gridEditingInProgress = gridEditingInProgress;
  }

  public Board getBoard() {
    return container == null ? null : container.getBoard();
  }

  // Note x, y is in Board co-ords, not Map co-ords
  public Area getSingleHex(int x, int y) {
    return getSingleHexShape(x, y, false);
  }

  public Area getSingleHex(Point p) {
    final Point snap = snapToHex(p);
    return getSingleHex(snap.x, snap.y);
  }

  public Area getSingleHex(TerrainHex t) {
    return getSingleHexByRC(t.getColumn(), t.getRow());
  }

  public Area getSingleHexByRC(int col, int row) {
    final Point p = getHexCenter(col, row);
    return getSingleHex(p.x, p.y);
  }

  public Area getSingleFatHex(TerrainHex t) {
    return getSingleFatHex(t.getColumn(), t.getRow());
  }

  public Area getSingleFatHex(int col, int row) {
    final Point p = getHexCenter(col, row);
    Point[] points = getFatHexVertices(p.x, p.y);
    Polygon poly = new Polygon();
    for (Point point : points) {
      poly.addPoint(point.x, point.y);
    }
    poly.addPoint(points[0].x, points[0].y);
    return new Area(poly);
  }

  // Return the vertices of a fat Hex in Board co-ordinates
  public Point[] getFatHexVertices(int centerX, int centerY) {

    final int fatness = 5;

    Point[] points = new Point[6];
    boolean reversed = false;

    float x = (float) (sideways ? centerY : centerX);
    float y = (float) (sideways ? centerX : centerY);

    float x1, y1, x2, y2, x3, y3, x4, y4, x5, y5, x6, y6;

    float deltaX = (float) (this.dx);
    float deltaY = (float) (this.dy);

    float r = 2.F * deltaX / 3.F;

    for (int i = 0; i < 6; i++) {
      points[i] = new Point();
    }

    x1 = x - r;
    y1 = y;
    points[0].setLocation(round(x1) - fatness, round(y1));

    x2 = x - 0.5F * r;
    y2 = reversed ? y + 0.5F * deltaY : y - 0.5F * deltaY;
    points[1].setLocation(round(x2) - fatness, round(y2) - fatness);

    x3 = x + 0.5F * r;
    y3 = y2;
    points[2].setLocation(round(x3) + 1 + fatness, round(y3) - fatness);

    x4 = x + r;
    y4 = y;
    points[3].setLocation(round(x4) + 1 + fatness, round(y4));

    x5 = x3;
    y5 = reversed ? y - 0.5F * deltaY : y + 0.5F * deltaY;
    points[4].setLocation(round(x5) + 1 + fatness, round(y5) + 1 + fatness);

    x6 = x2;
    y6 = y5;
    points[5].setLocation(round(x6) - fatness, round(y6) + 1 + fatness);

    if (sideways) {
      for (int i = 0; i < 6; i++) {
        rotate(points[i]);
      }
    }

    return points;
  }

  /*
   * Return the terrain at the given map x,y point (in Global Map coordinates)
   */
  public String getTerrainName(Point p) {
    buildTerrainMap();
    final HexRef hexPos = getHexPos(normalize(p));
    final TerrainHex hex = terrainMap.getHexTerrain(hexPos);
    if (hex != null) {
      return hex.getTerrain().getTerrainName();
    }
    else
      return "";
  }

  protected void buildTerrainMap() {
    if (terrainMap == null) {
      terrainMap = TerrainDefinitions.getInstance().getTerrainMap(this);
    }
  }

  public TerrainMap getTerrainMap() {
    buildTerrainMap();
    return terrainMap;
  }

  protected Point normalize(Point p) {
    final Rectangle bounds = container.getBoard().bounds();
    return new Point(p.x - bounds.x, p.y - bounds.y);
  }

  /**
   * Check if property can be satisfied by this grid and if so, return the value
   * 
   * @param propName
   *          Property Name
   * @param p
   *          GamePiece position
   * @return property value
   */
  public String getProperty(String propName, Point p) {

    // Does the Property name match an Attribute Terrain definition?
    String value = null;
    final AttributeTerrain at = (AttributeTerrain) TerrainDefinitions.getInstance().getAttributeTerrainDefinitions().getTerrain(propName);
    final Point boardPos = normalize(p);
    if (at != null) {
      buildTerrainMap();
      final HexRef hexPos = getHexPos(boardPos);
      final AttrRef ref = new AttrRef(hexPos, propName);
      final TerrainAttribute attr = terrainMap.getAttributeTerrain(ref);
      value = attr == null ? at.getDefaultValue() : attr.getValue();
    }
    return value;
  }

  /*
   * Return the column,row co-ords for a hex at the given point
   */

  public HexRef getHexPos(Point p) {
    final Point snap = snapToHex(p);
    final HexRef pos = new HexRef(getGridPosition(snap));
    rotateIfSideways(pos);
    return pos;
  }

  public void rotateIfSideways(HexRef p) {
    if (sideways) {
      p.rotate();
    }
  }

  /*
   * getRawRow & getRowColumn extracted from HexGridNumbering where they do not
   * belong! Should have been in HexGrid in the first place.
   */
  public int getRawColumn(Point p) {
    p = new Point(p);
    rotateIfSideways(p);
    int x = p.x - getOrigin().x;
    x = (int) Math.floor(x / getHexWidth() + 0.5);
    return x;
  }

  public int getRawRow(Point p) {
    p = new Point(p);
    rotateIfSideways(p);
    final Point origin = getOrigin();
    final double dx = getHexWidth();
    final double dy = getHexSize();
    final int nx = (int) Math.round((p.x - origin.x) / dx);
    int ny;
    if (nx % 2 == 0) {
      ny = (int) Math.round((p.y - origin.y) / dy);
    }
    else {
      ny = (int) Math.round((p.y - origin.y - dy / 2) / dy);
    }
    return ny;
  }

  protected int getMaxRows() {
    if (sideways) {
      return (int) Math.floor(getContainer().getSize().height / getHexSize() + 0.5) + 4;
    }
    else {
      return (int) Math.floor(getContainer().getSize().height / getHexWidth() + 0.5);
    }
  }

  protected int getMaxColumns() {
    if (sideways) {
      return (int) Math.floor(getContainer().getSize().width / getHexWidth() + 0.5);
    }
    else {
      return (int) Math.floor(getContainer().getSize().width / getHexSize() + 0.5);
    }
  }

  /*
   * Return the center of the specified hex
   */
  public Point getHexCenter(int column, int row) {
    int x, y;

    if (sideways) {
      x = origin.y + (int) (dy * column) + ((row % 2 == 0) ? 0 : (int) (dy / 2));
      y = origin.x + (int) (dx * row);
    }
    else {
      x = origin.x + (int) (dx * column);
      y = origin.y - (int) (dy * 0.5) + (int) (dy * row) + ((column % 2 == 0) ? (int) (dy / 2) : (int) dy);
    }
    return new Point(x, y);
  }

  protected Point2D.Float getHexCenter2D(int column, int row) {
    float x, y;

    if (sideways) {
      x = (float) (origin.y + dy * column + ((row % 2 == 0) ? 0 : (dy / 2)));
      y = (float) (origin.x + dx * row);
    }
    else {
      x = (float) (origin.x + dx * column);
      y = (float) (origin.y - dy * 0.5 + dy * row + ((column % 2 == 0) ? dy / 2 : dy));
    }
    return new Point2D.Float(x, y);
  }

  /*
   * Return the raw Grid Reference of the hex contining the given point
   */
  protected HexRef getGridPosition(Point p) {
    return new HexRef(getRawColumn(p), getRawRow(p), this);
  }

  public Area getGridShape(Point center, int range) {
    return getGridShape(center, 1, range);
  }

  public Area getGridShape(Point center, int minRange, int range) {
    Area shape = shapeCache.get(new Point(minRange, range));
    if (shape == null) {
      // Choose a starting point
      Point origin = new Point(0, 0);
      shape = getSingleHexShape(origin.x, origin.y, false);
      
      int xMin = (int) (minRange*dx) -10;

      for (int i = -range; i <= range; i++) {
        int x = origin.x + (int) (i * dx);

        int length = range * 2 + 1 - abs(i);

        int startY;
        if (length % 2 == 1) {
          startY = origin.y - (int) (dy * (length - 1) / 2);
        }
        else {
          startY = origin.y - (int) (dy * (0.5 + (length - 2) / 2));
        }

        int y = startY;

        for (int j = 0; j < length; j++) {
      
          int dist = (int) Point.distance(0, 0, x, y);
          
          if (dist > xMin) {
            Point p = new Point(x, y);
            rotateIfSideways(p);
            shape.add(getSingleHexShape(p.x, p.y, false));         
          }
          y += dy;
        }
      }

      rotateIfSideways(origin);
      shape.transform(AffineTransform.getTranslateInstance(-origin.x, -origin.y));
      shapeCache.put(new Point(minRange, range), shape);
    }
    shape = new Area(AffineTransform.getTranslateInstance(center.x, center.y).createTransformedShape(shape));
    return shape;
  }

  /*
   * Override editGrid() to use the new Terrain GridEditor
   */
  public void editGrid() {
    setGridEditingInProgress(true);
    gridEditor = new TerrainHexGridEditor(this);
    gridEditor.setVisible(true);
    // Local variables may have been updated by GridEditor so refresh
    // configurers. Setting the Dy configurer will auto-recalculate dx
    final double origDx = dx;
    final AutoConfigurer cfg = (AutoConfigurer) getConfigurer();
    cfg.getConfigurer(DY).setValue(String.valueOf(dy));
    dx = origDx;
    cfg.getConfigurer(DX).setValue(String.valueOf(dx));
    cfg.getConfigurer(X0).setValue(String.valueOf(origin.x));
    cfg.getConfigurer(Y0).setValue(String.valueOf(origin.y));
    cfg.getConfigurer(SIDEWAYS).setValue(String.valueOf(sideways));
  }

  /*
   * Return a list of the Grid positions of adjacent hexes. Do not include hexes
   * where no part of the hex appears on the board. Argument is a GridPosition
   * (c, r), not a map position(x, y)
   */

  protected HexRef[] getAdjacentHexes(HexRef pos) {

    final HexRef[] adjacent = new HexRef[6];
    final int c = pos.getColumn();
    final int r = pos.getRow();
    int next = 0;

    if (sideways) {
      if (r % 2 == 0) {
        next = addHex(adjacent, next, c - 1, r - 1);
        next = addHex(adjacent, next, c, r - 1);
        next = addHex(adjacent, next, c - 1, r);
        next = addHex(adjacent, next, c + 1, r);
        next = addHex(adjacent, next, c - 1, r + 1);
        next = addHex(adjacent, next, c, r + 1);
      }
      else {
        next = addHex(adjacent, next, c, r - 1);
        next = addHex(adjacent, next, c + 1, r - 1);
        next = addHex(adjacent, next, c - 1, r);
        next = addHex(adjacent, next, c + 1, r);
        next = addHex(adjacent, next, c, r + 1);
        next = addHex(adjacent, next, c + 1, r + 1);
      }
    }
    else {
      if (c % 2 == 0) {
        next = addHex(adjacent, next, c - 1, r - 1);
        next = addHex(adjacent, next, c, r - 1);
        next = addHex(adjacent, next, c + 1, r - 1);
        next = addHex(adjacent, next, c - 1, r);
        next = addHex(adjacent, next, c, r + 1);
        next = addHex(adjacent, next, c + 1, r);
      }
      else {
        next = addHex(adjacent, next, c - 1, r);
        next = addHex(adjacent, next, c, r - 1);
        next = addHex(adjacent, next, c + 1, r);
        next = addHex(adjacent, next, c - 1, r + 1);
        next = addHex(adjacent, next, c, r + 1);
        next = addHex(adjacent, next, c + 1, r + 1);
      }
    }

    return adjacent;
  }

  /*
   * Add Hex to array if it is the map
   */
  protected int addHex(HexRef[] points, int next, int column, int row) {
    if (column >= -1 && column <= getMaxColumns() + 1 && row >= -1 && row <= getMaxRows() + 1) {
      points[next++] = new HexRef(column, row, this);
    }
    return next;
  }

  /*
   * 2D Snapping
   */

  public Point2D.Float snapToHexVertex2D(Point2D.Float p) {

    p = new Point2D.Float(p.x, p.y);
    rotateIfSideways2D(p);

    float x = vertexX2D(p.x, p.y);
    float y = vertexY2D(p.x, p.y);

    p.setLocation(x, y);
    rotateIfSideways2D(p);

    return p;
  }

  public void rotate2D(Point2D.Float p) {
    float swap = p.x;
    p.x = p.y;
    p.y = swap;
  }

  public void rotateIfSideways2D(Point2D.Float p) {
    if (sideways) {
      rotate2D(p);
    }
  }

  protected float vertexX2D(float x, float y) {
    int ny = (int) floor((y - origin.y + dy / 4) * 2 / dy);
    if (ny % 2 == 0) {
      double dd = x - origin.x + dx / 3;
      float result = ((float) (2 * dx / 3 * (int) (floor(x - origin.x + dx / 3) * 3 / (2 * dx)) + origin.x));

      // Fix issue with edges crossing border of map
      if (dd < 0) {
        return  result - ( 2.0f * (float) dx / 3.0f);
      }

      return (x - origin.x + dx / 3) >= 0 ? result : -result;
    }
    else {
      return ((float) (2 * dx / 3 * (int) (floor(x - origin.x + dx / 3 + dx / 3) * 3 / (2 * dx)) - (int) (dx / 3) + origin.x));
    }
  }

  protected float vertexY2D(float x, float y) {
    return (float) (dy / 2 * (float) floor((y - origin.y + dy / 4) * 2 / dy) + origin.y);
  }
  
  // Handle snapping of Bridge Destroyed counters
  public static boolean forceEdgeSnap = false;
  
  public static void setForceEdgeSnap (boolean b) {
    forceEdgeSnap = b;
  }
  
  public Point snapTo(Point p) {
    if (! snapTo) {
      return p;
    }
    
    if (forceEdgeSnap) {
      return snapToHexSide(p);
    }
    
    Point center = snapToHex(p);

    if (edgesLegal && cornersLegal) {
      Point edge = snapToHexSide(p);
      Point vertex = snapToHexVertex(p);
      if ((p.x - edge.x) * (p.x - edge.x)
          + (p.y - edge.y) * (p.y - edge.y)
          < (p.x - vertex.x) * (p.x - vertex.x)
          + (p.y - vertex.y) * (p.y - vertex.y)) {
        return checkCenter(center, edge);
      }
      else {
        return checkCenter(center, vertex);
      }
    }
    else if (edgesLegal) {
      return checkCenter(center, snapToHexSide(p));
    }
    else if (cornersLegal) {
      return checkCenter(center, snapToHexVertex(p));
    }
    else {
      return snapToHex(p);
    }
  }

  @Override
  public boolean isVisible() {
    final boolean vis = super.isVisible();
    if (isGridEditingInProgress()) {
      return vis;
    }
    return true;
  }


  @Override
  public void draw(Graphics g, Rectangle bounds, Rectangle visibleRect, double zoom, boolean reversed) {
    super.draw(g, bounds, visibleRect, zoom, reversed);
    highlightTerrain(g, visibleRect, zoom);
  }

  @Override
  public void forceDraw(Graphics g, Rectangle bounds, Rectangle visibleRect, double zoom, boolean reversed) {
    super.forceDraw(g, bounds, visibleRect, zoom, reversed);
  }


  /*
    Grids are only drawn once per game per zoom level, and cached into the Tiles, so not bothering to over-optimize.    
   */
  public void highlightTerrain(Graphics g, Rectangle visibleRect, double zoom) {
    final Prefs prefs = GameModule.getGameModule().getPrefs();

    final boolean streamHighlight = "true".equals(prefs.getStoredValue(TdcProperties.TERRAIN_STREAM + TerrainHighlightMenu.HIGHLIGHT));
    final boolean riverHighlight = "true".equals(prefs.getStoredValue(TdcProperties.TERRAIN_RIVER + TerrainHighlightMenu.HIGHLIGHT));
    final boolean crestHighlight = "true".equals(prefs.getStoredValue(TdcProperties.TERRAIN_CREST + TerrainHighlightMenu.HIGHLIGHT));
    final boolean ridgeHighlight = "true".equals(prefs.getStoredValue(TdcProperties.TERRAIN_RIDGE + TerrainHighlightMenu.HIGHLIGHT));

    final Color streamColor = (Color) prefs.getValue(TdcProperties.TERRAIN_STREAM + "Color");
    final Color riverColor = (Color) prefs.getValue(TdcProperties.TERRAIN_RIVER + "Color");
    final Color crestColor = (Color) prefs.getValue(TdcProperties.TERRAIN_CREST + "Color");
    final Color ridgeColor = (Color) prefs.getValue(TdcProperties.TERRAIN_RIDGE + "Color");

    if (streamHighlight) {
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_STREAM);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_CREST_STREAM, crestHighlight ? null : streamColor);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIDGE_STREAM, ridgeHighlight ? null : streamColor);
    }

    if (riverHighlight) {
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIVER);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_CREST_RIVER, crestHighlight ? null : riverColor);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIDGE_RIVER, ridgeHighlight ? null : riverColor);
    }

    if (ridgeHighlight) {
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIDGE);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIDGE_STREAM, streamHighlight ? null : ridgeColor);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_RIDGE_RIVER,  riverHighlight ? null : ridgeColor);
    }

    if (crestHighlight) {
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_CREST);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_CREST_STREAM, streamHighlight ? null : crestColor);
      highlightEdges(g, visibleRect, zoom, TdcProperties.TERRAIN_CREST_RIVER,  riverHighlight ? null : crestColor);
    }

  }

  final Map<String, GeneralPath> polys = new HashMap<>();

  public void highlightEdges (Graphics g, Rectangle visibleRect, double zoom, String terrainName) {
    highlightEdges(g, visibleRect, zoom, terrainName, null);
  }

  public void highlightEdges (Graphics g, Rectangle visibleRect, double zoom, String terrainName, Color overrideColor) {
    final Graphics2D g2 = (Graphics2D) g;

    final Color oldColor = g2.getColor();
    final Composite oldComposite = g2.getComposite();
    final Shape oldClip = g2.getClip();
    final Stroke oldStroke = g2.getStroke();

    final Prefs prefs = GameModule.getGameModule().getPrefs();
    GeneralPath poly = polys.get(terrainName);
    if (poly == null) {
      poly = getTerrainMap().getEdgePoly(terrainName);
      polys.put(terrainName, poly);
    }
    if (poly == null) return;

    final Shape zoomed = poly.createTransformedShape(AffineTransform.getScaleInstance(zoom, zoom));
    final Color color = overrideColor == null ? (Color) prefs.getValue(terrainName + "Color") : overrideColor;
    final float transparency = ((int) prefs.getValue(terrainName + "Transparency")) / 100.0f;

    g2.setClip(visibleRect);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
    g2.setColor(color);
    g2.setStroke(new BasicStroke(8.0f * (float) zoom, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(zoomed);

    g2.setComposite(oldComposite);
    g2.setColor(oldColor);
    g2.setClip(oldClip);
    g2.setStroke(oldStroke);

  }
}