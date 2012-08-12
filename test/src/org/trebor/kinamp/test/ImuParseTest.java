package org.trebor.kinamp.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.trebor.kinamp.imu.Imu;
import org.trebor.kinamp.imu.ImuListener;
import org.trebor.kinamp.imu.Imu.Dimension;

public class ImuParseTest
{
  public static String[] RAW_OUTPUT =
  {
    "X=466Y=552Z=687B=625R=492\n",
    "Y=522Z=708B=628R=495\n",
    "X=500Z=718B=628R=490\n",
    "X=503Y=526B=627R=490\n",
    "X=486Y=518Z=708R=482\n",
    "X=470Y=524Z=673B=624\n",
    "\n",
    "R=479\n"
  };

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

  public static String[] DEGREE_OUTPUT =
  {
    "X=00356Y=00352R=-000B=4.014\n",
    "Y=00356R=-000B=4.020\n",
    "X=00350R=-000B=4.014\n",
    "X=00352Y=00358B=4.020\n",
    "X=00348Y=00360R=-000\n",
    "\n",
    "R=002\n",
  };

  public static Float[][] EXPECTED_GRAVITY_VALUES =
  {
    {
      -0.078f, 0.027f, -1.000f, 3.976f, -002f
    },
    {
      null, -0.059f, -1.000f, 3.976f, -000f
    },
    {
      -0.085f, null, -1.000f, 3.976f, -003f
    },
    {
      -0.062f, -0.141f, null, 3.970f, 000f
    },
    {
      -0.023f, -0.039f, -1.000f, null, -001f
    },
    {
      -0.023f, -0.043f, -0.999f, 3.988f, null
    },
    {
      null, null, null, null, null
    },
    {
      null, null, null, null, -002f
    },
  };

  public static Integer[][] EXPECTED_RAW_VALUES =
  {
    {
      466, 552, 687, 625, 492
    },
    {
      null, 522, 708, 628, 495
    },
    {
      500, null, 718, 628, 490
    },
    {
      503, 526, null, 627, 490
    },
    {
      486, 518, 708, null, 482
    },
    {
      470, 524, 673, 624, null
    },
    {
      null, null, null, null, null
    },
    {
      null, null, null, null, 479
    },
  };

  public static Float[][] EXPECTED_DEGREE_VALUES =
  {
    {
      00356f, 00352f, -000f, 4.014f
    },
    {
      null, 00356f, -000f, 4.020f
    },
    {
      00350f, null, -000f, 4.014f
    },
    {
      00352f, 00358f, null, 4.020f
    },
    {
      00348f, 00360f, -000f, null
    },
    {
      null, null, null, null
    },
    {
      null, null, 002f, null
    },
  };
  
  public static Float[][] EXPECTED_DEGREE_VALUES_END_TO_END =
  {
    {
      00356f, 00352f, 4.014f, -000f
    },
    {
      null, 00356f, 4.020f, -000f
    },
    {
      00350f, null, 4.014f, -000f
    },
    {
      00352f, 00358f, null, 4.020f
    },
    {
      00348f, 00360f, -000f, null
    },
    {
      null, null, null, null
    },
    {
      null, null, 002f, null
    },
  };

  public static int[] GROUP_INDEXES =
  {
    Imu.X_GROUP, Imu.Y_GROUP, Imu.Z_GROUP, Imu.B_GROUP, Imu.R_GROUP
  };

  public static int[] DEGREE_MODE_GROUP_INDEXES =
  {
    Imu.X_GROUP_DEGREE_MODE, Imu.Y_GROUP_DEGREE_MODE, Imu.R_GROUP_DEGREE_MODE, Imu.B_GROUP_DEGREE_MODE
  };

  @Test
  public void testRawParser()
  {
    String pattern = Imu.RAW_LINE_REGEX;
    Pattern p = Pattern.compile(pattern);

    int row = 0;
    for (String line : RAW_OUTPUT)
    {
      Matcher m = p.matcher(line);

      assertTrue(m.find());

      for (int i = 0; i < GROUP_INDEXES.length; ++i)
      {
        int groupIndex = GROUP_INDEXES[i];
        Integer x = m.group(groupIndex) == null
          ? null
          : Integer.valueOf(m.group(groupIndex));
        Integer ex = EXPECTED_RAW_VALUES[row][i];

        assertTrue(
          String.format("row %d value %s: %s != %s", row,
            m.group(groupIndex - 1), ex, x),
          (ex == null && x == null) || (ex.intValue() == x.intValue()));
      }
      ++row;
    }
  }

  @Test
  public void testFloat()
  {
    String source = "foo-0.059bar";
    Pattern p = Pattern.compile("^foo([\\d\\.\\-]*)bar$");
    Matcher m = p.matcher(source);

    assert (m.find());
    for (int i = 0; i < m.groupCount(); ++i)
      System.out.format("group[%d]: %s", i + 1, m.group(i + 1));
  }
  
  @Test
  public void testGravityParser()
  {
    String pattern = Imu.GRAVITY_LINE_REGEX;
    Pattern p = Pattern.compile(pattern);

    int row = 0;
    for (String line : GRAVITY_OUTPUT)
    {
      Matcher m = p.matcher(line);

      assertTrue(m.find());

      for (int i = 0; i < GROUP_INDEXES.length; ++i)
      {
        int groupIndex = GROUP_INDEXES[i];
        Float value = m.group(groupIndex) == null
          ? null
          : Float.valueOf(m.group(groupIndex));
        Float expected = EXPECTED_GRAVITY_VALUES[row][i];

        assertTrue(
          String.format("row %d value %s: %s != %s", row,
            m.group(groupIndex - 1), expected, value),
          (expected == null && value == null) ||
            (expected.floatValue() == value.floatValue()));
      }
      ++row;
    }
  }

