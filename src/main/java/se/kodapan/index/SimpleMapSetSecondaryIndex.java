package se.kodapan.index;

import se.kodapan.collections.MapSet;

import java.util.Set;

/**
 * Same secondary type as primary type
 * @author kalle
 * @since 2010-jul-16 04:24:47
 */
public abstract class SimpleMapSetSecondaryIndex<EntityType extends EntityObject> extends MapSetSecondaryIndex<EntityType, EntityType> {

  private static final long serialVersionUID = 1l;

  protected SimpleMapSetSecondaryIndex() {
  }

  protected SimpleMapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex) {
    super(name, entityTypePrimaryIndex);
  }

  protected SimpleMapSetSecondaryIndex(String name, PrimaryIndex<EntityType> entityTypePrimaryIndex, MapSet<Object, EntityType> objectEntityTypeMapSet) {
    super(name, entityTypePrimaryIndex, objectEntityTypeMapSet);
  }

  /**
   *
   * @param entity
   * @return true if removed
   */
  @Override
  public boolean remove(EntityType entity) {
    Object secondaryKey = getSecondaryKey(entity);
    Set<EntityType> values = getMapSet().get(secondaryKey);
    return values != null && values.remove(entity);
  }

  @Override
  public void put(EntityType object) {
    if (!getMapSet().add(getSecondaryKey(object), object)) {
      throw new InconsistencyException("Entity " + object.toString() + " is already known in " + this.toString());
    }
  }

}
