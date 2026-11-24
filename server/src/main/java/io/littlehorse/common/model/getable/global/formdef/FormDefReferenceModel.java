package io.littlehorse.common.model.getable.global.formdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.FormDefReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class FormDefReferenceModel extends LHSerializable<FormDefReference> {

    private String name;

    // Null means the latest version when each UserTaskRun is created.
    private Integer version;

    @Override
    public FormDefReference.Builder toProto() {
        FormDefReference.Builder out = FormDefReference.newBuilder().setName(name);
        if (version != null) out.setVersion(version);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        FormDefReference p = (FormDefReference) proto;
        name = p.getName();
        version = p.hasVersion() ? p.getVersion() : null;
    }

    @Override
    public Class<FormDefReference> getProtoBaseClass() {
        return FormDefReference.class;
    }
}
