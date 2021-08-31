/*
 * $Id: TerrainLink.java 9201 2015-09-23 01:28:23Z swampwallaby $
 *
 * Copyright (c) 2000-2015 by Brent Easton
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

// A Link to another hex
public class TerrainGraphLink {

  protected TerrainGraphNode neighbor;
  protected String edgeTerrain;
  protected String lineTerrain;

  public TerrainGraphLink(TerrainGraphNode neighbor) {
    this(neighbor, null, null);
  }

  public TerrainGraphLink(TerrainGraphNode neighbor, String edge, String line) {
    this.neighbor = neighbor;
    this.edgeTerrain = edge;
    this.lineTerrain = line;
  }

  public TerrainGraphNode getNeighbor() {
    return neighbor;
  }

  public void setNeighbor(TerrainGraphNode neighbor) {
    this.neighbor = neighbor;
  }

  public String getEdgeTerrain() {
    return edgeTerrain;
  }

  public void setEdgeTerrain(String edgeTerrain) {
    this.edgeTerrain = edgeTerrain;
  }

  public String getLineTerrain() {
    return lineTerrain;
  }

  public void setLineTerrain(String lineTerrain) {
    this.lineTerrain = lineTerrain;
  }

}