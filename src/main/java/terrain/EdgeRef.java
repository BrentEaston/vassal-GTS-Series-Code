/*
 * $Id: EdgeRef.java 9201 2015-09-23 01:28:23Z swampwallaby $
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
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * A reference to an Edge is made up of two hex grid references.
 * The edge is the edge between the two hexes
 *
 */
public class EdgeRef extends HexRef {
  
  protected int column2, row2;
  
  // Start and end a line that visually represents Edge
  protected Point2D.Float end1, end2;

  public EdgeRef() {
    this(0, 0, 0, 0);
  }
  
  public EdgeRef(HexRef h1, HexRef h2, TerrainHexGrid g) {
    this(h1.getColumn(), h1.getRow(), h2.getColumn(), h2.getRow());
    myGrid = g;
  }
  
  public EdgeRef(int c1, int r1, int c2, int r2) {
    this(c1, r1, c2, r2, null);
  }

  public EdgeRef(int c1, int r1, int c2, int r2, TerrainHexGrid g) {
    column = c1;
    row = r1;
    column2 = c2;
    row2 = r2;
    myGrid = g;
  }
  
  public String toString() {
    return "EdgeRef ref="+column+"/"+row+"-"+column2+"/"+row2+", Center="+getCenter()+", end1="+getEnd1().x+","+getEnd1().y+", end2="+getEnd2().x+","+getEnd2().y+", Gradient="+getGradient();
  }

  public EdgeRef(Point p, TerrainHexGrid g) {
    this(new Point2D.Float(p.x, p.y), g);
  }  
  
  public EdgeRef(Point2D.Float p, TerrainHexGrid g) {
  
    myGrid = g;
    center = new Point2D.Float(p.x, p.y);
    
    //FIXME Fix Snapping
    final Point p1 = myGrid.snapToHex(new Point(Math.round(center.x)+4, Math.round(center.y)+4));
    final Point p2 = myGrid.snapToHex(new Point(Math.round(center.x)-4, Math.round(center.y)-4));
    
    HexRef hex1 = new HexRef(myGrid.getGridPosition(p1));
    myGrid.rotateIfSideways(hex1);
    column = hex1.getColumn();
    row = hex1.getRow();
    
    HexRef hex2 = new HexRef(myGrid.getGridPosition(p2));
    myGrid.rotateIfSideways(hex2);
    column2 = hex2.getColumn();
    row2 = hex2.getRow();
   
    recalculate();
  }
  
  public EdgeRef(EdgeRef e, TerrainHexGrid g) {
    this(e.column, e.row, e.column2, e.row2, g);
  }
  
  public void invalidate() {
    super.invalidate();
    end1 = null;
    end2 = null;
  }
  
  // An Edge is defined by 2 hex references. Return the hex that is not = ref
  public HexRef getOtherHex(HexRef ref) {
     if (ref.getColumn() == column && ref.getRow() == row) {
       return new HexRef (column2, row2, myGrid);
     }
     else {
       return new HexRef (column, row, myGrid);
     }
  }
  
  public boolean isAdjacent(HexRef hex) {
    final int hc = hex.getColumn();
    final int hr = hex.getRow();
    return (hc == getColumn() && hr == getRow()) || (hc == getColumn2() && hr == getRow2());
  }
  
  public int getColumn2() {
    return column2;
  }
  
  public int getRow2() {
    return row2;
  }
  
  public void setCenter(Point2D.Float p) {
    if (center == null) {
      center = p;
    }
    else {
      center.setLocation(p);
    }
  }
  
  public boolean borders(HexRef h) {
    return (h.getColumn() == column && h.getRow() == row) ||
            (h.getColumn() == column2 && h.getRow() == row2);
  }
  
  public EdgeRef reversed() {
    return new EdgeRef(column2, row2, column, row, myGrid);
  }

  public Point2D.Float getCenter() {
    if (center == null) {
      recalculate();
    }
    return center;
  }
  
  public Point getIntCenter() {
    getCenter();
    return new Point (Math.round(center.x), Math.round(center.y));
  }
  
  public Point2D.Float getEnd1() {
    if (end1 == null) {
      recalculate();
    }
    return end1;
  }
  
  public Point getIntEnd1() {
    getEnd1();
    return new Point (Math.round(end1.x), Math.round(end1.y));
  }

  public Point2D.Float getEnd2() {
    if (end2 == null) {
      recalculate();
    }
    return end2;
  }
  
  public Point getIntEnd2() {
    getEnd2();
    return new Point (Math.round(end2.x), Math.round(end2.y));
  }
  
  public Rectangle getBounds() {
    if (end1 == null) {
      recalculate();
    }
    int x = Math.round(Math.min(end1.x, end2.x));
    int y = Math.round(Math.min(end1.y, end2.y));
    x -= 5;
    y -= 5;
    final int width = Math.abs(Math.round(end1.x - end2.x)) + 10;
    final int height = Math.abs(Math.round(end1.y - end2.y)) + 10;
    
    return new Rectangle(x, y, width, height);
  }
  
  /**
   * Recalculate Center and endpoints of the Edge.
   */
  protected void recalculate() {

    if (center == null) {
      center = getEdgeCenter();
    }
    
    if (myGrid.isSideways()) {    
      end1 = myGrid.snapToHexVertex2D(new Point2D.Float(center.x+5, center.y+2));     
      end2 = myGrid.snapToHexVertex2D(new Point2D.Float(center.x-5, center.y-2));
    }
    else {
      end1 = myGrid.snapToHexVertex2D(new Point2D.Float(center.x+2, center.y+5));
      end2 = myGrid.snapToHexVertex2D(new Point2D.Float(center.x-2, center.y-5));
    }

  } 
  
  protected Point2D.Float toPoint2D (Point p) {
    return new Point2D.Float(p.x, p.y);
  }
  
  protected Point2D.Float getEdgeCenter() {
    final Point2D.Float c1 = myGrid.getHexCenter2D(column, row);
    final Point2D.Float c2 = myGrid.getHexCenter2D(column2, row2);
    final Point p = new Point( Math.round((c1.x+c2.x)/2) ,   Math.round((c1.y+c2.y)/2));
    final Point c = myGrid.snapToHexSide(p);
    return new Point2D.Float(c.x, c.y);
  }
  
  public float getGradient() {
    return (end2.y-end1.y) / (end2.x-end1.x);
  }
  
  public boolean equals(Object o) {
    if (o instanceof EdgeRef) {
      final EdgeRef target = (EdgeRef) o;
      return ((target.column == column && target.row == row && target.column2 == column2 && target.row2 == row2));
    }
    return false;
  }
  
  public boolean equalsReversed (Object o) {
    if (o instanceof EdgeRef) {
      final EdgeRef target = (EdgeRef) o;
      return ((target.column == column2 && target.row == row2 && target.column2 == column && target.row2 == row));
    }
    return false;
  }
  
  public boolean equalsAny (Object o) {
    return equals(o) || equalsReversed(o);
  }
  
  public int hashCode() {
    return 29 * (column + column2) + row + row2;
  }
  
  public String toSring() {
    return super.toString() + " to "+column2+"/"+row2;
  }
  
  public boolean isEdgeRef() {
    return true;
  }
  
  public boolean isHexRef() {
    return false;
  }
}
