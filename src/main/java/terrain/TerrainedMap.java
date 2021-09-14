/*
 * $Id: TerrainedMap.java 9201 2015-09-23 01:28:23Z swampwallaby $
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

import java.awt.Point;
import java.util.ArrayList;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.configure.PropertyExpression;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;

public class TerrainedMap extends Map {
  
  // A combined graph of all terrain from all boards that make up this map
  protected TerrainGraph graph;
  
  // Find a Terrain property at a given point
  public static Object getTerrainProperty(GamePiece p, String prop) {
    Object value = null;
    final Map map = p.getMap();
    if (map instanceof TerrainedMap) {
      final Point pos = p.getPosition();
      final TerrainHexGrid hgrid = ((TerrainedMap) map).findTerrainHexGrid(pos);
      if (hgrid != null) {
        if (TerrainBasicPiece.CURRENT_TERRAIN.equals(prop)) {
          value = hgrid.getTerrainName(pos);
        }
        else {
          value = hgrid.getProperty(prop, pos);
        }
      }
    }
    return value;
  }

  // Find the Terrain hex grid on this map at this point
  public TerrainHexGrid findTerrainHexGrid(Point pos) {
    TerrainHexGrid hgrid = null;
    Board board = findBoard(pos);
    if (board != null) {
      MapGrid grid = board.getGrid();
      if (grid instanceof ZonedGrid) {
        ZonedGrid zGrid = (ZonedGrid) grid;
        grid = zGrid.getBackgroundGrid();
        if (grid == null) {
          Zone zone = zGrid.findZone(pos);
          if (zone != null) {
            grid = zone.getGrid();
          }
        }
        if (grid instanceof TerrainHexGrid) {
          hgrid = (TerrainHexGrid) grid;
        }
      }
      else {
        hgrid = (TerrainHexGrid) grid;
      }
    }
    return hgrid;
  }

  // Is this map the main map?
  public boolean isAttached() {
    return !getLaunchButton().isVisible();
  }

  // Is this map visible?
  public boolean isVisible() {
    return getView().isVisible();
  }

  // Build the terrain graph
  public void buildTerrainGraph() {
    graph = new TerrainGraph (this);
  }
  
  public TerrainGraph getTerrainGraph() {
    if (graph == null) {
      buildTerrainGraph();
    }
    return graph;
  }
  
  // Find the first piece that matches the expression
  public GamePiece findPiece (PropertyExpression expression) {
    for (GamePiece p : this.getPieces()) {
      if (p instanceof Stack) {
        final Stack s = (Stack) p;
        for (int i=0; i < s.getPieceCount(); i++) {
          if (expression.accept(s.getPieceAt(i))) {
            return s.getPieceAt(i);
          }
        }
      }
      else {
        if (expression.accept(p)) {
          return p;
        }
      }
    }
    return null;
  }
  
  // Find all pieces that matches the expression
  public ArrayList<GamePiece> findPieces (PropertyExpression expression) {
    final ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
    for (GamePiece p : this.getPieces()) {
      if (p instanceof Stack) {
        final Stack s = (Stack) p;
        for (int i=0; i < s.getPieceCount(); i++) {
          if (expression.accept(s.getPieceAt(i))) {
            pieces.add(s.getPieceAt(i));
          }
        }
      }
      else {
        if (expression.accept(p)) {
          pieces.add(p);
        }
      }
    }
    return pieces;
  }
}