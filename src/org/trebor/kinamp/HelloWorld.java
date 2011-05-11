package org.trebor.kinamp;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelloWorld extends Activity
{
  private Button mBeep;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    mBeep = (Button)findViewById(R.id.beep);
    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        MediaPlayer player =
          MediaPlayer.create(HelloWorld.this.getApplicationContext(),
            R.raw.sonar_ping);
        player.start();
      }
    });
  }
}