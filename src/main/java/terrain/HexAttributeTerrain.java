/*
 * $Id: HexAttributeTerrain.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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
 * Special form of AttributeTerrain that does not have a default value.
 * Used to attach tags to Hex Terrain types
 */
public class HexAttributeTerrain extends AttributeTerrain {
  
  public static String getConfigureTypeName() {
    return "Terrain Tag";
  }
  
  public String[] getAttributeNames() {
    return new String[] {NAME, ATTRIBUTE_TYPE, LIST};
  }

  public String[] getAttributeDescriptions() {
    return new String[] {"Name:  ", "Tag Type:  ", "List of Values:  "};
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[] {String.class, TypeConfig.class, String[].class};
  }
  
}