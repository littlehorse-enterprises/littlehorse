// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.30.0
// 	protoc        v3.21.12
// source: object_id.proto

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

type WfSpecId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Name    string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
	Version int32  `protobuf:"varint,2,opt,name=version,proto3" json:"version,omitempty"`
}

func (x *WfSpecId) Reset() {
	*x = WfSpecId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WfSpecId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WfSpecId) ProtoMessage() {}

func (x *WfSpecId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WfSpecId.ProtoReflect.Descriptor instead.
func (*WfSpecId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{0}
}

func (x *WfSpecId) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

func (x *WfSpecId) GetVersion() int32 {
	if x != nil {
		return x.Version
	}
	return 0
}

type TaskDefId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Name string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
}

func (x *TaskDefId) Reset() {
	*x = TaskDefId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskDefId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskDefId) ProtoMessage() {}

func (x *TaskDefId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskDefId.ProtoReflect.Descriptor instead.
func (*TaskDefId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{1}
}

func (x *TaskDefId) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

type ExternalEventDefId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Name string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
}

func (x *ExternalEventDefId) Reset() {
	*x = ExternalEventDefId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *ExternalEventDefId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*ExternalEventDefId) ProtoMessage() {}

func (x *ExternalEventDefId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use ExternalEventDefId.ProtoReflect.Descriptor instead.
func (*ExternalEventDefId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{2}
}

func (x *ExternalEventDefId) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

type GetLatestWfSpecRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Name string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
}

func (x *GetLatestWfSpecRequest) Reset() {
	*x = GetLatestWfSpecRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *GetLatestWfSpecRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*GetLatestWfSpecRequest) ProtoMessage() {}

func (x *GetLatestWfSpecRequest) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use GetLatestWfSpecRequest.ProtoReflect.Descriptor instead.
func (*GetLatestWfSpecRequest) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{3}
}

func (x *GetLatestWfSpecRequest) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

type UserTaskDefId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Name    string `protobuf:"bytes,1,opt,name=name,proto3" json:"name,omitempty"`
	Version int32  `protobuf:"varint,2,opt,name=version,proto3" json:"version,omitempty"`
}

func (x *UserTaskDefId) Reset() {
	*x = UserTaskDefId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[4]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *UserTaskDefId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*UserTaskDefId) ProtoMessage() {}

func (x *UserTaskDefId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[4]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use UserTaskDefId.ProtoReflect.Descriptor instead.
func (*UserTaskDefId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{4}
}

func (x *UserTaskDefId) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

func (x *UserTaskDefId) GetVersion() int32 {
	if x != nil {
		return x.Version
	}
	return 0
}

type TaskWorkerGroupId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	TaskDefName string `protobuf:"bytes,1,opt,name=task_def_name,json=taskDefName,proto3" json:"task_def_name,omitempty"`
}

func (x *TaskWorkerGroupId) Reset() {
	*x = TaskWorkerGroupId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[5]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskWorkerGroupId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskWorkerGroupId) ProtoMessage() {}

func (x *TaskWorkerGroupId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[5]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskWorkerGroupId.ProtoReflect.Descriptor instead.
func (*TaskWorkerGroupId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{5}
}

func (x *TaskWorkerGroupId) GetTaskDefName() string {
	if x != nil {
		return x.TaskDefName
	}
	return ""
}

type VariableId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WfRunId         string `protobuf:"bytes,1,opt,name=wf_run_id,json=wfRunId,proto3" json:"wf_run_id,omitempty"`
	ThreadRunNumber int32  `protobuf:"varint,2,opt,name=thread_run_number,json=threadRunNumber,proto3" json:"thread_run_number,omitempty"`
	Name            string `protobuf:"bytes,3,opt,name=name,proto3" json:"name,omitempty"`
}

func (x *VariableId) Reset() {
	*x = VariableId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[6]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *VariableId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*VariableId) ProtoMessage() {}

func (x *VariableId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[6]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use VariableId.ProtoReflect.Descriptor instead.
func (*VariableId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{6}
}

func (x *VariableId) GetWfRunId() string {
	if x != nil {
		return x.WfRunId
	}
	return ""
}

func (x *VariableId) GetThreadRunNumber() int32 {
	if x != nil {
		return x.ThreadRunNumber
	}
	return 0
}

func (x *VariableId) GetName() string {
	if x != nil {
		return x.Name
	}
	return ""
}

type ExternalEventId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WfRunId              string `protobuf:"bytes,1,opt,name=wf_run_id,json=wfRunId,proto3" json:"wf_run_id,omitempty"`
	ExternalEventDefName string `protobuf:"bytes,2,opt,name=external_event_def_name,json=externalEventDefName,proto3" json:"external_event_def_name,omitempty"`
	Guid                 string `protobuf:"bytes,3,opt,name=guid,proto3" json:"guid,omitempty"`
}

