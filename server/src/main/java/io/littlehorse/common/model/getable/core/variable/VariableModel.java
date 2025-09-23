package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.OutputTopicConfigModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.OutputTopicConfig.OutputTopicRecordingLevel;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class VariableModel extends CoreGetable<Variable> implements CoreOutputTopicGetable<Variable> {

    private VariableValueModel value;
    private VariableIdModel id;
    private Date createdAt;
    private WfSpecIdModel wfSpecId;

    private WfSpecModel wfSpec;
    private ExecutionContext executionContext;
    private boolean masked;

    public VariableModel() {}

    public VariableModel(
            String name,
            VariableValueModel value,
            WfRunIdModel wfRunId,
            int threadRunNumber,
            WfSpecModel wfSpec,
            boolean masked) {

        this.id = new VariableIdModel(wfRunId, threadRunNumber, name);
        Objects.requireNonNull(value, "Empty or value expected for variable: " + name);
        this.value = value;
        this.wfSpec = wfSpec;
        this.wfSpecId = wfSpec.getObjectId();
        this.masked = masked;
    }

    public Class<Variable> getProtoBaseClass() {
        return Variable.class;
    }

    public WfSpecModel getWfSpec(ReadOnlyMetadataManager metadataManager) {
        if (wfSpec == null) {
            wfSpec = metadataManager.get(
                    new WfSpecIdModel(wfSpecId.getName(), wfSpecId.getMajorVersion(), wfSpecId.getRevision()));
        }

        return wfSpec;
    }

    @Deprecated
    public WfSpecModel getWfSpec() {
        if (wfSpec == null) {
            wfSpec = getWfSpec(executionContext.metadataManager());
        }
        return wfSpec;
    }

    public void setWfSpec(WfSpecModel spec) {
        this.wfSpec = spec;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Variable p = (Variable) proto;
        masked = p.getMasked();
        if (masked && context.support(RequestExecutionContext.class)) {
            value = new VariableValueModel(LHConstants.STRING_MASK);
        } else {
            value = VariableValueModel.fromProto(p.getValue(), context);
        }
        id = LHSerializable.fromProto(p.getId(), VariableIdModel.class, context);
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.executionContext = context;
    }

    public Variable.Builder toProto() {
        if (value == null) {
            log.info("Variable id got null value: {}", id.toString());
        }
        Variable.Builder out = Variable.newBuilder()
                .setId(id.toProto())
                .setCreatedAt(LHUtil.fromDate(getCreatedAt()))
                .setValue(value.toProto())
                .setWfSpecId(wfSpecId.toProto())
                .setMasked(masked);

        return out;
    }

    @Override
    public VariableIdModel getObjectId() {
        return id;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                // with WfSPecId
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecId", GetableIndex.ValueType.SINGLE),
                                Pair.of("variable", GetableIndex.ValueType.DYNAMIC)),
                        Optional.empty(),
                        variable -> ((VariableModel) variable).isIndexable()),
                new GetableIndex<>(
                        List.of(
                                Pair.of("majorVersion", GetableIndex.ValueType.SINGLE),
                                Pair.of("variable", GetableIndex.ValueType.DYNAMIC)),
                        Optional.empty(),
                        variable -> ((VariableModel) variable).isIndexable()),

                // with workflow name only
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("variable", GetableIndex.ValueType.DYNAMIC)),
                        Optional.empty(),
                        variable -> ((VariableModel) variable).isIndexable()));
    }

    public String getName() {
        return id.getName();
    }

    private boolean isIndexable() {
        return !getName().equals(LHConstants.EXT_EVT_HANDLER_VAR) && !value.isNull();
    }

    @Override
    public boolean shouldProduceToOutputTopic(
            Variable previousValue,
            ReadOnlyMetadataManager metadataManager,
            ReadOnlyGetableManager getableManager,
            OutputTopicConfigModel config) {
        if (config.getDefaultRecordingLevel() == OutputTopicRecordingLevel.NO_ENTITY_EVENTS) {
            return false;
        }

        // Only PUBLIC_VAR variables should be pushed out.
        WfRunModel wfRun = getableManager.get(id.getWfRunId());
        String threadSpecName = wfRun.getThreadRun(id.getThreadRunNumber()).getThreadSpecName();
        ThreadSpecModel threadSpec =
                metadataManager.get(wfRun.getWfSpecId()).getThreadSpecs().get(threadSpecName);
        ThreadVarDefModel variableDef = threadSpec.getVarDef(id.getName());

        WfRunVariableAccessLevel accessLevel = variableDef.getAccessLevel();
        return accessLevel == WfRunVariableAccessLevel.PUBLIC_VAR
                || accessLevel == WfRunVariableAccessLevel.INHERITED_VAR;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(new IndexedField(key, this.getWfSpec().getName(), TagStorageType.LOCAL));
            }
            case "wfSpecId" -> {
                return List.of(new IndexedField(key, wfSpecId.toString(), TagStorageType.LOCAL));
            }
            case "majorVersion" -> {
                return List.of(new IndexedField(
                        key,
                        wfSpecId.getName() + "/" + LHUtil.toLHDbVersionFormat(wfSpecId.getMajorVersion()),
                        TagStorageType.LOCAL));
            }
            case "variable" -> {
                return getDynamicFields();
            }
        }
        return null;
    }

    private List<IndexedField> getDynamicFields() {
        VariableValueModel value = getValue();
        WfSpecModel wfSpec = getWfSpec();
        ThreadVarDefModel threadVarDef = wfSpec.getAllVariables().get(this.getName());

        TagStorageType indexType = TagStorageType.LOCAL;

        if (!threadVarDef.isSearchable()) {
            return List.of();
        }

        // Current behavior is that null variables are NOT indexed. This may change in future
        // releases, but it will be a backwards-compatible change.
        if (value.getType() == null) {
            return List.of();
        }

        switch (value.getType()) {
            case STR -> {
                return List.of(new IndexedField(this.getName(), value.getStrVal(), indexType));
            }
            case BOOL -> {
                return List.of(new IndexedField(this.getName(), value.getBoolVal(), indexType));
            }
            case INT -> {
                return List.of(new IndexedField(this.getName(), value.getIntVal(), indexType));
            }
            case DOUBLE -> {
                return List.of(new IndexedField(this.getName(), value.getDoubleVal(), indexType));
            }
            case JSON_OBJ -> {
                // Needs work
                Set<Pair<String, Object>> flattenedMap = flattenJsonObj(value.getJsonObjVal());

                return flattenedMap.stream()
                        .filter(flatKeyValue -> threadVarDef.isSearchableOn(flatKeyValue.getKey()))
                        .map(flatKeyValue -> {
                            return new IndexedField(
                                    this.getName() + "_" + flatKeyValue.getKey(), flatKeyValue.getValue(), indexType);
                        })
                        .toList();
            }
            case JSON_ARR -> {
                return jsonArrTagValues(threadVarDef);
            }
            default -> {
                log.warn("Tags unimplemented for variable type: {}", value.getType());
                return List.of();
            }
        }
    }

    private List<IndexedField> jsonArrTagValues(ThreadVarDefModel threadVarDef) {
        Set<Pair<String, Object>> flattenedPairs = new HashSet<>();
        for (Object listItem : this.value.getJsonArrVal()) {
            if (listItem instanceof Map) {
                flattenedPairs.addAll(flattenValue("$", listItem));
            } else if (listItem instanceof List) {
                log.debug("Unimplemented: indexes nested arrays inside JSON_ARR variables.");
            } else {
                flattenedPairs.add(Pair.of("", listItem));
            }
        }

        return flattenedPairs.stream()
                .map(flatKeyValue -> {
                    if (!flatKeyValue.getKey().isEmpty()) {
                        return new IndexedField(
                                this.getName() + "_" + flatKeyValue.getKey(),
                                flatKeyValue.getValue(),
                                TagStorageType.LOCAL);
                    } else {
                        return new IndexedField(this.getName(), flatKeyValue.getValue(), TagStorageType.LOCAL);
                    }
                })
                .toList();
    }

    private static Set<Pair<String, Object>> flattenJsonObj(Map<String, Object> map) {
        Set<Pair<String, Object>> flattenedMap = new HashSet<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            flattenedMap.addAll(flattenValue("$." + key, value));
        }
        return flattenedMap;
    }

    private static Set<Pair<String, Object>> flattenValue(String flatKey, Object value) {
        Set<Pair<String, Object>> out = new HashSet<>();

        if (value instanceof Map valueMap) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) valueMap).entrySet()) {
                out.addAll(flattenValue(flatKey + "." + entry.getKey(), entry.getValue()));
            }
        } else if (value instanceof List) {
            for (Object subValue : (List<?>) value) {
                out.addAll(flattenValue(flatKey, subValue));
            }
        } else {
            out.add(Pair.of(flatKey, value));
        }
        return out;
    }
}
