// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.me.andybrown.nanocounter.CustomIntent;


/*
 * Work queue implementation
 */

public class WorkQueue implements Runnable {

  private static final String LOGGER=WorkQueue.class.getName();

  protected final BlockingQueue<WorkItem> _items=new LinkedBlockingQueue<>();
  protected final Context _context;
  protected Thread _thread;


  /*
   * Constructor
   */

  public WorkQueue(Context context) {
    _context=context;
  }


  /*
   * Start the queue
   */

  public void start() {

    Log.i(LOGGER,"Starting work queue");

    if(_thread==null) {
      _thread=new Thread(this);
      _thread.start();
    }
  }


  public void stop() {

    Log.i(LOGGER,"Stopping work queue");

    if(_thread!=null) {
      _thread.interrupt();
      _thread=null;
    }
  }


  /*
   * Add a new item
   */

  public void add(WorkItem item) {
    Log.d(LOGGER,"Adding "+item.getClass().getSimpleName()+" to work queue");
    _items.add(item);
  }


  /*
   * Process items in the queue
   */

  @Override
  public void run() {

    WorkItem item;
    Intent intent;

    Log.d(LOGGER,"The work queue thread has started");

    for(;;) {

      try {
        item=_items.take();
        Log.d(LOGGER,"Processing "+item.getClass().getSimpleName()+" from work queue");

        try {
          item.doWork();
        }
        catch(InterruptedException ex) {
          throw ex;
        }
        catch(Exception ex) {

          // failed to process the command, send a notification

          intent=new Intent(CustomIntent.COMMAND_FAILED);
          intent.putExtra(CustomIntent.COMMAND_FAILED_EXTRA,ex.getMessage());

          _context.sendBroadcast(intent);
        }

        Log.d(LOGGER,"Finished processing "+item.getClass().getSimpleName());
      }
      catch(InterruptedException ex) {
        Log.i(LOGGER,"Work queue thread interrupted - exiting");
        break;
      }
    }
  }
}
