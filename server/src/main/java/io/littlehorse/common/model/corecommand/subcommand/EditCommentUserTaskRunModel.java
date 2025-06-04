package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.EditCommentUserTaskRunRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class EditCommentUserTaskRunModel extends CoreSubCommand<EditCommentUserTaskRunRequest> {

    @Override
    public boolean hasResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasResponse'");
    }

    @Override
    public Message process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

    @Override
    public String getPartitionKey() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPartitionKey'");
    }

    @Override
    public Builder<?> toProto() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toProto'");
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initFrom'");
    }

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProtoBaseClass'");
    }
    
}
