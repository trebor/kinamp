package org.trebor.kinamp;

import static org.trebor.kinamp.R.id.*;
import static org.trebor.kinamp.audio.NoiseBox.Sound.*;
import static org.trebor.kinamp.imu.Imu.Dimension.*;
import static org.trebor.kinamp.imu.Imu.GravityRange.*;
import static org.trebor.kinamp.imu.Imu.Mode.*;
import static org.trebor.kinamp.dsp.BumpMonitorEvent.BumpType.*;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.trebor.kinamp.audio.NoiseBox;
import org.trebor.kinamp.audio.SoundSeries;
import org.trebor.kinamp.dsp.Action;
import org.trebor.kinamp.dsp.BumpMonitor;
import org.trebor.kinamp.dsp.BumpMonitorEvent;
import org.trebor.kinamp.dsp.Dsp;
import org.trebor.kinamp.dsp.DspUtil;
import org.trebor.kinamp.dsp.RawMonitor;
import org.trebor.kinamp.dsp.RawMonitorEvent;
import org.trebor.kinamp.graph.GraphView;
import org.trebor.kinamp.graph.GraphingBumpMinitor;
import org.trebor.kinamp.imu.Imu;
import org.trebor.kinamp.imu.ImuListener;
import org.trebor.kinamp.imu.Imu.Dimension;
import org.trebor.kinamp.imu.Imu.GravityRange;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

@SuppressWarnings("unused")
public class KinAmp extends Activity
{
  private static final String FLOAT_FORMAT = "%6.4f";

  private final String T = "+" + getClass().getSimpleName().toString();

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
  private Map<Dimension, DimensionUi> mUiMap;
  private GraphView mGraphView;
  
  // the big stuff
  
  private BlueTooth mBluetooth;
  private NoiseBox mNoiseBox;
  private Imu mImu;
  private Dsp mDsp;

  // dimention color lookup  thable
  
  @SuppressWarnings("serial")
  public static final Map<Dimension, Integer> DIMENTION_COLOR_MAP =
    new HashMap<Dimension, Integer>()
    {
      {
        put(Dimension.X_AXIS, Color.rgb(255, 0, 255));
        put(Dimension.Y_AXIS, Color.rgb(0, 255, 255));
        put(Dimension.Z_AXIS, Color.rgb(255, 255, 0));
      }
    };

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
    mGraphView = (GraphView)findViewById(R.id.graph);

    mUiMap = new HashMap<Dimension, DimensionUi>();

    mUiMap.put(X_AXIS, new DimensionUi(seekBarX, minX, valueX, maxX));
    mUiMap.put(Y_AXIS, new DimensionUi(seekBarY, minY, valueY, maxY));
    mUiMap.put(Z_AXIS, new DimensionUi(seekBarZ, minZ, valueZ, maxZ));
    mGravityRange.setChecked(true);

    final DimensionUi unknown =
      new DimensionUi(seekBarUk, minUk, valueUk, maxUk);
    unknown.mSeekBar.setMax(100);

    configureClickListeners();

    if (mDisableBlueTooth)
      return;

    mBluetooth = new BlueTooth(WII_TILT_DEVICE_NAME);
    mImu = new Imu(mBluetooth.getInputStream(), mBluetooth.getOutputStream());

    // configureSliderUpdater();


    final SoundSeries testSet = new SoundSeries(DRUM1, FLOOP, NICEBEEP, TING, TREKWHST, FEMEEK2);
    
    float thresh = 0.10f;
    mDsp = new Dsp(mImu);
    mDsp.addMonitor(new BumpMonitor(X_AXIS, thresh, 0.0f,
      new Action<BumpMonitorEvent>()
      {
        public void execute(BumpMonitorEvent event)
        {
          if (event.getType() == UP)
            mNoiseBox.play(testSet.next(), event.getAmplitude());
        }
      }));
    
    mDsp.addMonitor(new BumpMonitor(Y_AXIS, thresh, 0.0f,
      new Action<BumpMonitorEvent>()
      {
        public void execute(BumpMonitorEvent event)
        {
          mNoiseBox.play(event.getType() == UP
            ? COWBELL1
            : COWBELL2, event.getAmplitude());
        }
      }));
  }

  private void configureClickListeners()
  {
    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        mNoiseBox.play(PING);
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
  }

  private void configureSliderUpdater()
  {
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
  
  
  private final Handler mHandleRunable = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
      ((Runnable)msg.obj).run();
    }
  };
  
  private void executeOnUi(Runnable action)
  {
    mHandleRunable.sendMessage(mHandleRunable.obtainMessage(0, action));
  }
}