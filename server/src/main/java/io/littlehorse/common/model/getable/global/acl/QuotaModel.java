package io.littlehorse.common.model.getable.global.acl;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.ClusterMetadataGetable;
import io.littlehorse.common.model.getable.objectId.QuotaIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Quota;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
public class QuotaModel extends ClusterMetadataGetable<Quota> {

    private QuotaIdModel id;
    private Date createdAt;
    private int writeRequestsPerSecond;

    public QuotaModel() {}

    public QuotaModel(QuotaIdModel id) {
        this.id = id;
    }

    @Override
    public Quota.Builder toProto() {
        Quota.Builder result = Quota.newBuilder()
                .setId(id.toProto())
                .setWriteRequestsPerSecond(writeRequestsPerSecond);

        if (createdAt != null) {
            result.setCreatedAt(LHUtil.fromDate(createdAt));
        }

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        Quota quota = (Quota) proto;
        id = LHSerializable.fromProto(quota.getId(), QuotaIdModel.class, context);
        if (quota.hasCreatedAt()) {
            createdAt = LHUtil.fromProtoTs(quota.getCreatedAt());
        }
        writeRequestsPerSecond = quota.getWriteRequestsPerSecond();
    }

    @Override
    public Class<Quota> getProtoBaseClass() {
        return Quota.class;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<QuotaModel>(
                        List.of(Pair.of("tenantId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<QuotaModel>(
                        List.of(
                                Pair.of("tenantId", GetableIndex.ValueType.SINGLE),
                                Pair.of("principalId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        quota -> quota.getId() != null && quota.getId().hasPrincipal()));
    }

    @Override
    public QuotaIdModel getObjectId() {
        return id;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        if (key.equals("tenantId")) {
            return List.of(new IndexedField(key, id.getTenant().getId(), TagStorageType.LOCAL));
        }

        if (key.equals("principalId") && id.hasPrincipal()) {
            return List.of(new IndexedField(key, id.getPrincipal().getId(), TagStorageType.LOCAL));
        }

        return List.of();
    }
}
