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
- **Async Waiters and StreamThreads**: Async waiters are completed within the StreamThreads, which increases processing latency when Netty threads are insufficient to send responses quickly enough.

This document proposes adopting the Thread-per-request model. In this new model, each request is treated as a short-lived task, and the carrier thread is responsible for completing these tasks efficiently.

## gRPC Listeners

As of now, gRPC listeners shares the same thread pool (aka `networkThreadPool`), and we create this thread pool with a fixed number of threads:
```java
Executors.newScheduledThreadPool(config.getNumNetworkThreads());
```

I propose to change this Executor instance to a virtual threads (VT):
```java
Executors.newVirtualThreadPerTaskExecutor();
```
Meaning that we can now deprecate/remove `LHS_NUM_NETWORK_THREADS` config


## Programming Model

Every request will now run in a virtual environment where blocking operations will release the ownership of the carrier thread, allowing other virtual thread to run while the thread is waiting for a response.
So, simplify our code base by just using completable futures that other threads (e.g StreamThread) will complete. For example: