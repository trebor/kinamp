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
  public static final String LINE_REGEX = "^(X=([\\d]*))?(Y=([\\d]*))?(Z=([\\d]*))?(B=([\\d]*))?(R=([\\d]*))?$";
  public static final int X_GROUP = 2;
  public static final int Y_GROUP = 4;
  public static final int Z_GROUP = 6;
  public static final int B_GROUP = 8;
  public static final int R_GROUP = 10;
  
  public enum Dimension
  {
    X_AXIS(X_GROUP), Y_AXIS(Y_GROUP), Z_AXIS(Z_GROUP), ROTATION(R_GROUP), BATTARY(B_GROUP);
    
    private final int mParseGroup;

    Dimension(int parseGroup)
    {
      mParseGroup = parseGroup;
    }

    public int getParseGroup()
    {
      return mParseGroup;
    }
  }
  
  private final List<ImuListener> mListeners;
  private final BufferedReader mSource;
  private final BufferedWriter mSink;
  private final Pattern mLinePattern;
  private Thread mProcessThread;
  
  public Imu(InputStream inputStream, OutputStream outputStream)
  {
    mSource = new BufferedReader(new InputStreamReader(inputStream));
    mSink = new BufferedWriter(new OutputStreamWriter(outputStream));
    mListeners = new ArrayList<ImuListener>();
    mProcessThread = null;
    mLinePattern = Pattern.compile(LINE_REGEX);
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
          processLine(line);
        else
          stop();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  private void processLine(String line)
  {
    Matcher m = mLinePattern.matcher(line);
    if (!m.find())
      return;
    
    for (Dimension dimension: Dimension.values())
    {
      String value = m.group(dimension.getParseGroup());
      if (value != null)
        for (ImuListener listener: mListeners)
          listener.onRaw(dimension, Integer.valueOf(value));
    }
  }

  public void start(boolean block)
  {
    if (isRunning())
      stop();
    
    startData();
    mProcessThread = new Thread()
    {
      public void run()
      {
        processImuData();
      }
    };

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

  private boolean isRunning()
  {
    return null != mProcessThread;
  }

  private void startData()
  {
    try
    {
      mSink.write(" 1");
      mSink.flush();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
    }
  }
  
  private void stopData()
  {
    try
    {
      mSink.write(" ");
      mSink.flush();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
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
}
