package io.littlehorse.server.streams.topology.core;

import io.littlehorse.common.AuthorizationContext;
import io.littlehorse.server.streams.store.ModelStore;
import io.littlehorse.server.streams.store.ReadOnlyModelStore;
import io.littlehorse.server.streams.storeinternals.ReadOnlyGetableManager;
import io.littlehorse.server.streams.util.MetadataCache;

/**
 * This interface is intended to be used as a context propagator from
 * Processors to Subcommands. Provides a set of method to build dependencies
 * for the sub commands
 * Each Record will have its instance for this class
 */
public interface ExecutionContext {
    default <T extends ExecutionContext> T castOnSupport(Class<T> clazz){
        if(clazz.isAssignableFrom(this.getClass())){
            return (T) this;
        }else {
            throw new IllegalArgumentException("Not supported");
        }
    }

    AuthorizationContext authorization();

    default void endExecution() {}

    // We should remove this direct access to the store in the future,
    // Every interaction with the store should be via StorageManager
    ReadOnlyModelStore store();

    // Utilities
    WfService service();

}
