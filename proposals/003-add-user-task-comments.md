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
  // Unique Identifier for a UserComment/ a 
  string user_comment_id = 1 ;

  string subject = 2 ;

  // This is the id of the associated `UserTaskRun`
  UserTaskRunId user_task_run_id = 3;

  // This is the id of the user 
  string user_id = 4;

  // This is the specific note/comment that a user wants to make 
  // on a `UserTask`
  string content = 5;

  // The time at which this specific comment was created
  google.protobuf.Timestamp created_at = 6;

  // The time at which this comment was updated
  optional google.protobuf.Timestamp updated_at = 7;

  // The time at which this comment was deleted
  optional google.protobuf.Timestamp deleted_at = 8;
}
```

This is adding a repeated field in the userTaskRun object which allows a user to create, update, and delete multiple comments

```proto
message UserTaskRun {
    ...
    // List of comments about the userTaskRun
    repeated UserComment user_comments = 13 ;
}
```


```proto
message putUserCommentRequest{

  // The id/name of comment being posted on a userTaskRun
  string user_comment_id = 1;

  // The subject or title of a UserComment
  string subject = 2

  // The correlated UserTaskRunId to a given UserComment
  UserTaskRunId user_task_run_id = 3;

  // The author of the comment being made on a UserTaskRun
  string user_id = 4;

  // The comment being made on a UserTaskRun
  string content = 5; 
}


message deleteUserCommentRequest{
  // The correlated userTaskRunId of to the UserComment you want to delete
  UserTaskRunId user_task_run_id = 2;

  // The Id of the comment you would like to delete
  string user_comment_id = 1;
}


mesage searchUserCommentRequest{
  // The correlated UserTaskRunId to a given UserComment
  UserTaskRunId user_task_run_id = 1;

  // The author of the UserComments you are searching
  optional string user_id = 2;
}

message getUserCommentRequest {
  // The correlated UserTaskRunId to a given UserComment
  UserTaskRunId user_task_run_id = 1;

  // The Id of the UserComment
  string user_comment_id = 2;
}


message UserCommentIdList{
  // A list of comment ids will be returned
  repeated string user_comment_ids = 1

}
```

There will be two ways to search for userComments through either a `userTaskRun` or `userComment`


`lhctl search userTaskRun userTaskRunid --userCommentId `

`lhctl get userComment userTaskRunId  userCommentId`

`lhctl search userComment UserTaskRunid --subject --userid --createdDate `

`lhctl put  userComment UserTaskRunid  userId subject content `

`lchtl delete userComment UserTaskRunid userCommentId`


```proto
Service Littlehorse {
    // ...

    // Put the UserComment to correlating UserTask
    rpc PutUserComment(PutUserCommentRequest) returns (UserComment) {}

    // Delete a User comment that belongs to s userTask
    rpc DeleteUserComment(DeleteUserCommentRequest) returns (Google.protobuf.Empty) {}

    // search for user comments belonging to a certain author
    rpc SearchUserComment(SearchUserCommentRequest) returns (UserCommentIdList) {}

    // Get a UserComment from a correlated User Task
    rpc GetUserComment(GetUserCommentRequest) returns (UserComment) {}

 // ...
}
```





