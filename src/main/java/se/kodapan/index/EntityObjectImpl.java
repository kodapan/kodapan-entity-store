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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.io.UnsupportedLocalVersion;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author kalle
 * @since 2010-jul-10 23:22:05
 */
public class EntityObjectImpl implements EntityObject {

  public static Logger log = LoggerFactory.getLogger(EntityObject.class);
  private static long serialVersionUID = 1l;

  private String id;

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeInt(1); // version
    objectOutput.writeObject(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    int version = objectInput.readInt();
    if (version == 1) {
      id = (String) objectInput.readObject();
    } else {
      throw new UnsupportedLocalVersion(version, 1);
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EntityObject that = (EntityObject) o;

    if (id != null ? !id.equals(that.getId()) : that.getId() != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "EntityObjectImpl{" +
        "id='" + id + '\'' +
        '}';
  }
}
