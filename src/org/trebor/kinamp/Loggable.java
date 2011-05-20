package org.trebor.kinamp;

public interface Loggable
{
  String debug(String format, Object...args);
  String error(String format, Object...args);
  String error(Throwable exception, String format, Object...args);
}
