package tdc.assault;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;
import tdc.AssaultWizard;

public class AssaultView {

  protected static final Font SMALL_FONT = new Font ("Dialog", Font.PLAIN, 12);
  protected static final Font MEDIUM_FONT = new Font ("Dialog", Font.PLAIN, 14);
  protected static final Font MEDIUM_BOLD_FONT = new Font ("Dialog", Font.BOLD, 14);
  protected static final Font LARGE_FONT = new Font ("Dialog", Font.PLAIN, 16);
  protected static final Font LARGE_BOLD_FONT = new Font ("Dialog", Font.BOLD, 16);
  
  protected static final Color GRID_COLOR = Color.DARK_GRAY;
  protected static final Border TOP_LEFT_BORDER = BorderFactory.createMatteBorder(1, 1, 0, 0, GRID_COLOR);
  protected static final Border TOP_RIGHT_BORDER = BorderFactory.createMatteBorder(1, 1, 0, 1, GRID_COLOR);
  protected static final Border BOT_LEFT_BORDER = BorderFactory.createMatteBorder(1, 1, 1, 0, GRID_COLOR);
  protected static final Border BOT_RIGHT_BORDER = BorderFactory.createMatteBorder(1, 1, 1, 1, GRID_COLOR);
  
  protected AssaultModel myModel;
  protected AssaultWizard myWizard;
  protected JPanel mainPanel;
  protected JPanel stepPanel;
  protected JPanel descPanel;
  protected JLabel stepNumber;
  protected JLabel stepDescription;
  protected JTextArea stepDetails; 
  protected JPanel detailPanel;
  protected JPanel continuePanel;
  protected JButton continueButton;
    

  public AssaultView(AssaultModel model, AssaultWizard wizard) {
    myModel = model;
    myWizard = wizard;
    myModel.setChangeListener(this);
  }

  public JPanel getContent() {
    if (mainPanel == null) {
      buildContent();
    }
    return mainPanel;
  }

  public void stepChanged() {
    stepNumber.setText("Step "+myModel.getStep());
    stepDescription.setText(myModel.getStepDescription());
    stepDetails.setText(myModel.getStepDetails());
    detailPanel.removeAll();
    detailPanel.add(getDetailPanelForStep(myModel.getStep()));
  }
  
