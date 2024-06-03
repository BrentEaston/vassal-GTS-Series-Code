/*
 * $Id: TdcThread.java 9200 2015-05-28 11:28:01Z swampwallaby $
 *
 * Copyright (c) 2005-2010 by Rodney Kinney, Brent Easton
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.LOS_Thread;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.ColorConfigurer;

/**
 * @author Brent Easton
 *
 * Modified LOS Thread 
 */
public class TdcThread extends LOS_Thread implements KeyListener {
  
  protected HashSet<BlockingTerrain> blockingTerrain = new HashSet<>();
  protected Point lastArrowSnap = new Point();
  
  protected ColorConfigurer ridgeColorConfig;
  protected ColorConfigurer crestColorConfig;
  protected ColorConfigurer blockColorConfig;
  protected ColorConfigurer possibleColorConfig;
  protected BooleanConfigurer blocksStartShowingConfig;
  
  protected boolean ctrlKeyDepressed = false;
  
  public TdcThread() {
    super();
  }
  
  public void addTo(Buildable b) {
    super.addTo(b);

    map.getView().addKeyListener(this);
    
    ridgeColorConfig = new ColorConfigurer(WizardThread.RIDGE_COLOR, "Blocking Ridge Color", Color.ORANGE);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, ridgeColorConfig);

    crestColorConfig = new ColorConfigurer(WizardThread.CREST_COLOR, "Blocking Crest Color", Color.YELLOW);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, crestColorConfig);

    blockColorConfig = new ColorConfigurer(WizardThread.BLOCK_COLOR, "Blocking Hex Color", Color.RED);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, blockColorConfig);

    possibleColorConfig = new ColorConfigurer(WizardThread.POSSIBLE_COLOR, "Possibly Blocking Hex Color", Color.PINK);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, possibleColorConfig);

    blocksStartShowingConfig = new BooleanConfigurer(WizardThread.BLOCKS_START_SHOWING, "Blocking Terrain starts showing (Ctrl to toggle)?", true);
    GameModule.getGameModule().getPrefs().addOption(TdcProperties.PREF_TAB, blocksStartShowingConfig);
  }
  

  public Color getRidgeColor() {
    return (Color) ridgeColorConfig.getValue();
  }

  public Color getCrestColor() {
    return (Color) crestColorConfig.getValue();
  }

  public Color getBlockColor() {
    return (Color) blockColorConfig.getValue();
  }

  public Color getPossibleColor() {
    return (Color) possibleColorConfig.getValue();
  }

  protected void findLOSBlockingTerrain() {
    ((TdcMap) map).findBlockingTerrain(anchor, arrow, blockingTerrain);
  }

  protected void initLOSBlockingTerrain() {
    ((TdcMap) map).initBlockingTerrain();
  }

  public void draw(java.awt.Graphics g, Map m) {
    super.draw(g, m);
    if (initializing || !visible) {
      return;
    }
    
    final boolean blocksStartShowing = blocksStartShowingConfig.getValueBoolean();
    if ((blocksStartShowing && !ctrlKeyDepressed) || (!blocksStartShowing && ctrlKeyDepressed)) {

      // Draw Hex Blocking terrain
      for (BlockingTerrain bt : blockingTerrain) {
        if (bt instanceof HexBlockingTerrain) {
          bt.draw(g, this);
        }
      }

      // Draw Edge Blocking terrain
      for (BlockingTerrain bt : blockingTerrain) {
        if (bt instanceof EdgeBlockingTerrain) {
          bt.draw(g, this);
        }
      }
    }
  }
  
  
  public void mouseDragged(MouseEvent e) {
    if (visible && !persisting && !mirroring) {
      retainAfterRelease = true;
     
      setCtrlKeyDepressed((e.getModifiersEx() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK);
     
      Point p = e.getPoint();

      map.scrollAtEdge(p, 15);

      if (Boolean.TRUE.equals
          (GameModule.getGameModule().getPrefs().getValue(SNAP_LOS))
          || snapEnd) {
        arrow = map.snapTo(map.componentToMap(p));
      }
      else
      {
        arrow = map.componentToMap(p);
      }
      
      // LOS target has snapped to a new Point, recalculate any Ridge/Crest
      // crossings
      if (!arrow.equals(lastArrowSnap)) {
        initLOSBlockingTerrain();
        findLOSBlockingTerrain();
        lastArrowSnap = new Point(arrow);
      }

      String location = map.localizedLocationName(arrow);
      if (!checkList.contains(location) && !location.equals(anchorLocation)) {
        checkList.add(location);
        lastLocation = location;
      }

      Point mapAnchor = map.componentToMap(lastAnchor);
      Point mapArrow = map.componentToMap(lastArrow);
      int fudge = (int) (1.0 / map.getZoom() * 2);
      Rectangle r = new Rectangle(Math.min(mapAnchor.x, mapArrow.x)-fudge,
          Math.min(mapAnchor.y, mapArrow.y)-fudge,
          Math.abs(mapAnchor.x - mapArrow.x)+1+fudge*2,
          Math.abs(mapAnchor.y - mapArrow.y)+1+fudge*2);
      map.repaint(r);

      if (drawRange) {
        r = new Rectangle(lastRangeRect);
        r.width += (int)(r.width / map.getZoom()) + 1;
        r.height += (int)(r.height / map.getZoom()) + 1;
        map.repaint(r);
      }
    }
  }

  protected void setCtrlKeyDepressed(boolean b) {
    ctrlKeyDepressed = b;
    map.repaint();
  }

  protected boolean isCtrlKeyDepressed() {
    return ctrlKeyDepressed;
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      setCtrlKeyDepressed(true);
    }
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      setCtrlKeyDepressed(false);
    }
  }

  public void keyTyped(KeyEvent e) {
    // TODO Auto-generated method stub

  }


}
