package uk.me.andybrown.nanocounter.commands;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/*
 * Base class for commands
 */

public abstract class Command {

  private static final String LOGGER=Command.class.getName();


  /*
   * Command sequence number
   */

  static int _sequenceNumber=0;


  /*
   * The command ID
   */

  protected int _commandId;


  /*
   * Constructor
   */

  protected Command(int id) {
    _commandId=id;
  }


  /*
   * Get the command as flat bytes
   */

  public byte[] getBytes() throws IOException {

    ByteArrayOutputStream bos;

    bos=new ByteArrayOutputStream();

    // write the id and sequence number

    bos.write(_commandId);
    writeInt(bos,_sequenceNumber++);

    // write the custom parts

    serialize(bos);

    // return the encoded command

    return bos.toByteArray();
  }


  /*
   * Write a 32-bit int
   */

  protected void writeInt(OutputStream os,int value) throws IOException {
    os.write(value & 0xff);
    os.write((value >> 8) & 0xff);
    os.write((value >> 16) & 0xff);
    os.write((value >> 24) & 0xff);
  }

  /*
   * Write a 32-bit int
   */

  protected void writeInt(OutputStream os,long value) throws IOException {
    os.write((int)(value & 0xff));
    os.write((int)((value >> 8) & 0xff));
    os.write((int)((value >> 16) & 0xff));
    os.write((int)((value >> 24) & 0xff));
  }


  /*
   * write a 16-bit int
   */

  protected void writeInt16(OutputStream os,int value) throws IOException {
    os.write(value & 0xff);
    os.write((value >> 8) & 0xff);
  }


  /*
   * Subclass serialization
   */

  protected void serialize(OutputStream os) throws IOException {
  }
}
