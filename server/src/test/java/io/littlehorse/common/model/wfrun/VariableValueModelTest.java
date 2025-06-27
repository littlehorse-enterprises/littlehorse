package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class VariableValueModelTest {

    @Test
    void castDoubleToInt() throws LHVarSubError {
        VariableValueModel doubleVarval = new VariableValueModel(22.1);

        assertThat(doubleVarval.getType()).isEqualTo(VariableType.DOUBLE);

        assertDoesNotThrow(() -> {
            doubleVarval.asInt();
        });
        VariableValueModel intVarVal = doubleVarval.asInt();

        assertThat(intVarVal.getType()).isEqualTo(VariableType.INT);
        assertThat(intVarVal.getIntVal()).isEqualTo(22);
    }

    @Test
    void castDoubleToString() throws LHVarSubError {
        VariableValueModel doubleVarval = new VariableValueModel(22.1);

        VariableValueModel strVarVal = doubleVarval.asStr();
        assertThat(strVarVal.getType()).isEqualTo(VariableType.STR);
        assertThat(strVarVal.getStrVal()).isEqualTo("22.1");
    }

    class ObjVarThing {

        public String foo;

        public ObjVarThing(String foo) {
            this.foo = foo;
        }
    }

    @Test
    void asObjShouldReturnCopy() throws LHVarSubError, LHSerdeException {
        ObjVarThing thing = new ObjVarThing("Hi There");
        VariableValueModel first = VariableValueModel.fromProto(LHLibUtil.objToVarVal(thing), mock());

        assertThat(first.getType()).isEqualTo(VariableType.JSON_OBJ);
        assertThat(first.getJsonObjVal().get("foo")).isEqualTo("Hi There");

        // copy
        VariableValueModel second = first.asObj();
        assertThat(second.getType()).isEqualTo(VariableType.JSON_OBJ);
        assertThat(second.getJsonObjVal().get("foo")).isEqualTo("Hi There");

        second.getJsonObjVal().put("foo", "bar");
        assertThat(second.getJsonObjVal().get("foo")).isEqualTo("bar");
        assertThat(first.getJsonObjVal().get("foo")).isEqualTo("Hi There");
    }

    @Test
    void shouldThrowVarSubError() {
        VariableValueModel doubleVarval = new VariableValueModel("not a double");
        LHVarSubError varSubError = (LHVarSubError) Assertions.catchThrowable(doubleVarval::asDouble);
        Assertions.assertThat(varSubError).isNotNull();
        Assertions.assertThat(varSubError.getMessage()).isEqualTo("Couldn't convert STR to DOUBLE");
    }

    @Test
    void shouldCreateWfRunIdVariable() {
        WfRunId wfrunId = WfRunId.newBuilder().setId("test").build();
        VariableValue valueWfRunId =
                VariableValue.newBuilder().setWfRunId(wfrunId).build();
        VariableValueModel variableValueModel = VariableValueModel.fromProto(valueWfRunId, mock());
        assertThat(variableValueModel.getType()).isEqualTo(VariableType.WF_RUN_ID);
        assertThat(variableValueModel.getWfRunId().toProto().build()).isEqualTo(wfrunId);
        assertThat(variableValueModel.toProto().build()).isEqualTo(valueWfRunId);
    }

    @Test
    void shouldConvertStrToWfRunId() throws LHVarSubError {
        WfRunId wfrunId = WfRunId.newBuilder().setId("test").build();
        VariableValueModel variableValueModel = new VariableValueModel("test");
        VariableValueModel valueWfRunId = variableValueModel.asWfRunId();
        assertThat(valueWfRunId.getWfRunId().toProto().build()).isEqualTo(wfrunId);
    }

    @Test
    void shouldConvertStrToWfRunIdWithParentId() throws LHVarSubError {
        WfRunId wfrunId = WfRunId.newBuilder()
                .setId("child")
                .setParentWfRunId(WfRunId.newBuilder().setId("parent").build())
                .build();
        VariableValueModel variableValueModel = new VariableValueModel("parent_child");
        VariableValueModel valueWfRunId = variableValueModel.asWfRunId();
        assertThat(valueWfRunId.getWfRunId().toProto().build()).isEqualTo(wfrunId);
    }

    @Test
    void shouldConvertDoubleToWfRunId() throws LHVarSubError {
        WfRunId wfrunId = WfRunId.newBuilder().setId("22.1").build();
        VariableValueModel variableValueModel = new VariableValueModel(22.1);
        VariableValueModel valueWfRunId = variableValueModel.asWfRunId();
        assertThat(valueWfRunId.getWfRunId().toProto().build()).isEqualTo(wfrunId);
    }

    @Test
    void shouldConvertIntToWfRunId() throws LHVarSubError {
        WfRunId wfrunId = WfRunId.newBuilder().setId("22").build();
        VariableValueModel variableValueModel = new VariableValueModel(22);
        VariableValueModel valueWfRunId = variableValueModel.asWfRunId();
        assertThat(valueWfRunId.getWfRunId().toProto().build()).isEqualTo(wfrunId);
    }

    @Test
    void shouldValidateIncompatibleTypesForWfRunId() {
        VariableValueModel booleanValue = new VariableValueModel(true);
        VariableValueModel jsonObjValue = new VariableValueModel(Map.of("hello", "world"));
        VariableValueModel jsonArrValue = new VariableValueModel(List.of(Map.of("hello", "world")));
        VariableValueModel bytesValue = new VariableValueModel(new byte[] {});

        assertThrows(LHVarSubError.class, booleanValue::asWfRunId);
        assertThrows(LHVarSubError.class, jsonObjValue::asWfRunId);
        assertThrows(LHVarSubError.class, jsonArrValue::asWfRunId);
        assertThrows(LHVarSubError.class, bytesValue::asWfRunId);
    }

    @Test
    void shouldConvertWfRunToStr() throws LHVarSubError {
        WfRunId wfrunId = WfRunId.newBuilder()
                .setId("child")
                .setParentWfRunId(WfRunId.newBuilder().setId("parent").build())
                .build();
        VariableValueModel valueWfRunId =
                new VariableValueModel(WfRunIdModel.fromProto(wfrunId, WfRunIdModel.class, mock()));
        VariableValueModel strVarVal = valueWfRunId.asStr();
        assertThat(strVarVal.getType()).isEqualTo(VariableType.STR);
        assertThat(strVarVal.getStrVal()).isEqualTo("parent_child");
    }
}
