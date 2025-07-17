# Event Registration Example

This example demonstrates how to use the LittleHorse SDK to create workflows and tasks, with a focus on automatic event registration using the `registered_as` method.

## Features

- **Automatic External Event Registration**: The `wait_for_event` method is used to wait for an external event (`EXT_EVENT`) and automatically registers the event definition with a payload type of `str` using `registered_as`.
- **Automatic Workflow Event Registration**: The `throw_event` method is used to throw a workflow event (`THROW_EVENT`) and automatically registers the event definition with a payload type of `str` using `registered_as`.

## Workflow Overview

The workflow consists of the following steps:
1. Execute the `ASK_FOR_NAME` task to prompt the user for their name.
2. Wait for the external event `EXT_EVENT` and assign its payload to the `name` variable.
3. Execute the `GREET` task to greet the user using their name.
4. Throw the workflow event `THROW_EVENT` with the user's name as the payload.

## Code Highlights

### External Event Registration
```python
ext_event_output = wf.wait_for_event(EXT_EVENT, timeout=60).registered_as(payload_type=str)
```
The `wait_for_event` method waits for the external event `EXT_EVENT` and registers it with a payload type of `str`.

### Workflow Event Registration
```python
wf.throw_event(THROW_EVENT, name).registered_as(payload_type=str)
```
The `throw_event` method throws the workflow event `THROW_EVENT` and registers it with a payload type of `str`.

## Running the Example

1. Ensure you have the LittleHorse SDK installed and configured.
2. Run the example script:
   ```bash
   poetry shell 
   python example_event_registration.py
   ```

## Conclusion

This example showcases the power of automatic event registration in LittleHorse workflows, simplifying the process of defining and using