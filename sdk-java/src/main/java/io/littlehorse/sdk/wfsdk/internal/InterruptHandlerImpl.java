package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.wfsdk.InterruptHandler;

public class InterruptHandlerImpl implements InterruptHandler {

    private final String interruptName;
    private final WorkflowThreadImpl parentThread;
    private boolean eventTypeRegistered = false;

    public InterruptHandlerImpl(String interruptName, WorkflowThreadImpl parentThread) {
        this.interruptName = interruptName;
        this.parentThread = parentThread;
    }

    @Override
    public void withEventType(Class<?> eventType) {
        if (eventTypeRegistered) {
            throw new LHMisconfigurationException("Interrupt event type already registered: " + interruptName);
        }
        eventTypeRegistered = true;
        parentThread.registerExternalEventDef(new InterruptExternalEventDefRegistration(interruptName, eventType));
    }
}
