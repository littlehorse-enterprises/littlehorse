# Proposal: Workflow Migrations

Author: Jake Rose

This proposal will introduce the following:
- `WfRunMigrationPlans`(metadata object)
- `WfRunMigration` (run time action)
- edge cases while migrating `wfRun`
- Future Plans

## Background

Long-running processes are one of the main attractions of any durable execution engine, allowing state to survive for days, weeks, or even months. As a result, migrations become an important feature, since services and processes inevitably change over time.

Migrations -- the practice of transferring runtime state from one actively running process (`wfRun`) to another compatible process specification (`wfSpec`) becomes a must have capability.


## `WfRunMigrationPlan`

`wfRunMigrationPlan` is a metadata object that sets the stage for how a migration will occur within `wfRun`

You can think of the `WfRunMigrationPlan` as a set of instructions that will be enforced during a given migration.

The rpc to register this metadata object in the lh server will look as follows:

### Proto changes
```
  rpc PutMigrationPlan(PutMigrationPlanRequest) returns (WfRunMigrationPlan) {}
```

After registering the metadata object, the lh server will return the object itself.

The following is the wfRunMigrationPlan proto:

```
message WfRunMigrationPlan {
    // Name
    MigrationPlanId id = 1;

    // When the MigrationPlan was created.
    google.protobuf.Timestamp created_at = 2; 

    // Wrapper object that stores how to migrate every thread
    MigrationPlan migration_plan = 3 ;
}
```
The first two fields are self-explanatory: the ID used to identify the plan and the time it was created. The third field is a wrapper class where metadata about the migration begins.

The `MigrationPlan` consists of the following proto:

```
message MigrationPlan { 
    // {Threadname: {How to migrate that thread}}
    map<string, ThreadMigrationPlan> thread_migrations = 1;
}
```
Migration plans are broken down to a thread by thread basis.
Every thread gets its own individual migration plan.

```
// How to migrate a thread 
message ThreadMigrationPlan {
    // Name of the new thread we are migrating to
    string new_thread_name = 1;
    // name of curNode - > how to migrate curNode in new wfSpec
    map<string, NodeMigrationPlan> node_migrations  = 2;
    // Any variable that will be derived before migration node can be provided here
    map<string, VariableValue> migration_variables = 3;
}
```
For every thread we are migrating we need to know its exact destination in the new `wfSpec`so every thread migration has a new_thread_name -- name of the thread in the new wfSpec.

Additionally we need to know what node within the new thread we are migrating to and for that reason we have node_migrations.

Migration_variables come into play anytime a variable in the new `wfSpec` was derived prior to the migration node. That can either be from a node output, input variable, or assignment. Migration_variables allow clients to inject variable values into variables that need a value but may not have one due to where the migration occured. 

```
//  How to migrate Node 
message NodeMigrationPlan {

    // Currently just migrate to node with this name 
    string new_node = 1;
}
```
As of now just the new_node name will be sufficient. A check will be done making sure nodes are of same sub type before migrating.

### Loose `WfRunMigrationPlan` Criteria

This implementation provides loose enforcement of rules when registering the `wfRunMigrationPlan` metadata object, since at registration time we do not yet know the runtime state of any `wfRun` that will be migrated or the version it will migrate to.

This design allows users to define a single `wfRunMigrationPlan` object and apply that migration to multiple `wfRuns` without having to redefine the migration plan for each migration.

### Additional Proto changes

```
enum GetableClassEnum {
    WF_RUN_MIGRATION_PLAN = 21;
}
```

Getting Object:

```
  // Get Migration Plan
  rpc getMigrationPlan(MigrationPlanId) returns (WfRunMigrationPlan) {}
```

```
message MigrationPlanId {
    string name = 1;
}
```

## Migrating a `wfRun`

Now that the `wfRunMigrationPlan` has been registered, we can migrate a `wfRun` using the following RPC and schema implementation.

```
rpc MigrateWfRun(MigrateWfRunRequest) returns (WfRunId) {}
```

```
message MigrateWfRunRequest {
  // Migration Plan to use
  MigrationPlanId migration_plan_id = 1; 

  // WfRun to Migrate
  WfRunId wf_run_id = 2;

  // What version to migrate to
  int32 revision_number = 3;
  int32 major_version_number = 4;

}
```
To perform a migration, we need the name of the migration plan, the `wfRunId` of the `wfRun` being migrated, the revision number, and the major_version_number of the new specification to which it will migrate.

