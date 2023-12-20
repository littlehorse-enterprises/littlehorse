package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.StatusChangedModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.proto.AggregateWfMetrics;
import io.littlehorse.common.proto.StatusChanged;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.ProcessorContext;

@Getter
@Slf4j
public class AggregateWfMetricsModel extends LHSerializable<AggregateWfMetrics> implements RepartitionSubCommand {

    private WfSpecIdModel wfSpecId;
    private String tenantId;
    private final List<StatusChangedModel> changes;

    public AggregateWfMetricsModel() {
        this.changes = new ArrayList<>();
    }

    public AggregateWfMetricsModel(WfSpecIdModel wfSpecId, List<StatusChangedModel> changes, String tenantId) {
        this.wfSpecId = wfSpecId;
        this.changes = changes;
        this.tenantId = tenantId;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        AggregateWfMetrics p = (AggregateWfMetrics) proto;
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.tenantId = p.getTenantId();
        changes.clear();
        p.getChangesList().stream()
                .map(statusChanged -> LHSerializable.fromProto(statusChanged, StatusChangedModel.class, context))
                .forEach(changes::add);
    }

    @Override
    public AggregateWfMetrics.Builder toProto() {
        AggregateWfMetrics.Builder out = AggregateWfMetrics.newBuilder();
        out.setWfSpecId(wfSpecId.toProto());
        out.setTenantId(tenantId);
        List<StatusChanged> statusChangesProto = changes.stream()
                .map(StatusChangedModel::toProto)
                .map(StatusChanged.Builder::build)
                .toList();
        out.addAllChanges(statusChangesProto);
        return out;
    }

    @Override
    public Class<AggregateWfMetrics> getProtoBaseClass() {
        return AggregateWfMetrics.class;
    }

    @Override
    public String getPartitionKey() {
        return wfSpecId.getName();
    }

    @Override
    public void process(ModelStore repartitionedStore, ProcessorContext<Void, Void> ctx) {
        log.info("para el wfspec %s hay %s cambios de estados".formatted(wfSpecId.getName(), changes.size()));
        for (StatusChangedModel change : changes) {
            log.info("cambio desde %s hasta %s"
                    .formatted(
                            change.getLhStatusChanged().getPreviousStatus(),
                            change.getLhStatusChanged().getNewStatus()));
        }
    }
}
