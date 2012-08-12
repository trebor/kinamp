package org.trebor.kinamp.imu;

import org.trebor.kinamp.imu.Imu.Dimension;

public interface ImuListener
{
  void onRaw(Dimension dimension, int value);
  void onGravity(Dimension dimension, float value);
  void onDegree(Dimension dimension, float value);
}
