package org.trebor.kinamp.dsp;

import static org.trebor.kinamp.imu.Imu.Mode.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.trebor.kinamp.imu.Imu;
import org.trebor.kinamp.imu.ImuListener;
import org.trebor.kinamp.imu.Imu.Dimension;
import org.trebor.kinamp.imu.Imu.Mode;

public class Dsp implements ImuListener
{
  private final Map<Mode, Map<Dimension, List<Monitor>>> mModeMonitorMap;
  
  public Dsp(Imu imu)
  {
    mModeMonitorMap = new HashMap<Mode, Map<Dimension, List<Monitor>>>();
    imu.addListner(this);
  }
  
  public void addMonitor(Monitor monitor)
  {
    Map<Dimension, List<Monitor>> dimensionMonitorsMap = mModeMonitorMap.get(monitor.getMode());
    if (null == dimensionMonitorsMap)
    {
      dimensionMonitorsMap = new HashMap<Dimension, List<Monitor>>();
      mModeMonitorMap.put(monitor.getMode(), dimensionMonitorsMap);
    }
    
    List<Monitor> monitors = dimensionMonitorsMap.get(monitor.getDimension());
    if (null == monitors)
    {
      monitors = new ArrayList<Monitor>();
      dimensionMonitorsMap.put(monitor.getDimension(), monitors);
    }
    
    monitors.add(monitor);
  }
  
  public void onRaw(Dimension dimension, int value)
  {
    processSample(RAW, dimension, value);
  }

  public void onGravity(Dimension dimension, float value)
  {
    processSample(GRAVITY, dimension, value);
  }

  public void onDegree(Dimension dimension, float value)
  {
    processSample(DEGREE, dimension, value);
  }
  
  private void processSample(Mode mode, Dimension dimension, float value)
  {
    Map<Dimension, List<Monitor>> dimensionMonitorsMap = mModeMonitorMap.get(mode);
    if (null == dimensionMonitorsMap)
      return;
    List<Monitor> monitors = dimensionMonitorsMap.get(dimension);
    if (null == monitors)
      return;
    for (Monitor monitor: monitors)
      monitor.sample(value);
  }
}
