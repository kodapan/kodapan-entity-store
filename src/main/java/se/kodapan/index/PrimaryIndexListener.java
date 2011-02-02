package se.kodapan.index;

import java.io.Serializable;

/**
 * todo an object that decouple should trigger listeners in the composite part!
 *
 * @author kalle
 * @since 2011-02-02 00.36
 */
public interface PrimaryIndexListener<T extends EntityObject> {

  public abstract void created(T object);
  public abstract void updated(T object);
  public abstract void deleted(T object);

}
