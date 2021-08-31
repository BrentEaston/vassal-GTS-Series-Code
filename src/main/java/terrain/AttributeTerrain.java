/*
 * $Id: AttributeTerrain.java 945 2006-07-09 12:42:41 +0000 (Sun, 09 Jul 2006) swampwallaby $
 *
 * Copyright (c) 2000-2008 by Brent Easton
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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import VASSAL.build.AutoConfigurable;
import VASSAL.configure.Configurer;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.configure.StringEnum;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.configure.VisibilityCondition;

public class AttributeTerrain extends MapTerrain {

  public static final String ATTRIBUTE_TYPE = "type";
  public static final String LIST = "list";
  public static final String DEFAULT = "default";

  public static final String TYPE_BOOLEAN = "true or false";
  public static final String TYPE_LIST = "list of values";
  public static final String TYPE_STRING = "any value";

  public static final String[] TYPES = new String[] { TYPE_BOOLEAN, TYPE_LIST,
      TYPE_STRING };

  protected String attributeType = TYPE_BOOLEAN;
  protected String[] itemList = new String[0];
  protected PropertyChangeListener changeListener;
  protected AttributeSetter config;
  protected String defaultValue = "";

  public AttributeTerrain() {
    super();
  }

  public static String getConfigureTypeName() {
    return "Tag Terrain";
  }

  public String[] getAttributeNames() {
    return new String[] { NAME, COLOR, ATTRIBUTE_TYPE, LIST, DEFAULT, BLOCKING, POSSIBLE_BLOCKING};
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Name:  ", "Display Color:  ", "Tag Type:  ",
        "List of Values:  ", "Value when no Tag defined:  ", "Blocking Terrain?", "Blocking Terrain only if LOS crosses graphic?" };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[] { String.class, Color.class, TypeConfig.class,
        String[].class, String.class, Boolean.class, Boolean.class };
  }

  public static class TypeConfig extends StringEnum {
    public String[] getValidValues(AutoConfigurable target) {
      return TYPES;
    }
  }

  public void setAttribute(String key, Object value) {
    if (ATTRIBUTE_TYPE.equals(key)) {
      attributeType = (String) value;
    }
    else if (LIST.equals(key)) {
      if (value instanceof String) {
        value = StringArrayConfigurer.stringToArray((String) value);
      }
      itemList = (String[]) value;
    }
    else if (DEFAULT.equals(key)) {
      defaultValue = (String) value;
    }
    else {
      super.setAttribute(key, value);
    }
  }

  public String getAttributeValueString(String key) {
    if (ATTRIBUTE_TYPE.equals(key)) {
      return attributeType;
    }
    else if (LIST.equals(key)) {
      return StringArrayConfigurer.arrayToString(itemList);
    }
    else if (DEFAULT.equals(key)) {
      return defaultValue;
    }
    else {
      return super.getAttributeValueString(key);
    }
  }

  public VisibilityCondition getAttributeVisibility(String key) {
    if (LIST.equals(key)) {
      return new VisibilityCondition() {
        public boolean shouldBeVisible() {
          return TYPE_LIST.equals(attributeType);
        }
      };
    }
    else
      return null;
  }

  public String[] getItemList() {
    return itemList;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setPropertyChangeListener(PropertyChangeListener listener) {
    changeListener = listener;
  }

  public PropertyChangeListener getPropertyChangeListener() {
    return changeListener;
  }

  public AttributeSetter getAttributeConfigurer() {
    if (config == null) {
      if (TYPE_BOOLEAN.equals(attributeType)) {
        config = new BooleanAttributeSetter(this);
      }
      else if (TYPE_LIST.equals(attributeType)) {
        config = new ListAttributeSetter(this);
      }
      else if (TYPE_STRING.equals(attributeType)) {
        config = new StringAttributeSetter(this);
      }
    }
    return config;
  }

  /**
   * Classes to set the value of a specific attribute accross one or more hexes.
   */
  public static abstract class AttributeSetter extends JPanel implements
      PropertyChangeListener {

    protected static final String DIFFERENT = "<different>";
    protected static final String NO_VALUE = "";
    protected static final String TRUE = "true";
    protected static final String FALSE = "false";

    private static final long serialVersionUID = 1L;
    protected AttributeTerrain myTerrain;
    protected Configurer myConfigurer;
    protected boolean dirty;
    protected JLabel label;

    public AttributeSetter(AttributeTerrain at) {
      super();
      myTerrain = at;
      label = new JLabel(at.getConfigureName(), BasicTerrainDefinitions
          .getTerrainIcon(at.getColor()), JLabel.LEADING);
      //add(label);
    }

    public JLabel getLabel() {
      return label;
    }
    
    public void propertyChange(PropertyChangeEvent e) {
      dirty = true;
      myTerrain.getPropertyChangeListener().propertyChange(null);
    }

    public String getName() {
      return myTerrain.getTerrainName();
    }

    public String getValue() {
      return myConfigurer.getValueString();
    }

    public void setValue(String value) {
      myConfigurer.setValue(value);
    }

    public boolean isDirty() {
      return dirty;
    }

    public AttributeTerrain getTerrain() {
      return myTerrain;
    }

    public abstract void setDifferent();

    public boolean isDifferent() {
      return DIFFERENT.equals(myConfigurer.getValue());
    }
    
    public boolean isBlank() {
      return myConfigurer.getValue() == null || myConfigurer.getValueString().length() == 0;
    }
  }

  public static class BooleanAttributeSetter extends AttributeSetter {

    private static final long serialVersionUID = 1L;

    public BooleanAttributeSetter(AttributeTerrain at) {
      super(at);
      myConfigurer = new StringEnumConfigurer("", "", new String[] { TRUE,
          FALSE, NO_VALUE });
      myConfigurer.setValue(NO_VALUE);
      myConfigurer.addPropertyChangeListener(this);
      add(myConfigurer.getControls());
    }

    public void setDifferent() {
      if (!isDifferent()) {
        ((StringEnumConfigurer) myConfigurer).setValidValues(new String[] {
            DIFFERENT, TRUE, FALSE, NO_VALUE });
      }
      myConfigurer.setValue(DIFFERENT);
    }
  }

  public static class ListAttributeSetter extends AttributeSetter {

    private static final long serialVersionUID = 1L;

    public ListAttributeSetter(AttributeTerrain at) {
      super(at);
      final String[] items = new String[at.getItemList().length + 1];
      for (int i = 0; i < at.getItemList().length; i++) {
        items[i] = at.getItemList()[i];
      }
      items[items.length - 1] = NO_VALUE;
      myConfigurer = new StringEnumConfigurer("", "", items);
      myConfigurer.setValue(NO_VALUE);
      myConfigurer.addPropertyChangeListener(this);
      add(myConfigurer.getControls());
    }

    public void setDifferent() {
      if (!isDifferent()) {
        final String[] items = new String[myTerrain.getItemList().length + 2];
        items[0] = DIFFERENT;
        for (int i = 0; i < myTerrain.getItemList().length; i++) {
          items[i + 1] = myTerrain.getItemList()[i];
        }
        items[items.length - 1] = NO_VALUE;
        ((StringEnumConfigurer) myConfigurer).setValidValues(items);
      }
      myConfigurer.setValue(DIFFERENT);
    }
  }

  public static class StringAttributeSetter extends AttributeSetter {

    private static final long serialVersionUID = 1L;

    public StringAttributeSetter(AttributeTerrain at) {
      super(at);
      myConfigurer = new StringConfigurer("", "", NO_VALUE);
      myConfigurer.addPropertyChangeListener(this);
      add(myConfigurer.getControls());
    }

    public void setDifferent() {
      myConfigurer.setValue(DIFFERENT);
    }
  }
}
