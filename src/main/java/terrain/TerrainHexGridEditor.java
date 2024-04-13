/*
 * $Id: TerrainHexGridEditor.java 9201 2015-09-23 01:28:23Z swampwallaby $
 *
 * Copyright (c) 2000-2015 by Brent Easton
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

import VASSAL.build.Buildable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.GridEditor;
import VASSAL.build.module.map.boardPicker.board.mapgrid.RegularGridNumbering;
import VASSAL.tools.image.LabelUtils;
import terrain.AttributeTerrain.AttributeSetter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import terrain.AttributeTerrain.AttributeSetter;

import VASSAL.build.Buildable;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.GridEditor;
import VASSAL.build.module.map.boardPicker.board.mapgrid.RegularGridNumbering;
import VASSAL.tools.image.LabelUtils;

/**
 * Edit Grid attributes, including Terrain
 */
public class TerrainHexGridEditor extends GridEditor implements ActionListener, MouseMotionListener {

  private static final long serialVersionUID = 1L;
  protected static float ACTIVE_OPACITY = 0.75f;
  protected static float INACTIVE_OPACITY = 0.15f;

  protected static final String BLANK = "Blank";
  protected static final String HEX = "Hex";
  protected static final String EDGE = "Edge";
  protected static final String LINE = "Line";
  protected static final String ATTRIBUTE = "Tag";
  protected static final String GRID = "Grid";
  protected static final String OPTIONS = "Options";
  protected static final String TOGGLE_GRID = "Hide/Show Grid";
  protected static final String TOGGLE_NUMBERING = "Hide/Show Numbering";

  protected TerrainHexGrid myGrid;
  protected TerrainMap terrainMap;
  protected TerrainDefinitions terrain;
  protected ArrayList<HexRef> selectedHexList = new ArrayList<>();
  protected ArrayList<EdgeRef> selectedEdgeList = new ArrayList<>();
  protected ArrayList<LineRef> selectedLineList = new ArrayList<>();
  protected ArrayList<Area> selectedHexArea = new ArrayList<>();
  protected JButton hideGridButton;
  protected boolean saveGridVisible;
  protected boolean saveNumberingVisible;
  protected boolean saveSnapToEdges;
  protected boolean saveSnapToCorners;

  protected JTabbedPane tabPane;
  protected String mode = HEX;
  protected Paint selectionPaint = null;
  protected OpacityConfigurer mapOpacity;
  protected HexRef lastRef;

  protected HexRef lastHex;
  protected Point lastSnap;

  protected AttributePanel attributePanel;

  public TerrainHexGridEditor(EditableGrid grid) {
    super(grid);
    myGrid = (TerrainHexGrid) grid;
    saveGridVisible = myGrid.isVisible();
    if (myGrid.getGridNumbering() != null) {
      saveNumberingVisible = myGrid.getGridNumbering().isVisible();
    }
    saveSnapToEdges = myGrid.isEdgesLegal();
    saveSnapToCorners = myGrid.isCornersLegal();
    terrainMap = getTerrainDefs().getTerrainMap(myGrid);
  }

  protected TerrainDefinitions getTerrainDefs() {
    if (terrain == null) {
      terrain = TerrainDefinitions.getInstance();
    }
    return terrain;
  }

  public void mouseClicked(MouseEvent e) {
    if (isGridMode()) {
      super.mouseClicked(e);
    }
  }

  public void mousePressed(MouseEvent e) {
    if (!isGridMode()) {
      if (isHexMode() || isAttributeMode()) {

        if (!e.isShiftDown() && !e.isControlDown() && selectedHexList.size() > 0) {
          clearHexSelection();
        }

        if (e.isControlDown()) {
          removeHexFromSelection(e.getPoint());
        }
        else {
          addHexToSelection(e.getPoint());
        }
      }
      else if (isEdgeMode()) {

        if (!e.isShiftDown() && !e.isControlDown() && selectedEdgeList.size() > 0) {
          clearEdgeSelection();
        }

        if (e.isControlDown()) {
          removeEdgeFromSelection(e.getPoint());
        }
        else {       
          addEdgeToSelection(e.getPoint());
        }
      }
      else if (isLineMode()) {
        final HexRef ref = new HexRef(myGrid.getHexPos(e.getPoint()));
        if (!e.isShiftDown() && !e.isControlDown()) {
          clearLineSelection();
          lastHex = ref;
        }
        else if (e.isControlDown()) {
          removeFromLineSelection(ref);
        }
        else {
          addToLineSelection(ref);
        }

        view.repaint();
      }
    }
  }

  /**
   * Return a reference to the nearest hex centre or edge
   * 
   * @param p
   *          Point
   * @return HexRef or EdgeRef
   */
  protected HexRef getEdgeOrCenter(Point p) {
    final Point hex = myGrid.snapToHex(p); // Nearest Hex Centre
    final Point snap = myGrid.snapTo(p); // Nearest Centre or Edge
    if (close(snap, hex)) {
      return myGrid.getHexPos(hex);
    }
    else {
      return new EdgeRef(snap, myGrid);
    }
  }

