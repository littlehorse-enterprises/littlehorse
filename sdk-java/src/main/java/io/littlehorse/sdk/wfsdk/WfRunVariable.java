package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.IndexTypePb;
import lombok.NonNull;

/**
 * A WfRunVariable is a handle on a Variable in a WfSpec.
 */
public interface WfRunVariable {
    /**
     * Valid only for output of the JSON_OBJ or JSON_ARR types. Returns
     * a new NodeOutput handle which points to Json element referred to
     * by the json path.
     *
     * Can only be called once--you can't call node.jsonPath().jsonPath().
     * @param path is the json path to evaluate.
     * @return a NodeOutput.
     */
    WfRunVariable jsonPath(String path);

    /**
     * Enables the storage of variables with a Non-null {@link IndexTypePb}.
     * For enhanced efficiency, it offers two types of indexing:
     * Remote Index and Local Index.
     * {@link IndexTypePb#REMOTE_INDEX Remote}: This type of indexing is
     * recommended for variables with low cardinality, which means
     * they have relatively few distinct values. For example,
     * storing userId.
     * {@link IndexTypePb#LOCAL_INDEX Local}: Local Index is designed for
     * variables with high cardinality.
     *
     * @param indexType Defines Local or Remote Index
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable withIndex(@NonNull IndexTypePb indexType);

    /**
     * Enables the storage of specific attributes inside a Json Variable.
     * For enhanced efficiency, it offers two types of indexing:
     * Remote Index and Local Index.
     * {@link IndexTypePb#REMOTE_INDEX Remote}: This type of indexing is
     * recommended for variables with low cardinality, which means
     * they have relatively few distinct values. For example,
     * storing userId.
     * {@link IndexTypePb#LOCAL_INDEX Local}: Local Index is designed for
     * variables with high cardinality.
     *
     * @param indexType Defines Local or Remote Index
     * @param jsonPath Json Attribute path starting with $. e.g: $.userId
     * @return same {@link WfRunVariable} instance
     * @throws io.littlehorse.sdk.common.exception.LHMisconfigurationException if jsonPath
     * doesn't start with $. or adding a jsonIndex to a non-json variable
     */
    WfRunVariable withJsonIndex(
        @NonNull String jsonPath,
        @NonNull IndexTypePb indexType
    );
}
