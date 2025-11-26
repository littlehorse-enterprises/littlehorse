package io.littlehorse.sdk.common;

import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class LHLibUtilTest {

    @Test
    void shouldParseParentWfRunId() {
        String child = "anakin";
        String parent = "obi-wan";
        String grandparent = "qui-gon";

        WfRunId quiGon = WfRunId.newBuilder().setId(grandparent).build();
        WfRunId obiWan =
                WfRunId.newBuilder().setId(parent).setParentWfRunId(quiGon).build();
        WfRunId anakin =
                WfRunId.newBuilder().setId(child).setParentWfRunId(obiWan).build();

        String anakinStr = LHLibUtil.wfRunIdToString(anakin);
        Assertions.assertThat(anakinStr).isEqualTo(grandparent + "_" + parent + "_" + child);

        // Darth vader is Anakin but re-constructed
        WfRunId darthVader = LHLibUtil.wfRunIdFromString(anakinStr);
        Assertions.assertThat(darthVader.getId()).isEqualTo(child);
        Assertions.assertThat(darthVader.getParentWfRunId().getId()).isEqualTo(parent);
        Assertions.assertThat(darthVader.getParentWfRunId().getParentWfRunId().getId())
                .isEqualTo(grandparent);
    }

    @Test
    void shouldToStringWfRunIdWhenNoParent() {
        String idStr = "asdfasdf";
        WfRunId id = WfRunId.newBuilder().setId(idStr).build();
        Assertions.assertThat(LHLibUtil.wfRunIdToString(id)).isEqualTo(idStr);

        String taskGuid = "task-guid";
        TaskRunId taskRunid =
                TaskRunId.newBuilder().setWfRunId(id).setTaskGuid(taskGuid).build();
        Assertions.assertThat(LHLibUtil.taskRunIdToString(taskRunid)).isEqualTo(idStr + "/" + taskGuid);
    }

    @Test
    void shouldIncludeParentWfRunidOnToString() {
        String parentId = "parent";
        String childId = "child";
        WfRunId id = WfRunId.newBuilder()
                .setId(childId)
                .setParentWfRunId(WfRunId.newBuilder().setId(parentId))
                .build();

        Assertions.assertThat(LHLibUtil.wfRunIdToString(id)).isEqualTo(parentId + "_" + childId);
    }

    @Test
    void shouldConvertJavaStringArrayClassToLHJsonArr() {
        Assertions.assertThat(LHLibUtil.javaClassToLHVarType(String[].class)).isEqualTo(VariableType.JSON_ARR);
    }

    @Test
    void shouldConvertJavaPojoArrayClassToLHJsonArr() {
        Assertions.assertThat(LHLibUtil.javaClassToLHVarType(Book[].class)).isEqualTo(VariableType.JSON_ARR);
    }

    @Test
    void shouldConvertJavaPojoClassToLHJsonObj() {
        Assertions.assertThat(LHLibUtil.javaClassToLHVarType(Book.class)).isEqualTo(VariableType.JSON_OBJ);
    }

    @Test
    void shouldConvertUUIDToString() {
        UUID uuid = UUID.randomUUID();

        VariableValue varVal = LHLibUtil.objToVarVal(uuid);

        Assertions.assertThat(varVal.getStr()).isEqualTo(uuid.toString());
    }

    @Test
    void validateMapToStringObjectConversion() {
        UUID uuid = UUID.fromString("fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("key1", 1); // Integer
        myMap.put("key2", 2.0); // Float
        myMap.put("key3", "three"); // String
        myMap.put("key4", true); // String
        myMap.put("key5", Map.of("four", 5.0)); // Map
        myMap.put("key6", List.of(6, uuid)); // List with UUID

        VariableValue varVal = LHLibUtil.objToVarVal(myMap);

        Assertions.assertThat(varVal.getJsonObj())
                .isEqualTo(
                        "{\"key1\":1,\"key2\":2.0,\"key5\":{\"four\":5.0},\"key6\":[6,\"0fc03087-d265-11e7-b8c6-83e29cd24f4c\"],\"key3\":\"three\",\"key4\":true}");
    }

    @Test
    void validateListToStringArrayConversion() {
        List<Object> list = new ArrayList<Object>();
        list.add("item1"); // String
        list.add(2); // Integer
        list.add(3.0); // Float
        list.add(Map.of("4", 5)); // Map
        list.add(List.of(6, "7")); // List

        VariableValue varVal = LHLibUtil.objToVarVal(list);

        Assertions.assertThat(varVal.getJsonArr()).isEqualTo("[\"item1\",2,3.0,{\"4\":5},[6,\"7\"]]");
    }

    @Test
    void validatePojoToStringObjectConversion() {
        VariableValue varVal = LHLibUtil.objToVarVal(getTestBook());

        Assertions.assertThat(varVal.getJsonObj())
                .isEqualTo(
                        "{\"title\":\"Frankenstein\",\"soldUnits\":3000000,\"cost\":9.99,\"genres\":[\"Classic\",\"Horror\",\"Science Fiction\"],\"isBestseller\":true,\"additionalAttributes\":{\"Published Year\":\"1818\",\"ISBN\":\"9780141439471\",\"Author\":\"Mary Shelley\"}}");
    }

    @Test
    void objToVarValFromInstantSetsUtcTimestamp() {
        Instant now = Instant.now();
        VariableValue val = LHLibUtil.objToVarVal(now);
        Assertions.assertThat(val.getValueCase()).isEqualTo(VariableValue.ValueCase.UTC_TIMESTAMP);
        Assertions.assertThat(val.getUtcTimestamp().getSeconds()).isEqualTo(now.getEpochSecond());
    }

    @Test
    void objToVarValFromDateSetsUtcTimestamp() {
        Date currentDate = new Date();
        VariableValue val = LHLibUtil.objToVarVal(currentDate);
        Assertions.assertThat(val.getValueCase()).isEqualTo(VariableValue.ValueCase.UTC_TIMESTAMP);
        Assertions.assertThat(val.getUtcTimestamp().getSeconds()).isEqualTo(currentDate.getTime() / 1000);
    }

    @Test
    void gsonInstantAndDateSerializeDeserialize() {
        Instant now = Instant.parse("2020-01-02T03:04:05.123Z");
        String instantJson = LHLibUtil.LH_GSON.toJson(now);
        Instant parsed = LHLibUtil.LH_GSON.fromJson(instantJson, Instant.class);
        Assertions.assertThat(parsed).isEqualTo(now);

        Date currentDate = Date.from(now);
        String dateJson = LHLibUtil.LH_GSON.toJson(currentDate);
        Date parsedDate = LHLibUtil.LH_GSON.fromJson(dateJson, Date.class);
        Assertions.assertThat(parsedDate.getTime()).isEqualTo(currentDate.getTime());
    }

    private Book getTestBook() {
        String title = "Frankenstein";
        int soldUnits = 3000000;
        double cost = 9.99;

        List<String> genres = List.of("Classic", "Horror", "Science Fiction");

        Boolean isBestseller = true;

        Map<String, String> additionalAttributes = new HashMap<>();
        additionalAttributes.put("Author", "Mary Shelley");
        additionalAttributes.put("Published Year", "1818");
        additionalAttributes.put("ISBN", "9780141439471");

        return new Book(title, soldUnits, cost, genres, isBestseller, additionalAttributes);
    }

    private class Book {
        private String title;
        private int soldUnits;
        private double cost;
        private List<String> genres;
        private Boolean isBestseller;
        private Map<String, String> additionalAttributes;

        public Book(
                String title,
                int soldUnits,
                double cost,
                List<String> genres,
                Boolean isBestseller,
                Map<String, String> additionalAttributes) {
            this.title = title;
            this.soldUnits = soldUnits;
            this.cost = cost;
            this.genres = genres;
            this.isBestseller = isBestseller;
            this.additionalAttributes = additionalAttributes;
        }
    }
}
