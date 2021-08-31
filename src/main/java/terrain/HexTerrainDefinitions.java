/*
 * $Id: HexTerrainDefinitions.java 3639 2008-05-23 11:24:55Z swampwallaby $
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
 * Container class for Hex Terrain definitions
 *
 */
public class HexTerrainDefinitions extends BasicTerrainDefinitions {
  
  
  public HexTerrainDefinitions() {
    super(HexTerrain.class);
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {HexTerrain.class, HexAttributeTerrain.class};
  }

  public static String getConfigureTypeName() {
    return "Full Hex Terrain Definitions";
  }
  
}
