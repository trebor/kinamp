package org.trebor.kinamp;

import static org.trebor.kinamp.NoiseBox.Sound.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class KinAmp extends Activity implements Loggable
{
  @SuppressWarnings("unused")
  private Loggable log;

 // device name

  public static final String WII_TILT_DEVICE_NAME = "FireFly-AAF0";

  // UI elements
  
  private Button mBeep;
  private ToggleButton mImuToggle;
  private SeekBar mSeekX;
  private SeekBar mSeekY;
  private SeekBar mSeekZ;
  private TextView mOutput;
  private ScrollView mOutputScroll;
  
  // the big stuff
  
  private BlueTooth mBluetooth;
  private NoiseBox mNoiseBox;
  private Imu mImu;
  
  private ImuListener mDebugListener = new ImuListener()
  {
    public void onRawX(int x)
    {
      setX(x);
    }

    public void onRawY(int y)
    {
      setY(y);
    }

    public void onRawZ(int z)
    {
      setZ(z);
    }

    public void onRawRotate(int rotate)
    {
      //log.debug("rotate: %d", rotate);
    }

    public void onRawBattery(int battery)
    {
      //log.debug("battery: %d", battery);
    }
  };

  private int xMax = 0;
  private int xHistory = 0;
  private int yMax = 0;
  private int zMax = 0;
  
  private int lowPass(int history, int sample, float filter)
  {
    return (int)(history * filter + sample * (1 - filter));
  }  
  
  public void setX(int value)
  {
    value = lowPass(xHistory, value, 0.99f);
    
    if (value > xMax)
      mSeekX.setMax(xMax = value);
    if (value > 800)
      mNoiseBox.play(COWBELL1);
    mSeekX.setProgress(value);
  }
  
  public void setY(int value)
  {
    if (value > yMax)
      mSeekY.setMax(yMax = value);
      
    mSeekY.setProgress(value);
  }

  public void setZ(int value)
  {
    if (value > zMax)
      mSeekZ.setMax(zMax = value);
    mSeekZ.setProgress(value);
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    mNoiseBox = new NoiseBox(this, getApplicationContext());

    setContentView(R.layout.main);
    mBeep = (Button)findViewById(R.id.beep);
    mImuToggle = (ToggleButton)findViewById(R.id.imuToggle);
    mOutput = (TextView)findViewById(R.id.output);
    mOutputScroll = (ScrollView)findViewById(R.id.scrollView1);
    mSeekX = (SeekBar)findViewById(R.id.seekBarX);
    mSeekY = (SeekBar)findViewById(R.id.seekBarY);
    mSeekZ = (SeekBar)findViewById(R.id.seekBarZ);

    log = this;

    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        ping();
      }
    });

    mImuToggle.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        if (mImuToggle.isChecked())
          mImu.start(false);
        else
          mImu.stop();
      }
    });

    mBluetooth = new BlueTooth(WII_TILT_DEVICE_NAME, this);
    mImu = new Imu(mBluetooth.getInputStream(), mBluetooth.getOutputStream());
    mImu.addListner(mDebugListener);
  }
  
  public void ping()
  {
    mNoiseBox.play(PING, 1);
  }

  Handler handleMessage = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      String message = (String)msg.obj;
      mOutput.append(message);
      mOutputScroll.fullScroll(ScrollView.FOCUS_DOWN);
    }
  };

  private String log(String format, Object...args)
  {
     String message = String.format(format + "\n", args);
     Message m = handleMessage.obtainMessage(0, message);
     handleMessage.sendMessage(m);
    return message;
  }
  
  public String debug(String format, Object... args)
  {
    return log(format, args);
  }
  
  public String error(String format, Object... args)
  {
    return log("ERROR: " + format, args);
  }
  
  public String error(Throwable exception, String format, Object... args)
  {
    return log("ERROR - " + exception.getMessage() + ": " + format, args);
  }
}