package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.proto.ComparatorPb;
import io.littlehorse.sdk.common.proto.EdgeConditionPb;
import io.littlehorse.sdk.wfsdk.WorkflowCondition;

public class WorkflowConditionImpl implements WorkflowCondition {

    private EdgeConditionPb spec;

    public WorkflowConditionImpl(EdgeConditionPb spec) {
        this.spec = spec;
    }

    public EdgeConditionPb getSpec() {
        return spec;
    }

    public EdgeConditionPb getReverse() {
        EdgeConditionPb.Builder out = EdgeConditionPb.newBuilder();
        out.setRight(spec.getRight());
        out.setLeft(spec.getLeft());
        switch (spec.getComparator()) {
            case LESS_THAN:
                out.setComparator(ComparatorPb.GREATER_THAN_EQ);
                break;
            case GREATER_THAN:
                out.setComparator(ComparatorPb.LESS_THAN_EQ);
                break;
            case LESS_THAN_EQ:
                out.setComparator(ComparatorPb.GREATER_THAN);
                break;
            case GREATER_THAN_EQ:
                out.setComparator(ComparatorPb.LESS_THAN);
                break;
            case IN:
                out.setComparator(ComparatorPb.NOT_IN);
                break;
            case NOT_IN:
                out.setComparator(ComparatorPb.IN);
                break;
            case EQUALS:
                out.setComparator(ComparatorPb.NOT_EQUALS);
                break;
            case NOT_EQUALS:
                out.setComparator(ComparatorPb.EQUALS);
                break;
            case UNRECOGNIZED:
                throw new RuntimeException("Not possible");
        }
        return out.build();
    }
}
