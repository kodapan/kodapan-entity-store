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

import se.kodapan.collections.MapSet;
import se.kodapan.io.UnsupportedLocalVersion;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

/**
 * @author kalle
 * @since 2010-jul-10 00:37:11
 *
 * @param <ResultType> 
 * @param <EntityType>
 */
public abstract class AbstractSecondaryIndex<ResultType, EntityType extends EntityObject>
    implements SecondaryIndex<ResultType, EntityType> {

  private static final long serialVersionUID = 1l;

  private String name;
  private PrimaryIndex<EntityType> primaryIndex;

  protected AbstractSecondaryIndex() {
  }

  protected AbstractSecondaryIndex(String name, PrimaryIndex<EntityType> primaryIndex) {
    this.name = name;
    this.primaryIndex = primaryIndex;
  }

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeInt(1); // version
    objectOutput.writeObject(name);
    objectOutput.writeObject(primaryIndex);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    int version = objectInput.readInt();
    if (version == 1) {
      name = (String) objectInput.readObject();
      primaryIndex = (PrimaryIndex) objectInput.readObject();

    } else {
      throw new UnsupportedLocalVersion(version, 1);
    }

  }

  @Override
  public ResultType get(Object... parameters) {    
    Set<ResultType> results = list(parameters);
    if (results == null || results.size() == 0) {
      return null;
    }
    if (results.size() > 1) {
      throw new RuntimeException("Expected a single unique entity but found " + results.size());
    }
    return results.iterator().next();
  }


  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public PrimaryIndex<EntityType> getPrimaryIndex() {
    return primaryIndex;
  }

  public void setPrimaryIndex(PrimaryIndex<EntityType> primaryIndex) {
    this.primaryIndex = primaryIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractSecondaryIndex that = (AbstractSecondaryIndex) o;

    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "AbstractSecondaryIndex{" +
        "name='" + name + '\'' +
        ", primaryIndex=" + primaryIndex +
        '}';
  }
}