/*
 * $Id: TdcLeader 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2014 by Brent Easton
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
 * 
 * 27-Mar-20 BE Add support for Flip to Replacement leader stats
 */

package tdc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import net.miginfocom.swing.MigLayout;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.MapGrid;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.configure.IntConfigurer;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.KeyCommandSubMenu;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.Properties;
import VASSAL.counters.Stack;
import VASSAL.tools.NamedKeyStroke;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.image.LabelUtils;
/**
 * Handle support for attachable independent units in D-Day This trait is for
 * the Leader Units. See also TdcIndependent
 */
public class TdcLeader extends Decorator implements EditablePiece {

  public static final String ID = "leader;";

  protected int commandRange;
  protected int activateLimit; // Max number of Ind activations
  protected int commandRangeR; // Command Range for Replacement Leader
  protected int activateLimitR; // Max number of Ind activations for Replacement Leader
  protected int activateCount; // Remaining Ind activations
  protected boolean replacement;
  protected KeyCommand[] keyCommands = new KeyCommand[0];
  protected NamedKeyStroke[] keyStrokes = new NamedKeyStroke[0];
  protected ArrayList<GamePiece> units = new ArrayList<>();
  protected ArrayList<String> activatedUnits = new ArrayList<>();
  protected double lastZoom = 0.0;
  protected Font displayFont;
  protected Font displayFont2;
  protected String infoCommand = "Info";
  protected String flipCommand = "Flip";
  protected KeyStroke infoKey = KeyStroke.getKeyStroke('I', java.awt.event.InputEvent.ALT_MASK);
  protected KeyStroke flipKey = KeyStroke.getKeyStroke('F', java.awt.event.InputEvent.CTRL_MASK);
  protected boolean isShowingInfo = false;
  protected BufferedImage infoImage;

  protected static final String NONE_ELIGIBLE = "None Eligible";
  protected static final String ACTIVATION_LIMIT_REACHED = "Activation Limit Reached";
  protected static final String ACTIVATE_INDEPENDENTS = "Activate Independents";

  protected static final Font FONT_LG = new Font("Dialog", Font.BOLD, 16);

  public TdcLeader() {
    this(ID + "6;10;4;8", null);
  }

