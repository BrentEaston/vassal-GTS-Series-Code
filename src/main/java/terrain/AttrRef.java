/*
 * $Id: AttrRef.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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

/**
 * Class used as a key to Attribute Terrain lookups. Key is hexRef/attribute name
 *
 */
public class AttrRef extends HexRef {
  
  private String attributeName;
  
  public AttrRef(HexRef ref, String attrName) {
    super(ref);
    attributeName = attrName;
  }
  
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return attributeName;
  }
  
  public boolean equals(Object o) {
    if (o instanceof AttrRef) {
      final AttrRef h = (AttrRef) o;
      return (h.getColumn() == getColumn() && h.getRow() == getRow() && h.getAttributeName().equals(getAttributeName()));
    }
    return false;
  }
  
  public int compareTo(HexRef o) {
    return super.compareTo(o);    
  }
  
  public String toString() {
    return super.toString() + " " + attributeName;
  }
  
  public boolean isAttributeRef() {
    return true;
  }
  
  public boolean isHexRef() {
    return false;
  }
}