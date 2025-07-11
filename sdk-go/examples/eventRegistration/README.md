# Event Registration Example

This example demonstrates a workflow that waits for an external event and processes it. The main features include registering the workflow specification (`RegisterWfSpec`) and handling external events using `RegisteredAs`.

---

## **Start the Task Worker**

The workflow uses task definitions (`TaskDef`) with associated task functions. To start the workers, run:

```bash
go run ./examples/eventRegistration/worker
```

---

## **Register the Workflow Specification**

The workflow specification (`WfSpec`) and event definitions are registered using the deploy script. This includes external event definitions and workflow event definitions.

Run the following command:

```bash
go run ./examples/eventRegistration/deploy
```

This will:
1. Register the external event definitions.
2. Register the workflow event definitions.
3. Compile and register the workflow specification using `wf.RegisterWfSpec(*client)`.

---

## **Run a Workflow Instance (`WfRun`)**

To start a workflow instance (`WfRun`), use the following command:

```bash
lhctl run workflow-and-external-event-registration
```

Take note of the `wfRunId` returned by the command. You can check the status of the workflow instance using:

```bash
lhctl get wfRun <wfRunId>
```

The status should be `RUNNING`, as the workflow is waiting for an external event.

---

## **Send the External Event**

Once the workflow is running, send the external event to the workflow instance using:

```bash
lhctl postEvent <wfRunId> my-name STR obi-wan
```

This sends the `ExternalEvent` (`my-name`) with a payload of type `STR` and value `obi-wan`. The payload type is specified using `.RegisteredAs(lhproto.VariableType_STR)`.

---

## **Check Workflow Completion**

After sending the event, check the status of the workflow instance again:

```bash
lhctl get wfRun <wfRunId>
```

The workflow should now be `COMPLETED`, as it has processed the external event and finished execution.

---

## **Summary**

This example demonstrates:
1. Declaring and registering external and workflow event definitions.
2. Waiting for an external event in a workflow.
3. Sending the external event to the workflow instance.
4. Completing the workflow after processing the event.

---

## **Key Concepts**

1. **RegisteredAs**:
   - This method is used to associate a payload type with an external event or workflow event.
   - In the workflow, `RegisteredAs` is used to specify the type of data expected from the external event (`STR` in this case).

2. **RegisterWfSpec**:
   - This method is used to register the workflow specification (`WfSpec`) along with external and workflow event definitions.
   - It ensures that the workflow and its associated events are properly defined and available for execution.
