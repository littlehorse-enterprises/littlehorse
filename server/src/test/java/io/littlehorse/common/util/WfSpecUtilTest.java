package io.littlehorse.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class WfSpecUtilTest {

    private static final String SPEC_NAME = "test";
    private static final String ENTRYPOINT_THREAD_NAME = "thread";

    // These two lower-case strings have the same Java String.hashCode().
    // That makes HashMap insertion-order leakage easier to reproduce deterministically.
    private static final String COLLIDING_VAR_A = "fvgki";
    private static final String COLLIDING_VAR_B = "feogjswl";

    private static ThreadVarDef variable(String name, VariableType type) {
        return ThreadVarDef.newBuilder()
                .setVarDef(VariableDef.newBuilder()
                        .setName(name)
                        .setTypeDef(TypeDefinition.newBuilder().setPrimitiveType(type)))
                .build();
    }

    private static WfSpec.Builder baseSpec() {
        return WfSpec.newBuilder()
                .setId(WfSpecId.newBuilder()
                        .setName(SPEC_NAME)
                        .setMajorVersion(0)
                        .setRevision(0))
                .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                .setEntrypointThreadName(ENTRYPOINT_THREAD_NAME);
    }

    private static WfSpec specWithFrozenVariables(List<ThreadVarDef> variables) {
        WfSpec.Builder spec = baseSpec();
        variables.forEach(spec::addFrozenVariables);
        return spec.build();
    }

    private static WfSpecModel modelFromBytes(WfSpec spec) throws Exception {
        return WfSpecModel.fromProto(WfSpec.parseFrom(spec.toByteArray()), null);
    }

    private static String frozenVariableOrder(List<ThreadVarDef> variables) {
        return variables.stream()
                .map(variable -> variable.getVarDef().getName())
                .collect(Collectors.joining(","));
    }

    private static WfSpec complexImageProcessingSpec() {
        PutWfSpecRequest request = Workflow.newWorkflow("repro-image-processing", wf -> {
                    WfRunVariable input = wf.declareJsonObj("input").required().asPublic();

                    WfRunVariable tenantId =
                            wf.declareStr("tenant-id").asPublic().searchable();
                    tenantId.assign(input.jsonPath("$.tenant_id"));

                    WfRunVariable creatorId =
                            wf.declareStr("creator-id").asPublic().searchable();
                    creatorId.assign(input.jsonPath("$.creator_id"));

                    WfRunVariable recordId =
                            wf.declareStr("record-id").asPublic().searchable();
                    recordId.assign(input.jsonPath("$.record_id"));

                    WfRunVariable storageUri = wf.declareStr("storage-uri");
                    storageUri.assign(input.jsonPath("$.storage_uri"));

                    input.jsonPath("$.quality_score").assign(null);
                    input.jsonPath("$.quality_metrics").assign(null);

                    wf.execute("repro-update-embeddings", storageUri, "Image", tenantId, creatorId, recordId);

                    input.jsonPath("$.status").assign("embeddings_processed");
                    wf.execute("repro-update-record", tenantId, creatorId, recordId, input);

                    NodeOutput faceResult =
                            wf.execute("repro-detect-faces", storageUri, recordId, "Image", tenantId, creatorId);

                    WfRunVariable faceCount = wf.declareInt("face-count");
                    faceCount.assign(faceResult.jsonPath("$.face_count"));

                    WfRunVariable bestFaceQuality = wf.declareDouble("best-face-quality");
                    bestFaceQuality.assign(faceResult.jsonPath("$.best_face_quality_score"));

                    NodeOutput qualityResult = wf.execute(
                            "repro-grade-image", storageUri, tenantId, creatorId, recordId, faceCount, bestFaceQuality);

                    input.jsonPath("$.quality_score").assign(qualityResult.jsonPath("$.quality_score"));
                    input.jsonPath("$.quality_metrics").assign(qualityResult.jsonPath("$.quality_metrics"));

                    input.jsonPath("$.status").assign("processed");
                    wf.execute("repro-update-record", tenantId, creatorId, recordId, input);
                })
                .compileWorkflow();

        WfSpec.Builder spec = WfSpec.newBuilder()
                .setId(WfSpecId.newBuilder()
                        .setName(request.getName())
                        .setMajorVersion(0)
                        .setRevision(0))
                .setCreatedAt(LHUtil.fromDate(new java.util.Date()))
                .setEntrypointThreadName(request.getEntrypointThreadName())
                .putAllThreadSpecs(request.getThreadSpecsMap());

        request.getThreadSpecsMap().values().stream()
                .flatMap(threadSpec -> threadSpec.getVariableDefsList().stream())
                .filter(variable ->
                        variable.getRequired() || variable.getAccessLevel() == WfRunVariableAccessLevel.PUBLIC_VAR)
                .forEach(spec::addFrozenVariables);

        return spec.build();
    }

    @Nested
    class Equals {
        @Test
        void shouldTreatCompiledComplexWorkflowAsEqualWhenFrozenVariableOrderChanges() throws Exception {
            WfSpec compiled = complexImageProcessingSpec();
            WfSpecModel original = modelFromBytes(compiled);

            List<ThreadVarDef> reversedFrozenVariables = new ArrayList<>(compiled.getFrozenVariablesList());
            Collections.reverse(reversedFrozenVariables);
            WfSpec reordered = compiled.toBuilder()
                    .clearFrozenVariables()
                    .addAllFrozenVariables(reversedFrozenVariables)
                    .build();

            WfSpecModel candidate = modelFromBytes(reordered);

            assertThat(compiled.getThreadSpecsMap()).hasSize(1);
            assertThat(compiled.getFrozenVariablesList()).hasSizeGreaterThan(3);
            assertThat(WfSpecUtil.equals(original, candidate)).isTrue();
        }

        @Test
        void shouldTreatSeededFrozenVariableOrderChangesAsEqualAfterSerde() throws Exception {
            List<ThreadVarDef> variables = List.of(
                    variable(COLLIDING_VAR_A, VariableType.STR),
                    variable(COLLIDING_VAR_B, VariableType.INT),
                    variable("tenantid", VariableType.STR),
                    variable("creatorid", VariableType.STR),
                    variable("recordid", VariableType.STR),
                    variable("facecount", VariableType.INT),
                    variable("bestfacequality", VariableType.DOUBLE));

            WfSpecModel baseline = modelFromBytes(specWithFrozenVariables(variables));

            for (int seed = 0; seed < 100; seed++) {
                List<ThreadVarDef> shuffled = new ArrayList<>(variables);
                Collections.shuffle(shuffled, new Random(seed));

                WfSpecModel candidate = modelFromBytes(specWithFrozenVariables(shuffled));

                assertThat(WfSpecUtil.equals(baseline, candidate))
                        .describedAs("seed=%s order=%s", seed, frozenVariableOrder(shuffled))
                        .isTrue();
            }
        }

        @Test
        void shouldTreatFrozenVariablesAsEqualRegardlessOfOrderAfterSerde() throws Exception {
            ThreadVarDef first = variable(COLLIDING_VAR_A, VariableType.STR);
            ThreadVarDef second = variable(COLLIDING_VAR_B, VariableType.INT);

            WfSpecModel left = modelFromBytes(specWithFrozenVariables(List.of(first, second)));
            WfSpecModel right = modelFromBytes(specWithFrozenVariables(List.of(second, first)));

            assertThat(WfSpecUtil.equals(left, right)).isTrue();
        }

        @Test
        void shouldDetectFrozenVariableTypeChangesAfterCanonicalization() throws Exception {
            WfSpecModel original =
                    modelFromBytes(specWithFrozenVariables(List.of(variable("same-name", VariableType.STR))));
            WfSpecModel changed =
                    modelFromBytes(specWithFrozenVariables(List.of(variable("same-name", VariableType.INT))));

            assertThat(WfSpecUtil.equals(original, changed)).isFalse();
        }

        @Test
        void shouldDetectComplexWorkflowThreadSpecChangesAfterCanonicalization() throws Exception {
            WfSpec compiled = complexImageProcessingSpec();
            WfSpecModel original = modelFromBytes(compiled);

            WfSpec changed = compiled.toBuilder()
                    .putThreadSpecs(
                            compiled.getEntrypointThreadName(),
                            compiled.getThreadSpecsOrThrow(compiled.getEntrypointThreadName()).toBuilder()
                                    .clearNodes()
                                    .build())
                    .build();
            WfSpecModel candidate = modelFromBytes(changed);

            assertThat(WfSpecUtil.equals(original, candidate)).isFalse();
        }

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
