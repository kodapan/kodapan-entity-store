package se.kodapan.index.domain;

import se.kodapan.lang.reflect.augmentation.annotations.BinaryAssociationEnd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kalle
 * @since 2010-jul-10 23:21:24
 */
public class Human extends LegalPerson {

  private String firstNames;
  private String preferredName;
  private String lastName;

  @BinaryAssociationEnd(otherEndName = "employees", otherEndClass = Organization.class, associationClass = Employment.class, multiplicity = "0..*")
  private List<Employment> employments = new ArrayList<Employment>();

  public Human() {
  }

  public Human(Date anno, String name, String firstNames, String preferredName, String lastName) {
    super(anno, name);
    this.firstNames = firstNames;
    this.preferredName = preferredName;
    this.lastName = lastName;
  }



  public List<Employment> getEmployments() {
    return employments;
  }

  public void setEmployments(List<Employment> employments) {
    this.employments = employments;
  }

  public String getFirstNames() {
    return firstNames;
  }

  public void setFirstNames(String firstNames) {
    this.firstNames = firstNames;
  }

  public String getPreferredName() {
    return preferredName;
  }

  public void setPreferredName(String preferredName) {
    this.preferredName = preferredName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}
