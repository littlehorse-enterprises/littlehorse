package taskworker

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

type LHTaskWorker struct {
	config    *common.LHConfig
	grpcStub  *model.LittleHorseClient
	taskFunc  interface{}
	taskSig   *common.TaskFuncSignature
	manager   *serverConnectionManager
	taskDefId *model.TaskDefId
}

func NewTaskWorker(
	config *common.LHConfig,
	taskFunction interface{},
	taskDefName string,
) (*LHTaskWorker, error) {
	taskSig, err := common.NewTaskSignature(taskFunction)
	if err != nil {
		return nil, err
	}

	stub, err := config.GetGrpcClient()
	if err != nil {
		return nil, err
	}

	tw := &LHTaskWorker{
		config:    config,
		taskFunc:  taskFunction,
		grpcStub:  stub,
		taskDefId: &model.TaskDefId{Name: taskDefName},
		taskSig:   taskSig,
	}
	tw.manager = newServerConnectionManager(tw)
	return tw, nil
}

func (tw *LHTaskWorker) RegisterTaskDef() error {
	return tw.registerTaskDef()
}

func (tw *LHTaskWorker) Start() error {
	return tw.start()
}

func (tw *LHTaskWorker) Close() error {
	return tw.close()
}
