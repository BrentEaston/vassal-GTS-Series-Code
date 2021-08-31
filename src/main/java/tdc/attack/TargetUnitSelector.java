/*
 * $Id: TargetUnitSelector 7690 2011-07-07 23:44:55Z swampwallaby $
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

import javax.swing.JLabel;

import VASSAL.counters.GamePiece;

public class TargetUnitSelector extends AbstractUnitSelector {

  public TargetUnitSelector(AttackView view, AttackModel model) {
    super(view, model);
  }

  protected GamePiece getPiece() {
    return myModel.getTarget();
  }

  protected int getPieceCount() {
    return myModel.getTargetCount();
  }

  protected int getSelectedIndex() {
    return myModel.getTargetIndex();
  }

  protected void selectPiece(int i) {
    myModel.selectTarget(i);
  }
  
  protected void build() {
    super.build();
    panel.add(new JLabel(myModel.getTargetTerrain()), "span 2,wrap,align center");
    panel.add(new JLabel(getAttackModel().getTargetLocation()), "span 2,align center,wrap");
  }
}