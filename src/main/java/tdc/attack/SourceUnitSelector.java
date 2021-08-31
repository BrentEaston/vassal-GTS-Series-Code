/*
 * $Id: SourceUnitSelector 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2012 by Brent Easton
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

package tdc.attack;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import VASSAL.counters.GamePiece;

public class SourceUnitSelector extends AbstractUnitSelector {

  public SourceUnitSelector(AttackView view, AttackModel model) {
    super(view, model);
  }

  protected GamePiece getPiece() {
    return myModel.getSource();
  }

  protected int getPieceCount() {
    return 1;
  }

  protected int getSelectedIndex() {
    return 0;
  }

  protected void selectPiece(int i) {

  }

  protected void build() {
    super.build();

    // No range display for special attacks
    JLabel r;
    if (!myModel.getSourceInfo().isNQOSArtinAP() && !myModel.isSpecialAttack()) {
      final int r1 = myModel.getRange();
      final int r2 = myModel.isAirDefence() ? 8 : myModel.getSourceInfo().getEffectiveRange();
      if (r2 == -1) {
        r = new JLabel("Range: "+r1+"/*");
      }
      else {
        String text = "<html><center>Range: " + r1 + "/" + r2;
        String rd = myModel.getSourceInfo().getRangeDetails();
        if (rd.length() > 0) {
          text = text + "<br>(" + rd + ")";
        }
        if (r1 > r2) {
          text = text + "<br>Out of Range!";
        }
        r = new JLabel(text);
        if (r1 > r2) {
          r.setFont(new Font("Dialog", Font.BOLD, 12));
          r.setForeground(Color.red);
        }
        
      }
      panel.add(r, "span 2,align center,wrap");
    }
    else if (myModel.isBeachDefenceAttack()) {
      r = new JLabel(myModel.getSourceInfo().getBeachAttackClass());
      panel.add(r, "span 2,align center,wrap");
    }
    panel.add(new JLabel(getAttackModel().getSourceLocation()), "span 2,align center,wrap");
  }
}