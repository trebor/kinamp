package org.trebor.kinamp.dsp;

public interface Action<T extends Monitor>
{
  void execute(T monitor);
}
