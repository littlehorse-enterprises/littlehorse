package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class VariableModel extends CoreGetable<Variable> {

    private VariableValueModel value;
    private String wfRunId;
    private int threadRunNumber;
    private String name;
    private Date date;
    private WfSpecIdModel wfSpecId;

    private WfSpecModel wfSpec;
    private ExecutionContext executionContext;

    public VariableModel() {}

    public VariableModel(
            String name, VariableValueModel value, String wfRunId, int threadRunNumber, WfSpecModel wfSpec) {
        this.name = name;
        this.value = value;
        this.wfRunId = wfRunId;
        this.wfSpec = wfSpec;
        this.threadRunNumber = threadRunNumber;
        this.wfSpecId = wfSpec.getObjectId();
    }

    public Class<Variable> getProtoBaseClass() {
        return Variable.class;
    }

    public WfSpecModel getWfSpec() {
        if (wfSpec == null) {
            wfSpec = executionContext.service().getWfSpec(wfSpecId.getName(), wfSpecId.getVersion());
        }
        return wfSpec;
    }

    public void setWfSpec(WfSpecModel spec) {
        this.wfSpec = spec;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        Variable p = (Variable) proto;
        value = VariableValueModel.fromProto(p.getValue(), context);
        wfRunId = p.getWfRunId();
        name = p.getName();
        threadRunNumber = p.getThreadRunNumber();
        date = LHUtil.fromProtoTs(p.getDate());
        wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
    }

    public Variable.Builder toProto() {
        Variable.Builder out = Variable.newBuilder()
                .setName(name)
                .setThreadRunNumber(threadRunNumber)
                .setWfRunId(wfRunId)
                .setDate(LHUtil.fromDate(getCreatedAt()))
                .setValue(value.toProto())
                .setWfSpecId(wfSpecId.toProto());

        return out;
    }

    public VariableIdModel getObjectId() {
        return new VariableIdModel(wfRunId, threadRunNumber, name);
    }

    public Date getCreatedAt() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("wfSpecVersion", GetableIndex.ValueType.SINGLE),
                                Pair.of("variable", GetableIndex.ValueType.DYNAMIC)),
                        Optional.empty(),
                        variable -> ((VariableModel) variable).isIndexable()),
                new GetableIndex<>(
                        List.of(
                                Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                                Pair.of("variable", GetableIndex.ValueType.DYNAMIC)),
                        Optional.empty(),
                        variable -> ((VariableModel) variable).isIndexable()));
    }

    private boolean isIndexable() {
        return !name.equals(LHConstants.EXT_EVT_HANDLER_VAR) && value.getType() != VariableType.NULL;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(new IndexedField(key, this.getWfSpec().getName(), TagStorageType.LOCAL));
            }
            case "wfSpecVersion" -> {
                return List.of(new IndexedField(
                        key, LHUtil.toLHDbVersionFormat(this.getWfSpec().version), TagStorageType.LOCAL));
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
        ThreadVarDefModel threadVarDef = wfSpec.getAllVariables().get(name);

        TagStorageType indexType = TagStorageType.LOCAL;

        if (!threadVarDef.isSearchable()) {
            return List.of();
        }

        // Current behavior is that null variables are NOT indexed. This may change in future
        // releases, but it will be a backwards-compatible change.
        if (value.getType() == VariableType.NULL) {
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
                Map<String, Object> flattenedMap = new HashMap<>();
                flattenJsonObj("$.", value.getJsonObjVal(), flattenedMap);

                return flattenedMap.entrySet().stream()
                        .filter(flatKeyValue -> threadVarDef.isSearchableOn(flatKeyValue.getKey()))
                        .map(flatKeyValue -> {
                            return new IndexedField(
                                    this.getName() + "_" + flatKeyValue.getKey(), flatKeyValue.getValue(), indexType);
                        })
                        .toList();
            }
            default -> {
                log.warn("Tags unimplemented for variable type: {}", value.getType());
                return List.of();
            }
        }
    }

    private static void flattenJsonObj(String prefix, Map<String, Object> map, Map<String, Object> flattenedMap) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenJsonObj(prefix + key + ".", nestedMap, flattenedMap);
            } else {
                flattenedMap.put(prefix + key, value);
            }
        }
    }
}
