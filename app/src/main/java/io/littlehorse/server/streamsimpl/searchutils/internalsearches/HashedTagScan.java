package io.littlehorse.server.streamsimpl.searchutils.internalsearches;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.AttributePbOrBuilder;
import io.littlehorse.common.proto.HashedTagScanPb;
import io.littlehorse.common.proto.HashedTagScanPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.ArrayList;
import java.util.List;

public class HashedTagScan extends LHInternalSubSearch<HashedTagScanPb> {

    public List<Attribute> attributes;

    public HashedTagScan() {
        attributes = new ArrayList<>();
    }

    public Class<HashedTagScanPb> getProtoBaseClass() {
        return HashedTagScanPb.class;
    }

    public HashedTagScanPb.Builder toProto() {
        HashedTagScanPb.Builder out = HashedTagScanPb.newBuilder();
        for (Attribute attr : attributes) {
            out.addAttributes(attr.toProto());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        HashedTagScanPbOrBuilder p = (HashedTagScanPbOrBuilder) proto;
        for (AttributePbOrBuilder attr : p.getAttributesOrBuilderList()) {
            attributes.add(Attribute.fromProto(attr));
        }
    }

    public static HashedTagScan fromProto(HashedTagScanPbOrBuilder proto) {
        HashedTagScan out = new HashedTagScan();
        out.initFrom(proto);
        return out;
    }
}
