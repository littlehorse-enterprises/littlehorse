package io.littlehorse.common.model.metadatacommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeletePrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteStructDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.DeleteWorkflowEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutExternalEventDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutPrincipalRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutStructDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutUserTaskDefRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWorkflowEventDefRequestModel;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.common.proto.MetadataCommand.MetadataCommandCase;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class MetadataCommandModel extends AbstractCommand<MetadataCommand> {

    private Date time;

    private MetadataCommandCase type;
    private PutWfSpecRequestModel putWfSpecRequest;
    private PutTaskDefRequestModel putTaskDefRequest;
    private PutStructDefRequestModel putStructDefRequest;
    private PutExternalEventDefRequestModel putExternalEventDefRequest;
    private DeleteWfSpecRequestModel deleteWfSpec;
    private DeleteTaskDefRequestModel deleteTaskDef;
    private DeleteExternalEventDefRequestModel deleteExternalEventDef;
    private DeleteWorkflowEventDefRequestModel deleteWorkflowEventDefRequest;
    private PutUserTaskDefRequestModel putUserTaskDefRequest;
    private DeleteUserTaskDefRequestModel deleteUserTaskDef;
    private PutPrincipalRequestModel putPrincipal;
    private DeletePrincipalRequestModel deletePrincipal;
    private PutTenantRequestModel putTenant;
    private PutWorkflowEventDefRequestModel putWorkflowEventDef;
    private DeleteStructDefRequestModel deleteStructDef;

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
            case PUT_STRUCT_DEF:
                out.setPutStructDef(putStructDefRequest.toProto());
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                out.setPutExternalEventDef(putExternalEventDefRequest.toProto());
                break;
            case DELETE_EXTERNAL_EVENT_DEF:
                out.setDeleteExternalEventDef(deleteExternalEventDef.toProto());
                break;
            case DELETE_WORKFLOW_EVENT_DEF:
                out.setDeleteWorkflowEventDef(deleteWorkflowEventDefRequest.toProto());
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
            case PUT_PRINCIPAL:
                out.setPutPrincipal(putPrincipal.toProto());
                break;
            case DELETE_PRINCIPAL:
                out.setDeletePrincipal(deletePrincipal.toProto());
                break;
            case WORKFLOW_EVENT_DEF:
                out.setWorkflowEventDef(putWorkflowEventDef.toProto());
                break;
            case DELETE_STRUCT_DEF:
                out.setDeleteStructDef(deleteStructDef.toProto());
                break;
            case METADATACOMMAND_NOT_SET:
                log.warn("Metadata command was empty! Will throw LHSerdeError in future.");
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetadataCommand p = (MetadataCommand) proto;
        time = LHUtil.fromProtoTs(p.getTime());

        if (p.hasCommandId()) {
            commandId = p.getCommandId();
        }

        type = p.getMetadataCommandCase();
        switch (type) {
            case PUT_WF_SPEC:
                putWfSpecRequest = PutWfSpecRequestModel.fromProto(p.getPutWfSpec(), context);
                break;
            case PUT_TASK_DEF:
                putTaskDefRequest = PutTaskDefRequestModel.fromProto(p.getPutTaskDef(), context);
                break;
            case PUT_STRUCT_DEF:
                putStructDefRequest = PutStructDefRequestModel.fromProto(p.getPutStructDef(), context);
                break;
            case PUT_EXTERNAL_EVENT_DEF:
                putExternalEventDefRequest =
                        PutExternalEventDefRequestModel.fromProto(p.getPutExternalEventDef(), context);
                break;
            case DELETE_EXTERNAL_EVENT_DEF:
                deleteExternalEventDef =
                        DeleteExternalEventDefRequestModel.fromProto(p.getDeleteExternalEventDef(), context);
                break;
            case DELETE_WORKFLOW_EVENT_DEF:
                deleteWorkflowEventDefRequest =
                        DeleteWorkflowEventDefRequestModel.fromProto(p.getDeleteWorkflowEventDef(), context);
                break;
            case DELETE_TASK_DEF:
                deleteTaskDef = DeleteTaskDefRequestModel.fromProto(p.getDeleteTaskDef(), context);
                break;
            case DELETE_WF_SPEC:
                deleteWfSpec = DeleteWfSpecRequestModel.fromProto(p.getDeleteWfSpec(), context);
                break;
            case PUT_USER_TASK_DEF:
                putUserTaskDefRequest =
                        LHSerializable.fromProto(p.getPutUserTaskDef(), PutUserTaskDefRequestModel.class, context);
                break;
            case DELETE_USER_TASK_DEF:
                deleteUserTaskDef = LHSerializable.fromProto(
                        p.getDeleteUserTaskDef(), DeleteUserTaskDefRequestModel.class, context);
                break;
            case PUT_TENANT:
                putTenant = LHSerializable.fromProto(p.getPutTenant(), PutTenantRequestModel.class, context);
                break;
            case PUT_PRINCIPAL:
                putPrincipal = LHSerializable.fromProto(p.getPutPrincipal(), PutPrincipalRequestModel.class, context);
                break;
            case DELETE_PRINCIPAL:
                deletePrincipal =
                        LHSerializable.fromProto(p.getDeletePrincipal(), DeletePrincipalRequestModel.class, context);
                break;
            case WORKFLOW_EVENT_DEF:
                putWorkflowEventDef = LHSerializable.fromProto(
                        p.getWorkflowEventDef(), PutWorkflowEventDefRequestModel.class, context);
                break;
            case DELETE_STRUCT_DEF:
                deleteStructDef = LHSerializable.fromProto(
                    p.getDeleteStructDef(), DeleteStructDefRequestModel.class, context);
                break;
            case METADATACOMMAND_NOT_SET:
                log.warn("Metadata command was empty! Will throw LHSerdeError in future.");
        }
    }

    public MetadataSubCommand<?> getSubCommand() {
        switch (type) {
            case PUT_WF_SPEC:
                return putWfSpecRequest;
            case PUT_TASK_DEF:
                return putTaskDefRequest;
            case PUT_STRUCT_DEF:
                return putStructDefRequest;
            case PUT_EXTERNAL_EVENT_DEF:
                return putExternalEventDefRequest;
            case DELETE_EXTERNAL_EVENT_DEF:
                return deleteExternalEventDef;
            case DELETE_WORKFLOW_EVENT_DEF:
                return deleteWorkflowEventDefRequest;
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
            case WORKFLOW_EVENT_DEF:
                return putWorkflowEventDef;
            case DELETE_STRUCT_DEF:
                return deleteStructDef;
            case METADATACOMMAND_NOT_SET:
        }
        throw new IllegalStateException("Not possible to have missing subcommand.");
    }

    public void setSubCommand(MetadataSubCommand<?> cmd) {
        Class<?> cls = cmd.getClass();
        if (cls.equals(PutTaskDefRequestModel.class)) {
            type = MetadataCommandCase.PUT_TASK_DEF;
            putTaskDefRequest = (PutTaskDefRequestModel) cmd;
        } else if (cls.equals(PutStructDefRequestModel.class)) {
            type = MetadataCommandCase.PUT_STRUCT_DEF;
            putStructDefRequest = (PutStructDefRequestModel) cmd;
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
        } else if (cls.equals(DeleteWorkflowEventDefRequestModel.class)) {
            type = MetadataCommandCase.DELETE_WORKFLOW_EVENT_DEF;
            deleteWorkflowEventDefRequest = (DeleteWorkflowEventDefRequestModel) cmd;
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
        } else if (cls.equals(PutWorkflowEventDefRequestModel.class)) {
            type = MetadataCommandCase.WORKFLOW_EVENT_DEF;
            putWorkflowEventDef = (PutWorkflowEventDefRequestModel) cmd;
        } else if (cls.equals(DeleteStructDefRequestModel.class)) {
            type = MetadataCommandCase.DELETE_STRUCT_DEF;
            deleteStructDef = (DeleteStructDefRequestModel) cmd;
        } else {
            throw new IllegalArgumentException("Unrecognized SubCommand class: " + cls.getName());
        }
    }

    public boolean hasResponse() {
        return getCommandId().isPresent();
    }

    public Message process(MetadataProcessorContext context) {
        return getSubCommand().process(context);
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
