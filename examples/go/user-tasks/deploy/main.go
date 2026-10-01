package main

import (
	"context"
	"log"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	usertasks "github.com/littlehorse-enterprises/littlehorse/examples/go/user-tasks"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	ctx := context.Background()
	forms := []struct {
		name  string
		value interface {
			LHStructDef() littlehorse.LHStructDefInfo
		}
	}{
		{usertasks.ITRequestForm, usertasks.ItemRequestForm{}},
		{usertasks.ApprovalTask, usertasks.ApprovalForm{}},
	}
	for _, form := range forms {
		fields, err := littlehorse.GoStructToInlineStructDef(form.value)
		if err != nil {
			log.Fatal(err)
		}
		definition, err := (*client).PutStructDef(ctx, &lhproto.PutStructDefRequest{
			Name:      form.value.LHStructDef().Name,
			StructDef: fields,
		})
		if err != nil {
			log.Fatal(err)
		}
		request, err := littlehorse.NewUserTaskSchema(definition.GetId(), form.name).Compile()
		if err != nil {
			log.Fatal(err)
		}
		if _, err = (*client).PutUserTaskDef(ctx, request); err != nil {
			log.Fatal(err)
		}
	}
	wf := littlehorse.NewWorkflow(usertasks.MyWorkflow, usertasks.WorkflowName)
	wf.RegisterWfSpec(*client)
}
