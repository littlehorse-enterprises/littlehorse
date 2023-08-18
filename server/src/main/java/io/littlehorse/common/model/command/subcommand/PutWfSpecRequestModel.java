package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutWfSpecResponseModel;
import io.littlehorse.common.model.meta.ThreadSpecModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PutWfSpecRequestModel
        extends SubCommand<io.littlehorse.sdk.common.proto.PutWfSpecRequest> {

    public String name;
    public Map<String, ThreadSpecModel> threadSpecs;
    public String entrypointThreadName;
    public Integer retentionHours;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<io.littlehorse.sdk.common.proto.PutWfSpecRequest> getProtoBaseClass() {
        return io.littlehorse.sdk.common.proto.PutWfSpecRequest.class;
    }

    public PutWfSpecRequestModel() {
        threadSpecs = new HashMap<>();
    }

    public io.littlehorse.sdk.common.proto.PutWfSpecRequest.Builder toProto() {
        io.littlehorse.sdk.common.proto.PutWfSpecRequest.Builder out =
                io.littlehorse.sdk.common.proto.PutWfSpecRequest.newBuilder()
                        .setName(name)
                        .setEntrypointThreadName(entrypointThreadName);
        if (retentionHours != null) {
            out.setRetentionHours(retentionHours);
        }

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            out.putThreadSpecs(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(Message proto) {
        io.littlehorse.sdk.common.proto.PutWfSpecRequest p =
                (io.littlehorse.sdk.common.proto.PutWfSpecRequest) proto;
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

    public PutWfSpecResponseModel process(LHDAO dao, LHConfig config) {
        PutWfSpecResponseModel out = new PutWfSpecResponseModel();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "WfSpec name must be a valid hostname";
            return out;
        }

        WfSpecModel spec = new WfSpecModel();
        spec.name = name;
        spec.entrypointThreadName = entrypointThreadName;
        spec.threadSpecs = threadSpecs;
        spec.createdAt = new Date();
        spec.retentionHours =
                retentionHours == null ? config.getDefaultWfRunRetentionHours() : retentionHours;
        spec.status = LHStatus.RUNNING;
        for (Map.Entry<String, ThreadSpecModel> entry : spec.threadSpecs.entrySet()) {
            ThreadSpecModel tspec = entry.getValue();
            tspec.wfSpecModel = spec;
            tspec.name = entry.getKey();
        }

        try {
            WfSpecModel oldVersion = dao.getWfSpec(name, null);
            if (oldVersion != null) {
                spec.version = oldVersion.version + 1;
            } else {
                spec.version = 0;
            }
            spec.validate(dao.getGlobalMetaStores(), config);
            out.code = LHResponseCode.OK;
            out.result = spec;
            dao.putWfSpec(spec);
        } catch (LHValidationError exn) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "Invalid wfSpec: " + exn.getMessage();
        }

        return out;
    }

    public static PutWfSpecRequestModel fromProto(
            io.littlehorse.sdk.common.proto.PutWfSpecRequest p) {
        PutWfSpecRequestModel out = new PutWfSpecRequestModel();
        out.initFrom(p);
        return out;
    }
}
