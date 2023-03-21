package io.littlehorse.common.model.observabilityevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import java.util.Date;

public abstract class SubEvent<U extends Message> extends LHSerializable<U> {

    public abstract void updateMetrics(LHDAO dao, Date time, String wfRunId);
}
