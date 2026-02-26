package io.littlehorse.common.exceptions;

/**
 * Checked exception thrown when a referenced StructDef does not exist.
 * Callers must catch this and translate it into an appropriate API or validation exception.
 */
public class UnknownStructDefException extends LHException {

    public UnknownStructDefException(String structDefName) {
        super(null, "Refers to non-existent StructDef %s".formatted(structDefName));
    }
}
