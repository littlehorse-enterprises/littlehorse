package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.node.EdgeModel;

/**
 * Exception thrown when an edge in a workflow specification is invalid.
 * Provides details about the invalid edge and the reason for failure.
 */
public class InvalidEdgeException extends LHValidationException {

    public InvalidEdgeException(String message, EdgeModel edge) {
        super("Edge with sink node " + edge.getSinkNodeName() + " invalid: " + message);
    }

    public InvalidEdgeException(LHValidationException cause, EdgeModel edge) {
        super("Edge with sink node " + edge.getSinkNodeName() + " invalid: " + cause.getMessage());
    }
}
