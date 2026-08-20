package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MapBuilder;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class MapBuilderModel extends LHSerializable<MapBuilder> {

    private List<MapBuilderEntryModel> entries = new ArrayList<>();

    @Setter
    private InlineMapDefModel mapType; // nullable

    @Override
    public Class<MapBuilder> getProtoBaseClass() {
        return MapBuilder.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        MapBuilder p = (MapBuilder) proto;
        entries = new ArrayList<>();
        for (MapBuilder.Entry entry : p.getEntriesList()) {
            entries.add(MapBuilderEntryModel.fromProto(entry, context));
        }
        if (p.hasMapType()) {
            mapType = InlineMapDefModel.fromProto(p.getMapType(), context);
        }
    }

    @Override
    public MapBuilder.Builder toProto() {
        MapBuilder.Builder out = MapBuilder.newBuilder();
        for (MapBuilderEntryModel entry : entries) {
            out.addEntries(entry.toProto());
        }
        if (mapType != null) {
            out.setMapType(mapType.toProto().build());
        }
        return out;
    }

    public static MapBuilderModel fromProto(MapBuilder proto, ExecutionContext context) {
        MapBuilderModel out = new MapBuilderModel();
        out.initFrom(proto, context);
        return out;
    }

    public Set<String> getRequiredWfRunVarNames() {
        Set<String> out = new HashSet<>();
        for (MapBuilderEntryModel entry : entries) {
            out.addAll(entry.getKey().getRequiredWfRunVarNames());
            out.addAll(entry.getValue().getRequiredWfRunVarNames());
        }
        return out;
    }

    public Set<String> getRequiredNodeNames() {
        Set<String> out = new HashSet<>();
        for (MapBuilderEntryModel entry : entries) {
            out.addAll(entry.getKey().getRequiredNodeNames());
            out.addAll(entry.getValue().getRequiredNodeNames());
        }
        return out;
    }

    public Collection<String> getRequiredVariableNames() {
        Set<String> out = new HashSet<>();
        for (MapBuilderEntryModel entry : entries) {
            out.addAll(entry.getKey().getRequiredVariableNames());
            out.addAll(entry.getValue().getRequiredVariableNames());
        }
        return out;
    }

    /**
     * Resolves the TypeDefinition for the resulting Map.
     *
     * Precedence:
     * 1. If mapType != null, return TypeDefinitionModel wrapping it.
     * 2. Else derive key/value types from the first entry.
     *
     * A native Map must always have concrete key/value types, so this throws rather than ever
     * producing a wildcard/untyped Map.
     */
    public Optional<TypeDefinitionModel> resolveTypeDefinition(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        if (mapType != null) {
            return Optional.of(new TypeDefinitionModel(mapType));
        }

        if (entries.isEmpty()) {
            throw new InvalidExpressionException(
                    "Cannot build an untyped empty Map; declare the Map's key and value types (map_type).");
        }

        MapBuilderEntryModel first = entries.get(0);
        Optional<TypeDefinitionModel> keyTypeOpt = first.getKey().getSourceType(manager, wfSpec, threadSpecName);
        Optional<TypeDefinitionModel> valTypeOpt = first.getValue().getSourceType(manager, wfSpec, threadSpecName);
        if (keyTypeOpt.isEmpty() || valTypeOpt.isEmpty()) {
            throw new InvalidExpressionException(
                    "Cannot resolve Map key/value types from the entries; declare the Map's key and value types "
                            + "(map_type).");
        }
        return Optional.of(new TypeDefinitionModel(new InlineMapDefModel(keyTypeOpt.get(), valTypeOpt.get())));
    }

    public void validate(NodeModel source, ReadOnlyMetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        WfSpecModel wfSpec = threadSpec.wfSpec;
        String threadSpecName = threadSpec.getName();

        // Resolves the concrete Map type or throws if it cannot be determined.
        TypeDefinitionModel resolved = resolveTypeDefinition(manager, wfSpec, threadSpecName)
                .orElseThrow(() -> new InvalidExpressionException("Cannot resolve Map type for MapBuilder"));

        // Enforce native-Map typing rules: primitive non-JSON key, concrete non-JSON value.
        try {
            resolved.validateMapKeyTypes();
        } catch (IllegalArgumentException e) {
            throw new InvalidExpressionException(e.getMessage());
        }

        InlineMapDefModel resolvedMap = resolved.getInlineMapDef();
        TypeDefinitionModel keyType = resolvedMap.getKeyType();
        TypeDefinitionModel valueType = resolvedMap.getValueType();

        for (int i = 0; i < entries.size(); i++) {
            MapBuilderEntryModel entry = entries.get(i);

            Optional<TypeDefinitionModel> keyTypeOpt = entry.getKey().getSourceType(manager, wfSpec, threadSpecName);
            if (keyTypeOpt.isPresent() && !keyType.isCompatibleWith(keyTypeOpt.get())) {
                throw new InvalidExpressionException("MapBuilder entry " + i + ": key type " + keyTypeOpt.get()
                        + " is not compatible with Map key type " + keyType);
            }

            Optional<TypeDefinitionModel> valTypeOpt = entry.getValue().getSourceType(manager, wfSpec, threadSpecName);
            if (valTypeOpt.isPresent() && !valueType.isCompatibleWith(valTypeOpt.get())) {
                throw new InvalidExpressionException("MapBuilder entry " + i + ": value type " + valTypeOpt.get()
                        + " is not compatible with Map value type " + valueType);
            }
        }
    }

    public static final class MapBuilderEntryModel extends LHSerializable<MapBuilder.Entry> {

        @Getter
        @Setter
        private VariableAssignmentModel key;

        @Getter
        @Setter
        private VariableAssignmentModel value;

        @Override
        public Class<MapBuilder.Entry> getProtoBaseClass() {
            return MapBuilder.Entry.class;
        }

        @Override
        public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
            MapBuilder.Entry p = (MapBuilder.Entry) proto;
            key = VariableAssignmentModel.fromProto(p.getKey(), context);
            value = VariableAssignmentModel.fromProto(p.getValue(), context);
        }

        @Override
        public MapBuilder.Entry.Builder toProto() {
            return MapBuilder.Entry.newBuilder().setKey(key.toProto()).setValue(value.toProto());
        }

        public static MapBuilderEntryModel fromProto(MapBuilder.Entry proto, ExecutionContext context) {
            MapBuilderEntryModel out = new MapBuilderEntryModel();
            out.initFrom(proto, context);
            return out;
        }
    }
}
