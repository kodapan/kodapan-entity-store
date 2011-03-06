package se.kodapan.index;

import java.io.Serializable;

/**
 * Events that cause changes to a primary index will be triggered in a new thread to the registered listeners.
 *
 * todo an object that decouple should trigger listeners in the composite part!
 *
 * todo the interface should define if events should be triggered
 * todo in a new thread each,
 * todo same thread for all
 * todo or the same thread as the event that triggered the change.
 *
 * todo to be able to read before decoupling one will have to refactor this to support a bunch of other methods instead,
 * todo ie beforeDelete, afterDelete, beforeCreated, afterCreated, beforeUpdated, afterUpdated
 *
 * @author kalle
 * @since 2011-02-02 00.36
 */
public interface PrimaryIndexListener<T extends EntityObject> {

  public abstract void created(T object);
  public abstract void updated(T object);
  public abstract void deleted(T object);

}
