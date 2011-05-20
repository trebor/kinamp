package org.trebor.kinamp;

import android.content.Context;
import android.media.MediaPlayer;

public class NoiseBox
{
  private final Loggable logger;
  private final Context mContext;

  public enum Sound
  {
    PING(R.raw.sonar_ping),
    COWBELL1(R.raw.cowbell1),
    COWBELL2(R.raw.cowbell2);
    
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
  
  public NoiseBox(Loggable logger, Context context)
  {
    this.logger = logger;
    mContext = context;
  }
  
  public void play(Sound sound)
  {
    play(sound, 1, 1);
  }
  
  public void play(Sound sound, float level)
  {
    play(sound, level, level);
  }
  
  public void play(final Sound sound, final float leftLevel, final float rightLevel)
  {
    new Thread()
    {
      public void run()
      {
        MediaPlayer player = MediaPlayer.create(mContext, sound.getResourceId());
        logger.debug("start: %d", sound.getResourceId());
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
        logger.debug("end: %d", sound.getResourceId());
      }
    }.start();
  }
}
