/*
 * $Id: UnitSelector 7690 2011-07-07 23:44:55Z swampwallaby $
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import VASSAL.counters.GamePiece;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.OpIcon;

public abstract class AbstractUnitSelector {
    protected JPanel panel;
    protected JButton leftButton;
    protected JButton rightButton;
    protected JPanel imagePanel;
    protected BufferedImage image;
    protected Graphics2D graphics;
    protected JLabel imageLabel;
    protected int selected;

  protected AttackView myView;
    protected AttackModel myModel;

    public AbstractUnitSelector(AttackView view, AttackModel model) {
      myView = view;
      myModel = model;
    }

    public AttackModel getAttackModel() {
      return myModel;
    }
    
    protected abstract GamePiece getPiece();
    protected abstract int getPieceCount();    
    protected abstract int getSelectedIndex();
    protected abstract void selectPiece(int i);

    public JPanel getContent() {
      if (panel == null) {
        build();
      }
      return panel;
    }
    
    protected void build() {

      panel = new JPanel(new MigLayout("", "[]push[]"));
      final Dimension size = new Dimension(44, 40);

      imagePanel = new JPanel(new MigLayout());
      imagePanel.setBorder(BorderFactory.createLineBorder(Color.black));
      image = new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB);
      graphics = image.createGraphics();
      imageLabel = new JLabel();
      imagePanel.add(imageLabel);
      
      leftButton = new JButton();
      rightButton = new JButton();
      
      panel.add(imagePanel, "span 2,wrap");
      if (getPieceCount() > 1) {
        leftButton.setIcon(new OpIcon(Op.load("prev32.png")));
        leftButton.setPreferredSize(size);
        leftButton.setMaximumSize(size);
        leftButton.addActionListener(e -> left());
        panel.add(leftButton);

        rightButton.setIcon(new OpIcon(Op.load("next32.png")));
        rightButton.setPreferredSize(size);
        rightButton.setMaximumSize(size);
        rightButton.addActionListener(e -> right());
        panel.add(rightButton, "wrap");
      }
      
      select(0);
    }

    public void select(int i) {
      selected = i;
      selectPiece(i);

      getPiece().draw(graphics, 37, 37, null, 1.0f);
      imageLabel.setIcon(new ImageIcon(image));

      leftButton.setEnabled(getSelectedIndex() > 0);
      rightButton.setEnabled(getSelectedIndex() < (getPieceCount() - 1));
      
    }

    protected void left() {
      select(getSelectedIndex() - 1);      
      myView.selectTarget(selected);
    }

    protected void right() {
      select(getSelectedIndex() + 1);
      myView.selectTarget(selected);
      
    }

  }