  public TdcLeader(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public int getActivateLimit() {
    return activateLimit;
  }

  public int getActivateLimitR() {
    return activateLimitR;
  }

  public int getCurrentActivateLimit() {
    return isReplacement() ? getActivateLimitR() : getActivateLimit();
  }

  public int getActivateCount() {
    return activateCount;
  }

  public int getCommandRange() {
    return commandRange;
  }

  public int getCommandRangeR() {
    return commandRangeR;
  }

  public int getCurrentCommandRange() {
    return isReplacement() ? commandRangeR : commandRange;
  }

  public boolean isReplacement() {
    return replacement;
  }

  public void setReplacement(boolean r) {
    replacement = r;
  }

  public String getActivateCountString() {
    return String.valueOf(activateCount);
  }

  public void setActivateCount(int newCount) {
    activateCount = newCount;
  }

  public void setActivateCount(String newCount) {
    try {
      setActivateCount(Integer.parseInt(newCount));
    }
    catch (Exception ignored) {

    }
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    piece.draw(g, x, y, obs, zoom);

    final double os_scale = Info.getSystemScaling();
    
    if (isShowingInfo && Boolean.TRUE.equals(getProperty(Properties.SELECTED))) {
      buildInfoImage();
      g.drawImage(infoImage, x - (int) (infoImage.getWidth() * os_scale / 2), y + (int) (zoom * os_scale * 37.5), null);
    }

    if (zoom != lastZoom) {
      lastZoom = zoom;
      displayFont = new Font("Dialog", Font.BOLD, (int) (16 * zoom * os_scale));
      displayFont2 = new Font("Dialog", Font.BOLD, (int) (14 * zoom * os_scale));
    }

    // Draw replacement values?
    if (isReplacement()) {
      LabelUtils.drawLabel(g, String.valueOf(getCurrentCommandRange()), x + (int) (30 * zoom * os_scale), y + (int) (-12 * zoom * os_scale), displayFont2, LabelUtils.CENTER, LabelUtils.CENTER, Color.BLACK, Color.RED, Color.WHITE);
      LabelUtils.drawLabel(g, String.valueOf(getCurrentActivateLimit()), x + (int) (30 * zoom * os_scale), y + (int) (7 * zoom * os_scale), displayFont2, LabelUtils.CENTER, LabelUtils.CENTER, Color.BLACK, Color.WHITE, Color.BLACK);
      g.setColor(Color.RED);
//      int x1 = 1, y1 = -5, cs = 15;
//      g.drawLine(x+(int) (x1*zoom), y+(int)(y1*zoom), x + (int) ((x1+cs) * zoom), y+(int)((y1+cs)*zoom));
//      g.drawLine(x+(int) ((x1)*zoom), y+(int)((y1+cs)*zoom), x + (int) ((x1+cs) * zoom), y+(int)(y1*zoom));
//      y1 += 23;
//      g.drawLine(x+(int) (x1*zoom), y+(int)(y1*zoom), x + (int) ((x1+cs) * zoom), y+(int)((y1+cs)*zoom));
//      g.drawLine(x+(int) ((x1)*zoom), y+(int)((y1+cs)*zoom), x + (int) ((x1+cs) * zoom), y+(int)(y1*zoom));
//      
    }

    final boolean activated = "2".equals(Decorator.getOutermost(piece).getProperty("Active_Level"));
    if (!activated) {
      return;
    }


    LabelUtils.drawLabel(g, getActivateCountString(), x + (int) (30 * zoom * os_scale), y + (int) (26 * zoom * os_scale), displayFont2, LabelUtils.CENTER, LabelUtils.CENTER, Color.BLUE, Color.WHITE, Color.BLACK);

  }

  public Rectangle boundingBox() {
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String getName() {
    return piece.getName();
  }

  public void mySetState(String newState) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(newState, ';');
    setActivateCount(st.nextInt(0));
    setReplacement(st.nextBoolean(false));
    activatedUnits.clear();
    int count = st.nextInt(0);
    for (int i = 0; i < count; i++) {
      activatedUnits.add(st.nextToken());
    }
  }

  public String myGetState() {
    final SequenceEncoder se = new SequenceEncoder(';');
    se.append(getActivateCountString()).append(isReplacement()).append(activatedUnits.size());
    for (String u : activatedUnits) {
      se.append(u);
    }

    return se.getValue();
  }

  public String myGetType() {
    final SequenceEncoder se = new SequenceEncoder(';');
    se.append(activateLimit).append(commandRange).append(activateLimitR).append(commandRangeR);
    return ID + se.getValue();
  }

  public Object getProperty(Object key) {
    if (TdcProperties.RANGE.equals(key)) {
      return String.valueOf(getCurrentCommandRange());
    }
    else if (TdcProperties.ACTIVATION_COUNT.equals(key)) {
      return getActivateCountString();
    }
    else if (TdcProperties.ACTIVATION_LIMIT.equals(key)) {
      return String.valueOf(getCurrentActivateLimit());
    }
    else
      return super.getProperty(key);
  }

  public Object getLocalizedProperty(Object key) {
    if (TdcProperties.RANGE.equals(key) || TdcProperties.ACTIVATION_COUNT.equals(key) || TdcProperties.ACTIVATION_LIMIT.equals(key)) {
      return getProperty(key);
    }
    else
      return super.getLocalizedProperty(key);
  }

  protected KeyCommand[] myGetKeyCommands() {

    final KeyCommandSubMenu sub = new KeyCommandSubMenu(ACTIVATE_INDEPENDENTS, this, null);
    final KeyCommand infoKeyCommand = new KeyCommand(infoCommand, infoKey, this);
    final KeyCommand flipKeyCommand = new KeyCommand(flipCommand, flipKey, this);

    // Activation Limit reached?
    if (getActivateCount() <= 0) {
      KeyCommand c = new KeyCommand(ACTIVATION_LIMIT_REACHED, (NamedKeyStroke) null, this);
      c.setEnabled(false);
      sub.setCommands(new String[] { ACTIVATION_LIMIT_REACHED });
      keyCommands = new KeyCommand[] { flipKeyCommand, infoKeyCommand, c, sub };
      return keyCommands;
    }

    // Set up a default Key Command for nothing available
    KeyCommand c = new KeyCommand(NONE_ELIGIBLE, (NamedKeyStroke) null, this);
    c.setEnabled(false);
    sub.setCommands(new String[] { NONE_ELIGIBLE });
    keyCommands = new KeyCommand[] { flipKeyCommand, infoKeyCommand, c, sub };

    // Only available if the Leader is activated
    boolean activated = "2".equals(Decorator.getOutermost(piece).getProperty("Active_Level"));
    if (!activated) {
      return new KeyCommand[] { flipKeyCommand, infoKeyCommand };
    }

    // If this is Crete and the Leader is German, then all independents in range of activated KG Leaders
    // from the same division are also eligible to be activated.
    ArrayList<GamePiece> kgLeaders = new ArrayList<>();
    if (UnitInfo.isCreteRules() && TdcProperties.ARMY_GERMAN.equals(getProperty(TdcProperties.ARMY))) {
      for (GamePiece piece : getMap().getPieces()) {
        if (piece instanceof Stack) {
          final Stack stack = (Stack) piece;
          for (int i = 0; i < stack.getPieceCount(); i++) {
            final GamePiece p = stack.getPieceAt(i);

            if (TdcProperties.ARMY_GERMAN.equals(p.getProperty(TdcProperties.ARMY)) && getProperty(TdcProperties.DIVISION).equals(p.getProperty(TdcProperties.DIVISION))
                && TdcProperties.SUBCLASS_KGR_LEADER.equals(p.getProperty(TdcProperties.SUBCLASS))) {
              kgLeaders.add(p);
            }
          }
        }
      }
    }

    // Build a list of all valid Independents within command range
    // - Same Division
    // - Independent
    // - Not currently activated
    units.clear();
    final String myDivision = (String) piece.getProperty(TdcProperties.DIVISION);

    MapGrid grid = null;
    final Point pos = getPosition();
    if (getMap() == null) {
      return keyCommands;
    }
    final Board b = getMap().findBoard(pos);
    if (b != null) {
      grid = b.getGrid();
    }
    if (grid == null) {
      return keyCommands;
    }

    for (GamePiece piece : getMap().getPieces()) {
      if (piece instanceof Stack) {
        final Stack stack = (Stack) piece;
        for (int i = 0; i < stack.getPieceCount(); i++) {
          final GamePiece p = stack.getPieceAt(i);

          final String type = (String) p.getProperty(TdcProperties.TYPE);
          if (TdcProperties.TYPE_UNIT.equals(type)) {
            final String division = (String) p.getProperty(TdcProperties.DIVISION);
            if (myDivision.equals(division)) {
              if ("true".equals(p.getProperty(TdcProperties.IS_INDEPENDENT))) {
                if ("1".equals(p.getProperty("Active_Level"))) {
                  final int range = grid.range(pos, p.getPosition());
                  if (range <= getCurrentCommandRange()) {
                    units.add(p);
                  }
                  // Check if in range of KG units
                  else {
                    for (GamePiece kg : kgLeaders) {
                      final int kgRange = grid.range(kg.getPosition(), p.getPosition());
                      if (kgRange < getCurrentCommandRange()) {
                        units.add(p);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (units.size() == 0) {

      return keyCommands;
    }

    keyCommands = new KeyCommand[units.size() + 3];
    keyCommands[0] = flipKeyCommand;
    keyCommands[1] = infoKeyCommand;

    final String[] names = new String[units.size()];
    keyStrokes = new NamedKeyStroke[units.size()];

    for (int i = 0; i < units.size(); i++) {
      final GamePiece pp = units.get(i);
      final String name = (String) pp.getProperty(BasicPiece.BASIC_NAME);
      keyStrokes[i] = new NamedKeyStroke(name);
      keyCommands[i + 2] = new KeyCommand(name, keyStrokes[i], this);
      names[i] = name;
    }

    sub.setCommands(names);
    keyCommands[units.size() + 2] = sub;

    return keyCommands;
    // KeyCommandSubMenu keyCommandSubMenu = new
    // KeyCommandSubMenu("Activate Independents", this, this);
    // return null;
  }

  public Command myKeyEvent(KeyStroke stroke) {
    if (stroke==null) {
      return null;
    }
    Command command = null;

    // Flip?
    if (stroke.equals(flipKey)) {
      final ChangeTracker c = new ChangeTracker(this);
      setReplacement(!isReplacement());
      initialiseLeader();
      command = c.getChangeCommand();
      return command;
    }

    // Info Panel?
    if (stroke.equals(infoKey)) {
      isShowingInfo = true;
      return command;
    }

    // Look for the Ctrl D Deactivate key and clear our counter
    if (stroke.equals(KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK))) {
      boolean deactivating = "2".equals(Decorator.getOutermost(piece).getProperty("Active_Level"));
      if (deactivating) {
        final ChangeTracker c = new ChangeTracker(this);
        setActivateCount(getCurrentActivateLimit());
        activatedUnits.clear();
        command = c.getChangeCommand();
      }
      return command;
    }
    // Look for the Ctrl A Activate key
    if (stroke.equals(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK))) {
      // Are we activating?
      boolean activating = "1".equals(Decorator.getOutermost(piece).getProperty("Active_Level"));
      if (activating) {
        final ChangeTracker c = new ChangeTracker(this);
        initialiseLeader();
        command = c.getChangeCommand();
      }
      return command;
    }
    // Is it one of our KeyStrokes?
    for (int i = 0; i < keyStrokes.length; i++) {
      if (keyStrokes[i].equals(stroke)) {
        return activate(units.get(i));
      }
    }
    return command;
  }

  protected Command activate(GamePiece piece) {
    final Command c = new NullCommand();
    // adjust our count
    final boolean isBlackStripe = ("true".equals(piece.getProperty(TdcProperties.BLACK_STRIPE_IND)));
    if (!isBlackStripe) {
      final ChangeTracker tracker = new ChangeTracker(this);
      setActivateCount(getActivateCount() - 1);
      c.append(tracker.getChangeCommand());
    }

    // Report
    Command r = getChatCommand("Leader " + getProperty(BasicPiece.BASIC_NAME) + " activates independent " + piece.getProperty(BasicPiece.BASIC_NAME) + ", " + getActivateCount() + " activations remaining", true);
    r.execute();
    c.append(r);

    // Activate the independent
    activatedUnits.add((String) piece.getProperty(BasicPiece.BASIC_NAME));
    c.append(piece.keyEvent(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK)));
    return c;

  }

  protected void initialiseLeader() {
    activateCount = getCurrentActivateLimit();
    activatedUnits.clear();

    // Check for Artillery Units in Contact
    final Stack myStack = getParent();
    boolean seen = false;
    boolean done = false;
    if (myStack != null) {
      for (int i = 0; i < myStack.getPieceCount() && !done; i++) {
        final GamePiece piece = myStack.getPieceAt(i);
        if (seen) {
          final String pieceType = (String) piece.getProperty(TdcProperties.TYPE);
          if (TdcProperties.LEADER.equals(pieceType)) {
            done = true; // Another Leader, stop searching
          }
          else if (TdcProperties.TYPE_ARTILLERY_MARKER.equals(pieceType)) {
            // Found an Artillery marker on us
            final boolean isBlackStripe = "true".equals(piece.getProperty(TdcProperties.BLACK_STRIPE_IND));
            final String unitName = (String) piece.getProperty(ArtilleryMarker.MARKER_NAME);
            activatedUnits.add(unitName);
            if (!isBlackStripe) {
              activateCount--;
            }
          }
        }
        else {
          if (piece == this) {
            seen = true;
          }
        }
      }
    }
  }

  public PieceEditor getEditor() {
    return new Ed(this);
  }

  public String getDescription() {
    return "GTS Leader Unit";
  }

  public void mySetType(String type) {
    type = type.substring(ID.length());
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    activateLimit = st.nextInt(6);
    commandRange = st.nextInt(10);
    activateLimitR = st.nextInt(4);
    commandRangeR = st.nextInt(8);
  }

  public void setProperty(Object key, Object val) {
    if (Properties.SELECTED.equals(key)) {
      if (!Boolean.TRUE.equals(val)) {
        isShowingInfo = false;
        infoImage = null;
      }
      super.setProperty(key, val);
    }
    else {
      super.setProperty(key, val);
    }
  }

  public Command getChatCommand(String s, boolean useId) {
    final String id = "<" + piece.getProperty(GlobalOptions.PLAYER_ID) + ">";
    return new Chatter.DisplayText(GameModule.getGameModule().getChatter(), (useId ? "* " + id + " " : "  " + id + " ") + s);
  }

  public void buildInfoImage() {
    if (infoImage != null) {
      return;
    }

    JLabel label;
    final String unitName = (String) piece.getProperty(BasicPiece.BASIC_NAME);

    JDialog dummy = new JDialog();
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEtchedBorder());

    panel.setLayout(new MigLayout("", "[][]"));

    final Font headFont = new Font("Dialog", Font.BOLD, 12);
    final Font mainFont = new Font("Dialog", Font.BOLD, 14);

    label = new JLabel(unitName);
    label.setFont(mainFont);
    panel.add(label, "span 2,center,wrap");

    if (isReplacement()) {
      label = new JLabel("Replacement");
      label.setFont(headFont);
      panel.add(label);
      label = new JLabel("True");
      panel.add(label, "wrap");
    }

    label = new JLabel("Command Range");
    label.setFont(headFont);
    panel.add(label);
    label = new JLabel(String.valueOf(getCurrentCommandRange()));
    panel.add(label, "wrap");

    label = new JLabel("Activation Limit");
    label.setFont(headFont);
    panel.add(label);
    label = new JLabel(String.valueOf(getCurrentActivateLimit()));
    panel.add(label, "wrap");

    label = new JLabel("Activations Left");
    label.setFont(headFont);
    panel.add(label);
    label = new JLabel(getActivateCountString());
    panel.add(label, "wrap");

    label = new JLabel("Activated Units");
    label.setFont(headFont);
    panel.add(label);

    for (String u : activatedUnits) {
      label = new JLabel(u);
      panel.add(label, "wrap");
      label = new JLabel("");
      panel.add(label);
    }

    dummy.add(panel);
    dummy.pack();

    infoImage = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
    final Graphics2D imageGraphics = infoImage.createGraphics();
    panel.paint(imageGraphics);
    panel = null;
    dummy = null;

  }

  public static class Ed implements PieceEditor {

    protected IntConfigurer limitConfig;
    protected IntConfigurer rangeConfig;
    protected IntConfigurer limitConfigR;
    protected IntConfigurer rangeConfigR;
    protected Box controls;

    public Ed(TdcLeader p) {

      rangeConfig = new IntConfigurer(null, "Command Range:  ", p.commandRange);
      limitConfig = new IntConfigurer(null, "Activation Limit:  ", p.activateLimit);
      rangeConfigR = new IntConfigurer(null, "Replacement Command Range:  ", p.commandRangeR);
      limitConfigR = new IntConfigurer(null, "Replacement Activation Limit:  ", p.activateLimitR);

      controls = Box.createVerticalBox();

      controls.add(rangeConfig.getControls());
      controls.add(limitConfig.getControls());
      controls.add(rangeConfigR.getControls());
      controls.add(limitConfigR.getControls());
    }

    public Component getControls() {
      return controls;
    }

    public String getType() {
      final SequenceEncoder se = new SequenceEncoder(';');
      se.append(limitConfig.getValueString());
      se.append(rangeConfig.getValueString());
      se.append(limitConfigR.getValueString());
      se.append(rangeConfigR.getValueString());
      return ID + se.getValue();
    }

    public String getState() {
      return null;
    }

  }

  public HelpFile getHelpFile() {
    return null;
  }

//  class DummyUnit extends BasicPiece {
//
//    public DummyUnit(String name) {
//      super(BasicPiece.ID + ";;;" + name + ";");
//    }
//
//  }
}
