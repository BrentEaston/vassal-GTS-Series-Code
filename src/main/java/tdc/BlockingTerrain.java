/*
 * $Id: BlockingTerrain.java 9192 2015-05-16 01:24:32Z swampwallaby $
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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;

public abstract class BlockingTerrain {
  private Point2D.Float center;
  protected TdcMap map;
  protected boolean possiblyBlocking;
  protected HashSet<String> reasons = new HashSet<>();



  public BlockingTerrain(TdcMap map, String description) {
    this.map = map;
    addReason(description);
  }

  public void setCenter(Point2D.Float p) {
    center = new Point2D.Float(p.x, p.y);
  }

  public void setCenter(Point p) {
    setCenter(new Point2D.Float(p.x, p.y));
  }
  
  // Is this the center of the hex (allow for rounding)
  public boolean hasCenter (Point p) {
    return Math.abs(p.x- getCenter().x) < 5 && Math.abs(p.y- getCenter().y) < 5;
  }
  
  public float getX() {
    return getCenter().x;
  }

  public float getY() {
    return getCenter().y;
  }

  public Point2D.Float getCenter() {
    return center;
  }
  
  public boolean isPossiblyBlocking() {
    return possiblyBlocking;
  }
  
  public void setPossiblyBlocking(boolean b) {
    possiblyBlocking = b;
  }
  
  public abstract void draw(Graphics g, WizardThread thread);
  public abstract void draw(Graphics g, TdcThread thread);

  public abstract boolean intersects(LOS los);

  protected Point offset(Point p, Rectangle offset) {
    return new Point(p.x + offset.x, p.y + offset.y);
  }

  protected Point2D.Float offset(Point2D.Float p, Rectangle offset) {
    return new Point2D.Float(p.x + offset.x, p.y + offset.y);
  }

  public double calcRange(Point2D p) {
    return p.distance(getCenter());
  }

  public void addReason(String r) {
    reasons.add(r);
  }
  
  public Iterator<String> getReasons() {
    return reasons.iterator();
  }
}