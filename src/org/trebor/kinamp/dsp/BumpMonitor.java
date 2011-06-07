package org.trebor.kinamp.dsp;

import static org.trebor.kinamp.Imu.Mode.*;
import static org.trebor.kinamp.dsp.DspUtil.lowPass;
import static org.trebor.kinamp.dsp.BumpMonitorEvent.BumpType.*;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Range;

public class BumpMonitor extends AbstractMonitor<BumpMonitorEvent>
{
  @SuppressWarnings("unused")
  private final String T = "+" + getClass().getSimpleName().toString();

  private float mFilterHistory;
  private final float mBumpThreshold;
  private final float mSmoothing;
  private Range mPersistantRange;
  protected float mOldSmooth = 0;
  protected float mOldFirstD = 0;
  protected float mOldSecondD = 0;
  protected boolean mReady = true;
  
  public BumpMonitor(Dimension dimension, float bumpThreshold, float smoothing, Action<BumpMonitorEvent> action)
  {
    super(RAW, dimension, action);
    mFilterHistory = 0;
    mBumpThreshold = bumpThreshold / 2;
    mSmoothing = smoothing;
    mPersistantRange = new Range(); 
  }

  public void sample(float value)
  {
    float smooth = lowPass(mOldSmooth, value, mSmoothing);
    float rawFirstD = smooth - mOldSmooth;
    mPersistantRange.register(-rawFirstD);
    float firstD = mPersistantRange.normal(rawFirstD) - 0.5f;
    float secondD = firstD - mOldFirstD;
    
    if (mOldSecondD > 0 && secondD < 0)
    {
      if (mReady && mOldFirstD > getBumpThreshold())
      {
        execute(new BumpMonitorEvent(UP, mOldFirstD * 2));
        mReady = false;
      }
    }
    else if (mOldSecondD < 0 && secondD > 0)
    {
      if (mReady && mOldFirstD < -getBumpThreshold())
      {
        execute(new BumpMonitorEvent(DOWN, mOldFirstD * -2));
        mReady = false;
      }
    }

    if (mOldFirstD > 0 && firstD < 0 || mOldFirstD < 0 && firstD > 0)
      mReady = true;
    
    mOldSmooth = smooth;
    mOldFirstD = firstD;
    mOldSecondD = secondD;
  }

  public float getFilterHistory()
  {
    return mFilterHistory;
  }

  public float getBumpThreshold()
  {
    return mBumpThreshold;
  }

  public float getSmoothing()
  {
    return mSmoothing;
  }
}
