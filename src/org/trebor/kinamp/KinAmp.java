package org.trebor.kinamp;

import static org.trebor.kinamp.NoiseBox.Sound.*;
import static org.trebor.kinamp.Imu.Dimension.*;
import static org.trebor.kinamp.Imu.Mode.*;
import static org.trebor.kinamp.Imu.GravityRange.*;
import static org.trebor.kinamp.R.id.*;

import java.util.HashMap;
import java.util.Map;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.GravityRange;

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
  private static final int GRAVITY_BAR_RANGE = 100;

  @SuppressWarnings("unused")
  private Loggable log;

 // device name

  public static final String WII_TILT_DEVICE_NAME = "FireFly-AAF0";

  // UI elements
  
  private Button mBeep;
  private ToggleButton mRawToggle;
  private ToggleButton mGravityToggle;
  private ToggleButton mGravityRange;
  private TextView mOutput;
  private ScrollView mOutputScroll;
  private Map<Dimension, DimensionUi> mUiMap;
  
  // the big stuff
  
  private BlueTooth mBluetooth;
  private NoiseBox mNoiseBox;
  private Imu mImu;

  class DimensionUi
  {
    public SeekBar mSeekBar;
    public TextView mMin;
    public TextView mValue;
    public TextView mMax;
    public Range mRange;

    public DimensionUi(int seekBarId, int minId, int valueId,
      int maxId)
    {
      mSeekBar = (SeekBar)findViewById(seekBarId);
      mMin = (TextView)findViewById(minId);
      mValue = (TextView)findViewById(valueId);
      mMax = (TextView)findViewById(maxId);
      mRange = new Range();
    }
  }
  
  class Range
  {
    private float mMin;
    private float mMax;

    public Range()
    {
      reset();
    }
    
    private void reset()
    {
      mMin = Float.MAX_VALUE;
      mMax = Float.MIN_VALUE;
    }

    public void register(float sample)
    {
      if (sample > mMax)
        mMax = sample;
      
      if (sample < mMin)
        mMin = sample;
    }
    
    public float normal(float sample)
    {
      register(sample);
      
      if (sample == mMin && sample == mMax)
        return 0;

      return (sample - mMin) / (mMax - mMin);
    }

    public float getMin()
    {
      return mMin;
    }

    public float getMax()
    {
      return mMax;
    }
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    mNoiseBox = new NoiseBox(this, getApplicationContext());

    setContentView(R.layout.main);
    mBeep = (Button)findViewById(R.id.beep);
    mRawToggle = (ToggleButton)findViewById(R.id.rawToggle);
    mGravityToggle = (ToggleButton)findViewById(R.id.gravityToggle);
    mGravityRange = (ToggleButton)findViewById(R.id.gravityRange);
    mOutput = (TextView)findViewById(R.id.output);
    mOutputScroll = (ScrollView)findViewById(R.id.scrollView1);

    mUiMap = new HashMap<Dimension, DimensionUi>();
    
    mUiMap.put(X_AXIS, new DimensionUi(seekBarX, minX, valueX, maxX));
    mUiMap.put(Y_AXIS, new DimensionUi(seekBarY, minY, valueY, maxY));
    mUiMap.put(Z_AXIS, new DimensionUi(seekBarZ, minZ, valueZ, maxZ));

    log = this;

    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        ping();
      }
    });

    mRawToggle.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        if (mRawToggle.isChecked())
        {
          for (DimensionUi ui: mUiMap.values())
          {
            ui.mRange.reset();
            ui.mSeekBar.setMax(0);
            ui.mMin.setText("0");
          }
          
          mGravityToggle.setEnabled(false);
          mGravityRange.setEnabled(false);
          mImu.start(RAW, getGravityRange());
        }
        else
        {
          mGravityToggle.setEnabled(true);
          mGravityRange.setEnabled(true);
          mImu.stop();
        }
      }
    });

    mGravityToggle.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        if (mGravityToggle.isChecked())
        {
          for (DimensionUi ui: mUiMap.values())
          {
            ui.mRange.reset();
            ui.mSeekBar.setMax(GRAVITY_BAR_RANGE);
          }
          
          mRawToggle.setEnabled(false);
          mGravityRange.setEnabled(false);
          mImu.start(GRAVITY, getGravityRange());
        }
        else
        {
          mRawToggle.setEnabled(true);
          mGravityRange.setEnabled(true);
          mImu.stop();
        }
      }
    });

    mBluetooth = new BlueTooth(WII_TILT_DEVICE_NAME, this);
    mImu = new Imu(mBluetooth.getInputStream(), mBluetooth.getOutputStream(), this);
    mImu.addListner(new ImuListener()
    {
      public void onRaw(final Dimension dimension, final int value)
      {
        final DimensionUi ui = mUiMap.get(dimension);
        if (null == ui)
          return;

        executeOnUi(new Runnable()
        {
          public void run()
          {
            ui.mRange.register(value);
            ui.mSeekBar.setMax((int)ui.mRange.getMax());
            ui.mMax.setText("" + ui.mRange.getMax());

            ui.mValue.setText("" + value);
            ui.mSeekBar.setProgress(value);
          }
        });
      }

      public void onGravity(final Dimension dimension, final float value)
      {
        final DimensionUi ui = mUiMap.get(dimension);
        if (null == ui)
          return;

        executeOnUi(new Runnable()
        {
          public void run()
          {
            final float normal = ui.mRange.normal(value);

            ui.mMax.setText("" + ui.mRange.getMax());
            ui.mValue.setText("" + value);
            ui.mMin.setText("" + ui.mRange.getMin());
            ui.mSeekBar.setProgress((int)(normal * GRAVITY_BAR_RANGE));
          }
        });
      }
    });
  }

  public GravityRange getGravityRange()
  {
    return mGravityRange.isChecked() ? HIGH_GRAVITY : LOW_GRAVITY;
  }
  
  public void ping()
  {
    mNoiseBox.play(PING, 1);
  }


  abstract class SelfHanlder extends Handler
  {
    public void start()
    {
      sendEmptyMessage(0);
    }
    
    @Override
    public void handleMessage(Message msg)
    {
      execute();
    }
    
    abstract public void execute();
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
  
  Handler handleRunable = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      Runnable action = (Runnable)msg.obj;
      action.run();
    }
  };
  
  private void executeOnUi(Runnable action)
  {
    handleRunable.sendMessage(handleRunable.obtainMessage(0, action));
  }
  
  private String log(String format, Object...args)
  {
     final String message = String.format(format + "\n", args);
    executeOnUi(new Runnable()
    {
      public void run()
      {
        mOutput.append(message);
        mOutputScroll.fullScroll(ScrollView.FOCUS_DOWN);
      }
    });
     
     
//     Message m = handleMessage.obtainMessage(0, message);
//     handleMessage.sendMessage(m);
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