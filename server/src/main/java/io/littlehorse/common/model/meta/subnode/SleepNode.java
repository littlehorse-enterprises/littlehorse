package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.common.model.wfrun.ThreadRunModel;
import io.littlehorse.common.model.wfrun.subnoderun.SleepNodeRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.SleepNodePb;
import io.littlehorse.sdk.common.proto.SleepNodePb.SleepLengthCase;
import java.time.Instant;
import java.util.Date;

public class SleepNode extends SubNode<SleepNodePb> {

    public SleepLengthCase type;
    public VariableAssignmentModel rawSeconds;
    public VariableAssignmentModel timestamp;
    public VariableAssignmentModel isoDate;

    public Class<SleepNodePb> getProtoBaseClass() {
        return SleepNodePb.class;
    }

    public SleepNodePb.Builder toProto() {
        SleepNodePb.Builder out = SleepNodePb.newBuilder();

        switch (type) {
            case RAW_SECONDS:
                out.setRawSeconds(rawSeconds.toProto());
                break;
            case TIMESTAMP:
                out.setTimestamp(timestamp.toProto());
                break;
            case ISO_DATE:
                out.setIsoDate(isoDate.toProto());
                break;
            case SLEEPLENGTH_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        return out;
    }

    public void initFrom(Message proto) {
        SleepNodePb p = (SleepNodePb) proto;
        type = p.getSleepLengthCase();
        switch (type) {
            case RAW_SECONDS:
                rawSeconds = VariableAssignmentModel.fromProto(p.getRawSeconds());
                break;
            case TIMESTAMP:
                timestamp = VariableAssignmentModel.fromProto(p.getTimestamp());
                break;
            case ISO_DATE:
                isoDate = VariableAssignmentModel.fromProto(p.getIsoDate());
                break;
            case SLEEPLENGTH_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }

    @Override
    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // TODO: once we have schemas, we need to validate that the
        // variable assignments are types that make sense (unsigned int, long,
        // or date string).
    }

    @Override
    public SleepNodeRun createSubNodeRun(Date time) {
        return new SleepNodeRun();
    }

    public Date getMaturationTime(ThreadRunModel thread) throws LHVarSubError {
        switch (type) {
            case RAW_SECONDS:
                long timeToSleep = thread.assignVariable(rawSeconds).asInt().intVal;
                return new Date(System.currentTimeMillis() + (1000 * timeToSleep));
            case ISO_DATE:
                String dateStr = thread.assignVariable(isoDate).asStr().strVal;
                try {
                    return new Date(Instant.parse(dateStr).toEpochMilli());
                } catch (Exception exn) {
                    throw new LHVarSubError(
                        exn,
                        "failed parsing date string " +
                        dateStr +
                        ": " +
                        exn.getMessage()
                    );
                }
            case TIMESTAMP:
                long ts = thread.assignVariable(timestamp).asInt().intVal;
                return new Date(ts);
            case SLEEPLENGTH_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }
}
