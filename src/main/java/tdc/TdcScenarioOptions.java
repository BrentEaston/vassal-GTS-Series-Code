/*
 * $Id: TdcScenarioOptions.java 963 2006-08-25 12:48:03Z swampwallaby $
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

import static VASSAL.configure.ComponentLayout.DEFAULT_COMPONENT_COLUMN_CONSTRAINTS;
import static VASSAL.configure.ComponentLayout.DEFAULT_COMPONENT_LAYOUT_CONSTRAINTS;

import VASSAL.configure.ComponentConfigPanel;
import VASSAL.counters.TraitLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.module.Map;
import VASSAL.build.module.properties.MutableProperty;
import VASSAL.configure.BooleanConfigurer;
import VASSAL.configure.Configurer;
import VASSAL.configure.StringEnumConfigurer;
import VASSAL.counters.GamePiece;
import VASSAL.counters.GlobalCommand;
import VASSAL.counters.PieceFilter;
import VASSAL.tools.RecursionLimiter.Loopable;
import net.miginfocom.swing.MigLayout;

public class TdcScenarioOptions extends Widget {

  public static final String NAME = "optionsName";
  protected JPanel panel;
  protected BooleanOption aicAllied;
  protected BooleanOption aicGerman;
  protected BooleanOption aic505;
  protected BooleanOption commandBreakdown;
  protected AttachOption attach_CG_82AB;
  protected AttachOption attach_GdsInd_43rd;
  protected AttachOption attach_82ABArt_Gds;
  protected AttachOption attach_FrInd_Hoh;
  protected AttachOption attach_Bruhn_Hoh;
  protected AttachOption attach_Harder_Spindler;
  protected AttachOption attach_Krafft_Spindler;
  protected AttachOption attach_vonAllwoerden_Spindler;
  protected AttachOption attach_Henke_Frundsberg;
  protected AttachOption attach_Knaust_Frundsberg;
  protected AttachOption attach_Reinhold_Frundsberg;
  protected AttachOption attach_Euling_Frundsberg;
  protected ChillBooleanOption chill_reorg;

  protected BooleanOption artilleryScatterRule;

  protected BooleanOption telephoneCommand736;
  protected BooleanOption add10toSwordNavalRange;
  protected AttachOption attach_22arm;

  protected BooleanOption creteDiv2nz;
  protected BooleanOption creteDivcrefor;
  protected BooleanOption creteDiv5geb;
  protected BooleanOption creteDiv7fl;
  protected BooleanOption creteDiv14th;
  protected BooleanOption creteDiv19th;
  protected BooleanOption creteDivfjr1;
  protected BooleanOption creteDivfjr2;
  
  protected StringEnumOption subRuleSet;
  
  public TdcScenarioOptions() {
    super();
  }

  public Component getComponent() {

    if (panel == null) {

      panel = new JPanel(new BorderLayout());

      final JPanel optionBox = new JPanel(new MigLayout("wrap 1", "[fill]", "[][][][][]push"));
      optionBox.setBorder(BorderFactory.createEtchedBorder());
      optionBox.add(new BoldLabel("Scenario Options"), "growx,pushx");

      final ComponentPanel generalBox = new ComponentPanel();

      generalBox.addHeading("General");

      aicAllied = new BooleanOption("AIC-Allied", "Allied units always in command");
      generalBox.add(aicAllied);

      aicGerman = new BooleanOption("AIC-German", "Axis units always in command");
      generalBox.add(aicGerman);

      optionBox.add(generalBox, "growx,push");

      if (UnitInfo.isCreteRules()) {
        
        final ComponentPanel creteBox = new ComponentPanel();

        creteBox.addHeading("Crete");

        creteDiv2nz = new BooleanOption("Used-2nz", "2nd NZ division card used in scenario");
        creteBox.add(creteDiv2nz);

        creteDivcrefor = new BooleanOption("Used-crefor", "CREFOR division card used in scenario");
        creteBox.add(creteDivcrefor);
        
        creteDiv14th = new BooleanOption("Used-14th", "14th Brigade division card used in scenario");
        creteBox.add(creteDiv14th);
        
        creteDiv19th = new BooleanOption("Used-19th", "19th Brigade division card used in scenario");
        creteBox.add(creteDiv19th);

        creteDiv5geb = new BooleanOption("Used-5geb", "5th Gebirgs division card used in scenario");
        creteBox.add(creteDiv5geb);
        
        creteDiv7fl = new BooleanOption("Used-7fl", "7th Flieger division card used in scenario");
        creteBox.add(creteDiv7fl);
        
        creteDivfjr1 = new BooleanOption("Used-fjr1", "FJR 1 Regiment division card used in scenario");
        creteBox.add(creteDivfjr1);
        
        creteDivfjr2 = new BooleanOption("Used-fjr2", "FJR 2 Regiment division card used in scenario");
        creteBox.add(creteDivfjr2);

        artilleryScatterRule = new BooleanOption("ArtilleryScatterRule", "Report Artillery Scatter Direction on roll of 9");
        creteBox.add(artilleryScatterRule);

        optionBox.add(creteBox, "growx,push");
      }

      if (UnitInfo.isTgdRules()) {

        final ComponentPanel ddBox = new ComponentPanel();
        ddBox.addHeading("The Greatest Day");
        telephoneCommand736 = new BooleanOption("736TelCom", "736 Rgt Command depends only on Phone Lines?");
        ddBox.add(telephoneCommand736);

        attach_22arm = new AttachOption("Attach-22Arm", "22 Arm Brigade are attached to division: ", new String[] { TdcProperties.DIVISION_7TH, TdcProperties.DIVISION_50TH }, "7th-v-50th");
        ddBox.add(attach_22arm);

        add10toSwordNavalRange = new BooleanOption(TdcProperties.ADD_10_TO_SWORD_NAVAL_RANGE, "+10 hexes to Sword Naval Range?");
        ddBox.add(add10toSwordNavalRange);

        
        subRuleSet = new StringEnumOption (TdcProperties.SUB_RULESET, "The Greatest Day Rule Subset (1=GJS, 2=Utah, 3=Omaha, 4=Combined): ", new String[] {TdcProperties.RULES_GJS, TdcProperties.RULES_UTAH, TdcProperties.RULES_OMAHA, TdcProperties.RULES_COMBINED});
        ddBox.add(subRuleSet);

        artilleryScatterRule = new BooleanOption("ArtilleryScatterRule", "Report Artillery Scatter Direction on roll of 9");
        ddBox.add(artilleryScatterRule);

        optionBox.add(ddBox, "growx,push");

      }

      if (UnitInfo.isTdcRules() || UnitInfo.isAmdRules()) {
        final ComponentPanel tdcBox = new ComponentPanel();
        tdcBox.addHeading("The Devils Cauldron");

        aic505 = new BooleanOption("AIC-505 PIR", "505 PIR is always in command");
        tdcBox.add(aic505);

        commandBreakdown = new BooleanOption("Command-Breakdown", "Command Breakdown has occured (Scenario AD#1)");
        tdcBox.add(commandBreakdown);

        attach_CG_82AB = new AttachOption("Attach-CG", "Coldstream Guards are attached to division ", new String[] { TdcProperties.DIVISION_GDS, TdcProperties.DIVISION_82AB }, "Gds-v-82AB");
        tdcBox.add(attach_CG_82AB);

        attach_GdsInd_43rd = new AttachOption("Attach-GdsInd", "Guards Independent are attached to division ", new String[] { TdcProperties.DIVISION_GDS, TdcProperties.DIVISION_43RD }, "Gds-v-43rd");
        tdcBox.add(attach_GdsInd_43rd);

        attach_82ABArt_Gds = new AttachOption("Attach-82ABArt", "82AB Artillery are attached to division ", new String[] { TdcProperties.DIVISION_82AB, TdcProperties.DIVISION_GDS }, "82AB-v-Gds");
        tdcBox.add(attach_82ABArt_Gds);

        attach_FrInd_Hoh = new AttachOption("Attach-FrInd", "Frundsberg Independents are attached to division ", new String[] { TdcProperties.DIVISION_FR, TdcProperties.DIVISION_HOH }, "Fr-v-Hoh");
        tdcBox.add(attach_FrInd_Hoh);

        attach_Bruhn_Hoh = new AttachOption("Attach-Bruhn", "KG Bruhn are attached to division ", new String[] { TdcProperties.DIVISION_VT, TdcProperties.DIVISION_HOH }, "VT-v-Hoh");
        tdcBox.add(attach_Bruhn_Hoh);

        if ( UnitInfo.isAmdRules()) {
          artilleryScatterRule = new BooleanOption("ArtilleryScatterRule", "Report Artillery Scatter Direction on roll of 9");
          tdcBox.add(artilleryScatterRule);
        }

        optionBox.add(tdcBox, "growx,push");

        final ComponentPanel germanReconfigBox = new ComponentPanel();

        attach_Harder_Spindler = new AttachOption("Attach-Harder-Spindler", "KG Harder are attached to Formation ", new String[] { TdcProperties.FORMATION_HARDER, TdcProperties.FORMATION_SPINDLER }, "S4.5.2-Harder");

        attach_Krafft_Spindler = new AttachOption("Attach-Krafft-Spindler", "KG Krafft are attached to Formation ", new String[] { TdcProperties.FORMATION_KRAFFT, TdcProperties.FORMATION_SPINDLER }, "S4.5.2-Krafft");

        attach_vonAllwoerden_Spindler = new AttachOption("Attach-vonAllwoerden-Spindler", "KG von Allwoerden are attached to Formation ", new String[] { TdcProperties.FORMATION_VONALLWOERDEN, TdcProperties.FORMATION_SPINDLER },
            "S4.5.2-vonAllwoerden");

        attach_Henke_Frundsberg = new AttachOption("Attach-Henke-Frundsberg", "KG Henke are attached to Formation ", new String[] { TdcProperties.FORMATION_HENKE, TdcProperties.FORMATION_FRUNDSBERG }, "S4.5.3-Henke");

        attach_Knaust_Frundsberg = new AttachOption("Attach-Knaust-Frundsberg", "KG Knaust are attached to Formation ", new String[] { TdcProperties.FORMATION_KNAUST, TdcProperties.FORMATION_FRUNDSBERG }, "S4.5.3-Knaust");

        attach_Reinhold_Frundsberg = new AttachOption("Attach-Reinhold-Frundsberg", "KG Reinhold are attached to Formation ", new String[] { TdcProperties.FORMATION_REINHOLD, TdcProperties.FORMATION_FRUNDSBERG }, "S4.5.3-Reinhold");

        attach_Euling_Frundsberg = new AttachOption("Attach-Euling-Frundsberg", "KG Euling are attached to Formation ", new String[] { TdcProperties.FORMATION_EULING, TdcProperties.FORMATION_FRUNDSBERG }, "S4.5.3-Euling");

        chill_reorg = new ChillBooleanOption("Eindhoven-Chill", "Eindhoven R.C. has reorganised to KG Chill");

        germanReconfigBox.addHeading("Where Eagles Dare");

        germanReconfigBox.add(attach_Harder_Spindler);
        germanReconfigBox.add(attach_Krafft_Spindler);
        germanReconfigBox.add(attach_vonAllwoerden_Spindler);
        germanReconfigBox.add(new JLabel(" "), "wrap");

        germanReconfigBox.add(attach_Henke_Frundsberg);
        germanReconfigBox.add(attach_Knaust_Frundsberg);
        germanReconfigBox.add(attach_Reinhold_Frundsberg);
        germanReconfigBox.add(attach_Euling_Frundsberg);
        germanReconfigBox.add(new JLabel(" "), "wrap");

        germanReconfigBox.add(chill_reorg);

        optionBox.add(germanReconfigBox, "growx,push");

      }

      panel.add(optionBox, BorderLayout.CENTER);

    }

    return panel;
  }

  public String[] getAttributeDescriptions() {
    return new String[] { "Name:  " };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class[] { String.class };
  }

  public String[] getAttributeNames() {
    return new String[] { NAME };
  }

  public String getAttributeValueString(String key) {
    if (NAME.equals(key)) {
      return getConfigureName();
    }
    return null;
  }

  public void setAttribute(String key, Object value) {
    if (NAME.equals(key)) {
      setConfigureName((String) value);
    }
  }

  public class BoldLabel extends JLabel {
    public BoldLabel(String label) {
      super(label);
      setFont(getFont().deriveFont(Font.BOLD));
    }
  }
  public class ComponentPanel extends ComponentConfigPanel {
    public ComponentPanel() {
      super();
      setLayout(new MigLayout(DEFAULT_COMPONENT_LAYOUT_CONSTRAINTS, DEFAULT_COMPONENT_COLUMN_CONSTRAINTS));
      setBorder(BorderFactory.createEtchedBorder());
    }

    public void add(BasicOption option) {
      super.add(new JLabel(option.getPrompt()), option.getConfigurer());
    }

    public void addHeading(String heading) {
      add(new BoldLabel(heading), "growx,pushx,wrap");
    }
  }

  public abstract static class BasicOption {

    protected String globalPropertyName;
    protected MutableProperty globalProperty;
    protected Configurer config;
    protected PropertyChangeListener localListener;
    protected String prompt;

    public BasicOption(String propertyName, String prompt) {
      globalPropertyName = propertyName;
      this.prompt = prompt;
      globalProperty = GameModule.getGameModule().getMutableProperty(globalPropertyName);
    }

    protected Configurer getConfigurer() {
      if (config == null) {
        getControls();
      }
      return config;
    }

    protected String getPrompt() {
      return prompt;
    }

    protected abstract Configurer createConfigurer();

    protected abstract PropertyChangeListener createGlobalListener();

    protected PropertyChangeListener createLocalListener() {
      return evt ->  GameModule.getGameModule().sendAndLog(globalProperty.setPropertyValue(config.getValueString()));
//FIXME  Add Send and Log message when GTS Options are chanvg
    }

    protected PropertyChangeListener getLocalListener() {
      return localListener;
    }

    public Component getControls() {
      if (config == null) {
        buildControls();
      }
      return config.getControls();
    }

    protected void buildControls() {
      config = createConfigurer();
      localListener = createLocalListener();
      config.addPropertyChangeListener(localListener);
      globalProperty.addMutablePropertyChangeListener(createGlobalListener());
    }

  }

  /**
   * A checkbox option
   */
  public static class BooleanOption extends BasicOption {

    public BooleanOption(String propertyName, String prompt) {
      super(propertyName, prompt);
    }

    protected Configurer createConfigurer() {
      return new BooleanConfigurer("", "", "true".equals(globalProperty.getPropertyValue()));
    }

    protected PropertyChangeListener createGlobalListener() {
      return evt -> {
        config.removePropertyChangeListener(getLocalListener());
        config.setValue(Boolean.valueOf((String) evt.getNewValue()));
        config.addPropertyChangeListener(getLocalListener());
      };
    }

  }

  public static class ChillBooleanOption extends BooleanOption {

    public ChillBooleanOption(String propertyName, String prompt) {
      super(propertyName, prompt);
    }

    protected Configurer createConfigurer() {
      return new BooleanConfigurer("", "", "2".equals(globalProperty.getPropertyValue()));
    }

    protected PropertyChangeListener createGlobalListener() {
      return evt -> {
        final String newValue = (String) evt.getNewValue();
        config.removePropertyChangeListener(getLocalListener());
        config.setValue((newValue.equals("2") || newValue.equals("true")) ? "true" : "false");
        config.addPropertyChangeListener(getLocalListener());
      };
    }

    protected PropertyChangeListener createLocalListener() {
      return evt -> {
        final String newValue = config.getValueString();
        globalProperty.setPropertyValue((newValue.equals("2") || newValue.equals("true")) ? "2" : "1");
      };
    }

  }

  /**
   * A list of values option
   */

  public static class StringEnumOption extends BasicOption {

    protected String[] options;

    public StringEnumOption(String propertyName, String prompt, String[] options) {
      super(propertyName, prompt);
      this.options = options;
    }

    protected PropertyChangeListener createGlobalListener() {
      return evt -> {
        config.removePropertyChangeListener(getLocalListener());
        config.setValue((String) evt.getNewValue());
        config.addPropertyChangeListener(getLocalListener());
      };
    }

    protected Configurer createConfigurer() {
      StringEnumConfigurer c = new StringEnumConfigurer("", "", options);
      c.setValue(globalProperty.getPropertyValue());
      return c;
    }

  }

  /**
   * A class to handle the updating of options for units that
   * can be attached to different Divisions. Whenever the option
   * is changed, all units whose 'Attach' property match the supplied
   * value must be notified to change the Division held as a Dynamic Property.
   * 
   */
  public static class AttachOption extends StringEnumOption {

    protected String attachMarkerValue;

    public AttachOption(String propertyName, String prompt, String[] options, String markerValue) {
      this(propertyName, prompt, options);
      this.attachMarkerValue = markerValue;
    }

    public AttachOption(String propertyName, String prompt, String[] options) {
      super(propertyName, prompt, options);
    }

    protected PropertyChangeListener createGlobalListener() {
      return evt -> {
        String newValue = (String) evt.getNewValue();
        config.removePropertyChangeListener(getLocalListener());
        config.setValue(newValue);
        doGlobalKey(newValue);
        config.addPropertyChangeListener(getLocalListener());
      };
    }

    protected void doGlobalKey(String newValue) {
      int index = 0;
      for (int i = 0; i < options.length; i++) {
        if (newValue.equals(options[i])) {
          index = i;
        }
      }
      KeyStroke stroke;
      if (index == 0) {
        stroke = KeyStroke.getKeyStroke('1', InputEvent.ALT_MASK);
      }
      else {
        stroke = KeyStroke.getKeyStroke('2', InputEvent.ALT_MASK);
      }

      GlobalCommand gc = new GlobalCommand(new Loopable() {
        public String getComponentName() {
          return "TdcScenarioOptions";
        }

        public String getComponentTypeName() {
          return "TdcScenarioOptions";
        }
      });
      gc.setKeyStroke(stroke);
      PieceFilter filter = new AttachFilter(attachMarkerValue);
      for (Map m : Map.getMapList()) {
        gc.apply(m, filter);
      }

    }

  }

  /**
   * PieceFilter selecting counters whose Attach property matches
   * the supplied value
   */
  public static class AttachFilter implements PieceFilter {

    protected String value;

    public AttachFilter(String markerValue) {
      value = markerValue;
    }

    public boolean accept(GamePiece piece) {
      return value.equals(piece.getProperty("Attach"));
    }

  }

}