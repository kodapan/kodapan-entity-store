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

import java.io.*;
import java.util.Set;

/**
 * MapSet based secondary index.
 *
 * @author kalle
 * @since 2010-jul-10 00:37:11
 */
public abstract class MapSetSecondaryIndex<ResultType, EntityType extends EntityObject>
    extends AbstractSecondaryIndex<ResultType, EntityType> {

  private static final long serialVersionUID = 1l;

  private MapSet<Object, ResultType> mapSet = new MapSet<Object, ResultType>();

  protected MapSetSecondaryIndex() {
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex) {
    super(name, entityTypePrimaryIndex);
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex, MapSet<Object, ResultType> mapSet) {
    super(name, entityTypePrimaryIndex);
    this.mapSet = mapSet;
  }

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    super.writeExternal(objectOutput);
    objectOutput.writeInt(1); // version
    objectOutput.writeObject(mapSet);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    super.readExternal(objectInput);
    int version = objectInput.readInt();
    if (version == 1) {
      mapSet = (MapSet) objectInput.readObject();

    } else {
      throw new UnsupportedLocalVersion(version, 1);
    }

  }


  public void reconstruct() {
    // todo lock
    getMapSet().clear();
    for (EntityType entity : getPrimaryIndex().getEntitiesById().values()) {
      put(entity);
    }
    // todo unlock
  }

  
  @Override
  public Set<ResultType> list(Object... parameters) {
    Object secondaryKey = getSecondaryKey(parameters);
    Set<ResultType> results = getMapSet().get(secondaryKey);
    if (results == null || results.size() == 0) {
      return null;
    }
    return results;
  }


  public MapSet<Object, ResultType> getMapSet() {
    return mapSet;
  }

  public void setMapSet(MapSet<Object, ResultType> mapSet) {
    this.mapSet = mapSet;
  }

}
