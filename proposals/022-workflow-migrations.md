# Workflow Migrations

**Author:** Jake Rose

### Motivation
LittleHorse gives a `wfRun` instance the ability to last for days, weeks, or even months.
Business processes may change over time (WfSpec versioning), but an existing `wfRun` may still operate on old business logic represented by a `threadSpec`. In order to keep active workflows up to date with the current business processes, as well as handle or avoid technical errors, `wfRun`s need the ability to migrate `threadRun`s to new `WfSpec` versions.

## Proto

```proto
message WorkflowMigrationPlan {
  // The id of the migration plan
  WorkflowMigrationPlanId workflow_migration_plan_id = 1;

  // Time WorkflowMigrationPlan was created
  google.protobuf.Timestamp created_at = 2;

  // Map from old threadSpec name to ThreadMigrationPlan
  map<string, ThreadMigrationPlan> thread_migrations = 3;
        
  // Source wfSpec
  WfSpecId old_wfSpec = 4;

  // major_version and revision of the destination specs 
  int32 major_version = 5;
  int32 revision = 6;
}


// EXPERIMENTAL: Plan describing how to migrate a ThreadRun to a threadSpec in the new WfSpec.
message ThreadMigrationPlan {
    
  // Name of threadSpec in new WfSpec that threadRun will migrate to
  string new_thread_name = 1;

  // Map of old node name -> how to migrate that node in the new WfSpec
  map<string, NodeMigrationPlan> node_migrations = 2;

  // Names of threadSpecs in the new WfSpec that must have already migrated to
  // before this thread can migrate (so any variables they create are available).
  // This list is created internally by the server.
  repeated string dependencies = 3;

}

// Request-side representation of a ThreadMigrationPlan. Dependencies are not provided by the client; they are computed internally by
// the server when building the WorkflowMigrationPlan.
// EXPERIMENTAL.
message ThreadMigrationPlanRequest {

  // Name of thread in new WfSpec that the thread will migrate to
  string new_thread_name = 1;

  // Map of old node name -> how to migrate that node in the new WfSpec
  map<string, NodeMigrationPlan> node_migrations = 2;
}

// EXPERIMENTAL: Plan describing which Node in the new WfSpec a migrated ThreadRun lands on.
message NodeMigrationPlan {

  // Name of node in the new WfSpec to migrate to
  string new_node_name = 1;
}

// EXPERIMENTAL: Variable assignments applied to a thread when it is migrated.
message MigrationVars {
  map<string, VariableAssignment>  var_assignment_by_var_name = 1;
}

```

```proto
message WfRun {

  // .... 


  // reference to WorkflowMigrationPlanId
  WorkflowMigrationPlanId workflow_migration_plan_id = 12;

  // Map to determine how to reassign variable values during migration
  // newThreadName -> MigrationVars
  map<string, MigrationVars> migration_variables = 13;
}
```


### RPC
```proto 
  // Register a workflow migration plan with lh server
  rpc PutWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest) returns (WorkflowMigrationPlan) {}

  // Get a workflow migration plan by ID
  rpc GetWorkflowMigrationPlan(WorkflowMigrationPlanId) returns (WorkflowMigrationPlan) {}

  // Deletes Workflow Migration Plan metadata object from the server
  // Maybe this is not a good idea since a `wfRun` holds the migration plan id
  rpc DeleteWorkflowMigrationPlan(DeleteWorkflowMigrationPlanRequest) returns (google.protobuf.Empty) {}

  rpc ApplyWorkflowMigrationPlan(ApplyWorkflowMigrationPlanRequest) returns (WfRun) {}
```


### Request Objects 

