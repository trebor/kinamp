package org.trebor.kinamp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BlueTooth
{
  private final Loggable log;
  private BluetoothAdapter mBluetoothAdapter;
  private final String mDeviceName;
  private BluetoothDevice mDevice;
  private BluetoothSocket mSocket;
  private static final UUID mUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  
  public BlueTooth(String deviceName, Loggable loggable)
  {
    mDeviceName = deviceName;
    log = loggable;
    init();
  }

  private void init()
  {
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter == null)
      throw new Error("Bluetooth not supported.");

    if (!mBluetoothAdapter.isEnabled())
      throw new Error("Bluetooth is not enabled.");
    
    try
    {
      mDevice = getDevice(mDeviceName);
      log.debug("device: %s (%s) (%s)", mDevice.getName(), mDevice.getAddress(), mDevice.getBondState());
      log.debug("device: %s", mDevice);
      mSocket = mDevice.createRfcommSocketToServiceRecord(mUuid);
      mSocket.connect();
      //log.debug("socket: %s", mSocket.get
    }
    catch (IOException e)
    {
      throw new Error("unable to connect to: " + mDeviceName, e);
    }
  }
  
  public InputStream getInputStream()
  {
    InputStream is = null;
    try
    {
      if (null != mSocket)
        is = mSocket.getInputStream();
    }
    catch (IOException e)
    {
      throw new Error("unable to get input stream from: " + mDeviceName, e);
    }
    
    return is;
  }
  
  public OutputStream getOutputStream()
  {
    OutputStream os = null;
    try
    {
      if (null != mSocket)
        os = mSocket.getOutputStream();
    }
    catch (IOException e)
    {
      throw new Error("unable to get outstream stream from: " + mDeviceName, e);
    }
    
    return os;
  }

  private BluetoothDevice getDevice(String name)
  {
      for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices())
        if (device.getName().equals(name))
          return device;
      
      // no device found
      
      return null;
  }
}
