# Checkpoint Task Example

This example demonstrates the use of `WorkerContext.ExecuteAndCheckpoint()` to create checkpoints during task execution. Checkpoints allow expensive operations to be saved and restored on task retries, preventing redundant work.

## How It Works

The `GreetWithCheckpoints` task function creates two checkpoints:

1. **First Checkpoint**: Creates a greeting message and logs it. This operation only runs once.
2. **Second Checkpoint**: Adds additional text to the result. This only runs on the retry after the first checkpoint is restored.

On the first attempt (attempt 0), the task deliberately fails after the first checkpoint. On the retry (attempt 1), the task:
- Retrieves the saved first checkpoint instead of re-executing it
- Successfully executes the second checkpoint
- Completes successfully

## Running the Example

1. Start the LittleHorse server

2. Deploy the workflow:
```bash
go run ./examples/go/checkpoint/deploy
```

3. Start the worker:
```bash
go run ./examples/go/checkpoint/worker
```

4. Run the workflow:
```bash
lhctl run checkpoint-example name World
```

## What to Observe

When you run this example, you'll see:

1. **First Attempt (Attempt 0)**:
   - "Hello from task worker on attempt 0 before the checkpoint"
   - "Hello from task worker on attempt 0 in the first checkpoint"
   - "Hello from after the first checkpoint"
   - Task fails with error message

2. **Retry (Attempt 1)**:
   - "Hello from task worker on attempt 1 before the checkpoint"
   - Notice: The first checkpoint code is NOT executed
   - "Hello from after the first checkpoint"
   - "Hi from inside the second checkpoint"
   - "Hi from after the checkpoints on attemptNumber 1"
   - Task completes successfully

The key observation is that on the retry, the first checkpoint's logic is skipped because its result was already saved to the server.

## Checkpoint Logs

Checkpoints can have their own logs using `CheckpointContext.Log()`. These logs are stored with the checkpoint and can be viewed in the LittleHorse dashboard.

## Use Cases

Checkpoints are useful for:
- Long-running AI agent tasks with multiple API calls
- Multi-step document processing
- Expensive database queries or transformations
- Image/video processing pipelines
- Any task where partial progress should be preserved across retries

## Checkpoint Lifecycle

Checkpoints are stored on the LittleHorse server and associated with the specific TaskRun:

- **On Success**: When a task completes successfully, all its checkpoint data remains available for auditing and debugging through the dashboard
- **On Permanent Failure**: If a task fails after all retries are exhausted, checkpoint data is retained to help diagnose what work was completed before the final failure
- **Cleanup**: Checkpoint data follows the same retention policies as other TaskRun data in LittleHorse
- **Persistence**: Checkpoints persist across task worker restarts and can be inspected via the dashboard or API
