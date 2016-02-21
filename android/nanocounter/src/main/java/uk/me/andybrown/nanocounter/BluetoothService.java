// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import uk.me.andybrown.nanocounter.commands.Command;
import uk.me.andybrown.nanocounter.commands.CommandResponse;
import uk.me.andybrown.nanocounter.workqueue.WorkItem;
import uk.me.andybrown.nanocounter.workqueue.WorkQueue;


/*
 * Application subclass
 */

public class BluetoothService extends Service {

  private static final String LOGGER=BluetoothService.class.getName();

  /*
   * Requests are a fixed size
   */

  private static final int REQUEST_BYTE_SIZE = 25;


  /*
   * Bluetooth adaptor and receiver
   */

  protected BluetoothSocket _socket;
  protected InputStream _socketInput;
  protected OutputStream _socketOutput;
  protected BluetoothAdapter _bluetoothAdapter;
  protected BroadcastReceiver _receiver;
  protected IBinder _binder;
  protected volatile LinkStatus _linkStatus;

  protected final WorkQueue _workQueue=new WorkQueue(this);


  /*
   * Create override
   */

  @Override
  public void onCreate() {

    IntentFilter ifilter;

    Log.d(LOGGER,"Creating the bluetooth service");

    // create the binder

    _binder=new BluetoothServiceBinder(this);

    // start the work queue

    _workQueue.start();

    // set members

    ifilter=new IntentFilter();
    ifilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    ifilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    ifilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

    // create the broadcast receiver for bluetooth events

    _receiver=new BroadcastReceiver() {
      @Override
      public void onReceive(Context context,Intent intent) {

        switch(intent.getAction()) {

          case BluetoothAdapter.ACTION_STATE_CHANGED:

            switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)) {

              case BluetoothAdapter.STATE_OFF:
                closeConnection();
                setLinkStatus(LinkStatus.DISABLED);
                break;

              case BluetoothAdapter.STATE_ON:
                setLinkStatus(LinkStatus.ENABLED);
                break;
            }
            break;

          case BluetoothDevice.ACTION_ACL_DISCONNECTED:
            setLinkStatus(LinkStatus.ENABLED);
            break;

          case BluetoothDevice.ACTION_ACL_CONNECTED:
            setLinkStatus(LinkStatus.CONNECTED);
            break;
        }
      }
    };

    registerReceiver(_receiver,ifilter);

    // create adaptor

    if((_bluetoothAdapter=BluetoothAdapter.getDefaultAdapter())==null)
      setLinkStatus(LinkStatus.NOT_SUPPORTED);
    else if(_bluetoothAdapter.isEnabled())
      setLinkStatus(LinkStatus.ENABLED);
    else
      setLinkStatus(LinkStatus.DISABLED);
  }


  /*
   * onCreate override
   */

  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {

    Log.d(LOGGER,"Starting the bluetooth service");
    return START_STICKY;
  }


  /*
   * Service is being destroyed
   */

  @Override
  public void onDestroy() {

    Log.d(LOGGER,"Destroying the bluetooth service");

    // close any socket connection

    closeConnection();

    // unregister intent receiver

    if(_receiver!=null)
      unregisterReceiver(_receiver);
  }


  /*
   * Service bind
   */

  @Override
  public IBinder onBind(Intent intent) {
    Log.d(LOGGER,"Binding to the bluetooth service");
    return _binder;
  }


  /*
   * All clients unbound
   */

  @Override
  public boolean onUnbind(Intent intent) {
    Log.d(LOGGER,"Unbinding from the bluetooth service");
    closeConnection();
    return false;
  }


  /*
   * Close the connection socket
   */

  protected void closeConnection() {

    if(_socket!=null) {

      Log.i(LOGGER,"Closing the bluetooth connection");

      try {
        _socket.close();
      }
      catch(IOException e) {
      }

      _socket=null;
    }
  }


  /*
   * Open a connection to the device: do not call directly!
   */

  public void openConnection() {

    BluetoothDevice device;
    Method m;

    Log.i(LOGGER,"Initiating a new connection");

    // check if in a connectable state

    if(_linkStatus==LinkStatus.CONNECTED ||
            _linkStatus==LinkStatus.CONNECTING ||
            _linkStatus==LinkStatus.NOT_SUPPORTED ||
            _linkStatus==LinkStatus.DISABLED) {

      Log.d(LOGGER,"Unable to make a connection while link status is "+_linkStatus.toString());
      return;
    }

    // get the paired device

    if((device=getPairedDevice())==null) {
      Log.i(LOGGER,"The device is not paired, cannot connect");
      setLinkStatus(LinkStatus.NOT_PAIRED);
      return;
    }

    setLinkStatus(LinkStatus.CONNECTING);

    // use reflection to call the connect method that doesn't require android on the other end

    try {
      m=device.getClass().getMethod("createRfcommSocket",new Class[]{int.class});
      _socket=(BluetoothSocket)m.invoke(device,1);

      // connect to the device (blocking) throws exception on fail

      _socket.connect();

      _socketInput=_socket.getInputStream();
      _socketOutput=_socket.getOutputStream();

      Log.i(LOGGER,"Connection successful");
    }
    catch(Exception ex) {
      Log.e(LOGGER,"Failed to make the connection: "+ex.toString());
      setLinkStatus(LinkStatus.CONNECTION_FAILED);
    }
  }


  /*
   * Check if we're paired with the oven
   */

  protected BluetoothDevice getPairedDevice() {

    String deviceName;

    // get the device name from the preferences object or default to HC-06

    deviceName=Preferences.getBluetoothId(this);
    Log.d(LOGGER,"Searching for device name "+deviceName);

    if(_bluetoothAdapter!=null) {

      for(BluetoothDevice device : _bluetoothAdapter.getBondedDevices()) {

        Log.d(LOGGER,"Found device: "+device.getName());

        if(device.getName().equals(deviceName)) {
          Log.d(LOGGER,"Device name matches, using it");
          return device;
        }
      }

    }

    Log.e(LOGGER,"Device not found");
    return null;
  }


  /*
   * Set a new link status
   */
  protected void setLinkStatus(LinkStatus newStatus) {

    Log.d(LOGGER,"Changing link status to "+newStatus.toString());

    if(_linkStatus!=newStatus) {
      _linkStatus=newStatus;
      notifyLinkStatus();
    }
  }


  /*
   * Notify a new link status
   */

  protected void notifyLinkStatus() {

    Intent intent;

    intent=new Intent(CustomIntent.LINK_STATUS);
    intent.putExtra(CustomIntent.LINK_STATUS_EXTRA,_linkStatus.ordinal());

    sendBroadcast(intent);
  }


  /*
   * Notify that the command has responded
   */

  public void notifyCommandResponse(CommandResponse response) {

    Intent intent;

    intent=new Intent(CustomIntent.COMMAND_RESPONSE);
    intent.putExtra(CustomIntent.COMMAND_RESPONSE_EXTRA,response);

    sendBroadcast(intent);
  }


  /*
   * Add a new item to the work queue
   */

  public void addWorkItem(WorkItem item) {
    _workQueue.add(item);
  }


  /*
   * Get the link status
   */

  public LinkStatus getLinkStatus() {
    return _linkStatus;
  }

  /*
   * Send a command, receive the response
   */

  public CommandResponse sendCommand(Command command) throws Exception {

    byte[] outbytes;
    int i;

    Log.i(LOGGER,"Sending command "+command.getClass().getSimpleName());

    // check the status

    if(_linkStatus!=LinkStatus.CONNECTED) {
      Log.e(LOGGER,"Unable to send command while link status is "+_linkStatus.toString());
      throw new Exception("Command cannot be send because I am not connected to Nanocounter");
    }

    // serialize the command

    outbytes=command.getBytes();

    // write what we've got

    if(_socketOutput==null)
      throw new Exception("Command cannot be sent because the socket has not been created yet");

    _socketOutput.write(outbytes);

    // pad out to the fixed command size

    for(i=0;i<REQUEST_BYTE_SIZE-outbytes.length;i++)
      _socketOutput.write(0);

    Log.d(LOGGER,"Command written to the bluetooth device, waiting for response");

    // create a command response object from the response stream

    return CommandResponse.instanceOf(_socketInput);
  }
}
