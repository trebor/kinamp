package org.trebor.kinamp;

public interface ImuListener
{
  void onRawX(int x);
  void onRawY(int y);
  void onRawZ(int z);
  void onRawRotate(int rotate);
  void onRawBattery(int battery);
}
