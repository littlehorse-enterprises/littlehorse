# Event Registration Example

This example demonstrates how to use the LittleHorse SDK to create workflows and tasks, with a focus on automatic event registration using the `auto_register`, `return_type`, and `correlated_event_config` parameters.

## Features

- **Automatic External Event Registration**: The `wait_for_event` method automatically registers external event definitions when `auto_register=True`, `return_type` is specified, or `correlated_event_config` is provided.
- **Automatic Workflow Event Registration**: The `throw_event` method automatically registers workflow event definitions when `return_type` is specified.
- **Correlated Events**: Demonstrates how to use correlation IDs to link events and configure event correlation behavior.

## Workflow Overview

The workflow consists of the following steps:
1. Wait for the external event `what-is-your-name` to get the user's name.
2. Execute the `greet` task to greet the user and generate a unique greeting ID.
3. Wait for the external event `how-old-are-you` with correlation to the greeting ID to get the user's age.
4. Wait for the external event `allow-summary` (correlated) to proceed with summary generation.
5. Execute the `do-summary` task to create a summary of the user's information.
6. Throw the workflow event `post-summary` with the summary as payload.

## Code Highlights

### External Event Registration with Auto-Register
```python
name = wf.wait_for_event(ASK_FOR_NAME_EVENT, 
                         timeout=60, 
                         auto_register=True, 
                         return_type=str)
```
The `wait_for_event` method waits for the external event and registers it with a payload type of `str` using `auto_register=True` and `return_type=str`.

### Correlated External Event Registration
```python
age = wf.wait_for_event(ASK_FOR_AGE_EVENT, 
                        timeout=60, 
                        correlation_id=greet_id, 
                        return_type=int,
                        correlated_event_config=CorrelatedEventConfig(
                            delete_after_first_correlation=True,
                        ))
```
This demonstrates correlated events where the age event is linked to the greeting ID, with automatic cleanup after correlation.

### Simple Auto-Registration
```python
wf.wait_for_event(ALLOW_SUMMARY_TASK, 
                  correlation_id=greet_id, 
                  auto_register=True)
```
Events can be auto-registered without specifying a return type by setting `auto_register=True`.

### Workflow Event Registration
```python
wf.throw_event(THROW_EVENT, summary, return_type=str)
```
The `throw_event` method throws a workflow event and automatically registers it with a payload type of `str`.

## Auto-Registration Behavior

The LittleHorse SDK automatically registers event definitions when:

### For External Events (`wait_for_event`):
- `auto_register=True` is explicitly set, OR
- `return_type` is specified, OR
- `correlated_event_config` is provided

### For Workflow Events (`throw_event`):
- `return_type` is specified

When any of these conditions are met, the corresponding event definition (ExternalEventDef or WorkflowEventDef) is automatically registered with the workflow.

## Running the Example

1. Ensure you have the LittleHorse SDK installed and configured.
2. Run the example script:
   ```bash
   poetry shell 
   python example_event_registration.py
   ```

## Key Components

- **Task Functions**: `greet` and `show_summary` are async functions that handle the business logic
- **Event Correlation**: Uses correlation IDs to link related events
- **Automatic Registration**: Events are registered automatically based on the parameters provided
- **Type Safety**: Return types are specified for proper payload handling

## Conclusion

This example showcases the power of automatic event registration in LittleHorse workflows, demonstrating how to:
- Automatically register external and workflow events
- Use correlation IDs to link related events
- Configure event correlation behavior
- Handle different payload types (str, int)
- Simplify workflow development by eliminating manual event definition registration