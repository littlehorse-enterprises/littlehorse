package io.littlehorse.common.model.meta.usertasks.actiontrigger;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;

// This class will be used later to remove copy-pasta once we implement the other
// triggers (reassign, cancel, etc)
public abstract class SubActionTrigger<T extends Message> extends LHSerializable<T> {

    public abstract void schedule(LHDAO dao) throws LHVarSubError;
}
