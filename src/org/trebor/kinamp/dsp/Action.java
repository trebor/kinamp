package org.trebor.kinamp.dsp;

public interface Action<T extends MonitorEvent>
{
  void execute(T event);
}
