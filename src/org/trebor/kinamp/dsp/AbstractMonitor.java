package org.trebor.kinamp.dsp;

import org.trebor.kinamp.imu.Imu.Dimension;
import org.trebor.kinamp.imu.Imu.Mode;

public abstract class AbstractMonitor<T extends MonitorEvent> implements Monitor
{
  private final Dimension mDimension;
  private Action<T> mAction;
  private final Mode mMode;
  
  public AbstractMonitor(Mode mode, Dimension dimension, Action<T> action)
  {
    mDimension = dimension;
    mAction = action;
    mMode = mode;
  }
  
  public Dimension getDimension()
  {
    return mDimension;
  }
  
  public Mode getMode()
  {
    return mMode;
  }
  
  protected void execute(T event)
  {
    mAction.execute(event);
  }
  
  public abstract void sample(float value);

  public void setAction(Action<T> action)
  {
    mAction = action;
  }

  public Action<T> getAction()
  {
    return mAction;
  }
}