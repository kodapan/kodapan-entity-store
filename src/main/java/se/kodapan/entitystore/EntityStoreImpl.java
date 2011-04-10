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
import se.kodapan.collections.MapSet;
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
public class EntityStoreImpl implements EntityStore, Serializable, Externalizable {

  public static Logger log = LoggerFactory.getLogger(EntityStoreImpl.class);
  private static final long serialVersionUID = 1l;

  private IdentityFactory identityFactory;
  private Map<Class, PrimaryIndex> primaryIndices = new ConcurrentHashMap<Class, PrimaryIndex>();
  private Map<String, SecondaryIndex> secondaryIndicesByName = new HashMap<String, SecondaryIndex>();

  /**
   * any class that is allowed to have a primary index
   * must either be available in this set
   * or be annotated at class level with @Entity
   */
  private Set<Class> entityTypes = new HashSet<Class>();


  @Override
  public void writeExternal(ObjectOutput objectOutput) throws IOException {
    objectOutput.writeInt(1); // local version
    objectOutput.writeObject(identityFactory);
    objectOutput.writeObject(primaryIndices);
    objectOutput.writeObject(secondaryIndicesByName);
    objectOutput.writeObject(entityTypes);
  }

  @Override
  public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
    int version = objectInput.readInt();
    if (version == 1) {
      identityFactory = (IdentityFactory) objectInput.readObject();
      primaryIndices = (Map<Class, PrimaryIndex>) objectInput.readObject();
      secondaryIndicesByName = (Map<String, SecondaryIndex>) objectInput.readObject();
      entityTypes = (Set<Class>)objectInput.readObject();
    } else {
      throw new IOException("Unsupported local version " + version + ", expected 1");
    }
  }

  /**
   * Adds a secondary index to the store.
   * <p/>
   * Warning! It might cause inconsistency if touching associated primary indices before this methods returns.
   *
   * @param secondaryIndex
   * @return
   */
  public boolean registerSecondaryIndex(SecondaryIndex secondaryIndex) {
    SecondaryIndex previous = getSecondaryIndicesByName().get(secondaryIndex.getName());
    if (previous != null) {
      log.warn("A secondary index named " + secondaryIndex.getName() + " already exists.");
      return false;
    }

    secondaryIndex.reconstruct();

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
   * <p/>
   * In effect all classes annotated at class level with {@link Entity}. todo or manually registered different
   *
   * @param _class class of which all entity classes is to be gathered from.
   * @return all entity classes associated with the class of parameter _class.
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
        if (!isEntityType(type)) {
          it.remove();
        }
      }
      primaryIndexClassesByClass.put(_class, allClasses);
    }
    return allClasses;
  }

  /**
   * Inspects if a class is an entity class or not
   *
   * @param entityType class to be inspected
   * @return true if class of parameter entityType is annotated with {@link Entity} todo or manually registered different
   */
  public boolean isEntityType(Class entityType) {
    return entityTypes.contains(entityType) || entityType.isAnnotationPresent(Entity.class);
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

  @Override
  public <EntityType> PrimaryIndex<Object, EntityType> getPrimaryIndex(Class<EntityType> entityType) {
    if (!isEntityType(entityType)) {
      throw new NotRegisteredAsPrimaryIndexException(entityType);
    }
    PrimaryIndex<Object, EntityType> index = (PrimaryIndex<Object, EntityType>) getPrimaryIndices().get(entityType);
    if (index == null) {
      throw new EntityStoreException("Illegal state, this method can not be used for primary index " + entityType.getName() + " until it has been registered with an identity class.");
    } else {
      return getPrimaryIndex(Object.class, entityType);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <IdentityType, EntityType> PrimaryIndex<IdentityType, EntityType> getPrimaryIndex(Class<IdentityType> identityType, Class<EntityType> entityType) {
    PrimaryIndex<IdentityType, EntityType> index = (PrimaryIndex<IdentityType, EntityType>) getPrimaryIndices().get(entityType);
    if (index == null) {
      if (!isEntityType(entityType)) {
        throw new NotRegisteredAsPrimaryIndexException(entityType);
      }
      index = new PrimaryIndex<IdentityType, EntityType>(this, identityType, entityType);
      getPrimaryIndices().put(entityType, index);
    }
    return index;
  }


  /**
   * Adds object to all class indices.
   *
   * @param identity
   * @param entity
   * @param <IdentityType>
   * @param <EntityType>
   * @return previous value
   */
  @SuppressWarnings("unchecked")
  @Override
  public <IdentityType, EntityType> EntityType put(IdentityType identity, EntityType entity) {

    if (identity == null) {
      throw new NullPointerException("No entity object identity in " + entity.toString());
    }

    boolean seen = false;
    EntityType previous = null;
    for (Class entityClass : gatherEntityObjectClasses(entity.getClass())) {
      EntityType value = (EntityType) getPrimaryIndex(entityClass).put((IdentityType) identity, entity);
      if (seen) {
        if ((previous != null && !previous.equals(value))
            || (value != null && !value.equals(previous))) {
          throw new InconsistencyException("At least two different instances was found in different primary indices using the same identity! You most likely found a new bug in the kodapan entity store project.");
        }
      }
      previous = value;
      seen = true;
    }
    return previous;
  }


  @Override
  public <IdentityType, EntityType> EntityType get(Class<EntityType> entityType, IdentityType identity) {
    return getPrimaryIndex(entityType).get(identity);
  }

  /**
   * You really need to use the top class level entity type or the object might not be removed from all primary indices.
   *
   * @param entityType
   * @param identity
   * @param <IdentityType>
   * @param <EntityType>
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public <IdentityType, EntityType> EntityType remove(Class<EntityType> entityType, IdentityType identity) {
    // todo first gather all that contains, make sure there is no inconsistency, then make the run again and remove.
    EntityType object = null;
    for (Class entityClass : gatherEntityObjectClasses(entityType)) {
      object = (EntityType) getPrimaryIndex(entityClass).getEntitiesById().remove(identity);
    }
    return object;
    // todo return gathered removed instance
  }

  /**
   * Removes an instance from other end of all bi-directional associations.
   * This is automatically done when an entity is removed from a primary index.
   *
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
          // todo qualifications

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
      if (isEntityType(otherEnd.getOwnerClass().getType())) {
        // remove other end from store
        PrimaryIndex otherEndPrimaryIndex = getPrimaryIndex(otherEnd.getOwnerClass().getType());
        Object otherEndIdentity = otherEndPrimaryIndex.getIdentity(otherEndInstance);
        remove(otherEnd.getOwnerClass().getType(), otherEndIdentity);

      } else {
        //todo decouple this from other end but leave other end in store.
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

  public Map<Class, PrimaryIndex> getPrimaryIndices() {
    return primaryIndices;
  }

  public void setPrimaryIndices(Map<Class, PrimaryIndex> primaryIndices) {
    this.primaryIndices = primaryIndices;
  }

  public Map<String, SecondaryIndex> getSecondaryIndicesByName() {
    return secondaryIndicesByName;
  }

  public void setSecondaryIndicesByName(Map<String, SecondaryIndex> secondaryIndicesByName) {
    this.secondaryIndicesByName = secondaryIndicesByName;
  }

  public Set<Class> getEntityTypes() {
    return entityTypes;
  }

  public void setEntityTypes(Set<Class> entityTypes) {
    this.entityTypes = entityTypes;
  }
}
