# LH Kernel: Thread-per-Request Model

The architecture of the [LittleHorse kernel](../server/README.md) deploys one or more gRPC server listeners to handle both queries and commands.

The query model is simple: A listener accesses a read-only store, executes the query over the key-value store, and sends the response back to the gRPC client.

Commands, on the other hand, are more complex. Processing a command involves sending a record to the core Kafka Streams topology and awaiting a response. The `AsyncWaiters` class handles the registration of gRPC `StreamObserver` instances, which are used to send responses back to the client. Both the `CommandProcessor` and `MetadataProcessor` instances interact with the `AsyncWaiters` class, using its `StreamObserver` to deliver processed command responses to clients.

The throughput of a gRPC listener is primarily influenced by the number of threads available to handle incoming requests. Each request occupies a thread while it is being executed. In the case of commands, the thread is released once the command is dispatched to the Kafka topic.

The current architecture has some notable limitations:

- **Network Thread Limitations**: The throughput of each gRPC listener is restricted by the number of available network threads. Operators often find it challenging to determine the optimal thread count.
- **Blocking Network Calls**: Network calls made within the request execution context are blocking, which negatively impacts performance.
- **Unclear Thread Model**: The thread modeling in the architecture is complex and relies on certain workarounds to prevent blocking the current thread.
- **Request Timeout Management**: Request timeouts are managed by an internal thread created through the `AsyncWaiters` class, adding complexity to the architecture.