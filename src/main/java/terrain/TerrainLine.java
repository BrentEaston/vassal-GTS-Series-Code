/*
 * $Id: TerrainLine.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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

import VASSAL.tools.SequenceEncoder;

/**
 * Concrete implementation of Line Terrain
 * Represents a Hex and the connections of a particular type between the centre and each of
 * the 6 hex sides.
 */
public class TerrainLine extends TerrainEdge {

  protected static final String TYPE = "l";
 
  public TerrainLine(String code, TerrainHexGrid grid) {
    super(code, grid);
  }
  
  public TerrainLine(LineRef ref, LineTerrain t, TerrainHexGrid g) {
    super();
    reference = ref;
    terrain = t;
    grid = g;
  }
   
  public boolean equals(Object o) {
    if (o instanceof TerrainLine) {
      return ((TerrainLine) o).getLocation().equals(getLocation());
    }
    return false;
  }
  
  public void recalculate() {
    reference.invalidate();
  }
  
  public void setTerrain(LineTerrain t) {
    terrain = t; 
  }
  
  public LineTerrain getLineTerrain() {
    return (LineTerrain) terrain;
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
    reference = new LineRef(sd.nextInt(0), sd.nextInt(0), sd.nextInt(0), sd.nextInt(0), grid);
    terrain = TerrainDefinitions.getInstance().getLineTerrainDefinitions().getTerrain(sd.nextToken(TerrainMap.NO_TERRAIN));
  }
}
