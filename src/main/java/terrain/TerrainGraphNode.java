/*
 * $Id: TerrainGraphNode.java 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2005-2015 by Brent Easton
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

/*
 * A Node in a terrain graph.
 * Represents a single hex
 */
package terrain;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.HashMap;

import VASSAL.build.module.map.boardPicker.Board;

public class TerrainGraphNode {

  protected Point position;
  protected String terrain;
  protected String hexName;
  protected Board board;
  protected TerrainHexGrid grid;
  protected HashMap<String, String> attributes = new HashMap<String, String>();

  protected TerrainGraphLink[] links = new TerrainGraphLink[6];

  public TerrainGraphNode(TerrainedMap map, Board board, TerrainHexGrid grid, TerrainHex hex) {
    this.board = board;
    this.grid = grid;
    position = hex.getLocation().convertCenterToMapCoords(map, board);
    terrain = hex.getTerrain().getTerrainName();
    hexName = map.locationName(position);
  }

  public TerrainGraphNode(Point snap, String terrain) {
    position = new Point(snap);
    this.terrain = terrain;
  }

  public String getTerrain() {
    return terrain;
  }
  
  public String getHexName() {
    return hexName;
  }
  
  // Return the Edge terrain between this hex and the specified neighbor
  public String getEdgeTerrain (TerrainGraphNode neighbor) {
    final TerrainGraphLink link = findLinkToNeighbor(neighbor);    
    return link == null ? null : link.getEdgeTerrain();    
  }

  // Return the Line terrain between this hex and the specified neighbor
  public String getLineTerrain (TerrainGraphNode neighbor) {
    final TerrainGraphLink link = findLinkToNeighbor(neighbor);    
    return link == null ? null : link.getLineTerrain();
  }
  
  public boolean equals(Object o) {
    if (o instanceof TerrainGraphNode) {
      TerrainGraphNode node = (TerrainGraphNode) o;
      return position.equals(node.getPosition());
    }
    return false;
  }

  public Point getPosition() {
    return position;
  }
  
  public TerrainGraphLink[] getLinks() {
    return links;
  }
  
  public boolean isAdjacent (TerrainGraphNode node) {
    return findLinkToNeighbor(node) != null;    
  }
  
  protected TerrainGraphLink findLinkToNeighbor (TerrainGraphNode node) {
    for (TerrainGraphLink link : links) {
      if (link != null && node != null) {
        if (node.equals(link.getNeighbor())) {
          return link;
        }
      }
    }
    return null;
  }
  
  public void setNeighbor(int side, TerrainGraphNode neighbor) {
    links[side] = new TerrainGraphLink(neighbor);
  }
  
  public void addAttribute (String name, String value) {
    attributes.put(name, value);
  }
  
  public String getAttribute (String name) {
    return attributes.get(name);
  }
  
  public void addEdge (TerrainGraphNode neighbor, String terrain) {
    if (neighbor == null) {
      return;
    }
    for (int side = 0; side < 6; side++) {
      if (links[side] != null && neighbor.equals(links[side].getNeighbor())) {
        links[side].setEdgeTerrain(terrain);
      }
    }
    
  }
  
  public void addLine (TerrainGraphNode neighbor, String terrain) {
    if (neighbor == null) {
      return;
    }
    for (int side = 0; side < 6; side++) {
      if (links[side] != null && neighbor.equals(links[side].getNeighbor())) {
        links[side].setLineTerrain(terrain);
      }
    }
    
  }

  // Return the shape of the hex 
  public Area getMapShape() {

    final Rectangle boardBounds = board.bounds();
    final Point boardPosition = new Point(
      position.x-boardBounds.x, position.y-boardBounds.y);

    Area a = grid.getSingleHex(boardPosition); // In board co-ords
    final AffineTransform t = AffineTransform.getTranslateInstance(
      boardBounds.x, boardBounds.y); // Translate back to map co-ords

    final double mag = board.getMagnification();
    if (mag != 1.0) {
      t.translate(boardPosition.x, boardPosition.y);
      t.scale(mag, mag);
      t.translate(-boardPosition.x, -boardPosition.y);
    }
    return a.createTransformedArea(t);
    
  }
}