  protected boolean close(Point a, Point b) {
    return Math.abs(a.x - b.x) <= 1 && Math.abs(a.y - b.y) <= 1;
  }

  public void mouseReleased(MouseEvent e) {
    if (isLineMode()) {
      view.repaint();
    }
  }

  protected void addHexToSelection(Point p) {
    final HexRef hexPos = new HexRef(myGrid.getHexPos(p));
    addHexToSelection(hexPos, p);
  }

  protected void addHexToSelection(HexRef hexPos) {
    addHexToSelection(hexPos, hexPos.getIntCenter());
  }

  protected void addHexToSelection(HexRef hexPos, Point p) {
    int index = selectedHexList.indexOf(hexPos);
    if (index < 0) {
      final Area hex = myGrid.getSingleHex(p);
      selectedHexList.add(hexPos);
      selectedHexArea.add(hex);
      view.repaint(hex.getBounds());
      updateButtonPanel();
    }
  }

  protected void removeHexFromSelection(Point p) {
    final HexRef hexPos = new HexRef(myGrid.getHexPos(p));
    int index = selectedHexList.indexOf(hexPos);
    if (index >= 0) {
      final Area hex = myGrid.getSingleHex(p);
      selectedHexList.remove(index);
      selectedHexArea.remove(index);
      view.repaint(hex.getBounds());
    }
    updateButtonPanel();
  }

  protected void updateButtonPanel() {
    if (isAttributeMode()) {
      attributePanel.updateValues();
    }
  }

  protected void addEdgeToSelection(Point p) {
    Point snap = getEdgeCenter(p);

    if (snap != null) {
      final int size = selectedEdgeList.size();
      if (size > 1) {
        EdgeRef prior = selectedEdgeList.get(size - 2);
        if (snap.equals(prior.getCenter())) {
          final EdgeRef last = selectedEdgeList.get(size - 1);
          selectedEdgeList.remove(size - 1);
          view.repaint(last.getBounds());
          return;
        }
      }

      if (!edgeListContainsPoint(snap)) {
        final EdgeRef ref = new EdgeRef(snap, myGrid); 
        selectedEdgeList.add(ref);
        view.repaint(ref.getBounds());
      }
    }
  }

  protected boolean edgeListContainsPoint(Point p) {
    for (int i = 0; i < selectedEdgeList.size(); i++) {
      //FIXME This is probably a bug
      if (selectedEdgeList.get(i).getCenter().equals(p)) {
        return true;
      }
    }
    return false;
  }

  protected void removeEdgeFromSelection(Point p) {
    final Point snap = getEdgeCenter(p);
    final EdgeRef edge = new EdgeRef(snap, myGrid);

    if (selectedEdgeList.contains(edge)) {
      selectedEdgeList.remove(edge);
      view.repaint(edge.getBounds());
    }
  }

  protected void removeFromLineSelection(HexRef ref) {

  }

  protected void addToLineSelection(HexRef ref) {

    if (!ref.equals(lastHex) && ref.isAdjacentTo(lastHex)) {
      final LineRef line1 = new LineRef(lastHex, ref, myGrid);
      final LineRef line2 = new LineRef(ref, lastHex, myGrid);

      if (selectedLineList.contains(line1) || selectedLineList.contains(line2)) {
        selectedLineList.remove(line1);
        selectedLineList.remove(line2);
      }
      else {
        selectedLineList.add(line1);
      }
      lastRef = line1;
      view.repaint(line1.getBounds());
    }

  }

  protected Point getEdgeCenter(Point p) {
    final Point snap = myGrid.snapToHexSide(p);
    final Point snapCentre = myGrid.snapToHex(p);

    // Ignore snaps to hex centres
    if (Math.abs(snap.x - snapCentre.x) <= 2 && Math.abs(snap.y - snapCentre.y) <= 2) {
      return null;
    }
    return snap;
  }

  public void mouseDragged(MouseEvent e) {
    if (isHexMode() || isAttributeMode()) {
      final HexRef pos = new HexRef(myGrid.getHexPos(e.getPoint()));
      if (!pos.equals(lastHex)) {
        if (e.isControlDown()) {
          removeHexFromSelection(e.getPoint());
        }
        else {
          addHexToSelection(e.getPoint());
        }
        lastHex = pos;
      }
    }
    else if (isEdgeMode()) {
      final Point snap = getEdgeCenter(e.getPoint());

      if (snap != null && !snap.equals(lastSnap)) {
        if (e.isControlDown()) {
          removeEdgeFromSelection(e.getPoint());
        }
        else {
          addEdgeToSelection(e.getPoint());
        }
        lastSnap = snap;
      }
    }
    else if (isLineMode()) {
      final HexRef ref = new HexRef(myGrid.getHexPos(e.getPoint()));
      if (!ref.equals(lastHex)) {
        addToLineSelection(ref);
        lastHex = ref;
      }
    }
  }

