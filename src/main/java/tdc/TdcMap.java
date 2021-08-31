/*
 * $Id: TdcMap.java 9301 2020-03-05 10:53:44Z swampwallaby $
 *
 * Copyright (c) 2005-2017 by Brent Easton
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
  * 12-Jul-17 Support Barrage counters blocking LOS
  */

package tdc;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

import qtree.QuadTreeException;
import terrain.HexRef;
import terrain.TerrainBasicPiece;
import terrain.TerrainHexGrid;
import VASSAL.build.Buildable;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import VASSAL.build.module.map.boardPicker.board.mapgrid.Zone;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.GamePiece;

/**
 *
 */
public class TdcMap extends Map {

  protected AttackWizard attackWizard;
  protected AssaultWizard assaultWizard;
  protected BlockingTerrainMap terrainMap;
  //protected CombinedEdgeMap edgeMap;

  // Is the specified map one of the Main maps?
  public static boolean isMainMap(Map m) {
    return m != null && m.getConfigureName().startsWith("Map");
  }

  public static boolean isOnMainMap(GamePiece p) {
    return isMainMap(p.getMap());
  }

  public AttackWizard getAttackWizard() {
    return attackWizard;
  }


  public void setAttackWizard(AttackWizard attackWizard) {
    this.attackWizard = attackWizard;
  }
  
  public AssaultWizard getAssaultWizard() {
    return assaultWizard;
  }

  public void setAssaultWizard(AssaultWizard assaultWizard) {
    this.assaultWizard = assaultWizard;
  }

  
  public void addTo(Buildable b) {
    super.addTo(b);
    TdcHighlighter highlighter = new TdcHighlighter();
    setHighlighter(highlighter);
    BasicPiece.setHighlighter(highlighter);
  }

  //public Point componentCoordinates(Point p1) {
  //  return new Point((int) Math.round(p1.x * getZoom()), (int) Math.round(p1.y * getZoom()));
  //}

  public Point2D.Float componentCoordinates2D(Point2D.Float p1) {
    return new Point2D.Float(p1.x * (float) getZoom(), p1.y * (float) getZoom());
  }
  
  public String getTerrainName(Point pos) {
    final TerrainHexGrid hgrid = findTerrainHexGrid(pos);
    if (hgrid != null) {
      return hgrid.getTerrainName(pos);
    }
    return "";
  }

  public static Object getTerrainProperty(GamePiece p, String prop) {
    Object value = null;
    final Map map = p.getMap();
    if (map instanceof TdcMap) {
      final Point pos = p.getPosition();
      final TerrainHexGrid hgrid = ((TdcMap) map).findTerrainHexGrid(pos);
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
  protected boolean isAttached() {
    return !launchButton.isVisible();
  }

  // Is this map visible?
  protected boolean isVisible() {
    return getView().isVisible();
  }
  
  public void setup(boolean starting) {
    super.setup(starting);
    terrainMap = null;
    if (starting) {
      buildLOSMap();
    }
  }
  
  protected void buildLOSMap() {
    try {
      terrainMap = new BlockingTerrainMap(this);
    }
    catch (QuadTreeException ignored) {

    }
  }
  
  // Update the barrage info
  public void initBlockingTerrain () {
    if (terrainMap != null) {
      terrainMap.updateBarrage();
    }
  }
  
  // Find blocking terrain along the LOS
  public void findBlockingTerrain (Point start, Point end, Set<BlockingTerrain> blockingTerrain) {
     blockingTerrain.clear();
    if (terrainMap != null) {
      terrainMap.findBlockingTerrain(start, end, blockingTerrain);
    }
  }

  // Direction 1=N 2=NE 3=SE 4=S 5=SW 6=NW
  public Point returnAdjacentHex(Point p, int direction) {
    if (UnitInfo.isAmdRules()) {
      return returnAdjacentHexAMD(p, direction);
    }
    final Point2D.Float center = new Point2D.Float(p.x, p.y);
    final Rectangle boardOffset = this.findBoard(p).bounds();
    final TerrainHexGrid grid = this.findTerrainHexGrid(p);
    final HexRef hex = grid.getHexPos(new Point(p.x - boardOffset.x, p.y - boardOffset.y));

    final int hexColumn = hex.getColumn();
    final int hexRow = hex.getRow();

    int scatterRow = 0;
    int scatterCol = 0;
    switch (direction)   {
      case 1:
        scatterRow = -1;
        scatterCol = 0;
        break;
      case 2:
        scatterRow = 0;
        scatterCol = 1;
        if (hexColumn % 2 == 0) {
          scatterRow = -1;
        }
        break;
      case 3:
        scatterRow = 1;
        scatterCol = 1;
        if (hexColumn % 2 == 0) {
          scatterRow = 0;
        }
        break;
      case 4:
        scatterRow = 1;
        scatterCol = 0;
        break;
      case 5:
        scatterRow = 1;
        scatterCol = -1;
        if (hexColumn % 2 == 0) {
          scatterRow = 0;
        }
        break;
      case 6:
        scatterRow = 0;
        scatterCol = -1;
        if (hexColumn % 2 == 0) {
          scatterRow = -1;
        }
        break;
    }

    hex.setRow(hexRow + scatterRow);
    hex.setColumn(hexColumn + scatterCol);
    return hex.convertCenterToMapCoords(this, this.findBoard(p));
  }

  public Point returnAdjacentHexAMD(Point p, int direction) {
    final Point2D.Float center = new Point2D.Float(p.x, p.y);
    final Rectangle boardOffset = this.findBoard(p).bounds();
    final TerrainHexGrid grid = this.findTerrainHexGrid(p);
    final HexRef hex = grid.getHexPos(new Point(p.x - boardOffset.x, p.y - boardOffset.y));
    final int hexColumn = hex.getColumn();
    final int hexRow = hex.getRow();

    int scatterRow = 0;
    int scatterCol = 0;
    switch (direction)   {
      case 1:
        scatterRow = -1;
        scatterCol = 0;
        if (hexRow % 2 == 0) {
          scatterCol = -1;
        }
        break;
      case 2:
        scatterRow = -1;
        scatterCol = 1;
        if (hexRow % 2 == 0) {
          scatterCol = 0;
        }
        break;
      case 3:
        scatterRow = 0;
        scatterCol = 1;
        break;
      case 4:
        scatterRow = 1;
        scatterCol = 1;
        if (hexRow % 2 == 0) {
          scatterCol = 0;
        }
        break;
      case 5:
        scatterRow = 1;
        scatterCol = 0;
        if (hexRow % 2 == 0) {
          scatterCol = -1;
        }
        break;
      case 6:
        scatterRow = 0;
        scatterCol = -1;
        break;
    }

    hex.setRow(hexRow + scatterRow);
    hex.setColumn(hexColumn + scatterCol);
    return hex.convertCenterToMapCoords(this, this.findBoard(p));
  }
}
