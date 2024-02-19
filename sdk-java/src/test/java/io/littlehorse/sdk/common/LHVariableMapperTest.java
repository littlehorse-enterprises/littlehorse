package io.littlehorse.sdk.common;
import com.google.protobuf.ByteString;
import io.littlehorse.sdk.common.proto.VariableValue;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static io.littlehorse.sdk.common.LHVariableMapper.*;
public class LHVariableMapperTest {

    @Test
    void shouldConvertJsonArrToList(){
        String jsonArray =
                "[" +
                        "  {" +
                        "    \"name\": \"John Doe\"," +
                        "    \"age\": 30," +
                        "    \"city\": \"New York\"" +
                        "  }," +
                        "  {" +
                        "    \"name\": \"Jane Smith\"," +
                        "    \"age\": 25," +
                        "    \"city\": \"Los Angeles\"" +
                        "  }," +
                        "  {" +
                        "    \"name\": \"Bob Johnson\"," +
                        "    \"age\": 35," +
                        "    \"city\": \"Chicago\"" +
                        "  }" +
                        "]";
        VariableValue jsonArr = VariableValue.newBuilder().setJsonArr(jsonArray).build();
        List<Person> listOfPeople = asList(jsonArr, Person.class);
        assertThat(listOfPeople).hasSize(3)
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
    void shouldExtractBooleanValue() {
        assertThat(asBoolean(VariableValue.newBuilder().setBool(true).build())).isEqualTo(true);
    }

    @Test
    void shouldExtractBytesValue() {
        ByteString bytes = ByteString.copyFrom("pedro".getBytes());
        assertThat(asBytes(VariableValue.newBuilder().setBytes(bytes).build()))
                .isEqualTo("pedro".getBytes());
    }

    @Test
    void shouldExtractStringValue() {
        assertThat(asString(VariableValue.newBuilder().setStr("pedro").build())).isEqualTo("pedro");
    }



    public static class Person {
        public String name, city;
        public int age;
    }

}
