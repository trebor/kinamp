package org.trebor.kinamp.dsp;

public class DspUtil
{
  public static float lowPass(float history, float sample, float filter)
  {
    return (history * filter) + (sample * (1 - filter));
  }
}
