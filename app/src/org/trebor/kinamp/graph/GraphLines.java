package org.trebor.kinamp.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GraphLines<Key>
{
  private final Map<Key, GraphLine> mLines;
  private final int mLength;
  private float mMin = Float.MAX_VALUE;
  private float mMax = Float.MIN_VALUE;

  public GraphLines(int length)
  {
    mLength = length;
    mLines = new HashMap<Key, GraphLine>();
  }
  
  public void register(Key key, float value)
  {
    GraphLine line = mLines.get(key);
    if (line == null)
    {
      line = new GraphLine(mLength);
      mLines.put(key, line);
    }

    if (line.add(value))
    {
      mMin = Float.MAX_VALUE;
      mMax = Float.MIN_VALUE;

      for (GraphLine l : mLines.values())
      {
        if (l.getMax() > mMax)
          mMax = l.getMax();
        if (l.getMin() < mMin)
          mMin = l.getMin();
      }
    }
  }

  public GraphLine getLine(Key key)
  {
    return mLines.get(key);
  }
  
  private float normal(float value)
  {
    return GraphLine.isSpecial(value) ? value : (value - mMin) / (mMax - mMin);
  }

  Iterable<Float> getNormalized(Key key)
  {
    GraphLine line = mLines.get(key);
    final Iterator<Float> iterator = line == null ? null : line.iterator();
    
    return new Iterable<Float>()
    {
      public Iterator<Float> iterator()
      {
        return new Iterator<Float>()
        {

          public boolean hasNext()
          {
            if (null == iterator)
              return false;
            return iterator.hasNext();
          }

          public Float next()
          {
            return normal(iterator.next());
          }

          public void remove()
          {
            iterator.remove();
          }
        };
      }
    };
  }

  public Set<Key> getKeys()
  {
    return mLines.keySet();
  }
}