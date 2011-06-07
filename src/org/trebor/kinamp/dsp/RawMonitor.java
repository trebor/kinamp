package org.trebor.kinamp.dsp;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.Mode;

public class RawMonitor extends AbstractMonitor<RawMonitorEvent>
{
  public RawMonitor(Mode mode, Dimension dimension, Action<RawMonitorEvent> action)
  {
    super(mode, dimension, action);
  }
  
  public void sample(float value)
  {
    execute(new RawMonitorEvent(value));
  }
}
