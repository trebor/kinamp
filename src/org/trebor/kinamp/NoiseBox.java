package org.trebor.kinamp;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.media.MediaPlayer;

public class NoiseBox
{
  private final Context mContext;

  public enum Sound
  {
    PING(R.raw.sonar_ping),
    COWBELL1(R.raw.cowbell1),
    COWBELL2(R.raw.cowbell2),
    DRUM1(R.raw.drum1),
    TINKLE(R.raw.tinkle);
    
    private final int mResourceId;
    
    Sound(int resourceId)
    {
      mResourceId = resourceId;
    }
    
    public int getResourceId()
    {
      return mResourceId;
    }
  }
  
  public NoiseBox(Context context)
  {
    mContext = context;
  }
  
  public AtomicBoolean play(Sound sound)
  {
    return play(sound, 1, 1);
  }
  
  public AtomicBoolean play(Sound sound, float level)
  {
    return play(sound, level, level);
  }
  
  public AtomicBoolean play(final Sound sound, final float leftLevel, final float rightLevel)
  {
    final MediaPlayer player = MediaPlayer.create(mContext, sound.getResourceId());
    final AtomicBoolean isDone = new AtomicBoolean(false);
    
    new Thread()
    {
      public void run()
      {
        //logger.debug("start: %d", sound.getResourceId());
        player.setVolume(leftLevel, rightLevel);
        player.start();
        synchronized (player)
        {
          try
          {
            while (player.isPlaying())
              player.wait(100);
          }
          catch (InterruptedException e)
          {
            e.printStackTrace();
          }
        }
        player.release();
        isDone.set(true);
        
        //logger.debug("end: %d", sound.getResourceId());
      }
    }.start();
    
    return isDone;
  }
}
