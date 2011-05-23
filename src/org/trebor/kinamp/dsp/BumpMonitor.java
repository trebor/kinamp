package org.trebor.kinamp.dsp;

import static org.trebor.kinamp.Imu.Mode.*;

import org.trebor.kinamp.Imu.Dimension;

public class BumpMonitor extends AbstractMonitor<BumpMonitor>
{
  boolean mIsFirst;
  
  private float mFilterHistory;
  
  public BumpMonitor(Dimension dimension, Action<BumpMonitor> action)
  {
    super(GRAVITY, dimension, action);
    mIsFirst = true;
    mFilterHistory = 0;
  }

  public void sample(float value)
  {
    if (mIsFirst)
    {
      mIsFirst = false;
      mFilterHistory = value;
      return;
    }
    
    mFilterHistory = lowPass(mFilterHistory, value, 0.95f);
    execute(this);
  }

  public float getFilterHistory()
  {
    return mFilterHistory;
  }
}
