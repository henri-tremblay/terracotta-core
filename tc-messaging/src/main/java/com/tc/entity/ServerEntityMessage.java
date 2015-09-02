package com.tc.entity;

import com.tc.net.protocol.tcm.TCMessage;
import com.tc.object.EntityDescriptor;

/**
 * @author twu
 */
public interface ServerEntityMessage extends TCMessage {

  void setMessage(EntityDescriptor entityDescriptor, byte[] payload);

  void setMessage(EntityDescriptor entityDescriptor, byte[] payload, long responseId);

  EntityDescriptor getEntityDescriptor();
  
  byte[] getMessage();

  Long getResponseId();
}
