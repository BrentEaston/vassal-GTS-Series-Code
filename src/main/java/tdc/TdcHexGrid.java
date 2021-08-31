/*
 * $Id: TdcHexGrid.java 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2005-2010 by Brent Easton
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;

import VASSAL.build.module.map.boardPicker.board.HexGrid;

/**
 * @author Brent Easton
 *
 * Draw the centre dots as 3x3 squares centred on the hex centre instead
 * of 2x2 offset. Improves look of LOS thread.
 */
public class TdcHexGrid extends HexGrid {
  public void forceDraw(Graphics g, Rectangle bounds, Rectangle visibleRect, double zoom, boolean reversed) {
    if (!bounds.intersects(visibleRect)) {
      return;
    }
    if (g instanceof Graphics2D) {
      ((Graphics2D) g).addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                            RenderingHints.VALUE_ANTIALIAS_ON));
    }

    g.setColor(color == null ? Color.black : color);

    float x1,y1, x2,y2, x3,y3, x4, y4;

    float deltaX = (float) (this.dx * zoom);
    float deltaY = (float) (this.dy * zoom);

    float r = 2.F * deltaX / 3.F;

    Rectangle region = bounds.intersection(visibleRect);

    Shape oldClip = g.getClip();
    if (oldClip != null) {
      Area clipArea = new Area(oldClip);
      clipArea.intersect(new Area(region));
      g.setClip(clipArea);
    }

    if (sideways) {
      bounds = new Rectangle(bounds.y, bounds.x, bounds.height, bounds.width);
      region = new Rectangle(region.y, region.x, region.height, region.width);
    }

    float xmin = reversed ? bounds.x + (float) zoom * origin.x + bounds.width - 2 * deltaX * (float) Math.ceil((bounds.x + zoom * origin.x + bounds.width - region.x) / (2 * deltaX))
        : bounds.x + (float) zoom * origin.x + 2 * deltaX * (float) Math.floor((region.x - bounds.x - zoom * origin.x) / (2 * deltaX));
    float xmax = region.x + region.width + 2 * deltaX;
    float ymin = reversed ? bounds.y + (float) zoom * origin.y + bounds.height - deltaY * (float) Math.ceil((bounds.y + zoom * origin.y + bounds.height - region.y) / deltaY)
        : bounds.y + (float) zoom * origin.y + deltaY * (float) Math.floor((region.y - bounds.y - zoom * origin.y) / deltaY);
    float ymax = region.y + region.height + deltaY;

    Point center = new Point();
    Point p1 = new Point();
    Point p2 = new Point();
    Point p3 = new Point();
    Point p4 = new Point();

    // x,y is the center of a hex
    for (float x = xmin; x < xmax; x += zoom * 2 * dx) {
      for (float y = ymin; y < ymax; y += zoom * dy) {
        x1 = x - r;
        y1 = y;
        p1.setLocation(Math.round(x1), Math.round(y1));
        x2 = x - .5F * r;
        y2 = reversed ? y + .5F * deltaY : y - .5F * deltaY;
        p2.setLocation(Math.round(x2), Math.round(y2));
        x3 = x + .5F * r;
        y3 = y2;
        p3.setLocation(Math.round(x3), Math.round(y3));
        x4 = x + r;
        y4 = y;
        p4.setLocation(Math.round(x4), Math.round(y4));
        if (sideways) {
          rotate(p1);
          rotate(p2);
          rotate(p3);
          rotate(p4);
        }
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        g.drawLine(p2.x, p2.y, p3.x, p3.y);
        g.drawLine(p3.x, p3.y, p4.x, p4.y);
        if (dotsVisible) {
          center.setLocation(Math.round(x), Math.round(y));
          rotateIfSideways(center);
          g.fillRect(center.x-1, center.y-1, 3, 3);
          center.setLocation(Math.round(x + deltaX), Math.round(y + deltaY / 2));
          rotateIfSideways(center);
          g.fillRect(center.x-1, center.y-1, 3, 3);
        }
        x1 += deltaX;
        x2 += deltaX;
        x3 += deltaX;
        x4 += deltaX;
        y1 += .5F * deltaY;
        y2 += .5F * deltaY;
        y3 += .5F * deltaY;
        y4 += .5F * deltaY;
        p1.setLocation(Math.round(x1), Math.round(y1));
        p2.setLocation(Math.round(x2), Math.round(y2));
        p3.setLocation(Math.round(x3), Math.round(y3));
        p4.setLocation(Math.round(x4), Math.round(y4));
        if (sideways) {
          rotate(p1);
          rotate(p2);
          rotate(p3);
          rotate(p4);
        }
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        g.drawLine(p2.x, p2.y, p3.x, p3.y);
        g.drawLine(p3.x, p3.y, p4.x, p4.y);
        if (x == xmin) {
          p1.setLocation(Math.round(x - r), Math.round(y));
          p2.setLocation(Math.round(x - r / 2), Math.round(y + deltaY / 2));
          if (sideways) {
            rotate(p1);
            rotate(p2);
          }
          g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
      }
    }
    g.setClip(oldClip);
  }
}
