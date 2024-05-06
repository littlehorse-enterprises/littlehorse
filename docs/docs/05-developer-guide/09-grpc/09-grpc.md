# Using the LittleHorse API

The public API exposed by the LittleHorse Server is a GRPC server, defined [here](https://github.com/littlehorse-enterprises/littlehorse/blob/0.7.0/schemas/service.proto#L25). We chose GRPC for multiple reasons, including:

* Well-defined compatibility and schema evolution rules.
* Efficient serialization with Protocol Buffers.
* Automatic client generation in multiple languages, guaranteeing strongly-typed calls.
* Support for bi-directional streaming, which is used extensively by the Task Workers.

The entirety of the LittleHorse functionality is exposed through our GRPC service. Our [`WfSpec` SDK's](/docs/developer-guide/wfspec-development/) are simply convenience utilities which facilitate the creation of a `PutWfSpecRequest`, and our [Task Worker SDK's](/docs/developer-guide/task-worker-development) utilize the grpc protocol to execute a provided function. These two client implementations are useful because they _drastically_ simplify the process of interacting with the LittleHorse API.

However, some actions, such as running a workflow, can be served perfectly well with the raw grpc client. The docs in this section show you how to use the LittleHorse GRPC API in order to perform common tasks, such as:

* Managing metadata
* Running workflows
* Posting `ExternalEvent`s
* Interacting with User Tasks
* Searching for data
* Using LittleHorse `WfRun`'s as a data store for business entities
