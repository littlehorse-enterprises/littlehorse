package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import e2e.Struct.Person;
import e2e.Struct.PhoneNumbers;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.VariableValue.ValueCase;
import io.littlehorse.sdk.common.util.Arg;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.LHWorkflow;
import io.littlehorse.test.WithStructDefs;
import io.littlehorse.test.WorkflowVerifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@LHTest
@WithStructDefs({Person.class})
public class MapsTest {

    @LHWorkflow("empty-map-assign-wf")
    private Workflow emptyMapWf;

    @LHWorkflow("filled-map-assign-wf")
    private Workflow filledMapWf;

    @LHWorkflow("map-get-wf")
    private Workflow mapGetWf;

    @LHWorkflow("map-contains-wf")
    private Workflow mapContainsWf;

    @LHWorkflow("map-extend-wf")
    private Workflow mapExtendWf;

    @LHWorkflow("map-remove-key-wf")
    private Workflow mapRemoveKeyWf;

    @LHWorkflow("map-size-wf")
    private Workflow mapSizeWf;

    @LHWorkflow("map-int-key-wf")
    private Workflow mapIntKeyWf;

    @LHWorkflow("map-task-input-wf")
    private Workflow mapTaskInputWf;

    @LHWorkflow("map-struct-value-wf")
    private Workflow mapStructValueWf;

    @LHWorkflow("map-array-value-wf")
    private Workflow mapArrayValueWf;

    private LittleHorseBlockingStub client;
    private WorkflowVerifier workflowVerifier;

