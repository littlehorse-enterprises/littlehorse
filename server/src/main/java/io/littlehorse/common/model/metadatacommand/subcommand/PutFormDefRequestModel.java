package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.InvalidExpressionException;
import io.littlehorse.common.model.getable.global.formdef.FormDefModel;
import io.littlehorse.common.model.getable.global.formdef.FormFieldDefModel;
import io.littlehorse.common.model.getable.global.formdef.FormMetadataValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.LHPathModel;
import io.littlehorse.common.model.getable.objectId.FormDefIdModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormDef;
import io.littlehorse.sdk.common.proto.FormFieldDef;
import io.littlehorse.sdk.common.proto.FormMetadataValue;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.sdk.common.proto.PutFormDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PutFormDefRequestModel extends MetadataSubCommand<PutFormDefRequest> {

    private String name;
    private StructDefIdModel resultStructDefId;
    private List<FormFieldDefModel> fields = new ArrayList<>();
    private Map<String, FormMetadataValueModel> metadata = new HashMap<>();

    @Override
    public PutFormDefRequest.Builder toProto() {
        PutFormDefRequest.Builder out = PutFormDefRequest.newBuilder().setName(name);
        if (resultStructDefId != null) out.setResultStructDefId(resultStructDefId.toProto());
        fields.forEach(field -> out.addFields(field.toProto()));
        metadata.forEach((key, value) -> out.putMetadata(key, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        PutFormDefRequest proto = (PutFormDefRequest) p;
        name = proto.getName();
        if (proto.hasResultStructDefId()) {
            resultStructDefId = LHSerializable.fromProto(proto.getResultStructDefId(), StructDefIdModel.class, context);
        }
        for (FormFieldDef field : proto.getFieldsList()) {
            fields.add(LHSerializable.fromProto(field, FormFieldDefModel.class, context));
        }
        for (Map.Entry<String, FormMetadataValue> entry : proto.getMetadataMap().entrySet()) {
            metadata.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), FormMetadataValueModel.class, context));
        }
    }

    @Override
    public FormDef process(MetadataProcessorContext context) {
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "FormDef name must be a valid hostname");
        }
        if (resultStructDefId == null || context.metadataManager().get(resultStructDefId) == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "StructDef %s does not exist".formatted(resultStructDefId));
        }
        validateFields(context);
        validatePlaceholders(metadata);

        FormDefModel spec = new FormDefModel();
        spec.setResultStructDefId(resultStructDefId);
        spec.setFields(fields);
        spec.setMetadata(metadata);
        spec.setCreatedAt(new Date());

        FormDefModel latest = context.service().getFormDef(name, null);
        if (latest == null) {
            spec.setId(new FormDefIdModel(name, 0));
        } else if (spec.contentEquals(latest)) {
            return latest.toProto().build();
        } else {
            spec.setId(latest.getObjectId().bumpVersion());
        }

        context.metadataManager().put(spec);
        return spec.toProto().build();
    }

    private void validateFields(MetadataProcessorContext context) {
        TypeDefinitionModel resultType = new TypeDefinitionModel(resultStructDefId);
        Set<String> seenPaths = new HashSet<>();
        for (FormFieldDefModel field : fields) {
            LHPathModel pathModel = field.getFieldPath();
            List<Selector> path = pathModel.getPath();
            if (path.isEmpty()) {
                throw new LHApiException(Status.INVALID_ARGUMENT, "FormDef field path must not be empty");
            }
            for (Selector selector : path) {
                if (selector.getSelectorTypeCase() != Selector.SelectorTypeCase.KEY) {
                    throw new LHApiException(
                            Status.INVALID_ARGUMENT, "FormDef field paths may only contain key selectors");
                }
            }
            if (!seenPaths.add(pathModel.toJsonPathStr())) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT, "Duplicate FormDef field path: " + pathModel.toJsonPathStr());
            }
            try {
                resultType.getNestedType(pathModel, context.metadataManager());
            } catch (InvalidExpressionException exn) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Invalid FormDef field path %s: %s".formatted(pathModel.toJsonPathStr(), exn.getMessage()));
            }
            validatePlaceholders(field.getMetadata());
        }
    }

    private static void validatePlaceholders(Map<String, FormMetadataValueModel> values) {
        for (Map.Entry<String, FormMetadataValueModel> entry : values.entrySet()) {
            FormMetadataValueModel value = entry.getValue();
            if (value.getType() == FormMetadataValue.ValueCase.VALUE_NOT_SET
                    || (value.getType() == FormMetadataValue.ValueCase.PLACEHOLDER
                            && value.getPlaceholder().isBlank())) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "FormDef metadata '%s' must have a literal or a non-empty placeholder"
                                .formatted(entry.getKey()));
            }
        }
    }

    @Override
    public Class<PutFormDefRequest> getProtoBaseClass() {
        return PutFormDefRequest.class;
    }

    public static PutFormDefRequestModel fromProto(PutFormDefRequest p, ExecutionContext context) {
        PutFormDefRequestModel out = new PutFormDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
