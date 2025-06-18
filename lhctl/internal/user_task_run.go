package internal

import (
	"bufio"
	"errors"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var executeUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Execute a UserTaskRun at the CLI",
	Long: `Given a provided wfRunId and userTaskGuid, this utility prompts you
for your userId and then prompts you to fill out each required field of the
UserTaskRun. At the end, the UserTaskRun is submitted`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		client := getGlobalClient(cmd)
		executeUserTask(cmd, args[0], args[1], &client)
	},
}

var cancelUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Cancel a UserTaskRun",
	Long: `Given a provided wfRunId and UserTaskGuid, this command allows you to
	cancel the specified UserTaskRun. Cancelling a UserTaskRun will halt the entire WfRun execution.`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		client := getGlobalClient(cmd)
		cancelUserTask(cmd, args[0], args[1], &client)
	},
}

var listUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId>",
	Short: "List all UserTaskRun's for a given WfRun Id.",
	Long: `
Lists all UserTaskRun's for a given WfRun Id.
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]

		req := &lhproto.ListUserTaskRunRequest{
			WfRunId: littlehorse.StrToWfRunId(wfRunId),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListUserTaskRuns(
			requestContext(cmd),
			req,
		))
	},
}

var assignUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Reassign a UserTaskRun to a userGroup or specific userId",
	Long: `Given a provided wfRunId and UserTaskGuid, this utility allows you
to reassign the specified UserTaskRun.

The --overrideClaim option allows you to override an assignment if the UserTaskRun
is already claimed by a specific UserId.

The following option groups are supported:
[userId] -> assign to a specific userId.
[userGroup] -> assign to a userGroup of users
`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		overrideClaim, _ := cmd.Flags().GetBool("overrideClaim")

		userId, _ := cmd.Flags().GetString("userId")
		userGroup, _ := cmd.Flags().GetString("userGroup")

		reassign := &lhproto.AssignUserTaskRunRequest{
			UserTaskRunId: &lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(args[0]),
				UserTaskGuid: args[1],
			},
			OverrideClaim: overrideClaim,
		}

		if userId != "" {
			reassign.UserId = &userId
		}
		if userGroup != "" {
			reassign.UserGroup = &userGroup
		}

		if userId == "" && userGroup == "" {
			log.Fatal("Must specify either --userId or --userGroup")
		}

		littlehorse.PrintResp(getGlobalClient(cmd).AssignUserTaskRun(
			requestContext(cmd),
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

	You may provide the identifiers as two separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.`,
	Args: func(cmd *cobra.Command, args []string) error {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			return errors.New("must provide exactly 1 or 2 arguments. See 'lhctl get userTaskRun -h'")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId, userTaskGuid := args[0], args[1]

		littlehorse.PrintResp(getGlobalClient(cmd).GetUserTaskRun(
			requestContext(cmd),
			&lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(wfRunId),
				UserTaskGuid: userTaskGuid,
			},
		))
	},
}

