/*
 * $Id: TerrainCommandEncoder.java 3639 2008-05-23 11:24:55Z swampwallaby $
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

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.GamePiece;

/**
 * Custom Command Encoder to create all BasicPiece's as TerrainBasicPiece
 *
 */
//FIXME: Merge into BasicCommandEncoder
public class TerrainCommandEncoder extends BasicCommandEncoder {
  
  protected GamePiece createBasic(String type) {
    if (type.startsWith(BasicPiece.ID)) {
      return new TerrainBasicPiece(type);
    }
    else {
      return super.createBasic(type);
    }
  }

}
