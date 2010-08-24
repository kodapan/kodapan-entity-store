package se.kodapan.index;

import junit.framework.TestCase;
import org.junit.Test;
import se.kodapan.index.domain.Employment;
import se.kodapan.index.domain.Human;
import se.kodapan.index.domain.LegalPerson;
import se.kodapan.index.domain.Organization;
import se.kodapan.io.SerializableTool;
import se.kodapan.lang.reflect.augmentation.Mirror;

import java.util.Date;

/**
 * @author kalle
 * @since 2010-jul-10 23:17:43
 */
public class TestEntityStore extends TestCase {

  @Test
  public void test() {

    EntityStore store = new EntityStore();
    store.setIdentityFactory(new DeterministicUIDHandler());

    BeanFieldSecondaryIndex<LegalPerson, String> legalPersonsByName = new BeanFieldSecondaryIndex<LegalPerson, String>("legalPersonsByName", store.getPrimaryIndex(LegalPerson.class), String.class, "name");
    assertTrue(store.registerSecondaryIndex(legalPersonsByName));

    // try to register it again
    BeanFieldSecondaryIndex<LegalPerson, String> legalPersonsByNameNotUsed = new BeanFieldSecondaryIndex<LegalPerson, String>("legalPersonsByName", store.getPrimaryIndex(LegalPerson.class), String.class, "name");
    assertFalse(store.registerSecondaryIndex(legalPersonsByNameNotUsed));


    Organization hd = new Organization(null, "Högsta domstolen");

    Human alice = new Human(null, "Alice Charlotta Tegnér", "Alice Charlotta", "Alice", "Tegnér");
    Employment jurist = Employment.factory(null, null, "Jurist", alice, hd);
    Employment.factory(null, null, "Häradshövding", alice, hd);
    Employment.factory(null, null, "Protokollsekreterare", alice, hd);

    alice.setId(store.getIdentityFactory().nextIdentity(new Date()));
    hd.setId(store.getIdentityFactory().nextIdentity(new Date()));

    store.put(alice);
    store.put(hd);

    assertEquals(hd, store.get(hd.getId()));

    assertEquals(alice, store.get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));

    assertEquals(alice, legalPersonsByName.get("Alice Charlotta Tegnér"));
    assertNull(legalPersonsByNameNotUsed.get("Alice Charlotta Tegnér"));

    Human bob = new Human(null, "Robert Gustavsson", "Robert", "Bob", "Gustavsson");
    bob.setId(store.getIdentityFactory().nextIdentity(new Date()));
    Employment.factory(null, null, "Snubbe", bob, hd);

    store.put(bob);

    assertEquals(alice, store.get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));

    assertEquals(bob, store.get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(LegalPerson.class).get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(Human.class).get(bob.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(bob .getId()));

    store.remove(alice);
    assertEquals(0, alice.getEmployments().size());
    assertEquals(1, hd.getEmployees().size());
    assertFalse(hd.getEmployees().contains(jurist));
    assertNull(jurist.getEmployee());
    assertNull(jurist.getEmployer());
    assertNull(alice.getContactInformation());

    assertNull(store.get(alice.getId()));
    assertNull(store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));
    assertNull(legalPersonsByName.get("Alice Charlotta Tegnér"));

    assertEquals(bob, store.get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(LegalPerson.class).get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(Human.class).get(bob.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(bob .getId()));
    assertEquals(bob, legalPersonsByName.get("Robert Gustavsson"));

    BeanFieldSecondaryIndex<Human, String> humansByLastName = new BeanFieldSecondaryIndex<Human, String>("humansByLastName", store.getPrimaryIndex(Human.class), String.class, "lastName");
    store.registerSecondaryIndex(humansByLastName);

    assertEquals(bob, humansByLastName.get("Gustavsson"));

    bob.setLastName("Svensson");
    store.put(bob);

    assertEquals(null, humansByLastName.get("Gustavsson"));
    assertEquals(bob, humansByLastName.get("Svensson"));

    System.currentTimeMillis();

    // makes sure its possible to serialize the graph.
    SerializableTool.clone(store);

    assertNull(store.getPrimaryIndex(SerializableEntityObjectImpl.class));
  }

}
