package io.littlehorse.server.streamsimpl.searchutils.internalsearches;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.AttributePb;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LocalTagScanPb;
import io.littlehorse.common.proto.LocalTagScanPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import java.util.ArrayList;
import java.util.List;

public class LocalTagScan extends LHInternalSubSearch<LocalTagScanPb> {

    public GETableClassEnumPb objectType;
    public List<Attribute> attributes;

    public Class<LocalTagScanPb> getProtoBaseClass() {
        return LocalTagScanPb.class;
    }

    public LocalTagScanPb.Builder toProto() {
        LocalTagScanPb.Builder out = LocalTagScanPb.newBuilder();
        for (Attribute attr : attributes) {
            out.addAttributes(attr.toProto());
        }
        return out;
    }

    public LocalTagScan() {
        attributes = new ArrayList<>();
    }

    public void initFrom(MessageOrBuilder proto) {
        LocalTagScanPbOrBuilder p = (LocalTagScanPbOrBuilder) proto;
        for (AttributePb attr : p.getAttributesList()) {
            attributes.add(Attribute.fromProto(attr));
        }
    }

    public static LocalTagScan fromProto(LocalTagScanPbOrBuilder proto) {
        LocalTagScan out = new LocalTagScan();
        out.initFrom(proto);
        return out;
    }
}
