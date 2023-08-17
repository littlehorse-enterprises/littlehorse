package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutUserTaskDefResponseModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskFieldModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.UserTaskField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PutUserTaskDefRequestModel extends SubCommand<PutUserTaskDefRequest> {

    public String name;
    public String description;
    public List<UserTaskFieldModel> fields;

    public PutUserTaskDefRequestModel() {
        fields = new ArrayList<>();
    }

    public Class<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest> getProtoBaseClass() {
        return io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.class;
    }

    public io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.Builder toProto() {
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.Builder out = io.littlehorse.sdk.common.proto.PutUserTaskDefRequest
            .newBuilder()
            .setName(name);
        if (description != null) {
            out.setDescription(description);
        }
        for (UserTaskFieldModel f : fields) {
            out.addFields(f.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest p = (io.littlehorse.sdk.common.proto.PutUserTaskDefRequest) proto;
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

    public PutUserTaskDefResponseModel process(LHDAO dao, LHConfig config) {
        PutUserTaskDefResponseModel out = new PutUserTaskDefResponseModel();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCode.VALIDATION_ERROR;
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
            out.code = LHResponseCode.OK;
            out.result = spec;
            dao.putUserTaskDef(spec);
        } catch (LHValidationError exn) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "Invalid UserTaskDef: " + exn.getMessage();
        }

        return out;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }
}
