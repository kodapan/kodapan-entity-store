package se.kodapan.index.domain;

import se.kodapan.index.SerializableEntityObjectImpl;
import se.kodapan.lang.reflect.augmentation.Aggregation;
import se.kodapan.lang.reflect.augmentation.annotations.BinaryAssociationEnd;

import java.util.Date;

/**
 * @author kalle
 * @since 2010-jul-10 23:21:19
 */
public class LegalPerson extends SerializableEntityObjectImpl {

  private Date anno;
  private String name;

  @BinaryAssociationEnd(aggregation = Aggregation.COMPOSITE, otherEndClass = ContactInformation.class)
  private ContactInformation contactInformation = new ContactInformation();

  public LegalPerson() {
  }

  public LegalPerson(Date anno, String name) {
    this.anno = anno;
    this.name = name;
  }

  public Date getAnno() {
    return anno;
  }

  public void setAnno(Date anno) {
    this.anno = anno;
  }

  public ContactInformation getContactInformation() {
    return contactInformation;
  }

  public void setContactInformation(ContactInformation contactInformation) {
    this.contactInformation = contactInformation;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "LegalPerson{" +
        "anno=" + anno +
        ", name='" + name + '\'' +
        ", contactInformation=" + contactInformation +
        '}';
  }
}
