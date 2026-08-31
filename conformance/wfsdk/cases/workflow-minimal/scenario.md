# workflow-minimal

Create a workflow named `probe-workflow-minimal` whose entrypoint thread
does nothing, and compile it (sdk-java:
`Workflow.newWorkflow(name, wf -> {}).compileWorkflow()`).

Single-variant case: "having a workflow at all" cannot be subtracted from a
workflow, so there is no base fixture. The one fixture pins everything the
builder decides on its own: the node naming scheme
(`0-entrypoint-ENTRYPOINT`, `1-exit-EXIT`), the automatic entrypoint→exit
edge, `entrypointThreadName: "entrypoint"`, and the `ALL_UPDATES` default.
