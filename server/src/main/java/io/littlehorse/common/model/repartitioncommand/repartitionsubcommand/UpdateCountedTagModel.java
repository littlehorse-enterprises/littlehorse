package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.CountedTagModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.proto.UpdateCountedTag;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateCountedTagModel extends CoreSubCommand<UpdateCountedTag> {

    private String attributeString;
    private long count;

    private static final Logger log = LoggerFactory.getLogger(UpdateCountedTagModel.class);

    public UpdateCountedTagModel() {}

    public UpdateCountedTagModel(String attributeString, long count) {
        this(attributeString, false, count);
    }

    public UpdateCountedTagModel(String attributeString, boolean delete, long count) {
        this.attributeString = attributeString;
        this.count = count;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        UpdateCountedTag updateCountedTagPb = (UpdateCountedTag) proto;
        this.attributeString = updateCountedTagPb.getAttributeString();
        this.count = updateCountedTagPb.getCount();
    }

    @Override
    public UpdateCountedTag.Builder toProto() {
        return UpdateCountedTag.newBuilder()
                .setAttributeString(this.attributeString)
                .setCount(this.count);
    }

    @Override
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        TenantScopedStore store = executionContext.getCoreStore();
        CountedTagModel countedTag = store.get(attributeString, CountedTagModel.class);
        if (countedTag == null) {
            countedTag = new CountedTagModel(attributeString);
        }
        countedTag.increment(count);
        store.put(countedTag);
        return Empty.getDefaultInstance();
    }

    @Override
    public Class<UpdateCountedTag> getProtoBaseClass() {
        return UpdateCountedTag.class;
    }

    @Override
    public String getPartitionKey() {
        return attributeString;
    }
}
