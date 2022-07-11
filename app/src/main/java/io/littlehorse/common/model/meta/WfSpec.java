package io.littlehorse.common.model.meta;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.littlehorse.common.LHUtil;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadSpecPb;
import io.littlehorse.common.proto.WFSpecPb;
import io.littlehorse.common.proto.WFSpecPbOrBuilder;

public class WfSpec {
    public String id;
    public Date createdAt;
    public Date updatedAt;

    public Map<String, ThreadSpec> threadSpecs;

    public String entrypointThreadName;
    public LHStatusPb status;

    public WfSpec() {
        threadSpecs = new HashMap<>();
    }

    public WFSpecPb.Builder toProtoBuilder() {
        WFSpecPb.Builder out = WFSpecPb.newBuilder()
            .setId(id)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setUpdatedAt(LHUtil.fromDate(updatedAt))
            .setEntrypointThreadName(entrypointThreadName)
            .setStatus(status);

        if (threadSpecs != null) {
            for (Map.Entry<String, ThreadSpec> p: threadSpecs.entrySet()) {
                out.putThreadSpecs(
                    p.getKey(),
                    p.getValue().toProtoBuilder().build()
                );
            }
        }

        return out;
    }

    public static WfSpec fromProto(WFSpecPbOrBuilder proto) {
        WfSpec out = new WfSpec();
        out.id = proto.getId();
        out.createdAt = LHUtil.fromProtoTs(proto.getCreatedAt());
        out.updatedAt = LHUtil.fromProtoTs(proto.getUpdatedAt());
        out.entrypointThreadName = proto.getEntrypointThreadName();
        out.status = proto.getStatus();

        for (
            Map.Entry<String, ThreadSpecPb> e: proto.getThreadSpecsMap().entrySet()
        ) {
            out.threadSpecs.put(e.getKey(), ThreadSpec.fromProto(e.getValue()));
        }
        return out;
    }
}
