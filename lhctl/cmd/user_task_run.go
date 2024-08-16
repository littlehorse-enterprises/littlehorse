package cmd

import (
	"bufio"
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
		executeUserTask(cmd, args[0], args[1], &client)
	},
}

var cancelUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Cancel a UserTaskRun",
	Long: `Given a provided wfRunId and UserTaskGuid, this command allows you to
	cancel the specified UserTaskRun. Cancelling a UserTaskRun will halt the entire WfRun execution.`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("You must provide the wfRunId and userTaskGuid")
		}
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
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("Must provide one arg: the WfRun ID!")
		}
		wfRunId := args[0]

		req := &model.ListUserTaskRunRequest{
			WfRunId: common.StrToWfRunId(wfRunId),
		}

		common.PrintResp(getGlobalClient(cmd).ListUserTaskRuns(
			requestContext(cmd),
			req,
		))
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

		reassign := &model.AssignUserTaskRunRequest{
			UserTaskRunId: &model.UserTaskRunId{
				WfRunId:      common.StrToWfRunId(args[0]),
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

		common.PrintResp(getGlobalClient(cmd).AssignUserTaskRun(
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
			requestContext(cmd),
			&model.UserTaskRunId{
				WfRunId:      common.StrToWfRunId(wfRunId),
				UserTaskGuid: userTaskGuid,
			},
		))
	},
}

var saveUserTaskRunProgressCmd = &cobra.Command{
	Use:   "saveUserTaskRun",
	Short: "Save progress on UserTaskRuns",
	Long: `Given a provied WfRunId and UserTaskGuid, this utility allows you
to save current progress on a UserTask before executing the it.
`,
	Run: func(cmd *cobra.Command, args []string) {

		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		userTaskGuid, _ := cmd.Flags().GetString("userTaskGuid")

		saveUserTaskRunProgress := &model.SaveUserTaskRunProgressRequest{
			Results: make(map[string]*model.VariableValue),
			UserTaskRunId: &model.UserTaskRunId{
				WfRunId:      common.StrToWfRunId(wfRunId),
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
			model.VariableType_STR,
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
		for k, v := range model.SaveUserTaskRunProgressRequest_SaveUserTaskRunAssignmentPolicy_name {
			fmt.Printf("%d: %s\n", k, v)
		}

		assignmentPolicy, err := promptFor(
			"Enter the number corresponding to your choice: ",
			model.VariableType_INT,
		)
		if err != nil {
			log.Fatal(err)
		}
		saveUserTaskRunProgress.Policy = model.SaveUserTaskRunProgressRequest_SaveUserTaskRunAssignmentPolicy(assignmentPolicy.GetInt())

		fmt.Println("completing userTaskRun!")
		// Post the result
		common.PrintResp(
			(client).SaveUserTaskRunProgress(requestContext(cmd), saveUserTaskRunProgress),
		)
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

		search := &model.SearchUserTaskRunRequest{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
		}

		statusStr, _ := cmd.Flags().GetString("userTaskStatus")
		if statusStr != "" {
			statusInt, ok := model.UserTaskRunStatus_value[statusStr]
			if !ok {
				log.Fatal("Invalid value provided for userTaskStatus")
			}
			statusVal := model.UserTaskRunStatus(statusInt)
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

		common.PrintResp(getGlobalClient(cmd).SearchUserTaskRun(requestContext(cmd), search))

	},
}

func executeUserTask(cmd *cobra.Command, wfRunId string, userTaskGuid string, client *model.LittleHorseClient) {
	fmt.Println("Executing UserTaskRun ", wfRunId, " ", userTaskGuid)

	completeUserTask := &model.CompleteUserTaskRunRequest{
		Results: make(map[string]*model.VariableValue),
		UserTaskRunId: &model.UserTaskRunId{
			WfRunId:      common.StrToWfRunId(wfRunId),
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
		model.VariableType_STR,
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
	common.PrintResp(
		(*client).CompleteUserTaskRun(requestContext(cmd), completeUserTask),
	)
}

func cancelUserTask(cmd *cobra.Command, wfRunId string, userTaskGuid string, client *model.LittleHorseClient) {
	cancelUserTask := &model.CancelUserTaskRunRequest{
		UserTaskRunId: &model.UserTaskRunId{
			WfRunId:      common.StrToWfRunId(wfRunId),
			UserTaskGuid: userTaskGuid,
		},
	}
	(*client).CancelUserTaskRun(requestContext(cmd), cancelUserTask)
}

func promptFor(prompt string, varType model.VariableType) (*model.VariableValue, error) {
	fmt.Print(prompt + ": ")
	// Create a new buffered reader to read from standard input
	reader := bufio.NewReader(os.Stdin)

	// Read the entire line of text entered by the user
	// The returned line will include the newline character '\n', so we'll trim it.
	userInput, _ := reader.ReadString('\n')
	return common.StrToVarVal(userInput[:len(userInput)-1], varType)
}

func getUserTaskDef(
	cmd *cobra.Command, userTaskRun *model.UserTaskRun, client *model.LittleHorseClient,
) (*model.UserTaskDef, error) {
	return (*client).GetUserTaskDef(requestContext(cmd), userTaskRun.UserTaskDefId)
}

func getUserTaskRun(
	cmd *cobra.Command, wfRunId, userTaskGuid string, client *model.LittleHorseClient,
) (*model.UserTaskRun, error) {
	resp, err := (*client).GetUserTaskRun(requestContext(cmd), &model.UserTaskRunId{
		WfRunId:      common.StrToWfRunId(wfRunId),
		UserTaskGuid: userTaskGuid,
	})
	if err != nil {
		return nil, err
	}
	if resp.Status == model.UserTaskRunStatus_DONE || resp.Status == model.UserTaskRunStatus_CANCELLED {
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

	rootCmd.AddCommand(saveUserTaskRunProgressCmd)
	saveUserTaskRunProgressCmd.Flags().String("wfRunId", "", "WfRunId of the WfRun the UserTaskRun belongs to.")
	saveUserTaskRunProgressCmd.Flags().String("userTaskGuid", "", "GUID of the User Task you are saving progress on.")

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
