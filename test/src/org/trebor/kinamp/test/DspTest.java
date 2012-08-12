package org.trebor.kinamp.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.trebor.kinamp.dsp.AbstractMonitor;
import org.trebor.kinamp.dsp.Dsp;
import org.trebor.kinamp.imu.Imu;

public class DspTest 
{
  public static String[] GRAVITY_OUTPUT =
  {
    "X=-0.078Y=0.027Z=-1.000B=3.976R=-002\n",
    "Y=-0.059Z=-1.000B=3.976R=-000\n",
    "X=-0.085Z=-1.000B=3.976R=-003\n",
    "X=-0.062Y=-0.141B=3.970R=000\n",
    "X=-0.023Y=-0.039Z=-1.000R=-001\n",
    "X=-0.023Y=-0.043Z=-0.999B=3.988\n",
    "\n",
    "R=-002\n",
  };

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })

  @Test
  public void testMonitorBasics()
  {
    final AtomicInteger xCount = new AtomicInteger();
    final AtomicInteger rCount = new AtomicInteger();
    Imu imu = createImu(GRAVITY_OUTPUT);
    Dsp dsp = new Dsp(imu);
    System.out.format("================\n");
    dsp.addMonitor(new AbstractMonitor(Imu.Mode.GRAVITY, Imu.Dimension.X_AXIS, null)
    {
      public void sample(float value)
      {
        xCount.getAndIncrement();
      }
    });
    dsp.addMonitor(new AbstractMonitor(Imu.Mode.GRAVITY, Imu.Dimension.ROTATION, null)
    {
      public void sample(float value)
      {
        rCount.getAndIncrement();
      }
    });
    System.out.println("imu pre start");
    imu.start(Imu.Mode.GRAVITY, Imu.GravityRange.HIGH_GRAVITY, true);
    System.out.println("imu post start");
    assertEquals(5, xCount.get(), 0);
    assertEquals(6, rCount.get(), 0);
    System.out.println("dsp done!");
  }

  
  public Imu createImu(String[] inputs)
  {
    StringBuffer buffer = new StringBuffer();
    for (String line : inputs)
      buffer.append(line);

    System.out.println("pre imu create");
    InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
    Imu imu = new Imu(is, new OutputStream()
    {
      public void write(int oneByte) throws IOException
      {
      }
    });
    System.out.println("post imu create");
    
    return imu;
  }
}
