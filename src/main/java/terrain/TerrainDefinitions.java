/*
 * $Id: TerrainDefinitions.java 5078 2009-02-09 05:40:45Z swampwallaby $
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

import java.util.HashMap;

import org.w3c.dom.Element;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.SingleChildInstance;

/**
 * Container class for Terrain Definitions
 */
public class TerrainDefinitions extends AbstractConfigurable {
  
  protected static TerrainDefinitions instance;
  protected HexTerrainDefinitions hexDefinitions;
  protected EdgeTerrainDefinitions edgeDefinitions;
  protected LineTerrainDefinitions lineDefinitions;
  protected AttributeTerrainDefinitions attributeDefinitions;
  protected HashMap<String, TerrainMap> terrainMaps;
  
  public TerrainDefinitions() {
    instance = this;
    terrainMaps = new HashMap<String, TerrainMap>();
  }
  
  public HexTerrainDefinitions getHexTerrainDefinitions() {
    return hexDefinitions;    
  }
  
  public EdgeTerrainDefinitions getEdgeTerrainDefinitions() {
    return edgeDefinitions;
  }

  public LineTerrainDefinitions getLineTerrainDefinitions() {
    return lineDefinitions;    
  }
  
  public AttributeTerrainDefinitions getAttributeTerrainDefinitions() {
    return attributeDefinitions;
  }
  
  public TerrainMap getTerrainMap(TerrainHexGrid grid) {
    TerrainMap tm = terrainMaps.get(grid.getBoard().getConfigureName());
    if (tm == null) {
      tm = new TerrainMap(grid);
      terrainMaps.put(grid.getBoard().getConfigureName(), tm);
    }
    return tm;
  }
  
  public static TerrainDefinitions getInstance() {
    return instance;
  }
  
  public void build(Element e) {
    super.build(e);

    if (hexDefinitions == null) { 
      addChild(new HexTerrainDefinitions());
      hexDefinitions.build(null);
    }
    if (edgeDefinitions == null) { 
      addChild(new EdgeTerrainDefinitions());
      edgeDefinitions.build(null);
    }
    if (lineDefinitions == null) { 
      addChild(new LineTerrainDefinitions());
      lineDefinitions.build(null);
    }
    if (attributeDefinitions == null) { 
      addChild(new AttributeTerrainDefinitions());
      attributeDefinitions.build(null);
    }
  }
  
  private void addChild(Buildable b) {
    add(b);
    b.addTo(this);
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
    validator = new SingleChildInstance(GameModule.getGameModule(),getClass());
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[] {
        HexTerrainDefinitions.class,
        EdgeTerrainDefinitions.class,
        LineTerrainDefinitions.class,
        AttributeTerrainDefinitions.class};
  }

  public static String getConfigureTypeName() {
    return "Terrain Definitions";
  }

  public void add(Buildable b) {
    super.add(b);
    if (b instanceof HexTerrainDefinitions) {
      hexDefinitions = (HexTerrainDefinitions) b;
    }
    else if (b instanceof EdgeTerrainDefinitions) {
      edgeDefinitions = (EdgeTerrainDefinitions) b;
    }  
    else if (b instanceof LineTerrainDefinitions) {
      lineDefinitions = (LineTerrainDefinitions) b;
    } 
    else if (b instanceof AttributeTerrainDefinitions) {
      attributeDefinitions = (AttributeTerrainDefinitions) b;
    } 
  }

  public void remove(Buildable b) {
    super.remove(b);
    if (b instanceof HexTerrainDefinitions) {
      hexDefinitions = null;
    }
    else if (b instanceof EdgeTerrainDefinitions) {
      edgeDefinitions = null;
    }
    else if (b instanceof LineTerrainDefinitions) {
      lineDefinitions = null;
    } 
    else if (b instanceof AttributeTerrainDefinitions) {
      attributeDefinitions = null;
    } 
  }
  
  public HelpFile getHelpFile() {
      return null;
  }

  public void removeFrom(Buildable parent) {
  }
  
}