var saveUserTaskRunProgressCmd = &cobra.Command{
	Use:   "userTaskRun",
	Short: "Save progress on UserTaskRuns",
	Long: `Given a provided WfRunId and UserTaskGuid, this utility allows you
to save current progress on a UserTask before executing the it.
`,
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {

		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		userTaskGuid, _ := cmd.Flags().GetString("userTaskGuid")

		saveUserTaskRunProgress := &lhproto.SaveUserTaskRunProgressRequest{
			Results: make(map[string]*lhproto.VariableValue),
			UserTaskRunId: &lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(wfRunId),
				UserTaskGuid: userTaskGuid,
			},
		}

		// First, get the UserTaskRun.
		client := getGlobalClient(cmd)
		userTaskRun, _ := getUserTaskRun(cmd, wfRunId, userTaskGuid, &client)

		if userTaskRun.Notes != nil {
			fmt.Println("\nNotes: " + *userTaskRun.Notes + "\n")
		}

		// Next, get the UserTaskDef.
		userTaskDef, err := getUserTaskDef(cmd, userTaskRun, &client)
		if err != nil {
			log.Fatal(err)
		}

		// Next, prompt for the userId.
		userIdVarVal, err := promptFor(
			"Enter the userId of the person completing the task",
			lhproto.VariableType_STR,
		)
		if err != nil {
			log.Fatal(err)
		}
		saveUserTaskRunProgress.UserId = userIdVarVal.GetStr()

		for _, field := range userTaskDef.Fields {
			fmt.Println("\nField: ", field.DisplayName)
			if field.Description != nil {
				fmt.Println(*field.Description)
			}
			resultVal, err := promptFor("Please enter the response for this field ("+field.Type.String()+")", field.Type)
			if err != nil {
				log.Fatal(err)
			}
			saveUserTaskRunProgress.Results[field.Name] = resultVal
		}

		fmt.Println("Select an assignment policy value:")
		for k, v := range lhproto.SaveUserTaskRunProgressRequest_SaveUserTaskRunAssignmentPolicy_name {
			fmt.Printf("%d: %s\n", k, v)
		}

		assignmentPolicy, err := promptFor(
			"Enter the number corresponding to your choice: ",
			lhproto.VariableType_INT,
		)
		if err != nil {
			log.Fatal(err)
		}
		saveUserTaskRunProgress.Policy = lhproto.SaveUserTaskRunProgressRequest_SaveUserTaskRunAssignmentPolicy(assignmentPolicy.GetInt())

		fmt.Println("completing userTaskRun!")
		// Post the result
		littlehorse.PrintResp(
			(client).SaveUserTaskRunProgress(requestContext(cmd), saveUserTaskRunProgress),
		)
	},
}

var PutUserTaskRunCommentCmd = &cobra.Command{
	Use:   "userTaskRunComment <wfRunId> <userTaskGuid> <userId> <comment>",
	Short: "Add a comment to a UserTaskRun",
	Long: `
Add a comment to a UserTaskRun.

Requires the WfRunId, UserTaskGuid, userId, and the comment text.
If userId or comment contain spaces, enclose the argument in quotation marks " ".
This command allows you to attach feedback or notes to a specific UserTaskRun for tracking or collaboration purposes.
`,
	Args: cobra.ExactArgs(4),
	Run: func(cmd *cobra.Command, args []string) {

		userTaskRunComment := &lhproto.PutUserTaskRunCommentRequest{
			UserTaskRunId: &lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(args[0]),
				UserTaskGuid: args[1],
			},
			UserId:  args[2],
			Comment: args[3],
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutUserTaskRunComment(requestContext(cmd), userTaskRunComment))
	},
}


var DeleteUserTaskRunCommentCmd = &cobra.Command{
	Use:   "userTaskRunComment <wfRunId> <userTaskGuid> <commentId> <userId>",
	Short: "Delete a comment from a userTaskRun",
	Long: `
Delete a comment from a UserTaskRun.

Requires the WfRunId, UserTaskGuid, userId, and the commentId of the comment to delete.
If userId contains spaces, enclose the argument in quotation marks " ".
This command allows you to remove a previously added comment from a UserTaskRun.
`,
	Args: cobra.ExactArgs(4),
	Run: func(cmd *cobra.Command, args []string) {

		commentId, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal("Unable to convert commentId to int:\n",err)
		}
		deleteUserTaskRunComment := &lhproto.DeleteUserTaskRunCommentRequest{
			UserTaskRunId: &lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(args[0]),
				UserTaskGuid: args[1],
			},
			UserId:        args[3],
			UserCommentId: int32(commentId),
		}
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteUserTaskRunComment(requestContext(cmd), deleteUserTaskRunComment))
	},
}


