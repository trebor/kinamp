package org.trebor.kinamp;

import org.trebor.kinamp.Imu.Dimension;

public interface ImuListener
{
  void onRaw(Dimension dimension, int value);
}
