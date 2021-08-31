/*
 * $Id: LineTerrain.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import VASSAL.build.AutoConfigurable;
import VASSAL.configure.StringEnum;

/**
 * Line Terrain definition. Line terrain defines objects like roads
 * and railways.
 */
public class LineTerrain extends MapTerrain {
  
  protected static final String WIDTH = "width";
  protected static final String STYLE = "style";
  public static final String PLAIN = "Plain";
  public static final String DASHED = "Dashed";

  protected int width = 2;
  protected String style = PLAIN;
  
  public LineTerrain() {
    super();
  }  
  
  public int getWidth() {
    return width;
  }
  
  public String getStyle() {
    return style;
  }
  
  public Stroke getStroke() {
    if (style.equals(PLAIN)) {
      return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
    }
    else {
      return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, new float[] {5.0f, 10.0f}, 0.0f);
    }
  }
  
  public String[] getAttributeNames() {
    return new String[] {NAME, COLOR, WIDTH, STYLE};
  }

  public String[] getAttributeDescriptions() {
    return new String[] {"Name:  ", "Display Color:  ", "Width:  ", "Style:  "};
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[] {String.class, Color.class, Integer.class, StylePrompt.class};
  }
  
  public static class StylePrompt extends StringEnum {
    
    public String[] getValidValues(AutoConfigurable target) {
      return new String[] { PLAIN, DASHED };
    }  
  }
  
  public void setAttribute(String key, Object value) {
    if (WIDTH.equals(key)) {
      if (value instanceof String) {
        value = Integer.valueOf((String) value);
      }
      width = (Integer) value;
    }
    else if (STYLE.equals(key)) {
      style = (String) value;
    }
    else {
      super.setAttribute(key, value);
    }
  }

  public String getAttributeValueString(String key) {
    if (WIDTH.equals(key)) {
      return String.valueOf(width);
    }
    else if (STYLE.equals(key)) {
      return style;
    }
    return super.getAttributeValueString(key);
  }
  
  public static String getConfigureTypeName() {
    return "Line Terrain";
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

}
