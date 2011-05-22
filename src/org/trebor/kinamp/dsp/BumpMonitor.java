package org.trebor.kinamp.dsp;

import static org.trebor.kinamp.Imu.Mode.*;

import org.trebor.kinamp.Imu.Dimension;

public class BumpMonitor extends AbstractMonitor<BumpMonitor>
{
  public BumpMonitor(Dimension dimension, Action<BumpMonitor> action)
  {
    super(GRAVITY, dimension, action);
  }

  public void sample(float value)
  {
    execute(this);
  }
}
