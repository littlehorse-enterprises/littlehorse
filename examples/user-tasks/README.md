## User Tasks

User Tasks are a type of `Node` in LittleHorse which allow you to assign a task (in this case, filling out a form) to a human. User Tasks have the following features:

- Can be assigned to a specific User ID or a User Group.
- Produce output that can be saved into a Workflow Run `Variable` and used elsewhere in the `WfRun`.
- Reminder Tasks are supported, such that a regular `TaskRun` is executed some period of time after the User Task is scheduled (if the User Task has not been completed yet). This is not included in this example.
- Can include notes which are presented to the person executing the User Task. Notes are unique for each instance of the Workflow.

For more information about User Tasks, please consult the [User Task Documentation](https://littlehorse.io/docs/server/concepts/user-tasks) on our website.

### The Business Logic

This example mimics a common Corporate workflow in which some employee requests an item from the IT Department, and the Finance Department must approve the purchase request first. The steps of the Workflow are roughly as follows:

* A User Task is assigned to the `user-id` who initiated the `WfRun`, which involves filling out a description of the requested item and a justification.
* A User Task is assigned to the `finance` User Group. The User Task contains notes from the output of the first User Task, and has one field: a `boolean` which determines whether the purchase is approved.
* If the purchase is approved, the requester is notified of approval.

### Running the Example

Let's take it slow and run the exmaple step-by-step so that we can see exactly what's going on.

**As a PreRequisite, get a running copy of the LittleHorse Server, as in the [Java Quickstart](../../docs/QUICKSTART_JAVA.md).**

#### Deploy It

First, run the `UserTasksExample.java` application to:
* Deploy your `TaskDef`, `UserTaskDef`s, and `WfSpec`
* Start the `LHTaskWorker` which sends fake "emails".


```
./gradlew example-user-tasks:run
```

#### Start the Workflow

In another terminal, use `lhctl` to run the workflow. Note that we set the initial `user-id` of the person requesting the new item to `anakin`.

```
lhctl run it-request user-id anakin
```

We check the status of the `WfRun` and see that it's running:
```
lhctl get wfRun <wf_run_id>
```

Note that there is only one `ThreadRun` in the `WfRun`, and the current `NodeRun` position is `1`. If you recall our `WfSpec`, we've arrived at a User Task Run, and it should be assigned to `anakin`, but if the assigned user does not complete the task in less than 1 minute, then the task will be released to `testGroup` group.

#### Find the User Task

There are two general ways to find the User Task Run that is assigned to `anakin`
1. Using the `SearchUserTaskRun` rpc, for example via `lhctl search userTaskRun`
2. Looking at the `NodeRun` to get the `UserTaskRunId`.

In most production use-cases, we would use option `1` since there would be a frontend that someone logs into which displays tasks assigned to them. Any of the following commands should work to find the User Task Run Id (note that it is a composite ID consisting of `wfRunId` and `userTaskGuid`).

```
lhctl search userTaskRun --userId anakin

lhctl search userTaskRun --userId anakin --userTaskStatus ASSIGNED

lhctl search userTaskRun --userTaskStatus ASSIGNED --userTaskDefName it-request
```

The commands behave roughly like they sound.

The second option to find the UserTaskRun's ID is to check the `NodeRun`. Recall that there is only one `ThreadRun` (with number `0`), and that `ThreadRun` is on `NodeRun` 1. We can get tne `NodeRun` as follows:

```
# provide wfRunId, threadRun number, and nodeRun position
lhctl get nodeRun <wfRunId> 0 1
```

You should see in `$.result.userTask.userTaskRunId` the same ID that resulted from all of the searches above.

#### Execute the User Task Run

Now that we have the `userTaskGuid`, we can use `lhctl` to execute the User Task Run. But first, let's inspect the `userTaskRun`:

```
lhctl get userTaskRun <wfRunId> <userTaskGuid>
```

Note that its status is `CLAIMED` and it's assigned to `anakin`.

_Note that in production, there would be a web frontend that users log in to in order to execute the User Task Runs. The LittleHorse server tracks the state of these User Tasks (including whom they are assigned to) but does not present them on a web front-end. This is because each user would likely need a highly-customized presentation of the tasks, such as on their mobile-app, internal tooling, customer-facing web app, etc. If you wish for a custom web front-end, please contact LittleHorse Professional Services (`sales@littlehorse.io`)._

Let's execute the task:

```
lhctl execute userTaskRun <wfRunId> <userTaskGuid>
```

Follow the prompts, entering your user-id (be sure to enter `anakin`), the item `description`, and the `justification`. For example:

```
->lhctl execute userTaskRun 89962fbd15e748358f2df1c130b34403 4579d4bd166d4156bda49042b10ad7bb

Executing UserTaskRun  89962fbd15e748358f2df1c130b34403   4579d4bd166d4156bda49042b10ad7bb
Enter the userId of the person completing the task: anakin

Field:  Your Request
The item you are requesting.
Please enter the response for this field (STR): the rank of master

Field:  Request Justification
Why you need this request.
Please enter the response for this field (STR): it's not fair to be on this council and not be a Master!
Saving userTaskRun progress!
{}
```

Now let's get the `userTaskRun` again:

```
lhctl get userTaskRun <wfRunId> <userTaskGuid>
```

It's now `DONE`! And we can see the results: Anakin is requesting the Rank of Jedi Master. Note that the searches are also updated:

```
# Doesn't show our User Task Run from before
lhctl search userTaskRun --userId anakin --userTaskStatus ASSIGNED

# DOES show the User Task Run
lhctl search userTaskRun --userId anakin --userTaskStatus DONE
```

#### Execute the Next User Task

Now let's check back on our `WfRun`.

```
lhctl get wfRun <wfRunId>
```

It's now on `NodeRun` with position `2`! That makes sense. It's that `UserTaskRun` that's assigned to the `finance` department. Let's find the ID:

```
lhctl search userTaskRun --userGroup finance --userTaskStatus UNASSIGNED
```

Now let's inspect the UserTaskRun again (use the new `userTaskGuid` from the search we just ran):

```
`lhctl get userTaskRun` <wfRunId> <userTaskGuid>
```

Note that `userId` is not set, but `userGroup` is set to `finance`. Let's assign it to `mace` (because we know Mace Windu and Anakin are besties).


```
lhctl assign userTaskRun <wfRunId> <userTaskGuid> --userId 'mace'
```

Now look at the `UserTaskRun` and note its status:

```
->lhctl get userTaskRun <wfRunId> <userTaskGuid>
->lhctl get userTaskRun b1810249cef64555ad5bb34534132477 af50671a0c904b008d9bfd9ed92c6df3
{
  "id":  {
    "wfRunId":  "b1810249cef64555ad5bb34534132477",
    "userTaskGuid":  "af50671a0c904b008d9bfd9ed92c6df3"
  },
  "userTaskDefId":  {
    "name":  "approve-it-request",
    "version":  0
  },
  "results":  [],
  "status":  "ASSIGNED",
  "events":  [
    {
      "time":  "2023-08-30T19:24:52.866Z",
      "reassigned":  {
        "newUserGroup":  {
          "id":  "finance"
        }
      }
    },
    {
      "time":  "2023-08-30T19:24:55.715Z",
      "taskExecuted":  {
        "taskRun":  {
          "wfRunId":  "b1810249cef64555ad5bb34534132477",
          "taskGuid":  "3514a3d59d70463393fb71ce106adc2e"
        }
      }
    },
    {
      "time":  "2023-08-30T19:29:30.412Z",
      "reassigned":  {
        "newUser":  {
          "id":  "mace",
          "userGroup":  {
            "id":  "finance"
          }
        }
      }
    }
  ],
  "notes":  "User anakin is requesting to buy item qwe.\nJustification: asf",
  "scheduledTime":  "2023-08-30T19:24:52.847Z",
  "nodeRunId":  {
    "wfRunId":  "b1810249cef64555ad5bb34534132477",
    "threadRunNumber":  0,
    "position":  2
  },
  "user":  {
    "id":  "mace",
    "userGroup":  {
      "id":  "finance"
    }
  }
}
```

It's now `ASSIGNED`! And assigned to `mace`. Also, notice the `.result.notes` field.

Let's execute the `UserTaskRun`.

```
-> lhctl execute userTaskRun <wfRunId> <userTaskGuid>

```

Now depending on whether you typed `true` or `false` (if you know Star Wars, you know that the correct answer is `false`), you should see some output in the logs of the process `./gradlew example-user-tasks:run`.
