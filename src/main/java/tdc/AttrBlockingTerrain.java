/*
 * $Id: AttrBlockingTerrain.java 9192 2015-05-16 01:24:32Z swampwallaby $
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
import java.awt.geom.Point2D;

import VASSAL.build.module.Map;
import terrain.AttributeTerrain;
import terrain.HexRef;
import terrain.TerrainAttribute;
import terrain.TerrainHex;

public class AttrBlockingTerrain extends HexBlockingTerrain {

  protected TerrainAttribute attr;
  
  public AttrBlockingTerrain(TdcMap map, TerrainAttribute attr, Rectangle offset) {
    super(map, attr.getTerrain().getTerrainName(), offset);
    this.attr = attr;
    this.hex = attr;
    setCenter(offset (attr.getLocation().getCenter(), offset));
    final AttributeTerrain ter = attr.getAttributeTerrain();
    setPossiblyBlocking (ter.isPossibleBlocking());
  }
  
  protected void calculateBaseShape() {
    final Point2D.Float p = attr.getLocation().getCenter();
    mapShape = attr.getLocation().getGrid().getSingleHex(new Point (Math.round(p.x), Math.round(p.y)));
    transformBaseShape();
   }
 
  protected HexRef getHexLocation() {
    return attr.getLocation();
  }


  /*// Need to calculate center differently. HexBlockingTerrain has an  non null hex attribute,
  // For AttrBlockingTerrain hex is null, but attr has a hex info
  @Override
  public boolean hasCenter(Point p) {
    if (attr==null) {
      return false;
    }
    HexRef hexRef = attr.getLocation();
    Point center = hexRef.convertCenterToMapCoords(map, ((Map) map).findBoard(p));
    return (p.equals(center));
  }*/
}