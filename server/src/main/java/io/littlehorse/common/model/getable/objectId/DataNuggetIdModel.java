package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.DataNuggetModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.DataNugget;
import io.littlehorse.sdk.common.proto.DataNuggetId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DataNuggetIdModel extends CoreObjectId<DataNuggetId, DataNugget, DataNuggetModel> {

    private String key;
    private ExternalEventDefIdModel externalEventDefId;
    private String guid;

    public DataNuggetIdModel() {}

    public DataNuggetIdModel(String key, ExternalEventDefIdModel extEvtDefId, String guid) {
        this.key = key;
        this.externalEventDefId = extEvtDefId;
        this.guid = guid;
    }

    @Override
    public Class<DataNuggetId> getProtoBaseClass() {
        return DataNuggetId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(key);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        DataNuggetId p = (DataNuggetId) proto;
        key = p.getKey();
        guid = p.getGuid();
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
    }

    @Override
    public DataNuggetId.Builder toProto() {
        DataNuggetId.Builder out = DataNuggetId.newBuilder()
                .setKey(key)
                .setExternalEventDefId(externalEventDefId.toProto())
                .setGuid(guid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(key, externalEventDefId.toString(), guid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        key = split[0];
        externalEventDefId =
                (ExternalEventDefIdModel) ObjectIdModel.fromString(split[1], ExternalEventDefIdModel.class);
        guid = split[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.DATA_NUGGET;
    }

    public String getExternalEventDefName() {
        return externalEventDefId.getName();
    }
}
