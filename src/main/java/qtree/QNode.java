/*
 * $Id: Qnode.java 9188 2015-04-17 04:37:20Z swampwallaby $
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

/*
 * The MIT License

Copyright (c) 2014 Varun Pant

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal 
in the Software without restriction, including without limitation the rights 
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
of the Software, and to permit persons to whom the Software is furnished to do 
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all 
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package qtree;

public class QNode {

  private double x;
  private double y;
  private double w;
  private double h;
  private QNode opt_parent;
  private QPoint point;
  private QNodeType nodetype = QNodeType.EMPTY;
  private QNode nw;
  private QNode ne;
  private QNode sw;
  private QNode se;

  /**
   * Constructs a new quad tree node.
   *
   * @param {double} x X-coordiate of node.
   * @param {double} y Y-coordinate of node.
   * @param {double} w Width of node.
   * @param {double} h Height of node.
   * @param {Node}   opt_parent Optional parent node.
   * @constructor
   */
  public QNode(double x, double y, double w, double h, QNode opt_parent) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.opt_parent = opt_parent;
  }

  public double getX() {
      return x;
  }

  public void setX(double x) {
      this.x = x;
  }

  public double getY() {
      return y;
  }

  public void setY(double y) {
      this.y = y;
  }

  public double getW() {
      return w;
  }

  public void setW(double w) {
      this.w = w;
  }

  public double getH() {
      return h;
  }

  public void setH(double h) {
      this.h = h;
  }

  public QNode getParent() {
      return opt_parent;
  }

  public void setParent(QNode opt_parent) {
      this.opt_parent = opt_parent;
  }

  public void setPoint(QPoint point) {
      this.point = point;
  }

  public QPoint getPoint() {
      return this.point;
  }

  public void setNodeType(QNodeType nodetype) {
      this.nodetype = nodetype;
  }

  public QNodeType getNodeType() {
      return this.nodetype;
  }


  public void setNw(QNode nw) {
      this.nw = nw;
  }

  public void setNe(QNode ne) {
      this.ne = ne;
  }

  public void setSw(QNode sw) {
      this.sw = sw;
  }

  public void setSe(QNode se) {
      this.se = se;
  }

  public QNode getNe() {
      return ne;
  }

  public QNode getNw() {
      return nw;
  }

  public QNode getSw() {
      return sw;
  }

  public QNode getSe() {
      return se;
  }
}