  protected void addLineToSelection(LineRef ref) {
    if (selectedLineList.contains(ref)) {
      removeLineFromSelection(ref);
    }
    else {
      selectedLineList.add(ref);
      view.repaint(ref.getBounds());
    }
  }

  protected void removeLineFromSelection(LineRef ref) {
    if (selectedLineList.contains(ref)) {
      selectedLineList.remove(ref);
      view.repaint(ref.getBounds());
    }
  }

  public void mouseMoved(MouseEvent e) {

  }

  protected void clearHexSelection() {
    selectedHexList.clear();
    selectedHexArea.clear();
    lastHex = null;
    view.repaint();
  }

  protected void clearEdgeSelection() {
    selectedEdgeList.clear();
    lastSnap = null;
    view.repaint();
  }

  protected void clearLineSelection() {
    selectedLineList.clear();
    lastRef = null;
    view.repaint();
  }

  // Copy any currently select Hex terrain into the terrain map
  protected void setSelectedHexTerrain(String terrainName) {
    terrainMap.setHexTerrainType(selectedHexList, (HexTerrain) getTerrainDefs().getHexTerrainDefinitions().getTerrain(terrainName));
    view.repaint();
  }

  // Copy any currently select Edge terrain into the terrain map
  protected void setSelectedEdgeTerrain(String terrainName) {
    terrainMap.setEdgeTerrainType(selectedEdgeList, myGrid, (EdgeTerrain) getTerrainDefs().getEdgeTerrainDefinitions().getTerrain(terrainName));
    view.repaint();
  }

  // Copy any currently select Line terrain into the terrain map
  protected void setSelectedLineTerrain(String terrainName) {
    terrainMap.setLineTerrainType(selectedLineList, myGrid, (LineTerrain) getTerrainDefs().getLineTerrainDefinitions().getTerrain(terrainName));
    view.repaint();
  }

  // Copy any Currently selected attribute terrain into the terrain map
  protected void setSelectedAttributeTerrain() {
    for (HexRef hex : selectedHexList) {
      for (AttributeSetter setter : attributePanel.getValues()) {
        if (!setter.isDifferent()) {
          TerrainAttribute attr = new TerrainAttribute(hex, setter.getTerrain(), (TerrainHexGrid) grid, setter.getValue());
          terrainMap.setAttributeTerrainType(attr);
        }
      }
    }
    view.repaint();
  }

  public void actionPerformed(ActionEvent e) {
    final String option = e.getActionCommand();

    if (OK.equals(option)) {
      cancelSetMode();
      setMode(BLANK);
      terrainMap.save();
      finalise();
    }
    else if (SET.equals(option)) {
      startSetMode();
    }
    else if (TOGGLE_NUMBERING.equals(option)) {
      ((RegularGridNumbering) grid.getGridNumbering()).setAttribute(RegularGridNumbering.VISIBLE, !grid.getGridNumbering().isVisible());
      repaint();
    }
    else if (TOGGLE_GRID.equals(option)) {
      grid.setVisible(!grid.isVisible());
      repaint();
    }
    else if (CANCEL_SET.equals(option)) {
      cancelSetMode();
    }
    else if (CANCEL.equals(option)) {
      cancelSetMode();
      grid.setDx(saveDx);
      grid.setDy(saveDy);
      grid.setOrigin(saveOrigin);
      finalise();
    }

    terrainMap.rebuild();
    view.repaint();
    view.requestFocus();
  }

  protected void finalise() {
    myGrid.setVisible(saveGridVisible);
    if (myGrid.getGridNumbering() != null) {
      ((RegularGridNumbering) grid.getGridNumbering()).setAttribute(RegularGridNumbering.VISIBLE, saveNumberingVisible);
    }
    myGrid.setEdgesLegal(saveSnapToEdges);
    myGrid.setCornersLegal(saveSnapToCorners);
    setVisible(false);
  }

  public void rebuild() {
    terrainMap.rebuild();
  }

  protected void initComponents() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    view = new TerrainGridPanel(board);

    view.addMouseListener(this);
    view.addMouseMotionListener(this);
    view.addKeyListener(this);
    view.setFocusable(true);

    scroll = new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    scroll.setPreferredSize(new Dimension(800, 600));

