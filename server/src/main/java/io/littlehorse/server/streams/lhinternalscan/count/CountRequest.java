package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.server.streams.lhinternalscan.InternalCount;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import java.util.List;

public abstract class CountRequest<T extends Message> extends LHSerializable<T> {

    protected abstract List<Attribute> countAttributes();

    protected abstract GetableClassEnum getObjectType();

    public final InternalCount internalCount() {
        Tag countedTagToSearch = new Tag(TagStorageType.COUNTED, getObjectType(), countAttributes());
        return new InternalCount(getObjectType(), countedTagToSearch.getAttributeString());
    }
}
