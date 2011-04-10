package se.kodapan.entitystore;

/**
 * @author kalle
 * @since 2011-04-09 22.04
 */
public class NotRegisteredAsPrimaryIndexException extends EntityStoreException {

  private static long serialVersionUID = 1l;

  private Class entityType;

  public NotRegisteredAsPrimaryIndexException() {
  }

  public NotRegisteredAsPrimaryIndexException(Class entityType) {
    super("Class " + entityType.getName() + " is not registered or annotated to have a primary index");
    this.entityType = entityType;
  }

  public Class getEntityType() {
    return entityType;
  }

  public void setEntityType(Class entityType) {
    this.entityType = entityType;
  }
}
