/*
 * $Id: AssaultModel 7690 2011-07-07 23:44:55Z swampwallaby $
 *
 * Copyright (c) 2012-2017 by Brent Easton
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


package tdc.assault;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import tdc.UnitInfo;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.GamePiece;

public class AssaultModel {

  protected List<GamePiece> sources;
  protected List<AssaultInfo> sourceInfos = new ArrayList<AssaultInfo>();
  protected List<GamePiece> targets;
  protected List<UnitInfo> targetInfos = new ArrayList<UnitInfo>();
  protected AssaultView changeListener;
  protected int step = 1;
  protected String[] stepDescriptions = new String[] {"Initialising", "Assault Unit Selection"};
  protected String[] stepDetails = new String[] {"Initialising", "Select the Units to continue with the Assault."};
  protected boolean sourceOverStacked = false;
  protected boolean targetOverStacked = false;
  
  public AssaultModel(List<GamePiece> sourcePieces, List<GamePiece> targetPieces) {
    sources = sourcePieces;
    targets = targetPieces;
    for (GamePiece gp : sources) {
      sourceInfos.add(new AssaultInfo(gp));
    }
    for (GamePiece gp : targets) {
      targetInfos.add(new UnitInfo(gp, false));
    }
    
    // Check if source hex is overstacked
    int columnCount = 0;
    int columnMass = 0;
    int totalMass =  sourceInfos.get(0).getInfo().getMass();
    for (AssaultInfo ai : sourceInfos) {
      if (ai.getInfo().isInColumn()) {
        columnCount++;
        columnMass += ai.getInfo().getSteps();
      }
    }
    if (columnCount > 1 || (totalMass-columnMass) > 8) {
      sourceOverStacked = true;
    }
    
    // Target hex?
    columnCount = 0;
    columnMass = 0;
    totalMass = 0;
    totalMass += targetInfos.get(0).getMass();
    for (UnitInfo ui : targetInfos) {    
      if (ui.isInColumn()) {
        columnCount++;
        columnMass += ui.getSteps();
      }
    }
    if (columnCount > 1 || (totalMass-columnMass) > 8) {
      targetOverStacked = true;
    }
    
  }
  
  public void setChangeListener(AssaultView listener) {
    changeListener = listener;
  }

  public boolean isSourceOverStacked () {
    return sourceOverStacked;
  }
  
  public boolean isTargetOverStacked () {
    return targetOverStacked;
  }
  
  public int getStep() {
    return step;
  }
  
  public String getStepDescription() {
    return stepDescriptions[step];
  }
  
  public String getStepDetails() {
    return stepDetails[step];
  }
  
  public BufferedImage getSourceImage(int i) {
    return sourceInfos.get(i).getImage();
  }
  
  public int getSourceCount() {
    return sourceInfos.size();
  }
  
  public String getSourceName(int i) {
    return sourceInfos.get(i).getName();
  }
  
  public boolean canAssault(int i) {
    return ! isSourceOverStacked() && sourceInfos.get(i).getInfo().canAssault(false);
  }
  
  public String getSourceAssaultReason(int i) {
    if (isSourceOverStacked()) {
      return "No, hex overstacked";
    }
    return sourceInfos.get(i).getInfo().getAssaultReason();
  }
  
  public void setContinuing (int i, boolean continuing) {
    sourceInfos.get(i).setContinuing(continuing);
  }
  
  public int getSourceTqr(int i) {
    return sourceInfos.get(i).getInfo().getEffectiveTqr();
  }
  
  public String getSourceDef (int i) {
    return sourceInfos.get(i).getInfo().getEffectiveDefenceRating();
  }
  
  public String getSourceAtt (int i) {
    return sourceInfos.get(i).getInfo().getEffectiveFireRating();
  }
  
  public String getSourceAss (int i) {
    return sourceInfos.get(i).getInfo().getEffectiveAssaultRating();
  }
  
  public boolean isDoubleAssault (int i) {
    return sourceInfos.get(i).getInfo().isEffectiveDoubleAssault();
  }
  
  public String getPrimaryAssaultRating (int i) {
    return isDoubleAssault(i) ? getSourceAss(i) : getSourceAtt(i);
  }
  
public String getSecondaryAssaultRating (int i) {
    return getSourceAss(i);
  }
  class AssaultInfo {
    protected GamePiece piece;
    protected UnitInfo info;
    protected int braveryRoll;
    protected boolean continuing;
    protected String imageName = "";
    protected BufferedImage image;
    
    public AssaultInfo (GamePiece gp) {
      setPiece(gp);
      setInfo(new UnitInfo(gp, false));
      setContinuing (info.canAssault(false));
      setBraveryRoll(info.getEffectiveTqr());
      findImage();
    }
    
    protected void findImage() {
      image = new BufferedImage(75, 75, BufferedImage.TYPE_INT_RGB);
      final Graphics2D graphics = image.createGraphics();
      getPiece().draw(graphics, 37, 37, null, 1.0f); 
      
    }
    
    public BufferedImage getImage() {
      return image;
    }
    
    public GamePiece getPiece() {
      return piece;
    }
    public void setPiece(GamePiece piece) {
      this.piece = piece;
    }
    public UnitInfo getInfo() {
      return info;
    }
    public void setInfo(UnitInfo info) {
      this.info = info;
    }
    public int getBraveryRoll() {
      return braveryRoll;
    }
    public void setBraveryRoll(int braveryRoll) {
      this.braveryRoll = braveryRoll;
    }
    public boolean isContinuing() {
      return continuing;
    }
    public void setContinuing(boolean continuing) {
      this.continuing = continuing;
    }
    public String getName() {
      return (String) piece.getProperty(BasicPiece.BASIC_NAME);
    }
  }


}
