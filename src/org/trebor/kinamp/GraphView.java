package org.trebor.kinamp;

import static org.trebor.kinamp.GraphView.State.*;
import static java.lang.String.format;

import org.trebor.kinamp.dsp.Dsp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * View that draws, takes keystrokes, etc. for a simple LunarLander game.
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing
 * the current ship physics. All x/y etc. are measured with (0,0) at the
 * lower left. updatePhysics() advances the physics based on realtime.
 * draw() renders the ship, and does an invalidate() to prompt another
 * draw() as soon as possible by the system.
 */

public class GraphView extends SurfaceView implements SurfaceHolder.Callback
{
  private final String T = "+" + getClass().getSimpleName().toString();
  
  private SurfaceHolder mSurfaceHolder;
  private int mCanvasWidth;
  private int mCanvasHeight;
  private GraphLines<Integer> mLines;
  private Dsp mDsp;
  private State mState;
  
  public enum State
  {
    SHOWING, HIDDEN;
  }
  
  public GraphView(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    // assume view is hidden
    
    mState = HIDDEN;
    
    // register our interest in hearing about changes to our surface

    mSurfaceHolder = getHolder();
    mSurfaceHolder.addCallback(this);
  }

  public void registerSample(int color, float sample)
  {
    synchronized (mSurfaceHolder)
    {
      if (null != mLines)
        mLines.register(color, sample);
    }
  }

  //@Override
  private void paint(Canvas canvas)
  {
    //canvas.s
    Paint paint = new Paint();
    paint.setStyle(Style.FILL);
    paint.setColor(Color.RED);
    paint.setAntiAlias(true);
    
    Log.d(T, format("w: %d h: %d", mCanvasWidth, mCanvasHeight));
    RectF oval = new RectF(0, 0, mCanvasWidth, mCanvasHeight);
    canvas.drawOval(oval, paint);
    
    paint.setColor(Color.GREEN);
    canvas.drawCircle(50, 50, 25, paint);
    // Draw the ship with its current rotation
    // canvas.save();
    // canvas.rotate((float) mHeading, (float) mX, mCanvasHeight- (float)
    // mY);
    // canvas.restore();
  }

  /**
   * Standard window-focus override. Notice focus lost so we can pause on
   * focus lost. e.g. user switches to take a call.
   */

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus)
  {
    synchronized (mSurfaceHolder)
    {
      mState = hasWindowFocus
        ? SHOWING
        : HIDDEN;
    }
  }

  /* Callback invoked when the surface dimensions change. */
  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
    int height)
  {
    synchronized (mSurfaceHolder)
    {
      Log.d(T, format("surfaceChanged: %d, %d", width, height));
      mCanvasHeight = height;
      mCanvasWidth = width;
      mState = SHOWING;
      repaint();
    }
  }

  public void surfaceCreated(SurfaceHolder holder)
  {
    synchronized (mSurfaceHolder)
    {
      Log.d(T, "surfaceCreated");
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    synchronized (mSurfaceHolder)
    {
      Log.d(T, "surfaceDestroyed");
      mState = HIDDEN;
    }
  }

  public void repaint()
  {
    Canvas c = null;
    try
    {
      c = mSurfaceHolder.lockCanvas(null);
      synchronized (mSurfaceHolder)
      {
        paint(c);
      }
    }
    finally
    {
      if (c != null)
        mSurfaceHolder.unlockCanvasAndPost(c);
    }
  }

  public void setDsp(Dsp dsp)
  {
    mDsp = dsp;
  }
}
