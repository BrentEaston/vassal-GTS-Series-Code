/*
 * $Id: BasicTerrainDefinitions.java 3639 2008-05-23 11:24:55Z swampwallaby $
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;

/**
 * Base class for all Terrrain Definition Containers
 */
public class BasicTerrainDefinitions extends AbstractConfigurable { 
  
  protected static final int ICON_SIZE = 16;
  protected Class<? extends MapTerrain> childTerrain;

  public BasicTerrainDefinitions() {
    super();
  }
  
  public BasicTerrainDefinitions(Class<? extends MapTerrain> c) {
    this();
    childTerrain = c;
  }
  
  // Include so this will work under vassal 3.0
  public List<Buildable> getBuildables() {
    return Collections.unmodifiableList(buildComponents);
  }
  
  protected int getChildCount() {
    int i = 0;
    for (Buildable b : getBuildables()) {
      if (b.getClass().equals(childTerrain)) {
        i++;
      }
    }
    return i;
  }
  
  public MapTerrain getTerrain(String terrainName) {
    
    for (Buildable b : getBuildables()) {
      final MapTerrain terrain = (MapTerrain) b;
      if (terrain.getConfigureName().equals(terrainName)) {
        return terrain;
      }
    }
    return null;
  }
  
  public String[] getTerrainNames() {
    final String[] names = new String[getChildCount()+1];
    int i = 0;
    for (Buildable b : getBuildables()) {
      if (b.getClass().equals(childTerrain)) {
        names[i++] = ((MapTerrain) b).getConfigureName();
      }
    }
    names[i] = TerrainMap.NO_TERRAIN;
    return names;
  }

  public Color[] getTerrainColors() {
    final Color[] colors = new Color[getChildCount()+1];
    int i = 0;
    for (Buildable b : getBuildables()) {
      if (b.getClass().equals(childTerrain)) {
        colors[i++] = ((MapTerrain) b).getColor();
      }
    }
    colors[i] = null;
    return colors;
  }

  public Icon[] getTerrainIcons() {
    final Color[] colors = getTerrainColors();
    final Icon[] icons = new Icon[colors.length];
    BufferedImage image;
    int i;
    for (i=0; i < colors.length-1; i++) {
      icons[i] = getTerrainIcon(colors[i]);
    }
    image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = image.getGraphics();
    g.setColor(Color.black);
    g.drawRect(0, 0, ICON_SIZE-1, ICON_SIZE-1);
    icons[i] = new ImageIcon(image);
    
    return icons;
  }
  
  public static Icon getTerrainIcon(Color color) {
    final BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    final Graphics2D g2 = (Graphics2D) image.getGraphics();
    final Composite oldComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
    g2.setColor(color);
    g2.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
    g2.setComposite(oldComposite);
    g2.setColor(Color.black);
    g2.drawRect(0, 0, ICON_SIZE-1, ICON_SIZE-1);
    return new ImageIcon(image);
  }
  
  public String[] getAttributeDescriptions() {
    return new String[0];
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[0];
  }

  public String[] getAttributeNames() {
    return new String[0];
  }

  public String getAttributeValueString(String key) {
    return null;
  }

  public void setAttribute(String key, Object value) {
  }

  public Configurer getConfigurer() {
    return null;
  }

  public void addTo(Buildable parent) {
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public HelpFile getHelpFile() {
      return null;
  }

  public void removeFrom(Buildable parent) {
  }

  
}
