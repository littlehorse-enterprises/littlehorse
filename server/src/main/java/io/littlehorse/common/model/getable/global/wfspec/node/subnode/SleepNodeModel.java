package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.SleepNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.SleepNode;
import io.littlehorse.sdk.common.proto.SleepNode.SleepLengthCase;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class SleepNodeModel extends SubNode<SleepNode> {

    public SleepLengthCase type;
    public VariableAssignmentModel rawSeconds;
    public VariableAssignmentModel timestamp;
    public VariableAssignmentModel isoDate;
    private CoreProcessorContext processorContext;

    public Class<SleepNode> getProtoBaseClass() {
        return SleepNode.class;
    }

    public SleepNode.Builder toProto() {
        SleepNode.Builder out = SleepNode.newBuilder();

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

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        SleepNode p = (SleepNode) proto;
        type = p.getSleepLengthCase();
        switch (type) {
            case RAW_SECONDS:
                rawSeconds = VariableAssignmentModel.fromProto(p.getRawSeconds(), context);
                break;
            case TIMESTAMP:
                timestamp = VariableAssignmentModel.fromProto(p.getTimestamp(), context);
                break;
            case ISO_DATE:
                isoDate = VariableAssignmentModel.fromProto(p.getIsoDate(), context);
                break;
            case SLEEPLENGTH_NOT_SET:
                throw new RuntimeException("Not possible");
        }
        this.processorContext = context.castOnSupport(CoreProcessorContext.class);
    }

    @Override
    public void validate(MetadataProcessorContext ctx) throws InvalidNodeException {
        switch (type) {
            case RAW_SECONDS:
                boolean canResolveToInt = rawSeconds.canBeType(VariableType.INT, node.threadSpec);
                if (!canResolveToInt) {
                    throw new InvalidNodeException(
                            new LHVarSubError(
                                    null, "Invalid value: variable can't resolve to " + VariableType.INT.name()),
                            node);
                }
                break;
            case ISO_DATE:
                boolean canResolveToStr = isoDate.canBeType(VariableType.STR, node.threadSpec);
                if (!canResolveToStr) {
                    throw new InvalidNodeException(
                            new LHVarSubError(
                                    null, "Invalid value: variable can't resolve to " + VariableType.STR.name()),
                            node);
                }
                break;
            case TIMESTAMP:
                boolean canResolveToTimestampOrInt = timestamp.canBeType(VariableType.TIMESTAMP, node.threadSpec)
                        || timestamp.canBeType(VariableType.INT, node.threadSpec);
                if (!canResolveToTimestampOrInt) {
                    throw new InvalidNodeException(
                            new LHVarSubError(
                                    null,
                                    "Invalid value: variable can't resolve to %s or %s"
                                            .formatted(VariableType.TIMESTAMP.name(), VariableType.INT.name())),
                            node);
                }
                break;
            case SLEEPLENGTH_NOT_SET:
                throw new InvalidNodeException("Invalid value case for SleepNode", node);
        }
    }

    @Override
    public SleepNodeRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new SleepNodeRunModel(processorContext);
    }

    public Date getMaturationTime(ThreadRunModel thread) throws LHVarSubError {
        switch (type) {
            case RAW_SECONDS:
                long timeToSleep = thread.assignVariable(rawSeconds).asInt().getIntVal();
                return new Date(System.currentTimeMillis() + (1000 * timeToSleep));
            case ISO_DATE:
                String dateStr = thread.assignVariable(isoDate).asStr().getStrVal();
                try {
                    return new Date(Instant.parse(dateStr).toEpochMilli());
                } catch (Exception exn) {
                    throw new LHVarSubError(exn, "failed parsing date string " + dateStr + ": " + exn.getMessage());
                }
            case TIMESTAMP:
                long epochSeconds = thread.assignVariable(timestamp).asInt().getIntVal();
                return new Date(epochSeconds);
            case SLEEPLENGTH_NOT_SET:
        }
        throw new RuntimeException("Not possible");
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        return Optional.of(new ReturnTypeModel());
    }
}
