package io.littlehorse.common.exceptions;

public class LHVarSubError extends LHException {

    /**
     * Creates a new LHVarSubError with the given source and prefix.
     * The message of the new error is constructed by concatenating the prefix, a colon, and the message of the source error.
     *
     * @param source the source LHVarSubError to create a new varSub error from
     * @param prefix the prefix to prepend to the message of the source error
     */
    public LHVarSubError(LHVarSubError source, String prefix) {
        super(source, prefix + ": " + source.getMessage());
    }

    public LHVarSubError(Exception exn, String msg) {
        super(exn, msg);
    }
}
