package io.littlehorse.sdk.common;

import io.littlehorse.sdk.common.proto.TaskRunId;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
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
    void shouldConvertUUIDToString() {
        UUID uuid = UUID.randomUUID();

        VariableValue varVal = LHLibUtil.objToVarVal(uuid);

        Assertions.assertThat(varVal.getStr()).isEqualTo(uuid.toString());
    }

    @Test
    void validateMapToStringObjectConversion() {
        UUID uuid = UUID.fromString("fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap.put("key1", 5);
        myMap.put("key2", "hello");
        myMap.put("key3", 7.0);
        myMap.put("key4", Map.of("key5", 2000.0));
        myMap.put("key4", Map.of("key5", uuid));
        

        VariableValue varVal = LHLibUtil.objToVarVal(myMap);

        Assertions.assertThat(varVal.getJsonObj()).isEqualTo("{\"key1\":5,\"key2\":\"hello\",\"key3\":7.0,\"key4\":{\"key5\":\"0fc03087-d265-11e7-b8c6-83e29cd24f4c\"}}");
    }

    @Test
    void validateListToStringArrayConversion() {
        List<Object> list = List.of("item1", 2, 3.0,Map.of("key1", "val1"));

        VariableValue varVal = LHLibUtil.objToVarVal(list);

        Assertions.assertThat(varVal.getJsonArr()).isEqualTo("[\"item1\",2,3.0,{\"key1\":\"val1\"}]");
    }

    @Test
    void validatePojoToStringObjectConversion() {
        Book myBook = new Book("Neuromancer", 20, 5);
        
        VariableValue varVal = LHLibUtil.objToVarVal(myBook);

        Assertions.assertThat(varVal.getJsonObj()).isEqualTo("{\"title\":\"Neuromancer\",\"soldUnits\":20,\"cost\":5.0}");
    }

    private class Book {
        private String title;
        private int soldUnits;
        private double cost;
    
        public Book(String title, int soldUnits, double cost) {
            this.title = title;
            this.soldUnits = soldUnits;
            this.cost = cost;
        }
    }
}
