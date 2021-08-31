/*
 * $Id: RatingDisplay 7690 2011-07-07 23:44:55Z swampwallaby $
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

import net.miginfocom.swing.MigLayout;
import tdc.TdcRatings;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class RatingDisplay {

  protected JPanel panel;
  protected JPanel ratingPanel;

  protected AttackModel myModel;
  protected BufferedImage image;
  protected Graphics2D graphics;
  protected JLabel label;

  protected Font ratingFont;

  public RatingDisplay(AttackModel model) {
    myModel = model;
  }

  public JPanel getContent() {
    if (panel == null) {
      buildContent();
    }
    return panel;
  }

  public void refresh() {

    final String c = myModel.getFireRatingColour();
    final Color bg = TdcRatings.getFireBg(c);
    final String fire = Integer.toString(myModel.getDisplayFireRating());

    if (!myModel.isBeachDefenceAttack()) {
      final BasicStroke stroke = new BasicStroke(1.5f);
      graphics.setStroke(stroke);
      graphics.setPaint(bg);
      graphics.fill(new RoundRectangle2D.Double(0, 0, 59, 74, 20, 20));
      graphics.setPaint(Color.black.equals(bg) ? Color.white : Color.black);
      graphics.draw(new RoundRectangle2D.Double(0, 0, 59, 74, 20, 20));

      graphics.setFont(ratingFont);
      int w = graphics.getFontMetrics().stringWidth(fire);
      int h = graphics.getFontMetrics().getAscent();

      graphics.drawString(fire, 30 - w / 2, 25 + h / 2);
    }

    label.setIcon(new ImageIcon(image));
  }

  protected void buildContent() {
    panel = new JPanel(new MigLayout("", "push[center]push"));
    ratingPanel = new JPanel(new MigLayout());
    image = new BufferedImage(60, 75, BufferedImage.TYPE_INT_ARGB);
    graphics = image.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    label = new JLabel();
    ratingPanel.add(label);
    panel.add(ratingPanel, "wrap");

    ratingFont = new Font("Dialog", Font.PLAIN, 60);
    refresh();
  }
}