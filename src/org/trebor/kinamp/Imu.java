package org.trebor.kinamp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Imu
{
  public static final String RAW_LINE_REGEX = "^(X=([\\d]*))?(Y=([\\d]*))?(Z=([\\d]*))?(B=([\\d]*))?(R=([\\d]*))?$";
  public static final String GRAVITY_LINE_REGEX = "^(X=([\\d\\.\\-]*))?(Y=([\\d\\.\\-]*))?(Z=([\\d\\.\\-]*))?(B=([\\d\\.\\-]*))?(R=([\\d\\-]*))?$";

  public static final int X_GROUP = 2;
  public static final int Y_GROUP = 4;
  public static final int Z_GROUP = 6;
  public static final int B_GROUP = 8;
  public static final int R_GROUP = 10;
  
  public static final char MAIN_MENU = ' ';
  public static final char START_DETECTION = '1';
  public static final char MODE_MENU = '5';
  public static final char GRAVITY_MODE = '1';
  public static final char RAW_MODE = '2';

  public enum Mode
  {
    RAW(RAW_LINE_REGEX, RAW_MODE) {
      protected void signal(Dimension dimension, String value,
        List<ImuListener> listeners)
      {
        int i = Integer.valueOf(value);
        for (ImuListener listener: listeners)
          listener.onRaw(dimension, i);
      }
    }, GRAVITY(GRAVITY_LINE_REGEX, GRAVITY_MODE) {
      protected void signal(Dimension dimension, String value,
        List<ImuListener> listeners)
      {
        float f = Float.valueOf(value);
        for (ImuListener listener: listeners)
          listener.onGravity(dimension, f);
      }
    };
    
    private final Pattern mPattern;
    private final char[] mStartCommand;
    private final char[] mStopCommand;
    
    Mode(String lineRegex, char modeSelect)
    {
      mPattern = Pattern.compile(lineRegex);
      mStartCommand = new char[]{MAIN_MENU, MODE_MENU, modeSelect, START_DETECTION};
      mStopCommand = new char[]{MAIN_MENU};
    }
    
    public Matcher parse(String line)
    {
      return mPattern.matcher(line);
    }
    
    public char[] getStartCommand()
    {
      return mStartCommand;
    }

    public char[] getStopCommand()
    {
      return mStopCommand;
    }

    public void processLine(String line, List<ImuListener> listeners)
    {
      Matcher m = parse(line);
      if (!m.find())
        return;
      
      for (Dimension dimension: Dimension.values())
      {
        String value = m.group(dimension.getParseGroup());
        if (null != value)
          signal(dimension, value, listeners);
      }
    }

    protected abstract void signal(Dimension dimension, String value,
      List<ImuListener> listeners);
  }
  
  public enum Dimension
  {
    X_AXIS(X_GROUP, "X"), Y_AXIS(Y_GROUP, "Y"), Z_AXIS(Z_GROUP, "Z"), BATTARY(B_GROUP, "BATTARY"), ROTATION(R_GROUP, "ROTATE");
    
    private final int mParseGroup;
    private final String mName;

    Dimension(int parseGroup, String name)
    {
      mParseGroup = parseGroup;
      mName = name;
    }

    public int getParseGroup()
    {
      return mParseGroup;
    }
    
    public String toString()
    {
      return mName;
    }
  }
  
  private final List<ImuListener> mListeners;
  private final BufferedReader mSource;
  private final BufferedWriter mSink;
  private Mode mMode;
  private Thread mProcessThread;
  
  public Imu(InputStream inputStream, OutputStream outputStream)
  {
    mSource = new BufferedReader(new InputStreamReader(inputStream));
    mSink = new BufferedWriter(new OutputStreamWriter(outputStream));
    mListeners = new ArrayList<ImuListener>();
    mProcessThread = null;
  }

  public void addListner(ImuListener listener)
  {
    mListeners.add(listener);
  }
  
  protected void processImuData()
  {
    while (null != mProcessThread)
    {
      try
      {
        String line = mSource.readLine();
        if (null != line)
          mMode.processLine(line, mListeners);
        else
          stop();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void start(Mode mode, boolean block)
  {
    // stop if running 
    
    if (isRunning())
      stop();
    
    // set mode
    
    mMode = mode;
    
    // start data for this mode
    
    startData();
    mProcessThread = new Thread()
    {
      public void run()
      {
        processImuData();
      }
    };

    // start the process thread and block if required
    
    synchronized (mProcessThread)
    {
      mProcessThread.start();

      if (block)
      {
        try
        {
          mProcessThread.wait();
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  public void stop()
  {
    synchronized (mProcessThread)
    {
      stopData();
      mProcessThread.notifyAll();
      mProcessThread = null;
    }
  }

  private boolean isRunning()
  {
    return null != mProcessThread;
  }

  private void startData()
  {
    sendCommand(mMode.getStartCommand());
  }
  
  private void stopData()
  {
    sendCommand(mMode.getStopCommand());
  }

  private void sendCommand(char[] command)
  {
    try
    {
      mSink.write(command);
      mSink.flush();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
    }
  }
}
