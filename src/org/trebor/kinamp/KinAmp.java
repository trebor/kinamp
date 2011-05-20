package org.trebor.kinamp;

import static org.trebor.kinamp.NoiseBox.Sound.*;
import static org.trebor.kinamp.Imu.Dimension.*;

import java.util.HashMap;
import java.util.Map;

import org.trebor.kinamp.Imu.Dimension;

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
  private Map<Dimension, SeekBar> mSeekBarMap;
  private Map<Dimension, Integer> mMaxMap;
  
  // the big stuff
  
  private BlueTooth mBluetooth;
  private NoiseBox mNoiseBox;
  private Imu mImu;
  
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

    mMaxMap = new HashMap<Dimension, Integer>();
    mSeekBarMap = new HashMap<Dimension, SeekBar>();
    mSeekBarMap.put(X_AXIS, mSeekX);
    mSeekBarMap.put(Y_AXIS, mSeekY);
    mSeekBarMap.put(Z_AXIS, mSeekZ);

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
    mImu.addListner(new ImuListener()
    {
      public void onRaw(Dimension dimension, int value)
      {
        SeekBar seekBar = mSeekBarMap.get(dimension);
        if (null != seekBar)
        {
          Integer max = mMaxMap.get(dimension);
          if (null == max || value > max)
          {
            mMaxMap.put(dimension, value);
            seekBar.setMax(value);
          }
            
          seekBar.setProgress(value);
        }
      }
    });

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