    @Test
    public void shouldAllowAssigningEmptyNativeMapToTypedMapVariable() {
        workflowVerifier
                .prepareRun(emptyMapWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(0);
                })
                .start();
    }

    @Test
    public void shouldRejectMismatchedValueTypeOnRunWf() {
        workflowVerifier
                .prepareRun(emptyMapWf)
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        String wfRunId = UUID.randomUUID().toString();

        VariableValue badMap = VariableValue.newBuilder()
                .setMap(io.littlehorse.sdk.common.proto.Map.newBuilder()
                        .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                                .setKey(VariableValue.newBuilder().setStr("hello"))
                                .setValue(VariableValue.newBuilder().setStr("world")))
                        .build())
                .build();

        assertThatThrownBy(() -> client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName("empty-map-assign-wf")
                        .setId(wfRunId)
                        .putVariables("my-map", badMap)
                        .build()))
                .matches(exn -> {
                    if (!(exn instanceof StatusRuntimeException)) return false;
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Status.Code.INVALID_ARGUMENT;
                });
    }

    @Test
    public void shouldAssignNativeMapWithContents() {
        workflowVerifier
                .prepareRun(filledMapWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(2);
                })
                .start();
    }

    @Test
    public void shouldAllowGettingMapValueByKey() {
        workflowVerifier
                .prepareRun(mapGetWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "picked", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.INT);
                    assertThat(variableValue.getInt()).isEqualTo(42L);
                })
                .start();
    }

    @Test
    public void shouldDetectMapContainsKey() {
        workflowVerifier
                .prepareRun(mapContainsWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "found", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.BOOL);
                    assertThat(variableValue.getBool()).isTrue();
                })
                .start();
    }

    @Test
    public void shouldPutEntryOnExtend() {
        workflowVerifier
                .prepareRun(mapExtendWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(3);
                })
                .start();
    }

    @Test
    public void shouldRemoveEntryOnRemoveKey() {
        workflowVerifier
                .prepareRun(mapRemoveKeyWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(1);
                    // Only "world" entry should remain
                    assertThat(variableValue.getMap().getEntries(0).getKey().getStr())
                            .isEqualTo("world");
                })
                .start();
    }

    @Test
    public void shouldComputeMapSize() {
        workflowVerifier
                .prepareRun(mapSizeWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "map-size", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.INT);
                    assertThat(variableValue.getInt()).isEqualTo(2L);
                })
                .start();
    }

    @Test
    public void shouldSupportIntegerKeys() {
        workflowVerifier
                .prepareRun(mapIntKeyWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(2);
                    // Verify the entries have INT keys
                    assertThat(variableValue.getMap().getEntries(0).getKey().getValueCase())
                            .isEqualTo(ValueCase.INT);
                })
                .start();
    }

    @Test
    public void shouldPassMapAsTaskInput() {
        workflowVerifier
                .prepareRun(mapTaskInputWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "result", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.INT);
                    // produceMapKeyCount returns the number of keys in the map (2)
                    assertThat(variableValue.getInt()).isEqualTo(2L);
                })
                .start();
    }

    @Test
    public void shouldRejectNonMapValueForMapVariable() {
        // Ensure WfSpec is registered first
        workflowVerifier
                .prepareRun(emptyMapWf)
                .waitForStatus(LHStatus.COMPLETED)
                .start();

        String wfRunId = UUID.randomUUID().toString();

        // Send a plain STR value to a Map variable
        VariableValue strVal = VariableValue.newBuilder().setStr("not-a-map").build();

        assertThatThrownBy(() -> client.runWf(RunWfRequest.newBuilder()
                        .setWfSpecName("empty-map-assign-wf")
                        .setId(wfRunId)
                        .putVariables("my-map", strVal)
                        .build()))
                .matches(exn -> {
                    if (!(exn instanceof StatusRuntimeException)) return false;
                    StatusRuntimeException sre = (StatusRuntimeException) exn;
                    return sre.getStatus().getCode() == Status.Code.INVALID_ARGUMENT;
                });
    }

    @Test
    public void shouldSupportStructValuesInMap() {
        workflowVerifier
                .prepareRun(mapStructValueWf)
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(2);
                    // Each map value should be a native STRUCT referencing the "person" StructDef
                    var entry = variableValue.getMap().getEntries(0);
                    assertThat(entry.getValue().getValueCase()).isEqualTo(ValueCase.STRUCT);
                    assertThat(entry.getValue().getStruct().getStructDefId().getName())
                            .isEqualTo("person");
                })
                .start();
    }

    @Test
    public void shouldSupportArrayValuesInMap() {
        // Build a native Map<STR, ARRAY<INT>> value and pass it as workflow input.
        // A pre-built VariableValue is passed through verbatim by LHLibUtil.objToVarVal.
        VariableValue arrayMap = VariableValue.newBuilder()
                .setMap(io.littlehorse.sdk.common.proto.Map.newBuilder()
                        .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                                .setKey(VariableValue.newBuilder().setStr("evens"))
                                .setValue(VariableValue.newBuilder()
                                        .setArray(io.littlehorse.sdk.common.proto.Array.newBuilder()
                                                .addItems(VariableValue.newBuilder()
                                                        .setInt(2))
                                                .addItems(VariableValue.newBuilder()
                                                        .setInt(4)))))
                        .addEntries(io.littlehorse.sdk.common.proto.Map.Entry.newBuilder()
                                .setKey(VariableValue.newBuilder().setStr("odds"))
                                .setValue(VariableValue.newBuilder()
                                        .setArray(io.littlehorse.sdk.common.proto.Array.newBuilder()
                                                .addItems(VariableValue.newBuilder()
                                                        .setInt(1))
                                                .addItems(VariableValue.newBuilder()
                                                        .setInt(3))
                                                .addItems(VariableValue.newBuilder()
                                                        .setInt(5)))))
                        .build())
                .build();

        workflowVerifier
                .prepareRun(mapArrayValueWf, Arg.of("my-map", arrayMap))
                .waitForStatus(LHStatus.COMPLETED)
                .thenVerifyVariable(0, "my-map", variableValue -> {
                    assertThat(variableValue.getValueCase()).isEqualTo(ValueCase.MAP);
                    assertThat(variableValue.getMap().getEntriesList()).hasSize(2);
                    // Each map value should be a native ARRAY of INT
                    var evens = variableValue.getMap().getEntriesList().stream()
                            .filter(e -> e.getKey().getStr().equals("evens"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(evens.getValue().getValueCase()).isEqualTo(ValueCase.ARRAY);
                    assertThat(evens.getValue().getArray().getItemsList()).hasSize(2);
                    assertThat(evens.getValue().getArray().getItems(0).getValueCase())
                            .isEqualTo(ValueCase.INT);
                    assertThat(evens.getValue().getArray().getItems(0).getInt()).isEqualTo(2L);
                })
                .start();
    }

    // ---- Workflow Definitions ----

    @LHWorkflow("empty-map-assign-wf")
    public Workflow buildEmptyMapAssignWf() {
        return new WorkflowImpl("empty-map-assign-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            TaskNodeOutput produced = thread.execute("produce-empty-map");
            mapVar.assign(produced);
        });
    }

    @LHWorkflow("filled-map-assign-wf")
    public Workflow buildFilledMapAssignWf() {
        return new WorkflowImpl("filled-map-assign-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
        });
    }

    @LHWorkflow("map-get-wf")
    public Workflow buildMapGetWf() {
        return new WorkflowImpl("map-get-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            WfRunVariable picked = thread.declareInt("picked");
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            picked.assign(mapVar.get("hello"));
        });
    }

    @LHWorkflow("map-contains-wf")
    public Workflow buildMapContainsWf() {
        return new WorkflowImpl("map-contains-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            WfRunVariable found = thread.declareBool("found");
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            // TODO: unnecessary task call because of mutation bug #2181
            thread.execute("produce-map");
            found.assign(mapVar.doesContain("hello"));
        });
    }

    @LHWorkflow("map-extend-wf")
    public Workflow buildMapExtendWf() {
        return new WorkflowImpl("map-extend-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            // TODO: unnecessary task call because of mutation bug #2181
            thread.execute("produce-map");
            // Put a new entry: "new-key" -> 99
            mapVar.assign(mapVar.extend(thread.execute("produce-single-entry-map")));
        });
    }

    @LHWorkflow("map-remove-key-wf")
    public Workflow buildMapRemoveKeyWf() {
        return new WorkflowImpl("map-remove-key-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            // TODO: unnecessary task call because of mutation bug #2181
            thread.execute("produce-map");
            mapVar.assign(mapVar.removeKey("hello"));
        });
    }

    @LHWorkflow("map-size-wf")
    public Workflow buildMapSizeWf() {
        return new WorkflowImpl("map-size-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            WfRunVariable mapSize = thread.declareInt("map-size");
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            mapSize.assign(mapVar.size());
        });
    }

    @LHTaskMethod("produce-empty-map")
    @LHType(isLHMap = true)
    public Map<String, Long> produceEmptyMap() {
        return new HashMap<>();
    }

    @LHTaskMethod("produce-map")
    @LHType(isLHMap = true)
    public Map<String, Long> produceMap() {
        Map<String, Long> result = new HashMap<>();
        result.put("hello", 42L);
        result.put("world", 99L);
        return result;
    }

    @LHTaskMethod("produce-single-entry-map")
    @LHType(isLHMap = true)
    public Map<String, Long> produceSingleEntryMap() {
        Map<String, Long> result = new HashMap<>();
        result.put("new-key", 99L);
        return result;
    }

    @LHWorkflow("map-int-key-wf")
    public Workflow buildMapIntKeyWf() {
        return new WorkflowImpl("map-int-key-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", Long.class, String.class);
            TaskNodeOutput produced = thread.execute("produce-int-key-map");
            mapVar.assign(produced);
        });
    }

    @LHTaskMethod("produce-int-key-map")
    @LHType(isLHMap = true)
    public Map<Long, String> produceIntKeyMap() {
        Map<Long, String> result = new HashMap<>();
        result.put(1L, "one");
        result.put(2L, "two");
        return result;
    }

    @LHWorkflow("map-task-input-wf")
    public Workflow buildMapTaskInputWf() {
        return new WorkflowImpl("map-task-input-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Long.class);
            WfRunVariable result = thread.declareInt("result");
            TaskNodeOutput produced = thread.execute("produce-map");
            mapVar.assign(produced);
            TaskNodeOutput count = thread.execute("count-map-keys", mapVar);
            result.assign(count);
        });
    }

    @LHTaskMethod("count-map-keys")
    public long countMapKeys(@LHType(isLHMap = true) Map<String, Long> input) {
        return input.size();
    }

    @LHWorkflow("map-struct-value-wf")
    public Workflow buildMapStructValueWf() {
        return new WorkflowImpl("map-struct-value-wf", thread -> {
            WfRunVariable mapVar = thread.declareMap("my-map", String.class, Person.class);
            TaskNodeOutput produced = thread.execute("produce-person-map");
            mapVar.assign(produced);
        });
    }

    @LHTaskMethod("produce-person-map")
    @LHType(isLHMap = true)
    public Map<String, Person> producePersonMap() {
        Map<String, Person> result = new HashMap<>();
        result.put(
                "jedi",
                new Person("Obi-Wan Kenobi", new String[] {"Yoda"}, new PhoneNumbers("111-222-3344", "555-667-7788")));
        result.put(
                "sith",
                new Person(
                        "Darth Vader", new String[] {"Palpatine"}, new PhoneNumbers("999-888-7766", "000-111-2233")));
        return result;
    }

    @LHWorkflow("map-array-value-wf")
    public Workflow buildMapArrayValueWf() {
        return new WorkflowImpl("map-array-value-wf", thread -> {
            // Map<STR, ARRAY<INT>> as a required input variable
            thread.declareMap("my-map", String.class, Long[].class).required();
        });
    }
}
