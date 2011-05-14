package org.trebor.kinamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
  
  private final BufferedReader mSource;
  private final ImuListener mListener;
  private final Pattern mLinePatter;
  private Thread mProcessThread;
  
  public Imu(InputStream imuDataStream, ImuListener listener)
  {
    mSource = new BufferedReader(new InputStreamReader(imuDataStream));
    mListener = listener;
    mProcessThread = null;
    mLinePatter = Pattern.compile(LINE_REGEX);
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
    Matcher m = mLinePatter.matcher(line);
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
  
  public void stop()
  {
    synchronized (mProcessThread)
    {
      mProcessThread.notifyAll();
      mProcessThread = null;
    }
  }
}
