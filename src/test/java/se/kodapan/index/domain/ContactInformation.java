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

package se.kodapan.index.domain;

import se.kodapan.io.SerializableBean;
import se.kodapan.lang.reflect.augmentation.Aggregation;
import se.kodapan.lang.reflect.augmentation.annotations.BinaryAssociationEnd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kalle
 * @since 2010-jul-13 20:31:22
 */
public class ContactInformation extends SerializableBean {

  @BinaryAssociationEnd(aggregation = Aggregation.COMPOSITE, otherEndClass = Address.class)
  private Address address = new Address();

  @BinaryAssociationEnd(aggregation = Aggregation.COMPOSITE, otherEndClass = PhoneNumber.class)
  private List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public List<PhoneNumber> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ContactInformation that = (ContactInformation) o;

    if (address != null ? !address.equals(that.address) : that.address != null) return false;
    if (phoneNumbers != null ? !phoneNumbers.equals(that.phoneNumbers) : that.phoneNumbers != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = address != null ? address.hashCode() : 0;
    result = 31 * result + (phoneNumbers != null ? phoneNumbers.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ContactInformation{" +
        "address=" + address +
        ", phoneNumbers=" + phoneNumbers +
        '}';
  }
}
