/*
 * $Id: TerrainEdge.java 9201 2015-09-23 01:28:23Z swampwallaby $
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

import VASSAL.tools.SequenceEncoder;

/**
 * Concrete implementation of Edge Terrain
 */
public class TerrainEdge extends AbstractTerrain {

  protected static final String TYPE = "e";
  protected EdgeRef reference;

  public TerrainEdge() {
    
  }
  
  public TerrainEdge(int c1, int r1, int c2, int r2, EdgeTerrain t, Point c, TerrainHexGrid g) {
    reference = new EdgeRef(c1, r1, c2, r2, grid);
    reference.setCenter(new Point2D.Float(c.x, c.y));
    grid = g;
    terrain = t;
  }

  public TerrainEdge(EdgeRef ref, EdgeTerrain t) {
    reference = ref;
    terrain = t;
  }

  public TerrainEdge(EdgeRef ref, EdgeTerrain t, TerrainHexGrid g) {
    this(ref, t);
    grid = g;
  }
  
  public TerrainEdge(Point centerPos, TerrainHexGrid g, EdgeTerrain t) {
    reference = new EdgeRef(new Point2D.Float(centerPos.x, centerPos.y), g);
    terrain = t;
    grid = g;
  }

  public TerrainEdge(String code, TerrainHexGrid grid) {
    decode(code, grid);
  }

  public boolean equals(Object o) {
    if (o instanceof TerrainEdge) {
      return ((TerrainEdge) o).getLocation().equals(getLocation());
    }
    return false;
  }

  public EdgeRef getReference() {
    return reference;
  }

  public void recalculate() {
    reference.invalidate();
  }

  public Point2D.Float getEnd1() {
    return reference.getEnd1();
  }

  public Point2D.Float getEnd2() {
    return reference.getEnd2();
  }

  public EdgeRef getLocation() {
    return reference;
  }

  public EdgeRef getReverseLocation() {
    return reference.reversed();
  }

  public TerrainEdge reversed() {
    return new TerrainEdge(reference.reversed(), (EdgeTerrain) terrain);
  }

  public Point2D.Float getCenter() {
    return reference.getCenter();
  }

  public EdgeTerrain getEdgeTerrain() {
    return (EdgeTerrain) terrain;
  }
  
  public String encode() {
    final SequenceEncoder se = new SequenceEncoder(TYPE, ',');
    se.append(reference.getColumn());
    se.append(reference.getRow());
    se.append(reference.getColumn2());
    se.append(reference.getRow2());
    se.append(terrain == null ? "" : terrain.getTerrainName());
    return se.getValue();
  }

  public void decode(String code, TerrainHexGrid grid) {
    final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ',');
    sd.nextToken();
    reference = new EdgeRef(sd.nextInt(0), sd.nextInt(0), sd.nextInt(0), sd.nextInt(0), grid);
    terrain = TerrainDefinitions.getInstance().getEdgeTerrainDefinitions().getTerrain(sd.nextToken(TerrainMap.NO_TERRAIN));
  }
}
