/*
 * $Id: TerrainGraph.java 7690 2011-07-07 23:44:55Z swampwallaby $
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

package terrain;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;

import VASSAL.build.module.map.boardPicker.Board;

public class TerrainGraph {

  protected TerrainedMap map;
  protected int gridDx;
  protected int gridDy;
  protected boolean gridSideways;

  // Index of all Terrain Nodes (hexes) by Snap location
  protected HashMap<Point, TerrainGraphNode> nodes = new HashMap<Point, TerrainGraphNode>();
 
  public TerrainGraph() {
    super();
  }

  public TerrainGraph(TerrainedMap map) {
    this();
    this.map = map;   
    build();
  }

  public TerrainGraphNode getNode(Point pos) {
    return nodes.get(pos);
  }
  
  // Build the combined terrain graph from the terrain maps for each board
  protected void build() {
    int minX = 9999999;
    int minY = 9999999;
    int maxX = 0;
    int maxY = 0;

    // Find the bounds of the entire Map
    for (Board b : map.getBoards()) {
      Rectangle bounds = b.bounds();
      if (bounds.x < minX)
        minX = bounds.x;
      if (bounds.y < minY)
        minY = bounds.y;
      if (bounds.x + bounds.width > maxX)
        maxX = bounds.x + bounds.width;
      if (bounds.y + bounds.height > maxY)
        maxY = bounds.y + bounds.height;
    }

    // Build the graph. 
    // Step 1 process all Hex and attribute terrain to create the nodes
    for (Board b : map.getBoards()) {
      final Rectangle bounds = b.bounds();
      final TerrainHexGrid grid = map.findTerrainHexGrid(new Point(bounds.x, bounds.y));
      if (grid != null) {
        processStage1(b, grid);
      }
    }

    processStage2();

    // Step 2 process all edge and line terrain to create the links
    for (Board b : map.getBoards()) {
      final Rectangle bounds = b.bounds();
      final TerrainHexGrid grid = map.findTerrainHexGrid(new Point(bounds.x, bounds.y));
      if (grid != null) {
        processStage3(b, grid);
      }
    }

  }

  // Stage 1, create a Node for each hex containing Hex Terrain
  // If a hex does not have Hex terrain, we won't see it.
  // Process all attribute terrain on this board
  protected void processStage1(Board board, TerrainHexGrid grid) {
    gridDx = (int) grid.getDx();
    gridDy = (int) grid.getDy();
    gridSideways = grid.isSideways();
    
    final TerrainMap terrainMap = grid.getTerrainMap();
    if (terrainMap == null) {
      return;
    }

    // Create a node for each hex and save it our master index
    for (TerrainHex hex : terrainMap.getAllHexTerrain()) {
      final TerrainGraphNode node = new TerrainGraphNode(map, board, grid, hex);
      nodes.put(node.getPosition(), node);
    }

    // Add all attributes
    for (Iterator<TerrainAttribute> i = terrainMap.getAllAttributeTerrain(); i.hasNext();) {
      final TerrainAttribute attribute = i.next();
      final Point pos = attribute.getLocation().convertCenterToMapCoords(map, board);
      final TerrainGraphNode node = nodes.get(pos);
      if (node != null) {
        node.addAttribute(attribute.getName(), attribute.getValue());
      }      
    }

  }

  // Stage 2 For every node, generate links to the 6 adjacent nodes
  protected void processStage2() {

    // Build a table of offsets from our hex to (roughly) the center of our neighbor hexes
    final int[] xOffset = new int[6];
    final int[] yOffset = new int[6];
    
    if (gridSideways) {
      xOffset[0] = +gridDy/2; yOffset[0] = +gridDx;
      xOffset[1] = -gridDy/2; yOffset[1] = +gridDx;
      xOffset[2] = -gridDy  ; yOffset[2] = 0;
      xOffset[3] = -gridDy/2; yOffset[3] = -gridDx;
      xOffset[4] = +gridDy/2; yOffset[4] = -gridDx;
      xOffset[5] = +gridDy  ; yOffset[5] = 0;      
    }
    else {
      xOffset[0] = +gridDx; yOffset[0] = -gridDy/2;
      xOffset[1] = +gridDx; yOffset[1] = +gridDy/2;
      xOffset[2] =  0     ; yOffset[2] = +gridDy;
      xOffset[3] = -gridDx; yOffset[3] = +gridDy/2;
      xOffset[4] = -gridDx; yOffset[4] = -gridDy/2;
      xOffset[5] =  0     ; yOffset[5] = -gridDy;
    }
    
    for (Point p : nodes.keySet()) {
      final TerrainGraphNode node = nodes.get(p);
      final Point pos = node.getPosition();
      for (int side = 0; side < 6; side++) {
        Point neighborPos = map.snapTo(new Point (pos.x+xOffset[side], pos.y+yOffset[side]));
        TerrainGraphNode neighborNode = nodes.get(neighborPos);
        // No node for neighbor means it is off the map
        if (neighborNode != null) {
          node.setNeighbor(side, neighborNode);
        }
      } 
    }
  }

  // Stage 3, Update the links with any line or edge terrain between hexes
  protected void processStage3(Board board, TerrainHexGrid grid) {
    final TerrainMap terrainMap = grid.getTerrainMap();
    if (terrainMap == null) {
      return;
    }
    
    for (Iterator<TerrainEdge> i = terrainMap.getAllEdgeTerrain(); i.hasNext();) {
      final TerrainEdge edge = i.next();
      final String terrain = edge.getTerrain().getTerrainName();
      final EdgeRef ref = edge.getReference();
      final TerrainGraphNode node1 = nodes.get(ref.convertCenterToMapCoords(map, board));
      final TerrainGraphNode node2 = nodes.get(ref.getOtherHex(ref).convertCenterToMapCoords(map, board));
      if (node1 != null) {
        node1.addEdge(node2, terrain);
      }
      if (node2 != null) {
        node2.addEdge(node1, terrain);
      }
    }

    for (Iterator<TerrainLine> i = terrainMap.getAllLineTerrain(); i.hasNext();) {
      final TerrainLine line = i.next();
      final String terrain = line.getTerrain().getTerrainName();
      final EdgeRef ref = line.getReference();
      final TerrainGraphNode node1 = nodes.get(ref.convertCenterToMapCoords(map, board));
      final TerrainGraphNode node2 = nodes.get(ref.getOtherHex(ref).convertCenterToMapCoords(map, board));
      if (node1 != null) {
        node1.addLine(node2, terrain);
      }
      if (node2 != null) {
        node2.addLine(node1, terrain);
      }      
    }
    
  }

}