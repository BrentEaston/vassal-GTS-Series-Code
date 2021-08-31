/*
 * $Id: BlockingTerrainMap.java 9192 2015-05-16 01:24:32Z swampwallaby $
 *
 * Copyright (c) 2017 by Brent Easton
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
import java.awt.geom.Point2D;

import terrain.HexRef;
import terrain.TerrainHex;
import terrain.TerrainHexGrid;

public class BarrageBlockingTerrain extends HexBlockingTerrain {
  
  public BarrageBlockingTerrain(TdcMap map, Point pos) {
      super(map, "Barrage");
      setCenter(new Point2D.Float((float) pos.getX(), (float) pos.getY()));
      boardOffset = map.findBoard(pos).bounds();
      TerrainHexGrid grid = map.findTerrainHexGrid(pos); 
      HexRef hex = grid.getHexPos(new Point (pos.x-boardOffset.x, pos.y-boardOffset.y));
      this.hex = new TerrainHex (hex.getColumn(), hex.getRow(), grid);      
      
  }


}
