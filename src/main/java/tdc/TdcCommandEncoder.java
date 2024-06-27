/*
 * $Id: TdcCommandEncoder.java 9374 2020-04-30 00:23:28Z swampwallaby $
 * 
 * Copyright (c) 2000-2005 by Rodney Kinney, Brent Easton
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License (LGPL) as published by
 * the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, copies are available at
 * http://www.opensource.org.
 */
package tdc;

import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;

import terrain.TerrainCommandEncoder;

public class TdcCommandEncoder extends TerrainCommandEncoder {

  public TdcCommandEncoder() {
    super();
    Info.checkVassalVersion();
  }

  protected GamePiece createBasic(String type) {
    if (type.startsWith(BasicPiece.ID)) {
      return new TdcBasicPiece(type);
    }
    else {
      return super.createBasic(type);
    }
  }
  
  public Decorator createDecorator(String type, GamePiece inner) {
    if (type.startsWith(PeekAtLayer.ID)) {
      return new PeekAtLayer(type, inner);
    }
    else if (type.startsWith(TdcSendToLocation.ID)) {
      return new TdcSendToLocation(type, inner);
    }
    else if (type.startsWith(TdcWizard.ID)) {
      return new TdcWizard(type, inner);
    }
    else if (type.startsWith(TdcRatings.ID)) {
      return new TdcRatings(type, inner);
    }
    else if (type.startsWith(ArtilleryMarker.ID)) {
      return new ArtilleryMarker(type, inner);
    }
    else if (type.startsWith(TdcLeader.ID)) {
      return new TdcLeader(type, inner);
    }
    else if (type.startsWith(HoverText.ID)) {
      return new HoverText(type, inner);
    }
    else if (type.startsWith(TerrainMarker.ID)) {
      return new TerrainMarker(type, inner);
    }
    else if (type.startsWith(Embellishment.ID) && type.contains("marker-column-semi.png")) {
      return new TdcColumnEmbellishment(type, inner);
    }
    return super.createDecorator(type, inner);
  }
}
