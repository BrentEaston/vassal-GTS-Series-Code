/*
 * $Id: ObliqueHexGridNumbering.java 7691 2011-07-07 23:48:22Z swampwallaby $
 * 
 * Copyright (c) 2000-20011 by Brent Easton
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License (LGPL) as published by
 * the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, copies are available at
 * http://www.opensource.org.
 */

package tdc;

import java.awt.Point;

import VASSAL.build.module.map.boardPicker.board.mapgrid.HexGridNumbering;

public class ObliqueHexGridNumbering extends HexGridNumbering {

  public int getBaseRow(Point p) {
    return super.getRow(p);
  }
  
  public int getBaseColumn(Point p) {
    return super.getColumn(p);
  }
  
  public boolean first = true;
  public boolean stagger = false;
  public boolean isStagger() {
    if (first) {
      first = false;
      stagger = "true".equals(getAttributeValueString(STAGGER));
      
    }
    return stagger;
  }
  
  public int getRow(Point p) {

    
    int row = super.getRow(p);
    int col = super.getColumn(p);

    int newRow = row - (col / 2);

    if (col % 2 == 0) {
      newRow++;
    }
    
    if (!isStagger() && col % 2 == 0) {
      newRow--;
    }
    
    return newRow;
  }

  public int getColumn(Point p) {

    int newCol = super.getColumn(p);
    int newRow = getRow(p);

    // Ensure all rounding occurs on positive integers
    // Otherwise artifacts appear when newRow goes negative
    newCol += (newRow + 10001) / 2 - 5000;
       
    return newCol;
  }

  public static String getConfigureTypeName() {
    return "Oblique Hex Grid Numbering";
  }
  
 }