package org.trebor.kinamp.dsp;

public class RawMonitorEvent implements MonitorEvent
{
  private final float mValue;

  public RawMonitorEvent(float value)
  {
    mValue = value;
  }

  public float getValue()
  {
    return mValue;
  }
}
