package org.trebor.kinamp.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.trebor.kinamp.graph.GraphLine;

public class GraphLineTest
{
  @Test
  public void testGraphLine()
  {
    GraphLine line = new GraphLine(3);
    line.add(1);
    assertEquals(1, line.getMin(), 0);
    assertEquals(1, line.getMax(), 0);
    line.add(2);
    assertEquals(1, line.getMin(), 0);
    assertEquals(2, line.getMax(), 0);
    line.add(-1);
    assertEquals(-1, line.getMin(), 0);
    assertEquals(2, line.getMax(), 0);
    line.add(0);
    assertEquals(-1, line.getMin(), 0);
    assertEquals(2, line.getMax(), 0);
    line.add(0);
    assertEquals(-1, line.getMin(), 0);
    assertEquals(0, line.getMax(), 0);
    line.add(3);
    assertEquals(0, line.getMin(), 0);
    assertEquals(3, line.getMax(), 0);
  }
}
