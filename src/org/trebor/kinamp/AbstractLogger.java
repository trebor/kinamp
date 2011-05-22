package org.trebor.kinamp;

public abstract class AbstractLogger implements Loggable
{
  public abstract String log(String format, Object... args);

  public String debug(String format, Object... args)
  {
    return log("DEBUG: " + format, args);
  }

  public String error(String format, Object... args)
  {
    return log("ERROR: " + format, args);
  }

  public String error(Throwable exception, String format, Object... args)
  {
    return log("ERROR " + exception.getMessage() + ": " + format, args);
  }
}
