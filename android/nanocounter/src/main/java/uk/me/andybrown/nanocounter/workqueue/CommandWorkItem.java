// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.Command;
import uk.me.andybrown.nanocounter.commands.CommandResponse;


/*
 * Work item that will be sending a command, reading a response
 */

public abstract class CommandWorkItem extends WorkItem {

  /*
   * Constructor
   */

  protected CommandWorkItem(BluetoothService service) {
    super(service);
  }


  /*
   * Send a command and process the response
   */

  protected void sendCommand(Command command) throws Exception {

    CommandResponse response;
    String errorText;

    response=_service.sendCommand(command);

    // if the command returned an error then process as an exception

    if((errorText=response.getErrorText())!=null)
      throw new Exception(errorText);

    // notify the command response

    _service.notifyCommandResponse(response);
  }
}
