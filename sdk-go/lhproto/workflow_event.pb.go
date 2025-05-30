// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v4.23.4
// source: workflow_event.proto

package lhproto

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

// A WorkflowEvent represents a "Thing That Happened" *INSIDE* a WfRun. It is DIFFERENT from
// an ExternalEvent, because an ExternalEvent represents something that happened OUTSIDE the WfRun,
// and is used to send information to the WfRun.
//
// In contrast, a WorkflowEvent is thrown by the WfRun and is used to send information to the outside
// world.
type WorkflowEvent struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the WorkflowEvent. Contains WfRunId and WorkflowEventDefId.
	Id *WorkflowEventId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The content of the WorkflowEvent.
	Content *VariableValue `protobuf:"bytes,2,opt,name=content,proto3" json:"content,omitempty"`
	// The time that the WorkflowEvent was created.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,3,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	// The NodeRun with which the WorkflowEvent is associated.
	NodeRunId *NodeRunId `protobuf:"bytes,4,opt,name=node_run_id,json=nodeRunId,proto3" json:"node_run_id,omitempty"`
}

func (x *WorkflowEvent) Reset() {
	*x = WorkflowEvent{}
	if protoimpl.UnsafeEnabled {
		mi := &file_workflow_event_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WorkflowEvent) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WorkflowEvent) ProtoMessage() {}

func (x *WorkflowEvent) ProtoReflect() protoreflect.Message {
	mi := &file_workflow_event_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WorkflowEvent.ProtoReflect.Descriptor instead.
func (*WorkflowEvent) Descriptor() ([]byte, []int) {
	return file_workflow_event_proto_rawDescGZIP(), []int{0}
}

func (x *WorkflowEvent) GetId() *WorkflowEventId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *WorkflowEvent) GetContent() *VariableValue {
	if x != nil {
		return x.Content
	}
	return nil
}

func (x *WorkflowEvent) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *WorkflowEvent) GetNodeRunId() *NodeRunId {
	if x != nil {
		return x.NodeRunId
	}
	return nil
}

// The WorkflowEventDef defines the blueprint for a WorkflowEvent.
type WorkflowEventDef struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the WorkflowEventDef. Contains the name of the WorkflowEventDef.
	Id *WorkflowEventDefId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The time that the WorkflowEventDef was created at.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,2,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	// The type of 'content' thrown with a WorkflowEvent based on this WorkflowEventDef.
	ContentType *ReturnType `protobuf:"bytes,3,opt,name=content_type,json=contentType,proto3" json:"content_type,omitempty"`
}

func (x *WorkflowEventDef) Reset() {
	*x = WorkflowEventDef{}
	if protoimpl.UnsafeEnabled {
		mi := &file_workflow_event_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *WorkflowEventDef) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*WorkflowEventDef) ProtoMessage() {}

func (x *WorkflowEventDef) ProtoReflect() protoreflect.Message {
	mi := &file_workflow_event_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use WorkflowEventDef.ProtoReflect.Descriptor instead.
func (*WorkflowEventDef) Descriptor() ([]byte, []int) {
	return file_workflow_event_proto_rawDescGZIP(), []int{1}
}

func (x *WorkflowEventDef) GetId() *WorkflowEventDefId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *WorkflowEventDef) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *WorkflowEventDef) GetContentType() *ReturnType {
	if x != nil {
		return x.ContentType
	}
	return nil
}

var File_workflow_event_proto protoreflect.FileDescriptor

