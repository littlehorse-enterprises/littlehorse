package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutWfSpecReply;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.PutWfSpecPb;
import io.littlehorse.jlib.common.proto.ThreadSpecPb;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PutWfSpec extends SubCommand<PutWfSpecPb> {

    public String name;
    public Map<String, ThreadSpec> threadSpecs;
    public String entrypointThreadName;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutWfSpecPb> getProtoBaseClass() {
        return PutWfSpecPb.class;
    }

    public PutWfSpec() {
        threadSpecs = new HashMap<>();
    }

    public PutWfSpecPb.Builder toProto() {
        PutWfSpecPb.Builder out = PutWfSpecPb
            .newBuilder()
            .setName(name)
            .setEntrypointThreadName(entrypointThreadName);

        for (Map.Entry<String, ThreadSpec> e : threadSpecs.entrySet()) {
            out.putThreadSpecs(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(Message proto) {
        PutWfSpecPb p = (PutWfSpecPb) proto;
        name = p.getName();
        entrypointThreadName = p.getEntrypointThreadName();
        for (Map.Entry<String, ThreadSpecPb> e : p.getThreadSpecsMap().entrySet()) {
            threadSpecs.put(e.getKey(), ThreadSpec.fromProto(e.getValue()));
        }
    }

    public boolean hasResponse() {
        return true;
    }

    public PutWfSpecReply process(LHDAO dao, LHConfig config) {
        PutWfSpecReply out = new PutWfSpecReply();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "WfSpec name must be a valid hostname";
            return out;
        }

        WfSpec spec = new WfSpec();
        spec.name = name;
        spec.entrypointThreadName = entrypointThreadName;
        spec.threadSpecs = threadSpecs;
        spec.createdAt = new Date();
        spec.status = LHStatusPb.RUNNING;
        for (Map.Entry<String, ThreadSpec> entry : spec.threadSpecs.entrySet()) {
            ThreadSpec tspec = entry.getValue();
            tspec.wfSpec = spec;
            tspec.name = entry.getKey();
        }

        try {
            WfSpec oldVersion = dao.getWfSpec(name, null);
            if (oldVersion != null) {
                spec.version = oldVersion.version + 1;
            } else {
                spec.version = 0;
            }
            spec.validate(dao.getGlobalMetaStores(), config);
            out.code = LHResponseCodePb.OK;
            out.result = spec;
            dao.putWfSpec(spec);
        } catch (LHValidationError exn) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "Invalid wfSpec: " + exn.getMessage();
        }

        return out;
    }

    public static PutWfSpec fromProto(PutWfSpecPb p) {
        PutWfSpec out = new PutWfSpec();
        out.initFrom(p);
        return out;
    }
}
