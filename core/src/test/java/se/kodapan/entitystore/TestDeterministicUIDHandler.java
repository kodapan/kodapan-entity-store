/*
 * Copyright 2010 Kodapan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.kodapan.entitystore;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author kalle
 * @since 2010-jul-11 07:02:11
 */
public class TestDeterministicUIDHandler extends TestCase {

  @Test
  public void test() throws Exception {

    Date date = new Date();

    DeterministicUIDHandler duid = new DeterministicUIDHandler();

    String duid0 = duid.nextIdentity(date);
    assertNotNull(duid0);


    String duid1 = duid.nextIdentity(date);
    assertNotNull(duid1);
    assertNotSame(duid0, duid1);

    String duid2 = duid.nextIdentity(date);
    assertNotNull(duid2);
    assertNotSame(duid1, duid2);
    Thread.sleep(1000);

    String duid3 = duid.nextIdentity(new Date());
    assertNotNull(duid3);
    assertNotSame(duid2, duid3);
    try {
      duid.nextIdentity(date);
      fail();
    } catch (Exception e) {
    }

    System.currentTimeMillis();

  }

  @Test
  public void testDecode() throws Exception {
    Date now = new Date();
    DeterministicUIDHandler handler = new DeterministicUIDHandler();

    assertEquals(new DeterministicUIDHandler.DeterministicUID(now, 1), DeterministicUIDHandler.decode(handler.nextIdentity(now)));
    assertEquals(new DeterministicUIDHandler.DeterministicUID(now, 2), DeterministicUIDHandler.decode(handler.nextIdentity(now)));
    assertEquals(new DeterministicUIDHandler.DeterministicUID(now, 3), DeterministicUIDHandler.decode(handler.nextIdentity(now)));

  }

  @Test
  public void testUniquness() {
    Date now = new Date();
    DeterministicUIDHandler duid = new DeterministicUIDHandler();
    Set<String> seen = new LinkedHashSet<String>();
    for (int i = 0; i < 100000; i++) {
      if (!seen.add(duid.nextIdentity(now))) {
        fail();
      }
    }
  }

}
