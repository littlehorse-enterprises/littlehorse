package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.proto.MetadataCommand.MetadataCommandCase;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class MetadataCommandModel extends AbstractCommand<MetadataCommand> {

    private String commandId;
    private Date time;

    private MetadataCommandCase type;
    private PutWfSpecRequestModel putWfSpecRequest;
    private PutTaskDefRequestModel putTaskDefRequest;
    private PutExternalEventDefRequestModel putExternalEventDefRequest;
    private DeleteWfSpecRequestModel deleteWfSpec;
    private DeleteTaskDefRequestModel deleteTaskDef;
    private DeleteExternalEventDefRequestModel deleteExternalEventDef;
    private PutUserTaskDefRequestModel putUserTaskDefRequest;
    private DeleteUserTaskDefRequestModel deleteUserTaskDef;
    private String tenantId;
    private PutPrincipalRequestModel putPrincipal;
    private DeletePrincipalRequestModel deletePrincipal;
    private PutTenantRequestModel putTenant;

    public MetadataCommandModel() {
        super();
        time = new Date();
        commandId = LHUtil.generateGuid();
    }

    public MetadataCommandModel(MetadataSubCommand<?> cmd) {
        this();
        setSubCommand(cmd);
    }

    @Override
    public Class<MetadataCommand> getProtoBaseClass() {
        return MetadataCommand.class;
    }

    @Override
    public MetadataCommand.Builder toProto() {
        MetadataCommand.Builder out = MetadataCommand.newBuilder();
        out.setTime(LHUtil.fromDate(time));
        out.setTenantId(tenantId);

        if (commandId != null) {
            out.setCommandId(commandId);
        }

        switch (type) {
            case PUT_WF_SPEC:
                out.setPutWfSpec(putWfSpecRequest.toProto());
                break;
            case PUT_TASK_DEF:
                out.setPutTaskDef(putTaskDefRequest.toProto());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                out.setPutExternalEventDef(putExternalEventDefRequest.toProto());
                break;
            case DELETE_EXTERNAL_EVENT_DEF:
                out.setDeleteExternalEventDef(deleteExternalEventDef.toProto());
                break;
            case DELETE_TASK_DEF:
                out.setDeleteTaskDef(deleteTaskDef.toProto());
                break;
            case DELETE_WF_SPEC:
                out.setDeleteWfSpec(deleteWfSpec.toProto());
                break;
            case PUT_USER_TASK_DEF:
                out.setPutUserTaskDef(putUserTaskDefRequest.toProto());
                break;
            case DELETE_USER_TASK_DEF:
                out.setDeleteUserTaskDef(deleteUserTaskDef.toProto());
                break;
            case PUT_TENANT:
                out.setPutTenant(putTenant.toProto());
                break;
            case METADATACOMMAND_NOT_SET:
                log.warn("Metadata command was empty! Will throw LHSerdeError in future.");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        MetadataCommand p = (MetadataCommand) proto;
        time = LHUtil.fromProtoTs(p.getTime());
        tenantId = p.getTenantId();

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }

        type = p.getMetadataCommandCase();
        switch (type) {
            case PUT_WF_SPEC:
                putWfSpecRequest = PutWfSpecRequestModel.fromProto(p.getPutWfSpec());
                break;
            case PUT_TASK_DEF:
                putTaskDefRequest = PutTaskDefRequestModel.fromProto(p.getPutTaskDef());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                putExternalEventDefRequest = PutExternalEventDefRequestModel.fromProto(p.getPutExternalEventDef());
                break;
            case DELETE_EXTERNAL_EVENT_DEF:
                deleteExternalEventDef = DeleteExternalEventDefRequestModel.fromProto(p.getDeleteExternalEventDef());
                break;
            case DELETE_TASK_DEF:
                deleteTaskDef = DeleteTaskDefRequestModel.fromProto(p.getDeleteTaskDef());
                break;
            case DELETE_WF_SPEC:
                deleteWfSpec = DeleteWfSpecRequestModel.fromProto(p.getDeleteWfSpec());
                break;
            case PUT_USER_TASK_DEF:
                putUserTaskDefRequest =
                        LHSerializable.fromProto(p.getPutUserTaskDef(), PutUserTaskDefRequestModel.class);
                break;
            case DELETE_USER_TASK_DEF:
                deleteUserTaskDef =
                        LHSerializable.fromProto(p.getDeleteUserTaskDef(), DeleteUserTaskDefRequestModel.class);
                break;
            case PUT_TENANT:
                putTenant = LHSerializable.fromProto(p.getPutTenant(), PutTenantRequestModel.class);
                break;
            case METADATACOMMAND_NOT_SET:
                log.warn("Metadata command was empty! Will throw LHSerdeError in future.");
        }
    }

    @Override
    public MetadataSubCommand<?> getSubCommand() {
        switch (type) {
            case PUT_WF_SPEC:
                return putWfSpecRequest;
            case PUT_TASK_DEF:
                return putTaskDefRequest;
            case PUT_EXTERNAL_EVENT_DEF:
                return putExternalEventDefRequest;
            case DELETE_EXTERNAL_EVENT_DEF:
                return deleteExternalEventDef;
            case DELETE_TASK_DEF:
                return deleteTaskDef;
            case DELETE_WF_SPEC:
                return deleteWfSpec;
            case PUT_USER_TASK_DEF:
                return putUserTaskDefRequest;
            case DELETE_USER_TASK_DEF:
                return deleteUserTaskDef;
            case PUT_PRINCIPAL:
                return putPrincipal;
            case DELETE_PRINCIPAL:
                return deletePrincipal;
            case PUT_TENANT:
                return putTenant;
            case METADATACOMMAND_NOT_SET:
        }
        throw new IllegalStateException("Not possible to have missing subcommand.");
    }

    public void setSubCommand(MetadataSubCommand<?> cmd) {
        Class<?> cls = cmd.getClass();
        if (cls.equals(PutTaskDefRequestModel.class)) {
            type = MetadataCommandCase.PUT_TASK_DEF;
            putTaskDefRequest = (PutTaskDefRequestModel) cmd;
        } else if (cls.equals(PutExternalEventDefRequestModel.class)) {
            type = MetadataCommandCase.PUT_EXTERNAL_EVENT_DEF;
            putExternalEventDefRequest = (PutExternalEventDefRequestModel) cmd;
        } else if (cls.equals(PutUserTaskDefRequestModel.class)) {
            type = MetadataCommandCase.PUT_USER_TASK_DEF;
            putUserTaskDefRequest = (PutUserTaskDefRequestModel) cmd;
        } else if (cls.equals(PutWfSpecRequestModel.class)) {
            type = MetadataCommandCase.PUT_WF_SPEC;
            putWfSpecRequest = (PutWfSpecRequestModel) cmd;
        } else if (cls.equals(DeleteExternalEventDefRequestModel.class)) {
            type = MetadataCommandCase.DELETE_EXTERNAL_EVENT_DEF;
            deleteExternalEventDef = (DeleteExternalEventDefRequestModel) cmd;
        } else if (cls.equals(DeleteTaskDefRequestModel.class)) {
            type = MetadataCommandCase.DELETE_TASK_DEF;
            deleteTaskDef = (DeleteTaskDefRequestModel) cmd;
        } else if (cls.equals(DeleteWfSpecRequestModel.class)) {
            type = MetadataCommandCase.DELETE_WF_SPEC;
            deleteWfSpec = (DeleteWfSpecRequestModel) cmd;
        } else if (cls.equals(DeleteUserTaskDefRequestModel.class)) {
            type = MetadataCommandCase.DELETE_USER_TASK_DEF;
            deleteUserTaskDef = (DeleteUserTaskDefRequestModel) cmd;
        } else if (cls.equals(PutPrincipalRequestModel.class)) {
            type = MetadataCommandCase.PUT_PRINCIPAL;
            putPrincipal = (PutPrincipalRequestModel) cmd;
        } else if (cls.equals(DeletePrincipalRequestModel.class)) {
            type = MetadataCommandCase.DELETE_PRINCIPAL;
            deletePrincipal = (DeletePrincipalRequestModel) cmd;
        } else if (cls.equals(PutTenantRequestModel.class)) {
            type = MetadataCommandCase.PUT_TENANT;
            putTenant = (PutTenantRequestModel) cmd;
        } else {
            throw new IllegalArgumentException("Unrecognized SubCommand class: " + cls.getName());
        }
    }

    public boolean hasResponse() {
        return getSubCommand().hasResponse();
    }

    public Message process(MetadataProcessorDAO dao, LHServerConfig config) {
        return getSubCommand().process(dao, config);
    }

    @Override
    public String getTopic(LHServerConfig config) {
        return config.getMetadataCmdTopicName();
    }

    @Override
    public LHStoreType getStore() {
        return LHStoreType.METADATA;
    }

    @Override
    public String getPartitionKey() {
        return "partition-key-not-needed-only-one-partition";
    }
}
