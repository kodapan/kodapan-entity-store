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
import se.kodapan.collections.DecoratedMap;
import se.kodapan.io.UnsupportedLocalVersion;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author kalle
 * @since 2010-jul-10 01:11:07
 */
public class PrimaryIndex<EntityType extends EntityObject>
    implements Serializable, Externalizable {

  private static final long serialVersionUID = 1l;

  public static Logger log = LoggerFactory.getLogger(PrimaryIndex.class);

  private EntityStore store;

  private Map<String, SecondaryIndex<Object, EntityType>> secondaryIndicesByName = new HashMap<String, SecondaryIndex<Object, EntityType>>();

  private Map<String, EntityType> entitiesById;
  private Class<EntityType> entityType;

  public PrimaryIndex() {
  }

  public PrimaryIndex(EntityStore store, Class<EntityType> entityType) {
    this(store, new HashMap<String, EntityType>(), entityType);
  }

  public PrimaryIndex(EntityStore store, Map<String, EntityType> entitiesById, Class<EntityType> entityType) {
    this.store = store;
    this.entityType = entityType;
    setEntitiesById(entitiesById);
  }

  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeInt(1); // version
    objectOutput.writeObject(store);
    objectOutput.writeObject(entityType);
    objectOutput.writeObject(entitiesById);
    objectOutput.writeObject(secondaryIndicesByName);

  }

  @Override
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    int version = objectInput.readInt();
    if (version == 1) {
      store = (EntityStore) objectInput.readObject();
      entityType = (Class) objectInput.readObject();
      entitiesById = ((Map) objectInput.readObject());
      secondaryIndicesByName = ((Map) objectInput.readObject());
    } else {
      throw new UnsupportedLocalVersion(version, 1);
    }

  }

  public EntityType remove(EntityType entity) {
    return remove(entity.getId());
  }

  public EntityType remove(String identity) {
    return getEntitiesById().remove(identity);
  }

  public EntityType put(EntityType entity) {
    return getEntitiesById().put(entity.getId(), entity);
  }

  public EntityType get(String id) {
    return getEntitiesById().get(id);
  }

  private void removeFromSecondaryIndices(EntityType entity) {
    for (SecondaryIndex<Object, EntityType> secondaryIndex : getSecondaryIndicesByName().values()) {
      if (!secondaryIndex.remove(entity)) {
        log.error("Inconsistency, the removed entity was not available in " + secondaryIndex.toString());
      }
    }
  }

  public Map<String, EntityType> getEntitiesById() {
    return entitiesById;
  }

  public void setEntitiesById(Map<String, EntityType> entitiesById) {
    if (entitiesById != null) {
      entitiesById = new DecoratedMap<String, EntityType>(entitiesById) {
        @Override
        public EntityType remove(Object o) {
          EntityType removed = super.remove(o);
          if (removed != null) {
            // remove in secondary indices
            removeFromSecondaryIndices(removed);
            // remove composite parts and decouple from associations
            getStore().decouple(removed);
          }
          return removed;
        }

        @Override
        public EntityType put(String s, EntityType entity) {
          EntityType previous = super.put(s, entity);
          if (previous != null && previous != entity) {
            // remove in secondary indices
            removeFromSecondaryIndices(previous);
            // remove composite parts and decouple from associations
            getStore().decouple(previous);
          }

          if (previous != entity) {
            // add in secondary indices
            for (SecondaryIndex<Object, EntityType> secondaryIndex : getSecondaryIndicesByName().values()) {
              secondaryIndex.put(entity);
            }
          }

          return previous;
        }

        @Override
        public void clear() {
          for (Iterator<Entry<String, EntityType>> it = entrySet().iterator(); it.hasNext();) {
            it.remove();
          }
        }
      };
    }
    this.entitiesById = entitiesById;
  }

  public Class<EntityType> getEntityType() {
    return entityType;
  }

  public Map<String, SecondaryIndex<Object, EntityType>> getSecondaryIndicesByName() {
    return secondaryIndicesByName;
  }

  public void setSecondaryIndicesByName(Map<String, SecondaryIndex<Object, EntityType>> secondaryIndicesByName) {
    this.secondaryIndicesByName = secondaryIndicesByName;
  }

  public EntityStore getStore() {
    return store;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PrimaryIndex that = (PrimaryIndex) o;

    if (!entityType.equals(that.entityType)) return false;
    if (!store.equals(that.store)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = store.hashCode();
    result = 31 * result + entityType.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "PrimaryIndex{" +
        "entityType=" + entityType +
        ", store=" + store +
        '}';
  }
}
