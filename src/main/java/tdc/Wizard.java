package tdc;

import java.awt.Point;

import VASSAL.counters.GamePiece;

public interface Wizard {
  void targetSelected(Point arrow, int range, WizardThread thread);
  GamePiece findAssaultForceMarker(String navalPark);
}
