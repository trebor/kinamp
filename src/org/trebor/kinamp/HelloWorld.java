package org.trebor.kinamp;

//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelloWorld extends Activity
{
  // Intent request codes

  // private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  // private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
  // private static final int REQUEST_ENABLE_BT = 3;

  private Button mBeep;
  private MediaPlayer mPlayer;
  private BluetoothAdapter mBluetoothAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setupBlueTooth();

    setContentView(R.layout.main);
    mBeep = (Button)findViewById(R.id.beep);
    mPlayer =
      MediaPlayer.create(HelloWorld.this.getApplicationContext(),
        R.raw.sonar_ping);

    mBeep.setOnClickListener(new OnClickListener()
    {
      public void onClick(View v)
      {
        if (mPlayer.isPlaying())
          mPlayer.stop();
        mPlayer.start();
      }
    });
  }

  private void setupBlueTooth()
  {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter == null)
      throw new Error("Bluetooth not supported.");

    if (!mBluetoothAdapter.isEnabled())
      throw new Error("Bluetooth not enabled.");
  }

//  private static final UUID MY_UUID = UUID.randomUUID();
  
/*
  private class AcceptThread extends Thread
  {
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread()
    {
      // Use a temporary object that is later assigned to mmServerSocket,
      // because mmServerSocket is final
      BluetoothServerSocket tmp = null;
      try
      {
        // MY_UUID is the app's UUID string, also used by the client code
        tmp =
          mBluetoothAdapter.listenUsingRfcommWithServiceRecord("kinamp",
            MY_UUID);
      }
      catch (IOException e)
      {
      }
      mmServerSocket = tmp;
    }

    public void run()
    {
      BluetoothSocket socket = null;
      // Keep listening until exception occurs or a socket is returned
      while (true)
      {
        try
        {
          socket = mmServerSocket.accept();
        }
        catch (IOException e)
        {
          break;
        }
        // If a connection was accepted

        if (socket != null)
        {
          // Do work to manage the connection (in a separate thread)
          manageConnectedSocket(socket);

          try
          {
            mmServerSocket.close();
          }
          catch (IOException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          break;
        }
      }
    }

    private void manageConnectedSocket(BluetoothSocket socket)
    {
    }

    // Will cancel the listening socket, and cause the thread to finish
     * 
    public void cancel()
    {
      try
      {
        mmServerSocket.close();
      }
      catch (IOException e)
      {
      }
    }
  }

  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket)
    {
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      // Get the input and output streams, using temp objects because
      // member streams are final
      try
      {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      }
      catch (IOException e)
      {
      }

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run()
    {
      byte[] buffer = new byte[1024]; // buffer store for the stream
      int bytes; // bytes returned from read()

      // Keep listening to the InputStream until an exception occurs
      while (true)
      {
        try
        {
          // Read from the InputStream
          bytes = mmInStream.read(buffer);
          // Send the obtained bytes to the UI Activity
          mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
            .sendToTarget();
        }
        catch (IOException e)
        {
          break;
        }
      }
    }

    // Call this from the main Activity to send data to the remote device 
    public void write(byte[] bytes)
    {
      try
      {
        mmOutStream.write(bytes);
      }
      catch (IOException e)
      {
      }
    }

    // Call this from the main Activity to shutdown the connection
    public void cancel()
    {
      try
      {
        mmSocket.close();
      }
      catch (IOException e)
      {
      }
    }
  }
  */
}