/*
 * $Id: BlockingTerrainMap.java 9192 2015-05-16 01:24:32Z swampwallaby $
 *
 * Copyright (c) 2015=2017 by Brent Easton
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
 * Use a QuadTree to record all blocking terrain from all boards on a Map.
 */

/*
 * 12-Jul-17 Handle Barrage markers.
 */
package tdc;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import VASSAL.counters.*;
import VASSAL.counters.Stack;
import qtree.QPoint;
import qtree.QuadTree;
import qtree.QuadTreeException;
import terrain.AttributeTerrain;
import terrain.EdgeTerrain;
import terrain.HexTerrain;
import terrain.TerrainAttribute;
import terrain.TerrainEdge;
import terrain.TerrainHex;
import terrain.TerrainHexGrid;
import terrain.TerrainMap;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;

public class BlockingTerrainMap
{

  protected TdcMap map;
  protected QuadTree terrain;
  protected ArrayList<HexBlockingTerrain> barragedHexes = new ArrayList<>();

  public BlockingTerrainMap(TdcMap map)
  {
    this.map = map;
    build();
  }

  // Build the combined terrain map from the terrain maps for each board
  protected void build()
  {
    int minX = 9999999;
    int minY = 9999999;
    int maxX = 0;
    int maxY = 0;

    // Find the bounds of the entire Map
    for (Board b : map.getBoards())
    {
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

    terrain = new QuadTree(minX, minY, maxX, maxY);

    // Build the map from blocking terrain recorded on each board
    for (Board b : map.getBoards())
    {
      final Rectangle bounds = b.bounds();
      final TerrainHexGrid grid = map.findTerrainHexGrid(new Point(bounds.x, bounds.y));
      if (grid != null)
      {
        processTerrainMap(b, grid.getTerrainMap());
      }
    }

  }

  public void updateBarrage()
  {
    barragedHexes.clear();

    for (GamePiece p : map.getAllPieces())
    {
      if (p instanceof Stack)
      {
        for (int i = 0; i < ((Stack) p).getPieceCount(); i++)
        {
          final GamePiece unit = ((Stack) p).getPieceAt(i);
          if (TdcProperties.TYPE_BARRAGE.equals(unit.getProperty(TdcProperties.TYPE)))
          {
            if (map.findTerrainHexGrid(unit.getPosition()) != null)
            {
              final HexBlockingTerrain bt = new BarrageBlockingTerrain(map, unit.getPosition());
              barragedHexes.add(bt);
            }
          }
        }
      }
    }
  }

  protected void processTerrainMap(Board board, TerrainMap tmap)
  {
    if (tmap == null)
    {
      return;
    }

    Rectangle bounds = board.bounds();

    // Process Edges
    for (Iterator<TerrainEdge> i = tmap.getAllEdgeTerrain(); i.hasNext(); )
    {
      final TerrainEdge edge = i.next();
      final EdgeTerrain ter = edge.getEdgeTerrain();
      if (ter.isBlocking())
      {
        final BlockingTerrain block = new EdgeBlockingTerrain(map, edge, bounds);
        try
        {
          terrain.set(block.getX(), block.getY(), block);
        } catch (QuadTreeException ignored)
        {
          // Centre point of Hex outside map bounds.
        }
      }
    }

    // Process Hexes
    for (TerrainHex hex : tmap.getAllHexTerrain())
    {
      final HexTerrain ter = hex.getHexTerrain();
      if (ter.isBlocking() || ter.isPossibleBlocking())
      {
        final BlockingTerrain block = new HexBlockingTerrain(map, hex, bounds);
        try
        {
          terrain.set(block.getX(), block.getY(), block);
        } catch (QuadTreeException ignored)
        {
          // Centre point of Hex outside map bounds.
        }
      }
    }

    // And Tags
    for (Iterator<TerrainAttribute> i = tmap.getAllAttributeTerrain(); i.hasNext(); )
    {
      final TerrainAttribute attribute = i.next();
      final AttributeTerrain ter = attribute.getAttributeTerrain();
      if (ter.isBlocking() || ter.isPossibleBlocking())
      {
        final BlockingTerrain abt = new AttrBlockingTerrain(map, attribute, bounds);
        BlockingTerrain existing = (BlockingTerrain) terrain.get(abt.getX(), abt.getY(), null);

        // This Hex not recorded yet,
        if (existing == null)
        {
          existing = abt;
        }
        // Hex is already blocked, ignore this attribute
        else if (!existing.isPossiblyBlocking())
        {
          existing.addReason(attribute.getName());
          continue;
        }
        // hex is already Possibly blocked and attribute is possibly blocked,
        // ignore
        else if (existing.isPossiblyBlocking() && abt.isPossiblyBlocking())
        {
          existing.addReason(attribute.getName());
          continue;
        }
        // Hex is only possibly blocked, but attribute is Blocking, upgrade
        // existing to blocked.
        else
        {
          existing.addReason(attribute.getName());
          existing.setPossiblyBlocking(false);
        }

        // Set the new or updated block.
        try
        {
          terrain.set(existing.getX(), existing.getY(), existing);
        } catch (QuadTreeException ignored)
        {
          // Centre point of Hex outside map bounds.
        }
      }
    }
  }

  public int getHexBarrageLevel(Point p) {
    int level = 0;
    for (HexBlockingTerrain bh : barragedHexes) {
      if (bh.hasCenter(p)) {
        final Stack s = (Stack) map.findPiece(p, PieceFinder.STACK_ONLY);
        for (final GamePiece gamePiece : s.asList()) {
          final String name = gamePiece.getName();
          switch (name) {
            case "Light Barrage":
              if (level < 1) {
                level = 1;
              }
              break;
            case "Heavy Barrage":
              if (level < 2) {
                level = 2;
              }
              break;
          }
        }
      }
    }
    return level;
  }

  // Does not check the barrage level
  public boolean isHexBarraged(Point p) {
    for (HexBlockingTerrain bh : barragedHexes) {
      if (bh.hasCenter(p)) {
        return true;
      }
    }
    return false;
  }

  /*
   * Find all blocking terrain that intersects with the LOS from start to end
   */
  public void findBlockingTerrain(Point start, Point end, Set<BlockingTerrain> blockingTerrain)
  {
    Set<BlockingTerrain> tempBlockingTerrain = new HashSet<>();
    Set<BlockingTerrain> tempBlockingTerrain2 = new HashSet<>();
    Double range = 0.0;

    if (blockingTerrain == null)
    {
      blockingTerrain = new HashSet<>();
    } else
    {
      blockingTerrain.clear();
    }

    // If the LOS runs along Hex edges, then the standard intersection testing
    // fails.
    final LOS los = new LOS(map, start, end);

    // Search for all terrain with the bounding box defining the LOS. Add a 50
    // pixel fudge to pick up
    // hexes and edges whose center maybe just outside the bounding box.
    // Return any blocking terrain that intersects with the LOS.

    final int minX = Math.min(start.x, end.x) - 60;
    final int minY = Math.min(start.y, end.y) - 60;
    final int maxX = Math.max(start.x, end.x) + 60;
    final int maxY = Math.max(start.y, end.y) + 60;

    final QPoint[] results = terrain.searchIntersect(minX, minY, maxX, maxY);

    for (QPoint qp : results)
    {
      final BlockingTerrain bt = (BlockingTerrain) qp.getValue();
      if (!bt.hasCenter(start) && !bt.hasCenter(end))
      {
        if (bt.intersects(los))
        {
          tempBlockingTerrain.add(bt);
        }
      }
    }

    // Remove Crest/Ridge adjacent to either source or target if it is the only one
    boolean edgeOfStart = false;
    boolean edgeOfEnd = false;
    for (BlockingTerrain bt : tempBlockingTerrain) {
      if (bt instanceof EdgeBlockingTerrain) {
        if (((EdgeBlockingTerrain) bt).isEdgeOf((Point2D) start)) {
          edgeOfStart = true;
        } else { //Only checks for edge of En if not already edge of source (if source/target adj, edge is attributed to source
          if (((EdgeBlockingTerrain) bt).isEdgeOf((Point2D) end)) {
            edgeOfEnd = true;
          }
        }
      }
    }
    if (!(edgeOfStart && edgeOfEnd)) {
      Iterator<BlockingTerrain> i = tempBlockingTerrain.iterator();
      while (i.hasNext()) {
        BlockingTerrain bt = i.next();
        // remove the single edge
        if (edgeOfStart) {
          if (bt instanceof EdgeBlockingTerrain) {
            if (((EdgeBlockingTerrain) bt).isEdgeOf((Point2D) start)) {
              i.remove();
            }
          }
        }
        if (edgeOfEnd) {
          if (bt instanceof EdgeBlockingTerrain) {
            if (((EdgeBlockingTerrain) bt).isEdgeOf((Point2D) end)) {
              i.remove();
            }
          }
        }
      }
    }

    // Check Barraged hexes. If there is already an entry for a hex with a barrage counter,
    // then just use it, upgrading from possibly blocking to blocking if necessary.
    for (HexBlockingTerrain bh : barragedHexes)
    {
      if (!bh.hasCenter(start) && !bh.hasCenter(end))
      {
        if (bh.intersects(los))
        {
          BlockingTerrain existing = null;
          for (BlockingTerrain b : tempBlockingTerrain)
          {
            if (b instanceof HexBlockingTerrain)
            {
              HexBlockingTerrain hbt = (HexBlockingTerrain) b;
              if (hbt.getHexLocation().equals(bh.getHexLocation()))
              {
                existing = b;
                break;
              }
            }
          }
          if (existing == null)
          {
            tempBlockingTerrain.add(bh);
          } else
          {
            existing.setPossiblyBlocking(false);
          }
        }
      }
    }

    // Crete or AMD
    // Remove 1st orchard, mark second + subsequent orchards as blocking.
    // Same with Scrub (Crete)
    if (UnitInfo.isAmdRules()||UnitInfo.isCreteRules()) {
      // 1st remove any possibly blocking orchards/scrubs where los runs along hexsides
      // The above situation can only occur when the LOS is Spinal. Else keep the temp result as is
      if (!los.isSpinal())
      {
        tempBlockingTerrain2.addAll(tempBlockingTerrain);
      } else
      {
        // Scan for orchard/scrubs
        // If vector from start to blocking hex is // to los, then keep it
        // else check if range to that hex matches range of another scrub/orchard or Blocking hex
        // if found it is a pair of scrubs/orchards hexes where LOS runs along the common hexside
        // if not found exclude that blocking hex
        for (BlockingTerrain bt : tempBlockingTerrain)
        {
          TerrainHex th;
          String terrainName = "";
          if (bt.getClass() == HexBlockingTerrain.class) {
//          if (bt instanceof HexBlockingTerrain) { BarrageBlockingTerrain is an extension of HexBlockingTerrain, but has no terrain so NPEs below
            th = ((HexBlockingTerrain) bt).hex;
            //HexTerrain hexTerrain = (HexTerrain) th.getTerrain();
            //if (hexTerrain==null) {
            //  return;
            //}
            terrainName = th.getTerrain().getTerrainName();
          }
          if (!(terrainName.equals("Orchard") || terrainName.equals("Scrub")))
          {
            tempBlockingTerrain2.add(bt);
          } else
          {
            if (isBtParallelToLOS(start, end, bt))
            {
              tempBlockingTerrain2.add(bt);
            } else
            {
              range = bt.calcRange((Point2D) start);
              for (BlockingTerrain bt2 : tempBlockingTerrain)
              {
                terrainName = "";
                if (bt instanceof HexBlockingTerrain) {
                  if (!bt.equals(bt2))
                  {
                    final Double range2 = bt2.calcRange((Point2D) start);
                    if (Math.abs(range - range2)<5.0)
                    {
                      // This means LOS runs along hexside between btw bt and bt2
                      // Keep this blocking terrain.
                      tempBlockingTerrain2.add(bt);
                    }
                  }
                }
              }
            }
          }
        }
      }

      range = 99999999.0;
      tempBlockingTerrain.clear();
      HexBlockingTerrain nonBlockingOrchard = null;
      for (BlockingTerrain bt : tempBlockingTerrain2) {
        if (bt instanceof HexBlockingTerrain) {
          final TerrainHex th = ((HexBlockingTerrain) bt).hex;
          if (th != null && th.getTerrain() != null) {
            String terrainName = th.getTerrain().getTerrainName();
            if (terrainName.equals("Orchard") || terrainName.equals("Scrub")) {
              final Double range2 = bt.calcRange((Point2D) start);
              if (range2 < range) {
                range = range2;
                nonBlockingOrchard = (HexBlockingTerrain) bt;
              }
            }
          }
        }
        bt.possiblyBlocking = false;
        tempBlockingTerrain.add(bt);
      }
      tempBlockingTerrain.remove(nonBlockingOrchard);
    }

    // Remove blocking / possibly blocking hex when LOS runs exactly along hexside
    // and only one of the 2 hexes touching that hexside is blocking / possibly blocking
    // See GTS 2.0b 15.4.1 "A Line of Sight does exist along a hexside between two
    // hexes, one with Blocking Terrain and the other without."

    // The above situation can only occur when the LOS is Spinal. Else keep the temp result as is
    if (!los.isSpinal())
    {
      blockingTerrain.addAll(tempBlockingTerrain);
    } else
    {

      // Scan the blocking terrains, keep all crests/ridges (EdgeBlodckingTerrain)
      // If vector from start to blocking hex is // to los, then include it
      // else check if range to that hex matches range of another blocking
      // if found it is a pair of blocking hexes where LOS runs along the common hexside
      // if not found exclude that blocking hex
      for (BlockingTerrain bt : tempBlockingTerrain)
      {
        if (!(bt instanceof HexBlockingTerrain))
        {
          blockingTerrain.add(bt);
        } else
        {
          if (isBtParallelToLOS(start, end, bt))
          {
            blockingTerrain.add(bt);
          } else
          {
            range = bt.calcRange((Point2D) start);
            for (BlockingTerrain bt2 : tempBlockingTerrain)
            {
              if (bt2 instanceof HexBlockingTerrain)
              {
                if (!bt.equals(bt2))
                {
                  final Double range2 = bt2.calcRange((Point2D) start);
                  if (Math.abs(range - range2)<5.0)
                  {
                    // This means LOS runs along hexside between btw bt and bt2
                    // Keep this blocking terrain.
                    blockingTerrain.add(bt);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public boolean isBtParallelToLOS(Point start, Point end, BlockingTerrain bt)
  {
    if ((end.x == start.x) && (Math.abs((double) (bt.getX() - start.x))<5.0)) {
      return true;
    }
    Double direction1 = (double) (end.y - start.y) / (double) (end.x - start.x);
    Double direction2 = (double) (bt.getY() - start.y) / (double) (bt.getX() - start.x);
    if (Math.abs(direction1-direction2) < 0.05)
    {
      return true;
    }
    return false;
  }
//  /*
//   * The intersection method of finding blocking terrain fails miserably when
//   * the LOS runs exactly along hex sides. A random mixture of fake positives
//   * and fake negatives results Instead, use this algorithm:
//   * 
//   * 1. Step along the LOS from start to finish in x pixel (unzoomed Map
//   * co-ordinate) steps. This will ensure we touch every hex along the LOS. Each
//   * point will either be within a hex or on an Edge
//   * 
//   * 2. If the point is within a Hex, then just check terrain and add if
//   * blocking
//   * 
//   * 3. If the point is on an edge then 3.1 Check the Edge and include it if it
//   * is blocking. 3.2 Identify the 2 hexes on either side of the edge and check
//   * them for blocking. - Neither blocking, do not add either - 1 is blocking,
//   * other is not, do not add either - both are full blocking, add both - both
//   * are possible blocking or 1 is full and 1 is possible, then add both as
//   * possible. 3.3 For each side hex, identify the 2 edges that run off the LOS
//   * and add them if blocking.
//   */
//  public void searchAlongLOS(LOS los, Set<BlockingTerrain> blockingTerrain) {
//
//    final HashSet<Point> checked = new HashSet<Point>();
//
//    final Board board = map.findBoard(los.getStart());
//    final Rectangle bounds = board.bounds();
//
//    final int startCheck = 100; // No point in starting search until outside the
//                                // starting hex
//    final int step = 40;
//
//    final int xLen = los.getEnd().x - los.getStart().x;
//    final int yLen = los.getEnd().y - los.getStart().y;
//
//    final int length = (int) Math.sqrt(xLen * xLen + yLen * yLen);
//
//    for (int i = startCheck; i < length; i += step) {
//      final int x = los.getStart().x + i * xLen / length;
//      final int y = los.getStart().y + i * yLen / length;
//      final Point p = new Point(x, y);
//
//      final TerrainHexGrid grid = map.findTerrainHexGrid(p);
//
//      // Snap point to nearest hex center
//      Point snap = snapToHex(p, grid, bounds);
//
//      // Is this hex on the LOS and has not been checked before
//
//      if (!checked.contains(snap)) {
//
//        if (los.isOnLine(snap)) {
//          // It's a new Hex right on the LOS. If is is blocking terrain, add it
//          // in.
//          final BlockingTerrain bt = findTerrain(snap);
//          if (bt != null) {
//            if (!blockingTerrain.contains(bt))
//              blockingTerrain.add(bt);
//          }
//        }
//      }
//      checked.add(new Point(snap));
//
//      // Now snap the same point to the nearest Hex edge
//      snap = snapToEdge(p, grid, bounds);
//
//      // Is this Edge on the LOS and has not been checked before?
//      if (!checked.contains(snap)) {
//        if (los.isOnLine(snap)) {
//          // It's a new Edge right on the LOS. If is blocking terrain, add it
//          // in.
//          final BlockingTerrain bt = findTerrain(snap);
//          if (bt != null) {
//            if (!blockingTerrain.contains(bt))
//              blockingTerrain.add(bt);
//          }
//
//          // Next need to check the hex on either side of the edge to see if
//          // both
//          // are blocking.
//          Point h1 = snapToHex(new Point(snap.x - 20, snap.y - 20), grid, bounds);
//          HexBlockingTerrain t1 = (HexBlockingTerrain) findTerrain(h1);
//
//          Point h2 = snapToHex(new Point(snap.x + 20, snap.y + 20), grid, bounds);
//          HexBlockingTerrain t2 = (HexBlockingTerrain) findTerrain(h2);
//
//          // If either is not blocking, then there is no block to LOS
//          // If either is possibly blocking, then record both as possibles.
//          // If both are full blocking
//          if (t1 != null && t2 != null) {
//            if (t1.isPossiblyBlocking() || t2.isPossiblyBlocking()) {
//              if (!t1.isPossiblyBlocking())
//                t1 = new HexBlockingTerrain((HexBlockingTerrain) t1, true);
//              if (!t2.isPossiblyBlocking())
//                t2 = new HexBlockingTerrain((HexBlockingTerrain) t2, true);
//            }
//            if (!blockingTerrain.contains(t1))
//              blockingTerrain.add(t1);
//            if (!blockingTerrain.contains(t2))
//              blockingTerrain.add(t2);
//          }
//
//          // Finally, check the edges of the straddling edges that touch the LOS
//          // and add them
//          BlockingTerrain e1 = null;
//          BlockingTerrain e2 = null;
//          findIntersectingEdges(los, h1, e1, e2, grid, bounds);
//          findIntersectingEdges(los, h2, e1, e2, grid, bounds);
//          
//          if (e1 != null && ! blockingTerrain.contains(e1)) blockingTerrain.add(e1);
//          if (e2 != null && ! blockingTerrain.contains(e2)) blockingTerrain.add(e2);
//
//        }
//
//        // Record this snap point as already been checked
//        checked.add(new Point(snap));
//
//      }
//    }
//  }
//
//  /**
//   * The Line from P1 to P2 runs along one of the Edges of the Hex whose center
//   * is center. Find the two edges of the Hex that intersect p1/p2 and return
//   * their terrain if blocking
//   * 
//   * @param los
//   *          The current LOS
//   * @param center
//   *          Centre of Hex in Map coords
//   * @param e1
//   *          Edge blocking terrain of 1st intersecting Edge
//   * @param e2
//   *          Edge blocking terrain of 2nd intersecting Edge
//   * @Param grid TerrainHexGrid of board containing hex
//   * @oaram bounds Bounds of Board containing hex
//   */
//  protected void findIntersectingEdges(LOS los, Point center, BlockingTerrain e1, BlockingTerrain e2, TerrainHexGrid grid, Rectangle bounds) {
//
//    e1 = null;
//    e2 = null;
//
//    final Point hexCenter = toBoardCoords(center, bounds);
//    final Point[] v = grid.getHexVertices(hexCenter.x, hexCenter.y);
//
//    // Find the 2 intersecting edges
//    int v1 = -1;
//    int v2 = -1;
//    for (int i = 0; i < v.length; i++) {
//      v[i] = toMapCoords(v[i], bounds);
//      if (los.isOnLine(v[i])) {
//        System.out.println("Vertex " + i + " is on LOS");
//        if (v1 < 0) {
//          v1 = i;
//        }
//        else {
//          v2 = i;
//        }
//      }
//    }
//    
//    if (v1 < 0 || v2 < 0) return;
//    
//    int w1s = 0, w1e = 0, w2s = 0, w2e = 0;
//  
//  
//    if (v1 == 0) {
//      if (v2 == 1) {
//        w1s = 0;
//        w1e = 5;
//        w2s = 1;
//        w2e = 2;
//      }
//      else {
//        w1s = 0;
//        w1e = 1;
//        w2s = 4;
//        w2e = 5;
//      }
//    }
//    else if (v1==4) {
//      w1s = 3;
//      w1e = 4;
//      w2s = 0;
//      w2e = 5;
//    }
//    else {
//      w1s = v1 - 1;
//      w1e = v1;
//      w2s = v2;
//      w2e = v2 + 1;
//    }
//        
//    Point e1p1 = v[w1s];
//    Point e1p2 = v[w1e];
//    Point e2p1 = v[w2s];
//    Point e2p2 = v[w2e];
//    
//    Point e1c = toBoardCoords(new Point (e1p1.x + (int) ((e1p2.x-e1p1.x)/2), e1p1.y + (int) ((e1p2.y-e1p1.y)/2)), bounds); 
//    Point e2c = toBoardCoords(new Point (e2p1.x + (int) ((e2p2.x-e2p1.x)/2), e2p1.y + (int) ((e2p2.y-e2p1.y)/2)), bounds);
//    
//    Point e1cs = toMapCoords(grid.snapToHexSide(e1c), bounds);
//    Point e2cs = toMapCoords(grid.snapToHexSide(e2c), bounds);
//    
//    e1 = findTerrain(e1cs);
//    e2 = findTerrain(e2cs);   
//    
//    
//  }
//
//  protected Point toBoardCoords(Point p, Rectangle offset) {
//    return new Point(p.x - offset.x, p.y - offset.y);
//  }
//
//  protected Point toMapCoords(Point p, Rectangle offset) {
//    return new Point(p.x + offset.x, p.y + offset.y);
//  }
//
//  protected Point snapToHex(Point p, TerrainHexGrid grid, Rectangle bounds) {
//    Point snap = new Point(p);
//    snap.translate(-bounds.x, -bounds.y);
//    snap = grid.snapToHex(snap);
//    snap.translate(bounds.x, bounds.y);
//    return snap;
//  }
//
//  protected Point snapToEdge(Point p, TerrainHexGrid grid, Rectangle bounds) {
//    Point snap = new Point(p);
//    snap.translate(-bounds.x, -bounds.y);
//    snap = grid.snapToHexSide(snap);
//    snap.translate(bounds.x, bounds.y);
//    return snap;
//  }
//
//  protected BlockingTerrain findTerrain(Point p) {
//
//    final QPoint[] results = terrain.searchIntersect(p.x - 5, p.y - 5, p.x + 5, p.y + 5);
//    if (results.length == 0) {
//      return null;
//    }
//    return (BlockingTerrain) results[0].getValue();
//  }



}