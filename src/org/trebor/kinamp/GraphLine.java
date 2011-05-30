package org.trebor.kinamp;

import java.util.Iterator;

class GraphLine implements Iterable<Float> 
{
  private Float mMin = null;
  private Float mMax = null;
  private Float[] mData;
  private int mIndex = 0;
  private final int mLength;
  
  public GraphLine(int length)
  {
    mLength = length;
    mData = new Float[mLength];
  }
  
  public boolean add(float value)
  {
    boolean change = false;
    
    Float oldValue = mData[mIndex];
    mData[mIndex] = value;
    mIndex = (mIndex + 1) % mLength;
    
    if (oldValue == mMin)
    {
      change = true;
      findMin();
    }
    if (oldValue == mMin)
    {
      change = true;
      findMax();
    }
    
    return change;
  }

  private void findMin()
  {
    for (Float value: this)
      if (value.compareTo(mMin) < 0)
        mMin = value;
  }
  
  private void findMax()
  {
    for (Float value: this)
      if (value.compareTo(mMax) > 0)
        mMax = value;
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
      
      public boolean hasNext()
      {
        return index != mIndex && mData[index] != null;
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