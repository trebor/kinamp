package org.trebor.kinamp.dsp;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.Mode;

interface Monitor
{
  Dimension getDimension();
  Mode getMode();
  void sample(float value);
}