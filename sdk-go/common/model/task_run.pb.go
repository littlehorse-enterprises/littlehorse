// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.30.0
// 	protoc        v4.23.4
// source: task_run.proto

package model

import (
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	timestamppb "google.golang.org/protobuf/types/known/timestamppb"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

type TaskRun struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id             *TaskRunId             `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	Attempts       []*TaskAttempt         `protobuf:"bytes,2,rep,name=attempts,proto3" json:"attempts,omitempty"`
	MaxAttempts    int32                  `protobuf:"varint,3,opt,name=max_attempts,json=maxAttempts,proto3" json:"max_attempts,omitempty"`
	TaskDefName    string                 `protobuf:"bytes,4,opt,name=task_def_name,json=taskDefName,proto3" json:"task_def_name,omitempty"`
	InputVariables []*VarNameAndVal       `protobuf:"bytes,5,rep,name=input_variables,json=inputVariables,proto3" json:"input_variables,omitempty"`
	Source         *TaskRunSource         `protobuf:"bytes,6,opt,name=source,proto3" json:"source,omitempty"`
	ScheduledAt    *timestamppb.Timestamp `protobuf:"bytes,7,opt,name=scheduled_at,json=scheduledAt,proto3" json:"scheduled_at,omitempty"`
	Status         TaskStatus             `protobuf:"varint,8,opt,name=status,proto3,enum=littlehorse.TaskStatus" json:"status,omitempty"`
	TimeoutSeconds int32                  `protobuf:"varint,9,opt,name=timeout_seconds,json=timeoutSeconds,proto3" json:"timeout_seconds,omitempty"`
}

func (x *TaskRun) Reset() {
	*x = TaskRun{}
	if protoimpl.UnsafeEnabled {
		mi := &file_task_run_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskRun) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskRun) ProtoMessage() {}

func (x *TaskRun) ProtoReflect() protoreflect.Message {
	mi := &file_task_run_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskRun.ProtoReflect.Descriptor instead.
func (*TaskRun) Descriptor() ([]byte, []int) {
	return file_task_run_proto_rawDescGZIP(), []int{0}
}

func (x *TaskRun) GetId() *TaskRunId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *TaskRun) GetAttempts() []*TaskAttempt {
	if x != nil {
		return x.Attempts
	}
	return nil
}

func (x *TaskRun) GetMaxAttempts() int32 {
	if x != nil {
		return x.MaxAttempts
	}
	return 0
}

func (x *TaskRun) GetTaskDefName() string {
	if x != nil {
		return x.TaskDefName
	}
	return ""
}

func (x *TaskRun) GetInputVariables() []*VarNameAndVal {
	if x != nil {
		return x.InputVariables
	}
	return nil
}

func (x *TaskRun) GetSource() *TaskRunSource {
	if x != nil {
		return x.Source
	}
	return nil
}

func (x *TaskRun) GetScheduledAt() *timestamppb.Timestamp {
	if x != nil {
		return x.ScheduledAt
	}
	return nil
}

func (x *TaskRun) GetStatus() TaskStatus {
	if x != nil {
		return x.Status
	}
	return TaskStatus_TASK_SCHEDULED
}

func (x *TaskRun) GetTimeoutSeconds() int32 {
	if x != nil {
		return x.TimeoutSeconds
	}
	return 0
}

type VarNameAndVal struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	VarName string         `protobuf:"bytes,1,opt,name=var_name,json=varName,proto3" json:"var_name,omitempty"`
	Value   *VariableValue `protobuf:"bytes,2,opt,name=value,proto3" json:"value,omitempty"`
}

func (x *VarNameAndVal) Reset() {
	*x = VarNameAndVal{}
	if protoimpl.UnsafeEnabled {
		mi := &file_task_run_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *VarNameAndVal) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*VarNameAndVal) ProtoMessage() {}

func (x *VarNameAndVal) ProtoReflect() protoreflect.Message {
	mi := &file_task_run_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use VarNameAndVal.ProtoReflect.Descriptor instead.
func (*VarNameAndVal) Descriptor() ([]byte, []int) {
	return file_task_run_proto_rawDescGZIP(), []int{1}
}

func (x *VarNameAndVal) GetVarName() string {
	if x != nil {
		return x.VarName
	}
	return ""
}

func (x *VarNameAndVal) GetValue() *VariableValue {
	if x != nil {
		return x.Value
	}
	return nil
}

type TaskAttempt struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Output            *VariableValue         `protobuf:"bytes,1,opt,name=output,proto3,oneof" json:"output,omitempty"`
	LogOutput         *VariableValue         `protobuf:"bytes,2,opt,name=log_output,json=logOutput,proto3,oneof" json:"log_output,omitempty"`
	ScheduleTime      *timestamppb.Timestamp `protobuf:"bytes,3,opt,name=schedule_time,json=scheduleTime,proto3,oneof" json:"schedule_time,omitempty"`
	StartTime         *timestamppb.Timestamp `protobuf:"bytes,4,opt,name=start_time,json=startTime,proto3,oneof" json:"start_time,omitempty"`
	EndTime           *timestamppb.Timestamp `protobuf:"bytes,5,opt,name=end_time,json=endTime,proto3,oneof" json:"end_time,omitempty"`
	TaskWorkerId      string                 `protobuf:"bytes,7,opt,name=task_worker_id,json=taskWorkerId,proto3" json:"task_worker_id,omitempty"`
	TaskWorkerVersion *string                `protobuf:"bytes,8,opt,name=task_worker_version,json=taskWorkerVersion,proto3,oneof" json:"task_worker_version,omitempty"`
	Status            TaskStatus             `protobuf:"varint,9,opt,name=status,proto3,enum=littlehorse.TaskStatus" json:"status,omitempty"`
}

func (x *TaskAttempt) Reset() {
	*x = TaskAttempt{}
	if protoimpl.UnsafeEnabled {
		mi := &file_task_run_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskAttempt) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskAttempt) ProtoMessage() {}

func (x *TaskAttempt) ProtoReflect() protoreflect.Message {
	mi := &file_task_run_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskAttempt.ProtoReflect.Descriptor instead.
func (*TaskAttempt) Descriptor() ([]byte, []int) {
	return file_task_run_proto_rawDescGZIP(), []int{2}
}

func (x *TaskAttempt) GetOutput() *VariableValue {
	if x != nil {
		return x.Output
	}
	return nil
}

func (x *TaskAttempt) GetLogOutput() *VariableValue {
	if x != nil {
		return x.LogOutput
	}
	return nil
}

func (x *TaskAttempt) GetScheduleTime() *timestamppb.Timestamp {
	if x != nil {
		return x.ScheduleTime
	}
	return nil
}

func (x *TaskAttempt) GetStartTime() *timestamppb.Timestamp {
	if x != nil {
		return x.StartTime
	}
	return nil
}

func (x *TaskAttempt) GetEndTime() *timestamppb.Timestamp {
	if x != nil {
		return x.EndTime
	}
	return nil
}

func (x *TaskAttempt) GetTaskWorkerId() string {
	if x != nil {
		return x.TaskWorkerId
	}
	return ""
}

func (x *TaskAttempt) GetTaskWorkerVersion() string {
	if x != nil && x.TaskWorkerVersion != nil {
		return *x.TaskWorkerVersion
	}
	return ""
}

func (x *TaskAttempt) GetStatus() TaskStatus {
	if x != nil {
		return x.Status
	}
	return TaskStatus_TASK_SCHEDULED
}

type TaskRunSource struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// Types that are assignable to TaskRunSource:
	//
	//	*TaskRunSource_TaskNode
	//	*TaskRunSource_UserTaskTrigger
	TaskRunSource isTaskRunSource_TaskRunSource `protobuf_oneof:"task_run_source"`
}

func (x *TaskRunSource) Reset() {
	*x = TaskRunSource{}
	if protoimpl.UnsafeEnabled {
		mi := &file_task_run_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskRunSource) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskRunSource) ProtoMessage() {}

func (x *TaskRunSource) ProtoReflect() protoreflect.Message {
	mi := &file_task_run_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskRunSource.ProtoReflect.Descriptor instead.
func (*TaskRunSource) Descriptor() ([]byte, []int) {
	return file_task_run_proto_rawDescGZIP(), []int{3}
}

func (m *TaskRunSource) GetTaskRunSource() isTaskRunSource_TaskRunSource {
	if m != nil {
		return m.TaskRunSource
	}
	return nil
}

func (x *TaskRunSource) GetTaskNode() *TaskNodeReference {
	if x, ok := x.GetTaskRunSource().(*TaskRunSource_TaskNode); ok {
		return x.TaskNode
	}
	return nil
}

func (x *TaskRunSource) GetUserTaskTrigger() *UserTaskTriggerReference {
	if x, ok := x.GetTaskRunSource().(*TaskRunSource_UserTaskTrigger); ok {
		return x.UserTaskTrigger
	}
	return nil
}

type isTaskRunSource_TaskRunSource interface {
	isTaskRunSource_TaskRunSource()
}

type TaskRunSource_TaskNode struct {
	TaskNode *TaskNodeReference `protobuf:"bytes,1,opt,name=task_node,json=taskNode,proto3,oneof"`
}

type TaskRunSource_UserTaskTrigger struct {
	UserTaskTrigger *UserTaskTriggerReference `protobuf:"bytes,2,opt,name=user_task_trigger,json=userTaskTrigger,proto3,oneof"`
}

func (*TaskRunSource_TaskNode) isTaskRunSource_TaskRunSource() {}

func (*TaskRunSource_UserTaskTrigger) isTaskRunSource_TaskRunSource() {}

type TaskNodeReference struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	NodeRunId *NodeRunId `protobuf:"bytes,1,opt,name=node_run_id,json=nodeRunId,proto3" json:"node_run_id,omitempty"`
	WfSpecId  *WfSpecId  `protobuf:"bytes,2,opt,name=wf_spec_id,json=wfSpecId,proto3" json:"wf_spec_id,omitempty"`
}

func (x *TaskNodeReference) Reset() {
	*x = TaskNodeReference{}
	if protoimpl.UnsafeEnabled {
		mi := &file_task_run_proto_msgTypes[4]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskNodeReference) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskNodeReference) ProtoMessage() {}

func (x *TaskNodeReference) ProtoReflect() protoreflect.Message {
	mi := &file_task_run_proto_msgTypes[4]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskNodeReference.ProtoReflect.Descriptor instead.
func (*TaskNodeReference) Descriptor() ([]byte, []int) {
	return file_task_run_proto_rawDescGZIP(), []int{4}
}

func (x *TaskNodeReference) GetNodeRunId() *NodeRunId {
	if x != nil {
		return x.NodeRunId
	}
	return nil
}

func (x *TaskNodeReference) GetWfSpecId() *WfSpecId {
	if x != nil {
		return x.WfSpecId
	}
	return nil
}

var File_task_run_proto protoreflect.FileDescriptor

var file_task_run_proto_rawDesc = []byte{
	0x0a, 0x0e, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x72, 0x75, 0x6e, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a, 0x1f, 0x67,
	0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74,
	0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x12,
	0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x5f, 0x65, 0x6e, 0x75, 0x6d, 0x73, 0x2e, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x1a, 0x0e, 0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x1a, 0x0f, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x1a, 0x10, 0x75, 0x73, 0x65, 0x72, 0x5f, 0x74, 0x61, 0x73, 0x6b, 0x73, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0xc0, 0x03, 0x0a, 0x07, 0x54, 0x61, 0x73, 0x6b, 0x52, 0x75,
	0x6e, 0x12, 0x26, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x16, 0x2e,
	0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x61, 0x73, 0x6b,
	0x52, 0x75, 0x6e, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x34, 0x0a, 0x08, 0x61, 0x74, 0x74,
	0x65, 0x6d, 0x70, 0x74, 0x73, 0x18, 0x02, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x18, 0x2e, 0x6c, 0x69,
	0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x61, 0x73, 0x6b, 0x41, 0x74,
	0x74, 0x65, 0x6d, 0x70, 0x74, 0x52, 0x08, 0x61, 0x74, 0x74, 0x65, 0x6d, 0x70, 0x74, 0x73, 0x12,
	0x21, 0x0a, 0x0c, 0x6d, 0x61, 0x78, 0x5f, 0x61, 0x74, 0x74, 0x65, 0x6d, 0x70, 0x74, 0x73, 0x18,
	0x03, 0x20, 0x01, 0x28, 0x05, 0x52, 0x0b, 0x6d, 0x61, 0x78, 0x41, 0x74, 0x74, 0x65, 0x6d, 0x70,
	0x74, 0x73, 0x12, 0x22, 0x0a, 0x0d, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x64, 0x65, 0x66, 0x5f, 0x6e,
	0x61, 0x6d, 0x65, 0x18, 0x04, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0b, 0x74, 0x61, 0x73, 0x6b, 0x44,
	0x65, 0x66, 0x4e, 0x61, 0x6d, 0x65, 0x12, 0x43, 0x0a, 0x0f, 0x69, 0x6e, 0x70, 0x75, 0x74, 0x5f,
	0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x73, 0x18, 0x05, 0x20, 0x03, 0x28, 0x0b, 0x32,
	0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x56, 0x61,
	0x72, 0x4e, 0x61, 0x6d, 0x65, 0x41, 0x6e, 0x64, 0x56, 0x61, 0x6c, 0x52, 0x0e, 0x69, 0x6e, 0x70,
	0x75, 0x74, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x73, 0x12, 0x32, 0x0a, 0x06, 0x73,
	0x6f, 0x75, 0x72, 0x63, 0x65, 0x18, 0x06, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69,
	0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x61, 0x73, 0x6b, 0x52, 0x75,
	0x6e, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x52, 0x06, 0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x12,
	0x3d, 0x0a, 0x0c, 0x73, 0x63, 0x68, 0x65, 0x64, 0x75, 0x6c, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18,
	0x07, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70,
	0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d,
	0x70, 0x52, 0x0b, 0x73, 0x63, 0x68, 0x65, 0x64, 0x75, 0x6c, 0x65, 0x64, 0x41, 0x74, 0x12, 0x2f,
	0x0a, 0x06, 0x73, 0x74, 0x61, 0x74, 0x75, 0x73, 0x18, 0x08, 0x20, 0x01, 0x28, 0x0e, 0x32, 0x17,
	0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x61, 0x73,
	0x6b, 0x53, 0x74, 0x61, 0x74, 0x75, 0x73, 0x52, 0x06, 0x73, 0x74, 0x61, 0x74, 0x75, 0x73, 0x12,
	0x27, 0x0a, 0x0f, 0x74, 0x69, 0x6d, 0x65, 0x6f, 0x75, 0x74, 0x5f, 0x73, 0x65, 0x63, 0x6f, 0x6e,
	0x64, 0x73, 0x18, 0x09, 0x20, 0x01, 0x28, 0x05, 0x52, 0x0e, 0x74, 0x69, 0x6d, 0x65, 0x6f, 0x75,
	0x74, 0x53, 0x65, 0x63, 0x6f, 0x6e, 0x64, 0x73, 0x22, 0x5c, 0x0a, 0x0d, 0x56, 0x61, 0x72, 0x4e,
	0x61, 0x6d, 0x65, 0x41, 0x6e, 0x64, 0x56, 0x61, 0x6c, 0x12, 0x19, 0x0a, 0x08, 0x76, 0x61, 0x72,
	0x5f, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07, 0x76, 0x61, 0x72,
	0x4e, 0x61, 0x6d, 0x65, 0x12, 0x30, 0x0a, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x52,
	0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x22, 0xb4, 0x04, 0x0a, 0x0b, 0x54, 0x61, 0x73, 0x6b, 0x41,
	0x74, 0x74, 0x65, 0x6d, 0x70, 0x74, 0x12, 0x37, 0x0a, 0x06, 0x6f, 0x75, 0x74, 0x70, 0x75, 0x74,
	0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68,
	0x6f, 0x72, 0x73, 0x65, 0x2e, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c,
	0x75, 0x65, 0x48, 0x00, 0x52, 0x06, 0x6f, 0x75, 0x74, 0x70, 0x75, 0x74, 0x88, 0x01, 0x01, 0x12,
	0x3e, 0x0a, 0x0a, 0x6c, 0x6f, 0x67, 0x5f, 0x6f, 0x75, 0x74, 0x70, 0x75, 0x74, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x48,
	0x01, 0x52, 0x09, 0x6c, 0x6f, 0x67, 0x4f, 0x75, 0x74, 0x70, 0x75, 0x74, 0x88, 0x01, 0x01, 0x12,
	0x44, 0x0a, 0x0d, 0x73, 0x63, 0x68, 0x65, 0x64, 0x75, 0x6c, 0x65, 0x5f, 0x74, 0x69, 0x6d, 0x65,
	0x18, 0x03, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61,
	0x6d, 0x70, 0x48, 0x02, 0x52, 0x0c, 0x73, 0x63, 0x68, 0x65, 0x64, 0x75, 0x6c, 0x65, 0x54, 0x69,
	0x6d, 0x65, 0x88, 0x01, 0x01, 0x12, 0x3e, 0x0a, 0x0a, 0x73, 0x74, 0x61, 0x72, 0x74, 0x5f, 0x74,
	0x69, 0x6d, 0x65, 0x18, 0x04, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67,
	0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65,
	0x73, 0x74, 0x61, 0x6d, 0x70, 0x48, 0x03, 0x52, 0x09, 0x73, 0x74, 0x61, 0x72, 0x74, 0x54, 0x69,
	0x6d, 0x65, 0x88, 0x01, 0x01, 0x12, 0x3a, 0x0a, 0x08, 0x65, 0x6e, 0x64, 0x5f, 0x74, 0x69, 0x6d,
	0x65, 0x18, 0x05, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74,
	0x61, 0x6d, 0x70, 0x48, 0x04, 0x52, 0x07, 0x65, 0x6e, 0x64, 0x54, 0x69, 0x6d, 0x65, 0x88, 0x01,
	0x01, 0x12, 0x24, 0x0a, 0x0e, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x77, 0x6f, 0x72, 0x6b, 0x65, 0x72,
	0x5f, 0x69, 0x64, 0x18, 0x07, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0c, 0x74, 0x61, 0x73, 0x6b, 0x57,
	0x6f, 0x72, 0x6b, 0x65, 0x72, 0x49, 0x64, 0x12, 0x33, 0x0a, 0x13, 0x74, 0x61, 0x73, 0x6b, 0x5f,
	0x77, 0x6f, 0x72, 0x6b, 0x65, 0x72, 0x5f, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x18, 0x08,
	0x20, 0x01, 0x28, 0x09, 0x48, 0x05, 0x52, 0x11, 0x74, 0x61, 0x73, 0x6b, 0x57, 0x6f, 0x72, 0x6b,
	0x65, 0x72, 0x56, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x88, 0x01, 0x01, 0x12, 0x2f, 0x0a, 0x06,
	0x73, 0x74, 0x61, 0x74, 0x75, 0x73, 0x18, 0x09, 0x20, 0x01, 0x28, 0x0e, 0x32, 0x17, 0x2e, 0x6c,
	0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x61, 0x73, 0x6b, 0x53,
	0x74, 0x61, 0x74, 0x75, 0x73, 0x52, 0x06, 0x73, 0x74, 0x61, 0x74, 0x75, 0x73, 0x42, 0x09, 0x0a,
	0x07, 0x5f, 0x6f, 0x75, 0x74, 0x70, 0x75, 0x74, 0x42, 0x0d, 0x0a, 0x0b, 0x5f, 0x6c, 0x6f, 0x67,
	0x5f, 0x6f, 0x75, 0x74, 0x70, 0x75, 0x74, 0x42, 0x10, 0x0a, 0x0e, 0x5f, 0x73, 0x63, 0x68, 0x65,
	0x64, 0x75, 0x6c, 0x65, 0x5f, 0x74, 0x69, 0x6d, 0x65, 0x42, 0x0d, 0x0a, 0x0b, 0x5f, 0x73, 0x74,
	0x61, 0x72, 0x74, 0x5f, 0x74, 0x69, 0x6d, 0x65, 0x42, 0x0b, 0x0a, 0x09, 0x5f, 0x65, 0x6e, 0x64,
	0x5f, 0x74, 0x69, 0x6d, 0x65, 0x42, 0x16, 0x0a, 0x14, 0x5f, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x77,
	0x6f, 0x72, 0x6b, 0x65, 0x72, 0x5f, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x22, 0xb6, 0x01,
	0x0a, 0x0d, 0x54, 0x61, 0x73, 0x6b, 0x52, 0x75, 0x6e, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x12,
	0x3d, 0x0a, 0x09, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x6e, 0x6f, 0x64, 0x65, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x0b, 0x32, 0x1e, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65,
	0x2e, 0x54, 0x61, 0x73, 0x6b, 0x4e, 0x6f, 0x64, 0x65, 0x52, 0x65, 0x66, 0x65, 0x72, 0x65, 0x6e,
	0x63, 0x65, 0x48, 0x00, 0x52, 0x08, 0x74, 0x61, 0x73, 0x6b, 0x4e, 0x6f, 0x64, 0x65, 0x12, 0x53,
	0x0a, 0x11, 0x75, 0x73, 0x65, 0x72, 0x5f, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x74, 0x72, 0x69, 0x67,
	0x67, 0x65, 0x72, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x25, 0x2e, 0x6c, 0x69, 0x74, 0x74,
	0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x55, 0x73, 0x65, 0x72, 0x54, 0x61, 0x73, 0x6b,
	0x54, 0x72, 0x69, 0x67, 0x67, 0x65, 0x72, 0x52, 0x65, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65,
	0x48, 0x00, 0x52, 0x0f, 0x75, 0x73, 0x65, 0x72, 0x54, 0x61, 0x73, 0x6b, 0x54, 0x72, 0x69, 0x67,
	0x67, 0x65, 0x72, 0x42, 0x11, 0x0a, 0x0f, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x72, 0x75, 0x6e, 0x5f,
	0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x22, 0x80, 0x01, 0x0a, 0x11, 0x54, 0x61, 0x73, 0x6b, 0x4e,
	0x6f, 0x64, 0x65, 0x52, 0x65, 0x66, 0x65, 0x72, 0x65, 0x6e, 0x63, 0x65, 0x12, 0x36, 0x0a, 0x0b,
	0x6e, 0x6f, 0x64, 0x65, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28,
	0x0b, 0x32, 0x16, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e,
	0x4e, 0x6f, 0x64, 0x65, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x52, 0x09, 0x6e, 0x6f, 0x64, 0x65, 0x52,
	0x75, 0x6e, 0x49, 0x64, 0x12, 0x33, 0x0a, 0x0a, 0x77, 0x66, 0x5f, 0x73, 0x70, 0x65, 0x63, 0x5f,
	0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x15, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c,
	0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x57, 0x66, 0x53, 0x70, 0x65, 0x63, 0x49, 0x64, 0x52,
	0x08, 0x77, 0x66, 0x53, 0x70, 0x65, 0x63, 0x49, 0x64, 0x42, 0x2c, 0x0a, 0x1f, 0x69, 0x6f, 0x2e,
	0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e,
	0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x07,
	0x2e, 0x3b, 0x6d, 0x6f, 0x64, 0x65, 0x6c, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_task_run_proto_rawDescOnce sync.Once
	file_task_run_proto_rawDescData = file_task_run_proto_rawDesc
)

func file_task_run_proto_rawDescGZIP() []byte {
	file_task_run_proto_rawDescOnce.Do(func() {
		file_task_run_proto_rawDescData = protoimpl.X.CompressGZIP(file_task_run_proto_rawDescData)
	})
	return file_task_run_proto_rawDescData
}

var file_task_run_proto_msgTypes = make([]protoimpl.MessageInfo, 5)
var file_task_run_proto_goTypes = []interface{}{
	(*TaskRun)(nil),                  // 0: littlehorse.TaskRun
	(*VarNameAndVal)(nil),            // 1: littlehorse.VarNameAndVal
	(*TaskAttempt)(nil),              // 2: littlehorse.TaskAttempt
	(*TaskRunSource)(nil),            // 3: littlehorse.TaskRunSource
	(*TaskNodeReference)(nil),        // 4: littlehorse.TaskNodeReference
	(*TaskRunId)(nil),                // 5: littlehorse.TaskRunId
	(*timestamppb.Timestamp)(nil),    // 6: google.protobuf.Timestamp
	(TaskStatus)(0),                  // 7: littlehorse.TaskStatus
	(*VariableValue)(nil),            // 8: littlehorse.VariableValue
	(*UserTaskTriggerReference)(nil), // 9: littlehorse.UserTaskTriggerReference
	(*NodeRunId)(nil),                // 10: littlehorse.NodeRunId
	(*WfSpecId)(nil),                 // 11: littlehorse.WfSpecId
}
var file_task_run_proto_depIdxs = []int32{
	5,  // 0: littlehorse.TaskRun.id:type_name -> littlehorse.TaskRunId
	2,  // 1: littlehorse.TaskRun.attempts:type_name -> littlehorse.TaskAttempt
	1,  // 2: littlehorse.TaskRun.input_variables:type_name -> littlehorse.VarNameAndVal
	3,  // 3: littlehorse.TaskRun.source:type_name -> littlehorse.TaskRunSource
	6,  // 4: littlehorse.TaskRun.scheduled_at:type_name -> google.protobuf.Timestamp
	7,  // 5: littlehorse.TaskRun.status:type_name -> littlehorse.TaskStatus
	8,  // 6: littlehorse.VarNameAndVal.value:type_name -> littlehorse.VariableValue
	8,  // 7: littlehorse.TaskAttempt.output:type_name -> littlehorse.VariableValue
	8,  // 8: littlehorse.TaskAttempt.log_output:type_name -> littlehorse.VariableValue
	6,  // 9: littlehorse.TaskAttempt.schedule_time:type_name -> google.protobuf.Timestamp
	6,  // 10: littlehorse.TaskAttempt.start_time:type_name -> google.protobuf.Timestamp
	6,  // 11: littlehorse.TaskAttempt.end_time:type_name -> google.protobuf.Timestamp
	7,  // 12: littlehorse.TaskAttempt.status:type_name -> littlehorse.TaskStatus
	4,  // 13: littlehorse.TaskRunSource.task_node:type_name -> littlehorse.TaskNodeReference
	9,  // 14: littlehorse.TaskRunSource.user_task_trigger:type_name -> littlehorse.UserTaskTriggerReference
	10, // 15: littlehorse.TaskNodeReference.node_run_id:type_name -> littlehorse.NodeRunId
	11, // 16: littlehorse.TaskNodeReference.wf_spec_id:type_name -> littlehorse.WfSpecId
	17, // [17:17] is the sub-list for method output_type
	17, // [17:17] is the sub-list for method input_type
	17, // [17:17] is the sub-list for extension type_name
	17, // [17:17] is the sub-list for extension extendee
	0,  // [0:17] is the sub-list for field type_name
}

func init() { file_task_run_proto_init() }
func file_task_run_proto_init() {
	if File_task_run_proto != nil {
		return
	}
	file_common_enums_proto_init()
	file_variable_proto_init()
	file_object_id_proto_init()
	file_user_tasks_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_task_run_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskRun); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_task_run_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*VarNameAndVal); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_task_run_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskAttempt); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_task_run_proto_msgTypes[3].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskRunSource); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_task_run_proto_msgTypes[4].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskNodeReference); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	file_task_run_proto_msgTypes[2].OneofWrappers = []interface{}{}
	file_task_run_proto_msgTypes[3].OneofWrappers = []interface{}{
		(*TaskRunSource_TaskNode)(nil),
		(*TaskRunSource_UserTaskTrigger)(nil),
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_task_run_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   5,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_task_run_proto_goTypes,
		DependencyIndexes: file_task_run_proto_depIdxs,
		MessageInfos:      file_task_run_proto_msgTypes,
	}.Build()
	File_task_run_proto = out.File
	file_task_run_proto_rawDesc = nil
	file_task_run_proto_goTypes = nil
	file_task_run_proto_depIdxs = nil
}
