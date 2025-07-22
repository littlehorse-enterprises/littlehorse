package io.littlehorse.common.exceptions.validation;

import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.getable.global.wfspec.node.EdgeModel;

public class InvalidEdgeException extends LHValidationException {

    public InvalidEdgeException(String message, EdgeModel edge) {
        super("Edge with sink node " + edge.getSinkNodeName() + " invalid: " + message);
    }

    public InvalidEdgeException(LHValidationException cause, EdgeModel edge) {
        super("Edge with sink node " + edge.getSinkNodeName() + " invalid: " + cause.getMessage());
    }
}
