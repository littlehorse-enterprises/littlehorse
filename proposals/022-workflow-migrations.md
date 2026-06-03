## Workflow Migrations

### Motivation
Littlehorse gives a wfRun instance the ability to last days, weeks, or even months.
Business proccesses may change overtime (wfSpec versioning), but an existing wfRun may still operate on old business logic respresented by a threadSpec. In order to keep active workflows up to date with the current business processes, as well as avoid as handle or avoid technical errors wfRuns need the ability to migrate threadRuns to execute under new threadSpecs created in newly versioned wfSpecs

### Goals
- Allow a wfRun's threadRun to change the threadSpec it is operating on
- Minimize possible runtime errors when apply migrations.

## Proto


Message names up for debate I was more so focused on impl

```proto 


message WorkflowMigrationPlan{
    // The id of the migration plann
    WorkflowMigrationPlanId workflow_migration_plan_id= 1;

    // Time WorkflowMigrationPlan was created
    google.protobuf.Timestamp created_at = 2;

    // Map that represent old threadSpec name -> How to migrate that threadspec
    map<string, ThreadMigrationPlan> thread_migrations = 3;
        
    // Source wfSpec
    WfSpecId old_wfSpec = 4;

    // major_version and revision of the destination specs 
    int32 major_version = 5;
    int32 revision = 6;
}


message ThreadMigrationPlan {
    
    // Name of thread in new wfSpec that thread wants 
    // to migrate to 
    string new_thread_name = 1;

    // Name of node in old thread to migrate from
    string from_node = 2;

    // name of node to migrate to within new spec
    string to_node = 3;

    // List of variables that threadRun MUST have access to when migrating to new_thread_name
    // useful when new_thread_spec uses a variable that is not in old_thread_spec
    repeated string required_variables = 4;

    // List of threads threads that must migrate first
    // used in server logic to make sure variables 
    repeated string dependencies = 5;

}

// varName -> variableAssignment
// used to insert values at runtime
message MigrationVars {
  map<string, VariableAssignment>  var_assignment_by_var_name = 1;

```proto

message WfRUn {

    // .... 


    // reference to WorkflowMigrationPlanId
    WorkflowMigrationPlanId workflow_migration_plan_id = 12;

    // Map to determine how to reassign variable values during Migration
    // newThreadName -> MigrationVar
    map<string, MigrationVars> migration_variables = 13;
}
```


### RPC
```proto 
  // Register a workflow migration plan with lh server
  rpc PutWorkflowMigrationPlan(PutWorkflowMigrationPlanRequest) returns (WorkflowMigrationPlan) {}

  // Get a workflow migration plan by ID
  rpc GetWorkflowMigrationPlan(WorkflowMigrationPlanId) returns (WorkflowMigrationPlan) {}

  // Deletes Workflow Migration Plan Metadata object from the server
  // Maybe this is not a good idea since wfRun hold migrationPlanId
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

  map<string, ThreadMigrationPlan> thread_migrations = 5;

}

message DeleteWorkflowMigrationPlanRequest{
  WorkflowMigrationPlanId id = 1;
}


message ApplyWorkflowMigrationPlanRequest {

  WorkflowMigrationPlanId id = 1;

  WfRunId wfRun_id = 2;

  map<string, MigrationVars> migration_vars_by_thread= 3;

}
```


## Implemention details


### Thread Migration Rules 
- Entrypoint threads can only migrate to entrypoint threads
- Child threads, interupt, and failure handlers can migrate between
eachother as of current draft pr. This is not advised though when an engineer
is making a workflowMigrationPlan then should migrate interupt->interupt and so forth.

- The wfRun wfSpec only updates when entrypoint migrates, then wfSpec updates and prior wfSpec is added to oldWfSpec
This means that a wfRun will allow threadRuns to have threadSpecs on different version, so dashboard will have ot update
### MigrationNodes
- You have the ability to migrate from and to any node type unless the worklowMigrationPlan
is applied and a threadRun is currently at a migration node. If the migration node is not a waiting node then the applyMigrationRequest will be rejected

- In the case of which we migrate from an activeNode then the nodeRun
is put into a halted status when migrated from, but maybe this could be changed to a MIGRATED status.

### Migration Lifecycle
    - MigrationPlanId will stay stamped on a wfRun
    till each threadMigration has been satisfied 
#### .advance() with migrationPlan
    wfRun.advance()
    Calls thread.advance with planId if thread Migrates and has migrationPlan
    ```java
          if(workflowMigrationPlanId != null && thread.isMigrating(workflowMigrationPlanId)){
                    statusChanged = thread.advance(time, workflowMigrationPlanId) || statusChanged;
                    maybeDoneMigration();
                }else{
                    statusChanged = thread.advance(time, null) || statusChanged;
                }
    ```
    thread.advance()
    This action takes places on active nodes b
    ```java
       boolean migrated = false;
            // Try to migrate from a long-running active node even if it hasn't completed yet.
            // This must happen before the canAdvance gate so that a waiting ExternalEvent/UserTask/Sleep
            // node can be migrated away without waiting for the event to arrive.
            if (wfMigrationPlanId != null && currentNR.getNode().isLongRunning()) {
                migrated = maybeMigrate(wfMigrationPlanId, currentNR.getNodeName(), true);
            }
            if (migrated) return true;
    ```


    If we are moving to the next node and thread is migrating check if next node is the migration node
    this allows us to migrate from any node (depending on when migration is applied) 
    
    ```java

    } else {
                NodeModel nextNode = currentNR.evaluateOutgoingEdgesAndMaybeMutateVariables(processorContext);
                // Before we activate next node
                // check if we are migrating
                if(wfMigrationPlanId != null){
                    migrated = maybeMigrate(wfMigrationPlanId, nextNode.getName(), false);
                }
                if(migrated){
                    return true;
                }
                activateNode(nextNode);
            }
    ```

### Variables 
- required variables are currently determined by the engineer
creating the workflowMigrationPlan

   ```proto 
message ThreadMigrationPlan {
    
    // List of variables that threadRun MUST have access to when migrating to new_thread_name
    // useful when new_thread_spec uses a variable that is not in old_thread_spec
    repeated string required_variables = 4; 

}
```
- required_variables represents variables that must exist within the wfRun when migrating to a new threadSpec 
    
- if the variable is new, meaning created in the new Spec the following logic will take place
1) check if newThreadSpec owns VarDef, if so the variable will be created when this thread migrates
2) if newSpec does not own the variable find which thread has the varDef.
3) From the thread that owns the varDef check if the newThreadSpec is a descendent.
4) If it is a descendent then the variable is within the scope
5) Now check if thread has the varDef is migrating, if not reject request becuase var will never exist
6) If the thread that owns the varDef is migrating add its name to the current threadMigration list of dependency threads

#### Dependency Threads
Naturally migration occur in no particular order, if a thread can migrate it will
that is where dependencies come into action.

When a thread has a dependency it will make sure that the dependency thread migrates first.
This can help with avoiding run time errors when trying to access variables that would not exist.
If a threadMigrationPlan has a required_var that variable is guranteed to exist at runtime or the migration will be 
rejected.

##TODO 
- spawn_Threads
- ciruclar dependencies
    








