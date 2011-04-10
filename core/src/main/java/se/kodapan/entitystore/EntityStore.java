package se.kodapan.entitystore;

/**
 * @author kalle
 * @since 2011-04-09 04.45
 */
public interface EntityStore {


  /**
   *
   * @param identity
   * @param entity
   * @param <IdentityType>
   * @param <EntityType>
   * @return
   * @throws EntityStoreException
   */
  public abstract <IdentityType, EntityType> EntityType put(IdentityType identity, EntityType entity) throws EntityStoreException;

  /**
   *
   * @param entityType
   * @param identity
   * @param <IdentityType>
   * @param <EntityType>
   * @return
   * @throws EntityStoreException
   */
  public abstract <IdentityType, EntityType> EntityType get(Class<EntityType> entityType, IdentityType identity) throws EntityStoreException;

  /**
   *
   * @param entityType
   * @param identity
   * @param <IdentityType>
   * @param <EntityType>
   * @return
   * @throws EntityStoreException
   */
  public abstract <IdentityType, EntityType> EntityType remove(Class<EntityType> entityType, IdentityType identity) throws EntityStoreException;


  /**
   * @param entityType
   * @param <EntityType>
   * @return
   * @throws EntityStoreException mainly if method accessed prior to using {@link #getPrimaryIndex(Class, Class)} to register the identity class.
   */
  public abstract <EntityType> PrimaryIndex<Object, EntityType> getPrimaryIndex(Class<EntityType> entityType) throws EntityStoreException;

  /**
   * Retrieves primary index for the given parameters,
   * or register and creates it if not yet existing.
   *
   * @param identityType must be of the top level implementation class when registering the primary index, but when registered this parameters can be any super class or interfaces of the identity.
   * @param entityType
   * @param <IdentityType>
   * @param <EntityType>
   * @return primary index with identity- and entity type of the supplied classes
   * @throws EntityStoreException
   */
  public abstract <IdentityType, EntityType> PrimaryIndex<IdentityType, EntityType> getPrimaryIndex(Class<IdentityType> identityType, Class<EntityType> entityType) throws EntityStoreException;
}