```proto
message PutWorkflowMigrationPlanRequest {

  string name = 1;

  WfSpecId old_wfSpec = 2;

  int32 major_version = 3;

  int32 revision = 4;

  // ThreadMigrationPlanRequest object
  map<string, ThreadMigrationPlanRequest> thread_migrations = 5;

}

message DeleteWorkflowMigrationPlanRequest {
  WorkflowMigrationPlanId id = 1;
}


message ApplyWorkflowMigrationPlanRequest {

  WorkflowMigrationPlanId id = 1;

  WfRunId wfRun_id = 2;

  map<string, MigrationVars> migration_vars_by_thread= 3;

}
```



## `putWorkflowMigrationPlan` validations

### Thread Migration Rules 
- Entrypoint threads can only migrate to entrypoint threads.

- Child threads, interrupt handlers, and failure handlers can migrate between each other, but engineers will be strongly advised to carefully create `WorkflowMigrationPlan`s where `threadRun`s migrate to semantically equivalent `threadSpec`s.
  
- Any `newThreadSpec` that uses a variable that is not in the scope of the `oldThreadSpec` will result in an invalid request unless the parent `newThreadSpec` that defines the variable is migrating as well.


### Migration Nodes Rules
- You can migrate to and from any working node type within LittleHorse.

## `applyWorkflowMigrationPlan` validations

`ApplyWorkflowMigrationPlan` stamps a `workflowMigrationPlanId` onto a `wfRun`, allowing migration to happen lazily. The only validation is to check if any `migrationVar`'s `varAssignment` is provided as a literal value. If so, the server will ensure that the literal value is compatible with the type of the variable it is trying to update.


## Migration

### Migrating `threadRun`s

Migration occurs lazily: the `wfRun` is stamped with the `workflowMigrationPlanId`. On each advance, the server checks if the thread is migrating and whether it is ready to migrate. There are two types of migrations that can occur. The first is migrating from an active node. Migrating from an active node occurs when the apply request comes in and a `threadRun` is at a migration node that is long-running (external event, user task, sleep, waitForCondition). The active long-running node will be safely halted and then migration will take place. This example is helpful when a `wfRun`/`threadRun` is stuck at some long-running node and needs to be moved to a new `threadSpec`. The other type of migration occurs when the next node is the migration node. If the current node completes and the next node is a migration node, migration will take place before the migration node is activated. This implementation allows `wfRun`s to migrate from any type of working node.

The actual mechanism for migration updates the `threadRun`'s name to the new `threadSpec` and the `threadRun`'s `wfSpec` to the new `WfSpec`. After that, the new node is activated and the `threadRun` continues operating under the new `threadSpec`.

### Migration Lifecycle

The `workflowMigrationPlanId` will stay stamped onto a `wfRun` until every `threadRun`'s `wfSpec` is on the new `WfSpec` version. This condition ensures that every child `threadRun` spawned that uses the old `WfSpec` will be checked for migration. Once the `workflowMigrationPlanId` is cleared, the previous `wfSpec` version will be added to `oldWfSpecVersions` on the `wfRun`.

When an entrypoint `threadRun` migrates to a new `WfSpec`, the `workflowMigrationPlanId` will eventually be removed from the `wfRun`. This is due to the nature of the entrypoint `threadRun`'s status: the entrypoint `threadRun` will not complete until all child `threadRun`s have been settled, allowing migration to complete either when all child `threadRun`s are completed or when all `threadRun`s are operating under the new `WfSpec`.

In the case where only child `threadRun`s are migrated to a new `WfSpec`, the `workflowMigrationPlanId` will remain stamped onto the `wfRun` because the above condition will never be met. While the `workflowMigrationPlanId` is stamped on the `wfRun`, any other `ApplyWorkflowMigrationPlanRequest` will be rejected.

#### Dependency Threads
Naturally, thread migrations occur in no particular order. Threads will migrate immediately when `.advance()` is called and the `threadRun` is at a migration node. In the situation where the new `threadSpec` uses a variable defined in a parent thread, this behavior could cause runtime errors. To handle this, we internally build a list of thread `dependencies` that must exist at runtime for the new `threadSpec` to be migrated to.


    
## Future work
- bulk migrations







