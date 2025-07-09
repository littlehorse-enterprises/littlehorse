package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name="person")
public class Person {
  public String firstName;
  public String lastName;
  public Integer accountId;
  public String phoneNumber;

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }
}
