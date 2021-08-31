/*
 * $Id: gradient.java 9192 2015-05-16 01:24:32Z swampwallaby $
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

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * The Gradient of a Line
 * @author Brent
 *
 */
public class Gradient {
  
  // Any gradient > 1000 (including +/- infinity) is stored as 1000
  public static final float MAX_GRADIENT = 1000;
  
  // Two gradients are the same if within this value
  public static final float FUDGE_GRADIENT = 0.005f;
 
  protected float gradient;
  
  public Gradient (Point p1, Point p2) {
    this (new Point2D.Float(p1.x, p1.y), new Point2D.Float(p2.x, p2.y));
  }
  
  public Gradient (Point2D.Float p1, Point2D.Float p2) {
       
    try {
      gradient = (p2.y - p1.y) / (p2.x - p1.x);
    }
    catch (Exception x) {
      gradient = MAX_GRADIENT;
    }
    
    if (gradient > MAX_GRADIENT || gradient < -MAX_GRADIENT) gradient = MAX_GRADIENT;
    
  }

  public float getGradient () {
    return gradient;
  }  
  
  public boolean equals (Gradient g) {
    return equals(g.getGradient());
  }
  
  public boolean equals(float g) {
    float d = Math.abs(g - getGradient());
    return d <= FUDGE_GRADIENT ;    
  }
   
}
 