/*
 * $Id: TdcHighlighter.java 9374 2020-04-30 00:23:28Z swampwallaby $
 *
 * Copyright (c) 2005-2012 by Brent Easton
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
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import VASSAL.counters.ColoredBorder;
import VASSAL.counters.GamePiece;
import VASSAL.tools.image.LabelUtils;
import VASSAL.counters.Properties;

/**
 * Change border color depending on Command Status Display Non-Formation command
 * penalty Total and display any Troop Quality bonuses/penalties
 * 
 */
public class TdcHighlighter extends ColoredBorder {

  protected static final String TRUE = "true";

  protected static Font numberFont = null;
  protected static double lastZoom = 0;

  public TdcHighlighter() {
    super();
  }

  /**
   * Change outline Color to 3 pixel wide Red and a diagonal 1 pixel slash if
   * unit is not in command.
   */

  public void draw(GamePiece p, Graphics g, int x, int y, Component obs, double zoom) {

    final boolean isHidden = Boolean.TRUE.equals(p.getProperty(Properties.INVISIBLE_TO_ME));
    final boolean isObscured = Boolean.TRUE.equals(p.getProperty(Properties.OBSCURED_TO_ME));
    final int unitWidth = UnitInfo.getStandardUnitSize();

    if (p.getMap() == null || isHidden || isObscured) {
      super.draw(p, g, x, y, obs, zoom);
      return;
    }

    final UnitInfo info = new UnitInfo(p, false);
    if (info.isNoHighlighting()) {
      return;
    }

    Color oldColor = getColor();
    int oldThickness = getThickness();

    Rectangle r = p.getShape().getBounds();
    
    /* Out of command/formation color display and label */
    if (!info.isInCommand()) {
      setColor(Color.red);
      setThickness(3);
      int x1 = x + (int) (zoom  * r.x) - 1;
      int y2 = y + (int) (zoom  * (r.y + r.height));
      int x2 = x1 + (int) (zoom  * r.width);
      int y1 = y2 - (int) (zoom  * unitWidth);
      g.setColor(Color.red);
      g.drawLine(x1, y2, x2, y1);
    }
    else if (info.isNonFormationCommand()) {
      setColor(Color.magenta);
      setThickness(3);
      LabelUtils.drawLabel(g, "+1CP", x + (int) (15 * zoom), y - (int) (50 * zoom), getNumberFont(zoom),
          LabelUtils.CENTER, LabelUtils.CENTER, Color.RED, Color.WHITE, Color.BLACK);
    }

    super.draw(p, g, x, y, obs, zoom);
    setColor(oldColor);
    setThickness(oldThickness);

    if (info.isTqrAdjusted()) {
      Color color;
      final int tqAdjustment = info.getTqrAdjustment() ;
      if (tqAdjustment < 0) {
        color = Color.red;
      }
      else if (tqAdjustment == 0) {
        color = Color.green;
      }
      else {
        color = Color.blue;
      }
      LabelUtils.drawLabel(g, info.getTqrAdjustmentString(), x + (int) (52 * zoom), y - (int) (28 * zoom), getNumberFont(zoom),
          LabelUtils.CENTER, LabelUtils.CENTER, color, Color.WHITE, Color.BLACK);
    }

    if (info.isMoveAdjusted()) {
      LabelUtils.drawLabel(g, info.getAdjustedMove(), x + (int) (71 * zoom), y - (int) (6 * zoom), getNumberFont(zoom),
          LabelUtils.LEFT, LabelUtils.CENTER, Color.BLACK, Color.WHITE, Color.BLACK);
    }
    
    if (info.isFireAdjusted()) {
      LabelUtils.drawLabel(g, info.getFireAdjustmentString(), x - (int) (51 * zoom), y - (int) (30 * zoom), getNumberFont(zoom),
          LabelUtils.CENTER, LabelUtils.CENTER, Color.BLACK, Color.WHITE, Color.BLACK);
    }

    if (info.isAssaultAdjusted()) {
      LabelUtils.drawLabel(g, info.getAssaultAdjustmentString(), x - (int) (51 * zoom), y - (int) (10 * zoom), getNumberFont(zoom),
          LabelUtils.CENTER, LabelUtils.CENTER, Color.BLACK, Color.WHITE, Color.BLACK);
    }

    if (info.isDefenceAdjusted()) {
      LabelUtils.drawLabel(g, info.getDefenceAdjustmentString(), x - (int) (51 * zoom), y
          + (int) (15 * zoom), getNumberFont(zoom), LabelUtils.CENTER, LabelUtils.CENTER, Color.BLACK,
          Color.WHITE, Color.BLACK);
    }
  }

  private Font getNumberFont(double zoom) {
    if (numberFont == null || lastZoom != zoom) {
      numberFont = new Font(Font.DIALOG, Font.BOLD, (int) (UnitInfo.getStandardUnitSize() * 0.21 * zoom));
    }
    return numberFont;
  }

}
