package io.littlehorse.server.streams.topology.core.background;

/**
 * Wraps an {@link InterruptedException} raised while a background job was scheduling an action, so
 * that it can propagate through call sites that cannot declare a checked exception — typically
 * shared model code that takes a {@code Consumer}.
 *
 * <p>{@link PartitionActionScheduler} unwraps this and treats it as a genuine interruption, so
 * partition revocation still unwinds the worker promptly.
 */
public class UncheckedInterruptedException extends RuntimeException {

    public UncheckedInterruptedException(InterruptedException cause) {
        super(cause);
    }

    @Override
    public synchronized InterruptedException getCause() {
        return (InterruptedException) super.getCause();
    }
}