  protected void buildContent() {

    mainPanel = new JPanel(new MigLayout("insets 5,hidemode 3","[grow]"));    
    stepPanel = new JPanel(new MigLayout("insets 0", "[grow 10][grow 90]"));
    
    final JPanel stepNoPanel = new JPanel();
    stepNoPanel.setBorder(BorderFactory.createEtchedBorder());
    stepNumber = new JLabel();
    stepNumber.setFont(LARGE_BOLD_FONT);
    stepNoPanel.add(stepNumber);
    
    final JPanel stepDescPanel = new JPanel();
    stepDescPanel.setBorder(BorderFactory.createEtchedBorder());
    stepDescription = new JLabel();
    stepDescription.setFont(LARGE_BOLD_FONT);
    stepDescPanel.add(stepDescription);
    
    final JPanel descPanel = new JPanel();
    descPanel.setBorder(BorderFactory.createEtchedBorder());
    descPanel.setLayout(new BorderLayout());
    stepDetails = new JTextArea();
    stepDetails.setEditable(false);
    stepDetails.setLineWrap(true);
    stepDetails.setWrapStyleWord(true);
    stepDetails.setFont(MEDIUM_FONT);    
    descPanel.add(stepDetails);

    detailPanel = new JPanel();

    stepChanged();
    

    stepPanel.add(stepNoPanel, "growx 10");
    stepPanel.add(stepDescPanel, "growx 90,growy,wrap");
    
    mainPanel.add(stepPanel, "grow,wrap");
    mainPanel.add(descPanel, "span 2,growx,wmin 10,wrap");
    mainPanel.add(detailPanel, "span 2,growx,wmin 10,wrap");
    
    continuePanel = new JPanel (new MigLayout("insets 4","push[]push","push[]push"));
    continueButton = new JButton("Done (more to come)");
    continueButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doContinue();
      }});
    continuePanel.add(continueButton);
    mainPanel.add(continuePanel, "span 2,growx");
  }

  protected JPanel getDetailPanelForStep(int step) {
    JPanel detailPanel = new JPanel();

    if (step == AssaultModel.STEP_SELECT) {
      detailPanel =  new JPanel(new MigLayout("gap 0,insets 5,fill", "[center,fill,grow 20][center,fill,grow 5][center,fill,grow 5][center,fill,grow 5][fill,grow 5][fill,grow 20][fill,grow 20]","[fill,grow]"));
      detailPanel.setBorder(BorderFactory.createEtchedBorder());

      JLabelPanel l = new JLabelPanel("Unit", SwingConstants.CENTER);
      l.setFont(LARGE_BOLD_FONT);
      l.setBorder(TOP_LEFT_BORDER);
      detailPanel.add(l, "span 1 2");

      JLabelPanel name = new JLabelPanel("Effective", SwingConstants.CENTER);
      name.setFont(LARGE_BOLD_FONT);
      name.setBorder(TOP_LEFT_BORDER);
      detailPanel.add (name, "span 3");

      JLabelPanel details = new JLabelPanel("Details", SwingConstants.CENTER);
      details.setFont(LARGE_BOLD_FONT);
      details.setBorder(TOP_LEFT_BORDER);
      detailPanel.add (details, "span 1 2");

      JLabelPanel assault = new JLabelPanel("<html><center>May<br>Assault?", SwingConstants.CENTER);
      assault.setFont(LARGE_BOLD_FONT);
      assault.setBorder(TOP_LEFT_BORDER);
      detailPanel.add (assault, "span 1 2");

      JLabelPanel cont = new JLabelPanel("Continue?", SwingConstants.CENTER);
      cont.setFont(LARGE_BOLD_FONT);
      cont.setBorder(TOP_RIGHT_BORDER);
      detailPanel.add (cont, "span 1 2,wrap");

      l = new JLabelPanel("Attack", SwingConstants.CENTER);
      l.setFont(LARGE_BOLD_FONT);
      l.setBorder(TOP_LEFT_BORDER);
      detailPanel.add(l);

      l = new JLabelPanel("Def", SwingConstants.CENTER);
      l.setFont(LARGE_BOLD_FONT);
      l.setBorder(TOP_LEFT_BORDER);
      detailPanel.add(l);

      l = new JLabelPanel("Tqr", SwingConstants.CENTER);
      l.setFont(LARGE_BOLD_FONT);
      l.setBorder(TOP_LEFT_BORDER);
      detailPanel.add(l,"wrap");

      final int count = myModel.getSourceCount();

      for (int i = 0; i < count; i++) {
        l = new JLabelPanel("", SwingConstants.CENTER);
        final ImageIcon ii = new ImageIcon();
        ii.setImage(myModel.getSourceImage(i));
        l.setIcon(ii);
        l.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add(l);

        name = new JLabelPanel(
          "<html>" +
            myModel.getPrimaryAssaultRating(i) +
            "/" +
            myModel.getPrimaryAssaultColour(i) +
            "<br>" +
            myModel.getSecondaryAssaultRating(i) +
            "/" +
            myModel.getSecondaryAssaultColour(i)
          ,
          SwingConstants.CENTER);
        name.setFont(MEDIUM_FONT);
        name.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add (name);

        name = new JLabelPanel(myModel.getSourceDef(i), SwingConstants.CENTER);
        name.setFont(MEDIUM_FONT);
        name.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add (name);

        name = new JLabelPanel(String.valueOf(myModel.getSourceTqr(i)), SwingConstants.CENTER);
        name.setFont(MEDIUM_FONT);
        name.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add (name);

        name = new JLabelPanel(myModel.getSourceDetails(i), SwingConstants.CENTER);
        name.setFont(MEDIUM_FONT);
        name.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add (name);

        assault = new JLabelPanel(myModel.getSourceAssaultReason(i), SwingConstants.CENTER);
        assault.setFont(MEDIUM_FONT);
        assault.setBorder(i < (count-1) ? TOP_LEFT_BORDER : BOT_LEFT_BORDER);
        detailPanel.add (assault);

        JPanel selectPanel = new JPanel(new MigLayout("insets 4,fillx,filly","push[]push","push[]push"));
        JCheckBox check = new JCheckBox();
        check.setEnabled(myModel.canAssault(i));
        check.setName(String.valueOf(i));
        check.addActionListener(e -> checkChanged((JCheckBox) e.getSource()));

        selectPanel.add(check);
        selectPanel.setBorder(i < (count-1) ? TOP_RIGHT_BORDER : BOT_RIGHT_BORDER);
        detailPanel.add(selectPanel, "wrap");

      }
    }
    return detailPanel;
  }

  protected void doContinue() {
    myModel.nextStep();
    stepChanged();
  }
  
  public void checkChanged(JCheckBox check) {
    int i = Integer.parseInt(check.getName());
    myModel.setContinuing(i, check.isSelected());
  }
  
  class JLabelPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    protected static final int PAD = 4;
    protected JLabel myLabel;
    
    public JLabelPanel (String text, int alignment) {
      setLayout(new MigLayout("fill,insets "+PAD, "[grow,fill]", "[grow,fill]"));
      myLabel = new JLabel (text, alignment);
      add(myLabel);      
    }
    
    public void setFont (Font font) {
      if (myLabel == null) {
        super.setFont(font);
      }
      else {
        myLabel.setFont(font);
      }
    }
    
    public void setIcon (Icon icon) {
      myLabel.setIcon(icon);
    }
  }
}
