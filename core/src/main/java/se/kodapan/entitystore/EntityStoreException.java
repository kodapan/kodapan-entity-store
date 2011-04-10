package se.kodapan.entitystore;

/**
 * @author kalle
 * @since 2011-04-09 20.51
 */
public class EntityStoreException extends RuntimeException {

  private static long serialVersionUID = 1l;

  public EntityStoreException() {
  }

  public EntityStoreException(String s) {
    super(s);
  }

  public EntityStoreException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public EntityStoreException(Throwable throwable) {
    super(throwable);
  }

}
