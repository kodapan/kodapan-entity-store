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
import se.kodapan.collections.MapSet;
import se.kodapan.io.SerializableBean;
import se.kodapan.io.UnsupportedLocalVersion;
import se.kodapan.lang.reflect.augmentation.Aggregation;
import se.kodapan.lang.reflect.augmentation.BinaryAssociationClassEnd;
import se.kodapan.lang.reflect.augmentation.BinaryAssociationEnd;
import se.kodapan.lang.reflect.augmentation.Mirror;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kalle
 * @since 2010-jul-10 00:29:40
 */
public class EntityStore extends SerializableBean {

  public static Logger log = LoggerFactory.getLogger(EntityStore.class);
  private static final long serialVersionUID = 1l;

  private IdentityFactory identityFactory;
  private Map<Class<? extends EntityObject>, PrimaryIndex> primaryIndices = new ConcurrentHashMap<Class<? extends EntityObject>, PrimaryIndex>();
  private Map<String, SecondaryIndex> secondaryIndicesByName = new HashMap<String, SecondaryIndex>();


  public boolean registerSecondaryIndex(SecondaryIndex secondaryIndex) {
    SecondaryIndex previous = getSecondaryIndicesByName().get(secondaryIndex.getName());
    if (previous != null) {
      log.warn("A secondary index named " + secondaryIndex.getName() + " already exists.");
      return false;
    }

    // todo lock entity store put and remove
    secondaryIndex.reconstruct();
    // todo unlock

    getSecondaryIndicesByName().put(secondaryIndex.getName(), secondaryIndex);
    secondaryIndex.getPrimaryIndex().getSecondaryIndicesByName().put(secondaryIndex.getName(), secondaryIndex);


    return true;
  }

  private transient Map<Class, Set<Class>> primaryIndexClassesByClass = new HashMap<Class, Set<Class>>();

  /**
   * one index per class,
   * and one index per any super class or interface of type EntityDomainObject
   * <p/>
   * [Organization]- - -|>[LegalPerson]<|- - -[Human]
   * Humans and Organizations are all available in the primary index for LegalPersons.
   */
  private Set<Class> gatherEntityObjectClasses(Class _class) {
    Set<Class> allClasses = primaryIndexClassesByClass.get(_class);
    if (allClasses == null) {
      allClasses = new HashSet<Class>();
      while (_class != Object.class) {
        allClasses.add(_class);
        gatherInterfaces(allClasses, _class);
        _class = _class.getSuperclass();
      }
      for (Iterator<Class> it = allClasses.iterator(); it.hasNext();) {
        Class type = it.next();
        if (!hasPrimaryIndex(type)) {
          it.remove();
        }
      }
      primaryIndexClassesByClass.put(_class, allClasses);
    }
    return allClasses;
  }

  public boolean hasPrimaryIndex(Class entityType) {
    return EntityObject.class.isAssignableFrom(entityType)
        && !entityType.isAnnotationPresent(NoPrimaryIndex.class);
  }

  private void gatherInterfaces(Set<Class> superInterfaces, Class _class) {
    if (_class.getInterfaces() != null) {
      for (Class superInterface : _class.getInterfaces()) {
        if (superInterface != Serializable.class
            && superInterface != Externalizable.class) {
          superInterfaces.add(superInterface);
          gatherInterfaces(superInterfaces, superInterface);
        }
      }
    }

  }

  @SuppressWarnings("unchecked")
  public <EntityType extends EntityObject> PrimaryIndex<EntityType> getPrimaryIndex(Class<EntityType> entityType) {
    PrimaryIndex<EntityType> index = (PrimaryIndex<EntityType>) getPrimaryIndices().get(entityType);
    if (index == null) {
      if (!hasPrimaryIndex(entityType)) {
        return null;
      }
      index = new PrimaryIndex<EntityType>(this, entityType);
      getPrimaryIndices().put(entityType, index);
    }
    return index;
  }


