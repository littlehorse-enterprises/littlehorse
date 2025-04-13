package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.taskrun.VarNameAndValModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.TypeDefinitionModel;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class VariableDefModel extends LHSerializable<VariableDef> {

    private TypeDefinitionModel type;
    private String name;
    private VariableValueModel defaultValue;

    public Class<VariableDef> getProtoBaseClass() {
        return VariableDef.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableDef p = (VariableDef) proto;

        // Version 0.13.2 refactored the proto by:
        // - Deprecate VariableType.type and VariableType.masked
        // - Introduce the TypeDefinition, which wraps them.
        //
        // In this constructor we gracefully handle that migration in a compatible manner.
        //
        // Note that this means that servers newer than `0.13.2` don't work with *dashboards*
        // older than `0.13.2`, but clients older than `0.13.1` will continue to work. If this
        // was post-1.0, we would not modify the stored proto.
        if (p.hasType()) {
            log.debug("Detected a `VariableDef` from before 0.13.2!");
            this.type = new TypeDefinitionModel();
            this.type.setMasked(p.getMaskedValue());
            this.type.setType(p.getType());
        } else {
            // This means the proto is up-to-date, so we're all good.
            this.type = LHSerializable.fromProto(p.getTypeDef(), TypeDefinitionModel.class, context);
        }

        name = p.getName();
        if (p.hasDefaultValue()) {
            defaultValue = VariableValueModel.fromProto(p.getDefaultValue(), context);
        }
    }

    public VariableDef.Builder toProto() {
        VariableDef.Builder out =
                VariableDef.newBuilder().setTypeDef(type.toProto()).setName(name);

        if (defaultValue != null) out.setDefaultValue(defaultValue.toProto());
        return out;
    }

    public static VariableDefModel fromProto(VariableDef proto, ExecutionContext context) {
        VariableDefModel o = new VariableDefModel();
        o.initFrom(proto, context);
        return o;
    }

    public boolean isJson() {
        return type.isJson();
    }

    public boolean isMaskedValue() {
        return type.isMasked();
    }

    public void validateValue(VariableValueModel value) throws LHValidationError {
        if (value.getType() == null || value.getType() == type.getType()) {
            return;
        }
        throw new LHValidationError(
                null, "Variable " + name + " should be " + type + " but is of type " + value.getType());
    }

    public VarNameAndValModel assignValue(VariableValueModel value) throws LHVarSubError {
        try {
            validateValue(value);
        } catch (LHValidationError e) {
            throw new LHVarSubError(e, e.getMessage());
        }
        if (type.isMasked()) {
            return new VarNameAndValModel(name, value, true);
        }
        return new VarNameAndValModel(name, value, false);
    }
}
