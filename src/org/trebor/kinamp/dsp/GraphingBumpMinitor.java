package org.trebor.kinamp.dsp;

import org.trebor.kinamp.GraphLine;
import org.trebor.kinamp.GraphView;
import org.trebor.kinamp.Imu.Dimension;
import org.trebor.kinamp.dsp.BumpMonitorEvent.BumpType;

import android.graphics.Color;

public class GraphingBumpMinitor extends BumpMonitor
{
  public static final int CLR_1ST = Color.RED;
  public static final int CLR_2ND = Color.GREEN;
  public static final int CLR_3RD = Color.BLUE;
  public static final int CLR_ZERO = Color.rgb(192, 192, 192);
  public static final int CLR_EDGE1 = Color.rgb(255, 100, 100);
  public static final int CLR_EDGE2 = Color.rgb(100, 100, 255);
  
  private GraphView mView;
  private float mBumped;
  
  public GraphingBumpMinitor(GraphView view, Dimension dimension, float bumpThreshold,
    float smoothing, final Action<BumpMonitorEvent> action)
  {
    super(dimension, bumpThreshold, smoothing, action);
    mView = view;
    mBumped = 0;
  }
  
  @Override
  public void sample(float value)
  {
    super.sample(value);
    mView.registerSample(CLR_1ST, mOldFirstD);
    mView.registerSample(CLR_3RD, mOldSecondD);
    mView.registerSample(CLR_EDGE1, getBumpThreshold());
    mView.registerSample(CLR_EDGE2, -getBumpThreshold());
    mView.registerSample(CLR_ZERO, mBumped);
    mBumped = 0;
  }
  
  @Override
  public void execute(BumpMonitorEvent event)
  {
    super.execute(event);
    mBumped = event.getType() == BumpMonitorEvent.BumpType.UP ? GraphLine.UP_LINE : GraphLine.DN_LINE;
  }
}
