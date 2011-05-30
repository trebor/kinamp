package org.trebor.kinamp;

import org.trebor.kinamp.dsp.Dsp;

import android.app.Activity;
import android.os.Bundle;

public class Graph extends Activity
{
  private GraphView mGraphView;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.graph);
    
    // get handles to the LunarView from XML, and its LunarThread
    
    mGraphView = (GraphView)findViewById(R.id.graph);
  }
  
  public void setDsp(Dsp dsp)
  {
    mGraphView.setDsp(dsp);
  }
}
