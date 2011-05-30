package org.trebor.kinamp;

import static org.trebor.kinamp.NoiseBox.Sound.*;
import static org.trebor.kinamp.Imu.Dimension.*;
import static org.trebor.kinamp.Imu.Mode.*;
import static org.trebor.kinamp.Imu.GravityRange.*;
import static org.trebor.kinamp.R.id.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.Imu.GravityRange;
import org.trebor.kinamp.dsp.Action;
import org.trebor.kinamp.dsp.BumpMonitor;
import org.trebor.kinamp.dsp.Dsp;

import android.app.Activity;
import android.content.Intent;
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

public class KinAmp extends Activity
{
 // constants

  public static final String WII_TILT_DEVICE_NAME = "FireFly-AAF0";
  private static final int GRAVITY_BAR_RANGE = 100;

  private boolean mDisableBlueTooth = false;
  
  // UI elements
  
  private Button mBeep;
  private ToggleButton mRawToggle;
  private ToggleButton mGravityToggle;
  private ToggleButton mDegreeToggle;
  private ToggleButton mGravityRange;
  private TextView mOutput;
  private ScrollView mOutputScroll;
  private Map<Dimension, DimensionUi> mUiMap;
  
  // the big stuff
  
  private BlueTooth mBluetooth;
  private NoiseBox mNoiseBox;
  private Imu mImu;
  private Dsp mDsp;

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
    setContentView(R.layout.main);

    mNoiseBox = new NoiseBox(getApplicationContext());
    mBeep = (Button)findViewById(R.id.beep);
    mRawToggle = (ToggleButton)findViewById(R.id.rawToggle);
    mGravityToggle = (ToggleButton)findViewById(R.id.gravityToggle);
    mDegreeToggle = (ToggleButton)findViewById(R.id.degreeToggle);
    mGravityRange = (ToggleButton)findViewById(R.id.gravityRange);
    mOutput = (TextView)findViewById(R.id.output);
    mOutputScroll = (ScrollView)findViewById(R.id.scrollView1);

    mUiMap = new HashMap<Dimension, DimensionUi>();
    
    mUiMap.put(X_AXIS, new DimensionUi(seekBarX, minX, valueX, maxX));
    mUiMap.put(Y_AXIS, new DimensionUi(seekBarY, minY, valueY, maxY));
    mUiMap.put(Z_AXIS, new DimensionUi(seekBarZ, minZ, valueZ, maxZ));
    mGravityRange.setChecked(true);
    
    final DimensionUi unknown = new DimensionUi(seekBarUk, minUk, valueUk, maxUk);
    unknown.mSeekBar.setMax(100);
    
    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        //setContentView(R.layout.graph);        
        Intent graphIntent = new Intent(KinAmp.this, Graph.class);
        startActivityForResult(graphIntent, 0);
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
            ui.mSeekBar.setProgress(0);
            ui.mMin.setText("0");
          }
          
          mGravityToggle.setEnabled(false);
          mDegreeToggle.setEnabled(false);
          mGravityRange.setEnabled(false);
          mImu.start(RAW, getGravityRange());
        }
        else
        {
          mGravityToggle.setEnabled(true);
          mDegreeToggle.setEnabled(true);
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
            ui.mSeekBar.setProgress(0);
          }
          
          mRawToggle.setEnabled(false);
          mDegreeToggle.setEnabled(false);
          mGravityRange.setEnabled(false);
          mImu.start(GRAVITY, getGravityRange());
        }
        else
        {
          mRawToggle.setEnabled(true);
          mDegreeToggle.setEnabled(true);
          mGravityRange.setEnabled(true);
          mImu.stop();
        }
      }
    });

    mDegreeToggle.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        if (mDegreeToggle.isChecked())
        {
          for (DimensionUi ui: mUiMap.values())
          {
            ui.mRange.reset();
            ui.mSeekBar.setMax(360);
            ui.mSeekBar.setProgress(0);
          }
          
          mRawToggle.setEnabled(false);
          mGravityToggle.setEnabled(false);
          mGravityRange.setEnabled(false);
          mImu.start(DEGREE, getGravityRange());
        }
        else
        {
          mRawToggle.setEnabled(true);
          mGravityToggle.setEnabled(true);
          mGravityRange.setEnabled(true);
          mImu.stop();
        }
      }
    });

    if (mDisableBlueTooth)
      return;
    
    mBluetooth = new BlueTooth(WII_TILT_DEVICE_NAME);
    mImu = new Imu(mBluetooth.getInputStream(), mBluetooth.getOutputStream());
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

      public void onDegree(final Dimension dimension, final float value)
      {
        final DimensionUi ui = mUiMap.get(dimension);
        if (null == ui)
          return;

        executeOnUi(new Runnable()
        {
          public void run()
          {
             ui.mRange.register(value);

            ui.mMax.setText("" + ui.mRange.getMax());
            ui.mValue.setText("" + value);
            ui.mMin.setText("" + ui.mRange.getMin());
            ui.mSeekBar.setProgress((int)value);
          }
        });
      }
    });
    
    mDsp = new Dsp(mImu);
    mDsp.addMonitor(new BumpMonitor(Y_AXIS, new Action<BumpMonitor>()
    {
      float o1 = 0.5f;
      float o2 = 0.5f;
      AtomicBoolean p1 = null;
      AtomicBoolean p2 = null;

      public void execute(BumpMonitor monitor)
      {
        final float value = monitor.getFilterHistory();
        final float THRESH = 0.1f;
        
        final float n = unknown.mRange.normal(value);

        float d1 = n - o1;
        float d2 = o1 - o2;
        
        if (d1 > THRESH && d2 > THRESH && d1 > d2)
        {
//          if (p1 == null || p1.get())
            p1 = mNoiseBox.play(COWBELL1);
        }
        if (d1 < -THRESH && d2 < -THRESH && d1 > d2)
        {
//          if (p2 == null || p2.get())
            p2 = mNoiseBox.play(COWBELL2);
        }

        o2 = o1;
        o1 = n;
        
        executeOnUi(new Runnable()
        {
          public void run()
          {
//            unknown.mMax.setText(ff(d1));
//            unknown.mValue.setText(ff(n));
//            unknown.mMin.setText(ff(d2));

             unknown.mMax.setText(ff(unknown.mRange.getMax()));
             unknown.mValue.setText(ff(value));
             unknown.mMin.setText(ff(unknown.mRange.getMin()));
            unknown.mSeekBar.setProgress((int)(n * 100));
          }
        });
      }

      private String ff(float value)
      {
        return "" + (Math.floor(value * 100) / 100);
      }
    }));
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
}