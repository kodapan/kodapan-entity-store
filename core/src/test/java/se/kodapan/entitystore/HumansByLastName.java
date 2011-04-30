package se.kodapan.entitystore;

import se.kodapan.collections.SetMap;
import se.kodapan.entitystore.domain.Human;

/**
 * @author kalle
 * @since 2011-04-10 12.29
 */
public class HumansByLastName extends SimpleMapSetSecondaryIndex<Long, Human> {

  public HumansByLastName() {
  }

  public HumansByLastName(String name, PrimaryIndex<Long, Human> longHumanPrimaryIndex) {
    super(name, longHumanPrimaryIndex);
  }

  public HumansByLastName(String name, PrimaryIndex<Long, Human> longHumanPrimaryIndex, SetMap<Object, Human> objectHumanMapSet) {
    super(name, longHumanPrimaryIndex, objectHumanMapSet);
  }

  @Override
      public String getSecondaryKey(Human human) throws UnsupportedOperationException {
        return human.getLastName();
      }

      @Override
      public String getSecondaryKey(Object... parameters) {
        return (String)parameters[0];
      }
}
