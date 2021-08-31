/*
 * $Id: TerrainMap.java 9201 2015-09-23 01:28:23Z swampwallaby $
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

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import VASSAL.build.GameModule;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;

/**
 * The terrain map. Holds all defined terrain for a board, plus appropriate
 * indexes to ensure fast access
 */
public class TerrainMap {
  
  public static final String CHAR_SET = "UTF-8";
  public static final String MAP_DIR = "terrainMaps";
  public static final String FILE_SUFFIX = "txt";

  protected static final String NO_TERRAIN = "No Terrain";
  
  protected Board board;
  protected TerrainHexGrid grid;
  protected HashMap<HexRef, TerrainHex> hexMap;   // Keyed by Point = Hex Grid Position
  protected HashMap<String, Area> hexArea;
  protected HashMap<EdgeRef, TerrainEdge> edgeMap;  // Keyed by Rect = 2 Hex Grid Positions
  protected HashMap<String, GeneralPath> edgePoly; 
  protected HashMap<EdgeRef, TerrainLine> lineMap;  // Keyed by EdgeRef
  protected HashMap<String, GeneralPath> linePoly;
  protected HashMap<AttrRef, TerrainAttribute> attributeMap; // Keyed by AttrRef = Point/Attribute name
  protected SortedSet<AttrRef> attributeList;  // Tags sorted by hex
  
  public TerrainMap() {
    hexMap = new HashMap<HexRef, TerrainHex>();
    hexArea = new HashMap<String, Area>();
    edgeMap = new HashMap<EdgeRef, TerrainEdge>();
    edgePoly = new HashMap<String, GeneralPath>();
    lineMap = new HashMap<EdgeRef, TerrainLine>();
    linePoly = new HashMap<String, GeneralPath>();
    attributeMap = new HashMap<AttrRef, TerrainAttribute>();
    attributeList = new TreeSet<AttrRef>(new AttrComp());
  }
  
  public TerrainMap(TerrainHexGrid g) {
    this();
    grid = g;
    board = grid.getBoard();
    load();
  }
  
  public void setHexTerrainType(TerrainHex hex) {
    if (hex.getTerrain() == null) {
      hexMap.remove(hex.getLocation());
    }
    else {
      hexMap.put(hex.getLocation(), hex);
    }
    clearHexAreas();
  }

  /*
   * Add edge to map twice, so that we can look it up from
   * either hex
   */
  public void setEdgeTerrainType(TerrainEdge edge) {
    if (edge.getTerrain() == null) {
      edgeMap.remove(edge.getLocation());
      edgeMap.remove(edge.getReverseLocation());
    }
    else {
      edgeMap.put(edge.getLocation(), edge);
      edgeMap.put(edge.getReverseLocation(), edge.reversed());
    }
    rebuildEdgeAreas();
  }
  
  public void setLineTerrainType(TerrainLine line) {
    if (line.getTerrain() == null) {
      lineMap.remove(line.getLocation());
    }
    else {
      lineMap.put(line.getLocation(), line);
    }
    rebuildLineAreas();
  }
  
  public void setAttributeTerrainType(TerrainAttribute attr) {
    final String value = attr.getValue();
    if (attr.getTerrain() == null || value == null || value.length() == 0) {      
      final AttrRef ref = new AttrRef(attr.getLocation(), attr.getName());
      attributeMap.remove(ref);
      attributeList.remove(ref);  
    }
    else {
      final AttrRef ref = new AttrRef(attr.getLocation(), attr.getName());
      attributeMap.put(ref, attr);
      attributeList.add(ref); 
    }
  }
  
  public void setHexTerrainType(ArrayList<HexRef> hexes, HexTerrain terrain) {
    for (HexRef hexRef : hexes) {
      setHexTerrainType(new TerrainHex(hexRef, terrain, grid));
    }
  }

  public void setEdgeTerrainType(ArrayList<EdgeRef> edges, TerrainHexGrid grid, EdgeTerrain terrain) {
    for (EdgeRef edgeRef : edges) {
      setEdgeTerrainType(new TerrainEdge(edgeRef, terrain, grid));
    }
  }
  
  public void setLineTerrainType(ArrayList<LineRef> lines, TerrainHexGrid grid, LineTerrain terrain) {
    for (LineRef lineRef : lines) {
      setLineTerrainType(new TerrainLine(lineRef, terrain, grid));
    }
  }
   
