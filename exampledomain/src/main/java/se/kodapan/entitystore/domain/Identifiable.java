package se.kodapan.entitystore.domain;

import se.kodapan.entitystore.Entity;

import java.io.Serializable;

/**
 * @author kalle
 * @since 2011-04-09 18.54
 */
@Entity
public abstract class Identifiable implements Serializable {

  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Identifiable that = (Identifiable) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
