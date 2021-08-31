/*
 * $Id: LineTerrainDefinitions.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import VASSAL.build.Buildable;

/**
 * Container class for Line Terrain Definitions
 *
 */
public class LineTerrainDefinitions extends BasicTerrainDefinitions {
  
  
  public LineTerrainDefinitions() {
    super(LineTerrain.class);
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {LineTerrain.class};
  }

  public static String getConfigureTypeName() {
    return "Line Terrain Definitions";
  }
  
  public Icon[] getTerrainIcons() {
    final Color[] colors = getTerrainColors();
    final Icon[] icons = new Icon[colors.length];
    final Stroke[] strokes = new Stroke[colors.length];
    
    int i = 0;
    for (Buildable b : getBuildables()) {
       strokes[i++] = ((LineTerrain) b).getStroke();
    } 
    
    BufferedImage image;
    
    for (i=0; i < colors.length-1; i++) {
      image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
      final Graphics2D g2 = (Graphics2D) image.getGraphics();
      g2.setColor(colors[i]);
      final Stroke oldStroke = g2.getStroke();
      g2.setStroke(strokes[i]);
      g2.drawLine(0, ICON_SIZE/2, ICON_SIZE, ICON_SIZE/2);
      g2.setStroke(oldStroke);
      g2.setColor(Color.black);
      g2.drawRect(0, 0, ICON_SIZE-1, ICON_SIZE-1);
      icons[i] = new ImageIcon(image);
    }
    image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    final Graphics g = image.getGraphics();
    g.setColor(Color.black);
    g.drawRect(0, 0, ICON_SIZE-1, ICON_SIZE-1);
    icons[i] = new ImageIcon(image);
    
    return icons;
  }
  
}