var editUserTaskRunCommentCmd = &cobra.Command{
	Use:   "userTaskRunComment <wfRunId> <userTaskGuid> <commentId> <userId> <comment>",
	Short: "Edit a comment on a UserTaskRun",
	Long: `
Edit a comment on a UserTaskRun.

Requires the WfRunId, UserTaskGuid, userId, the new comment text, and the commentId of the comment to edit.
If userId or comment contain spaces, enclose the argument in quotation marks " ".
This command allows you to update the content of a previously added comment on a UserTaskRun for correction or clarification purposes.
`,
	Args: cobra.ExactArgs(5),
	Run: func(cmd *cobra.Command, args []string) {

		commentId, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal("Unable to convert commentId to integer:\n", err)
		}
		userTaskRunComment := &lhproto.EditUserTaskRunCommentRequest{
			UserTaskRunId: &lhproto.UserTaskRunId{
				WfRunId:      littlehorse.StrToWfRunId(args[0]),
				UserTaskGuid: args[1],
			},
			UserId:        args[3],
			Comment:       args[4],
			UserCommentId: int32(commentId),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).EditUserTaskRunComment(requestContext(cmd), userTaskRunComment))
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
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &lhproto.SearchUserTaskRunRequest{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
		}

		statusStr, _ := cmd.Flags().GetString("userTaskStatus")
		if statusStr != "" {
			statusInt, ok := lhproto.UserTaskRunStatus_value[statusStr]
			if !ok {
				log.Fatal("Invalid value provided for userTaskStatus")
			}
			statusVal := lhproto.UserTaskRunStatus(statusInt)
			search.Status = &statusVal
		}

		userIdStr, _ := cmd.Flags().GetString("userId")
		userGroupStr, _ := cmd.Flags().GetString("userGroup")

		if userIdStr != "" {
			search.UserId = &userIdStr
		}
		if userGroupStr != "" {
			search.UserGroup = &userGroupStr
		}

		userTaskDefNameStr, _ := cmd.Flags().GetString("userTaskDefName")
		if userTaskDefNameStr != "" {
			search.UserTaskDefName = &userTaskDefNameStr
		}

		search.Bookmark = bookmark
		search.Limit = &limit

		littlehorse.PrintResp(getGlobalClient(cmd).SearchUserTaskRun(requestContext(cmd), search))

	},
}

func executeUserTask(cmd *cobra.Command, wfRunId string, userTaskGuid string, client *lhproto.LittleHorseClient) {
	fmt.Println("Executing UserTaskRun ", wfRunId, " ", userTaskGuid)

	completeUserTask := &lhproto.CompleteUserTaskRunRequest{
		Results: make(map[string]*lhproto.VariableValue),
		UserTaskRunId: &lhproto.UserTaskRunId{
			WfRunId:      littlehorse.StrToWfRunId(wfRunId),
			UserTaskGuid: userTaskGuid,
		},
	}

	// First, get the UserTaskRun.
	userTaskRun, err := getUserTaskRun(cmd, wfRunId, userTaskGuid, client)
	if err != nil {
		log.Fatal(err)
	}

	if userTaskRun.Notes != nil {
		fmt.Println("\nNotes: " + *userTaskRun.Notes + "\n")
	}

	// Next, get the UserTaskDef.
	userTaskDef, err := getUserTaskDef(cmd, userTaskRun, client)
	if err != nil {
		log.Fatal(err)
	}

	// Next, prompt for the userId.
	userIdVarVal, err := promptFor(
		"Enter the userId of the person completing the task",
		lhproto.VariableType_STR,
	)
	if err != nil {
		log.Fatal(err)
	}
	completeUserTask.UserId = userIdVarVal.GetStr()

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
		completeUserTask.Results[field.Name] = resultVal
	}

	fmt.Println("Saving userTaskRun progress!")
	// Post the result
	littlehorse.PrintResp(
		(*client).CompleteUserTaskRun(requestContext(cmd), completeUserTask),
	)
}

