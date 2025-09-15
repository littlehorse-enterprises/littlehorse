package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "person")
public class Person {
    public String firstName;
    public String lastName;
    public Integer accountId;

    public Person() {}

    public Person(String firstName, String lastName, Integer accountId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountId = accountId;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}