var file_workflow_event_proto_rawDesc = []byte{
	0x0a, 0x14, 0x77, 0x6f, 0x72, 0x6b, 0x66, 0x6c, 0x6f, 0x77, 0x5f, 0x65, 0x76, 0x65, 0x6e, 0x74,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f,
	0x72, 0x73, 0x65, 0x1a, 0x1f, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74,
	0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70,
	0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0e, 0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x2e, 0x70,
	0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0f, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x13, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x5f, 0x77, 0x66,
	0x73, 0x70, 0x65, 0x63, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0xe6, 0x01, 0x0a, 0x0d, 0x57,
	0x6f, 0x72, 0x6b, 0x66, 0x6c, 0x6f, 0x77, 0x45, 0x76, 0x65, 0x6e, 0x74, 0x12, 0x2c, 0x0a, 0x02,
	0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1c, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c,
	0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x57, 0x6f, 0x72, 0x6b, 0x66, 0x6c, 0x6f, 0x77, 0x45,
	0x76, 0x65, 0x6e, 0x74, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x34, 0x0a, 0x07, 0x63, 0x6f,
	0x6e, 0x74, 0x65, 0x6e, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69,
	0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62,
	0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x52, 0x07, 0x63, 0x6f, 0x6e, 0x74, 0x65, 0x6e, 0x74,
	0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x03,
	0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70,
	0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x41, 0x74, 0x12, 0x36, 0x0a, 0x0b, 0x6e,
	0x6f, 0x64, 0x65, 0x5f, 0x72, 0x75, 0x6e, 0x5f, 0x69, 0x64, 0x18, 0x04, 0x20, 0x01, 0x28, 0x0b,
	0x32, 0x16, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4e,
	0x6f, 0x64, 0x65, 0x52, 0x75, 0x6e, 0x49, 0x64, 0x52, 0x09, 0x6e, 0x6f, 0x64, 0x65, 0x52, 0x75,
	0x6e, 0x49, 0x64, 0x22, 0xba, 0x01, 0x0a, 0x10, 0x57, 0x6f, 0x72, 0x6b, 0x66, 0x6c, 0x6f, 0x77,
	0x45, 0x76, 0x65, 0x6e, 0x74, 0x44, 0x65, 0x66, 0x12, 0x2f, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01,
	0x20, 0x01, 0x28, 0x0b, 0x32, 0x1f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72,
	0x73, 0x65, 0x2e, 0x57, 0x6f, 0x72, 0x6b, 0x66, 0x6c, 0x6f, 0x77, 0x45, 0x76, 0x65, 0x6e, 0x74,
	0x44, 0x65, 0x66, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65,
	0x61, 0x74, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e,
	0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e,
	0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74,
	0x65, 0x64, 0x41, 0x74, 0x12, 0x3a, 0x0a, 0x0c, 0x63, 0x6f, 0x6e, 0x74, 0x65, 0x6e, 0x74, 0x5f,
	0x74, 0x79, 0x70, 0x65, 0x18, 0x03, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74,
	0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x52, 0x65, 0x74, 0x75, 0x72, 0x6e, 0x54,
	0x79, 0x70, 0x65, 0x52, 0x0b, 0x63, 0x6f, 0x6e, 0x74, 0x65, 0x6e, 0x74, 0x54, 0x79, 0x70, 0x65,
	0x42, 0x4d, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72,
	0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x09, 0x2e, 0x3b, 0x6c, 0x68, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0xaa, 0x02, 0x1c, 0x4c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x48, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53,
	0x64, 0x6b, 0x2e, 0x43, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x50, 0x72, 0x6f, 0x74, 0x6f, 0x62,
	0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_workflow_event_proto_rawDescOnce sync.Once
	file_workflow_event_proto_rawDescData = file_workflow_event_proto_rawDesc
)

func file_workflow_event_proto_rawDescGZIP() []byte {
	file_workflow_event_proto_rawDescOnce.Do(func() {
		file_workflow_event_proto_rawDescData = protoimpl.X.CompressGZIP(file_workflow_event_proto_rawDescData)
	})
	return file_workflow_event_proto_rawDescData
}

var file_workflow_event_proto_msgTypes = make([]protoimpl.MessageInfo, 2)
var file_workflow_event_proto_goTypes = []interface{}{
	(*WorkflowEvent)(nil),         // 0: littlehorse.WorkflowEvent
	(*WorkflowEventDef)(nil),      // 1: littlehorse.WorkflowEventDef
	(*WorkflowEventId)(nil),       // 2: littlehorse.WorkflowEventId
	(*VariableValue)(nil),         // 3: littlehorse.VariableValue
	(*timestamppb.Timestamp)(nil), // 4: google.protobuf.Timestamp
	(*NodeRunId)(nil),             // 5: littlehorse.NodeRunId
	(*WorkflowEventDefId)(nil),    // 6: littlehorse.WorkflowEventDefId
	(*ReturnType)(nil),            // 7: littlehorse.ReturnType
}
var file_workflow_event_proto_depIdxs = []int32{
	2, // 0: littlehorse.WorkflowEvent.id:type_name -> littlehorse.WorkflowEventId
	3, // 1: littlehorse.WorkflowEvent.content:type_name -> littlehorse.VariableValue
	4, // 2: littlehorse.WorkflowEvent.created_at:type_name -> google.protobuf.Timestamp
	5, // 3: littlehorse.WorkflowEvent.node_run_id:type_name -> littlehorse.NodeRunId
	6, // 4: littlehorse.WorkflowEventDef.id:type_name -> littlehorse.WorkflowEventDefId
	4, // 5: littlehorse.WorkflowEventDef.created_at:type_name -> google.protobuf.Timestamp
	7, // 6: littlehorse.WorkflowEventDef.content_type:type_name -> littlehorse.ReturnType
	7, // [7:7] is the sub-list for method output_type
	7, // [7:7] is the sub-list for method input_type
	7, // [7:7] is the sub-list for extension type_name
	7, // [7:7] is the sub-list for extension extendee
	0, // [0:7] is the sub-list for field type_name
}

func init() { file_workflow_event_proto_init() }
func file_workflow_event_proto_init() {
	if File_workflow_event_proto != nil {
		return
	}
	file_variable_proto_init()
	file_object_id_proto_init()
	file_common_wfspec_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_workflow_event_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WorkflowEvent); i {
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
		file_workflow_event_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*WorkflowEventDef); i {
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
			RawDescriptor: file_workflow_event_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   2,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_workflow_event_proto_goTypes,
		DependencyIndexes: file_workflow_event_proto_depIdxs,
		MessageInfos:      file_workflow_event_proto_msgTypes,
	}.Build()
	File_workflow_event_proto = out.File
	file_workflow_event_proto_rawDesc = nil
	file_workflow_event_proto_goTypes = nil
	file_workflow_event_proto_depIdxs = nil
}
