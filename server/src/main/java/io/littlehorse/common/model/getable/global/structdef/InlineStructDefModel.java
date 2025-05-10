package io.littlehorse.common.model.getable.global.structdef;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InlineStructDefModel extends LHSerializable<InlineStructDef> {

    private Map<String, StructFieldDefModel> fields = new HashMap<>();

    @Override
    public InlineStructDef.Builder toProto() {
        InlineStructDef.Builder out = InlineStructDef.newBuilder();

        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            out.putFields(field.getKey(), field.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        InlineStructDef proto = (InlineStructDef) p;

        for (Entry<String, StructFieldDef> structFieldDef : proto.getFieldsMap().entrySet()) {
            fields.put(
                    structFieldDef.getKey(),
                    LHSerializable.fromProto(structFieldDef.getValue(), StructFieldDefModel.class, context));
        }
    }

    @Override
    public Class<InlineStructDef> getProtoBaseClass() {
        return InlineStructDef.class;
    }

    public void validate() {
        for (Entry<String, StructFieldDefModel> field : fields.entrySet()) {
            if (!LHUtil.isValidLHName(field.getKey())) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        MessageFormat.format("StructField name [{0}] must be a valid hostname", field.getKey()));
            }
        }
    }
}
