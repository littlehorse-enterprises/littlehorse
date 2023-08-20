package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.actiontrigger;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.dao.CoreProcessorDAO;
import io.littlehorse.common.exceptions.LHVarSubError;

// This class will be used later to remove copy-pasta once we implement the other
// triggers (reassign, cancel, etc)
public abstract class SubActionTrigger<T extends Message> extends LHSerializable<T> {

    public abstract void schedule(CoreProcessorDAO dao) throws LHVarSubError;
}
