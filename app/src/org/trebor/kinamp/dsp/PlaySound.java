package org.trebor.kinamp.dsp;

import org.trebor.kinamp.R;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class PlaySound extends Service
{
  // originally from
  // http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
  // and modified by Steve Pomeroy <steve@staticfree.info>

  private final int mSeconds = 3; // seconds
  private final int mSampleRate = 8000;
  private final int mSampleCount = mSeconds * mSampleRate;
  private final double mWaveFrom[] = new double[mSampleCount];
  private final double mToneFequency = 440; // hz

  private final byte mGeneratedSnd[] = new byte[2 * mSampleCount];

  private final Handler mHandler = new Handler();

  // @Override
  // public void onCreate(Bundle savedInstanceState) {
  // super.onCreate(savedInstanceState);
  // setContentView(R.layout.main);
  // }

  @Override
  protected void onResume()
  {
    super.onResume();

  }

  /*
   * (non-Javadoc)
   * @see android.app.Service#onCreate()
   */
  public void onCreate()
  {
    // TODO Auto-generated method stub
    super.onCreate();
  }

  /*
   * (non-Javadoc)
   * @see android.app.Service#onDestroy()
   */
  public void onDestroy()
  {
    // TODO Auto-generated method stub
    super.onDestroy();
  }

  void genTone()
  {
    // fill out the array
    for (int i = 0; i < mSampleCount; ++i)
    {
      mWaveFrom[i] = Math.sin(2 * Math.PI * i / (mSampleRate / mToneFequency));
    }

    // convert to 16 bit pcm sound array
    // assumes the sample buffer is normalised.
    int idx = 0;
    for (final double dVal : mWaveFrom)
    {
      // scale to maximum amplitude
      final short val = (short)((dVal * 32767));
      // in 16 bit wav PCM, first byte is the low order byte
      mGeneratedSnd[idx++] = (byte)(val & 0x00ff);
      mGeneratedSnd[idx++] = (byte)((val & 0xff00) >>> 8);
    }
  }

  void playSound()
  {
    final AudioTrack audioTrack =
      new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate,
        AudioFormat.CHANNEL_CONFIGURATION_MONO,
        AudioFormat.ENCODING_PCM_16BIT, mSampleCount, AudioTrack.MODE_STATIC);
    audioTrack.write(mGeneratedSnd, 0, mGeneratedSnd.length);
    audioTrack.play();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    int result = super.onStartCommand(intent, flags, startId);
    
    // Use a new tread as this can take a while
    final Thread thread = new Thread(new Runnable()
    {
      public void run()
      {
        genTone();
        mHandler.post(new Runnable()
        {
          public void run()
          {
            playSound();
          }
        });
      }
    });
    thread.start();
    
    return result;
  }

  @Override
  public IBinder onBind(Intent arg0)
  {
    return null;
  }
}
