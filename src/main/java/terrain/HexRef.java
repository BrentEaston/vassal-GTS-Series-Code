/*
 * $Id: HexRef.java 9206 2015-11-21 10:27:43Z swampwallaby $
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

import java.awt.Point;
import java.awt.geom.Point2D;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;

/**
 * Wrapper around a Point to represent a Hex Grid reference.
 * A HexRef is defined as a row/column reference to the Hex
 *
 */
public class HexRef implements Comparable<HexRef> {

  protected int column;
  protected int row;
  protected Point2D.Float center;
  protected TerrainHexGrid myGrid;
  
  public HexRef() {
    
  }
  
  public HexRef (int c, int r, Point2D.Float p, TerrainHexGrid g) {
    column = c;
    row = r;    
    center = p;
    myGrid = g;
  }
  
  public HexRef(int c, int r, TerrainHexGrid g) {
    this(c, r, null, g);
  }
  
  public HexRef(int c, int r) {
    this(c, r, null);
  }  
   
  public HexRef(HexRef ref) {
    this(ref.getColumn(), ref.getRow(), ref.getGrid());
  }
  
  public HexRef(HexRef ref, Point2D.Float c) {
    this(ref);
    center = c;
  }
  
  public TerrainHexGrid getGrid() {
    return myGrid;
  }
  
  public void setGrid(TerrainHexGrid g) {
    myGrid = g;
  }
  
  public int getColumn() {
    return column;
  }
  
  public int getRow() {
    return row;
  }
  
  public void setColumn(int c) {
    column = c;
  }
  
  public void setRow(int r) {
    row = r;
  }
  
  public void invalidate() {
    center = null;
  }
  
  public Point2D.Float getCenter() {
    if (center == null && myGrid != null) {
      center = myGrid.getHexCenter2D(column, row);
    }
    return center;
  }
  
  public Point getIntCenter() {
    if (center == null && myGrid != null) {
      center = myGrid.getHexCenter2D(column, row);
    }
    return new Point (Math.round(center.x), Math.round(center.y));
  }
  
  // Return the hex center converted to map-wide co-ordinates
  public Point convertCenterToMapCoords (Map map, Board board) {
    if ((map==null)||(board==null)) { return null; }
    if (center == null && myGrid != null) {
      center = myGrid.getHexCenter2D(column, row);
    }
    final Point c2 = new Point((int) center.x + board.bounds().x, (int) center.y + board.bounds().y);
    return map.snapTo(c2);  
  }
  
  public void rotate() {
    final int x = column;
    column = row;
    row = x;
    if (center != null) {
      center.setLocation(center.y, center.x);
    }
  }

  public boolean equals(Object o) {
    if (o instanceof HexRef) {      
      final HexRef h = (HexRef) o;
      if (h.isHexRef()) {
        return (h.getColumn() == getColumn() && h.getRow() == getRow());
      }
    }
    return false;
  }
  
  public int hashCode() {
    return 29 * column + row;
  }

  public int compareTo(HexRef o) {
    if (o== null) {
      throw new NullPointerException();
    }
    else if (getColumn() < o.getColumn()) {
      return -1;
    }
    else if (getColumn() > o.getColumn()) {
      return 1;
    }
    else if (getRow() < o.getRow()) {
      return -1;
    }
    else if (getRow() > o.getRow()) {
      return 1;
    }
    return 0;
  }
  
  public String toString() {
    return column+"/"+row;
  }
  
  public boolean isHexRef() {
    return true;
  }
  
  public boolean isEdgeRef() {
    return false;
  }
  
  public boolean isLineRef() {
    return false;
  }
  
  public boolean isAttributeRef() {
    return false;
  }
  
  /**
   * Is this hex adjacent to the supplied hex?
   * 
   * @param ref
   * @return
   */
  public boolean isAdjacentTo(HexRef ref) {
    Point c1 = ref.getIntCenter();
    Point c2 = this.getIntCenter();
    double dist = Point.distance(c1.x, c1.y, c2.x, c2.y);
    return Math.abs(dist-myGrid.getDy()) < 5;
  }
  
}
