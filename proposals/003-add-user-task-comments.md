Author: Jake Rose

## Motivation

We want to add comments to userTasks so a user can add additional notes

* Allow users to provide better feedback/Documentation on a User Task 

In the current implementation of UserTask we can add a note, but there is no way to add or remove additional notes 


## Changes in the schemas

In order to store details about a comment made on a `userTaskRun` we need to create a UserCommentRequest object that is created by the LH rpc api

```proto
// This is the object storing the comment details of a `UserTaskRun`
message UTEComment {

  // This is the id of the user 
  string user_id = 1;

  // This is the specific note/comment that a user wants to make 
  // on a `UserTask`
  string comment = 2;
}
```


```proto
message CommentUserTaskRunRequest{
  // The id of UserTaskRun to save.
  UserTaskRunId user_task_run_id = 1;

  // The author of the comment being made on a UserTaskRun
  string user_id = 1;

  // The comment being made on a UserTaskRun
  string comment = 2;
}

```

```proto
  oneof event {
    // ...
    UTECommented commented = 6;
  }
```

```proto
Service Littlehorse {
    // ...

    // Put the UserComment to correlating UserTask
    rpc CommentUserTaskRun(CommentUserTaskRunRequest) returns (google.protobuf.Empty) {};

 // ...
}
```
To enable the command to process a comment in the userTaskRun, it is necessary to add a new command request field

```proto
message Command {
    // ...

    oneof command {
        // ...
        CommentUserTaskRunRequest comment_user_task_run = 28;
    }
}

```

## Server
To use proto objects, it is necessary to create a `UTECommentedModel` class which allows initializing a proto message into a
usable java object that can represent a `UserTaskComment` within a `UserTaskRun`. 

In the `LHServerListener.class`, it is necessary to add a method to process the request `commentUserTaskRun`



