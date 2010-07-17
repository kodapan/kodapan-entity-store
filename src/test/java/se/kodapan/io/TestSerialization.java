package se.kodapan.io;

import junit.framework.TestCase;
import org.junit.Test;
import se.kodapan.index.*;
import se.kodapan.index.domain.*;
import se.kodapan.lang.reflect.ReflectionUtil;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * @author kalle
 * @since 2010-jan-07 14:16:43
 */
public class TestSerialization extends TestCase {

  private List<Field> manuallyRequiredChecks;

  @Override
  protected void setUp() throws Exception {
    manuallyRequiredChecks = new ArrayList<Field>();
  }

  @Test
  public void test() throws Exception {

    assertSerializationMatches(LegalPerson.class);
    assertSerializationMatches(Human.class);
    assertSerializationMatches(Employment.class);
    assertSerializationMatches(Organization.class);
    assertSerializationMatches(ContactInformation.class);
    assertSerializationMatches(Address.class);
    assertSerializationMatches(PhoneNumber.class);

    if (manuallyRequiredChecks.size() > 0) {
      System.out.println("Manual checking of serialization is required for the following fields:");
      for (Field field : manuallyRequiredChecks) {
        System.out.println(field.getDeclaringClass().getName() + "#" + field.getName());
      }
    }

  }


  /**
   * sets the id of my entity objects.
   */
  public void setNonNullableField(Object object) {
    if (object instanceof EntityObject) {
      ((EntityObject) object).setId(UUID.randomUUID().toString());
    }
  }

  public void assertSerializationMatches(Class _class) throws Exception {

    System.out.println("Testing " + _class.getName());

    Object object = _class.newInstance();
    setNonNullableField(object);

    List<Field> allFields = new ArrayList<Field>(ReflectionUtil.gatherAllBeanFields(_class).values());
    assertNonNullSerializedValuesEquals(_class, object, allFields);
    assertNullSerializedValuesEquals(_class, object, allFields);


    System.out.println();

  }

