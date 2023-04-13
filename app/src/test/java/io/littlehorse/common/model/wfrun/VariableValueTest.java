package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.jlib.common.LHLibUtil;
import io.littlehorse.jlib.common.exception.LHSerdeError;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import org.junit.jupiter.api.Test;

public class VariableValueTest {

    @Test
    void castDoubleToInt() throws LHVarSubError {
        VariableValue doubleVarval = new VariableValue(22.1);

        assertThat(doubleVarval.type).isEqualTo(VariableTypePb.DOUBLE);

        assertDoesNotThrow(() -> {
            doubleVarval.asInt();
        });
        VariableValue intVarVal = doubleVarval.asInt();

        assertThat(intVarVal.type).isEqualTo(VariableTypePb.INT);
        assertThat(intVarVal.intVal).isEqualTo(22);
    }

    @Test
    void castDoubleToString() throws LHVarSubError {
        VariableValue doubleVarval = new VariableValue(22.1);

        VariableValue strVarVal = doubleVarval.asStr();
        assertThat(strVarVal.type).isEqualTo(VariableTypePb.STR);
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
        VariableValue first = VariableValue.fromProto(LHLibUtil.objToVarVal(thing));

        assertThat(first.type).isEqualTo(VariableTypePb.JSON_OBJ);
        assertThat(first.jsonObjVal.get("foo")).isEqualTo("Hi There");

        // copy
        VariableValue second = first.asObj();
        assertThat(second.type).isEqualTo(VariableTypePb.JSON_OBJ);
        assertThat(second.jsonObjVal.get("foo")).isEqualTo("Hi There");

        second.jsonObjVal.put("foo", "bar");
        assertThat(second.jsonObjVal.get("foo")).isEqualTo("bar");
        assertThat(first.jsonObjVal.get("foo")).isEqualTo("Hi There");
    }
}