The migration schema itself is fairly straightforward. The complexity arises in the server-side logic executed during a migration.

To help guide this proposal, I have put together several diagrams to illustrate possible edge cases.

### Edge cases

#### **Base Case:** 
First is the base case this shows a successful migration from a `wfRun` to a new `wfSpec`
Some details to note here, we are migrating to the same sub node type, there
are no additional variables, and there are no threads to be handled.  

[Migration base case](./img/WfRun_Migration_base_case.jpg)

#### **Case 2**: 
**Valid Migration w/ Threading**

This second example introduces threading into the equation of a migration.
If a thread in a new `wfSpec` spawned a thread prior to the migration node we still
have a valid migration, but the work in that thread never gets done.

[Valid Migration w/ threading](./img/wf_run_threading_migration_valid.jpg)

#### **Case 3**:
**Invalid Migration w/ threading**

In case number three we see an example of an invalid migration that will be rejected by the lh server. In this case a thread is spawned prior to the migration node and after the migration node `waitForThread` node exist. This node would wait indefinitely since the thread was never spawned and work will not be done. 

[Invalid migration with threading](./img/Migration_w_threading_invalid.jpg)

#### **Case 4**: 
**Introducing Variables**:

In case 4 variables that exist within new `wfSpec` that do not exist within the
current `wfSpec`/`wfRun` must be provided within the map of variables for the given thread. If not then the migration is invalid do to missing state.


[migration w/ new vars needed](./img/migration_w_variables.jpg)

## Server Impl

For this implementation of `wfRunMigrations`, only a single workflow run is migrated per request. Each request is partitioned by the `wfRunId`, which provides access to the `wfRun` state stores for the specific `wfRun` being migrated.

Migrations will occur in a two phase process. Phase 1 will be validating the migration based on current 
`wfRun` state and new `wfSpec` version

`WfRunMigration` validation logic will exists within the wfRunModel.

wfRun.validateMigration(newSpec, `wfRunMigrationPlan`)



During Validation will ensure that

- The `wfRun` up for migration is currently running

- The new `wfSpec` version actually exists

- All active threads in the current `wfRun` have a migration plan, if not then the migration is invalid

- The current node within the active thread matches the subnode in the new `wfSpec`, if not the migration is invalid.

- Check in the new `wfSpec` for `waitForThread` nodes. If exists make sure the threads derive after the migration node in the the new wfSpec.

- Check for variables, any unaccounted for variable must be provided in a variable mapping or derived after the migration. 

In phase two we now need to actually migrate the workflow. This consists of making the `wfRun` that is migrating reference the new `wfSpec` and updating the `oldWfSpecVersions` field like so:

```java
    this.getOldWfSpecVersions().add(wfSpecId);
    this.setWfSpec(newWfSpec);
    this.setWfSpecId(newWfSpec.getId());
```

Now For every active thread we have collected we migrate them to a new `wfSpec`.

```java 
  // Iterating through every active thread
  for(int i = 0 ; i < activeThreads.size(); i++){
            // Getting our active thread
            ThreadRunModel migrateThread = activeThreads.get(i);
            // Make old thread reference a new wfSpec
            migrateThread.setWfSpecId(newWfSpec.getId());
            // Get the threadMigrationPlan for current active thread
            ThreadMigrationPlanModel threadMigrationPlan = migrationPlan.getMigrationPlan().getThreadMigrations().get(migrateThread.getThreadSpecName());
            // Get name of new thread in new Spec
            String newThreadName = threadMigrationPlan.getNewThreadName();
            // Change the name of the current thread 
            migrateThread.setThreadSpecName(newThreadName);
            // Get the current node run of current thread
            NodeRunModel currNode = migrateThread.getCurrentNodeRun();
            
            // Now set current node run = the new node run
            currNode.setWfSpecId(newWfSpec.getId());
            currNode.setThreadSpecName(newThreadName);
            currNode.setNodeName(threadMigrationPlan.getNodeMigrations().get(currNode.getNodeName()).getNewNode());
        } 
```

This lays the basis for a migration.

### Future Work

This implementation only allows migrating one workflow instance at a time. Future implementations could support migrating multiple `wfRuns` in a single request by allowing clients to provide a list of `wfRunIds`. The LH Server would then create a command for each individual `wfRun`, partitioning them by `wfRunId` so that each migration has access to the appropriate `wfRun` state store.