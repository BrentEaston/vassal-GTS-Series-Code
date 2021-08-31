/*
 * $Id: gradient.java 9192 2015-05-16 01:24:32Z swampwallaby $
 *
 * Copyright (c) 2015 by Brent Easton
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import terrain.TerrainHexGrid;
import VASSAL.build.module.map.boardPicker.board.mapgrid.HexGridNumbering;

/**
 * A class representing a Line of Sight on a map. Main purpose is to determine
 * of the LOS runs along hex edges or not Co-ordinate system is Map Co-ordinates
 * 
 * @author Brent
 * 
 */
public class LOS {

  protected TdcMap map;
  protected Point p1, p2;
  protected Gradient gradient;
  protected Rectangle bounds;
  protected Area shape;

  public LOS(TdcMap map, Point p1, Point p2) {

    this.map = map;
    this.p1 = p1;
    this.p2 = p2;
    gradient = new Gradient(p1, p2);
    bounds = new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p2.x - p1.x), Math.abs(p2.y - p1.y));

  }

  public String toString() {
    return "LOS from "+p1.x+","+p1.y+" to "+p2.x+","+p2.y;
  }
  
  public Point getStart() {
    return p1;
  }

  public Point getEnd() {
    return p2;
  }

  private boolean isEven( int i) {
    return (i & 1) == 0;
  }

  private boolean isSameParity(int a, int b) {
    return (isEven(a) && isEven(b)) || (!isEven(a) && !isEven(b));
  }

  public boolean isSpinal() {

    boolean spinal = false;

    final TerrainHexGrid grid = map.findTerrainHexGrid(p1);
    if (grid == null)
      return spinal;

    final int col1 = grid.getRawColumn(p1);
    final int col2 = grid.getRawColumn(p2);
    final int row1 = grid.getRawRow(p1);
    final int row2 = grid.getRawRow(p2);
       
    final int cd = col2 - col1;
    final int rd = row2 - row1;

    // Spinal if rows are equal and columns are same parity
    if (row1 == row2 && isSameParity(col1, col2)) {
      spinal = true;
    }

    // From and to columns both even or both odd
    else if (isSameParity(col1, col2)) {
      spinal = ((2 * rd) == (3 * cd)) || ((2 * rd) == (3 * -cd));
    }

    // From even to odd
    else if (isEven(col1) &&!isEven(col2)) {
      spinal = ((2 * rd + 1) == (3 * cd)) || ((2 * rd + 1) == (3 * -cd));
    }

    // From odd to even
    else {
      spinal = ((2 * rd - 1) == (3 * cd)) || ((2 * rd - 1) == (3 * -cd));
    }


    return spinal;
  }

  // A Point is on the line if it is within the bounding box of the LOS and a
  // line from the LOS origin to the point has the same gradient as the LOS
  public boolean isOnLine(Point p) {

    double dist = Line2D.ptLineDistSq(p1.x, p1.y, p2.x, p2.y, p.x, p.y);
    return Math.abs(dist) < 4f;

  }

  /*
   * Area's can only be intersected with other areas, not a line, so turn this LOS
   * into a very narrow Area
   */
  public Area getLosShape() {
    if (shape == null) {
      final GeneralPath path = new GeneralPath();
      path.moveTo(p1.x, p1.y);
      path.lineTo(p1.x - .1f, p1.y - .1f);
      path.lineTo(p2.x - .1f, p2.y - .1f);
      path.lineTo(p2.x, p2.y);
          
      path.closePath();
      shape = new Area(path);
    }
    return shape;
  }
  
  /*
   * Does this LOS intersect the supplied Area?
   */
  public boolean intersects (Area a) {
    final Area los = new Area(getLosShape());
    los.intersect(a);
    return ! los.isEmpty();    
  }
  
  public boolean intersects (float x1, float y1, float x2, float y2) {
    return Line2D.linesIntersect(x1, y1, x2, y2, getStart().x, getStart().y, getEnd().x, getEnd().y);
  }
}
