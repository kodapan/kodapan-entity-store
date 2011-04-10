package se.kodapan.entitystore;

/**
 * @author kalle
 * @since 2011-02-02 00.38
 */
public class PrimaryIndexListenerAdapter<IdentityType, EntityType> implements PrimaryIndexListener<IdentityType, EntityType> {

  @Override
  public void created(IdentityType identity, EntityType entity) {
  }

  @Override
  public void updated(IdentityType identity, EntityType entity) {
  }

  @Override
  public void deleted(IdentityType identity, EntityType entity) {
  }

}
