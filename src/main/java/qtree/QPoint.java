/*
 * $Id: QPoint.java 9188 2015-04-17 04:37:20Z swampwallaby $
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

public class QPoint implements Comparable<Object> {

  private double x;
  private double y;
  private Object opt_value;

  /**
   * Creates a new point object.
   *
   * @param {double} x The x-coordinate of the point.
   * @param {double} y The y-coordinate of the point.
   * @param {Object} opt_value Optional value associated with the point.     
   */
  public QPoint(double x, double y, Object opt_value) {
      this.x = x;
      this.y = y;
      this.opt_value = opt_value;
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

  public Object getValue() {
      return opt_value;
  }

  public void setValue(Object opt_value) {
      this.opt_value = opt_value;
  }

  @Override
  public String toString() {
      return "(" + this.x + ", " + this.y + ")";
  }

  public int compareTo(Object o) {
      QPoint tmp = (QPoint) o;
      if (this.x < tmp.x) {
          return -1;
      } else if (this.x > tmp.x) {
          return 1;
      } else {
          if (this.y < tmp.y) {
              return -1;
          } else if (this.y > tmp.y) {
              return 1;
          }
          return 0;
      }

  }

}

