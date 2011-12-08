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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.collections.DecoratedMap;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kalle
 * @since 2010-jul-10 01:11:07
 */
public class PrimaryIndex<IdentityType, EntityType>
    implements Serializable, Iterable<EntityType> {

  private static final long serialVersionUID = 1l;

  public static Logger log = LoggerFactory.getLogger(PrimaryIndex.class);

  private ReentrantLock updateLock = new ReentrantLock();


  private transient Set<PrimaryIndexListener<IdentityType, EntityType>> listeners = new HashSet<PrimaryIndexListener<IdentityType, EntityType>>();

  private EntityStoreImpl store;

  private Map<IdentityType, SecondaryIndex<Object, IdentityType, EntityType>> secondaryIndicesByName = new HashMap<IdentityType, SecondaryIndex<Object, IdentityType, EntityType>>();

  private Map<IdentityType, EntityType> entitiesById;
  private Class<EntityType> entityType;
  private Class<IdentityType> identityType;

  public PrimaryIndex() {
  }

  public PrimaryIndex(EntityStoreImpl store, Class<IdentityType> identityType, Class<EntityType> entityType) {
    this(store, identityType, entityType, new HashMap<IdentityType, EntityType>());
  }

  public PrimaryIndex(EntityStoreImpl store, Class<IdentityType> identityType, Class<EntityType> entityType, Map<IdentityType, EntityType> entitiesById) {
    this.store = store;
    this.entityType = entityType;
    this.identityType = identityType;
    setEntitiesById(entitiesById);
  }

  public IdentityType getIdentity(EntityType entity) {
    for (Map.Entry<IdentityType, EntityType> entry : new ArrayList<Map.Entry<IdentityType, EntityType>>(getEntitiesById().entrySet())) {
      if (entity.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  public Class<IdentityType> getIdentityType() {
    return identityType;
  }

  public void setIdentityType(Class<IdentityType> identityType) {
    this.identityType = identityType;
  }

  @Override
  public Iterator<EntityType> iterator() {
    return getEntitiesById().values().iterator();
  }

  public EntityType remove(IdentityType identity) {
    return getEntitiesById().remove(identity);
  }

  public EntityType put(IdentityType identity, EntityType entity) {
    return getEntitiesById().put(identity, entity);
  }

  public EntityType get(IdentityType id) {
    return getEntitiesById().get(id);
  }

  private void removeFromSecondaryIndices(EntityType entity) {
    for (SecondaryIndex<Object, IdentityType, EntityType> secondaryIndex : getSecondaryIndicesByName().values()) {
      secondaryIndex.remove(entity);
    }
  }

  public Map<IdentityType, EntityType> getEntitiesById() {
    return entitiesById;
  }

  public void setEntitiesById(Map<IdentityType, EntityType> entitiesById) {
    if (entitiesById != null) {
      entitiesById = new EntitiesMap(entitiesById);
    }
    this.entitiesById = entitiesById;
  }

  public Class<EntityType> getEntityType() {
    return entityType;
  }

  public Map<IdentityType, SecondaryIndex<Object, IdentityType, EntityType>> getSecondaryIndicesByName() {
    return secondaryIndicesByName;
  }

  public void setSecondaryIndicesByName(Map<IdentityType, SecondaryIndex<Object, IdentityType, EntityType>> secondaryIndicesByName) {
    this.secondaryIndicesByName = secondaryIndicesByName;
  }

  public EntityStoreImpl getStore() {
    return store;
  }

  public Set<PrimaryIndexListener<IdentityType, EntityType>> getListeners() {
    return listeners;
  }

  public void setListeners(Set<PrimaryIndexListener<IdentityType, EntityType>> listeners) {
    this.listeners = listeners;
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

  /**
   * decorates entities and makes sure they
   * at put-time are added to secondary indices
   * (and that previous instance with same identity is decoupled)
   * at remove-time is decoupled and removed from secondary indices
   */
  public class EntitiesMap extends DecoratedMap<IdentityType, EntityType> {

    private static final long serialVersionUID = 1L;

    private EntitiesMap(Map<IdentityType, EntityType> decoratedMap) {
      super(decoratedMap);
    }

    @Override
    public EntityType remove(final Object identity) {
      final EntityType removed = super.remove(identity);
      if (removed != null) {
        // remove in secondary indices
        removeFromSecondaryIndices(removed);
        // remove composite parts and decouple from associations
        getStore().decouple(removed);

        for (final PrimaryIndexListener<IdentityType, EntityType> listener : listeners) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              listener.deleted((IdentityType) identity, removed);
            }
          }).start();
        }

      }
      return removed;
    }

    @Override
    public EntityType put(final IdentityType identity, final EntityType entity) {
      final EntityType previous = super.put(identity, entity);
      if (previous != null && previous != entity) {
        // remove composite parts and decouple from associations
        getStore().decouple(previous);
      }

      if (previous != null) {
        // remove in secondary indices
        removeFromSecondaryIndices(previous);
      }

      // add in secondary indices
      for (SecondaryIndex<Object, IdentityType, EntityType> secondaryIndex : getSecondaryIndicesByName().values()) {
        secondaryIndex.put(entity);
      }

      for (final PrimaryIndexListener<IdentityType, EntityType> listener : listeners) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            if (previous == null) {
              listener.created(identity, entity);
            } else {
              listener.updated(identity, entity);
            }
          }
        }).start();
      }

      return previous;
    }

    @Override
    public void clear() {
      for (Iterator<Map.Entry<IdentityType, EntityType>> it = entrySet().iterator(); it.hasNext();) {
        it.remove();
      }
    }
  }

  public ReentrantLock getUpdateLock() {
    return updateLock;
  }

  public void setUpdateLock(ReentrantLock updateLock) {
    this.updateLock = updateLock;
  }
}
