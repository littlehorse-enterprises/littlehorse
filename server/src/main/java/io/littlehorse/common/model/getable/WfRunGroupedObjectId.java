package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;

public abstract class WfRunGroupedObjectId<T extends Message, U extends Message, V extends CoreGetable<U>>
        extends CoreObjectId<T, U, V> {

    public abstract WfRunIdModel getGroupingWfRunId();
}
