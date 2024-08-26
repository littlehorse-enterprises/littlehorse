package littlehorse

import (
	"fmt"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
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
	config               *LHConfig
	grpcStub             *lhproto.LittleHorseClient
	taskFunc             interface{}
	taskSig              *TaskFuncSignature
	manager              *serverConnectionManager
	taskDefId            *lhproto.TaskDefId
	maskedInputVariables []bool
	maskedOutput         bool
}

func NewTaskWorker(
	config *LHConfig,
	taskFunction interface{},
	taskDefName string,
) (*LHTaskWorker, error) {
	taskSig, err := NewTaskSignature(taskFunction)
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
		taskDefId: &lhproto.TaskDefId{Name: taskDefName},
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

func (tw *LHTaskWorker) MaskedFields(maskedFieldValues []bool) {
	if tw != nil {
		tw.maskedInputVariables = maskedFieldValues
	}
}

func (tw *LHTaskWorker) MaskedOutput() {
	if tw != nil {
		if !tw.taskSig.HasOutput {
			panic("Task signature doesn't contain an output type")
		}
		tw.maskedOutput = true
	}
}