  @Test
  public void testDegreeParser()
  {
    String pattern = Imu.DEGREE_LINE_REGEX;
    Pattern p = Pattern.compile(pattern);

    int row = 0;
    for (String line : DEGREE_OUTPUT)
    {
      Matcher m = p.matcher(line);

      assertTrue(m.find());

      for (int i = 0; i < DEGREE_MODE_GROUP_INDEXES.length; ++i)
      {
        int groupIndex = DEGREE_MODE_GROUP_INDEXES[i];
        Float value = m.group(groupIndex) == null
          ? null
          : Float.valueOf(m.group(groupIndex));
        Float expected = EXPECTED_DEGREE_VALUES[row][i];

        assertTrue(
          String.format("row %d value %s: %s != %s", row,
            m.group(groupIndex - 1), expected, value),
          (expected == null && value == null) ||
            (expected.floatValue() == value.floatValue()));
      }
      ++row;
    }
  }

  @Test
  public void testEndToEndRaw()
  {
    final AtomicInteger count = new AtomicInteger();
    ImuListener il = new ImuListener()
    {
      int row = 0;
      int group = 0;

      private int getExpectedValue()
      {
        Integer value;

        do
        {
          value = EXPECTED_RAW_VALUES[row][group];
          group = (group + 1) % EXPECTED_RAW_VALUES[row].length;
          if (group == 0)
            ++row;
        }
        while (value == null);

        System.out.format("value[%d][%d]: %s\n", row, group, value);

        return value.intValue();
      }

      public void onRaw(Dimension dimension, int value)
      {
        assertEquals(getExpectedValue(), value, 0);
        count.getAndIncrement();
      }

      public void onGravity(Dimension dimension, float value)
      {
        assert (false);
      }

      public void onDegree(Dimension dimension, float value)
      {
        assert (false);
      }
    };

    StringBuffer buffer = new StringBuffer();
    for (String line : RAW_OUTPUT)
      buffer.append(line);

    System.out.println(buffer.toString());
    InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
    Imu imu = new Imu(is, new OutputStream()
    {
      public void write(int oneByte) throws IOException
      {
      }
    });
    imu.addListner(il);
    imu.start(Imu.Mode.RAW, Imu.GravityRange.HIGH_GRAVITY, true);
    assertEquals(26, count.get(), 0);
    System.out.println("raw done!");
  }

  @Test
  public void testEndToEndGravity()
  {
    final AtomicInteger count = new AtomicInteger();
    ImuListener il = new ImuListener()
    {
      int row = 0;
      int group = 0;

      private float getExpectedValue()
      {
        Float value;

        do
        {
          value = EXPECTED_GRAVITY_VALUES[row][group];
          group = (group + 1) % EXPECTED_GRAVITY_VALUES[row].length;
          if (group == 0)
            ++row;
        }
        while (value == null);

        System.out.format("value[%d][%d]: %s\n", row, group, value);

        return value.floatValue();
      }

      public void onGravity(Dimension dimension, float value)
      {
        assertEquals(getExpectedValue(), value, 0);
        count.getAndIncrement();
      }

      public void onRaw(Dimension dimension, int value)
      {
        assert (false);
      }

      public void onDegree(Dimension dimension, float value)
      {
        assert (false);
      }
    };

    StringBuffer buffer = new StringBuffer();
    for (String line : GRAVITY_OUTPUT)
      buffer.append(line);

    System.out.println(buffer.toString());
    InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
    Imu imu = new Imu(is, new OutputStream()
    {
      public void write(int oneByte) throws IOException
      {
      }
    });
    imu.addListner(il);
    imu.start(Imu.Mode.GRAVITY, Imu.GravityRange.HIGH_GRAVITY, true);
    assertEquals(26, count.get(), 0);
    System.out.println("gravity done!");
  }

  @Test
  public void testEndToEndDegree()
  {
    final AtomicInteger count = new AtomicInteger();
    ImuListener il = new ImuListener()
    {
      int row = 0;
      int group = 0;

      private float getExpectedValue()
      {
        Float value;

        do
        {
          value = EXPECTED_DEGREE_VALUES_END_TO_END[row][group];
          group = (group + 1) % EXPECTED_DEGREE_VALUES_END_TO_END[row].length;
          if (group == 0)
            ++row;
        }
        while (value == null);

        System.out.format("value[%d][%d]: %s\n", row, group, value);

        return value.floatValue();
      }

      public void onDegree(Dimension dimension, float value)
      {
        assertEquals(getExpectedValue(), value, 0);
        count.getAndIncrement();
      }

      public void onRaw(Dimension dimension, int value)
      {
        assert (false);
      }

      public void onGravity(Dimension dimension, float value)
      {
        assert (false);
      }
    };

    StringBuffer buffer = new StringBuffer();
    for (String line : DEGREE_OUTPUT)
      buffer.append(line);

    System.out.println(buffer.toString());
    InputStream is = new ByteArrayInputStream(buffer.toString().getBytes());
    Imu imu = new Imu(is, new OutputStream()
    {
      public void write(int oneByte) throws IOException
      {
      }
    });
    imu.addListner(il);
    imu.start(Imu.Mode.DEGREE, Imu.GravityRange.HIGH_GRAVITY, true);
    assertEquals(17, count.get(), 0);
    System.out.println("degree done!");
  }
}
