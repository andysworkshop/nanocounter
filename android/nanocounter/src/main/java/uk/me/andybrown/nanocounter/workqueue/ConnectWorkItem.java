package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;


/*
 * Connect to the remote end
 */

public class ConnectWorkItem extends WorkItem {


  /*
   * Constructor
   */

  public ConnectWorkItem(BluetoothService service) {
    super(service);
  }


  /*
   * Make the connection
   */

  @Override
  public void doWork() {
    _service.openConnection();
  }
}
