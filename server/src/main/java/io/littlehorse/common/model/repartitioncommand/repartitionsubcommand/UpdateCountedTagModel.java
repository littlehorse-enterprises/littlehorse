package io.littlehorse.common.model.repartitioncommand.repartitionsubcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.CountedTagModel;
import io.littlehorse.common.proto.DeleteMetricWindow;
import io.littlehorse.common.proto.UpdateCountedTag;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UpdateCountedTagModel extends CoreSubCommand<DeleteMetricWindow> {

    private Tag tag;
    private final static Logger log = LoggerFactory.getLogger(UpdateCountedTagModel.class);

    public UpdateCountedTagModel() {}

    public UpdateCountedTagModel(Tag tag) {
        this.tag = tag;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        UpdateCountedTag updateCountedTagPb = (UpdateCountedTag) proto;
        this.tag = LHSerializable.fromProto(updateCountedTagPb.getTag(), Tag.class, context);
    }

    @Override
    public UpdateCountedTag.Builder toProto() {
        return UpdateCountedTag.newBuilder().setTag(this.tag.toProto());
    }

    @Override
    public Message process(CoreProcessorContext executionContext, LHServerConfig config) {
        TenantScopedStore store = executionContext.getCoreStore();
        String attributeString = tag.getAttributeString();
        CountedTagModel countedTag = store.get(attributeString, CountedTagModel.class);
        if (countedTag == null) {
            countedTag = new CountedTagModel(attributeString);
        }
        countedTag.increment();
        store.put(countedTag);
        log.info("updated " + attributeString + " to " + countedTag.getCount());
        return Empty.getDefaultInstance();
    }

    @Override
    public Class<UpdateCountedTag> getProtoBaseClass() {
        return UpdateCountedTag.class;
    }

    @Override
    public String getPartitionKey() {
        return tag.getAttributeString();
    }
}
