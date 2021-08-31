/*
 * $Id: EdgeTerrainDefinitions.java 3639 2008-05-23 11:24:55Z swampwallaby $
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
 * Container class for Edge Terrain Definitions
 *
 */
public class EdgeTerrainDefinitions extends BasicTerrainDefinitions {
  
  public EdgeTerrainDefinitions() {
    super(EdgeTerrain.class);
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {EdgeTerrain.class};
  }

  public static String getConfigureTypeName() {
    return "Hex Edge Terrain Definitions";
  }
  
}
