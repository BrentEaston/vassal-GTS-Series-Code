/*
 * $Id: Info.java 9184 2015-03-17 21:08:43Z uckelman $
 *
 * Copyright (c) 2003 by Rodney Kinney
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

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/**
 * Class for storing release-related information
 */
public final class Info {

  /** This class should not be instantiated */
  private Info() { }

  public static double getSystemScaling() {
    return  GraphicsEnvironment.isHeadless() ? 1.0 :
      GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getDefaultScreenDevice()
      .getDefaultConfiguration()
      .getDefaultTransform()
      .getScaleX();
  }

  public static final String MINIMUM_VASSAL_VERSION = "3.4.0";

  public static void checkVassalVersion() {
    if (Info.comparePrimaryVersions(Info.MINIMUM_VASSAL_VERSION, VASSAL.Info.getVersion()) > 0) {
      JOptionPane.showMessageDialog(null,
        "This module requires VASSAL version " + Info.MINIMUM_VASSAL_VERSION + " or later to be installed.\nPlease update your version of VASSAL before running this module.",
        "Incompatible VASSAL Version",
        JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }

  public static int comparePrimaryVersions(String v0, String v1) {
    final String ver0 = v0.indexOf('-') >= 0 ? v0.substring(0, v0.indexOf('-')) : v0;
    final String ver1 = v1.indexOf('-') >= 0 ? v1.substring(0, v1.indexOf('-')) : v1;
    return compareVersions(ver0, ver1);
  }

  public static int compareVersions(String v0, String v1) {
    final ComparableVersion comparableVersion0 = new ComparableVersion(v0);
    final ComparableVersion comparableVersion1 = new ComparableVersion(v1);
    return comparableVersion0.compareTo(comparableVersion1);
  }
}
