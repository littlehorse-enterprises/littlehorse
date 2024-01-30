package io.littlehorse.canary.kafka;

public class MetricsEmitterException extends RuntimeException {
    public MetricsEmitterException() {}

    public MetricsEmitterException(String message) {
        super(message);
    }

    public MetricsEmitterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetricsEmitterException(Throwable cause) {
        super(cause);
    }
}
