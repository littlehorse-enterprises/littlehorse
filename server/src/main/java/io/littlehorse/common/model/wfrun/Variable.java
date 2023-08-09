package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.JsonIndex;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@Slf4j
public class Variable extends Getable<VariablePb> {

    public VariableValue value;
    public String wfRunId;
    public int threadRunNumber;
    public String name;
    public String threadSpecName;
    public Date date;

    private WfSpec wfSpec;

    public Variable() {}

    public Variable(
        String name,
        VariableValue value,
        String wfRunId,
        int threadRunNumber,
        WfSpec wfSpec
    ) {
        this.name = name;
        this.value = value;
        this.wfRunId = wfRunId;
        this.wfSpec = wfSpec;
        this.threadRunNumber = threadRunNumber;
    }

    public Class<VariablePb> getProtoBaseClass() {
        return VariablePb.class;
    }

    public WfSpec getWfSpec() {
        return wfSpec;
    }

    public void setWfSpec(WfSpec spec) {
        this.wfSpec = spec;
    }

    public void initFrom(Message proto) {
        VariablePb p = (VariablePb) proto;
        value = VariableValue.fromProto(p.getValue());
        wfRunId = p.getWfRunId();
        name = p.getName();
        threadRunNumber = p.getThreadRunNumber();
        date = LHUtil.fromProtoTs(p.getDate());
    }

    public VariablePb.Builder toProto() {
        VariablePb.Builder out = VariablePb
            .newBuilder()
            .setName(name)
            .setThreadRunNumber(threadRunNumber)
            .setWfRunId(wfRunId)
            .setDate(LHUtil.fromDate(getCreatedAt()))
            .setValue(value.toProto());

        return out;
    }

    public VariableId getObjectId() {
        return new VariableId(wfRunId, threadRunNumber, name);
    }

    public Date getCreatedAt() {
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of(
            new GetableIndex<>(
                List.of(
                    Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE),
                    Pair.of("wfSpecVersion", GetableIndex.ValueType.SINGLE),
                    Pair.of("variable", GetableIndex.ValueType.DYNAMIC)
                ),
                Optional.empty(),
                variable ->
                    ((Variable) variable).getValue().getType() !=
                    VariableTypePb.NULL &&
                    !((Variable) variable).getName()
                        .equals(LHConstants.EXT_EVT_HANDLER_VAR)
            )
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getWfSpec().getName(),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
            case "wfSpecVersion" -> {
                return List.of(
                    new IndexedField(
                        key,
                        LHUtil.toLHDbVersionFormat(this.getWfSpec().version),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
            case "variable" -> {
                return getDynamicFields();
            }
        }
        return null;
    }

    private Map<String, VariableDef> variableDefMap() {
        Map<String, VariableDef> out = new HashMap<>();
        for (ThreadSpec tSpec : getWfSpec().getThreadSpecs().values()) {
            for (VariableDef varDef : tSpec.getVariableDefs()) {
                out.put(varDef.getName(), varDef);
            }
        }

        return out;
    }

    private List<IndexedField> getDynamicFields() {
        VariableValue variableValue = getValue();
        Map<String, VariableDef> stringVariableDefMap = variableDefMap();
        VariableDef variableDef = stringVariableDefMap.get(this.getName());
        TagStorageTypePb tagStorageTypePb = variableDef.getTagStorageType();
        if (
            tagStorageTypePb == null &&
            variableDef.getType() != VariableTypePb.JSON_OBJ
        ) {
            return List.of();
        }
        switch (variableValue.getType()) {
            case STR -> {
                return List.of(
                    new IndexedField(
                        this.getName(),
                        variableValue.getStrVal(),
                        tagStorageTypePb
                    )
                );
            }
            case BOOL -> {
                return List.of(
                    new IndexedField(
                        this.getName(),
                        variableValue.getBoolVal(),
                        TagStorageTypePb.LOCAL
                    )
                );
            }
            case INT -> {
                return List.of(
                    new IndexedField(
                        this.getName(),
                        variableValue.getIntVal(),
                        tagStorageTypePb
                    )
                );
            }
            case DOUBLE -> {
                return List.of(
                    new IndexedField(
                        this.getName(),
                        variableValue.getDoubleVal(),
                        tagStorageTypePb
                    )
                );
            }
            case JSON_OBJ -> {
                Map<String, Object> flattenedMap = new HashMap<>();
                flatten("$.", variableValue.getJsonObjVal(), flattenedMap);
                return flattenedMap
                    .entrySet()
                    .stream()
                    .map(keyValueJson -> {
                        TagStorageTypePb storageTypePb = findStorageTypeFromVariableDef(
                            variableDef,
                            keyValueJson.getKey()
                        )
                            .orElse(null);
                        if (storageTypePb == null) {
                            return null;
                        }
                        return new IndexedField(
                            keyValueJson.getKey(),
                            keyValueJson.getValue(),
                            storageTypePb
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();
            }
            default -> {
                log.warn(
                    "Tags unimplemented for variable type: {}",
                    variableValue.getType()
                );
                return List.of();
            }
        }
    }

    private Optional<TagStorageTypePb> findStorageTypeFromVariableDef(
        VariableDef variableDef,
        String jsonPath
    ) {
        return variableDef
            .getJsonIndices()
            .stream()
            .filter(jsonIndex -> {
                return jsonIndex.getPath().equals(jsonPath);
            })
            .map(JsonIndex::getTagStorageType)
            .findFirst();
    }

    private static void flatten(
        String prefix,
        Map<String, Object> map,
        Map<String, Object> flattenedMap
    ) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flatten(prefix + key + ".", nestedMap, flattenedMap);
            } else {
                flattenedMap.put(prefix + key, value);
            }
        }
    }

    @Override
    public TagStorageTypePb tagStorageTypePb() {
        return getWfSpec()
            .getThreadSpecs()
            .values()
            .stream()
            .map(threadSpec -> {
                VariableDef currentVariableDef = threadSpec
                    .getVariableDefs()
                    .stream()
                    .filter(variableDef ->
                        variableDef.getName().equals(this.getName())
                    )
                    .findFirst()
                    .orElse(null);
                return (
                        currentVariableDef != null &&
                        currentVariableDef.getTagStorageType() != null
                    )
                    ? currentVariableDef.getTagStorageType()
                    : TagStorageTypePb.LOCAL;
            })
            .findFirst()
            .get();
    }
}
