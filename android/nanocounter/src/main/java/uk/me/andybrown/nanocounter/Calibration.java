package uk.me.andybrown.nanocounter;

import android.os.Parcel;

import java.math.BigDecimal;
import java.util.Date;

/*
 * Class to hold calibration data
 */

public class Calibration {

  protected Date _date=new Date(0);
  protected BigDecimal _temperature=BigDecimal.ZERO;      // deg C x 10. e.g. 25.3C = 253
  protected BigDecimal _offset=BigDecimal.ZERO;

  protected int _index;                   // not returned from MCU


  /*
   * Read from a parcel
   */

  public void readFromParcel(Parcel in) {
    setDate(new Date(in.readLong()));
    setTemperature(new BigDecimal(in.readString()));
    setOffset(new BigDecimal(in.readString()));
    setIndex(in.readInt());
  }

  /*
   * Write to a parcel
   */

  public void writeToParcel(Parcel out) {
    out.writeLong(getDate().getTime());
    out.writeString(getTemperature().toPlainString());
    out.writeString(getOffset().toPlainString());
    out.writeInt(getIndex());
  }


  public Date getDate() {
    return _date;
  }

  public BigDecimal getTemperature() {
    return _temperature;
  }

  public BigDecimal getOffset() {
    return _offset;
  }

  public void setDate(Date date) {
    _date=date;
  }

  public void setTemperature(BigDecimal temperature) {
    _temperature=temperature;
  }

  public void setOffset(BigDecimal offset) {
    _offset=offset;
  }

  public int getIndex() {
    return _index;
  }

  public void setIndex(int _index) {
    this._index=_index;
  }
}
