# Proposal: Workflow Migrations

Author: Jake Rose


## Introduction
This proposal will introduce the following:
- `WfRunMigrationPlan` (metadata object)
- `WfRunMigration` (run time action)
- `wfRun` schema extension
- Edge cases while migrating `wfRun`
- Future Plans

## Background

Long-running processes are one of the main attractions of any durable execution engine, allowing state to persist for days, weeks, or even months. Since systems evolve, supporting process migrations between specifications becomes essential.


# Migrations Overview

Migrations are the practice of transferring runtime state from one actively running process (`wfRun`) to another compatible process specification (`wfSpec`)

A Migration will be a runtime action that takes place on a `wfRun` manipulating underlying specifications to allow a `wfRun` to keep its identity while changing the course of action.

Every `threadRun` or every actively running `threadSpec` within a `wfRun` must be provided with a set of instructions on how to migrate to the new `wfSpec`'s correlated `threadSpec`, in return this will modify the existing `threadrun`'s path of exectution to the new `threadSpec`.
(See considerations)


Migrating `threadSpecs` will allow for an 
alternative course of execution, but migrations that do not  properly address runtime state can cause errors within a new `threadRun`. This means variables must be carefully handled ensuring proper semantics.

The remaining of this proposal will introduce schema impl for migrations as well as migration safety, ensuring migrations do not cause unintended errors.


# PROTO CHANGES 
## `WfRunMigrationPlan` 

The `wfRunMigrationPlan` is a wrapper object around set of instructions provided by the user to determine how a `wfRun` will migrate.


```
message WfRunMigrationPlan {
    // Name
    MigrationPlanId id = 1;

    // When the MigrationPlan was created.
    google.protobuf.Timestamp created_at = 2; 

    // ThreadRun Name -> ThreadMigrationPlan
    // How each threadrun will migrate to the new wfSpec
    map<string, ThreadMigrationPlan> migration_plan = 3 ;

    // The id of wfSpec that this plan migrates to
    WfSpecId new_wfSpec = 4;

}
```


```


// Plan  to migrate individual threadRun
message ThreadMigrationPlan {

    // Name of the new threadSpec we are migrating to
    string new_thread_name = 1;

    // name of migrationNode - > where to start in new threadSpec
    map<string, NodeMigrationPlan> node_migrations  = 2;

    // The set of vars within the new threadRun
    // that state would have been derived prior
    // to the migration node
    repeated string required_migration_vars = 3;
}
```


```
//  How to migrate Node within threadRun
message NodeMigrationPlan {

    // Currently just migrate to node with this name 
    string new_node = 1;
}

```
### Variables
**Required_Migration_vars** field represents the set of variables in the new `WfSpec` whos state will need to be derived at migration time.

This could include a variable whos value would have been assigned prior to the migration node.

In the migration impl the `wfRunId` and `threadRunNumber` will remain the same, so just because we change the specs we do not lose any variable state. 
That being said if the new `threadSpec` has a variable named the same as in the previous `threadSpec` the state will carry. If this is not the desired variable state then it can be reassigned at migration time.

This impl lets the `migrationPlan` create strict rules regarding what variables will need variable assignment at the time of migration. 




## Creating `wfRunMigrationPlan` object

```
  rpc PutMigrationPlan(PutMigrationPlanRequest) returns (WfRunMigrationPlan) {}
```

```
// Request object to create Migration Plan
message PutMigrationPlanRequest {
    string name = 1; 
    WfSpecId newSpec = 2;
    map<string, ThreadMigrationPlan> = 3;
}
```

### Additional Proto changes

**ID**:
```
message MigrationPlanId {
    string name = 1;
}
```
**GETTABLE ENUM**:
```
enum GetableClassEnum {
    WF_RUN_MIGRATION_PLAN = 21;
}
```

**Get Request**: 
```
  // Get Migration Plan
  rpc getMigrationPlan(MigrationPlanId) returns (WfRunMigrationPlan) {}
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


    // Name of threadRun -> Migration Vars needed 
    map<string, MigrationVariables> migration_var_assignments= 3;

}

```

```
message MigrationVariables {
    // Name of var -> how to derive value
    map<string, VariableAssignment> migrations_vars = 1
}

```
## WfRun Proto Extension

