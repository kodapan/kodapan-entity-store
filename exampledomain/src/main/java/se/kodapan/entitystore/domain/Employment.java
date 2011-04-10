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

package se.kodapan.entitystore.domain;

import se.kodapan.lang.reflect.augmentation.annotations.BinaryAssociationClassEnd;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kalle
 * @since 2010-jul-10 23:31:04
 */
public class Employment implements Serializable {

  private static final long serialVersionUID = 1l;
  
  private Date started;
  private Date ended;

  private String title;

  @BinaryAssociationClassEnd(otherEndName = "employments", otherEndClass = Human.class)
  private Human employee;

  @BinaryAssociationClassEnd(otherEndName = "employees", otherEndClass = Organization.class)
  private Organization employer;

  public Employment() {
  }

  public Employment(Date started, Date ended, String title, Human employee, Organization employer) {
    this.started = started;
    this.ended = ended;
    this.title = title;
    this.employee = employee;
    this.employer = employer;
  }

  public static Employment factory(Date started, Date ended, String title, Human employee, Organization employer) {
    Employment employment = new Employment(started, ended, title, employee, employer);
    employee.getEmployments().add(employment);
    employer.getEmployees().add(employment);
    return employment;
  }
  
  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public Date getEnded() {
    return ended;
  }

  public void setEnded(Date ended) {
    this.ended = ended;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Human getEmployee() {
    return employee;
  }

  public void setEmployee(Human employee) {
    this.employee = employee;
  }

  public Organization getEmployer() {
    return employer;
  }

  public void setEmployer(Organization employer) {
    this.employer = employer;
  }
}
