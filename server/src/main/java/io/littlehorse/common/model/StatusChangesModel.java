package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.StatusChanged;
import io.littlehorse.common.proto.StatusChanges;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatusChangesModel extends LHSerializable<StatusChanges> {

    public List<StatusChangedModel> statusChanges = new ArrayList<>();

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        StatusChanges p = (StatusChanges) proto;
        statusChanges = p.getChangesList().stream()
                .map(statusChanged -> LHSerializable.fromProto(statusChanged, StatusChangedModel.class, context))
                .collect(Collectors.toList());
    }

    @Override
    public StatusChanges.Builder toProto() {
        StatusChanges.Builder out = StatusChanges.newBuilder();
        List<StatusChanged> statusChangesProto = statusChanges.stream()
                .map(StatusChangedModel::toProto)
                .map(StatusChanged.Builder::build)
                .toList();
        out.addAllChanges(statusChangesProto);
        return out;
    }

    @Override
    public Class<StatusChanges> getProtoBaseClass() {
        return StatusChanges.class;
    }
}
