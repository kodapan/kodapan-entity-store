package se.kodapan.entitystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @author kalle
 * @since 2011-04-10 11.58
 */
public class AugmentedEntityStore extends EntityStoreImpl {

  public static Logger log = LoggerFactory.getLogger(EntityStoreImpl.class);
  private static final long serialVersionUID = 1l;

  public AugmentedEntityStore() {
  }

  @Override
  public boolean isEntityType(Class entityType) {
    return super.isEntityType(entityType) || entityType.isAnnotationPresent(Entity.class);
  }

  private Set<Class> registeredPrimaryIndices = new HashSet<Class>();

  @Override
  public <IdentityType, EntityType> PrimaryIndex<IdentityType, EntityType> getPrimaryIndex(Class<IdentityType> identityType, Class<EntityType> entityType) {
    PrimaryIndex primaryIndex = super.getPrimaryIndex(identityType, entityType);
    if (registeredPrimaryIndices.add(entityType)) {
      for (Field field : entityType.getFields()) {
        if (field.isAnnotationPresent(Index.class)) {
          Index key = field.getAnnotation(Index.class);
          String name = key.name();
          if (name.isEmpty()) {
            name = entityType.getSimpleName() + " by " + field.getName();
          }
          SecondaryIndex secondaryIndex = new BeanFieldSecondaryIndex(name, primaryIndex, field.getType(), field.getName());
          if (!registerSecondaryIndex(secondaryIndex)) {
            throw new RuntimeException("Name for secondary index on " + entityType.getName() + "#" + field.getName() + " is not unique! " + name);
          }
        }
      }
    }
    return primaryIndex;
  }
}
