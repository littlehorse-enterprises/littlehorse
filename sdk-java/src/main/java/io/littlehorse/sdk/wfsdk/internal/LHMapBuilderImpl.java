package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.MapBuilder;
import io.littlehorse.sdk.wfsdk.LHMapBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class LHMapBuilderImpl implements LHMapBuilder {

    private final WorkflowThreadImpl thread;
    private final List<Entry> entries;

    LHMapBuilderImpl(WorkflowThreadImpl thread) {
        this.thread = thread;
        this.entries = new ArrayList<>();
    }

    @Override
    public LHMapBuilder put(Serializable key, Serializable value) {
        entries.add(new Entry(key, value));
        return this;
    }

    MapBuilder toProto() {
        MapBuilder.Builder builder = MapBuilder.newBuilder();
        for (Entry entry : entries) {
            builder.addEntries(MapBuilder.Entry.newBuilder()
                    .setKey(thread.assignVariable(entry.key))
                    .setValue(thread.assignVariable(entry.value))
                    .build());
        }
        return builder.build();
    }

    private record Entry(Object key, Object value) {}
}
