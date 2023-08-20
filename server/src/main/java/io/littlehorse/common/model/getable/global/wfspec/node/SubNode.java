package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class SubNode<T extends Message> extends LHSerializable<T> {

    public abstract SubNodeRun<?> createSubNodeRun(Date time);

    public abstract void validate(ReadOnlyMetadataStore stores, LHConfig config) throws LHValidationError;

    protected NodeModel node;

    public void setNode(NodeModel node) {
        this.node = node;
    }

    // Can be overriden
    public Set<String> getNeededVariableNames() {
        return new HashSet<>();
    }
}
