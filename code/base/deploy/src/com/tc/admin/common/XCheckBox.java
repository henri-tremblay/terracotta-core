/*
 * Copyright (c) 2003-2006 Terracotta, Inc. All rights reserved.
 */
package com.tc.admin.common;

import javax.swing.JPopupMenu;

import org.dijon.CheckBox;

public class XCheckBox extends CheckBox {
  protected XPopupListener m_popupListener;
  
  public XCheckBox() {
    super();
    m_popupListener = new XPopupListener(this);
    
    JPopupMenu popup = createPopup();
    if(popup != null) {
      setPopupMenu(popup);
    }
  }
  
  public XCheckBox(String label) {
    this();
    setText(label);
  }
  
  protected JPopupMenu createPopup() {
    return null;
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    m_popupListener.setPopupMenu(popupMenu);
  }

  public JPopupMenu getPopupMenu() {
    return m_popupListener.getPopupMenu();
  }
}
