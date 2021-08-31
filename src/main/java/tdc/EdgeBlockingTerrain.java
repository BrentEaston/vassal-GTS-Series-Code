/*
 * $Id: EdgeBlockingTerrain.java 9192 2015-05-16 01:24:32Z swampwallaby $
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
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import terrain.TerrainEdge;
import terrain.TerrainHexGrid;

public class EdgeBlockingTerrain extends BlockingTerrain {
    
    protected Point2D.Float end1;
    protected Point2D.Float end2;
    protected Gradient gradient;
    protected Area fatShape;
    protected String terrainName;
    
    public EdgeBlockingTerrain (TdcMap map, TerrainEdge edge, Rectangle offset) {
        super(map, edge.getTerrain().getTerrainName());
        setCenter(offset (edge.getCenter(), offset));
        end1 = offset (edge.getEnd1(), offset);
        end2 = offset (edge.getEnd2(), offset);
        gradient = new Gradient(end1, end2);
        terrainName = edge.getTerrain().getTerrainName();
    }
    
    public void draw(Graphics g, TdcThread thread) {
      draw(g, thread.getRidgeColor(), thread.getCrestColor());
    }
    
    public void draw(Graphics g, WizardThread thread) {
      draw(g, thread.getRidgeColor(), thread.getCrestColor());
    }
    
    protected void draw (Graphics g, Color ridgeColor, Color crestColor) {
      final Point2D.Float p1 = map.componentCoordinates2D(end1);
      final Point2D.Float p2 = map.componentCoordinates2D(end2);
      final Graphics2D g2 = (Graphics2D) g;      
      
      final double os_scale = Info.getSystemScaling();
    
      if (terrainName.equals(TdcProperties.RIDGE)) {
        g2.setColor(ridgeColor);
      }
      else {
        g2.setColor(crestColor);
      }
      
      final Stroke saveStroke = g2.getStroke();
      g2.setStroke(new BasicStroke((float) (6.0f * map.getZoom() * os_scale), BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_ROUND));
      
      g2.drawLine((int) Math.round(p1.x * os_scale), (int)  Math.round(p1.y * os_scale), (int)  Math.round(p2.x * os_scale), (int)  Math.round(p2.y * os_scale));
      g2.setStroke(saveStroke);
    }
  public double centerToCenterRange(Point p) {
    return map.findTerrainHexGrid(p).getHexSize();
  }

    public boolean isEdgeOf(Point2D p) {
      // check if distance from p to both ends of the hexside is equal to 1/2 the hex height.
      final double distToEnd1 = this.end1.distance(p);
      final double distToEnd2 = this.end2.distance(p);
      final TerrainHexGrid thg = this.map.findTerrainHexGrid((Point) p);
      if (thg != null) {
        final double w = thg.getHexSize() / 2 + 15; // 10px safety margin as the returned values do not seem exact
        if ((w > distToEnd1)&&(w > distToEnd2)) {
          return true;
        }
      }
      return false;
    }

    public boolean intersects(Point2D.Float p1, Point2D.Float p2) {
      boolean intersects;
      
      // Check for an intersection of the LOS and the Edge segment
      intersects = Line2D.linesIntersect(end1.x, end1.y, end2.x, end2.y, p1.x, p1.y, p2.x, p2.y);
      
      // If no intersection, then check for the special case of the LOS running exactly along
      // Edge segment but not intersection. The gradient of the LOS and the segment will be
      // the same (almost) and centre if the edge segment will be on the LOS (almost)
      if (! intersects) {
        final Gradient losGradient = new Gradient (p1, p2);
        if (losGradient.equals(gradient)) {
          System.out.println("here");
        }
      }
      
      return intersects;
    }
    
   /*
    * Does the supplied LOS intersect this Edge?
    * If the LOS is non-spinal, just check for intersection of the Line segments.
    * For a spinal LOS (running along hex edges), create a fatter Area that includes the Edge
    * and check for intersection with that.
    */
   public boolean intersects(LOS los) {
     
     boolean intersects;
     
     if (los.isSpinal()) {
       intersects = los.intersects(getFatShape());
     }
     else {
       intersects = los.intersects(end1.x, end1.y, end2.x, end2.y);
     }
     
     return intersects;
   }

   public Area getFatShape() {
     float offset = 5f;
     if (fatShape == null) {
       GeneralPath path = new GeneralPath();
       path.moveTo(end1.x, end1.y-offset);
       path.lineTo(end2.x, end2.y-offset);
       path.lineTo(end2.x, end2.y+offset);
       path.lineTo(end1.x, end1.y+offset);
       path.closePath();
       fatShape = new Area(path);
     }
     return fatShape;
   }
  }