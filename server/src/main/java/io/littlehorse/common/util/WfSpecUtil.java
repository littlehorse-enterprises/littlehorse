package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public class WfSpecUtil {
    private WfSpecUtil() {}

    /**
     * This method returns true when two WfSpecModels are equal
     * excluding `majorVersion`, `revision` and `createdAt`
     * @param left WfSpecModel to be compared
     * @param right WfSpecModel compared against
     * @return true when it is equal, false otherwise.
     */
    public static boolean equals(WfSpecModel left, WfSpecModel right) {
        WfSpec.Builder copy = left.toProto();
        Timestamp date = LHUtil.fromDate(new Date());
        sanitize(copy, date);
        WfSpec.Builder toCopy = right.toProto();
        sanitize(toCopy, date);
        return Arrays.equals(copy.build().toByteArray(), toCopy.build().toByteArray());
    }

    private static void sanitize(WfSpec.Builder spec, Timestamp date) {
        spec.setId(WfSpecId.newBuilder().setName(spec.getId().getName())).setCreatedAt(date);
    }

    /**
     * Verifies if left WfSpecModel have breaking changes comparing to right
     * when either:
     * - set of left required variables does not match right required variables
     * - set of left PUBLIC_VAR variables does not match right PUBLIC_VAR variables
     * @param left WfSpecModel to be compared
     * @param right WfSpecModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    public static boolean hasBreakingChanges(WfSpecModel left, WfSpecModel right, ReadOnlyMetadataManager manager) {
        Collection<ThreadVarDefModel> leftPublicVars = left.getPublicVars();
        Collection<ThreadVarDefModel> rightPublicVars = right.getPublicVars();

        if (leftPublicVars.size() != rightPublicVars.size()) return true;
        if (!leftPublicVars.containsAll(rightPublicVars)) return true;

        // Check required variables
        Collection<ThreadVarDefModel> rightRequiredVars =
                right.getEntrypointThread().getRequiredVarDefs();
        Collection<ThreadVarDefModel> leftRequiredVars =
                left.getEntrypointThread().getRequiredVarDefs();

        if (leftRequiredVars.size() != rightRequiredVars.size() || !leftRequiredVars.containsAll(rightRequiredVars)) {
            return true;
        }

        // check output type
        Optional<ReturnTypeModel> firstOutputType = left.getOutputType(manager);
        Optional<ReturnTypeModel> secondOutputType = right.getOutputType(manager);
        if (firstOutputType.isPresent() && secondOutputType.isEmpty()
                || firstOutputType.isEmpty() && secondOutputType.isPresent()) {
            return true;
        }
        if (!firstOutputType.get().equals(secondOutputType.get())) {
            return true;
        }

        return false;
    }
}