  /**
   * Adds object to all class indices.
   *
   * @param entity
   * @return previous value
   */
  @SuppressWarnings("unchecked")
  public EntityObject put(EntityObject entity) {

    if (entity.getId() == null) {
      throw new NullPointerException("No entity object identity in " + entity.toString());
    }

    boolean seen = false;
    EntityObject previous = null;
    for (Class entityClass : gatherEntityObjectClasses(entity.getClass())) {
      EntityObject value = getPrimaryIndex(entityClass).put(entity);
      if (seen) {
        if ((previous != null && !previous.equals(value))
            || (value != null && !value.equals(previous))) {
          throw new InconsistencyException("");
        }
      }
      previous = value;
      seen = true;
    }
    return previous;

  }


  public EntityObject get(String id) {
    return getPrimaryIndex(EntityObject.class).get(id);
  }

  @SuppressWarnings("unchecked")
  public void remove(EntityObject entity) {
    for (Class entityClass : gatherEntityObjectClasses(entity.getClass())) {
      getPrimaryIndex(entityClass).getEntitiesById().remove(entity.getId());
    }
  }

  /**
   * Removes an instance from other end of all bi directional associations.
   * This is automatically done when an entity is removed from a primary index.
   * @param instance object to be decoupled
   */
  public void decouple(Object instance) {

    if (log.isInfoEnabled()) {
      log.info("Decoupling " + instance);
    }

    Mirror mirror = Mirror.reflect(instance.getClass());

    for (BinaryAssociationEnd end : mirror.getBinaryAssociationEnds().values()) {

      Object value = end.getAccessor().get(instance);
      if (value == null) {
        continue;
      }

      BinaryAssociationEnd otherEnd = end.getOtherEnd();

      if (end.isNavigatable()) {

        if (log.isInfoEnabled()) {
          log.info("Decoupling " + end.toString());
        }

        if (end.getQualification() != null) {
          // has qualifications
          if (end.getBinaryAssociation().getAssociationClassEnds() != null) {
            // has qualifications and association class
            if (end.getMultiplicity().isMaximumOne()) {
              // this is a Map
              throw new UnsupportedOperationException("Not implemented");
            } else {
              // this is a MapSet
              throw new UnsupportedOperationException("Not implemented");
            }
          } else {
            // has qualifications without association class
            if (end.getMultiplicity().isMaximumOne()) {
              // this is a Map
              throw new UnsupportedOperationException("Not implemented");
            } else {
              // this is a MapSet
              throw new UnsupportedOperationException("Not implemented");
            }
          }

        } else {
          // unqualified association
          if (end.getBinaryAssociation().getAssociationClassEnds() != null) {
            // has association class
            if (end.getMultiplicity().isMaximumOne()) {
              // this is a field with a unqualified association class
              Object associationClassInstance = value;
              Object otherEndInstance = otherEnd.getAssociationClassEnd().getAccessor().get(associationClassInstance);
              if (otherEndInstance != null) {
                end.getAccessor().set(instance, (Object) null);
                decoupleInOtherEnd(instance, end, otherEnd, otherEndInstance, associationClassInstance);
              }
              nullBinaryAssociationClassEnds(end, associationClassInstance);

            } else {
              // this is a collection of unqualified association classes
              for (Iterator it = ((Collection) value).iterator(); it.hasNext();) {
                Object associationClassInstance = it.next();
                it.remove();
                Object otherEndInstance = otherEnd.getAssociationClassEnd().getAccessor().get(associationClassInstance);
                decoupleInOtherEnd(instance, end, otherEnd, otherEndInstance, associationClassInstance);
                nullBinaryAssociationClassEnds(end, associationClassInstance);
              }

            }
          } else {
            // plain old binary association
            if (end.getMultiplicity().isMaximumOne()) {
              // this is a field
              Object otherEndInstance = value;
              end.getAccessor().set(instance, null);
              decoupleInOtherEnd(instance, end, otherEnd, otherEndInstance, null);

            } else {
              // this is a collection
              for (Iterator it = ((Collection) value).iterator(); it.hasNext();) {
                Object otherEndInstance = it.next();
                it.remove();
                decoupleInOtherEnd(instance, end, otherEnd, otherEndInstance, null);
              }
            }
          }
        }

      }
    }


  }

