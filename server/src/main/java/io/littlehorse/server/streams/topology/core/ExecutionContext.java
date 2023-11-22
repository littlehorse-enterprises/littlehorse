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
        if(support(clazz)){
            return (T) this;
        }else {
            return null;
        }
    }

    default <T extends  ExecutionContext> boolean support(Class<T> clazz){
        return clazz.isAssignableFrom(this.getClass());
    }

    AuthorizationContext authorization();

    default void endExecution() {}

    // Utilities
    WfService service();

}
