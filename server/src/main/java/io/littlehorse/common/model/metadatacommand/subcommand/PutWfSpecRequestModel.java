package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.WfSpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PutWfSpecRequestModel extends MetadataSubCommand<PutWfSpecRequest> {

    public String name;
    public Map<String, ThreadSpecModel> threadSpecs;
    public String entrypointThreadName;
    public Integer retentionHours;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutWfSpecRequest> getProtoBaseClass() {
        return PutWfSpecRequest.class;
    }

    public PutWfSpecRequestModel() {
        threadSpecs = new HashMap<>();
    }

    public PutWfSpecRequest.Builder toProto() {
        PutWfSpecRequest.Builder out =
                PutWfSpecRequest.newBuilder().setName(name).setEntrypointThreadName(entrypointThreadName);
        if (retentionHours != null) {
            out.setRetentionHours(retentionHours);
        }

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            out.putThreadSpecs(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(Message proto) {
        PutWfSpecRequest p = (PutWfSpecRequest) proto;
        name = p.getName();
        entrypointThreadName = p.getEntrypointThreadName();
        if (p.hasRetentionHours()) retentionHours = p.getRetentionHours();
        for (Map.Entry<String, ThreadSpec> e : p.getThreadSpecsMap().entrySet()) {
            threadSpecs.put(e.getKey(), ThreadSpecModel.fromProto(e.getValue()));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public WfSpec process(MetadataProcessorDAO dao, LHServerConfig config) {
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "WfSpecName must be a valid hostname");
        }

        WfSpecModel spec = new WfSpecModel();
        spec.name = name;
        spec.entrypointThreadName = entrypointThreadName;
        spec.threadSpecs = threadSpecs;
        spec.createdAt = new Date();
        spec.retentionHours = retentionHours == null ? config.getDefaultWfRunRetentionHours() : retentionHours;
        spec.status = LHStatus.RUNNING;
        for (Map.Entry<String, ThreadSpecModel> entry : spec.threadSpecs.entrySet()) {
            ThreadSpecModel tspec = entry.getValue();
            tspec.wfSpecModel = spec;
            tspec.name = entry.getKey();
        }

        WfSpecModel oldVersion = dao.getWfSpec(name, null);
        if (oldVersion != null) {
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }
        spec.validate(dao, config);
        dao.put(spec);
        return spec.toProto().build();
    }

    public static PutWfSpecRequestModel fromProto(PutWfSpecRequest p) {
        PutWfSpecRequestModel out = new PutWfSpecRequestModel();
        out.initFrom(p);
        return out;
    }
}
