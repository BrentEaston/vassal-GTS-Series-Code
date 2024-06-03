package terrain;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.ToolbarMenu;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.LaunchButton;
import net.miginfocom.swing.MigLayout;
import tdc.TdcProperties;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class TerrainHighlightMenu extends ToolbarMenu {

  final static String HIGHLIGHT = "Highlight";
  JDialog dialog;

  @Override
  public void addTo(Buildable parent) {
    super.addTo(parent);

    final Prefs prefs = GameModule.getGameModule().getPrefs();

    final BooleanConfigurer streamConfig = new BooleanConfigurer(TdcProperties.TERRAIN_STREAM + HIGHLIGHT, "");
    prefs.addOption(streamConfig);

    final BooleanConfigurer riverConfig = new BooleanConfigurer(TdcProperties.TERRAIN_RIVER + HIGHLIGHT, "");
    prefs.addOption(riverConfig);

    final BooleanConfigurer crestConfig = new BooleanConfigurer(TdcProperties.TERRAIN_CREST + HIGHLIGHT, "");
    prefs.addOption(crestConfig);

    final BooleanConfigurer ridgeConfig = new BooleanConfigurer(TdcProperties.TERRAIN_RIDGE + HIGHLIGHT, "");
    prefs.addOption(ridgeConfig);

  }

  @Override
  public void launch() {
    final LaunchButton lb = getLaunchButton();
    if (lb.isShowing()) {
      final Point pos = getLaunchButton().getLocation();
      pos.y += 40;
      dialog.setLocation(pos.x, pos.y);
      dialog.setVisible(true);
    }
  }

  @Override
  protected void buildMenu() {

    dialog = new JDialog(GameModule.getGameModule().getPlayerWindow());
    dialog.setTitle("Terrain Highlighting");
    dialog.setLayout(new MigLayout("", "[]rel[]"));
    dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    dialog.add(new JLabel("Changes require restart"), "span 2, wrap");

    addItem(TdcProperties.TERRAIN_STREAM);
    addItem(TdcProperties.TERRAIN_RIVER);
    addItem(TdcProperties.TERRAIN_CREST);
    addItem(TdcProperties.TERRAIN_RIDGE);

    dialog.pack();

  }

  protected void addItem(String terrainName) {
    final Prefs prefs = GameModule.getGameModule().getPrefs();

    final JCheckBox checkbox = new JCheckBox();
    checkbox.setSelected("true".equals(prefs.getStoredValue(terrainName + "Highlight")));
    checkbox.addItemListener(e -> {
      setPref(terrainName, e.getStateChange() == ItemEvent.SELECTED);
    });
    dialog.add(checkbox);
    final JLabel label = new JLabel("Highlight " + terrainName + "s");
    label.setLabelFor(checkbox);
    dialog.add(label);

    final BufferedImage image = new BufferedImage(30, 10, BufferedImage.TYPE_4BYTE_ABGR);
    final Color color = (Color) prefs.getValue(terrainName + "Color");
    final Graphics2D g2 = (Graphics2D) image.getGraphics();
    g2.setColor(color);
    g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    final GeneralPath path = new GeneralPath();
    path.moveTo(5.0, 5.0);
    path.lineTo(25.0, 5.0);
    g2.draw(path);
    final JLabel stroke = new JLabel(new ImageIcon(image));
    dialog.add(stroke, "wrap");

  }

  protected void setPref(String item, boolean state) {
    final String key = item + HIGHLIGHT;
    GameModule.getGameModule().getPrefs().setValue(key, state);
  }
}

