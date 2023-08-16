package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutUserTaskDefReply;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskFieldModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.PutUserTaskDefPb;
import io.littlehorse.sdk.common.proto.UserTaskField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PutUserTaskDef extends SubCommand<PutUserTaskDefPb> {

    public String name;
    public String description;
    public List<UserTaskFieldModel> fields;

    public PutUserTaskDef() {
        fields = new ArrayList<>();
    }

    public Class<PutUserTaskDefPb> getProtoBaseClass() {
        return PutUserTaskDefPb.class;
    }

    public PutUserTaskDefPb.Builder toProto() {
        PutUserTaskDefPb.Builder out = PutUserTaskDefPb.newBuilder().setName(name);
        if (description != null) {
            out.setDescription(description);
        }
        for (UserTaskFieldModel f : fields) {
            out.addFields(f.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        PutUserTaskDefPb p = (PutUserTaskDefPb) proto;
        name = p.getName();
        if (p.hasDescription()) description = p.getDescription();
        for (UserTaskField utfpb : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utfpb, UserTaskFieldModel.class));
        }
    }

    public String getPartitionkey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public boolean hasResponse() {
        return true;
    }

    public PutUserTaskDefReply process(LHDAO dao, LHConfig config) {
        PutUserTaskDefReply out = new PutUserTaskDefReply();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "UserTaskDef name must be a valid hostname";
            return out;
        }

        UserTaskDefModel spec = new UserTaskDefModel();
        spec.name = name;
        spec.description = description;
        spec.fields = fields;
        spec.createdAt = new Date();

        UserTaskDefModel oldVersion = dao.getUserTaskDef(name, null);
        if (oldVersion != null) {
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }

        try {
            spec.validate(dao.getGlobalMetaStores(), config);
            out.code = LHResponseCodePb.OK;
            out.result = spec;
            dao.putUserTaskDef(spec);
        } catch (LHValidationError exn) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "Invalid UserTaskDef: " + exn.getMessage();
        }

        return out;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }
}
