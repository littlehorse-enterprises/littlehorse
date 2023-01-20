package io.littlehorse.server.streamsimpl.searchutils.internalsearches;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.proto.ObjectIdPrefixScanPb;
import io.littlehorse.common.proto.ObjectIdPrefixScanPbOrBuilder;
import io.littlehorse.server.streamsimpl.searchutils.LHInternalSubSearch;

public class ObjectIdPrefixScan extends LHInternalSubSearch<ObjectIdPrefixScanPb> {

    public String objectIdPrefix;

    public Class<ObjectIdPrefixScanPb> getProtoBaseClass() {
        return ObjectIdPrefixScanPb.class;
    }

    public ObjectIdPrefixScanPb.Builder toProto() {
        ObjectIdPrefixScanPb.Builder out = ObjectIdPrefixScanPb.newBuilder();
        out.setObjectIdPrefix(objectIdPrefix);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ObjectIdPrefixScanPbOrBuilder p = (ObjectIdPrefixScanPbOrBuilder) proto;
        objectIdPrefix = p.getObjectIdPrefix();
    }

    public static ObjectIdPrefixScan fromProto(ObjectIdPrefixScanPbOrBuilder proto) {
        ObjectIdPrefixScan out = new ObjectIdPrefixScan();
        out.initFrom(proto);
        return out;
    }
}
