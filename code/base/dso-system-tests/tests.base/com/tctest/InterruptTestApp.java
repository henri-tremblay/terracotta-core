/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tctest;

import EDU.oswego.cs.dl.util.concurrent.CyclicBarrier;

import com.tc.object.config.ConfigVisitor;
import com.tc.object.config.DSOClientConfigHelper;
import com.tc.object.config.TransparencyClassSpec;
import com.tc.simulator.app.ApplicationConfig;
import com.tc.simulator.listener.ListenerProvider;
import com.tctest.runner.AbstractTransparentApp;

public class InterruptTestApp extends AbstractTransparentApp {
  private boolean interruptedFlag = false;
  private final Object lockObject = new Object();
  
  private final CyclicBarrier barrier;

  public InterruptTestApp(String appId, ApplicationConfig cfg, ListenerProvider listenerProvider) {
    super(appId, cfg, listenerProvider);
    barrier = new CyclicBarrier(getParticipantCount());
  }

  public void run() {
    try {
      int index = barrier.barrier();
      testWaitInterrupt(index);
    } catch (Throwable t) {
      notifyError(t);
    }
  }

  private void testWaitInterrupt(int index) throws Exception {
    if (index == 0) {
      final CyclicBarrier localBarrier = new CyclicBarrier(2);
      Thread t = new Thread(new Runnable() {
        public void run() {
          try {
            synchronized(lockObject) {
              localBarrier.barrier();
              lockObject.wait();
            }
          } catch (InterruptedException e) {
            interruptedFlag = true;
          }
        }
      });
      t.start();
      localBarrier.barrier();
      while (!interruptedFlag) {
        t.interrupt();
      }
    }
  }

  public static void visitL1DSOConfig(ConfigVisitor visitor, DSOClientConfigHelper config) {
    TransparencyClassSpec spec = config.getOrCreateSpec(CyclicBarrier.class.getName());
    config.addWriteAutolock("* " + CyclicBarrier.class.getName() + "*.*(..)");

    String testClass = InterruptTestApp.class.getName();
    spec = config.getOrCreateSpec(testClass);

    config.addIncludePattern(testClass + "$*");

    String methodExpression = "* " + testClass + "*.*(..)";
    config.addWriteAutolock(methodExpression);

    spec.addRoot("barrier", "barrier");
    spec.addRoot("lockObject", "lockObject");
  }
}
