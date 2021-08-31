/*
 * $Id: AssaultWizard 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2006-2017 by Brent Easton
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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tdc.assault.AssaultModel;
import tdc.assault.AssaultView;
import tdc.assault.AssaultWizardThread;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import VASSAL.tools.SequenceEncoder;
import VASSAL.tools.UniqueIdManager;

public class AssaultWizard extends AbstractConfigurable implements GameComponent, CommandEncoder, UniqueIdManager.Identifyable, Wizard {

  public static final String ASSAULT_WIZARD_COMMAND = "Assaultwiz\t";
  protected static UniqueIdManager idMgr = new UniqueIdManager("AssaultWizard");
  public static final String WIZARD_OPEN = "Open";
  public static final String WIZARD_CLOSE = "Close";

  protected GamePiece source;
  protected UnitInfo sourceInfo;
  protected TdcMap map;
  protected WizardThread myThread;
  protected AssaultModel myModel;
  protected AssaultView myView;
  protected Point targetPoint;
  protected ArrayList<GamePiece> sourcePieces = new ArrayList<>();
  protected ArrayList<GamePiece> targetPieces = new ArrayList<>();
  protected String wizardID = "";
  protected JDialog myDialog;
  protected JPanel dialogContent;
  protected boolean attackNotResolved = false;

  protected WizardThread getMyThread() {
    if (myThread == null) {
      myThread = new AssaultWizardThread(this);
    }
    return myThread;
  }

  public void addTo(Buildable b) {
    idMgr.add(this);
    map = (TdcMap) b;
    getMyThread().addTo(map);
    map.setAssaultWizard(this);
    GameModule.getGameModule().getGameState().addGameComponent(this);
    GameModule.getGameModule().addCommandEncoder(this);
  }

  public void removeFrom(Buildable b) {
    idMgr.remove(this);
    GameModule.getGameModule().getGameState().removeGameComponent(this);
    GameModule.getGameModule().removeCommandEncoder(this);
    getMyThread().removeFrom(map);
    map.setAssaultWizard(null);
  }

  public Command getRestoreCommand() {
    // TODO Auto-generated method stub
    return null;
  }

  public HelpFile getHelpFile() {
    return null;
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public String[] getAttributeDescriptions() {
    return new String[0];
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[0];
  }

  public String[] getAttributeNames() {
    return new String[0];
  }

  public void setAttribute(String key, Object value) {

  }

  public String getAttributeValueString(String key) {
    return null;
  }

  public AssaultWizard() {

  }

  public void launch(GamePiece source) {
    this.source = source;
    sourceInfo = new UnitInfo(source, false);
    getMyThread().launch(source, 1, false, 0);
  }

  /*
   * User has selected a hex on the current map using the target selector. p is
   * the centre of the selected hex. Called back from WizardThread when user
   * selects a target stack
   */

  public void targetSelected(Point p, int rangeToTarget, WizardThread losThread) {

    if (rangeToTarget != 1) {
      getMyThread().setVisible(false);
      return;
    }

    setTarget(p);

    if (targetPieces.size() > 0) {
      final Command wizCmd = new AssaultOpenCommand(this, source.getId(), targetPoint);
      final Command reportCmd = getChatCommand("START ASSAULT", true);
      // reportCmd.append(getChatCommand(myModel.getAttackText() + " - " + myModel.getSmlModeString(), false));
      reportCmd.execute();
      wizCmd.append(reportCmd);
      GameModule.getGameModule().sendAndLog(wizCmd);
      attackNotResolved = true;
    }
    else {
      getMyThread().setVisible(false); // N target selected, just hide the thread
    }
  }

  public GamePiece findAssaultForceMarker(String navalPark) {
    return null;
  }

  public void setId(String id) {
    wizardID = id;
  }

  public String getId() {
    return wizardID;
  }

  public GamePiece getSource() {
    return source;
  }

  public void setSource(GamePiece source) {
    this.source = source;
    sourceInfo = new UnitInfo(source, true);
  }

  public Command getChatCommand(String s, boolean useId) {
    final String id = "<" + source.getProperty(GlobalOptions.PLAYER_ID) + ">";
    return new Chatter.DisplayText(GameModule.getGameModule().getChatter(), (useId ? "* " + id + " - " : "  " + id + " ") + s);
  }

  public String getConfigureName() {
    return "Assault Wizard";
  }

  public Command decode(String com) {
    if (com.startsWith(ASSAULT_WIZARD_COMMAND + getId())) {
      final SequenceEncoder.Decoder sd = new SequenceEncoder.Decoder(com, '\t');
      sd.nextToken();
      sd.nextToken();
      final String command = sd.nextToken("");
      if (WIZARD_OPEN.equals(command)) {
        final String pieceId = sd.nextToken("");
        final int x = sd.nextInt(0);
        final int y = sd.nextInt(0);
        return new AssaultOpenCommand(this, pieceId, new Point(x, y));
      }
      else if (WIZARD_CLOSE.equals(command)) {
        return new AssaultCloseCommand(this);
      }

    }
    return null;
  }

  public String encode(Command c) {
    if (c instanceof AssaultWizardCommand) {
      final AssaultWizardCommand com = (AssaultWizardCommand) c;
      SequenceEncoder se = new SequenceEncoder(com.wizard.getId(), '\t');
      se.append(com.command);
      if (c instanceof AssaultOpenCommand) {
        final AssaultOpenCommand woc = (AssaultOpenCommand) c;
        se.append(woc.sourceId).append(woc.target.x).append(woc.target.y);
      }
      //else if (c instanceof AssaultCloseCommand) {
        // No further parameters
      //}
      return ASSAULT_WIZARD_COMMAND + se.getValue();
    }
    return null;
  }

  // Close Dialog on game close
  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      hideDialog();
    }
  }

  public void hideDialog() {
    if (myDialog != null) {
      myDialog.setVisible(false);
      getMyThread().setVisible(false);
    }
  }

  public Point getTargetPoint() {
    return targetPoint;
  }

  public void setTarget(Point p) {
    boolean found;
    sourcePieces.clear();
    targetPieces.clear();
    targetPoint = map.snapTo(p);

    // Locate the stack of units in the target Hex and adjacent enemy units
    GamePiece[] pieces = map.getPieces();
    for (GamePiece piece : pieces) {
      // If it is a stack, see if it is in the target hex and that it contains
      // at least one enemy unit. If its position does not change when snapped,
      // then it is in an offboard box, so check the bounds of the piece
      if (piece instanceof Stack) {
        final Point pp = map.snapTo(piece.getPosition());

        if (pp.equals(piece.getPosition())) {
          final Rectangle box = piece.boundingBox();
          found = targetPoint.x >= pp.x + box.x && targetPoint.x <= pp.x + box.x + box.width && targetPoint.y >= pp.y + box.y && targetPoint.y <= pp.y + box.y + box.height;
        }
        //
        else {
          found = pp.equals(targetPoint);
        }

        if (found) {
          final Stack stack = (Stack) piece;
          for (Iterator<GamePiece> i = stack.getPiecesIterator(); i.hasNext();) {
            final GamePiece gp = i.next();
            final String type = (String) gp.getProperty(TdcProperties.TYPE);
            if (TdcRatings.isUnitType(type)) {
              if (!sourceInfo.getArmy().equals(gp.getProperty(TdcProperties.ARMY))) {
                targetPieces.add(gp);
              }
            }
          }
        }
      }
    }

    // Only show dialog if there is at least one enemy unit in the target hex
    // Find all friendly piece in stack and build the dialog
    if (targetPieces.size() > 0) {

      final Stack stack = source.getParent();
      if (stack == null) {
        sourcePieces.add(source);
      }
      else {
        for (Iterator<GamePiece> i = stack.getPiecesIterator(); i.hasNext();) {
          final GamePiece gp = i.next();
          if (TdcRatings.isUnitType((String) gp.getProperty(TdcProperties.TYPE))) {
            sourcePieces.add(gp);
          }
        }
      }

      buildDialog();
      myDialog.setVisible(true);
    }
  }

  public void buildDialog() {
    initialiseDialog();
    myModel = new AssaultModel(sourcePieces, targetPieces);
    myView = new AssaultView(myModel, this);
    dialogContent = myView.getContent();
    myDialog.add(dialogContent);
    myDialog.pack();

    positionDialog(source.getPosition());
  }

  public void initialiseDialog() {
    if (myDialog == null) {
      final Window w = SwingUtilities.getWindowAncestor(map.getView());
      if (w instanceof Dialog) {
        myDialog = new JDialog((Dialog) w);
      }
      else {
        myDialog = new JDialog((Frame) w);
      }

      myDialog.addWindowListener(new WindowListener() {
        public void windowActivated(WindowEvent arg0) {
        }

        public void windowClosed(WindowEvent arg0) {
        }

        public void windowClosing(WindowEvent arg0) {
          wizardCloses();
        }

        public void windowDeactivated(WindowEvent arg0) {
        }

        public void windowDeiconified(WindowEvent arg0) {
        }

        public void windowIconified(WindowEvent arg0) {
        }

        public void windowOpened(WindowEvent arg0) {
        }
      });
    }

    if (dialogContent != null) {
      myDialog.remove(dialogContent);
      dialogContent = null;
    }
    myDialog.setVisible(false);

  }

  protected void positionDialog(Point pos) {

    final Dimension dialogSize = myDialog.getSize();

    final Rectangle r = new Rectangle(pos.x, pos.y, dialogSize.width, dialogSize.height);
    final Rectangle viewRect = map.getView().getVisibleRect();
    Rectangle bounds = map.mapToComponent(r);
    int maxX = bounds.x + dialogSize.width;
    int maxV = viewRect.x + viewRect.width;
    if (maxX > maxV) {
      bounds.x -= (maxX - maxV);
      if (bounds.x < viewRect.x) {
        bounds.x = viewRect.x;
      }
    }
    int maxY = bounds.y + dialogSize.height;
    maxV = viewRect.y + viewRect.height;
    if (maxY > maxV) {
      bounds.y -= (maxY - maxV);
      if (bounds.y < viewRect.y) {
        bounds.y = viewRect.y;
      }
    }

    // Force the map open
    if (!map.getView().getTopLevelAncestor().isVisible()) {
      map.getView().getTopLevelAncestor().setVisible(true);
    }
    final Point cp = map.getView().getLocationOnScreen();
    int left = cp.x + bounds.x;
    int top = cp.y + bounds.y;
    if (left < 0)
      left = 0;
    if (top < 0)
      top = 0;
    myDialog.setLocation(left, top);
  }

  public void pack() {
    myDialog.pack();
  }

  // Our Dialog is closing, inform other clients
  public void wizardCloses() {
    getMyThread().setVisible(false);
        final Command closeCmd = new AssaultCloseCommand(this);
        if (attackNotResolved) {
          attackNotResolved = false;
          final Command cancelCmd = getChatCommand("ASSAULT CANCELLED", true);
          cancelCmd.execute();
          closeCmd.append(cancelCmd);
        }
        GameModule.getGameModule().sendAndLog(closeCmd);

  }

  public static abstract class AssaultWizardCommand extends Command {

    protected AssaultWizard wizard;
    protected String command;
    protected int newValue;
    protected int oldValue;

    protected AssaultWizardCommand(AssaultWizard wizard, String command) {
      this.wizard = wizard;
      this.command = command;
    }

    public int getOldValue() {
      return oldValue;
    }

    public void setOldValue(int oldValue) {
      this.oldValue = oldValue;
    }

    public int getNewValue() {
      return newValue;
    }

    public void setNewValue(int newValue) {
      this.newValue = newValue;
    }

    protected abstract void executeCommand();

    protected abstract Command myUndoCommand();

  }

  // ===== OPEN ===== //
  public static class AssaultOpenCommand extends AssaultWizardCommand {

    protected String sourceId;
    protected Point target;

    public AssaultOpenCommand(AssaultWizard wizard, String source, Point targLoc) {
      super(wizard, WIZARD_OPEN);
      this.sourceId = source;
      target = new Point(targLoc);
    }

    protected void executeCommand() {
      final GamePiece source = GameModule.getGameModule().getGameState().getPieceForId(sourceId);
      wizard.hideDialog();
      wizard.setSource(source);
      wizard.setTarget(target);
      wizard.getMyThread().setEndPoints(source.getPosition(), target, false);
      wizard.getMyThread().setVisible(true);
      wizard.getMyThread().setActive(false);
    }

    protected Command myUndoCommand() {
      return new AssaultCloseCommand(wizard);
    }

  }

  // ===== CLOSE ===== //
  public static class AssaultCloseCommand extends AssaultWizardCommand {

    public AssaultCloseCommand(AssaultWizard wizard) {
      super(wizard, WIZARD_CLOSE);
    }

    protected void executeCommand() {
      wizard.hideDialog();
    }

    protected Command myUndoCommand() {
      if (wizard == null || wizard.getSource() == null || wizard.getTargetPoint() == null) {
        return null;
      }
      return new AssaultOpenCommand(wizard, wizard.getSource().getId(), wizard.getTargetPoint());
    }

  }

}
