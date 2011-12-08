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

import se.kodapan.collections.SetMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ConcurrentModificationException;
import java.util.Set;

/**
 * MapSet based secondary index.
 *
 * @param <ResultType>   response class type, usually the same as the entity type
 * @param <PrimaryIndexIdentityType> class of identity in entity
 * @param <EntityType>   class of entity
 * @author kalle
 * @since 2010-jul-10 00:37:11
 */
public abstract class MapSetSecondaryIndex<ResultType, PrimaryIndexIdentityType, EntityType>
    extends AbstractSecondaryIndex<ResultType, PrimaryIndexIdentityType, EntityType> {

  private static final long serialVersionUID = 1l;

  private SetMap<Object, ResultType> mapSet = new SetMap<Object, ResultType>();

  protected MapSetSecondaryIndex() {
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<PrimaryIndexIdentityType, EntityType> entityTypePrimaryIndex) {
    super(name, entityTypePrimaryIndex);
  }

  protected MapSetSecondaryIndex(String name, PrimaryIndex<PrimaryIndexIdentityType, EntityType> entityTypePrimaryIndex, SetMap<Object, ResultType> mapSet) {
    super(name, entityTypePrimaryIndex);
    this.mapSet = mapSet;
  }

  /**
   * Reconstructs the secondary index based on all items in the primary index.
   *
   * @throws ConcurrentModificationException
   *          if primary index is modified while reconstructing the secondary index.
   */
  public void reconstruct() throws ConcurrentModificationException {
    // todo lock put and delete in primary index
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


  public SetMap<Object, ResultType> getMapSet() {
    return mapSet;
  }

  public void setMapSet(SetMap<Object, ResultType> mapSet) {
    this.mapSet = mapSet;
  }

}