  private void assertNonNullSerializedValuesEquals(Class _class, Object object, List<Field> allFields) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
    for (int i = 0; i < allFields.size(); i++) {
      Field field = allFields.get(i);

      System.out.println("Setting " + _class.getName() + "#" + field.getName());

      // todo should really make sure that the default value is not the same as the value we set it to!

      Method setter = ReflectionUtil.getSetter(field);
      Method getter = ReflectionUtil.getGetter(field);
      if (field.getType().isArray()) {
        Object theArray = Array.newInstance(field.getType().getComponentType(), i);
        setter.invoke(object, theArray);
      } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
        setter.invoke(object, !((Boolean) getter.invoke(object))); // set to the !default
      } else if (field.getType() == Byte.class || field.getType() == byte.class) {
        // todo
        if (i > 128) {
          throw new RuntimeException("This code can only handle 128 attributes safely!");
        }
        setter.invoke(object, (byte) i);
      } else if (field.getType() == Short.class || field.getType() == short.class) {
        setter.invoke(object, (short) i);
      } else if (field.getType() == Integer.class || field.getType() == int.class) {
        setter.invoke(object, Integer.valueOf(i));
      } else if (field.getType() == Long.class || field.getType() == long.class) {
        setter.invoke(object, Long.valueOf(i));
      } else if (field.getType() == Float.class || field.getType() == float.class) {
        setter.invoke(object, Float.valueOf(i));
      } else if (field.getType() == Double.class || field.getType() == double.class) {
        setter.invoke(object, Double.valueOf(i));
      } else if (field.getType() == String.class) {
        setter.invoke(object, String.valueOf(i));
      } else if (field.getType() == List.class) {
        List list = (List) getter.invoke(object);
        if (list == null) {
          setter.invoke(object, new ArrayList());
        } else {
          setter.invoke(object, list.getClass().newInstance());
        }
      } else if (field.getType() == Set.class) {
        Set set = (Set) getter.invoke(object);
        if (set == null) {
          setter.invoke(object, new HashSet());
        } else {
          setter.invoke(object, set.getClass().newInstance());
        }
      } else if (field.getType() == Map.class) {
        Map map = (Map) getter.invoke(object);
        if (map == null) {
          setter.invoke(object, new HashMap());
        } else {
          setter.invoke(object, map.getClass().newInstance());
        }
      } else if (field.getType().isEnum()) {
        // set value to something different than default value.
        Object value = getter.invoke(object);
        if (value != field.getType().getEnumConstants()[0]) {
          setter.invoke(object, field.getType().getEnumConstants()[0]);
        } else {
          setter.invoke(object, field.getType().getEnumConstants()[1]);
        }
      } else {
        Object instance = null;
        if (Modifier.isAbstract(field.getType().getModifiers())) {
          // todo i have no clue how to handle this. need to set one of the implementations.
          manuallyRequiredChecks.add(field);
        } else {
          instance = field.getType().newInstance();
        }
        setNonNullableField(instance);
        setter.invoke(object, instance);
      }
    }

    assertSerializedEquals(_class, object);

  }

  private void assertNullSerializedValuesEquals(Class _class, Object object, List<Field> allFields) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
    for (int i = 0; i < allFields.size(); i++) {
      Field field = allFields.get(i);

      System.out.println("Setting " + _class.getName() + "#" + field.getName());

      Method setter = ReflectionUtil.getSetter(field);
      Method getter = ReflectionUtil.getGetter(field);
      if (!field.getType().isPrimitive()) {
        setter.invoke(object, (Object)null);
      } else if (field.getType() == boolean.class) {
        setter.invoke(object, !((Boolean) getter.invoke(object))); // set to the !default
      } else if (field.getType() == byte.class) {
        // todo
        if (i > 128) {
          throw new RuntimeException("This code can only handle 128 attributes safely!");
        }
        setter.invoke(object, (byte) i);
      } else if (field.getType() == short.class) {
        setter.invoke(object, (short) i);
      } else if (field.getType() == int.class) {
        setter.invoke(object, i);
      } else if (field.getType() == long.class) {
        setter.invoke(object, (long) i);
      } else if (field.getType() == float.class) {
        setter.invoke(object, (float) i);
      } else if (field.getType() == double.class) {
        setter.invoke(object, (double) i);
      }
    }

    assertSerializedEquals(_class, object);

  }


  private void assertSerializedEquals(Class _class, Object object) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(object);
    oos.close();
    baos.close();
    ObjectInputStream os = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

    Object clone = os.readObject();

    for (Field field : ReflectionUtil.gatherAllBeanFields(object.getClass()).values()) {

      System.out.println("Testing " + _class.getName() + "#" + field.getName());

      Method getter = ReflectionUtil.getGetter(field);
      if (field.getType().isArray()) {
        arraysEquals(field, getter.invoke(object), getter.invoke(clone));
      } else {
        assertEquals("Field " + field.getName() + " in class " + object.getClass().getName() + " does not match.", getter.invoke(object), getter.invoke(clone));
      }

    }
  }

  private boolean arraysEquals(Field field, Object a, Object b) {

    if (!a.getClass().equals(b.getClass())) {
      fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: Not the same Class");
    }

    if (Array.getLength(a) != Array.getLength(b)) {
      fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: Not the same length");
    }

    for (int i = 0; i < Array.getLength(a); i++) {
      if (field.getType().isPrimitive()) {

        if (field.getType() == boolean.class) {
          if (Array.getBoolean(a, i) != (Array.getBoolean(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }

        } else if (field.getType() == byte.class) {
          if (Array.getByte(a, i) != (Array.getByte(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }
        } else if (field.getType() == short.class) {
          if (Array.getShort(a, i) != (Array.getShort(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }

        } else if (field.getType() == int.class) {
          if (Array.getInt(a, i) != (Array.getInt(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }

        } else if (field.getType() == long.class) {
          if (Array.getLong(a, i) != (Array.getLong(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }

        } else if (field.getType() == float.class) {
          if (Array.getFloat(a, i) != (Array.getFloat(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }

        } else if (field.getType() == double.class) {
          if (Array.getDouble(a, i) != (Array.getDouble(b, i))) {
            fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
          }
        }
      } else {
        if (!Array.get(a, i).equals(Array.get(b, i))) {
          fail("Array field " + field.getName() + " in class " + field.getDeclaringClass().getClass().getName() + " does not match: a[" + i + "] != b[" + i + "]");
        }
      }
    }

    return false;
  }


}