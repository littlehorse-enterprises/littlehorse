package io.littlehorse.common.model.getable.core.wfrun;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.protobuf.Message;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.objectId.ArchivedThreadRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ArchivedThreadRun;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

public class ArchivedThreadRunModel extends CoreGetable<ArchivedThreadRun> implements CoreOutputTopicGetable<ArchivedThreadRun> {

  private ArchivedThreadRunIdModel id;

  @Getter
  private ThreadRunModel threadRun;

  public ArchivedThreadRunModel(ThreadRunModel threadRun) {
    this.threadRun = threadRun;
    this.id = new ArchivedThreadRunIdModel(threadRun.getWfRun().getId(), threadRun.getNumber());
  }

  @Override
  public Date getCreatedAt() {
    return threadRun.getStartTime();
  }

  @Override
  public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
    return List.of();
  }

  @Override
  public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
    return List.of();
  }

  @Override
  public ArchivedThreadRunIdModel getObjectId() {
    return id;
  }

  @Override
  public ArchivedThreadRun.Builder toProto() {
    ArchivedThreadRun.Builder out = ArchivedThreadRun.newBuilder();
    out.setThreadRun(this.threadRun.toProto());
    return out;
  }

  @Override
  public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
    ArchivedThreadRun p = (ArchivedThreadRun) proto;
    this.threadRun = ThreadRunModel.fromProto(p.getThreadRun(), context);
  }

  @Override
  public Class<ArchivedThreadRun> getProtoBaseClass() {
    return ArchivedThreadRun.class;
  }
  
}
