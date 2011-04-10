package se.kodapan.entitystore.domain;

import se.kodapan.lang.reflect.augmentation.annotations.BinaryAssociationEnd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kalle
 * @since 2010-jul-10 23:21:24
 */
public class Human extends LegalPerson {

  private static final long serialVersionUID = 1l;
  
  private String firstNames;
  private String preferredName;
  private String lastName;

  @BinaryAssociationEnd(otherEndName = "employees", otherEndClass = Organization.class, associationClass = Employment.class, multiplicity = "0..*")
  private List<Employment> employments = new ArrayList<Employment>();

  // parent and children are actually never used in the tests,
  // they are here to make sure that null values work!

  @BinaryAssociationEnd(otherEndName = "parents", otherEndClass = Human.class, multiplicity = "0..*")
  private List<Human> children;

  @BinaryAssociationEnd(otherEndName = "children", otherEndClass = Human.class, multiplicity = "0..2")
  private List<Human> parents;  

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

  public List<Human> getChildren() {
    return children;
  }

  public void setChildren(List<Human> children) {
    this.children = children;
  }

  public List<Human> getParents() {
    return parents;
  }

  public void setParents(List<Human> parents) {
    this.parents = parents;
  }

  @Override
  public String toString() {
    return "Human{" +
        "firstNames='" + firstNames + '\'' +
        ", preferredName='" + preferredName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", employments=" + employments +
        ", children=" + children +
        ", parents=" + parents +
        '}';
  }
}
