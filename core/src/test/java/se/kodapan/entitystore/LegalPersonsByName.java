package se.kodapan.entitystore;

import se.kodapan.collections.MapSet;
import se.kodapan.entitystore.domain.LegalPerson;

/**
 * @author kalle
 * @since 2011-04-10 12.27
 */
public class LegalPersonsByName extends SimpleMapSetSecondaryIndex<Long, LegalPerson> {

  public LegalPersonsByName() {
  }

  public LegalPersonsByName(String name, PrimaryIndex<Long, LegalPerson> longLegalPersonPrimaryIndex) {
    super(name, longLegalPersonPrimaryIndex);
  }

  public LegalPersonsByName(String name, PrimaryIndex<Long, LegalPerson> longLegalPersonPrimaryIndex, MapSet<Object, LegalPerson> objectLegalPersonMapSet) {
    super(name, longLegalPersonPrimaryIndex, objectLegalPersonMapSet);
  }

  @Override
  public String getSecondaryKey(LegalPerson legalPerson) throws UnsupportedOperationException {
    return legalPerson.getName();
  }

  @Override
  public String getSecondaryKey(Object... parameters) {
    return (String) parameters[0];
  }

}
