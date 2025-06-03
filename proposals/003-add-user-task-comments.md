Author: Jake Rose

## Motivation

We want to add comments to userTasks so a user can add additional notes

* Allow users to provide better feedback/Documentation on a User Task 

In the current implementation of UserTask we can add a note, but there is no way to add or remove additional notes 


## Changes in the schemas

In order to store details about a comment made on a `userTaskRun` we need to create a `CommentUserTaskRunRequest` object that is created by the LH rpc api

### Option 1: Add comments through UserTaskRunEvents

The first option is to create a new type of UserTaskRunEvent called `UTEComment` that will be used to store comment details.

```proto
// This is the object storing the comment details of a `UserTaskRun`
message UTEComment {
  // The id of the user comment
  string user_comment_id = 1;

  // This is the id of the user 
  string user_id = 2;

  // This is the specific note/comment that a user wants to make 
  // on a `UserTask`
  string comment = 3;

  // Flag representing if this comment has been edited 
  boolean is_edited = 4; 

  // Flag representing if this comment has been deleted 
  boolean is_deleted = 5
}

message UTEDeleteComment {
  // The id of the comment that will be deleted
  string user_comment_id = 1;

}
```

```proto
message CommentUserTaskRunRequest{
  // The id of UserTaskRun to save.
  UserTaskRunId user_task_run_id = 1;

  // The author of the comment being made on a UserTaskRun
  string user_id = 2;

  // The comment being made on a UserTaskRun
  string comment = 3;

  // This the id of a individual user comment , if it is passed in the
  // the corresponding comment will be updated and the flag is_edited will be set to true 
  optional string user_comment_id = 4 ;

}

// When this request is called the flag is_deleted will change to true 
message DeleteCommentUserTaskRunRequest{
  string user_comment_id = 1;
}

```

```proto
  oneof event {
    // ...
    UTEComment commented = 6;
    UTEDeleteComment comment_deleted = 7;
  }
```

```proto
Service Littlehorse {
    // ...

    // Put the UserComment to correlating UserTask
    rpc CommentUserTaskRun(CommentUserTaskRunRequest) returns (CommentUserTaskRun) {};
    rpc DeleteCommentUserTaskRun(DeleteCommentUserTaskRunRequest) returns {google.protobuf.Empty}

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
        DeleteCommentUserTaskRunRequest delete_comment_user_task_run = 29;
    }
}

```

To list the comments for a `UserTaskRun`, we can use the `lhctl`. This will return the `UserTaskRun` object with the comments included in the `events` field.

```bash
`lhctl get userTaskRun <wfRunId> <userTaskGuid>`.
```

List of events in the `UserTaskRun` object will look like this:

```json
{
  "events": [
    {
      "time": "2025-06-02T15:58:14.686Z",
      "assigned": {
        "newUserId": "anakin",
        "newUserGroup": "testGroup"
      }
    },
    {
      "time": "2025-06-02T15:59:14.710Z",
      "commented": {
        "userCommentId": "4db2eff5-74a9-464a-be1b-2e22e341a086",
        "userId": "anakin",
        "comment": "This is a test comment",
        "isEdited": false
      }
    },
    {
      "time": "2025-06-02T16:59:14.710Z",
      "assigned": {
        "oldUserId": "anakin",
        "oldUserGroup": "testGroup",
        "newUserGroup": "test2Group"
      }
    },
    {
      "time": "2025-06-02T17:00:01.710Z",
      "commented": {
        "userCommentId": "d46abb2d-96bf-4e16-a9b8-6966b8686e5e",
        "userId": "mace",
        "comment": "This is other test comment",
        "isEdited": true
      }
    },
    {
      "time": "2025-06-02T17:40:14.710Z",
      "comment_deleted": {
        "user_comment_id": "4db2eff5-74a9-464a-be1b-2e22e341a086"
      }
    }
  ]
}
```

### Option 2: Add comments through a list of new objects in the UserTaskRun
It is possible to add comments through a new object which can be added as a repeated field in the `UserTaskRun` object. 
This allows for more flexibility in managing comments, as they can be created, updated, and deleted independently of the `UserTaskRunEvents`.

```proto
// This is the object storing the comment details of a `UserTaskRun`
message UserComment {
  // Unique Identifier for a UserComment/ a 
  string user_comment_id = 1 ;
  
  // This is the id of the associated `UserTaskRun`
  UserTaskRunId user_task_run_id = 2;
  
  // This is the id of the user 
  string user_id = 3;
  
  string subject = 4;
  
  // This is the specific note/comment that a user wants to make 
  // on a `UserTask`
  string content = 5;
  
  // The time at which this specific comment was created
  google.protobuf.Timestamp created_at = 6;
  
  // The time at which this comment was updated
  optional google.protobuf.Timestamp updated_at = 7;
}
```

This is adding a repeated field in the `userTaskRun` object which allows a user to create, update, and delete multiple comments

```proto
message UserTaskRun {
    ...
    // List of comments about the userTaskRun
    repeated UserComment user_comments = 13 ;
}
```


```proto
message PutUserCommentRequest{
  // The subject or title of a UserComment
  string subject = 1;
  
  // The correlated UserTaskRunId to a given UserComment
  UserTaskRunId user_task_run_id = 2;
  
  // The author of the comment being made on a UserTaskRun
  string user_id = 3;
  
  // The comment being made on a UserTaskRun
  string content = 4; 
}

message DeleteUserCommentRequest{
  // The Id of the comment you would like to delete
  string user_comment_id = 2;
}

mesage SearchUserCommentRequest{
  // The correlated UserTaskRunId to a given UserComment
  UserTaskRunId user_task_run_id = 1;
  
  // The author of the UserComments you are searching
  optional string user_id = 2;
  
  // The subject of the UserComments you are searching
  optional string subject = 3;
}

message ListUserCommentRequest{
  // The UserTaskRunId to list the comments for the UserTaskRun
   UserTaskRunId user_task_run_id = 1;
}
```

There will be two ways to search for userComments through either a `userTaskRun` or `userComment`

`lhctl get userTaskRun <wfRunId> <userTaskGuid>`

`lhctl search userComment <userTaskGuid> --userid --subject --createdDate `

`lhctl put userComment <userTaskGuid> <userId> <subject> <content> `

`lchtl delete userComment <userCommentId>`


```proto
Service Littlehorse {
    // ...
    // Put the UserComment to correlating UserTask
    rpc PutUserComment(PutUserCommentRequest) returns (UserComment) {}
    // Delete a User comment that belongs to s userTask
    rpc DeleteUserComment(DeleteUserCommentRequest) returns (Google.protobuf.Empty) {}
    // search for user comments belonging to a certain author, created at a certain time or subject
    rpc SearchUserComment(SearchUserCommentRequest) returns (UserCommentList) {}
    // List user comments from a correlated User Task
    rpc ListUserComments(GetUserCommentRequest) returns (UserCommentList) {}
 // ...
}
```


