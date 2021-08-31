/*
 * $Id: OpacityConfigurer.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
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

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import VASSAL.configure.IntConfigurer;

/**
 * A slider to configure opacity level
 *
 */
public class OpacityConfigurer extends IntConfigurer {

    public OpacityConfigurer(String key, String name, int val) {
      super(key, name, val);
    }
    
    public void setValue(int i) {
      setValue(Integer.valueOf(i));
    }
    
    public Component getControls() {

      final JSlider slider = new JSlider(JSlider.HORIZONTAL,0,100,getIntValue(100));

      final Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
      labelTable.put(0, new JLabel("Transparent") );
      labelTable.put(100, new JLabel("Opaque") );

      slider.setMajorTickSpacing(10);
      slider.setPaintTicks(true);
      slider.setLabelTable(labelTable);
      slider.setPaintLabels(true);
      slider.setBorder(javax.swing.BorderFactory.createTitledBorder(name));
      slider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          final JSlider source = (JSlider)e.getSource();
          if (!source.getValueIsAdjusting()) {    
            setValue(source.getValue());
          }
        }});

      return slider;
    }
    
  }