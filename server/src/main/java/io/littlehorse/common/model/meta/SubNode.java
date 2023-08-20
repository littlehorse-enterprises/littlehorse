package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class SubNode<T extends Message> extends LHSerializable<T> {

    public abstract SubNodeRun<?> createSubNodeRun(Date time);

    public abstract void validate(LHGlobalMetaStores stores, LHConfig config) throws LHValidationError;

    protected NodeModel node;

    public void setNode(NodeModel node) {
        this.node = node;
    }

    // Can be overriden
    public Set<String> getNeededVariableNames() {
        return new HashSet<>();
    }
}
