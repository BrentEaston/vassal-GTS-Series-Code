/*
 * $Id: HexBlockingTerrain.java 9192 2015-05-16 01:24:32Z swampwallaby $
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

package tdc;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import VASSAL.build.module.Map;
import terrain.HexRef;
import terrain.HexTerrain;
import terrain.TerrainHex;

public class HexBlockingTerrain extends BlockingTerrain {

  protected Area shape; // The displayable shape of this Hex after zooming
                        // (Zoomed Map-coords)
  protected Area mapShape; // The unzoomed shape of the Hex (Zoom = 1 Map
                           // co-ords)
  protected Area fatShape; // An oversized version of mapShape for testing
                           // intersection with spinal LOS

  protected double lastZoom = 0;
  protected TerrainHex hex;
  protected Rectangle boardOffset;

  public HexBlockingTerrain(TdcMap map, String description) {
    super(map, description);
  }
  
  public HexBlockingTerrain(TdcMap map, String terrainName, Rectangle offset) {
    super(map, terrainName);
    this.boardOffset = offset;    
  }

  public HexBlockingTerrain(TdcMap map, TerrainHex hex, Rectangle offset) {
    this(map, hex.getTerrain().getTerrainName(), offset);
    setCenter(offset(hex.getLocation().getCenter(), offset));
    final HexTerrain ter = hex.getHexTerrain();
    setPossiblyBlocking(ter.isPossibleBlocking());
    this.hex = hex;
  }

  public HexBlockingTerrain(HexBlockingTerrain hbt) {
    this(hbt.map, hbt.hex, hbt.boardOffset);
  }

  public HexBlockingTerrain(HexBlockingTerrain hbt, boolean possiblyBlocked) {
    this(hbt);
    setPossiblyBlocking(possiblyBlocked);
  }

  // Draw the blocked Hex
  public void draw(Graphics g, TdcThread thread) {
    draw(g, thread.getPossibleColor(), thread.getBlockColor());
  }
  
  public void draw(Graphics g, WizardThread thread) {
    draw(g, thread.getPossibleColor(), thread.getBlockColor());
  }
  
  protected void draw (Graphics g, Color possibleColor, Color blockColor) {

    final Graphics2D g2 = (Graphics2D) g;

    final Stroke saveStroke = g2.getStroke();
    final Shape saveClip = g2.getClip();
    final Composite oldComposite = g2.getComposite();

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

    g2.setClip(getZoomedShape());
    g2.setColor(isPossiblyBlocking() ? possibleColor : blockColor);
    g2.setStroke(new BasicStroke((float) (20.0f * map.getZoom() * Info.getSystemScaling()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g2.draw(getZoomedShape());
    g2.setStroke(saveStroke);
    g2.setClip(saveClip);
    g2.setComposite(oldComposite);
  }

  // Get the displayable shape of this Hex.
  public Area getZoomedShape() {
    if (shape == null || lastZoom != map.getZoom()) {
      calculateZoomedShape();
    }
    return shape;
  }

  // Get the unzoomed Shape
  public Area getMapShape() {
    if (mapShape == null) {
      calculateBaseShape();
    }
    return mapShape;
  }

  // Get a larger sized unzoomed shape
  public Area getFatShape() {
    if (fatShape == null) {
      calculateFatShape();
    }
    return fatShape;
  }

  // Calculate the shape of this Hex 
  protected void calculateBaseShape() {

    // Get the Shape in Board coords and convert it to Map coords
    final HexRef hexLocation = getHexLocation();
    mapShape = hexLocation.getGrid().getSingleHexByRC(hexLocation.getColumn(), hexLocation.getRow());
    transformBaseShape();
  }
  
  protected void transformBaseShape() {
    final AffineTransform translate = new AffineTransform();
    translate.translate(boardOffset.x, boardOffset.y);
    mapShape.transform(translate);
  }
  
  protected HexRef getHexLocation() {
    return hex.getLocation();
  }
  
  protected void calculateFatShape() {
    final HexRef hexLocation = getHexLocation();
    fatShape = hexLocation.getGrid().getSingleFatHex(hexLocation.getColumn(), hexLocation.getRow());
    final AffineTransform translate = new AffineTransform();
    translate.translate(boardOffset.x, boardOffset.y);
    fatShape.transform(translate);
  }

  //
  // Calculate the Zoomed shape
  protected void calculateZoomedShape() {

    if (mapShape == null) {
      calculateBaseShape();
    }

    shape = new Area(mapShape);
    final double os_scale = Info.getSystemScaling();
    
   // if (map.getZoom() < .999 || map.getZoom() > 1.001) {
      final AffineTransform scale = new AffineTransform();
      scale.scale(map.getZoom() * os_scale, map.getZoom() * os_scale);
      shape.transform(scale);
   // }
    lastZoom = map.getZoom();
    
  }


  @Override
  public boolean hasCenter(Point p) {
    if (hex==null) {
      return false;
    }
    HexRef hexRef = hex.getLocation();

    Point center = hexRef.convertCenterToMapCoords(map, hexRef.getGrid().getBoard());
    return (p.equals(center));
  }

  // Does the Hex intersect with the supplied LOS?
  public boolean intersects(LOS los) {
    
    if (los.isSpinal()) {
      return los.intersects(getFatShape());
    }
    else {
      return los.intersects(getMapShape());
    }

  }

}