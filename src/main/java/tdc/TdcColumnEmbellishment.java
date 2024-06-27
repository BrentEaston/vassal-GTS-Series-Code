package tdc;

import VASSAL.build.GameModule;
import VASSAL.counters.Embellishment;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Properties;
import VASSAL.tools.imageop.ScaledImagePainter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

public class TdcColumnEmbellishment extends Embellishment {

  public static final String STANDARD_IMAGE = "marker-column-semi.png";
  public static final String SMALL_IMAGE = "marker-column-sml.png";
  private Boolean lastStandardColumnType = Boolean.TRUE;

  public TdcColumnEmbellishment() {
  }

  public TdcColumnEmbellishment(String type, GamePiece d) {
    super(type, d);
  }

  @Override
  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
    final boolean drawUnder = drawUnderneathWhenSelected && Boolean.TRUE.equals(getProperty(Properties.SELECTED));

    if (!drawUnder) {
      piece.draw(g, x, y, obs, zoom);
    }

    checkPropertyLevel();

    if (followProperty && !onlyPropertyName.isEmpty()) {
      final boolean check = checkProperty(onlyPropertyName);

      if (check != ("true".equals(onlyPropertyState))) { //NON-NLS
        if (drawUnder) {
          piece.draw(g, x, y, obs, zoom);
        }
        return;
      }
    }

    if (!isActive()) {
      if (drawUnder) {
        piece.draw(g, x, y, obs, zoom);
      }
      return;
    }

    final int i = value - 1;


    final Boolean currentStandardColumnType = AttackWizard.COLUMN_LONG.equals(GameModule.getGameModule().getPrefs().getValue(AttackWizard.COLUMN_TYPE));

    if (!currentStandardColumnType.equals(lastStandardColumnType)) {
      imagePainter[0] = new ScaledImagePainter();
      imagePainter[0].setImageName(currentStandardColumnType ? STANDARD_IMAGE : SMALL_IMAGE);
      size[0] = null;
      xOff = currentStandardColumnType ? 0 : 40;
      yOff = currentStandardColumnType ? 0 : 5;
      lastStandardColumnType = currentStandardColumnType;
    }

    if (i < imagePainter.length && imagePainter[i] != null) {
      final Rectangle r = getCurrentImageBounds();
      final double myzoom = (scale == 1.0) ? zoom : zoom * scale; //BR// If we have our own personal scale factor, apply it
      imagePainter[i].draw(g, x + (int)(zoom * r.x), y + (int)(zoom * r.y), myzoom, obs);
    }

    if (drawUnder) {
      piece.draw(g, x, y, obs, zoom);
    }
  }
}
