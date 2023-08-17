package common

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type WorkerContext struct {
	ScheduledTask *model.ScheduledTaskPb
	ScheduleTime  *timestamppb.Timestamp
	stderr        string
}

func NewWorkerContext(
	scheduledTask *model.ScheduledTaskPb,
	scheduleTime *timestamppb.Timestamp,
) *WorkerContext {

	return &WorkerContext{
		ScheduledTask: scheduledTask,
		ScheduleTime:  scheduleTime,
	}

}

func (wc *WorkerContext) GetWfRunId() *string {
	return GetWfRunIdFromTaskSource(wc.ScheduledTask.Source)
}

func (wc *WorkerContext) GetNodeRunId() *model.NodeRunId {
	switch src := wc.ScheduledTask.Source.TaskRunSource.(type) {
	case *model.TaskRunSourcePb_TaskNode:
		return src.TaskNode.NodeRunId
	case *model.TaskRunSourcePb_UserTaskTrigger:
		return src.UserTaskTrigger.NodeRunId
	}
	return nil
}

func (wc *WorkerContext) GetAttemptNumber() int32 {
	return wc.ScheduledTask.GetAttemptNumber()
}

func (wc *WorkerContext) GetScheduledTime() *timestamppb.Timestamp {
	return wc.ScheduleTime
}

func (wc *WorkerContext) GetIdempotencyKey() string {
	return wc.ScheduledTask.TaskRunId.WfRunId + "/" + wc.ScheduledTask.TaskRunId.TaskGuid
}

func (wc *WorkerContext) Log(thing interface{}) {
	if thing != nil {
		wc.stderr += fmt.Sprint(thing)
	} else {
		wc.stderr += "nil"
	}
}

func (wc *WorkerContext) GetLogOutput() string {
	return wc.stderr
}
