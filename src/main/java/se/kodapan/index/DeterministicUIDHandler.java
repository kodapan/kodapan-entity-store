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

package se.kodapan.index;


import se.kodapan.io.UnsupportedLocalVersion;

import java.io.*;
import java.util.Date;

/**
 * @author kalle
 * @since 2010-jan-09 12:47:22
 */
public class DeterministicUIDHandler implements IdentityFactory, Serializable, Externalizable {

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
      throw new UnsupportedLocalVersion(version, 1);
    }
  }

  @Override
  public synchronized String nextIdentity(Date executionTime) {

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

    byte[] duid = new byte[10];

    byte[] timeBytes = longToByteArray(previousExecutionTime.getTime());

    int index = 0;

    duid[index++] = timeBytes[7];
    duid[index++] = timeBytes[6];
    duid[index++] = timeBytes[5];
    duid[index++] = timeBytes[4];

    duid[index++] = timeBytes[1];
    duid[index++] = timeBytes[0];

    duid[index++] = (byte) sequence;
    duid[index++] = (byte) (sequence>>> 8);

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


}