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

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Set;

/**
 * @author kalle
 * @since 2010-jul-12 16:24:21
 */
public interface SecondaryIndex<EntityType extends EntityObject> extends Serializable, Externalizable {

  public abstract Object getSecondaryKey(EntityType entity);

  public abstract Object getSecondaryKey(Object... parameters);


  public abstract void put(EntityType object);


  /**
   * @param parameters
   * @return 
   * @throws RuntimeException if more than one instance match the parameters (is not unique).
   */
  public abstract EntityType get(Object... parameters);

  public abstract Set<EntityType> list(Object... parameters);

  /**
   * @param entity
   * @return true if removed
   */
  public abstract boolean remove(EntityType entity);

  public abstract String getName();

  public abstract PrimaryIndex<EntityType> getPrimaryIndex();

  /**
   * adds all entities from the primary index to the secondary index,
   * called upon by the store when a secondary index is registered.
   */
  public abstract void reconstruct();

}
