/*
 * $Id: LineRef.java 945 2006-07-09 12:42:41Z swampwallaby $
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


/**
 * Key class for LineTerrain.  
 * A reference to a piece of Line terrain is the same as an EdgeRef
 * It refers to the piece of line between one hex center and the next.
 *
 */
public class LineRef extends EdgeRef {
 
  public LineRef() {
    super();
  }
  
  public LineRef(int c1, int r1, int c2, int r2, TerrainHexGrid grid) {
    super(c1, r1, c2, r2, grid);
  }
  
  public LineRef(HexRef h1, HexRef h2, TerrainHexGrid g) {
    super(h1, h2, g);
  }

  public LineRef reversed() {
    return new LineRef(column2, row2, column, row, myGrid);
  }
  
  protected void recalculate() {

    end1 = myGrid.getHexCenter2D(getColumn(), getRow());
    end2 = myGrid.getHexCenter2D(column2, row2);

  }
  
  public String toString() {
    return "Line " + super.toString();
  }
  
  public boolean equals(Object o) {
    if (o instanceof LineRef) {
      final LineRef ref = (LineRef) o;
      return super.equals(ref) || super.equals(ref.reversed());
    }
    return false;
  }
  
  public boolean isEdgeRef() {
    return false;
  }
  
  public boolean isLineRef() {
    return true;
  }
  
}