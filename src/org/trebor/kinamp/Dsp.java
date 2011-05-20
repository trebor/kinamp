package org.trebor.kinamp;

import org.trebor.kinamp.Imu.Dimension;

public class Dsp implements ImuListener
{
  @SuppressWarnings("unused")
  private int lowPass(int history, int sample, float filter)
  {
    return (int)(history * filter + sample * (1 - filter));
  }  

  public void onRaw(Dimension dimension, int value)
  {
  }

  public void onGravity(Dimension dimension, float value)
  {
  }
}
