package io.littlehorse.common.model.getable.global.externaleventdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ExternalEventRetentionPolicy;
import io.littlehorse.sdk.common.proto.ExternalEventRetentionPolicy.ExtEvtGcPolicyCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ExternalEventRetentionPolicyModel extends LHSerializable<ExternalEventRetentionPolicy> {

    private ExtEvtGcPolicyCase type;
    private Integer secondsAfterPut;

    public ExternalEventRetentionPolicyModel() {
        type = ExtEvtGcPolicyCase.EXTEVTGCPOLICY_NOT_SET;
    }

    @Override
    public Class<ExternalEventRetentionPolicy> getProtoBaseClass() {
        return ExternalEventRetentionPolicy.class;
    }

    @Override
    public ExternalEventRetentionPolicy.Builder toProto() {
        ExternalEventRetentionPolicy.Builder out = ExternalEventRetentionPolicy.newBuilder();

        switch (type) {
            case SECONDS_AFTER_PUT:
                out.setSecondsAfterPut(secondsAfterPut);
                break;
            case EXTEVTGCPOLICY_NOT_SET:
                // nothing to do
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        ExternalEventRetentionPolicy p = (ExternalEventRetentionPolicy) proto;
        type = p.getExtEvtGcPolicyCase();
        switch (type) {
            case SECONDS_AFTER_PUT:
                secondsAfterPut = (int) p.getSecondsAfterPut();
                break;
            case EXTEVTGCPOLICY_NOT_SET:
                // nothing to do.
                break;
        }
    }

    public Optional<Date> scheduleCleanup(Date eventTime) {
        switch (type) {
            case SECONDS_AFTER_PUT:
                return Optional.of(new Date(eventTime
                        .toInstant()
                        .plus(Duration.ofSeconds(secondsAfterPut))
                        .toEpochMilli()));
            case EXTEVTGCPOLICY_NOT_SET:
            default:
                return Optional.empty();
        }
    }
}
