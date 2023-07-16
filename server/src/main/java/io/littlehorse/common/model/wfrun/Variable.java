package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.meta.JsonIndex;
import io.littlehorse.common.model.meta.VariableDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.objectId.VariableId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
public class Variable extends Getable<VariablePb> {

    public VariableValue value;
    public String wfRunId;
    public int threadRunNumber;
    public String name;
    public Date date;

    private WfSpec wfSpec;

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
                Optional.empty()
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
        return this.getWfSpec()
            .getThreadSpecs()
            .entrySet()
            .stream()
            .flatMap(stringThreadSpecEntry -> {
                return stringThreadSpecEntry.getValue().getVariableDefs().stream();
            })
            .collect(Collectors.toMap(VariableDef::getName, Function.identity()));
    }

    private List<IndexedField> getDynamicFields() {
        VariableValue variableValue = getValue();
        Map<String, VariableDef> stringVariableDefMap = variableDefMap();
        VariableDef variableDef = stringVariableDefMap.get(this.getName());
        TagStorageTypePb tagStorageTypePb = variableDef.getTagStorageTypePb() != null
            ? variableDef.getTagStorageTypePb()
            : TagStorageTypePb.LOCAL;
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
                flatten("", variableValue.getJsonObjVal(), flattenedMap);
                return flattenedMap
                    .entrySet()
                    .stream()
                    .map(keyValueJson -> {
                        TagStorageTypePb storageTypePb = findStorageTypeFromVariableDef(
                            variableDef,
                            keyValueJson.getKey()
                        )
                            .orElse(TagStorageTypePb.LOCAL);
                        return new IndexedField(
                            keyValueJson.getKey(),
                            keyValueJson.getValue(),
                            storageTypePb
                        );
                    })
                    .toList();
            }
            default -> {
                throw new IllegalArgumentException(
                    "Variable %s not supported yet".formatted(variableValue.getType())
                );
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
            .map(JsonIndex::getIndexTypePb)
            .map(indexTypePb -> {
                return indexTypePb == IndexTypePb.REMOTE_INDEX
                    ? TagStorageTypePb.REMOTE
                    : TagStorageTypePb.LOCAL;
            })
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
                        currentVariableDef.getTagStorageTypePb() != null
                    )
                    ? currentVariableDef.getTagStorageTypePb()
                    : TagStorageTypePb.LOCAL;
            })
            .findFirst()
            .get();
    }
}
