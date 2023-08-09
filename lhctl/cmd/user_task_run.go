package cmd

import (
	"bufio"
	"context"
	"errors"
	"fmt"
	"log"
	"os"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

var executeUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Execute a UserTaskRun at the CLI",
	Long: `Given a provided wfRunId and userTaskGuid, this utility prompts you
for your userId and then prompts you to fill out each required field of the
UserTaskRun. At the end, the UserTaskRun is submitted`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("You must provide the wfRunId and userTaskGuid")
		}
		client := getGlobalClient(cmd)
		executeUserTask(args[0], args[1], &client)
	},
}

var assignUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid> [options]",
	Short: "Reassign a UserTaskRun to a userGroup or specific userId",
	Long: `Given a provided wfRunId and UserTaskGuid, this utility allows you
to reassign the specified UserTaskRun.

The --overrideClaim option allows you to override an assignment if the UserTaskRun
is already claimedby a specific UserId.

The following option groups are supported:
[userId] -> assign to a specific userId.
[userGroup] -> assign to a userGroup of users
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("Must provide two arguments: wfRunId, userTaskGuid")
		}
		overrideClaim, _ := cmd.Flags().GetBool("overrideClaim")

		userId, _ := cmd.Flags().GetString("userId")
		userGroup, _ := cmd.Flags().GetString("userGroup")
		// userId and userGroup can't both be non-empty because the flags are
		// mutually exclusive.

		reassign := &model.AssignUserTaskRunPb{
			UserTaskRunId: &model.UserTaskRunIdPb{
				WfRunId:      args[0],
				UserTaskGuid: args[1],
			},
			OverrideClaim: overrideClaim,
		}

		if userId != "" {
			var user = &model.UserPb{
				Id: userId,
			}
			reassign.Assignee = &model.AssignUserTaskRunPb_User{
				User: user,
			}
		} else if userGroup != "" {
			var userGroupPb = &model.UserGroupPb{
				Id: userGroup,
			}
			reassign.Assignee = &model.AssignUserTaskRunPb_UserGroup{
				UserGroup: userGroupPb,
			}
		} else {
			log.Fatal("Must specify either --userId or --userGroup")
		}

		common.PrintResp(getGlobalClient(cmd).AssignUserTaskRun(
			context.Background(),
			reassign,
		))
	},
}

// getUserTaskRunCmd represents the nodeRun command
var getUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Get a UserTaskRun by WfRun, ThreadRun, and UserTask Run Position",
	Long: `UserTaskRun's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- ThreadRun Number
	- UserTaskRun Number (i.e. chronological position within the ThreadRun)

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.`,
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide exactly 1 or 2 arguments. See 'lhctl get userTaskRun -h'")

		}

		wfRunId, userTaskGuid := args[0], args[1]

		common.PrintResp(getGlobalClient(cmd).GetUserTaskRun(
			context.Background(),
			&model.UserTaskRunIdPb{
				WfRunId:      wfRunId,
				UserTaskGuid: userTaskGuid,
			},
		))
	},
}

var searchUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun",
	Short: "Search for UserTaskRun's either by WfRunId or by {takDefId,Status}",
	Long: `
Search for UserTaskRun's by either the WfRunId or by providing the taskDefName and the Status.

Returns a list of ObjectId's that can be passed into 'lhctl get nodeRun'.

Choose one of the following option groups:
// Returns all UserTaskRun's from a specified WfRun.
[wfRunId]

// For user task search. Use any combination of the following, except note
// that userId and userGroup are mutually exclusive.
[userTaskDefName, userTaskStatus, userId, userGroup]

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
  options with any group except [--wfRunId] to put a time bound on WfRun's
  which are returned. The time bound applies to the time that the WfRun was
  created.
