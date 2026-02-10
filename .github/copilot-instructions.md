# Copilot instructions for LittleHorse

## Big picture (repo layout)
- Monorepo: server (Java/Kafka engine), dashboard (Next.js UI), SDKs (sdk-java/go/python/dotnet/js), lhctl (Go CLI), canary (synthetic monitoring), test-utils (E2E test harness), schemas (protobuf), docker (image build context). See [README.md](README.md) and [local-dev/README.md](local-dev/README.md).
- Server is the workflow engine backed by Kafka/Kafka Streams; topologies and storage live under [server/src/main/java/io/littlehorse/server/streams](server/src/main/java/io/littlehorse/server/streams). Entrypoints/listeners are in [server/src/main/java/io/littlehorse/server](server/src/main/java/io/littlehorse/server).
- Protobuf definitions live in [schemas](schemas); generated code is required by server/SDKs. Any schema change must be followed by ./local-dev/compile-proto.sh (see [local-dev/README.md](local-dev/README.md)).

### Server Internals

- Protobuf Stuff
  - The public API is in protobuf defined in `schemas`
  - In the `server`, we implement an `LHSerializable` for every proto object.
- A `Getable` is an abstract class used to represent something that can be fetched from the API (eg. `lhctl get wfRun/taskRun/etc`)
- A `Getable` has index configurations on it which create `Tag`s, which are stored in rocksdb as a secondary index.
  - Tags are stored with their key being one or more attributes (key-value pair) and the timestamp of when the `Getable` was created.
  - Searches return an "attribute string" to range-scan over in rocksdb (see `BackendInternalComms` and `SearchWfRunRequestModel` for example)
  - All `Getable`s have a partition key. Most of them are co-partitioned along with the `WfRun` they belong to.

### API Glossary

These things are defined in the `schemas` folder.

* **`WfSpec`**: a workflow specification.
* **`WfRun`**: a workflow run instance. Created when you run a `WfSpec`.
* **`TaskDef`**: a definition for a task (to be executed by a computer) that can be used in `WfSpec`s.
* **`TaskRun`**: an instance of a `TaskDef` being executed.
* **`ExternalEventDef`**: defines a type of event that can occur outside of the context of a `WfRun`. A `WfSpec` can interact with `ExternalEvent`s by waiting for them or by handling them as signals.
* **`ExternalEvent`**: an instance of an event. This is the execution analog of `ExternalEventDef`. Each `ExternalEvent` is associated with one specific `WfRun`.
* **`CorrelatedEvent`**: is a precursor to an `ExternalEvent`. A `CorrelatedEvent` creates an `ExternalEvent` when it is correlated to a `WfRun` based on some key.
* **`WorkflowEvent`**: a type of event that can be _thrown by_ a `WfRun`. In contrast to an `ExternalEvent` (which a `WfRun` listens to), a `WfRun` _produces_ a `WorkflowEvent`.
* **`WorkflowEventDef`**: definition for a `WorkflowEvent`. This is the metadata analog.
* **`StructDef`**: a user-provided definition for a complex data type in LittleHorse (see `Struct` below).
* **`UserTaskDef`**: defines a task that is to be executed by a human user.
* **`UserTaskRun`**: an instance of a `UserTaskDef`.
* **`NodeRun`**: a running instance of a `Node`. See below for definition of a `Node`.

Next, some items which aren't  `Getable`s, but should be mentioned here:

* **`VariableValue`**: a value in a LittleHorse workflow.
* **`Struct`**: a `VariableValue` in LittleHorse whose value conforms to a `StructDef`. Just like a struct instance in go.
* **`ThreadRun`**: a `WfRun` consists of one or more `ThreadRun`s. The lifecycle of a `WfRun` mirrors the entrypoint `ThreadRun`. Each `ThreadRun` can execute one computational step at a time: if you want to do multiple things at once in a `WfRun`, you need multiple `ThreadRun`s.
* **`ThreadSpec`**: a `WfSpec` consists of one or more `ThreadSpec`s. A `ThreadSpec` is a directed graph of computational steps (`Node`s).
* **`Node`**: a `Node` represents a computational step within the graph of a `ThreadSpec`. Some types of `Node`s are Task nodes, User Task nodes, and External Event nodes. When a `ThreadRun` starts, it moves to the first `Node` in the `ThreadSpec`. Every time it reaches a `Node`, a `NodeRun` is created to track the computational step defined by the `Node`.

Lastly for Getables, some metadata `Getable`s which do not have anything to do with a workflow but rather are "cluster-level" metadata:

* **`Principal`**: defines the identity of some user (machine or human) of the LH Kernel and the associated permissions.
* **`Tenant`**: a logically isolated environment within the LH Kernel, like a namespace in kubernetes.

## Critical dev workflows

- If the human hasn't set up kafka, you can look into ./local-dev/setup.sh (see [local-dev/README.md](local-dev/README.md)).
- If the human doesn't have the server running locally: ./local-dev/do-server.sh
- If you need to run the dashboard: in [dashboard](dashboard), npm install && npm run dev; expects server on localhost:2023 (see [dashboard/README.md](dashboard/README.md)).
- Compile protobuf after changing anything in [schemas](schemas): `./local-dev/compile-proto.sh`
- Install `lhctl` by `cd lhctl && go install .`
- Use `poetry run ...` etc inside `sdk-python`

### E2E Tests

If you edit the server, and every time you add functionality in the `sdk-java`, please add an end-to-end test. Look in `server/src/test/java/e2e` for examples.

You can run them with
```
./gradlew server:e2e --tests MyTestFile
```

Try your best to avoid tests that require sleeping or waiting a long time as we don't want the pipeline to take too long.

It's also helpful to reuse a single `WfSpec` for multiple test cases rather than generate one `WfSpec` per e2e test because registering the `WfSpec` takes longer than processing a test.

## Project-specific conventions

- Dashboard env lives in dashboard/.env.local (not repo root). Use LHC_* vars to point to the server; OAuth env uses LHD_* and Keycloak vars (see [dashboard/README.md](dashboard/README.md)).
- Server auth modes (OAuth/mTLS) require local-dev scripts and config files in local-dev/configs (see [local-dev/README.md](local-dev/README.md)).
- PR messages follow Conventional Commits v1.0.0 (see [local-dev/README.md](local-dev/README.md)).

## Integration points & boundaries

- Server uses Kafka as the state store; Interactive Queries are used for GETs across LH Server (see multi-server installation notes in [local-dev/README.md](local-dev/README.md)).
- Dashboard talks to server over the LHC API host/port; default server port is 2023 and dashboard runs on 3000.
- SDKs and examples are language-specific consumers of the protobuf schema; check [examples](examples) when changing API behaviors.

## Code Style

Keep code concise and readable.