  public TerrainHex getHexTerrain(HexRef hexPos) {
    return hexMap.get(hexPos);
  }

  public Collection<TerrainHex> getAllHexTerrain() {
    return hexMap.values();
  }

  public TerrainEdge getEdgeTerrain(HexRef hexPos1, HexRef hexPos2) {
    final EdgeRef key = new EdgeRef(hexPos1, hexPos2, grid);
    return edgeMap.get(key);
  }
  
  public Iterator<TerrainEdge> getAllEdgeTerrain() {
    return edgeMap.values().iterator();
  }

  public Iterator<TerrainLine> getAllLineTerrain() {
    return lineMap.values().iterator();
  }
  
  public Iterator<TerrainAttribute> getAllAttributeTerrain() {
    return attributeMap.values().iterator();
  }
  
  public Iterator<TerrainAttribute> getSortedAttributeTerrain() {
    return new Iterator<TerrainAttribute>() {
      Iterator<AttrRef> i = attributeList.iterator();
      
      public boolean hasNext() {
        return i.hasNext();
      }

      public TerrainAttribute next() {       
        return attributeMap.get(i.next());
      }

      public void remove() {
        return;        
      }};
  }
  
  public TerrainAttribute getAttributeTerrain(HexRef hexPos) {   
    return attributeMap.get(hexPos);
  }
  
  public Area getHexArea(String terrainType) {
    if (terrainType.equals(NO_TERRAIN)) {
      return null;
    }
    Area area = hexArea.get(terrainType);
    if (area == null) {
      rebuildHexArea(terrainType);
      area = hexArea.get(terrainType);
    }
    return area;
  }

  public GeneralPath getEdgePoly(String terrainType) {
    if (terrainType.equals(NO_TERRAIN)) {
      return null;
    }
    GeneralPath poly = edgePoly.get(terrainType);
    if (poly == null) {
      rebuildEdgeAreas();
      poly = edgePoly.get(terrainType);
    }
    return poly;
  }
  
  public GeneralPath getLinePoly(String terrainType) {
    if (terrainType.equals(NO_TERRAIN)) {
      return null;
    }
    GeneralPath poly = linePoly.get(terrainType);
    if (poly == null) {
      rebuildLineAreas();
      poly = linePoly.get(terrainType);
    }
    return poly;
  }
  
  public Collection<String> getHexAreaTypes() {
    return hexArea.keySet();
  }

  public Collection<String> getEdgeAreaTypes() {
    return edgePoly.keySet();
  }
  
  protected void clearHexAreas() {
    hexArea.clear();
  }

  protected void clearEdgeAreas() {
    edgePoly.clear();
  }
  
  protected void clearLineAreas() {
    linePoly.clear();
  }
  
  public void rebuild() {
    rebuildHexAreas();
    rebuildEdgeAreas();
    rebuildLineAreas();
  }
  
  protected void rebuildHexAreas() {
    clearHexAreas();
    for (TerrainHex hex : getAllHexTerrain()) {
      final String type = hex.getTerrain().getConfigureName();
      Area area = hexArea.get(type);
      if (area == null) area = new Area();
      area.add(grid.getSingleHex(hex));
      hexArea.put(type, area);
    }  
  }
  
  protected void rebuildHexArea(String type) {
    hexArea.remove(type);
    for (TerrainHex hex : getAllHexTerrain()) {
      final String t = hex.getTerrain().getConfigureName();
      if (t.equals(type)) {
        Area area = hexArea.get(type);
        if (area == null) area = new Area();
        area.add(grid.getSingleHex(hex));
        hexArea.put(type, area);
      }
    }
  }
  
  protected void rebuildEdgeAreas() {
    clearEdgeAreas();
    final Iterator<TerrainEdge> i = getAllEdgeTerrain();
    while (i.hasNext()) {
      final TerrainEdge edge = i.next();
      edge.recalculate();
      final String type = edge.getTerrain().getConfigureName();
      GeneralPath poly = edgePoly.get(type);
      if (poly == null) poly = new GeneralPath();
      Point2D.Float end = edge.getEnd1();
      poly.moveTo(end.x, end.y);
      end = edge.getEnd2();
      poly.lineTo(end.x, end.y);
      edgePoly.put(type, poly);
    }  
  }
  
