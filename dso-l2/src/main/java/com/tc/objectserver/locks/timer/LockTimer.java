/*
 *
 *  The contents of this file are subject to the Terracotta Public License Version
 *  2.0 (the "License"); You may not use this file except in compliance with the
 *  License. You may obtain a copy of the License at
 *
 *  http://terracotta.org/legal/terracotta-public-license.
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 *  the specific language governing rights and limitations under the License.
 *
 *  The Covered Software is Terracotta Core.
 *
 *  The Initial Developer of the Covered Software is
 *  Terracotta, Inc., a Software AG company
 *
 */
package com.tc.objectserver.locks.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.net.ClientID;
import com.tc.object.locks.LockID;
import com.tc.object.locks.ThreadID;
import com.tc.objectserver.locks.LockHelper;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class LockTimer {
  private static final Logger logger = LoggerFactory.getLogger(LockTimer.class);

  private final Timer           timer     = new Timer("DSO Lock Object.wait() timer", true);
  private boolean               started   = false;
  private boolean               shutdown  = false;
  private LinkedList<TaskImpl>  taskQueue = new LinkedList<>();

  public LockTimer() {
    super();
  }

  public Timer getTimer() {
    return timer;
  }

  public synchronized void start() {
    started = true;
    scheduleQueuedTasks();
    taskQueue = null;
  }

  private void scheduleQueuedTasks() {
    for (TaskImpl task : taskQueue) {
      long timeDelay = task.getScheduleDelay() - (System.currentTimeMillis() - task.scheduledAt());
      timeDelay = timeDelay < 0 ? 0 : timeDelay;
      timer.schedule(task, timeDelay);
    }
  }

  public TimerTask scheduleTimer(TimerCallback callback, long timeInMillis, LockTimerContext callbackObject) {
    final TaskImpl rv = new TaskImpl(callback, timeInMillis, callbackObject);

    synchronized (this) {
      if (!started) {
        taskQueue.addLast(rv);
        return rv;
      }
    }

    timer.schedule(rv, timeInMillis);
    return rv;
  }

  public synchronized void shutdown() {
    if (shutdown) return;
    shutdown = true;
    this.timer.cancel();
  }

  private static class TaskImpl extends TimerTask {

    private final TimerCallback    callback;
    private final LockTimerContext callbackObject;
    private final long             scheduleDelayInMillis;
    private final long             scheduledAt;

    TaskImpl(TimerCallback callback, long timeInMillis, LockTimerContext callbackObject) {
      this.callback = callback;
      this.callbackObject = callbackObject;
      this.scheduleDelayInMillis = timeInMillis;
      this.scheduledAt = System.currentTimeMillis();
    }

    public long getScheduleDelay() {
      return scheduleDelayInMillis;
    }

    public long scheduledAt() {
      return scheduledAt;
    }

    @Override
    public void run() {
      try {
        callback.timerTimeout(callbackObject);
      } catch (Exception e) {
        logger.error("Error processing wait timeout for " + callbackObject, e);
      }
    }

    @Override
    public boolean cancel() {
      return super.cancel();
    }
  }

  public static class LockTimerContext {
    private final LockID     lockID;
    private final ThreadID   tid;
    private final ClientID   cid;
    private final LockHelper helper;

    public LockTimerContext(LockID lockID, ThreadID tid, ClientID cid, LockHelper helper) {
      this.lockID = lockID;
      this.tid = tid;
      this.cid = cid;
      this.helper = helper;
    }

    public LockID getLockID() {
      return lockID;
    }

    public ThreadID getThreadID() {
      return tid;
    }

    public ClientID getClientID() {
      return cid;
    }

    public LockHelper getHelper() {
      return helper;
    }
  }
}
