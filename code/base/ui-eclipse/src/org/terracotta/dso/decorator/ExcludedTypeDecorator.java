/*
 * Copyright (c) 2003-2006 Terracotta, Inc. All rights reserved.
 */
package org.terracotta.dso.decorator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;

import org.terracotta.dso.ConfigurationHelper;
import org.terracotta.dso.TcPlugin;

/**
 * Adorns Java types that are excluded from instrumentation.
 * 
 * The adornment appears in the Package Explorer amd Outline view.
 * 
 * @see org.eclipse.jface.viewers.LabelProvider
 * @see org.terracotta.dso.ConfigurationHelper.isExcluded
 */

public class ExcludedTypeDecorator extends LabelProvider
  implements ILightweightLabelDecorator
{
  private static final ImageDescriptor
  m_imageDesc = ImageDescriptor.createFromURL(
    ExcludedTypeDecorator.class.getResource(
      "/com/tc/admin/icons/error_co.gif"));

  public static final String
    DECORATOR_ID = "org.terracotta.dso.excludedTypeDecorator";

  public void decorate(Object element, IDecoration decoration) {
    TcPlugin plugin  = TcPlugin.getDefault();
    IType    type    = (IType)element;
    IProject project = type.getJavaProject().getProject();
  
    if(plugin.hasTerracottaNature(project)) {
      ConfigurationHelper config = plugin.getConfigurationHelper(project);

      if(config != null && config.isExcluded(type)) {
        decoration.addOverlay(m_imageDesc);
      }
    }
  }

  public static void updateDecorators() {
    TcPlugin.getDefault().updateDecorator(DECORATOR_ID);
  }
}
