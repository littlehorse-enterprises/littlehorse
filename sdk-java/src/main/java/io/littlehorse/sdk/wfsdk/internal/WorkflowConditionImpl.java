package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LegacyEdgeCondition;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;

class WorkflowConditionImpl implements WorkflowCondition {

    private LegacyEdgeCondition spec;

    public WorkflowConditionImpl(LegacyEdgeCondition spec) {
        this.spec = spec;
    }

    public LegacyEdgeCondition getSpec() {
        return spec;
    }

    public LegacyEdgeCondition getReverse() {
        LegacyEdgeCondition.Builder out = LegacyEdgeCondition.newBuilder();
        out.setRight(spec.getRight());
        out.setLeft(spec.getLeft());
        switch (spec.getComparator()) {
            case LESS_THAN:
                out.setComparator(Comparator.GREATER_THAN_EQ);
                break;
            case GREATER_THAN:
                out.setComparator(Comparator.LESS_THAN_EQ);
                break;
            case LESS_THAN_EQ:
                out.setComparator(Comparator.GREATER_THAN);
                break;
            case GREATER_THAN_EQ:
                out.setComparator(Comparator.LESS_THAN);
                break;
            case IN:
                out.setComparator(Comparator.NOT_IN);
                break;
            case NOT_IN:
                out.setComparator(Comparator.IN);
                break;
            case EQUALS:
                out.setComparator(Comparator.NOT_EQUALS);
                break;
            case NOT_EQUALS:
                out.setComparator(Comparator.EQUALS);
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }
        return out.build();
    }
}
