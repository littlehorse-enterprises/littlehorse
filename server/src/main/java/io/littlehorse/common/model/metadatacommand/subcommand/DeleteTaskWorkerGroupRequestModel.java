package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;

public class DeleteTaskWorkerGroupRequestModel extends CoreSubCommand {

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
  public Builder toProto() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toProto'");
  }

  @Override
  public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'initFrom'");
  }

  @Override
  public Class getProtoBaseClass() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getProtoBaseClass'");
  }
  
}