`,
	Run: func(cmd *cobra.Command, args []string) {

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &model.SearchUserTaskRunPb{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
		}

		statusStr, _ := cmd.Flags().GetString("userTaskStatus")
		if statusStr != "" {
			statusInt, ok := model.UserTaskRunStatusPb_value[statusStr]
			if !ok {
				log.Fatal("Invalid value provided for userTaskStatus")
			}
			statusVal := model.UserTaskRunStatusPb(statusInt)
			search.Status = &statusVal
		}

		userIdStr, _ := cmd.Flags().GetString("userId")
		userGroupStr, _ := cmd.Flags().GetString("userGroup")
		if userIdStr != "" {
			if userGroupStr != "" {
				var userGroup = &model.UserGroupPb{
					Id: userGroupStr,
				}
				search.TaskOwner = &model.SearchUserTaskRunPb_User{
					User: &model.UserPb{
						Id:        userIdStr,
						UserGroup: userGroup,
					},
				}
			} else {
				search.TaskOwner = &model.SearchUserTaskRunPb_User{
					User: &model.UserPb{
						Id: userIdStr,
					},
				}
			}

		}

		if userGroupStr != "" {
			var userGroupPb = &model.UserGroupPb{
				Id: userGroupStr,
			}
			search.TaskOwner = &model.SearchUserTaskRunPb_UserGroup{
				UserGroup: userGroupPb,
			}
		}

		userTaskDefNameStr, _ := cmd.Flags().GetString("userTaskDefName")
		if userTaskDefNameStr != "" {
			search.UserTaskDefName = &userTaskDefNameStr
		}

		search.Bookmark = bookmark
		search.Limit = &limit

		common.PrintResp(getGlobalClient(cmd).SearchUserTaskRun(context.Background(), search))

	},
}

func executeUserTask(wfRunId string, userTaskGuid string, client *model.LHPublicApiClient) {
	fmt.Println("Executing UserTaskRun ", wfRunId, " ", userTaskGuid)

	completeUserTask := &model.CompleteUserTaskRunPb{
		Result: &model.UserTaskResultPb{
			Fields: make([]*model.UserTaskFieldResultPb, 0),
		},
		UserTaskRunId: &model.UserTaskRunIdPb{
			WfRunId:      wfRunId,
			UserTaskGuid: userTaskGuid,
		},
	}

	// First, get the UserTaskRun.
	userTaskRun, err := getUserTaskRun(wfRunId, userTaskGuid, client)
	if err != nil {
		log.Fatal(err)
	}

	if userTaskRun.Notes != nil {
		fmt.Println("\nNotes: " + *userTaskRun.Notes + "\n")
	}

	// Next, get the UserTaskDef.
	userTaskDef, err := getUserTaskDef(userTaskRun, client)
	if err != nil {
		log.Fatal(err)
	}

	// Next, prompt for the userId.
	userIdVarVal, err := promptFor(
		"Enter the userId of the person completing the task",
		model.VariableTypePb_STR,
	)
	if err != nil {
		log.Fatal(err)
	}
	completeUserTask.UserId = *userIdVarVal.Str

	// Prompt for all of the fields and build the result
	for _, field := range userTaskDef.Fields {
		fmt.Println("\nField: ", field.DisplayName)
		if field.Description != nil {
			fmt.Println(*field.Description)
		}
		resultVal, err := promptFor("Please enter the response for this field ("+field.Type.String()+")", field.Type)
		if err != nil {
			log.Fatal(err)
		}
		completeUserTask.Result.Fields = append(completeUserTask.Result.Fields,
			&model.UserTaskFieldResultPb{
				Name:  field.Name,
				Value: resultVal,
			},
		)
	}

	fmt.Println("completing userTaskRun!")
	// Post the result
	common.PrintResp(
		(*client).CompleteUserTaskRun(context.Background(), completeUserTask),
	)
}

func promptFor(prompt string, varType model.VariableTypePb) (*model.VariableValuePb, error) {
	fmt.Print(prompt + ": ")
	// Create a new buffered reader to read from standard input
	reader := bufio.NewReader(os.Stdin)

	// Read the entire line of text entered by the user
	// The returned line will include the newline character '\n', so we'll trim it.
	userInput, _ := reader.ReadString('\n')
	return common.StrToVarVal(userInput[:len(userInput)-1], varType)
}

func getUserTaskDef(userTaskRun *model.UserTaskRunPb, client *model.LHPublicApiClient) (*model.UserTaskDefPb, error) {
	resp, err := (*client).GetUserTaskDef(context.Background(), userTaskRun.UserTaskDefId)
	if err != nil {
		return nil, err
	}
	if resp.Code != model.LHResponseCodePb_OK {
		return nil, errors.New(*resp.Message)
	}
	return resp.Result, nil
}

func getUserTaskRun(wfRunId, userTaskGuid string, client *model.LHPublicApiClient) (*model.UserTaskRunPb, error) {
	resp, err := (*client).GetUserTaskRun(context.Background(), &model.UserTaskRunIdPb{
		WfRunId:      wfRunId,
		UserTaskGuid: userTaskGuid,
	})
	if err != nil {
		return nil, err
	}
	if resp.Code != model.LHResponseCodePb_OK {
		return nil, errors.New(*resp.Message)
	}
	if resp.Result.Status == model.UserTaskRunStatusPb_DONE || resp.Result.Status == model.UserTaskRunStatusPb_CANCELLED {
		return nil, errors.New("userTaskRun already in terminated state")
	}

	return resp.Result, nil
}

func init() {
	getCmd.AddCommand(getUserTaskRunCmd)
	searchCmd.AddCommand(searchUserTaskRunCmd)
	executeCmd.AddCommand(executeUserTaskRunCmd)
	assignCmd.AddCommand(assignUserTaskRunCmd)

	assignUserTaskRunCmd.Flags().String("userId", "", "User Id to assign to.")
	assignUserTaskRunCmd.Flags().String("userGroup", "", "User Group to assign to.")
	assignUserTaskRunCmd.Flags().Bool("overrideClaim", false, "Whether to forcefully steal task if it's already assigned.")
	assignUserTaskRunCmd.MarkFlagsMutuallyExclusive("userId", "userGroup")

	searchUserTaskRunCmd.Flags().String("userTaskDefName", "", "UserTaskDef ID of User Task Run's to search for.")
	searchUserTaskRunCmd.Flags().String("userId", "", "Search for User Task Runs assigned to this User ID.")
	searchUserTaskRunCmd.Flags().String("userGroup", "", "Search for User Task Runs assigned to this User Group.")
	searchUserTaskRunCmd.Flags().String("userTaskStatus", "", "Status of User Task Runs to search for.")
	searchUserTaskRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for User Task Runs that started no more than this number of minutes ago")
	searchUserTaskRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for User Task Runs that started at least this number of minutes ago")

}
