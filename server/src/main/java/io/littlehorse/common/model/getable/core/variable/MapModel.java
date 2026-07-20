package io.littlehorse.common.model.getable.core.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.structdef.InlineMapDefModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Map;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class MapModel extends LHSerializable<Map> {

    @Getter
    private List<MapEntryModel> entries;

    @Getter
    @Setter
    private InlineMapDefModel mapType;

    public MapModel() {
        this.entries = new ArrayList<>();
    }

    public MapModel(List<MapEntryModel> entries, InlineMapDefModel mapType) {
        this.entries = new ArrayList<>();
        if (entries != null) {
            for (MapEntryModel entry : entries) {
                this.entries.add(new MapEntryModel(
                        entry.getKey().getCopy(), entry.getValue().getCopy()));
            }
        }
        this.mapType = mapType == null ? null : new InlineMapDefModel(mapType);
    }

    public MapModel(MapModel other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy-construct MapModel from null");
        }

        this.entries = new ArrayList<>();
        if (other.entries != null) {
            for (MapEntryModel entry : other.entries) {
                this.entries.add(new MapEntryModel(
                        entry.getKey().getCopy(), entry.getValue().getCopy()));
            }
        }

        this.mapType = other.mapType == null ? null : new InlineMapDefModel(other.mapType);
    }

    @Override
    public Map.Builder toProto() {
        Map.Builder out = Map.newBuilder();
        for (MapEntryModel entry : entries) {
            out.addEntries(Map.Entry.newBuilder()
                    .setKey(entry.getKey().toProto())
                    .setValue(entry.getValue().toProto()));
        }
        if (mapType != null) {
            out.setMapType(mapType.toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Map p = (Map) proto;
        entries = new ArrayList<>();
        for (Map.Entry entry : p.getEntriesList()) {
            VariableValueModel key = VariableValueModel.fromProto(entry.getKey(), VariableValueModel.class, context);
            VariableValueModel value =
                    VariableValueModel.fromProto(entry.getValue(), VariableValueModel.class, context);
            entries.add(new MapEntryModel(key, value));
        }
        if (p.hasMapType()) {
            this.mapType = InlineMapDefModel.fromProto(p.getMapType(), InlineMapDefModel.class, context);
        }
    }

    public InlineMapDefModel getInlineMapDef() {
        TypeDefinitionModel keyType = mapType != null ? mapType.getKeyType() : null;
        TypeDefinitionModel valueType = mapType != null ? mapType.getValueType() : null;
        return new InlineMapDefModel(keyType, valueType);
    }

    @Override
    public Class<Map> getProtoBaseClass() {
        return Map.class;
    }

    /**
     * A single key/value entry in the MapModel.
     */
    @Getter
    public static final class MapEntryModel {
        private final VariableValueModel key;
        private final VariableValueModel value;

        public MapEntryModel(VariableValueModel key, VariableValueModel value) {
            this.key = key;
            this.value = value;
        }
    }
}
