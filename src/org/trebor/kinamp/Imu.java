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
  
  private final List<ImuListener> mListeners;
  private final ImuListener mListener;
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
    mListener = new ImuListener()
    {
      public void onRawX(int x)
      {
        for (ImuListener listener: mListeners)
          listener.onRawX(x);
      }

      public void onRawY(int y)
      {
        for (ImuListener listener: mListeners)
          listener.onRawY(y);
      }

      public void onRawZ(int z)
      {
        for (ImuListener listener: mListeners)
          listener.onRawZ(z);
      }

      public void onRawRotate(int rotate)
      {
        for (ImuListener listener: mListeners)
          listener.onRawRotate(rotate);
      }

      public void onRawBattery(int battery)
      {
        for (ImuListener listener: mListeners)
          listener.onRawBattery(battery);
      }
    };
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

    // handle x value
    
    String xValue = m.group(X_GROUP);
    if (xValue != null)
      mListener.onRawX(Integer.valueOf(xValue));

    // handle y value
    
    String yValue = m.group(Y_GROUP);
    if (yValue != null)
      mListener.onRawY(Integer.valueOf(yValue));

    // handle z value
    
    String zValue = m.group(Z_GROUP);
    if (zValue != null)
      mListener.onRawZ(Integer.valueOf(zValue));

    // handle b value
    
    String bValue = m.group(B_GROUP);
    if (bValue != null)
      mListener.onRawBattery(Integer.valueOf(bValue));
    
    // handle r value
    
    String rValue = m.group(R_GROUP);
    if (rValue != null)
      mListener.onRawRotate(Integer.valueOf(rValue));
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
