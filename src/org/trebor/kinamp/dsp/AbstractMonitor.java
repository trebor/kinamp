package org.trebor.kinamp.dsp;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.Mode;

public abstract class AbstractMonitor<T extends Monitor> implements Monitor
{
  private final Dimension mDimension;
  private final Action<T> mAction;
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
  
  protected void execute(T monitor)
  {
    mAction.execute(monitor);
  }
  
  public abstract void sample(float value);
}