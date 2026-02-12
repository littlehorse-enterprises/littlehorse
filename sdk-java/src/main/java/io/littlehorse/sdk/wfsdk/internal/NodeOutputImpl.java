package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.sdk.common.proto.Operation;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

class NodeOutputImpl implements NodeOutput {

    public String nodeName;
    public WorkflowThreadImpl parent;

    @Getter
    @Setter
    private String jsonPath;

    @Getter
    private List<Selector> lhPath;

    public NodeOutputImpl(String nodeName, WorkflowThreadImpl parent) {
        this.nodeName = nodeName;
        this.parent = parent;
        this.lhPath = new ArrayList<>();
    }

    public NodeOutputImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same node!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.setJsonPath(path);
        return out;
    }

    public NodeOutputImpl get(String index) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonPath() and get() on same var!");
        }
        NodeOutputImpl out = new NodeOutputImpl(nodeName, parent);
        out.getLhPath().add(Selector.newBuilder().setKey(index).build());
        return out;
    }

    @Override
    public LHExpression add(Serializable other) {
        return new LHExpressionImpl(this, Operation.ADD, other);
    }

    @Override
    public LHExpression subtract(Serializable other) {
        return new LHExpressionImpl(this, Operation.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(Serializable other) {
        return new LHExpressionImpl(this, Operation.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(Serializable other) {
        return new LHExpressionImpl(this, Operation.DIVIDE, other);
    }

    @Override
    public LHExpression extend(Serializable other) {
        return new LHExpressionImpl(this, Operation.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(Serializable other) {
        return new LHExpressionImpl(this, Operation.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, Operation.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(Serializable key) {
        return new LHExpressionImpl(this, Operation.REMOVE_KEY, key);
    }

    @Override
    public LHExpression castTo(VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }

    @Override
    public LHExpression or(LHExpression elseExpr) {
        return null;
    }

    @Override
    public LHExpression isLessThan(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isLessThanEq(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isGreaterThanEq(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isGreaterThan(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isEqualTo(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isNotEqualTo(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression doesContain(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression doesNotContain(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isIn(Serializable rhs) {
        return null;
    }

    @Override
    public LHExpression isNotIn(Serializable rhs) {
        return null;
    }
}
