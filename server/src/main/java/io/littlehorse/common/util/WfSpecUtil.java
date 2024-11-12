package io.littlehorse.common.util;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.sdk.common.proto.WfSpecId;

import java.util.*;

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
     * - set of left searchable variables does not match right searchable variables
     * @param left WfSpecModel to be compared
     * @param right WfSpecModel compared against
     * @return true when there is a breaking change, false otherwise
     */
    public static boolean hasBreakingChanges(WfSpecModel left, WfSpecModel right) {
        return !variablesMatch(left.getRequiredVariables(), right.getRequiredVariables())
                || !variablesMatch(left.getSearchableVariables(), right.getSearchableVariables())
                || !variableAccessLevelMatch(
                        left.getEntrypointThread().getVariableDefs(),
                        right.getEntrypointThread().getVariableDefs());
    }

    private static boolean variablesMatch(Map<String, ThreadVarDefModel> left, Map<String, ThreadVarDefModel> right) {
        Set<String> leftVariables = left.keySet();
        Set<String> rightVariables = right.keySet();
        if (leftVariables.size() != rightVariables.size()) return false;

        return leftVariables.containsAll(rightVariables);
    }

    private static boolean variableAccessLevelMatch(List<ThreadVarDefModel> left, List<ThreadVarDefModel> right) {
        final Map<String, WfRunVariableAccessLevel> leftVariables = new HashMap<>();
        for (ThreadVarDefModel leftVarDefinition : left) {
            leftVariables.put(leftVarDefinition.getVarDef().getName(), leftVarDefinition.getAccessLevel());
        }
        for (ThreadVarDefModel rightVarDefinition : right) {
            WfRunVariableAccessLevel rightAccessLevel = rightVarDefinition.getAccessLevel();
            if (rightAccessLevel == WfRunVariableAccessLevel.PUBLIC_VAR
                    || rightAccessLevel == WfRunVariableAccessLevel.INHERITED_VAR) {
                WfRunVariableAccessLevel leftAccessLevel =
                        leftVariables.get(rightVarDefinition.getVarDef().getName());
                if (leftAccessLevel != null && leftAccessLevel.equals(WfRunVariableAccessLevel.PRIVATE_VAR)) {
                    return false;
                }
            }
        }
        return true;
    }
}
