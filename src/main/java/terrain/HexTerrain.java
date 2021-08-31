/*
 * $Id: HexTerrain.java 3128 2008-02-20 06:12:23Z swampwallaby $
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
 * Hex Terrain covers a full hex 
 *
 */
public class HexTerrain extends MapTerrain {
  
  public HexTerrain() {
    super();
  }  

  public static String getConfigureTypeName() {
    return "Hex Terrain";
  }

}
