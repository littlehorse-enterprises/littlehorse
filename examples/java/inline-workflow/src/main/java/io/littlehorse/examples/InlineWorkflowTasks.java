package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class InlineWorkflowTasks {

    @LHTaskMethod(value = "inline-greet", description = "Greet someone by name and age.")
    public String greet(String name, int age) {
        return "Hello, " + name + "! You are " + age + " years old.";
    }

    @LHTaskMethod(value = "inline-add", description = "Add two integers.")
    public int add(int left, int right) {
        return left + right;
    }

    @LHTaskMethod(value = "inline-greet-person", description = "Greet a person provided as a struct.")
    public String greetPerson(Person person) {
        return greet(person.getName(), person.getAge());
    }
}
