package org.trebor.kinamp.audio;

import java.util.ArrayList;
import java.util.List;

import org.trebor.kinamp.audio.NoiseBox.Sound;

public class SoundSeries
{
  private final List<Sound> mSounds;
  private int mNext = 0;
  
  public SoundSeries(Sound... sounds)
  {
    mSounds = new ArrayList<Sound>();
    for (Sound sound: sounds)
      mSounds.add(sound);
  }
  
  public void add(Sound sound)
  {
    mSounds.add(sound);
  }

  public Sound next()
  {
    int current = mNext;
    mNext = (mNext + 1) % mSounds.size();
    return mSounds.get(current);
  }
}