  protected void rebuildLineAreas() {
    clearLineAreas();
    final Iterator<TerrainLine> i = getAllLineTerrain();
    while (i.hasNext()) {
      final TerrainLine line = i.next();
      final String type = line.getTerrain().getConfigureName();
      GeneralPath poly = linePoly.get(type);
      if (poly == null) poly = new GeneralPath();
      Point2D.Float end = line.getEnd1();
      poly.moveTo(end.x, end.y);
      end = line.getEnd2();
      poly.lineTo(end.x, end.y);
      linePoly.put(type, poly);
    }  
  }
  
  public void save() {
    final StringBuilder buffer = new StringBuilder(2000);
    
    for (TerrainHex hex : hexMap.values()) {
      buffer.append(hex.encode());
      buffer.append(System.getProperty("line.separator"));
    }

    /*
     * Edges appear in the TerrainMap twice, once for each hex they seperate.
     * Only write them out once.
     */
    for (TerrainEdge edge : edgeMap.values()) {
      final EdgeRef ref = edge.getLocation();
      if (ref.getColumn() < ref.getColumn2() || 
          (ref.getColumn() == ref.getColumn2() && 
              ref.getRow() <= ref.getRow2())) {
        buffer.append(edge.encode());
        buffer.append(System.getProperty("line.separator"));
      }
    }
    
    for (TerrainLine line : lineMap.values()) {
      buffer.append(line.encode());
      buffer.append(System.getProperty("line.separator"));
    }
    
    for (TerrainAttribute attr : attributeMap.values()) {
      if (attr.getValue() != null && attr.getValue().length() > 0) {
        buffer.append(attr.encode());
        buffer.append(System.getProperty("line.separator"));
      }
    }
    
    final ArchiveWriter writer = GameModule.getGameModule().getArchiveWriter();
    byte[] bytes = new byte[0];
    try {
      bytes = buffer.toString().getBytes(CHAR_SET);
    }
    catch (Exception e) {
      
    }
    writer.addFile(getMapFileName(board), bytes);
    
  }
  
  public void load() {
    try {
      final DataArchive archive = GameModule.getGameModule().getDataArchive();
    
      InputStream stream = null;
      try {
        stream = archive.getInputStream(getMapFileName(board));

        final InputStreamReader reader = new InputStreamReader(stream, CHAR_SET);
        final BufferedReader buffer = new BufferedReader(reader);
        for (String line = buffer.readLine(); line != null; line = buffer.readLine()) {
          if (line.startsWith(TerrainHex.TYPE)) {
            addHexTerrain(line);
          }
          else if (line.startsWith(TerrainEdge.TYPE)) {
            addEdgeTerrain(line);
          }
          else if (line.startsWith(TerrainLine.TYPE)) {
            addLineTerrain(line);
          }
          else if (line.startsWith(TerrainAttribute.TYPE)) {
            addAttributeTerrain(line);
          }
        }
      }
      finally {
        try {
          stream.close();
        }
        catch (Exception e) {
          ;
        }        
      }
    }
    catch (Exception e) {

    }
  }
  
  public String getMapFileName(Board board) {
    return MAP_DIR + "/" + board.getName() + "." + FILE_SUFFIX;
  }

  protected void addHexTerrain(String line) {
    final TerrainHex hex = new TerrainHex(line, grid);
    setHexTerrainType(hex);    
  }
  
  protected void addEdgeTerrain(String line) {
    final TerrainEdge edge = new TerrainEdge(line, grid);
    setEdgeTerrainType(edge);    
  }

  protected void addLineTerrain(String line) {
    final TerrainLine tl = new TerrainLine(line, grid);
    setLineTerrainType(tl);    
  }
  
  protected void addAttributeTerrain(String line) {
    final TerrainAttribute attr = new TerrainAttribute(line, grid);
    setAttributeTerrainType(attr);    
  }
  
  static class AttrComp implements Comparator<AttrRef>{

   public int compare(AttrRef a1, AttrRef a2) {
       int i = ((HexRef) a1).compareTo(a2);
       if (i == 0)
          return a1.getAttributeName().compareTo(a2.getAttributeName());
       return i;
    }
  }    

}