package io.littlehorse.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The InMemoryGetableManager class provides an in-memory implementation of the GetableManager.
 * This class is designed to manage and store Getable objects in an internal, thread-safe memory buffer,
 * rather than relying on Kafka Streams KeyValue stores. It is particularly useful for testing or scenarios
 * where persistence is not required.
 * In the constructor, you can use either an actual `ProcessorExecutionContext` or a mock with a valid return value for
 * `ExecutionContext#authorization`
 * Example Usage:
 * <pre>{@code
 * // Create an execution context, either real or mocked
 * ProcessorExecutionContext executionContext = new ProcessorExecutionContext(...);
 *
 * // Instantiate the InMemoryGetableManager
 * InMemoryGetableManager getableManager = new InMemoryGetableManager(executionContext);
 *
 * // Store a Getable object in the buffer
 * getableManager.put(myGetableInstance);
 *
 * // Retrieve the object later
 * WfRunIdModel result = getableManager.get(myGetableInstance.getObjectId());
 * }</pre>
 *
 * `TestProcessorExecutionContext` is an alternative option for this class where a InMemory Kafka Streams store is configured.
 */
public class InMemoryGetableManager extends GetableManager {

    private final Map<ObjectIdModel<?, ?, ?>, AbstractGetable<?>> buffer = new ConcurrentHashMap<>();

    public InMemoryGetableManager(ProcessorExecutionContext executionContext) {
        super(null, null, null, null, executionContext);
    }

    @Override
    public <U extends Message, T extends CoreGetable<U>> void put(T getable) throws IllegalStateException {
        buffer.put(getable.getObjectId(), getable);
    }

    @Override
    public <U extends Message, T extends CoreGetable<U>> T get(CoreObjectId<?, U, T> id) {
        return (T) buffer.get(id);
    }

    @Override
    public void commit() {
        // nothing to do
    }
}