func cancelUserTask(cmd *cobra.Command, wfRunId string, userTaskGuid string, client *lhproto.LittleHorseClient) {
	cancelUserTask := &lhproto.CancelUserTaskRunRequest{
		UserTaskRunId: &lhproto.UserTaskRunId{
			WfRunId:      littlehorse.StrToWfRunId(wfRunId),
			UserTaskGuid: userTaskGuid,
		},
	}
	(*client).CancelUserTaskRun(requestContext(cmd), cancelUserTask)
}

func promptFor(prompt string, varType lhproto.VariableType) (*lhproto.VariableValue, error) {
	fmt.Print(prompt + ": ")
	// Create a new buffered reader to read from standard input
	reader := bufio.NewReader(os.Stdin)

	// Read the entire line of text entered by the user
	// The returned line will include newline characters such as '\n', so we'll trim it.
	userInput, _ := reader.ReadString('\n')
	return littlehorse.StrToVarVal(strings.TrimSpace(userInput), varType)
}

func getUserTaskDef(
	cmd *cobra.Command, userTaskRun *lhproto.UserTaskRun, client *lhproto.LittleHorseClient,
) (*lhproto.UserTaskDef, error) {
	return (*client).GetUserTaskDef(requestContext(cmd), userTaskRun.UserTaskDefId)
}

func getUserTaskRun(
	cmd *cobra.Command, wfRunId, userTaskGuid string, client *lhproto.LittleHorseClient,
) (*lhproto.UserTaskRun, error) {
	resp, err := (*client).GetUserTaskRun(requestContext(cmd), &lhproto.UserTaskRunId{
		WfRunId:      littlehorse.StrToWfRunId(wfRunId),
		UserTaskGuid: userTaskGuid,
	})

	if err != nil {
		return nil, err
	}
	if resp.Status == lhproto.UserTaskRunStatus_DONE || resp.Status == lhproto.UserTaskRunStatus_CANCELLED {
		return nil, errors.New("userTaskRun already in terminated state")
	}

	return resp, nil
}

func init() {
	getCmd.AddCommand(getUserTaskRunCmd)
	searchCmd.AddCommand(searchUserTaskRunCmd)
	executeCmd.AddCommand(executeUserTaskRunCmd)
	assignCmd.AddCommand(assignUserTaskRunCmd)
	listCmd.AddCommand(listUserTaskRunCmd)
	cancelUserTaskCmd.AddCommand(cancelUserTaskRunCmd)
	putCmd.AddCommand(PutUserTaskRunCommentCmd)
	deleteCmd.AddCommand(DeleteUserTaskRunCommentCmd)
	editCmd.AddCommand(editUserTaskRunCommentCmd)

	saveCmd.AddCommand(saveUserTaskRunProgressCmd)
	saveUserTaskRunProgressCmd.Flags().String("wfRunId", "", "WfRunId of the WfRun the UserTaskRun belongs to.")
	saveUserTaskRunProgressCmd.MarkFlagRequired("wfRunId")
	saveUserTaskRunProgressCmd.Flags().String("userTaskGuid", "", "GUID of the User Task you are saving progress on.")
	saveUserTaskRunProgressCmd.MarkFlagRequired("userTaskGuid")

	assignUserTaskRunCmd.Flags().String("userId", "", "User Id to assign to.")
	assignUserTaskRunCmd.Flags().String("userGroup", "", "User Group to assign to.")
	assignUserTaskRunCmd.Flags().Bool("overrideClaim", false, "Whether to forcefully steal task if it's already assigned.")

	searchUserTaskRunCmd.Flags().String("userTaskDefName", "", "UserTaskDef ID of User Task Run's to search for.")
	searchUserTaskRunCmd.Flags().String("userId", "", "Search for User Task Runs assigned to this User ID.")
	searchUserTaskRunCmd.Flags().String("userGroup", "", "Search for User Task Runs assigned to this User Group.")
	searchUserTaskRunCmd.Flags().String("userTaskStatus", "", "Status of User Task Runs to search for.")
	searchUserTaskRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for User Task Runs that started no more than this number of minutes ago")
	searchUserTaskRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for User Task Runs that started at least this number of minutes ago")
}
