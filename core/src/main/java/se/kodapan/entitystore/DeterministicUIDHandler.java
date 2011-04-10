/*
 * Copyright 2010 Kodapan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.kodapan.entitystore;

import java.io.*;
import java.util.Date;

/**
 * Generates 12 bytes long deterministic identities using timestamp and an integer counter.
 * <p/>
 * <p/>
 * That means one single transaction (the same execution time) can not produce more than Integer.MAX_VALUE identities,
 * after that a new transaction must be created. (remember children, 640 kilobytes RAM should be enough for anyone!)
 *
 * @author kalle
 * @since 2010-jan-09 12:47:22
 */
public class DeterministicUIDHandler implements IdentityFactory<String>, Serializable, Externalizable {

  private static final long serialVersionUID = 1l;

  private Date previousExecutionTime = new Date(0);
  private int previousExecutionTimeSequence = 0;

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeInt(1); // local object version
    objectOutput.writeObject(previousExecutionTime);
    objectOutput.writeInt(previousExecutionTimeSequence);
  }

  @Override
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    int version = objectInput.readInt();
    if (version == 1) {
      previousExecutionTime = (Date) objectInput.readObject();
      previousExecutionTimeSequence = objectInput.readInt();
    } else {
      throw new IOException("Unsupported local version " + version + ", expected 1");
    }
  }

  @Override
  public synchronized String nextIdentity(Date executionTime) {

    if (previousExecutionTimeSequence == Integer.MAX_VALUE) {
      throw new RuntimeException("This transaction (executionTime) can not create more unique identities. Please create a new transaction or update the sequence counter to long rather than int.");
    }

    int sequence;
    if (!executionTime.equals(previousExecutionTime)) {
      if (previousExecutionTime.getTime() > executionTime.getTime()) {
        throw new RuntimeException("Execution time was less than previous execution time");
      }
      previousExecutionTime = executionTime;
      sequence = previousExecutionTimeSequence = 1;
    } else {
      sequence = ++previousExecutionTimeSequence;
    }

    byte[] duid = new byte[12];

    byte[] timeBytes = longToByteArray(previousExecutionTime.getTime());
    byte[] sequenceBytes = intToByteArray(sequence);
    int index = 0;

    duid[index++] = timeBytes[7];
    duid[index++] = timeBytes[6];
    duid[index++] = timeBytes[5];
    duid[index++] = timeBytes[4];

    duid[index++] = timeBytes[1];
    duid[index++] = timeBytes[0];

    duid[index++] = sequenceBytes[0];
    duid[index++] = sequenceBytes[1];
    duid[index++] = sequenceBytes[2];
    duid[index++] = sequenceBytes[3];

    duid[index++] = timeBytes[3];
    duid[index++] = timeBytes[2];

    return toHex(duid);
  }

  public static byte[] longToByteArray(long l) {
    byte[] buf = new byte[8];
    for (int i = 7; i >= 0; --i) {
      buf[i] = (byte) (l & 0xffL);
      l >>>= 8;
    }
    return buf;
  }

  public static byte[] intToByteArray(int i) {
    byte[] buf = new byte[4];
    for (int index = 3; index >= 0; --index) {
      buf[index] = (byte) (i & 0xffL);
      i >>>= 8;
    }
    return buf;
  }

  public static int byteArrayToInt(byte[] array) {
    if (array.length != 4) {
      throw new IllegalArgumentException("Expected 4 bytes but found " + array.length);
    }
    return ((array[0] & 0xff) << 24) |
        ((array[1] & 0xff) << 16) |
        ((array[2] & 0xff) << 8) |
        (array[3] & 0xff);
  }


  public static long byteArrayToLong(byte[] array) {
    if (array.length != 8) {
      throw new IllegalArgumentException("Expected 8 bytes but found " + array.length);
    }
    return ((long) (array[0] & 0xff) << 56) |
        ((long) (array[1] & 0xff) << 48) |
        ((long) (array[2] & 0xff) << 40) |
        ((long) (array[3] & 0xff) << 32) |
        ((long) (array[4] & 0xff) << 24) |
        ((long) (array[5] & 0xff) << 16) |
        ((long) (array[6] & 0xff) << 8) |
        ((long) (array[7] & 0xff));
  }


  public static String toHex(byte[] arr) {
    StringBuilder sb = new StringBuilder(arr.length * 2);
    for (byte b : arr) {
      int v = b & 0xff;
      if (v < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(v));
    }
    return sb.toString();
  }

  public static DeterministicUID decode(String hex) {
    return decode(hex2ByteArray(hex));
  }

  public static DeterministicUID decode(byte[] bytes) {
    if (bytes.length != 12) {
      throw new IllegalArgumentException("Expected 12 bytes, got " + bytes.length);
    }
    byte[] executionTimeBytes = new byte[8];
    byte[] sequenceBytes = new byte[4];


    int index = 0;
    executionTimeBytes[7] = bytes[index++];
    executionTimeBytes[6] = bytes[index++];
    executionTimeBytes[5] = bytes[index++];
    executionTimeBytes[4] = bytes[index++];

    executionTimeBytes[1] = bytes[index++];
    executionTimeBytes[0] = bytes[index++];

    sequenceBytes[0] = bytes[index++];
    sequenceBytes[1] = bytes[index++];
    sequenceBytes[2] = bytes[index++];
    sequenceBytes[3] = bytes[index++];

    executionTimeBytes[3] = bytes[index++];
    executionTimeBytes[2] = bytes[index++];

    Date executionTime = new Date(byteArrayToLong(executionTimeBytes));
    int sequence = byteArrayToInt(sequenceBytes);

    return new DeterministicUID(executionTime, sequence);
  }

  public static class DeterministicUID implements Serializable {

    private Date executionTime;
    private int sequence;

    public DeterministicUID(Date executionTime, int sequence) {
      this.executionTime = executionTime;
      this.sequence = sequence;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      DeterministicUID that = (DeterministicUID) o;

      if (sequence != that.sequence) return false;
      if (executionTime != null ? !executionTime.equals(that.executionTime) : that.executionTime != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = executionTime != null ? executionTime.hashCode() : 0;
      result = 31 * result + sequence;
      return result;
    }

    public final Date getExecutionTime() {
      return executionTime;
    }

    public final int getSequence() {
      return sequence;
    }
  }


  private static final String hexVal = "0123456789ABCDEF";

  /**
   * http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
   *
   * @param hexString
   * @return
   */
  public static byte[] hex2ByteArray(String hexString) {
    hexString = hexString.toUpperCase();
    byte[] out = new byte[hexString.length() / 2];

    int n = hexString.length();

    for (int i = 0; i < n; i += 2) {
      //make a bit representation in an int of the hex value
      int hn = hexVal.indexOf(hexString.charAt(i));
      int ln = hexVal.indexOf(hexString.charAt(i + 1));
      //now just shift the high order nibble and add them together
      out[i / 2] = (byte) ((hn << 4) | ln);
    }

    return out;
  }
}