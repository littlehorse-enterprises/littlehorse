package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.TimerIteratorHint;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class TimerIteratorHintModel extends Storeable<TimerIteratorHint> {

    /**
     * All `Timer`'s for every Tenant go in the same bucket.
     */
    public static final String TIMER_ITERATOR_HINT_KEY = "timer_hint";

    private Timestamp lastProcessedTimer;

    public TimerIteratorHintModel() {}

    public TimerIteratorHintModel(Date date) {
        this.lastProcessedTimer = LHUtil.fromDate(date);
    }

    @Override
    public Class<TimerIteratorHint> getProtoBaseClass() {
        return TimerIteratorHint.class;
    }

    @Override
    public TimerIteratorHint.Builder toProto() {
        return TimerIteratorHint.newBuilder().setLastProcessedTimer(lastProcessedTimer);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        TimerIteratorHint p = (TimerIteratorHint) proto;
        this.lastProcessedTimer = p.getLastProcessedTimer();
    }

    @Override
    public StoreableType getType() {
        return StoreableType.TIMER_ITERATOR_HINT;
    }

    @Override
    public String getStoreKey() {
        return TimerIteratorHintModel.TIMER_ITERATOR_HINT_KEY;
    }
}
