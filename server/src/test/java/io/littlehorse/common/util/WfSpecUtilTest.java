package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class WfSpecUtilTest {

    @Nested
    class Equals {
        @Test
        void shouldBeTrueWhenTwoWfSpecModelsAreEqual() {
            String name = "test";
            String entrypointThreadName = "thread";
            ThreadVarDef.Builder variable = ThreadVarDef.newBuilder()
                    .setVarDef(VariableDef.newBuilder()
                            .setName("variable")
                            .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR)));
            WfSpec originalSpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(1)
                            .setRevision(3))
                    .addFrozenVariables(variable)
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();
            WfSpec copySpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(0)
                            .setRevision(1))
                    .addFrozenVariables(variable)
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();

            WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
            WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

            assertThat(WfSpecUtil.equals(original, copy)).isTrue();
        }

        @Test
        void shouldBeFalseWhenSpecNameIsDifferent() {
            String name = "test";
            String copyName = "test1";
            String entrypointThreadName = "thread";
            WfSpec originalSpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(1)
                            .setRevision(3))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();
            WfSpec copySpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(copyName)
                            .setMajorVersion(0)
                            .setRevision(1))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();

            WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
            WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

            assertThat(WfSpecUtil.equals(original, copy)).isFalse();
        }

        @Test
        void shouldBeFalseWhenEntrypointThreadNameIsDifferent() {
            String name = "test";
            String entrypointThreadName = "thread";
            String entrypointThreadNameCopy = "thread-1";
            WfSpec originalSpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(1)
                            .setRevision(3))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();
            WfSpec copySpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(0)
                            .setRevision(1))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadNameCopy)
                    .build();

            WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
            WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

            assertThat(WfSpecUtil.equals(original, copy)).isFalse();
        }

        @Test
        void shouldBeFalseWhenVariablesAreDifferent() {
            String name = "test";
            String entrypointThreadName = "thread";
            String entrypointThreadNameCopy = "thread-1";
            WfSpec originalSpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(1)
                            .setRevision(3))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .addFrozenVariables(ThreadVarDef.newBuilder()
                            .setVarDef(VariableDef.newBuilder()
                                    .setName("variable")
                                    .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();
            WfSpec copySpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(0)
                            .setRevision(1))
                    .addFrozenVariables(ThreadVarDef.newBuilder()
                            .setVarDef(VariableDef.newBuilder()
                                    .setName("variable-2")
                                    .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))))
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadNameCopy)
                    .build();

            WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
            WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

            assertThat(WfSpecUtil.equals(original, copy)).isFalse();
        }
    }

    @Nested
    class BreakingChange {
        @Test
        void shouldBeFalseWhenVariablesAreEqual() {
            String name = "test";
            String entrypointThreadName = "thread";
            ThreadVarDef.Builder variable = ThreadVarDef.newBuilder()
                    .setVarDef(VariableDef.newBuilder()
                            .setName("variable")
                            .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR)))
                    .setRequired(true);
            ThreadSpec threadSpec =
                    ThreadSpec.newBuilder().addVariableDefs(variable).build();
            WfSpec originalSpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(1)
                            .setRevision(3))
                    .putThreadSpecs(entrypointThreadName, threadSpec)
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();
            WfSpec copySpec = WfSpec.newBuilder()
                    .setId(WfSpecId.newBuilder()
                            .setName(name)
                            .setMajorVersion(0)
                            .setRevision(1))
                    .putThreadSpecs(entrypointThreadName, threadSpec)
                    .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                    .setEntrypointThreadName(entrypointThreadName)
                    .build();

            WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
            WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

            assertThat(WfSpecUtil.hasBreakingChanges(original, copy, null)).isFalse();
        }
    }

    @Test
    void shouldBeTrueWhenChangingRequiredVariable() {
        String name = "test";
        String entrypointThreadName = "thread";
        ThreadVarDef.Builder variable = ThreadVarDef.newBuilder()
                .setVarDef(VariableDef.newBuilder()
                        .setName("variable")
                        .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR)));
        ThreadSpec threadSpec = ThreadSpec.newBuilder()
                .addVariableDefs(variable.setRequired(true))
                .build();
        ThreadSpec newThreadSpec = ThreadSpec.newBuilder()
                .addVariableDefs(variable.setRequired(false))
                .build();
        WfSpec originalSpec = WfSpec.newBuilder()
                .setId(WfSpecId.newBuilder().setName(name).setMajorVersion(1).setRevision(3))
                .putThreadSpecs(entrypointThreadName, threadSpec)
                .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                .setEntrypointThreadName(entrypointThreadName)
                .build();
        WfSpec copySpec = WfSpec.newBuilder()
                .setId(WfSpecId.newBuilder().setName(name).setMajorVersion(0).setRevision(1))
                .putThreadSpecs(entrypointThreadName, newThreadSpec)
                .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                .setEntrypointThreadName(entrypointThreadName)
                .build();

        WfSpecModel original = WfSpecModel.fromProto(originalSpec, null);
        WfSpecModel copy = WfSpecModel.fromProto(copySpec, null);

        assertThat(WfSpecUtil.hasBreakingChanges(original, copy, null)).isTrue();
    }
}
