package org.trebor.kinamp;

import static org.trebor.kinamp.GraphView.State.*;
import static java.lang.String.format;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
  private int mWidth;
  private int mHeight;
  private GraphLines<Integer> mLines;
  private State mState;
  private Thread mPaintThread;
  
  public enum State
  {
    SHOWING, HIDDEN;
  }
  
  public GraphView(Context context, AttributeSet attrs)
  {
    super(context, attrs);

    // assume view is hidden
    
    setState(HIDDEN);
    
    // register our interest in hearing about changes to our surface

    mSurfaceHolder = getHolder();
    mSurfaceHolder.addCallback(this);
    mPaintThread = new Thread()
    {
      @Override
      public void run()
      {
        runPaintThread();
      }

    };
  }

  private void runPaintThread()
  {
    while (getState() == SHOWING)
    {
      try
      {
        synchronized (mLines)
        {
          mLines.wait();
        }
        paint();
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public void registerSample(int color, float sample)
  {
    synchronized (mSurfaceHolder)
    {
      if (null != mLines)
        mLines.register(color, sample);
    }
  }

  public void repaint()
  {
    synchronized (mLines)
    {
      mLines.notify();
    }
 }
  
  private void paint()
  {
    if (getState() != SHOWING)
      return;

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

  private void paint(Canvas canvas)
  {
    Paint paint = new Paint();
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(Color.BLACK);
    canvas.drawRect(canvas.getClipBounds(), paint);
    
    //RectF oval = new RectF(0, 0, mWidth, mHeight);
    //canvas.drawOval(oval, paint);
    
    //paint.setColor(Color.GREEN);
    //canvas.drawCircle(50, 50, 25, paint);
    
    paint.setStyle(Style.STROKE);
    for (int color: mLines.getKeys())
    {
      paint.setColor(color);
      int x = 0;
      int old = Integer.MAX_VALUE;
      for (float value: mLines.getNormalized(color))
      {
        if (value < 0 || value > 1)
          Log.d(T, "value: " + value);
        
        int corrected = (int)(mHeight * value);
        if (old == Integer.MAX_VALUE)
          canvas.drawPoint(x, corrected, paint);
        else
          canvas.drawLine(x - 1, old, x, corrected, paint);
        
        ++x;
        old = corrected;
      }
    }
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
      //setState(hasWindowFocus
//        ? SHOWING
//        : HIDDEN);
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
      mHeight = height;
      mWidth = width;
      mLines = new GraphLines<Integer>(mWidth);
      setState(SHOWING);
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
      setState(HIDDEN);
    }
  }

  public void setState(State state)
  {
    mState = state;
    if (mState == SHOWING)
      mPaintThread.start();
  }

  public State getState()
  {
    return mState;
  }

}
