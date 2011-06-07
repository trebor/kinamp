package org.trebor.kinamp;

import static org.trebor.kinamp.Imu.Dimension.*;

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
  @SuppressWarnings("unused")
  private final String T = "+" + getClass().getSimpleName().toString();

  public static final String RAW_LINE_REGEX = "^(X=([\\d]*))?(Y=([\\d]*))?(Z=([\\d]*))?(B=([\\d]*))?(R=([\\d]*))?$";
  public static final String GRAVITY_LINE_REGEX = "^(X=([\\d\\.\\-]*))?(Y=([\\d\\.\\-]*))?(Z=([\\d\\.\\-]*))?(B=([\\d\\.\\-]*))?(R=([\\d\\-]*))?$";
  public static final String DEGREE_LINE_REGEX = "^(X=([\\d]*))?(Y=([\\d]*))?(R=([\\d\\-]*))?(B=([\\d\\.]*))?$";

  public static final int NO_GROUP = -1;
  
  public static final int X_GROUP = 2;
  public static final int Y_GROUP = 4;
  public static final int Z_GROUP = 6;
  public static final int B_GROUP = 8;
  public static final int R_GROUP = 10;

  public static final int X_GROUP_DEGREE_MODE = 2;
  public static final int Y_GROUP_DEGREE_MODE = 4;
  public static final int R_GROUP_DEGREE_MODE = 6;
  public static final int B_GROUP_DEGREE_MODE = 8;
  public static final int Z_GROUP_DEGREE_MODE = NO_GROUP;
  
  public static final char MAIN_MENU = ' ';
  public static final char START_DETECTION = '1';
  public static final char SENSOR_RANGE_MENU = '4';
  public static final char MODE_MENU = '5';
  public static final char GRAVITY_MODE = '1';
  public static final char RAW_MODE = '2';
  public static final char BINARY_MODE = '3';
  public static final char DEGREE_MODE = '4';
  public static final char RANGE_1_5G_MODE = '1';
  public static final char RANGE_6_0G_MODE = '2';

  public enum GravityRange
  {
    LOW_GRAVITY(RANGE_1_5G_MODE, 1.5f), HIGH_GRAVITY(RANGE_6_0G_MODE, 6.0f);
    
    final private float mRange;
    final private char[] mRangeCommand;
    
    GravityRange(char rangeCommand, float range)
    {
      mRangeCommand = new char[]{MAIN_MENU, SENSOR_RANGE_MENU, rangeCommand};
      mRange = range;
    }
    
    public char[] getConfigureCommand()
    {
      return mRangeCommand;
    }

    public float getRange()
    {
      return mRange;
    }
  }
  
  public enum Mode
  {
    RAW(RAW_LINE_REGEX, RAW_MODE, false) {
      protected void signal(Dimension dimension, String value,
        List<ImuListener> listeners, GravityRange range)
      {
        int i = Integer.valueOf(value);
        for (ImuListener listener: listeners)
          listener.onRaw(dimension, i);
      }
    }, GRAVITY(GRAVITY_LINE_REGEX, GRAVITY_MODE, false) {
      protected void signal(Dimension dimension, String value,
        List<ImuListener> listeners, GravityRange range)
      {
        float f = Float.valueOf(value);
        
        float r = range.getRange();
        
        if ((f < -r || f > r) && (dimension == X_AXIS || dimension == Y_AXIS || dimension == Z_AXIS))
          f = Math.max(Math.min(f, r), -r);
        
        for (ImuListener listener: listeners)
          listener.onGravity(dimension, f);
      }
    }, DEGREE(DEGREE_LINE_REGEX, DEGREE_MODE, true) {
      protected void signal(Dimension dimension, String value,
        List<ImuListener> listeners, GravityRange range)
      {
        float f = Float.valueOf(value);
        
        for (ImuListener listener: listeners)
          listener.onDegree(dimension, f);
      }
    };
    
    
    private final Pattern mPattern;
    private final char[] mStartCommand;
    private final char[] mStopCommand;
    private final boolean mDegreeMode;
    
    Mode(String lineRegex, char modeSelect, boolean degreeMode)
    {
      mPattern = Pattern.compile(lineRegex);
      mStartCommand = new char[]{MAIN_MENU, MODE_MENU, modeSelect, START_DETECTION};
      mStopCommand = new char[]{MAIN_MENU};
      mDegreeMode = degreeMode;
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
    
    public void processLine(final String line, final GravityRange range,
      final List<ImuListener> listeners)
    {
      Matcher m = parse(line);
      if (!m.find())
        return;

      for (Dimension dimension : Dimension.values())
      {
        int parseGroup = dimension.getParseGroup(mDegreeMode);
        if (parseGroup == NO_GROUP)
          continue;

        String value = m.group(parseGroup);
        if (null == value)
          continue;
        
        signal(dimension, value, listeners, range);
      }
    }

    protected abstract void signal(Dimension dimension, String value,
      List<ImuListener> listeners, GravityRange range);
  }
  
  public enum Dimension
  {
    X_AXIS(X_GROUP, X_GROUP_DEGREE_MODE, "X"), Y_AXIS(Y_GROUP,
      Y_GROUP_DEGREE_MODE, "Y"), Z_AXIS(Z_GROUP, Z_GROUP_DEGREE_MODE, "Z"),
    BATTARY(B_GROUP, B_GROUP_DEGREE_MODE, "BATTARY"), ROTATION(R_GROUP,
      R_GROUP_DEGREE_MODE, "ROTATE");

    private final int mParseGroup;
    private final int mParseGroupDegreeMode;
    private final String mName;

    Dimension(int parseGroup, int parseGroupDegreeMode, String name)
    {
      mParseGroup = parseGroup;
      mParseGroupDegreeMode = parseGroupDegreeMode;
      mName = name;
    }

    public int getParseGroup(boolean inDegreeMode)
    {
      return inDegreeMode ? mParseGroupDegreeMode : mParseGroup;
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
  private GravityRange mRange;
  private Thread mProcessThread;

  public Imu(InputStream inputStream, OutputStream outputStream)
  {
    mSource = new BufferedReader(new InputStreamReader(inputStream));
    mSink = new BufferedWriter(new OutputStreamWriter(outputStream));
    mListeners = new ArrayList<ImuListener>();
    mProcessThread = new Thread()
    {
      public void run()
      {
        processImuData();
      }
    };

    mMode = Mode.RAW;
    stop();
    mProcessThread.start();
  }

  public void addListner(ImuListener listener)
  {
    mListeners.add(listener);
  }
  
  protected void processImuData()
  {
    try
    {
      while (null != mProcessThread || mSource.ready())
      {
        try
        {
          String line = mSource.readLine();
          if (null != line)
            mMode.processLine(line, mRange, mListeners);
          else
            stop();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public void start(Mode mode, GravityRange range)
  {
    start(mode, range, false);
  }
  
  public void start(Mode mode, GravityRange range, boolean block)
  {
    // set mode and range

    mMode = mode;
    mRange = range;

    // start the data

    startData();

    // start the process thread and block if required

    if (block)
    {
      try
      {
        synchronized (mProcessThread)
        {
          mProcessThread.wait();
        }
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }

  public void stop()
  {
    stopData();
    synchronized (mProcessThread)
    {
      mProcessThread.notify();
    }
  }

  private void startData()
  {
    sendCommand(mRange.getConfigureCommand());
    sendCommand(mMode.getStartCommand());
  }
  
  private void stopData()
  {
    sendCommand(mMode.getStopCommand());
  }

  private void sendCommand(char[] command)
  {
    synchronized (mProcessThread)
    {
      try
      {
        mSink.write(command);
        mSink.flush();
        mProcessThread.wait(100);
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
}
