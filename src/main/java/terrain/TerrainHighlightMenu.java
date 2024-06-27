package terrain;

import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.ToolbarMenu;
import VASSAL.build.module.Map;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.LaunchButton;

import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.ScaleOp;
import VASSAL.tools.imageop.SourceOp;
import net.miginfocom.swing.MigLayout;
import tdc.TdcProperties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class TerrainHighlightMenu extends ToolbarMenu {

  public final static String HIGHLIGHT_GP_SUFFIX = "Highlight";
  public final static String HIGHLIGHT_STROKE_WIDTH = "highlightSrokeWidth";
  public final static String HIGHLIGHT_STROKE_TRANSPARENCY = "highlightSrokeTransparency";
  public static final String VICTORY_HEX_MARKER = "V";

  JDialog dialog;
  JPanel panel;

  public static TerrainHighlightMenu instance;

  private boolean hillVisible = true;
  private boolean opVisible = true;
  private boolean strongpointVisible = true;
  private boolean vVisible = true;


  public static boolean isTerrainVisible(String terrainName) {
    if (TdcProperties.OP.equals(terrainName)) {
      return instance.opVisible;
    }
    else if (TdcProperties.HILL.equals(terrainName)) {
      return instance.hillVisible;
    }
    else if (TdcProperties.STRONGPOINT.equals(terrainName)) {
      return instance.strongpointVisible;
    }
    else if (VICTORY_HEX_MARKER.equals(terrainName)) {
      return instance.vVisible;
    }
    return false;
  }

  @Override
  public void addTo(Buildable parent) {
    super.addTo(parent);

    instance = this;

    final Prefs prefs = GameModule.getGameModule().getPrefs();

    final BooleanConfigurer streamConfig = new BooleanConfigurer(TdcProperties.TERRAIN_STREAM + HIGHLIGHT_GP_SUFFIX, null, true);
    prefs.addOption(null, streamConfig);

    final BooleanConfigurer riverConfig = new BooleanConfigurer(TdcProperties.TERRAIN_RIVER + HIGHLIGHT_GP_SUFFIX, null, false);
    prefs.addOption(null, riverConfig);

    final BooleanConfigurer crestConfig = new BooleanConfigurer(TdcProperties.TERRAIN_CREST + HIGHLIGHT_GP_SUFFIX, null, true);
    prefs.addOption(null, crestConfig);

    final BooleanConfigurer ridgeConfig = new BooleanConfigurer(TdcProperties.TERRAIN_RIDGE + HIGHLIGHT_GP_SUFFIX, null, false);
    prefs.addOption(null, ridgeConfig);

    final StringEnumConfigurer strokeConfig = new StringEnumConfigurer(HIGHLIGHT_STROKE_WIDTH, "Terrain Highlight Stroke Width", new String[] {"1", "2", "3", "4", "5", "6", "7", "8"});
    strokeConfig.setValue("6");
    strokeConfig.addPropertyChangeListener(e -> refresh());
    prefs.addOption(TdcProperties.PREF_TAB, strokeConfig);

    final OpacityConfigurer transparencyConfig = new OpacityConfigurer(HIGHLIGHT_STROKE_TRANSPARENCY, " Terrain Highlight Transparency", 30, false);
    transparencyConfig.addPropertyChangeListener(e -> refresh());
    prefs.addOption(TdcProperties.PREF_TAB, transparencyConfig);

    final BooleanConfigurer hillConfig = new BooleanConfigurer(TdcProperties.HILL + HIGHLIGHT_GP_SUFFIX, null, hillVisible);
    hillConfig.addPropertyChangeListener(e -> hillVisible = (Boolean) e.getNewValue());
    prefs.addOption(null, hillConfig);
    hillVisible = (Boolean) prefs.getValue(TdcProperties.HILL + HIGHLIGHT_GP_SUFFIX);

    final BooleanConfigurer opConfig = new BooleanConfigurer(TdcProperties.OP + HIGHLIGHT_GP_SUFFIX, null, opVisible);
    opConfig.addPropertyChangeListener(e -> opVisible = (Boolean) e.getNewValue());
    prefs.addOption(null, opConfig);
    opVisible = (Boolean) prefs.getValue(TdcProperties.OP + HIGHLIGHT_GP_SUFFIX);

    final BooleanConfigurer spConfig = new BooleanConfigurer(TdcProperties.STRONGPOINT + HIGHLIGHT_GP_SUFFIX, null, strongpointVisible);
    spConfig.addPropertyChangeListener(e -> strongpointVisible = (Boolean) e.getNewValue());
    prefs.addOption(null, spConfig);
    strongpointVisible = (Boolean) prefs.getValue(TdcProperties.STRONGPOINT + HIGHLIGHT_GP_SUFFIX);

    final BooleanConfigurer vConfig = new BooleanConfigurer(VICTORY_HEX_MARKER + HIGHLIGHT_GP_SUFFIX, null, vVisible);
    vConfig.addPropertyChangeListener(e -> vVisible = (Boolean) e.getNewValue());
    prefs.addOption(null, vConfig);
    vVisible = (Boolean) prefs.getValue(VICTORY_HEX_MARKER + HIGHLIGHT_GP_SUFFIX);

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

    panel = new JPanel(new MigLayout("ins panel", "[]rel[]rel[]"));
    panel.setBorder(BorderFactory.createEtchedBorder());
    dialog.add(panel);
    dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    addEdgeItem(TdcProperties.TERRAIN_STREAM);
    addEdgeItem(TdcProperties.TERRAIN_RIVER);
    addEdgeItem(TdcProperties.TERRAIN_CREST);
    addEdgeItem(TdcProperties.TERRAIN_RIDGE);

    addOpItem(TdcProperties.HILL, "Hill");
    addOpItem(TdcProperties.OP, "op");
    addOpItem(TdcProperties.STRONGPOINT, "sp");
    addOpItem(VICTORY_HEX_MARKER, "marker-vp-highlight");

    dialog.pack();

  }

  protected void addOpItem(String terrainName, String iconName) {
    final Prefs prefs = GameModule.getGameModule().getPrefs();

    addCheckbox(terrainName, prefs);

    final String terrainImageName = iconName + ".png";
    final SourceOp op = Op.load(terrainImageName);
    final ScaleOp sp = Op.scale(op, 0.6);
    final JLabel label2 = new JLabel(new ImageIcon(sp.getImage()));
    panel.add(label2, "center, wrap");

  }

  private void addCheckbox(String terrainName, Prefs prefs) {
    final JCheckBox checkbox = new JCheckBox();
    final Boolean prefValue = (Boolean) prefs.getValue(terrainName + HIGHLIGHT_GP_SUFFIX);
    checkbox.setSelected(prefValue);
    checkbox.addItemListener(e -> {
      setPref(terrainName, e.getStateChange() == ItemEvent.SELECTED);
    });
    panel.add(checkbox);
    final JLabel label = new JLabel((VICTORY_HEX_MARKER.equals(terrainName) ? "Victory Hex Marker" : terrainName) + "s");
    label.setLabelFor(checkbox);
    panel.add(label);
  }

  protected void addEdgeItem(String terrainName) {
    final Prefs prefs = GameModule.getGameModule().getPrefs();

    addCheckbox(terrainName, prefs);

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
    panel.add(stroke, "center, wrap");

  }

  protected void setPref(String item, boolean state) {
    final String key = item + HIGHLIGHT_GP_SUFFIX;
    GameModule.getGameModule().getPrefs().setValue(key, state);
    refresh();
  }

  protected void refresh() {
    // Repaint the main map after any change
    final Map map = Map.getMapById("Map");
    if (map != null) {
      map.repaint();
    }
  }
}

