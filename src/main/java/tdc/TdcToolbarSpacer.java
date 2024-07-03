package tdc;

import VASSAL.build.AbstractFolder;
import VASSAL.build.AbstractToolbarItem;
import VASSAL.build.Buildable;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.i18n.Resources;
import VASSAL.tools.ToolBarComponent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import java.awt.image.BufferedImage;

public class TdcToolbarSpacer extends AbstractToolbarItem {

  private JToolBar toolbar;
  private JLabel spacer;

  public TdcToolbarSpacer() {
    this.setLaunchButton(this.makeLaunchButton("Spacer", Resources.getString("General.menu"), "" , null));
    this.launch = this.getLaunchButton();
  }

  @Override
  public void addTo(Buildable parent) {
    super.addTo(parent);
    if (parent instanceof AbstractFolder) {
      parent = ((AbstractFolder)parent).getNonFolderAncestor();
    }

    if (parent instanceof ToolBarComponent) {
      this.toolbar = ((ToolBarComponent)parent).getToolBar();
    }

    if (this.toolbar != null) {
      this.toolbar.add(this.getLaunchButton());
    }
    BufferedImage b = new BufferedImage(8, 32, BufferedImage.TYPE_4BYTE_ABGR);
    spacer = new JLabel(new ImageIcon(b));
    toolbar.add(spacer);
  }

  @Override
  public void removeFrom(Buildable parent) {
    this.toolbar.remove(spacer);
  }

  @Override
  public HelpFile getHelpFile() {
    return null;
  }

  @Override
  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  @Override
  public boolean isMandatory() {
    return false;
  }

  @Override
  public boolean isMovable() {
    return true;
  }

  @Override
  public boolean isUnique() {
    return false;
  }

  @Override
  public String getComponentTypeName() {
    return "Toolbar Spacer";
  }

  @Override
  public String getComponentName() {
    return super.getComponentName();
  }
}
