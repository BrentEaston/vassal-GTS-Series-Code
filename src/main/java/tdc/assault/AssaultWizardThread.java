package tdc.assault;

import java.awt.Point;

import tdc.Wizard;
import tdc.WizardThread;
import VASSAL.build.module.Map;

public class AssaultWizardThread extends WizardThread {

  public AssaultWizardThread(Wizard w) {
    super(w);
  }

  public void draw(java.awt.Graphics g, Map m) {
    if (!visible)
      return;

    int r = calculateRange();
    g.setColor(getColor(r));
    final Point mapAnchor = map.mapToComponent(anchor);
    final Point mapArrow = map.mapToComponent(arrow);

    // Draw LOS
    g.drawLine(mapAnchor.x, mapAnchor.y, mapArrow.x, mapArrow.y);

    // Draw Target
    imagePainter.draw(g, mapArrow.x - 16, mapArrow.y - 16, 1.0d, m.getView());

    // Draw Range
    if (drawRange) {
      drawRange(g, r, getColor(r));
    }

    lastAnchor = mapAnchor;
    lastArrow = mapArrow;
  }
}
