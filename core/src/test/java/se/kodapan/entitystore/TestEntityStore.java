package se.kodapan.entitystore;

import junit.framework.TestCase;
import org.junit.Test;
import se.kodapan.entitystore.domain.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author kalle
 * @since 2010-jul-10 23:17:43
 */
public class TestEntityStore extends TestCase {

  @Test
  public void test() {

    EntityStoreImpl store = new EntityStoreImpl();

    /** register all non annotated classes with a primary index */
    store.getEntityTypes().add(Human.class);

    // register all primary indices with their identity.
    store.getPrimaryIndex(Long.class, Identifiable.class);
    store.getPrimaryIndex(Long.class, LegalPerson.class);
    store.getPrimaryIndex(Long.class, Human.class);
    store.getPrimaryIndex(Long.class, Organization.class);
    store.getPrimaryIndex(Long.class, AnInterfaceWithPrimaryIndex.class);

    BeanFieldSecondaryIndex<Long, LegalPerson, String> legalPersonsByName = new BeanFieldSecondaryIndex<Long, LegalPerson, String>("legalPersonsByName", store.getPrimaryIndex(Long.class, LegalPerson.class), String.class, "name");
    assertTrue(store.registerSecondaryIndex(legalPersonsByName));

    // try to register it again
    BeanFieldSecondaryIndex<Long, LegalPerson, String> legalPersonsByNameNotUsed = new BeanFieldSecondaryIndex<Long, LegalPerson, String>("legalPersonsByName", store.getPrimaryIndex(Long.class, LegalPerson.class), String.class, "name");
    assertFalse(store.registerSecondaryIndex(legalPersonsByNameNotUsed));

    final AtomicBoolean failed = new AtomicBoolean(false);
    store.getPrimaryIndex(Long.class, LegalPerson.class).getListeners().add(new PrimaryIndexListener<Long, LegalPerson>() {

      ConcurrentLinkedQueue<String> expectedEvents = new ConcurrentLinkedQueue<String>(Arrays.asList(
          "created Alice Charlotta Tegnér",
          "created Högsta domstolen",
          "created Robert Gustavsson",
          "deleted Alice Charlotta Tegnér",
          "updated Robert Gustavsson"
      ));

      @Override
      public void created(Long identity, LegalPerson entity) {
//        System.out.println("created " + object.getName());

        String expected = "created " + entity.getName();
        String got = expectedEvents.poll();
        if (!expected.equals(got)) {
          failed.set(false);
          fail("Expected '" + expected + "' + but got '" + got + "'");
        }
      }

      @Override
      public void updated(Long identity, LegalPerson entity) {
//        System.out.println("updated " + object.getName());

        String expected = "updated " + entity.getName();
        String got = expectedEvents.poll();
        if (!expected.equals(got)) {
          failed.set(false);
          fail("Expected '" + expected + "' + but got '" + got + "'");
        }
      }

      @Override
      public void deleted(Long identity, LegalPerson entity) {
//        System.out.println("deleted " + object.getName());

        String expected = "deleted " + entity.getName();
        String got = expectedEvents.poll();
        if (!expected.equals(got)) {
          failed.set(false);
          fail("Expected '" + expected + "' + but got '" + got + "'");
        }

      }


    });

    Organization hd = new Organization(null, "Högsta domstolen");

    Human alice = new Human(null, "Alice Charlotta Tegnér", "Alice Charlotta", "Alice", "Tegnér");
    Employment jurist = Employment.factory(null, null, "Jurist", alice, hd);
    Employment.factory(null, null, "Häradshövding", alice, hd);
    Employment.factory(null, null, "Protokollsekreterare", alice, hd);

    alice.setId(0l);
    hd.setId(1l);

    store.put(alice.getId(), alice);
    store.put(hd.getId(), hd);

    assertEquals(hd, store.get(Organization.class, hd.getId()));

    assertEquals(alice, store.get(Human.class, alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));

    assertEquals(alice, legalPersonsByName.get("Alice Charlotta Tegnér"));
    assertNull(legalPersonsByNameNotUsed.get("Alice Charlotta Tegnér"));

    Human bob = new Human(null, "Robert Gustavsson", "Robert", "Bob", "Gustavsson");
    bob.setId(2l);
    Employment.factory(null, null, "Snubbe", bob, hd);

    store.put(bob.getId(), bob);

    assertEquals(alice, store.get(Human.class, alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertEquals(alice, store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));

    assertEquals(bob, store.get(Human.class, bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(LegalPerson.class).get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(Human.class).get(bob.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(bob.getId()));

    store.remove(Human.class, alice.getId());
    assertEquals(0, alice.getEmployments().size());
    assertEquals(1, hd.getEmployees().size());
    assertFalse(hd.getEmployees().contains(jurist));
    assertNull(jurist.getEmployee());
    assertNull(jurist.getEmployer());
    assertNull(alice.getContactInformation());

    assertNull(store.get(Human.class, alice.getId()));
    assertNull(store.getPrimaryIndex(LegalPerson.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Human.class).get(alice.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(alice.getId()));
    assertNull(legalPersonsByName.get("Alice Charlotta Tegnér"));

    assertEquals(bob, store.get(Human.class, bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(LegalPerson.class).get(bob.getId()));
    assertEquals(bob, store.getPrimaryIndex(Human.class).get(bob.getId()));
    assertNull(store.getPrimaryIndex(Organization.class).get(bob.getId()));
    assertEquals(bob, legalPersonsByName.get("Robert Gustavsson"));

    BeanFieldSecondaryIndex<String, Human, String> humansByLastName = new BeanFieldSecondaryIndex<String, Human, String>("humansByLastName", store.getPrimaryIndex(String.class, Human.class), String.class, "lastName");
    store.registerSecondaryIndex(humansByLastName);

    assertEquals(bob, humansByLastName.get("Gustavsson"));

    bob.setLastName("Svensson");
    store.put(bob.getId(), bob);

    assertEquals(null, humansByLastName.get("Gustavsson"));
    assertEquals(bob, humansByLastName.get("Svensson"));

    System.currentTimeMillis();

    // makes sure its possible to serialize the whole store.
    clone(store);

    // make sure there are no non registered primary indices
    try {
      assertNull(store.getPrimaryIndex(DummyInterface.class));
      fail();
    } catch (EntityStoreException e) {
      // all good
    }

    assertNotNull(store.getPrimaryIndex(AnInterfaceWithPrimaryIndex.class));
    assertNotNull(store.getPrimaryIndex(Identifiable.class));


    if (failed.get()) {
      fail("Listeners failed. See log.");
    }
  }

  public static <T> T clone(T object) {
    if (object == null) {
      return null;
    }
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(object);
      oos.close();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      object = (T)ois.readObject();
      ois.close();
      baos.close();
      return object;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


}
