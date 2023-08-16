package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.VariableType;
import org.junit.jupiter.api.Test;

public class VariableValueModelTest {

    @Test
    void castDoubleToInt() throws LHVarSubError {
        VariableValueModel doubleVarval = new VariableValueModel(22.1);

        assertThat(doubleVarval.type).isEqualTo(VariableType.DOUBLE);

        assertDoesNotThrow(() -> {
            doubleVarval.asInt();
        });
        VariableValueModel intVarVal = doubleVarval.asInt();

        assertThat(intVarVal.type).isEqualTo(VariableType.INT);
        assertThat(intVarVal.intVal).isEqualTo(22);
    }

    @Test
    void castDoubleToString() throws LHVarSubError {
        VariableValueModel doubleVarval = new VariableValueModel(22.1);

        VariableValueModel strVarVal = doubleVarval.asStr();
        assertThat(strVarVal.type).isEqualTo(VariableType.STR);
        assertThat(strVarVal.strVal).isEqualTo("22.1");
    }

    class ObjVarThing {

        public String foo;

        public ObjVarThing(String foo) {
            this.foo = foo;
        }
    }

    @Test
    void asObjShouldReturnCopy() throws LHVarSubError, LHSerdeError {
        ObjVarThing thing = new ObjVarThing("Hi There");
        VariableValueModel first = VariableValueModel.fromProto(
            LHLibUtil.objToVarVal(thing)
        );

        assertThat(first.type).isEqualTo(VariableType.JSON_OBJ);
        assertThat(first.jsonObjVal.get("foo")).isEqualTo("Hi There");

        // copy
        VariableValueModel second = first.asObj();
        assertThat(second.type).isEqualTo(VariableType.JSON_OBJ);
        assertThat(second.jsonObjVal.get("foo")).isEqualTo("Hi There");

        second.jsonObjVal.put("foo", "bar");
        assertThat(second.jsonObjVal.get("foo")).isEqualTo("bar");
        assertThat(first.jsonObjVal.get("foo")).isEqualTo("Hi There");
    }
}
