Author: Jake Rose

## Motivation

We want to add comments to userTasks so a user can add additional notes

* Allow users to provide better feedback/Documentation on a User Task 

In the current implementation of UserTask we can add a note, but there is no way to add or remove additional notes 


## Changes in the schemas

In order to store details about a comment made on a `userTaskRun` we need to create a UserCommentRequest object that is created by the LH rpc api

```proto
// This is the object storing the comment details of a `UserTaskRun`
message UserComment {

  // This is the id of the user 
  string user_id = 4;

  // This is the specific note/comment that a user wants to make 
  // on a `UserTask`
  string content = 5;

}
```


```proto
message putUserCommentRequest{
  
  // The author of the comment being made on a UserTaskRun
  string user_id = 4;

  // The comment being made on a UserTaskRun
  string content = 5; 
}


```

```proto
Service Littlehorse {
    // ...

    // Put the UserComment to correlating UserTask
    rpc CommentUserTaskRun(PutUserCommentRequest) returns (google.protobuf.Empty) {}

 // ...
}
```





