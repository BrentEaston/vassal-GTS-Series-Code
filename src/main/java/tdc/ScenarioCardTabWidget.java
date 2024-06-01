/*
 * $Id: ScenarioCardTabWidget 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2012-2017 by Brent Easton
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

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import VASSAL.build.Buildable;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.widget.MapWidget;
import VASSAL.build.widget.WidgetMap;
import VASSAL.command.Command;

/**
 * A Widget that corresponds to a JTabbedPane. Adding a Widget to a BoxWidget
 * adds the child Widget's component to the JTabbedPane, setting the tab's name
 * to the child's name (via {@link Configurable#getConfigureName})
 */
public class ScenarioCardTabWidget extends Widget implements ChangeListener, PropertyChangeListener, GameComponent {
  protected JTabbedPane tab = null;
  protected List<Widget> widgets = new ArrayList<>();
  protected boolean unusedTabsHidden = false;
  protected boolean[] tabVisibility;

  public ScenarioCardTabWidget() {
  }

  public static String getConfigureTypeName() {
    return "Tabbed Panel";
  }

  public void stateChanged(ChangeEvent e) {
    int index = tab.getSelectedIndex();
    if (index >= 0) {
      tab.setComponentAt(index, widgets.get(index).getComponent());
    }
    if (!unusedTabsHidden) {
      updateVisibility();
    }
  }

  public void addTo(Buildable b) {
    super.addTo(b);
    GameModule.getGameModule().getGameState().addGameComponent(this);
  }
  
  public void add(Buildable b) {
    if (b instanceof Widget) {
      final Widget w = (Widget) b;
      widgets.add(w);
      if (tab != null) {
        tab.removeChangeListener(this);
        if (widgets.size() > 1) {
          tab.addTab(w.getConfigureName(), new JPanel());
        }
        else {
          tab.addTab(w.getConfigureName(), w.getComponent());
        }
        w.addPropertyChangeListener(this);
        tab.addChangeListener(this);
      }
    }
    super.add(b);
  }

  public void remove(Buildable b) {
    if (b instanceof Widget) {
      final Widget w = (Widget) b;
      if (tab != null) {
        tab.removeChangeListener(this); // prevent bad recursion
        tab.removeTabAt(widgets.indexOf(w));
        w.removePropertyChangeListener(this);
        tab.addChangeListener(this); // restore listener
      }
      widgets.remove(w);
    }
    super.remove(b);
  }

  public void propertyChange(PropertyChangeEvent evt) {
    if (Configurable.NAME_PROPERTY.equals(evt.getPropertyName())) {
      final Widget src = (Widget) evt.getSource();
      final int index = widgets.indexOf(src);
      final Object name = evt.getNewValue();
      tab.setTitleAt(index, name == null ? "" : name.toString());
    }
  }

  public Component getComponent() {
    if (tab == null) {
      rebuild();
      tab = new JTabbedPane();
      for (final Widget w : widgets) {
        w.addPropertyChangeListener(this);
        tab.addTab(w.getConfigureName(), new JPanel());
      }
      tab.addChangeListener(this);
      if (widgets.size() > 0) {
        tab.setSelectedIndex(0);
      }
      stateChanged(null);
      tab.addAncestorListener(new AncestorListener() {
        @Override
        public void ancestorAdded(AncestorEvent event) {
          updateVisibility();
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {

        }

        @Override
        public void ancestorMoved(AncestorEvent event) {

        }
      });
    }
    return tab;
  }

  public String[] getAttributeNames() {
    return new String[] { NAME };
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Name:  " };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] { String.class };
  }

  public void setAttribute(String name, Object value) {
    if (NAME.equals(name)) {
      setConfigureName((String) value);
    }
  }

  public String getAttributeValueString(String name) {
    if (NAME.equals(name)) {
      return getConfigureName();
    }
    return null;
  }
  
  /* 
   * For TDC/TGD, Find the board name loaded on each scenario card. If is the Unused board,
     hide the tab.
     
     For Crete, check the Used-XXXX global variable matching the Map-Division property of the map on that tab
     
   */
  protected void updateVisibility() {

     // Determine which tabs should be visible
    tabVisibility = new boolean[widgets.size()];
    String[] names = new String[widgets.size()];

    for (int i = 0; i < widgets.size(); i++) {
      Widget widget = widgets.get(i);
      if (widget instanceof MapWidget) {
        final MapWidget mw = (MapWidget) widget;
        final WidgetMap wm = mw.getMap();
        Iterator<Board> it = wm.getBoards().iterator();
        if (!it.hasNext()) {
          // Too Early, no boards loaded yet
          if (i == 0) {
            return;
          }
        }
        else {
          if (UnitInfo.isCreteRules()) {
            final String division = (String) wm.getProperty(TdcProperties.MAP_DIVISION);
            final String useMap = (String) GameModule.getGameModule().getProperty("Used-" + division);

            tabVisibility[i] = !"false".equals(useMap);
          }
          else {
            names[i] = it.next().getAttributeValueString(Board.IMAGE);
            if (names[i] == null) {
              tabVisibility[i] = true;
            }
            else {
              tabVisibility[i] = !names[i].contains("unused");
            }
          }
        }
      }
    }

    setUnusedTabVisibility(false);

  }

  protected void setUnusedTabVisibility(boolean visible) {
    for (int i = 0; i < tabVisibility.length; i++) {
      if (! tabVisibility[i]) {
        tab.setEnabledAt(i, visible);
        tab.setTabComponentAt(i, visible ? null : new JLabel());
      }
    }
    setUnusedTabsHidden(! visible);
    tab.repaint();
  }

  protected void hideUnusedTabs() {
    setUnusedTabVisibility(false);
  }

  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      // Game is ending, rest the tab visibility
      if (unusedTabsHidden) {
        setUnusedTabVisibility(true);
        setUnusedTabsHidden(false);
      }
    }
  }

  public boolean isUnusedTabsHidden() {
    return unusedTabsHidden;
  }

  public void setUnusedTabsHidden(boolean unusedTabsHidden) {
    this.unusedTabsHidden = unusedTabsHidden;
  }

  public Command getRestoreCommand() {
    return null;
  }
}
