package org.trebor.kinamp.audio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.trebor.kinamp.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class NoiseBox
{
  private final SoundPool mSoundPool;
  private final Context mContext;
  private final Map<Sound, Integer> mSoundMap;
  
  public enum Sound
  {
    PING(R.raw.sonar_ping),
    COWBELL1(R.raw.cowbell1),
    COWBELL2(R.raw.cowbell2),
    DRUM1(R.raw.drum1),
    TINKLE(R.raw.tinkle),
    BLIP2(R.raw.blip2),
    BRDGBTN7(R.raw.brdgbtn7),
    FEMEEK2(R.raw.femeek2),
    FLOOP(R.raw.floop),
    NICEBEEP(R.raw.nicebeep),
    TREKWHST(R.raw.trekwhst),
    TREKDOOR(R.raw.trekdoor),
    TING(R.raw.ting);
    
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
    mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
    mSoundMap = new HashMap<Sound, Integer>();
    for (Sound sound: Sound.values())
      mSoundMap.put(sound, mSoundPool.load(mContext, sound.getResourceId(), 1));
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
    mSoundPool.play(mSoundMap.get(sound), leftLevel, rightLevel, 1, 0, 1f);
    return null;
  }
}
