package se.kodapan.entitystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
