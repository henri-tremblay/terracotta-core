/*
 * Copyright (c) 2003-2006 Terracotta, Inc. All rights reserved.
 */
package com.tc.admin;

public interface ConnectionListener {
  void handleConnection();
  void handleException();
}
