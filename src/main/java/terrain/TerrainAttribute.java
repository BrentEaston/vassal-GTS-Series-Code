/*
 * $Id: TerrainAttribute.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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
 * Concrete implementation of Attribute Terrain
 */
public class TerrainAttribute extends TerrainHex {

  protected static final String TYPE = "a";
  protected String value;

  public TerrainAttribute(int c, int r, AttributeTerrain t, TerrainHexGrid g,
      String val) {
    super(c, r, t, g);
    value = val;
  }

  public TerrainAttribute(int c, int r, TerrainHexGrid g, String val) {
    this(c, r, null, g, val);
  }

  public TerrainAttribute(HexRef ref, AttributeTerrain t, TerrainHexGrid g,
      String val) {
    this(ref.getColumn(), ref.getRow(), t, g, val);
  }

  public TerrainAttribute(String code, TerrainHexGrid g) {
    super(code, g);
  }

  public String getValue() {
    return value;
  }

  public String getName() {
    return terrain == null ? "" : terrain.getTerrainName();
  }

  public void setTerrain(AttributeTerrain t) {
    terrain = t;
  }

  public AttributeTerrain getAttributeTerrain() {
    return (AttributeTerrain) terrain;
  }

  public String encode() {
    final SequenceEncoder se = new SequenceEncoder(TYPE, ',');
    se.append(reference.getColumn());
    se.append(reference.getRow());
    if (terrain != null) {
      se.append(terrain.getTerrainName());
      se.append(value);
    }
    return se.getValue();
  }

  public void decode(String code, TerrainHexGrid grid) {
    final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(code, ',');
    sd.nextToken();
    reference = new HexRef(sd.nextInt(0), sd.nextInt(0), grid);
    terrain = TerrainDefinitions.getInstance()
        .getAttributeTerrainDefinitions().getTerrain(sd.nextToken(""));
    value = sd.nextToken("");
  }

}
