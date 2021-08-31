/*
 * $Id: TerrainHex.java 9201 2015-09-23 01:28:23Z swampwallaby $
 *
 * Copyright (c) 2000-2015 by Brent Easton
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
 * Concrete implementation of Hex Terrain
 */
public class TerrainHex extends AbstractTerrain {

  protected static final String TYPE = "h";
  protected HexRef reference;
   
  public TerrainHex (int c, int r, MapTerrain t, TerrainHexGrid g) {
    reference = new HexRef(c, r, g);
    setTerrain(t);
    setHexGrid(g);
  }
  
  public TerrainHex (int c, int r, TerrainHexGrid g) {
    this(c, r, null, g);
  }

  public TerrainHex (HexRef ref, MapTerrain t, TerrainHexGrid g) {
    this(ref.getColumn(), ref.getRow(), t, g);
  }
  
  public TerrainHex(String code, TerrainHexGrid g) {
    decode(code, g);
  }
  
  public int getRow() {
    return reference.getRow();
  }
  
  public int getColumn() {
    return reference.getColumn();
  }
  
  public HexRef getLocation() {
    return reference;
  }
  
  public HexTerrain getHexTerrain() {
    return (HexTerrain) terrain;
  }
  
  public String encode() {
    final SequenceEncoder se = new SequenceEncoder(TYPE, ',');
    se.append(reference.getColumn());
    se.append(reference.getRow());
    se.append(terrain == null ? "" : terrain.getTerrainName());
    return se.getValue();
  }
  
  public void decode(String code, TerrainHexGrid grid) {
    final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ',');
    sd.nextToken();
    reference = new HexRef(sd.nextInt(0), sd.nextInt(0), grid);
    terrain = TerrainDefinitions.getInstance().getHexTerrainDefinitions().getTerrain(sd.nextToken(TerrainMap.NO_TERRAIN));
  }
}