    tabPane = new JTabbedPane();
    tabPane.setBorder(BorderFactory.createEtchedBorder());

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    /*
     * Hex Terrain Mode button and panel
     */
    final JPanel hexPanel = new HexPanel();
    tabPane.add(HEX, new JScrollPane(hexPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    /*
     * Edge Terrain Mode button and panel
     */
    final JPanel edgePanel = new EdgePanel();
    tabPane.add(EDGE, new JScrollPane(edgePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    /*
     * Connection Terrain Mode button and panel
     */
    final JPanel connectPanel = new LinePanel();
    tabPane.add(LINE, new JScrollPane(connectPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    /*
     * Attribute Terrain Mode button and panel
     */
    attributePanel = new AttributePanel(this);
    attributePanel.updateValues();
    final JScrollPane attributeScroll = new JScrollPane(attributePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    tabPane.add(ATTRIBUTE, attributeScroll);

    /*
     * Set Grid Mode button and panel
     */

    final JPanel gridPanel = new JPanel();
    gridPanel.setLayout(new GridBagLayout());
    gridPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

    final JPanel commentPanel = new JPanel();
    commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
    JLabel l = new JLabel("Arrow Keys - Move Grid");
    l.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    commentPanel.add(l);
    l = new JLabel("Control-Arrow Keys - Resize Grid");
    l.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    commentPanel.add(l);
    l = new JLabel("Shift Key - Increase speed of other keys");
    l.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    commentPanel.add(l);

    final GridBagConstraints comC = new GridBagConstraints();
    comC.gridx = 0;
    comC.gridy = 0;
    comC.gridheight = 4;
    comC.insets = new Insets(0, 0, 0, 20);
    gridPanel.add(commentPanel, comC);

    setButton = new JButton(SET);
    setButton.addActionListener(this);
    final GridBagConstraints setC = new GridBagConstraints();
    setC.gridx = 1;
    setC.gridy = 0;
    setC.fill = GridBagConstraints.HORIZONTAL;
    gridPanel.add(setButton, setC);

    canSetButton = new JButton(CANCEL_SET);
    canSetButton.addActionListener(this);
    canSetButton.setVisible(false);
    final GridBagConstraints canC = new GridBagConstraints();
    canC.gridx = 1;
    canC.gridy = 1;
    canC.fill = GridBagConstraints.HORIZONTAL;
    gridPanel.add(canSetButton, canC);

    numberingButton = new JButton(TOGGLE_NUMBERING);
    numberingButton.addActionListener(this);
    numberingButton.setEnabled(grid.getGridNumbering() != null);
    numberingButton.setVisible(true);
    final GridBagConstraints numC = new GridBagConstraints();
    numC.gridx = 1;
    numC.gridy = 2;
    numC.fill = GridBagConstraints.HORIZONTAL;
    gridPanel.add(numberingButton, numC);

    hideGridButton = new JButton(TOGGLE_GRID);
    hideGridButton.addActionListener(this);
    hideGridButton.setVisible(true);
    final GridBagConstraints toggleC = new GridBagConstraints();
    toggleC.gridx = 1;
    toggleC.gridy = 3;
    toggleC.fill = GridBagConstraints.HORIZONTAL;
    gridPanel.add(hideGridButton, toggleC);

    tabPane.add(GRID, new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

    /*
     * Options button and panel
     */
    final JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
    mapOpacity = new OpacityConfigurer(null, "Map Opacity", 75);
    mapOpacity.addPropertyChangeListener(e -> view.repaint());
    optionsPanel.add(mapOpacity.getControls());
    tabPane.add(OPTIONS, new JScrollPane(optionsPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

    /*
     * Save Button
     */
    final JPanel saveCanPanel = new JPanel();
    saveCanPanel.setLayout(new GridBagLayout());
    saveCanPanel.setBorder(BorderFactory.createEtchedBorder());

    final JButton okButton = new JButton(OK);
    okButton.addActionListener(this);
    okButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

    final JButton canButton = new JButton(CANCEL);
    canButton.addActionListener(this);
    canButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

    final GridBagConstraints fc1 = new GridBagConstraints();
    fc1.gridx = 0;
    fc1.gridy = 0;
    saveCanPanel.add(Box.createRigidArea(new Dimension(30, 30)), fc1);

    final GridBagConstraints sc = new GridBagConstraints();
    sc.gridx = 0;
    sc.gridy = 1;
    sc.fill = GridBagConstraints.HORIZONTAL;
    saveCanPanel.add(okButton, sc);

    final GridBagConstraints cc = new GridBagConstraints();
    cc.gridx = 0;
    cc.gridy = 2;
    cc.fill = GridBagConstraints.HORIZONTAL;
    saveCanPanel.add(canButton, cc);

    final GridBagConstraints fc2 = new GridBagConstraints();
    fc2.gridx = 0;
    fc2.gridy = 3;
    saveCanPanel.add(Box.createRigidArea(new Dimension(30, 30)), fc2);

    bottomPanel.add(saveCanPanel, BorderLayout.WEST);

    bottomPanel.add(tabPane, BorderLayout.CENTER);

    final JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, bottomPanel);
    getContentPane().add(splitter);

    tabPane.addChangeListener(e -> setMode(tabPane.getTitleAt(tabPane.getSelectedIndex())));

    // Turn off grid caching
    board.setCacheGrid(false);

    scroll.revalidate();
    pack();
    repaint();
  }

  protected void addCenter(JComponent parent, JComponent child) {
    child.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    parent.add(child);
  }

  protected int getMapOpacity() {
    return mapOpacity.getIntValue(50);
  }

  protected void setMode(String m) {
    if (isGridMode() && !m.equals(GRID)) {
      cancelSetMode();
    }
    mode = m;

    if (!isHexMode() && !isAttributeMode()) {
      clearHexSelection();
    }

    if (!isEdgeMode()) {
      clearEdgeSelection();
    }

    if (!isLineMode()) {
      clearLineSelection();
    }

    if (isHexMode()) {
      clearEdgeSelection();
    }
    else if (isEdgeMode()) {
      clearHexSelection();
    }
    else if (isLineMode()) {
      clearHexSelection();
      clearEdgeSelection();
      myGrid.setCornersLegal(false);
      myGrid.setEdgesLegal(true);
    }
    else if (isAttributeMode()) {
      clearHexSelection();
      clearEdgeSelection();
      attributePanel.updateValues();
    }
    else if (isGridMode()) {
      clearHexSelection();
      clearEdgeSelection();
    }
    // terrainMap.rebuild();
    view.repaint();
    view.requestFocus();
  }

  protected boolean isGridMode() {
    return mode.equals(GRID);
  }

  protected boolean isLineMode() {
    return mode.equals(LINE);
  }

  protected boolean isHexMode() {
    return mode.equals(HEX);
  }

  protected boolean isEdgeMode() {
    return mode.equals(EDGE);
  }

  protected boolean isAttributeMode() {
    return mode.equals(ATTRIBUTE);
  }

  public void keyPressed(KeyEvent e) {
    if (isGridMode()) {
      super.keyPressed(e);
    }
  }

  public void paintTerrain(Graphics g) {
    final Graphics2D g2 = (Graphics2D) g;
    final Color oldColor = g.getColor();
    final Composite oldComposite = g2.getComposite();

    float opacity = isHexMode() ? ACTIVE_OPACITY : INACTIVE_OPACITY;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

    String[] terrainNames = TerrainDefinitions.getInstance().getHexTerrainDefinitions().getTerrainNames();
    for (final String type : terrainNames) {
      final Area area = terrainMap.getHexArea(type);
      if (area != null) {
        final Color color = TerrainDefinitions.getInstance().getHexTerrainDefinitions().getTerrain(type).getColor();
        g.setColor(color);
        g2.fill(area);
      }
    }

    opacity = isEdgeMode() ? ACTIVE_OPACITY : INACTIVE_OPACITY;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

    terrainNames = TerrainDefinitions.getInstance().getEdgeTerrainDefinitions().getTerrainNames();
    for (final String type : terrainNames) {
      final GeneralPath poly = terrainMap.getEdgePoly(type);
      if (poly != null) {
        final Color color = TerrainDefinitions.getInstance().getEdgeTerrainDefinitions().getTerrain(type).getColor();
        g2.setColor(color);
        g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(poly);
      }
    }

    opacity = isLineMode() ? ACTIVE_OPACITY : INACTIVE_OPACITY;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

    terrainNames = TerrainDefinitions.getInstance().getLineTerrainDefinitions().getTerrainNames();
    for (final String type : terrainNames) {
      final GeneralPath poly = terrainMap.getLinePoly(type);
      if (poly != null) {
        final LineTerrain lt = (LineTerrain) TerrainDefinitions.getInstance().getLineTerrainDefinitions().getTerrain(type);
        g2.setColor(lt.getColor());
        g2.setStroke(lt.getStroke());
        g2.draw(poly);
      }
    }

    opacity = isAttributeMode() ? ACTIVE_OPACITY : INACTIVE_OPACITY;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    HexRef lastRef = null;
    int count = 0;
    for (Iterator<TerrainAttribute> i = terrainMap.getSortedAttributeTerrain(); i.hasNext();) {
      final TerrainAttribute attr = i.next();
      if (attr.getLocation().equals(lastRef)) {
        count++;
      }
      else {
        count = 0;
        lastRef = attr.getLocation();
      }
      final Point pos = attr.getLocation().getIntCenter();
      final Color color = attr.getTerrain().getColor();
      final String text = attr.getValue();
      LabelUtils.drawLabel(g, text, pos.x, pos.y + ((count-1) * 15), 0, 0, Color.black, color);
    }

    g2.setComposite(oldComposite);
    g.setColor(oldColor);
  }

  public void paintSelected(Graphics g) {
    if (selectedHexList.size() > 0) {
      final Graphics2D g2 = (Graphics2D) g;
      final Color oldColor = g.getColor();
      final Composite oldComposite = g2.getComposite();
       g2.setPaint(getSelectionPaint());
      for (Area hex : selectedHexArea) {
        g2.fill(hex);
      }
      g2.setComposite(oldComposite);
      g.setColor(oldColor);
    }

    if (selectedEdgeList.size() > 0) {
      final Graphics2D g2 = (Graphics2D) g;
      final Color oldColor = g.getColor();
      final Composite oldComposite = g2.getComposite();
      g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.setColor(Color.red);
      g2.setPaint(getSelectionPaint());
      final GeneralPath poly = new GeneralPath();
      for (EdgeRef ref : selectedEdgeList) {
        final Point end1 = ref.getIntEnd1();
        final Point end2 = ref.getIntEnd2();
        poly.moveTo(end1.x, end1.y);
        poly.lineTo(end2.x, end2.y);
      }
      g2.draw(poly);
      g2.setComposite(oldComposite);
      g.setColor(oldColor);
    }

    if (selectedLineList.size() > 0 || lastRef != null) {
      final Graphics2D g2 = (Graphics2D) g;
      final Color oldColor = g.getColor();
      g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g.setColor(Color.red);
      g2.setPaint(getSelectionPaint());
      final GeneralPath poly = new GeneralPath();
      if (selectedLineList.size() == 0) {
        final Point p = lastRef.getIntCenter();
        poly.moveTo(p.x, p.y);
        poly.lineTo(p.x, p.y);
      }
      else {
        // Point lastEnd = null;
        for (LineRef ref : selectedLineList) {
          final Point end1 = ref.getIntEnd1();
          final Point end2 = ref.getIntEnd2();
          // if (end1.equals(lastEnd)) {
          // poly.lineTo(end2.x, end2.y);
          // lastEnd = end2;
          // }
          // else if (end2.equals(lastEnd)) {
          // poly.lineTo(end1.x, end1.y);
          // lastEnd = end1;
          // }
          // else {
          poly.moveTo(end1.x, end1.y);
          poly.lineTo(end2.x, end2.y);
          // lastEnd = end2;
          // }
        }
      }
      g2.draw(poly);
      g.setColor(oldColor);
    }
  }

  protected Paint getSelectionPaint() {
    if (selectionPaint == null) {
      final BufferedImage pattern = new BufferedImage(3, 3, BufferedImage.TYPE_4BYTE_ABGR);
      final Graphics2D g2 = pattern.createGraphics();
      g2.setColor(Color.red);
      g2.drawLine(0, 2, 2, 0);
      // g2.drawLine(1, 1, 1, 1);
      selectionPaint = new TexturePaint(pattern, new Rectangle(0, 0, 3, 3));
    }
    return selectionPaint;
  }

  /*
   * ---------------------------------------------------------------------
   * Following code extracted from HexGrid.HexGridEditor
   */
  /*
   * Calculate approximate grid metrics based on the three adjacent points
   * picked out by the user.
   */
  public void calculate() {

    /*
     * Two of the points must lie on the same horizontal or vertical line (be
     * perpendicular to). The third point must not be perpendicular to either of
     * the first two. First step is to work out which is which as we can't be
     * sure what order they picked out the points in.
     */

    if (isPerpendicular(hp1, hp2)) {
      calculate_step2(hp1, hp2, hp3);
    }
    else if (isPerpendicular(hp1, hp3)) {
      calculate_step2(hp1, hp3, hp2);
    }
    else if (isPerpendicular(hp2, hp3)) {
      calculate_step2(hp2, hp3, hp1);
    }
    else {
      reportShapeError();
    }
    terrainMap.rebuild();
    view.repaint();
  }

  /*
   * Step 2. Check third point is not perpendicular to either of the first two,
   * then call appropriate calculation routine depending on location relative to
   * the first two.
   */
  protected void calculate_step2(Point p1, Point p2, Point p3) {
    if (!isPerpendicular(p1, p3) && !isPerpendicular(p2, p3)) {
      if (isHorizontal(p1, p2)) {
        if ((p3.x < p1.x && p3.x < p2.x) || (p3.x > p1.x && p3.x > p2.x)) {
          check(false, p1, p2, p3);
        }
        else {
          checkEnd(true, p1, p2, p3);
        }
      }
      else {
        if ((p3.y < p1.y && p3.y < p2.y) || (p3.y > p1.y && p3.y > p2.y)) {
          check(true, reverse(p1), reverse(p2), reverse(p3));
        }
        else {
          checkEnd(false, reverse(p1), reverse(p2), reverse(p3));
        }
      }
    }
    else {
      reportShapeError();
    }
  }

  protected Point reverse(Point p) {
    return new Point(p.y, p.x);
  }

  protected void check(boolean sideways, Point p1, Point p2, Point p3) {

    final int r = Math.abs(p1.x - p2.x);
    final int width = r * 3 / 2;
    final int height = Math.abs(p3.y - p2.y) * 2;

    final int Xoff = (Math.min(p1.x, p2.x)) % width + r / 2;
    final int col = Math.min(p1.x, p2.x) / width;
    int Yoff = Math.min(p1.y, p2.y) % height - ((col % 2 == 1) ? 0 : (int) (height / 2));
    if (Yoff < 0)
      Yoff += height;

    setMetrics(width, height, Xoff, Yoff, sideways);
  }

  protected void checkEnd(boolean sideways, Point p1, Point p2, Point p3) {
    if (Math.abs((p1.x + p2.x) / 2 - p3.x) > ERROR_MARGIN) {
      reportShapeError();
      return;
    }

    final int r = Math.abs(p3.y - p1.y) * 2;
    final int width = r * 3 / 2;
    final int height = Math.abs(p3.x - p2.x) * 2;

    final int xOrigin = p1.y - (p3.y < p1.y ? 0 : r);
    final int Xoff = xOrigin % width + r / 2;
    final int col = xOrigin / width;
    int Yoff = Math.min(p1.x, p2.x) % height - ((col % 2 == 1) ? 0 : (height / 2));

    setMetrics(width, height, Xoff, Yoff, sideways);
  }

  protected void setMetrics(int width, int height, int xoff, int yoff, boolean b) {

    grid.setDx(width);
    grid.setDy(height);
    grid.setOrigin(new Point(xoff, yoff));
    grid.setSideways(b);

  }

  protected class TerrainGridPanel extends GridEditor.GridPanel {

    private static final long serialVersionUID = 1L;
    protected Board board;

    public TerrainGridPanel() {
      super();
    }

    public TerrainGridPanel(Board b) {
      super(b);
    }

    public void paint(Graphics g) {
      final Graphics2D g2 = (Graphics2D) g;
      Composite oldComposite = null;
      if (getMapOpacity() < 100) {
        oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getMapOpacity() / 100.0f));
      }
      super.paint(g);
      if (oldComposite != null) {
        g2.setComposite(oldComposite);
      }
      paintTerrain(g);
      paintSelected(g);
    }

  }

  protected abstract class BasicPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    public static final int COLS = 4;
    protected ArrayList<JButton> buttons = new ArrayList<>();

    public BasicPanel() {
      super();
    }

    public void init() {

      setLayout(new GridBagLayout());

      GridBagConstraints[] constraints = new GridBagConstraints[COLS];
      for (int i = 0; i < COLS; i++) {
        constraints[i] = new GridBagConstraints();
        constraints[i].gridx = i;
      }
      int col = 3;
      int row = -1;

      for (JButton button : buttons) {
        remove(button);
      }
      buttons.clear();

      final String[] terrainNames = getTerrainNames();
      final Icon[] terrainIcons = getTerrainIcons();

      for (int i = 0; i < terrainNames.length; i++) {
        col++;
        if (col >= COLS) {
          col = 0;
          row++;
          for (int j = 0; j < COLS; j++) {
            constraints[j].gridy = row;
          }
        }
        JButton button;
        if (terrainNames[i] == null) {
          button = new JButton(terrainNames[i]);
        }
        else {
          button = new JButton(terrainNames[i], terrainIcons[i]);
        }
        button.addActionListener(this);
        add(button, constraints[col]);
        buttons.add(button);
      }
    }

    public abstract String[] getTerrainNames();

    public abstract Icon[] getTerrainIcons();

  }

  /*
   * Panel of Terrain Selection buttons for Hex Mode
   */
  protected class HexPanel extends BasicPanel {

    private static final long serialVersionUID = 1L;

    public HexPanel() {
      super();
      init();
    }

    public void actionPerformed(ActionEvent e) {
      setSelectedHexTerrain(e.getActionCommand());
      clearHexSelection();
    }

    @Override
    public Icon[] getTerrainIcons() {
      return getTerrainDefs().getHexTerrainDefinitions().getTerrainIcons();
    }

    @Override
    public String[] getTerrainNames() {
      return getTerrainDefs().getHexTerrainDefinitions().getTerrainNames();
    }

  }

  /*
   * Panel of Terrain Selection buttons for Edge Mode
   */
  protected class EdgePanel extends BasicPanel {

    private static final long serialVersionUID = 1L;

    public EdgePanel() {
      super();
      init();
    }

    public void actionPerformed(ActionEvent e) {
      setSelectedEdgeTerrain(e.getActionCommand());
      clearEdgeSelection();
    }

    @Override
    public Icon[] getTerrainIcons() {
      return getTerrainDefs().getEdgeTerrainDefinitions().getTerrainIcons();
    }

    @Override
    public String[] getTerrainNames() {
      return getTerrainDefs().getEdgeTerrainDefinitions().getTerrainNames();
    }
  }

  /*
   * Panel of Terrain Selection buttons for Line Mode
   */
  protected class LinePanel extends BasicPanel {

    private static final long serialVersionUID = 1L;

    public LinePanel() {
      super();
      init();
    }

    public void actionPerformed(ActionEvent e) {
      setSelectedLineTerrain(e.getActionCommand());
      clearLineSelection();
    }

    @Override
    public Icon[] getTerrainIcons() {
      return getTerrainDefs().getLineTerrainDefinitions().getTerrainIcons();
    }

    @Override
    public String[] getTerrainNames() {
      return getTerrainDefs().getLineTerrainDefinitions().getTerrainNames();
    }
  }

  /*
   * Panel of Terrain Selection for Attribute Mode
   */
  protected class AttributePanel extends JPanel implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    protected ArrayList<AttributeSetter> setters = new ArrayList<>();
    protected TerrainHexGridEditor myEditor;
    protected JButton saveButton;

    public AttributePanel(TerrainHexGridEditor editor) {
      super();
      myEditor = editor;
      init();
      updateValues();
    }

    public void init() {

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      saveButton = new JButton("<html><center>Save<br>Changes");
      saveButton.setEnabled(false);
      saveButton.addActionListener(this);
      saveButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
      saveButton.setMaximumSize(new Dimension(80, 80));
      saveButton.setPreferredSize(new Dimension(80, 80));

      final JPanel savePanel = new JPanel();
      savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));
      savePanel.add(Box.createHorizontalStrut(10));
      savePanel.add(saveButton);
      add(savePanel);

      final JPanel listPanel = new JPanel();
      listPanel.setLayout(new GridBagLayout());

      final GridBagConstraints c1 = new GridBagConstraints();
      c1.gridx = 0;
      c1.anchor = GridBagConstraints.LINE_START;

      final GridBagConstraints c2 = new GridBagConstraints();
      c2.gridx = 1;

      final GridBagConstraints c3 = new GridBagConstraints();
      c3.gridx = 2;

      final GridBagConstraints c4 = new GridBagConstraints();
      c4.gridx = 3;
      c4.anchor = GridBagConstraints.LINE_START;
      c4.insets = new Insets(0, 10, 0, 0);

      final GridBagConstraints c5 = new GridBagConstraints();
      c5.gridx = 4;

      final GridBagConstraints c6 = new GridBagConstraints();
      c6.gridx = 5;
      c6.insets = new Insets(0, 0, 0, 10);

      int row = 0;
      boolean left = true;
      for (Buildable b : getTerrainDefs().getAttributeTerrainDefinitions().getBuildables()) {

        final AttributeTerrain at = (AttributeTerrain) b;
        at.setPropertyChangeListener(this);
        final AttributeSetter bac = at.getAttributeConfigurer();
        setters.add(bac);
        bac.setPreferredSize(new Dimension(150, (int) bac.getPreferredSize().getHeight()));

        final JLabel label = bac.getLabel();
        label.setMaximumSize(label.getPreferredSize());

        final JButton saButton = new JButton("Select");
        saButton.setMaximumSize(saButton.getPreferredSize());
        saButton.addActionListener(new SelectActionListener(at, bac));

        if (left) {
          c1.gridy = c2.gridy = c3.gridy = row;
          listPanel.add(label, c1);
          listPanel.add(bac, c2);
          listPanel.add(saButton, c3);
        }
        else {
          c4.gridy = c5.gridy = c6.gridy = row++;
          listPanel.add(label, c4);
          listPanel.add(bac, c5);
          listPanel.add(saButton, c6);
        }
        left = !left;

      }

      add(listPanel);

    }

    /**
     * ActionListener attached to the AtrributeTerrain Select buttons. Hitting
     * the Select button selects all hexes of that terrain type with a value
     * equal to the setter
     * 
     */
    class SelectActionListener implements ActionListener {

      private final AttributeTerrain attr;
      private final AttributeSetter setter;

      public SelectActionListener(AttributeTerrain at, AttributeSetter as) {
        attr = at;
        setter = as;
      }

      public void actionPerformed(ActionEvent e) {
        clearHexSelection();
        final String searchValue = (setter.isBlank() || setter.isDifferent()) ? null : setter.getValue();
        for (Iterator<TerrainAttribute> i = terrainMap.getAllAttributeTerrain(); i.hasNext();) {
          final TerrainAttribute terrain = i.next();
          final AttributeTerrain at = terrain.getAttributeTerrain();
          if (at.getConfigureName().equals(attr.getConfigureName())) {
            if (searchValue == null || searchValue.equals(terrain.getValue())) {
              addHexToSelection(terrain.getLocation());
            }
          }
        }
      }

    }

    protected List<AttributeSetter> getValues() {
      return setters;
    }

    /**
     * Update the values in all Setters to reflect the values saved in the
     * current selected hex list. Set the Setter to show '<different>' if all
     * hexes do not have the same value
     */
    protected void updateValues() {
      String lastValue;
      if (selectedHexList != null) {
        for (AttributeSetter setter : setters) {
          setter.setValue("");
          lastValue = null;
          for (HexRef hexRef : myEditor.selectedHexList) {
            final AttrRef ref = new AttrRef(hexRef, setter.getName());
            final TerrainAttribute terrain = terrainMap.getAttributeTerrain(ref);
            final String value = (terrain == null ? "" : terrain.getValue());
            if (lastValue == null) {
              setter.setValue(value);
            }
            else if (!lastValue.equals(value)) {
              setter.setDifferent();
            }
            lastValue = value;
          }
        }
      }
      saveButton.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      setSelectedAttributeTerrain();
      clearHexSelection();
      saveButton.setEnabled(false);
    }

    public void propertyChange(PropertyChangeEvent e) {
      saveButton.setEnabled(true);
    }

  }
}
