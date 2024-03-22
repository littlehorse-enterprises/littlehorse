package taskworker

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

type TaskWorkerHealthReason int32

const (
	Healthy TaskWorkerHealthReason = iota
	Unhealthy
	ServerRebalancing
)

func (e TaskWorkerHealthReason) String() string {
	switch e {
	case Healthy:
		return "Healthy"
	case Unhealthy:
		return "Unhealthy"
	case ServerRebalancing:
		return "ServerRebalancing"
	default:
		return fmt.Sprintf("%d", int(e))
	}
}

type LHTaskWorkerHealth struct {
	Healthy bool
	Reason  TaskWorkerHealthReason
}

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

func (tw *LHTaskWorker) Health() LHTaskWorkerHealth {
	if !tw.manager.clusterHealthy {
		return LHTaskWorkerHealth{
			Healthy: false,
			Reason:  ServerRebalancing,
		}
	}

	if !tw.manager.workerHealthy {
		return LHTaskWorkerHealth{
			Healthy: false,
			Reason:  Unhealthy,
		}
	}

	return LHTaskWorkerHealth{
		Healthy: true,
		Reason:  Healthy,
	}
}
