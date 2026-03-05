package io.littlehorse.sdk.worker.adapter;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;

/**
 * Adapter base for Java types represented as LittleHorse {@link VariableType#WF_RUN_ID} values.
 *
 * @param <T> the custom Java type handled by this adapter
 */
public abstract class LHWfRunIdAdapter<T> implements LHTypeAdapter<T> {

    /**
     * Converts a Java value into a protobuf {@link WfRunId}.
     *
     * @param src input Java value
     * @return serialized workflow run id value
     */
    public abstract WfRunId toWfRunId(T src);

    /**
     * Converts a LittleHorse workflow run id into the target Java type.
     *
     * @param src runtime workflow run id value
     * @return deserialized Java value
     */
    public abstract T fromWfRunId(WfRunId src);

    /**
     * Returns the LittleHorse runtime type for this adapter.
     *
     * @return {@link VariableType#WF_RUN_ID}
     */
    public VariableType getVariableType() {
        return VariableType.WF_RUN_ID;
    }
}
