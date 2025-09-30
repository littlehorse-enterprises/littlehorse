# **RPC MigrateWfSpec**

## **Motivation**

### **At the core of Every Company Lie Their Workflows**
 Workflows directly correlate to business goals and hence they are ever changing. It is not uncommon for companies to change goals and business processes abruptly.

For instance think of the following simplified workflow:
 
(Customer places Order) -> (Check inventory) -> (charge customer) -> (ship order)-> (wait for package delivered event) -> (notify customer package was delivered)

This Workflow is durable and can span days, weeks, or even months. 

Now this company decided they want to make customer satisfaction more of a concern so they devise the following workflow:

(Customer places Order) -> (Check inventory) -> (charge customer) -> (ship order)-> (wait for package delivered event) -> (notify customer package was delivered)->(ask customer about delivery experience) -> (ask for product review) -> (human in the loop follow up if bad experience)

This revised workflow is a continuation of the initial workflow. The problem is that they have x deliveries in progress that will not track customer experience due to the fact that they were initialized under their first `wfSpec`.

The following proposal will introduce `rpc migratewfSpec` a solution to allow our x `wfRuns` to still align with our new workflow changes or new `wfSpec`

## Proposal

This proposal can be broken into two separate cores and they are the following.
One defining what a Migration is and two understanding how to migrate while keeping compatibility. 

### Defining a Migration

There are numerous ways you could think of a migration from `wfRunA` -> to new `wfRunB` that is under a different `wfSpec` so it is important to provide a strict definition to enable `wfSpec` authors to understand exactly how to make a `wfSpec` compatible for migration.

I propose the term Migration within littlehorse to be defined as the following.

A migration is when an instance of a `wfSpec_A` `wfRun_A`s current structure can be rebuilt in an observationally identical manner within the new `wfSpec_b`.

To meet this definition we must adhere to the following criteria:

1) WfRun_A total threads == new_wfRun_B total threads at the migration point
2) (wfRun_A Thread_A node position = 2) == (wfRun_b Thread_b node position = 2)
3) Any thread that is running or completed must be mapped to a destination thread
4) Any Node within Wf_runA with a status of completed must be mapped to another node within wfSpec_b
5) Edge Condition and variable Mutations amongst interior `wfRun nodes` must be the same(Along the path of execution). If it is a pivot node no validation is needed 

Once the criteria is met `wfSpecB` can diverge in any manner.


### Proto
The `migrateWfSpecRequest` should include the following:

1) A specification on what to migrate either a list of wfRuns or wfSpec(migrates all wfRuns under a wfSpec)

2) Specifications on how each thread and node should be migrated:
3)  Map to provide new input variables required in wfSpecB or override existing
input variables
4) Optional flag to determine if wfRun are migrated atomically
5) Specify if events posted to the Workflow Carry between wfRun's


```proto
// Request to build Migration plan
message MigrateWfSpecRequest {

    oneof scope {
        // Migrate every wfRun under a wfSpec if provided
        optional WfSpecId old_wf_spec = 1;

        // Migrate a set of wfRuns
        optional repeated WfRunId wf_run_list =2;

    } 

    // Migration plan that specifies thread and node mapping
    WfSpecVersionMigration migration_plan = 3;
    
    // All or nothing 
    boolean atomic = 4;

    // Carry Events 
    boolean do_not_migrate_event = 5;

    // Validates which wfRuns are compatible but
    // does not actually migrate
    boolean validate_only = 6;
}

message WfSpecVersionMigration {
    int32 new_major_version = 1;

    int32 new_revision = 2;

    // individual thread migration plans
    map<string, ThreadSpecMigration> thread_spec_migrations = 3;
}


message ThreadSpecMigration {
    // The name of the ThreadSpec in the new WfSpec 
    // that this ThreadSpec should migrate to
    string new_thread_spec_name = 1;

    // Map from name of node to be migrated to the migrations plan for that node
    // note nodes can only have ONE migration plan per thread
    map<string, NodeMigration> node_migrations = 2;
    
    // List of ThreadVariables . If new wfSpec requires 
    // an additional input variable it can be provided here.
    map<string,threadVarDef> variables = 3;
}

message NodeMigration {
    // The name of the Node on the new WfSpec to move to.
    string new_node_name = 1;

}
```

## Versioning 

Migration can occur between two `wfSpecs` if they have the same name and different versions. Versioning does not imply compatibility for wfSpec migration but it is a prerequisite.

Compatibility checks will be done in the server on a per wfRun basis.

# Server impl

The server impl will include validation around the surrounding criteria mentioned earlier.

1) WfRun_A total threads == new_wfRun_B total threads
2) (wfRun_A Thread_A node position = 2) == (wfRun_b Thread_b node position = 2)
3) Any thread that is running or completed must be mapped to a destination thread
4) Any Node within Wf_runA with a status of completed must be mapped to another node within wfSpec_b
5) Edge Condition and variable Mutations amongst interior `wfRun nodes`(along the path of execution) must be equivalent.


The new `wfRun` will be incrementally built. After each .advance() call a subset of the above criteria will be checked. If a wfRun violates this criteria it will be disregarded and continue on its previous path under the original wfSpec. 