  private void decoupleInOtherEnd(Object endInstance, BinaryAssociationEnd end, BinaryAssociationEnd otherEnd, Object otherEndInstance, Object associationClassInstance) {

    if (otherEnd.isNavigatable()) {
      if (otherEnd.getQualification() != null) {
        // has qualifications
        if (otherEnd.getBinaryAssociation().getAssociationClassEnds() != null) {

          // has qualifications and association class
          if (otherEnd.getMultiplicity().isMaximumOne()) {
            // this is a Map
            ((Map) otherEnd.getAccessor().get(otherEndInstance)).values().remove(associationClassInstance);
          } else {
            // this is a MapSet
            ((MapSet) otherEnd.getAccessor().get(otherEndInstance)).removeSetValue(associationClassInstance);
          }
        } else {
          // has qualifications without association class
          if (otherEnd.getMultiplicity().isMaximumOne()) {
            // this is a Map
            ((Map) otherEnd.getAccessor().get(otherEndInstance)).values().remove(endInstance);

          } else {
            // this is a MapSet
            ((Map) otherEnd.getAccessor().get(otherEndInstance)).values().remove(endInstance);
          }
        }

      } else {
        // unqualified association
        if (otherEnd.getBinaryAssociation().getAssociationClassEnds() != null) {
          // has association class
          if (otherEnd.getMultiplicity().isMaximumOne()) {
            // this is a field with an association class
            otherEnd.getAccessor().set(otherEndInstance, null);
          } else {
            // this is a collection of association classes
            ((Collection) otherEnd.getAccessor().get(otherEndInstance)).remove(associationClassInstance);
          }
        } else {
          // plain old binary association
          if (otherEnd.getMultiplicity().isMaximumOne()) {
            // this is a field
            otherEnd.getAccessor().set(otherEndInstance, null);
          } else {
            // this is a collection
            ((Collection) otherEnd.getAccessor().get(otherEndInstance)).remove(endInstance);
          }
        }
      }

      if (associationClassInstance != null) {
        log.info("Instance " + associationClassInstance + " removed from " + otherEndInstance + " " + otherEnd.toString() + " " + endInstance);
      } else {
        log.info("Instance " + endInstance + " removed from " + otherEnd.toString() + " " + otherEndInstance);
      }

    }


    if (end.getAggregation() == Aggregation.COMPOSITE) {
      if (otherEndInstance instanceof EntityObject) {
        remove((EntityObject) otherEndInstance);
      } else {
        //decouple(otherEndInstance);
      }
    }
  }

  private void nullBinaryAssociationClassEnds(BinaryAssociationEnd end, Object associationClassInstance) {
    for (BinaryAssociationClassEnd bace : end.getBinaryAssociation().getAssociationClassEnds()) {
      bace.getAccessor().set(associationClassInstance, null);
    }
  }


  public IdentityFactory getIdentityFactory() {
    return identityFactory;
  }

  public void setIdentityFactory(IdentityFactory identityFactory) {
    this.identityFactory = identityFactory;
  }

  public Map<Class<? extends EntityObject>, PrimaryIndex> getPrimaryIndices() {
    return primaryIndices;
  }

  public void setPrimaryIndices(Map<Class<? extends EntityObject>, PrimaryIndex> primaryIndices) {
    this.primaryIndices = primaryIndices;
  }

  public Map<String, SecondaryIndex> getSecondaryIndicesByName() {
    return secondaryIndicesByName;
  }

  public void setSecondaryIndicesByName(Map<String, SecondaryIndex> secondaryIndicesByName) {
    this.secondaryIndicesByName = secondaryIndicesByName;
  }

}
