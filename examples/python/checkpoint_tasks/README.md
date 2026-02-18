## Running Checkpointed Task Example

This is a simple example which shows the usage of checkpoints in tasks.

Let's run the example:

```
poetry shell
python -m example_checkpoint_tasks
```

In another terminal, use `lhctl` to run the workflow:

```bash
lhctl run example-checkpointed-tasks input-name "Qui-Gon Jinn"
```

Observe the task worker output to see what's going on.
