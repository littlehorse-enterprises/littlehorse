package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;

/**
 * Exception thrown when an edge in a workflow specification is invalid.
 * Provides details about the invalid edge and the reason for failure.
 */
public class InvalidEdgeException extends LHValidationException {

    public InvalidEdgeException(String message, String sinkNodeName) {
        super("Edge with sink node " + sinkNodeName + " invalid: " + message);
    }

    public InvalidEdgeException(LHValidationException cause, String sinkNodeName) {
        super("Edge with sink node " + sinkNodeName + " invalid: " + cause.getMessage());
    }
}
