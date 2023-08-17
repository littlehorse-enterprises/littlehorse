package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.JsonIndexModel;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.VariableDefModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.objectId.VariableIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableType;
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
public class VariableModel extends Getable<Variable> {

    public VariableValueModel value;
    public String wfRunId;
    public int threadRunNumber;
    public String name;
    public String threadSpecName;
    public Date date;

    private WfSpecModel wfSpecModel;

    public VariableModel() {}

    public VariableModel(
        String name,
        VariableValueModel value,
        String wfRunId,
        int threadRunNumber,
        WfSpecModel wfSpecModel
    ) {
        this.name = name;
        this.value = value;
        this.wfRunId = wfRunId;
        this.wfSpecModel = wfSpecModel;
        this.threadRunNumber = threadRunNumber;
    }

    public Class<Variable> getProtoBaseClass() {
        return Variable.class;
    }

    public WfSpecModel getWfSpecModel() {
        return wfSpecModel;
    }

    public void setWfSpecModel(WfSpecModel spec) {
        this.wfSpecModel = spec;
    }

    public void initFrom(Message proto) {
        Variable p = (Variable) proto;
        value = VariableValueModel.fromProto(p.getValue());
        wfRunId = p.getWfRunId();
        name = p.getName();
        threadRunNumber = p.getThreadRunNumber();
        date = LHUtil.fromProtoTs(p.getDate());
    }

    public Variable.Builder toProto() {
        Variable.Builder out = Variable
            .newBuilder()
            .setName(name)
            .setThreadRunNumber(threadRunNumber)
            .setWfRunId(wfRunId)
            .setDate(LHUtil.fromDate(getCreatedAt()))
            .setValue(value.toProto());

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
                    ((VariableModel) variable).getValue().getType() !=
                    VariableType.NULL &&
                    !((VariableModel) variable).getName()
                        .equals(LHConstants.EXT_EVT_HANDLER_VAR)
            )
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageType> tagStorageTypePb
    ) {
        switch (key) {
            case "wfSpecName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getWfSpecModel().getName(),
                        TagStorageType.LOCAL
                    )
                );
            }
            case "wfSpecVersion" -> {
                return List.of(
                    new IndexedField(
                        key,
                        LHUtil.toLHDbVersionFormat(this.getWfSpecModel().version),
                        TagStorageType.LOCAL
                    )
                );
            }
            case "variable" -> {
                return getDynamicFields();
            }
        }
        return null;
    }

    private Map<String, VariableDefModel> variableDefMap() {
        Map<String, VariableDefModel> out = new HashMap<>();
        for (ThreadSpecModel tSpec : getWfSpecModel().getThreadSpecs().values()) {
            for (VariableDefModel varDef : tSpec.getVariableDefs()) {
                out.put(varDef.getName(), varDef);
            }
        }

        return out;
    }

    private List<IndexedField> getDynamicFields() {
        VariableValueModel variableValue = getValue();
        Map<String, VariableDefModel> stringVariableDefMap = variableDefMap();
        VariableDefModel variableDef = stringVariableDefMap.get(this.getName());
        TagStorageType tagStorageTypePb = variableDef.getTagStorageType();
        if (
            tagStorageTypePb == null && variableDef.getType() != VariableType.JSON_OBJ
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
                        TagStorageType.LOCAL
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
                        TagStorageType storageTypePb = findStorageTypeFromVariableDef(
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

    private Optional<TagStorageType> findStorageTypeFromVariableDef(
        VariableDefModel variableDef,
        String jsonPath
    ) {
        return variableDef
            .getJsonIndices()
            .stream()
            .filter(jsonIndex -> {
                return jsonIndex.getPath().equals(jsonPath);
            })
            .map(JsonIndexModel::getTagStorageType)
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
    public TagStorageType tagStorageTypePb() {
        return getWfSpecModel()
            .getThreadSpecs()
            .values()
            .stream()
            .map(threadSpec -> {
                VariableDefModel currentVariableDef = threadSpec
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
                    : TagStorageType.LOCAL;
            })
            .findFirst()
            .get();
    }
}
