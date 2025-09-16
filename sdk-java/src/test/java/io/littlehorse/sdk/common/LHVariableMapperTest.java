package io.littlehorse.sdk.common;

import static io.littlehorse.sdk.common.LHVariableMapper.*;
import static io.littlehorse.sdk.common.LHVariableMapper.as;
import static org.assertj.core.api.Assertions.*;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.util.Collection;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class LHVariableMapperTest {

    @Test
    void shouldConvertJsonArrToList() {
        String jsonArray = "[" + "  {"
                + "    \"name\": \"John Doe\","
                + "    \"age\": 30,"
                + "    \"city\": \"New York\""
                + "  },"
                + "  {"
                + "    \"name\": \"Jane Smith\","
                + "    \"age\": 25,"
                + "    \"city\": \"Los Angeles\""
                + "  },"
                + "  {"
                + "    \"name\": \"Bob Johnson\","
                + "    \"age\": 35,"
                + "    \"city\": \"Chicago\""
                + "  }"
                + "]";
        VariableValue jsonArr = VariableValue.newBuilder().setJsonArr(jsonArray).build();
        Collection<Person> listOfPeople = asList(jsonArr, Person.class);
        assertThat(listOfPeople)
                .hasSize(3)
                .allMatch(Objects::nonNull)
                .allMatch(person -> Objects.nonNull(person.name))
                .allMatch(person -> Objects.nonNull(person.city))
                .allMatch(person -> person.age > 0);
    }

    @Test
    void shouldExtractIntValue() {
        assertThat(asInt(VariableValue.newBuilder().setInt(22).build())).isEqualTo(22);
    }

    @Test
    void shouldExtractLongValue() {
        assertThat(asLong(VariableValue.newBuilder().setInt(22L).build())).isEqualTo(22L);
    }

    @Test
    void shouldExtractDoubleValue() {
        assertThat(asDouble(VariableValue.newBuilder().setDouble(22.3).build())).isEqualTo(22.3);
    }

    @Test
    void shouldConvertBooleanValue() {
        assertThat(asBoolean(VariableValue.newBuilder().setBool(true).build())).isEqualTo(true);
    }

    @Test
    void shouldConvertBytesValue() {
        ByteString bytes = ByteString.copyFrom("pedro".getBytes());
        assertThat(asBytes(VariableValue.newBuilder().setBytes(bytes).build())).isEqualTo("pedro".getBytes());
    }

    @Test
    void shouldConvertStringValue() {
        assertThat(asString(VariableValue.newBuilder().setStr("pedro").build())).isEqualTo("pedro");
    }

    @Test
    void shouldVerifyVariableTypeBeforeConvertingTheValue() {
        VariableValue stringVal = VariableValue.newBuilder().setStr("str").build();
        VariableValue boolVal = VariableValue.newBuilder().setBool(true).build();
        assertThatThrownBy(() -> asString(boolVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to String");
        assertThatThrownBy(() -> asInt(boolVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to Integer");
        assertThatThrownBy(() -> asLong(boolVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to Long");
        assertThatThrownBy(() -> asDouble(boolVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to Double");
        assertThatThrownBy(() -> asBoolean(stringVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type STR to Boolean");
        assertThatThrownBy(() -> asBytes(boolVal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to Byte");
        assertThatThrownBy(() -> asList(boolVal, Person.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type BOOL to Collection");
        assertThat(as(boolVal, Person.class)).isNull();
    }

    public static class Person {
        public String name, city;
        public int age;
    }

    @Test
    void shouldVerifyUtcTimestampVariableTypeBehavior() {
        Timestamp protoTs =
                Timestamp.newBuilder().setSeconds(1690000000L).setNanos(0).build();
        VariableValue tsVar =
                VariableValue.newBuilder().setUtcTimestamp(protoTs).build();

        assertThatThrownBy(() -> asString(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to String");

        assertThatThrownBy(() -> asInt(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Integer");

        assertThatThrownBy(() -> asLong(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Long");

        assertThatThrownBy(() -> asDouble(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Double");

        assertThatThrownBy(() -> asBoolean(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Boolean");

        assertThatThrownBy(() -> asBytes(tsVar))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Byte");

        assertThatThrownBy(() -> asList(tsVar, Person.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not possible to convert variable type UTC_TIMESTAMP to Collection");

        // as(...) should return null since there's no json payload
        assertThat(as(tsVar, Person.class)).isNull();
    }
}
