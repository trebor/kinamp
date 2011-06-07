package org.trebor.kinamp;

public class Range
{
  private float mMin;
  private float mMax;

  public Range()
  {
    reset();
  }
  
  public void reset()
  {
    mMin = Float.MAX_VALUE;
    mMax = Float.MIN_VALUE;
  }

  public void resetMin()
  {
    mMin = Float.MAX_VALUE;
  }

  public void resetMax()
  {
    mMax = Float.MIN_VALUE;
  }
  
  public void register(float sample)
  {
    if (sample > mMax)
      mMax = sample;
    
    if (sample < mMin)
      mMin = sample;
  }
  
  public float normal(float sample)
  {
    register(sample);
    
    if (sample == mMin && sample == mMax)
      return 0;

    return (sample - mMin) / (mMax - mMin);
  }

  public float getDelta()
  {
    return mMax - mMin;
  }
  
  public float getMin()
  {
    return mMin;
  }

  public float getMax()
  {
    return mMax;
  }
}