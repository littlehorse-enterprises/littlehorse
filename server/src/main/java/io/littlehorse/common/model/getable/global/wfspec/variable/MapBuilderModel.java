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
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
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
     * 2. Else if entries is non-empty, derive from the first entry.
     * 3. Else return wildcard map.
     */
    public Optional<TypeDefinitionModel> resolveTypeDefinition(
            ReadOnlyMetadataManager manager, WfSpecModel wfSpec, String threadSpecName)
            throws InvalidExpressionException {
        if (mapType != null) {
            return Optional.of(new TypeDefinitionModel(mapType));
        }

        if (!entries.isEmpty()) {
            MapBuilderEntryModel first = entries.get(0);
            Optional<TypeDefinitionModel> keyTypeOpt = first.getKey().getSourceType(manager, wfSpec, threadSpecName);
            Optional<TypeDefinitionModel> valTypeOpt = first.getValue().getSourceType(manager, wfSpec, threadSpecName);
            TypeDefinitionModel keyType = keyTypeOpt.orElse(new TypeDefinitionModel());
            TypeDefinitionModel valType = valTypeOpt.orElse(new TypeDefinitionModel());
            return Optional.of(new TypeDefinitionModel(new InlineMapDefModel(keyType, valType)));
        }

        // Empty builder, no mapType — wildcard map
        return Optional.of(
                new TypeDefinitionModel(new InlineMapDefModel(new TypeDefinitionModel(), new TypeDefinitionModel())));
    }

    public void validate(NodeModel source, ReadOnlyMetadataManager manager, ThreadSpecModel threadSpec)
            throws InvalidExpressionException {
        WfSpecModel wfSpec = threadSpec.wfSpec;
        String threadSpecName = threadSpec.getName();

        for (int i = 0; i < entries.size(); i++) {
            MapBuilderEntryModel entry = entries.get(i);

            // Key must resolve to primitive type (skip if unknowable)
            Optional<TypeDefinitionModel> keyTypeOpt = entry.getKey().getSourceType(manager, wfSpec, threadSpecName);
            if (keyTypeOpt.isPresent()) {
                TypeDefinitionModel keyType = keyTypeOpt.get();
                if (keyType.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE
                        && keyType.getDefinedTypeCase() != DefinedTypeCase.DEFINEDTYPE_NOT_SET) {
                    throw new InvalidExpressionException(
                            "MapBuilder entry " + i + ": key must resolve to a primitive type, got " + keyType);
                }
            }

            // If mapType declared, validate key/value compatibility
            if (mapType != null) {
                if (keyTypeOpt.isPresent() && !mapType.getKeyType().isCompatibleWith(keyTypeOpt.get())) {
                    throw new InvalidExpressionException("MapBuilder entry " + i + ": key type " + keyTypeOpt.get()
                            + " is not compatible with declared key type " + mapType.getKeyType());
                }

                Optional<TypeDefinitionModel> valTypeOpt =
                        entry.getValue().getSourceType(manager, wfSpec, threadSpecName);
                if (valTypeOpt.isPresent() && !mapType.getValueType().isCompatibleWith(valTypeOpt.get())) {
                    throw new InvalidExpressionException("MapBuilder entry " + i + ": value type "
                            + valTypeOpt.get()
                            + " is not compatible with declared value type "
                            + mapType.getValueType());
                }
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