```
message WfRun {
    // ...
    // ...

    // Designates this wfRun as migrating 
    MigrationPlanid migration_plan_id =11
}
```

Migration_plan_id will serve as a flag to determine if a `wfRun` is currently migrating to a new wfSpec.

This allows a migration to be called prior to a `threadRun` being at the migration node. If migration_plan_id is not null then we check on every threadRun.advance() call if we moved to the migration node, if so then migrate.

If any `threadRun` has already passed the migration node, at the time rpc migrateWfRun is called then the migration will be rejected.



# Edge cases

To help guide this proposal, I have put together several diagrams to illustrate possible edge cases.

#### **Base Case:** 
First is the base case this shows a successful migration from a `wfRun` to a new `wfSpec`
Some details to note here, we are migrating to the same sub node type, there
are no additional variables, and there are no threads to be handled.  

[Migration base case](./img/WfRun_Migration_base_case.jpg)

#### **Case 2**: 
**Valid Migration w/ Threading**

This second example introduces threading into the equation of a migration.
If a `threadSpec` in th new `wfSpec` spawned a `threadRun` prior to the migration node we still have a valid migration, but the work in that thread never gets executed.

[Valid Migration w/ threading](./img/wf_run_threading_migration_valid.jpg)

#### **Case 3**:
**Invalid Migration w/ threading**

In case number three we see an example of an invalid migration. In this case a threadRun is spawned prior to the migration node and after the migration node `waitForThread` node exist. This node would wait indefinitely since the thread was never spawned and work will not be done. 

(I am not sure if this validation can be done within the lh Server)

[Invalid migration with threading](./img/Migration_w_threading_invalid.jpg)

#### **Case 4**: 
**Introducing Variables**:

In case 4 variables that exist within new `wfSpec` that do not exist within the current `wfSpec`/`wfRun` must be provided at the time of migration and provided in required_migration_vars for the given threadMigrationPlan.

[migration w/ new vars needed](./img/migration_w_variables.jpg)

## Server Impl

For this implementation of `wfRunMigrations`, only a single workflow run can be migrated per request. Each request is partitioned by the `wfRunId`, which provides access to the `wfRun` state stores for the specific `wfRun` being migrated.

Migrations will occur in a two phase process. Phase 1 will be validating the migration based on current 
`wfRun` state and new `wfSpec` version

`WfRunMigration` validation logic will exists within the wfRunModel.

wfRun.validateMigration(`wfRunMigrationPlan`)



During Validation we will ensure that

- The `wfRun` up for migration exists

- All active threads in the current `wfRun` have a migration plan, if not then the migration is invalid (possibly)

- All migrations nodes for a given  `threadRun` have not been executed yet

- The current node within the active thread matches the subnode in the new `wfSpec`, if not the migration is invalid.

- Check in the new `wfSpec` for `waitForThread` nodes. If exists make sure the threads derive after the migration node in the the new wfSpec. (I am not sure about this yet maybe passed to client)

- Ensure all required migration variables defined in the required_migration_vars for `threadMigrationPlan` are provided


After we validate the above we can now migrate the wfRun.



The actual migration process consist of changing the underlying specifications and applying variable assignments. When a `threadRun` arrives at its migration node then we change the `nodeRun` to reference the destination node spec's within the `nodeMigrationPlan` as well as the `threadSpec` name and the `wfSpec` name.

Once every `threadRun` has been migrated to its new threadSpec then the `wfRun`s `wfSpecId` will be updated.

### Considerations and feedback please

Should every threadRun need a threadMigrationPlan ?  

If we allow rpc migrateWfRun to be called prior to all threadRuns being at the migration node then new threadRuns can be spawned after the rpc. This would mean that we can not ensure all active threadRuns have a migrationPlan. Some threadRun could be left behind running on the old wfSpec. I do not think we can ensure every threadRun is operating under the new wfSpec.

If we ensure every thread is at the migration node. Then every threadRun will migrate to the new wfSpec and threadSpec.


### Future Work

This implementation only allows migrating one workflow instance at a time. Future implementations could support migrating multiple `wfRuns` in a single request by allowing clients to provide a list of `wfRunIds`, or possibly by providing a wfSpec version and migrations all `wfRun`s for that version.