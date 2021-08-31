/*
 * $Id: AbstractTerrain.java 3639 2008-05-23 11:24:55Z swampwallaby $
 *
 * Copyright (c) 2015 by Brent Easton
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

public abstract class AbstractTerrain {
  
  protected MapTerrain terrain;
  protected TerrainHexGrid grid;
  
  public AbstractTerrain() {
    super();
  }
  
  public abstract String encode();
  public abstract void decode(String code, TerrainHexGrid grid);
  
  public MapTerrain getTerrain() {
    return terrain;
  }
  
  public void setTerrain(MapTerrain t) {
    terrain = t; 
  }
  
  public TerrainHexGrid getHexGrid() {
    return grid;
  }
   
  public void setHexGrid(TerrainHexGrid g) {
    grid = g;
  }
  
}