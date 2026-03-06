package io.littlehorse.sdk.wfsdk;

public interface InterruptHandler {

    /**
     * Registers an ExternalEventDef for this interrupt handler with the given payload type.
     *
     * <p>Allowed classes are String, Integer, Double, and Boolean. If {@code eventType} is
     * {@code null}, then the ExternalEventDef will have a null payload.
     *
     * @param eventType the class of the interrupt event payload.
     */
    void withEventType(Class<?> eventType);
}
