package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class SubNode<T extends Message> extends LHSerializable<T> {

    public abstract SubNodeRun<?> createSubNodeRun(Date time, CoreProcessorContext processorContext);

    public abstract void validate(MetadataProcessorContext ctx) throws InvalidNodeException;

    protected NodeModel node;

    public void setNode(NodeModel node) {
        this.node = node;
    }

    public abstract Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager);

    // Can be overriden
    public Set<String> getNeededVariableNames() {
        return new HashSet<>();
    }
}
