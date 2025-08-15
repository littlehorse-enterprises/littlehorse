package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.node.NodeModel;

/**
 * Exception that means that there's a problem with a `Node` inside a PutWfSpecRequest that was
 * attempted by a client. Should be handled by the ThreadSpec.
 */
public class InvalidNodeException extends LHValidationException {

    public InvalidNodeException(LHValidationException cause, NodeModel node) {
        super("Node " + node.getName() + " invalid: " + cause.getMessage());
    }

    public InvalidNodeException(String message, NodeModel node) {
        super("Node " + node.getName() + " invalid: " + message);
    }
}
