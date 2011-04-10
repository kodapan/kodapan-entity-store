package se.kodapan.entitystore;

import se.kodapan.collections.MapSet;

/**
 * Same secondary type as primary type
 * @author kalle
 * @since 2010-jul-16 04:24:47
 */
public abstract class SimpleMapSetSecondaryIndex<IdentityType, EntityType> extends MapSetSecondaryIndex<EntityType, IdentityType, EntityType> {

  private static final long serialVersionUID = 1l;

  protected SimpleMapSetSecondaryIndex() {
  }

  protected SimpleMapSetSecondaryIndex(String name, PrimaryIndex<IdentityType, EntityType> entityTypePrimaryIndex) {
    super(name, entityTypePrimaryIndex);
  }

  protected SimpleMapSetSecondaryIndex(String name, PrimaryIndex<IdentityType, EntityType> entityTypePrimaryIndex, MapSet<Object, EntityType> objectEntityTypeMapSet) {
    super(name, entityTypePrimaryIndex, objectEntityTypeMapSet);
  }

  /**
   *
   * @param entity
   * @return true if removed
   */
  @Override
  public void remove(EntityType entity) {
    getMapSet().removeSetValue(entity);
  }

  @Override
  public void put(EntityType object) {
    if (!getMapSet().add(getSecondaryKey(object), object)) {
      throw new InconsistencyException("Entity " + object.toString() + " is already known in " + this.toString());
    }
  }

}
