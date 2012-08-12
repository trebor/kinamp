package org.trebor.kinamp.dsp;

public class BumpMonitorEvent implements MonitorEvent
{
  private final BumpType mType;
  private final float mAmplitude;

  public enum BumpType {UP, DOWN}
  
  public BumpMonitorEvent(BumpType type, float amplitude)
  {
    mType = type;
    mAmplitude = amplitude;
  }

  public BumpType getType()
  {
    return mType;
  }

  public float getAmplitude()
  {
    return mAmplitude;
  }
}
