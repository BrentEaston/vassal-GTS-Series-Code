/*
 * $Id: AttributeTerrainDefinitions.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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

import javax.swing.Icon;

import VASSAL.build.Buildable;

/**
 * Container class for Attribute Terrain Definitions
 *
 */
public class AttributeTerrainDefinitions extends BasicTerrainDefinitions {
  
  
  public AttributeTerrainDefinitions() {
    super(AttributeTerrain.class);
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {AttributeTerrain.class};
  }

  public static String getConfigureTypeName() {
    return "Tag Definitions";
  }
  
  public Icon[] getTerrainIcons() {
    return null;
  }
  
  public boolean containsTerrain(String terrainName) {
    for (Buildable b : getBuildables()) {
      if (b instanceof AttributeTerrain) {
        if (terrainName.equals(((AttributeTerrain) b).getTerrainName())) {
          return true;
        }
      }
    }
    return false;
  }
}
