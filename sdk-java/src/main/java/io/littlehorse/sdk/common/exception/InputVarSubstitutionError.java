package io.littlehorse.jlib.common.exception;

public class InputVarSubstitutionError extends Exception {

    private Exception parent;
    private String message;

    public Exception getParent() {
        return parent;
    }

    public void setParent(Exception parent) {
        this.parent = parent;
    }

    public String getMessage() {
        if (parent != null) {
            return message + ": " + parent.getMessage();
        } else {
            return message;
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InputVarSubstitutionError(String message, Exception parent) {
        this.message = message;
        this.parent = parent;
    }
}
