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
public abstract class MapSetSecondaryIndex<EntityType extends EntityObject>
    extends AbstractSecondaryIndex<EntityType> {

  private static final long serialVersionUID = 1l;

  private MapSet<Object, EntityType> mapSet = new MapSet<Object, EntityType>();

  protected MapSetSecondaryIndex() {
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex) {
    super(name, entityTypePrimaryIndex);
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex, MapSet<Object, EntityType> mapSet) {
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


  /**
   *
   * @param entity
   * @return true if removed
   */
  @Override
  public boolean remove(EntityType entity) {
    return remove(entity, getSecondaryKey(entity));
  }

  public boolean remove(EntityType object, Object secondaryKey) {
    Set<EntityType> values = getMapSet().get(secondaryKey);
    return values != null && values.remove(object);
  }

  @Override
  public void put(EntityType object) {
    if (!getMapSet().add(getSecondaryKey(object), object)) {
      throw new InconsistencyException("Entity " + object.toString() + " is already known in " + this.toString());
    }
  }

  @Override
  public EntityType get(Object... parameters) {
    Object secondaryKey = getSecondaryKey(parameters);
    Set<EntityType> entities = getMapSet().get(secondaryKey);
    if (entities == null || entities.size() == 0) {
      return null;
    }
    if (entities.size() > 1) {
      throw new RuntimeException("Expected a single unique entity but found " + entities.size());
    }
    return entities.iterator().next();
  }

  @Override
  public Set<EntityType> list(Object... parameters) {
    Object secondaryKey = getSecondaryKey(parameters);
    Set<EntityType> entities = getMapSet().get(secondaryKey);
    if (entities == null || entities.size() == 0) {
      return null;
    }
    return entities;
  }


  public MapSet<Object, EntityType> getMapSet() {
    return mapSet;
  }

  public void setMapSet(MapSet<Object, EntityType> mapSet) {
    this.mapSet = mapSet;
  }

}
