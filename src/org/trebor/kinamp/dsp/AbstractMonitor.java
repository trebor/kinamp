package org.trebor.kinamp.dsp;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.Mode;

public abstract class AbstractMonitor<T extends Monitor> implements Monitor
{
  private final Dimension mDimension;
  private final Action<T> mAction;
  private final Mode mMode;
  
  public AbstractMonitor(Dimension dimension, Mode mode, Action<T> action)
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
  
  protected static float lowPass(float history, float sample, float filter)
  {
    return history * filter + sample * (1 - filter);
  }

  public abstract void sample(float value);
}