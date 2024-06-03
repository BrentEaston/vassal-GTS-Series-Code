/*
 * $Id: MapTerrain.java 9193 2015-05-18 00:27:45Z swampwallaby $
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

import java.awt.Color;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.ColorConfigurer;

/**
 * Base class for all Terrain definitions
 *
 */
public class MapTerrain extends AbstractConfigurable {
  
  protected static final String NAME = "name";
  protected static final String COLOR = "color";
  protected static final String BLOCKING  = "blocking";
  public static final String POSSIBLE_BLOCKING = "PossBlocking";

  protected Color color = null;
  protected boolean blocking = false;
  protected boolean possibleBlocking = false;
  
  public MapTerrain() {
    super();
  }  
  
  public String getTerrainName() {
    return getConfigureName();
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }
  
  public boolean isBlocking() {
    return blocking;
  }
  
  public boolean isPossibleBlocking() {
    return possibleBlocking;
  }

  public String[] getAttributeNames() {
    return new String[] {NAME, COLOR, BLOCKING, POSSIBLE_BLOCKING};
  }

  public String[] getAttributeDescriptions() {
    return new String[] {"Name:  ", "Display Color:  ", "Blocking Terrain?", "Possible Blocking Terrain?"};
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[] {String.class, Color.class, Boolean.class, Boolean.class};
  }
  
  public void setAttribute(String key, Object value) {
    if(NAME.equals(key)) {
      setConfigureName((String) value);
    }
    else if (COLOR.equals(key)) {
      if (value instanceof String) {
        value = ColorConfigurer.stringToColor((String) value);
      }
      setColor((Color) value);
    }
    else if (BLOCKING.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      blocking = (Boolean) value;
    }
    else if (POSSIBLE_BLOCKING.equals(key)) {
      if (value instanceof String) {
        value = Boolean.valueOf((String) value);
      }
      possibleBlocking = (Boolean) value;
    }
  }

  public String getAttributeValueString(String key) {
    if(NAME.equals(key)) {
      return getConfigureName();
    }
    else if (COLOR.equals(key)) {
      return ColorConfigurer.colorToString(color);
    }
    else if (BLOCKING.equals(key)) {
      return String.valueOf(blocking);
    }
    else if (POSSIBLE_BLOCKING.equals(key)) {
      return String.valueOf(possibleBlocking);
    }
    return null;
  }
  
  public void addTo(Buildable b) {

  }

  public void removeFrom(Buildable b) {

  }

  public HelpFile getHelpFile()  {
      return null;
  }

  public static String getConfigureTypeName() {
    return "Terrain";
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {TerrainAttribute.class};
  }

}
