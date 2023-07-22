package cmd

import (
	"context"
	"errors"
	"fmt"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var executeCmd = &cobra.Command{
	Use:   "execute <wfRunId> <userTaskGuid>",
	Short: "Execute a specified UserTaskRun from the command line.",
	Long: `
Execute a UserTaskRun specified by wfRunId and userTaskGuid.

The Command will prompt you to provide your userId, and also values for each of the
form fields required by the associated UserTaskDef.
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("You must provide the wfRunId and userTaskGuid")
		}
		client := getGlobalClient(cmd)
		executeUserTask(args[0], args[1], &client)
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
	fmt.Print(prompt + " (put multiple-word responses in '' quotes): ")
	var userInput string
	fmt.Scanln(&userInput)
	return common.StrToVarVal(userInput, varType)
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
	rootCmd.AddCommand(executeCmd)
}
