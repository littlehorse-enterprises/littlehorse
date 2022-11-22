package io.littlehorse.common.model.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GlobalPOSTable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.ExternalEventDefPb;
import io.littlehorse.common.proto.ExternalEventDefPbOrBuilder;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ExternalEventDef extends GlobalPOSTable<ExternalEventDefPbOrBuilder> {

    public String name;
    public int version;
    public Date createdAt;

    public ExternalEventDef() {}

    public Date getCreatedAt() {
        if (createdAt == null) createdAt = new Date();
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public String getSubKey() {
        return LHUtil.getCompositeId(name, String.valueOf(version));
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<ExternalEventDefPb> getProtoBaseClass() {
        return ExternalEventDefPb.class;
    }

    public ExternalEventDefPb.Builder toProto() {
        ExternalEventDefPb.Builder b = ExternalEventDefPb
            .newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(getCreatedAt()));

        return b;
    }

    public void initFrom(MessageOrBuilder p) {
        ExternalEventDefPbOrBuilder proto = (ExternalEventDefPbOrBuilder) p;
        name = proto.getName();
        createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
    }

    public void handlePost(
        POSTable<ExternalEventDefPbOrBuilder> old,
        LHGlobalMetaStores c,
        LHConfig config
    ) throws LHValidationError {
        if (!(old == null || old instanceof ExternalEventDef)) {
            throw new RuntimeException("Bad method call.");
        }
        ExternalEventDef oldTd = old == null ? null : (ExternalEventDef) old;
        if (oldTd != null) {
            throw new LHValidationError(
                null,
                "Conflict: Cannot mutate ExternalEventDef"
            );
        }
    }

    public boolean handleDelete() {
        return true;
    }

    @JsonIgnore
    public List<Tag> getTags() {
        return Arrays.asList(new Tag(this, Pair.of("name", name)));
    }

    public static ExternalEventDef fromProto(ExternalEventDefPbOrBuilder p) {
        ExternalEventDef out = new ExternalEventDef();
        out.initFrom(p);
        return out;
    }
}
