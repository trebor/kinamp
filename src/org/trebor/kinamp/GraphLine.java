package org.trebor.kinamp;

import java.util.Iterator;

public class GraphLine implements Iterable<Float> 
{
  public static final float UP_LINE = Float.POSITIVE_INFINITY;
  public static final float DN_LINE = Float.NEGATIVE_INFINITY;
  public static final float VERTICAL_LINE = Float.NaN;

  private Float mMin = Float.MAX_VALUE;
  private Float mMax = Float.MIN_VALUE;
  private Float[] mData;
  private int mIndex = 0;
  private final int mLength;
  
  public GraphLine(int length)
  {
    mLength = length;
    mData = new Float[mLength];
  }
  
  public boolean add(float newValue)
  {
    boolean change = false;
    Float value = newValue;
    Float oldValue = mData[mIndex];
    mData[mIndex] = value;
    mIndex = (mIndex + 1) % mLength;

    if (!isSpecial(value))
    {
      if (value <= mMin)
      {
        change = true;
        mMin = value;
      }

      if (value >= mMax)
      {
        change = true;
        mMax = value;
      }
    }

    if (oldValue == mMin)
    {
      change = true;
      mMin = null;
      findMin();
    }
    if (oldValue == mMax)
    {
      change = true;
      mMax = null;
      findMax();
    }

    return change;
  }

  private void findMin()
  {
    for (Float value: this)
      if (!isSpecial(value) && (mMin == null || value.compareTo(mMin) < 0))
        mMin = value;
  }
  
  private void findMax()
  {
    for (Float value: this)
        if (!isSpecial(value) && (mMax == null || value.compareTo(mMax) > 0))
          mMax = value;
  }
  
  public static boolean isSpecial(float value)
  {
    return value == VERTICAL_LINE || value == UP_LINE || value == DN_LINE;
  }
  
  public Float getMin()
  {
    return mMin;
  }

  public Float getMax()
  {
    return mMax;
  }

  public Iterator<Float> iterator()
  {
    return new Iterator<Float>()
    {
      int index = (mIndex + 1) % mLength;
      
      {
        while (mData[index] == null && index != mIndex)
          index = (index + 1) % mLength;
      }
      
      public boolean hasNext()
      {
        return index != mIndex;
      }

      public Float next()
      {
        Float value = mData[index];
        index = (index + 1) % mLength;
        return value;
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}