func (x *ExternalEventId) Reset() {
	*x = ExternalEventId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[7]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *ExternalEventId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*ExternalEventId) ProtoMessage() {}

func (x *ExternalEventId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[7]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use ExternalEventId.ProtoReflect.Descriptor instead.
func (*ExternalEventId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{7}
}

func (x *ExternalEventId) GetWfRunId() string {
	if x != nil {
		return x.WfRunId
	}
	return ""
}

func (x *ExternalEventId) GetExternalEventDefName() string {
	if x != nil {
		return x.ExternalEventDefName
	}
	return ""
}

func (x *ExternalEventId) GetGuid() string {
	if x != nil {
		return x.Guid
	}
	return ""
}

type WfRunId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id string `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
}

func (x *WfRunId) Reset() {
	*x = WfRunId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[8]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WfRunId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WfRunId) ProtoMessage() {}

func (x *WfRunId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[8]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WfRunId.ProtoReflect.Descriptor instead.
func (*WfRunId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{8}
}

func (x *WfRunId) GetId() string {
	if x != nil {
		return x.Id
	}
	return ""
}

type NodeRunId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WfRunId         string `protobuf:"bytes,1,opt,name=wf_run_id,json=wfRunId,proto3" json:"wf_run_id,omitempty"`
	ThreadRunNumber int32  `protobuf:"varint,2,opt,name=thread_run_number,json=threadRunNumber,proto3" json:"thread_run_number,omitempty"`
	Position        int32  `protobuf:"varint,3,opt,name=position,proto3" json:"position,omitempty"`
}

func (x *NodeRunId) Reset() {
	*x = NodeRunId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[9]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *NodeRunId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*NodeRunId) ProtoMessage() {}

func (x *NodeRunId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[9]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use NodeRunId.ProtoReflect.Descriptor instead.
func (*NodeRunId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{9}
}

func (x *NodeRunId) GetWfRunId() string {
	if x != nil {
		return x.WfRunId
	}
	return ""
}

func (x *NodeRunId) GetThreadRunNumber() int32 {
	if x != nil {
		return x.ThreadRunNumber
	}
	return 0
}

func (x *NodeRunId) GetPosition() int32 {
	if x != nil {
		return x.Position
	}
	return 0
}

type TaskRunId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WfRunId  string `protobuf:"bytes,1,opt,name=wf_run_id,json=wfRunId,proto3" json:"wf_run_id,omitempty"`
	TaskGuid string `protobuf:"bytes,2,opt,name=task_guid,json=taskGuid,proto3" json:"task_guid,omitempty"`
}

func (x *TaskRunId) Reset() {
	*x = TaskRunId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[10]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskRunId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskRunId) ProtoMessage() {}

func (x *TaskRunId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[10]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskRunId.ProtoReflect.Descriptor instead.
func (*TaskRunId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{10}
}

func (x *TaskRunId) GetWfRunId() string {
	if x != nil {
		return x.WfRunId
	}
	return ""
}

func (x *TaskRunId) GetTaskGuid() string {
	if x != nil {
		return x.TaskGuid
	}
	return ""
}

type UserTaskRunId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WfRunId      string `protobuf:"bytes,1,opt,name=wf_run_id,json=wfRunId,proto3" json:"wf_run_id,omitempty"`
	UserTaskGuid string `protobuf:"bytes,2,opt,name=user_task_guid,json=userTaskGuid,proto3" json:"user_task_guid,omitempty"`
}

func (x *UserTaskRunId) Reset() {
	*x = UserTaskRunId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[11]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *UserTaskRunId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*UserTaskRunId) ProtoMessage() {}

func (x *UserTaskRunId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[11]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use UserTaskRunId.ProtoReflect.Descriptor instead.
func (*UserTaskRunId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{11}
}

func (x *UserTaskRunId) GetWfRunId() string {
	if x != nil {
		return x.WfRunId
	}
	return ""
}

func (x *UserTaskRunId) GetUserTaskGuid() string {
	if x != nil {
		return x.UserTaskGuid
	}
	return ""
}

type TaskDefMetricsId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WindowStart *timestamppb.Timestamp `protobuf:"bytes,1,opt,name=window_start,json=windowStart,proto3" json:"window_start,omitempty"`
	WindowType  MetricsWindowLength    `protobuf:"varint,2,opt,name=window_type,json=windowType,proto3,enum=littlehorse.MetricsWindowLength" json:"window_type,omitempty"`
	TaskDefName string                 `protobuf:"bytes,3,opt,name=task_def_name,json=taskDefName,proto3" json:"task_def_name,omitempty"`
}

func (x *TaskDefMetricsId) Reset() {
	*x = TaskDefMetricsId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[12]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *TaskDefMetricsId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*TaskDefMetricsId) ProtoMessage() {}

func (x *TaskDefMetricsId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[12]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use TaskDefMetricsId.ProtoReflect.Descriptor instead.
func (*TaskDefMetricsId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{12}
}

func (x *TaskDefMetricsId) GetWindowStart() *timestamppb.Timestamp {
	if x != nil {
		return x.WindowStart
	}
	return nil
}

func (x *TaskDefMetricsId) GetWindowType() MetricsWindowLength {
	if x != nil {
		return x.WindowType
	}
	return MetricsWindowLength_MINUTES_5
}

func (x *TaskDefMetricsId) GetTaskDefName() string {
	if x != nil {
		return x.TaskDefName
	}
	return ""
}

type WfSpecMetricsId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	WindowStart   *timestamppb.Timestamp `protobuf:"bytes,1,opt,name=window_start,json=windowStart,proto3" json:"window_start,omitempty"`
	WindowType    MetricsWindowLength    `protobuf:"varint,2,opt,name=window_type,json=windowType,proto3,enum=littlehorse.MetricsWindowLength" json:"window_type,omitempty"`
	WfSpecName    string                 `protobuf:"bytes,3,opt,name=wf_spec_name,json=wfSpecName,proto3" json:"wf_spec_name,omitempty"`
	WfSpecVersion int32                  `protobuf:"varint,4,opt,name=wf_spec_version,json=wfSpecVersion,proto3" json:"wf_spec_version,omitempty"`
}

func (x *WfSpecMetricsId) Reset() {
	*x = WfSpecMetricsId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_object_id_proto_msgTypes[13]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WfSpecMetricsId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WfSpecMetricsId) ProtoMessage() {}

func (x *WfSpecMetricsId) ProtoReflect() protoreflect.Message {
	mi := &file_object_id_proto_msgTypes[13]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WfSpecMetricsId.ProtoReflect.Descriptor instead.
func (*WfSpecMetricsId) Descriptor() ([]byte, []int) {
	return file_object_id_proto_rawDescGZIP(), []int{13}
}

func (x *WfSpecMetricsId) GetWindowStart() *timestamppb.Timestamp {
	if x != nil {
		return x.WindowStart
	}
	return nil
}

func (x *WfSpecMetricsId) GetWindowType() MetricsWindowLength {
	if x != nil {
		return x.WindowType
	}
	return MetricsWindowLength_MINUTES_5
}

func (x *WfSpecMetricsId) GetWfSpecName() string {
	if x != nil {
		return x.WfSpecName
	}
	return ""
}

func (x *WfSpecMetricsId) GetWfSpecVersion() int32 {
	if x != nil {
		return x.WfSpecVersion
	}
	return 0
}

var File_object_id_proto protoreflect.FileDescriptor

var file_object_id_proto_rawDesc = []byte{
	0x0a, 0x0f, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e, 0x70, 0x72, 0x6f, 0x74,
	0x6f, 0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a, 0x1f,
	0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f,
	0x74, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a,
	0x12, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x5f, 0x65, 0x6e, 0x75, 0x6d, 0x73, 0x2e, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x22, 0x38, 0x0a, 0x08, 0x57, 0x66, 0x53, 0x70, 0x65, 0x63, 0x49, 0x64, 0x12,
	0x12, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x6e,
	0x61, 0x6d, 0x65, 0x12, 0x18, 0x0a, 0x07, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x18, 0x02,
	0x20, 0x01, 0x28, 0x05, 0x52, 0x07, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x22, 0x1f, 0x0a,
	0x09, 0x54, 0x61, 0x73, 0x6b, 0x44, 0x65, 0x66, 0x49, 0x64, 0x12, 0x12, 0x0a, 0x04, 0x6e, 0x61,
	0x6d, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x28,
	0x0a, 0x12, 0x45, 0x78, 0x74, 0x65, 0x72, 0x6e, 0x61, 0x6c, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x44,
	0x65, 0x66, 0x49, 0x64, 0x12, 0x12, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x09, 0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x2c, 0x0a, 0x16, 0x47, 0x65, 0x74, 0x4c,
	0x61, 0x74, 0x65, 0x73, 0x74, 0x57, 0x66, 0x53, 0x70, 0x65, 0x63, 0x52, 0x65, 0x71, 0x75, 0x65,
	0x73, 0x74, 0x12, 0x12, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09,
	0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x3d, 0x0a, 0x0d, 0x55, 0x73, 0x65, 0x72, 0x54, 0x61,
	0x73, 0x6b, 0x44, 0x65, 0x66, 0x49, 0x64, 0x12, 0x12, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18,
	0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x12, 0x18, 0x0a, 0x07, 0x76,
	0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x18, 0x02, 0x20, 0x01, 0x28, 0x05, 0x52, 0x07, 0x76, 0x65,
	0x72, 0x73, 0x69, 0x6f, 0x6e, 0x22, 0x37, 0x0a, 0x11, 0x54, 0x61, 0x73, 0x6b, 0x57, 0x6f, 0x72,
	0x6b, 0x65, 0x72, 0x47, 0x72, 0x6f, 0x75, 0x70, 0x49, 0x64, 0x12, 0x22, 0x0a, 0x0d, 0x74, 0x61,
	0x73, 0x6b, 0x5f, 0x64, 0x65, 0x66, 0x5f, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28,
	0x09, 0x52, 0x0b, 0x74, 0x61, 0x73, 0x6b, 0x44, 0x65, 0x66, 0x4e, 0x61, 0x6d, 0x65, 0x22, 0x68,
	0x0a, 0x0a, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x49, 0x64, 0x12, 0x1a, 0x0a, 0x09,
	0x77, 0x66, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52,
	0x07, 0x77, 0x66, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x2a, 0x0a, 0x11, 0x74, 0x68, 0x72, 0x65,
	0x61, 0x64, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x6e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x05, 0x52, 0x0f, 0x74, 0x68, 0x72, 0x65, 0x61, 0x64, 0x52, 0x75, 0x6e, 0x4e, 0x75,
	0x6d, 0x62, 0x65, 0x72, 0x12, 0x12, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x03, 0x20, 0x01,
	0x28, 0x09, 0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x78, 0x0a, 0x0f, 0x45, 0x78, 0x74, 0x65,
	0x72, 0x6e, 0x61, 0x6c, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x49, 0x64, 0x12, 0x1a, 0x0a, 0x09, 0x77,
	0x66, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07,
	0x77, 0x66, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x35, 0x0a, 0x17, 0x65, 0x78, 0x74, 0x65, 0x72,
	0x6e, 0x61, 0x6c, 0x5f, 0x65, 0x76, 0x65, 0x6e, 0x74, 0x5f, 0x64, 0x65, 0x66, 0x5f, 0x6e, 0x61,
	0x6d, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x14, 0x65, 0x78, 0x74, 0x65, 0x72, 0x6e,
	0x61, 0x6c, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x44, 0x65, 0x66, 0x4e, 0x61, 0x6d, 0x65, 0x12, 0x12,
	0x0a, 0x04, 0x67, 0x75, 0x69, 0x64, 0x18, 0x03, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x67, 0x75,
	0x69, 0x64, 0x22, 0x19, 0x0a, 0x07, 0x57, 0x66, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x0e, 0x0a,
	0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x02, 0x69, 0x64, 0x22, 0x6f, 0x0a,
	0x09, 0x4e, 0x6f, 0x64, 0x65, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x1a, 0x0a, 0x09, 0x77, 0x66,
	0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07, 0x77,
	0x66, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x2a, 0x0a, 0x11, 0x74, 0x68, 0x72, 0x65, 0x61, 0x64,
	0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x6e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x18, 0x02, 0x20, 0x01, 0x28,
	0x05, 0x52, 0x0f, 0x74, 0x68, 0x72, 0x65, 0x61, 0x64, 0x52, 0x75, 0x6e, 0x4e, 0x75, 0x6d, 0x62,
	0x65, 0x72, 0x12, 0x1a, 0x0a, 0x08, 0x70, 0x6f, 0x73, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x18, 0x03,
	0x20, 0x01, 0x28, 0x05, 0x52, 0x08, 0x70, 0x6f, 0x73, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x22, 0x44,
	0x0a, 0x09, 0x54, 0x61, 0x73, 0x6b, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x1a, 0x0a, 0x09, 0x77,
	0x66, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07,
	0x77, 0x66, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x1b, 0x0a, 0x09, 0x74, 0x61, 0x73, 0x6b, 0x5f,
	0x67, 0x75, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x08, 0x74, 0x61, 0x73, 0x6b,
	0x47, 0x75, 0x69, 0x64, 0x22, 0x51, 0x0a, 0x0d, 0x55, 0x73, 0x65, 0x72, 0x54, 0x61, 0x73, 0x6b,
	0x52, 0x75, 0x6e, 0x49, 0x64, 0x12, 0x1a, 0x0a, 0x09, 0x77, 0x66, 0x5f, 0x72, 0x75, 0x6e, 0x5f,
	0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x07, 0x77, 0x66, 0x52, 0x75, 0x6e, 0x49,
	0x64, 0x12, 0x24, 0x0a, 0x0e, 0x75, 0x73, 0x65, 0x72, 0x5f, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x67,
	0x75, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0c, 0x75, 0x73, 0x65, 0x72, 0x54,
	0x61, 0x73, 0x6b, 0x47, 0x75, 0x69, 0x64, 0x22, 0xb8, 0x01, 0x0a, 0x10, 0x54, 0x61, 0x73, 0x6b,
	0x44, 0x65, 0x66, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x73, 0x49, 0x64, 0x12, 0x3d, 0x0a, 0x0c,
	0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f, 0x73, 0x74, 0x61, 0x72, 0x74, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74,
	0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x0b,
	0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x53, 0x74, 0x61, 0x72, 0x74, 0x12, 0x41, 0x0a, 0x0b, 0x77,
	0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f, 0x74, 0x79, 0x70, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0e,
	0x32, 0x20, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d,
	0x65, 0x74, 0x72, 0x69, 0x63, 0x73, 0x57, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x4c, 0x65, 0x6e, 0x67,
	0x74, 0x68, 0x52, 0x0a, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x54, 0x79, 0x70, 0x65, 0x12, 0x22,
	0x0a, 0x0d, 0x74, 0x61, 0x73, 0x6b, 0x5f, 0x64, 0x65, 0x66, 0x5f, 0x6e, 0x61, 0x6d, 0x65, 0x18,
	0x03, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0b, 0x74, 0x61, 0x73, 0x6b, 0x44, 0x65, 0x66, 0x4e, 0x61,
	0x6d, 0x65, 0x22, 0xdd, 0x01, 0x0a, 0x0f, 0x57, 0x66, 0x53, 0x70, 0x65, 0x63, 0x4d, 0x65, 0x74,
	0x72, 0x69, 0x63, 0x73, 0x49, 0x64, 0x12, 0x3d, 0x0a, 0x0c, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77,
	0x5f, 0x73, 0x74, 0x61, 0x72, 0x74, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67,
	0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54,
	0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x0b, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77,
	0x53, 0x74, 0x61, 0x72, 0x74, 0x12, 0x41, 0x0a, 0x0b, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f,
	0x74, 0x79, 0x70, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0e, 0x32, 0x20, 0x2e, 0x6c, 0x69, 0x74,
	0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x73,
	0x57, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x4c, 0x65, 0x6e, 0x67, 0x74, 0x68, 0x52, 0x0a, 0x77, 0x69,
	0x6e, 0x64, 0x6f, 0x77, 0x54, 0x79, 0x70, 0x65, 0x12, 0x20, 0x0a, 0x0c, 0x77, 0x66, 0x5f, 0x73,
	0x70, 0x65, 0x63, 0x5f, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x03, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0a,
	0x77, 0x66, 0x53, 0x70, 0x65, 0x63, 0x4e, 0x61, 0x6d, 0x65, 0x12, 0x26, 0x0a, 0x0f, 0x77, 0x66,
	0x5f, 0x73, 0x70, 0x65, 0x63, 0x5f, 0x76, 0x65, 0x72, 0x73, 0x69, 0x6f, 0x6e, 0x18, 0x04, 0x20,
	0x01, 0x28, 0x05, 0x52, 0x0d, 0x77, 0x66, 0x53, 0x70, 0x65, 0x63, 0x56, 0x65, 0x72, 0x73, 0x69,
	0x6f, 0x6e, 0x42, 0x2c, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68,
	0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x07, 0x2e, 0x3b, 0x6d, 0x6f, 0x64, 0x65, 0x6c,
	0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_object_id_proto_rawDescOnce sync.Once
	file_object_id_proto_rawDescData = file_object_id_proto_rawDesc
)

func file_object_id_proto_rawDescGZIP() []byte {
	file_object_id_proto_rawDescOnce.Do(func() {
		file_object_id_proto_rawDescData = protoimpl.X.CompressGZIP(file_object_id_proto_rawDescData)
	})
	return file_object_id_proto_rawDescData
}

var file_object_id_proto_msgTypes = make([]protoimpl.MessageInfo, 14)
var file_object_id_proto_goTypes = []interface{}{
	(*WfSpecId)(nil),               // 0: littlehorse.WfSpecId
	(*TaskDefId)(nil),              // 1: littlehorse.TaskDefId
	(*ExternalEventDefId)(nil),     // 2: littlehorse.ExternalEventDefId
	(*GetLatestWfSpecRequest)(nil), // 3: littlehorse.GetLatestWfSpecRequest
	(*UserTaskDefId)(nil),          // 4: littlehorse.UserTaskDefId
	(*TaskWorkerGroupId)(nil),      // 5: littlehorse.TaskWorkerGroupId
	(*VariableId)(nil),             // 6: littlehorse.VariableId
	(*ExternalEventId)(nil),        // 7: littlehorse.ExternalEventId
	(*WfRunId)(nil),                // 8: littlehorse.WfRunId
	(*NodeRunId)(nil),              // 9: littlehorse.NodeRunId
	(*TaskRunId)(nil),              // 10: littlehorse.TaskRunId
	(*UserTaskRunId)(nil),          // 11: littlehorse.UserTaskRunId
	(*TaskDefMetricsId)(nil),       // 12: littlehorse.TaskDefMetricsId
	(*WfSpecMetricsId)(nil),        // 13: littlehorse.WfSpecMetricsId
	(*timestamppb.Timestamp)(nil),  // 14: google.protobuf.Timestamp
	(MetricsWindowLength)(0),       // 15: littlehorse.MetricsWindowLength
}
var file_object_id_proto_depIdxs = []int32{
	14, // 0: littlehorse.TaskDefMetricsId.window_start:type_name -> google.protobuf.Timestamp
	15, // 1: littlehorse.TaskDefMetricsId.window_type:type_name -> littlehorse.MetricsWindowLength
	14, // 2: littlehorse.WfSpecMetricsId.window_start:type_name -> google.protobuf.Timestamp
	15, // 3: littlehorse.WfSpecMetricsId.window_type:type_name -> littlehorse.MetricsWindowLength
	4,  // [4:4] is the sub-list for method output_type
	4,  // [4:4] is the sub-list for method input_type
	4,  // [4:4] is the sub-list for extension type_name
	4,  // [4:4] is the sub-list for extension extendee
	0,  // [0:4] is the sub-list for field type_name
}

func init() { file_object_id_proto_init() }
func file_object_id_proto_init() {
	if File_object_id_proto != nil {
		return
	}
	file_common_enums_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_object_id_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WfSpecId); i {
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
		file_object_id_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskDefId); i {
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
		file_object_id_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*ExternalEventDefId); i {
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
		file_object_id_proto_msgTypes[3].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*GetLatestWfSpecRequest); i {
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
		file_object_id_proto_msgTypes[4].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*UserTaskDefId); i {
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
		file_object_id_proto_msgTypes[5].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskWorkerGroupId); i {
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
		file_object_id_proto_msgTypes[6].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*VariableId); i {
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
		file_object_id_proto_msgTypes[7].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*ExternalEventId); i {
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
		file_object_id_proto_msgTypes[8].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WfRunId); i {
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
		file_object_id_proto_msgTypes[9].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*NodeRunId); i {
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
		file_object_id_proto_msgTypes[10].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskRunId); i {
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
		file_object_id_proto_msgTypes[11].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*UserTaskRunId); i {
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
		file_object_id_proto_msgTypes[12].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*TaskDefMetricsId); i {
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
		file_object_id_proto_msgTypes[13].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WfSpecMetricsId); i {
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
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_object_id_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   14,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_object_id_proto_goTypes,
		DependencyIndexes: file_object_id_proto_depIdxs,
		MessageInfos:      file_object_id_proto_msgTypes,
	}.Build()
	File_object_id_proto = out.File
	file_object_id_proto_rawDesc = nil
	file_object_id_proto_goTypes = nil
	file_object_id_proto_depIdxs = nil
}
