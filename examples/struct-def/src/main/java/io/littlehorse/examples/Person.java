package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;

@LHStructDef(name = "person")
public class Person {
    public String old;
    public String lastName;
    public String[] names;
    public Integer accountId;
    public String phoneNumber;
    public Car car;

    public Car getCar() {
        return new Car();
    }

    public Integer getFirstName() {
        return 5;
    }

    public void setFirstName(String name) {
        this.old = name;
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
