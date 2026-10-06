package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

@LHStructDef(value = "person", description = "A person with a name and optional home address.")
public record Person(
        @LHStructField(description = "This is the first name of the person, i.e. their given name.") String firstName,
        @LHStructField(description = "This is the last name of the person, i.e. their family name.") String lastName,
        @LHStructField(description = "The home address of the person.", masked = true, isNullable = true)
                Address homeAddress) {
    @Override
    public String toString() {
        return String.format("%s %s", firstName, lastName);
    }
}
