/*
 * $Id: TdcBasicPiece.java 3639 2008-05-23 11:24:55Z swampwallaby $
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
package tdc;

import terrain.TerrainBasicPiece;

/**
 * A subclass of TerrainbasicPiece that adjusted Current Terrain of Beach hexes
 */

public class TdcBasicPiece extends TerrainBasicPiece {

  public TdcBasicPiece() {
    super();
  }

  public TdcBasicPiece(String type) {
    super(type);
  }

  public Object getProperty(Object key) {
    if (CURRENT_TERRAIN.equals(key)) {
      final String terrain = (String) super.getProperty(key); // Base terrain
      final String overlay = (String) super.getProperty(TdcProperties.OVERLAY); // Is hex on an overlay?
      final boolean isBeach = "true".equals(super.getProperty(TdcProperties.TERRAIN_BEACH)); // Is it an overlay beach hex?     
      if (isBeach) {
//        if (overlay.startsWith("Utah")) {
//          final String overlay_level = (String) super.getProperty("Overlay-Utah");
//          if (("2".equals(overlay_level) && "Utah-pl".equals(overlay)) || ("3".equals(overlay_level) && "Utah-act".equals(overlay))) { // Is overlay active?
//            return TdcProperties.TERRAIN_BEACH; // Convert to Beach terrain
//          }
//        }
//        else {
          final String overlay_level = (String) super.getProperty("Overlay-" + overlay);
          if ("2".equals(overlay_level)) { // Is overlay active?
            return TdcProperties.TERRAIN_BEACH; // Convert to Beach terrain
          }
//        }
      }
      return terrain;
    }
    return super.getProperty(key);
  }

  public Object getLocalizedProperty(Object key) {
    if (CURRENT_TERRAIN.equals(key)) {
      return getProperty(key);
    }
    return super.getLocalizedProperty(key);
  }
}