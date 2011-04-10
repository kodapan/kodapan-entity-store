package se.kodapan.io;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2010-nov-16 23:09:00
 */
public class SerializationObjectWithTransientField implements Serializable {

  /** getter and setter */
  private String string;

  /** no setter. */
  private transient String transientString;

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public String getTransientString() {
    return transientString;
  }

}
