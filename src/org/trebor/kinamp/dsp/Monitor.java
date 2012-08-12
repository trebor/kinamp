package org.trebor.kinamp.dsp;

import org.trebor.kinamp.imu.Imu.Dimension;
import org.trebor.kinamp.imu.Imu.Mode;

interface Monitor
{
  Dimension getDimension();
  Mode getMode();
  void sample(float value);
}