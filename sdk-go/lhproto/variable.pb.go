// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v5.29.3
// source: variable.proto

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

// VariableValue is a structure containing a value in LittleHorse. It can be
// used to pass input variables into a WfRun/ThreadRun/TaskRun/etc, as output
// from a TaskRun, as the value of a WfRun's Variable, etc.
type VariableValue struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The value held in this VariableValue. If this is unset, treat it as
	// a NULL.
	//
	// Types that are assignable to Value:
	//	*VariableValue_JsonObj
	//	*VariableValue_JsonArr
	//	*VariableValue_Double
	//	*VariableValue_Bool
	//	*VariableValue_Str
	//	*VariableValue_Int
	//	*VariableValue_Bytes
	Value isVariableValue_Value `protobuf_oneof:"value"`
}

func (x *VariableValue) Reset() {
	*x = VariableValue{}
	if protoimpl.UnsafeEnabled {
		mi := &file_variable_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *VariableValue) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*VariableValue) ProtoMessage() {}

func (x *VariableValue) ProtoReflect() protoreflect.Message {
	mi := &file_variable_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use VariableValue.ProtoReflect.Descriptor instead.
func (*VariableValue) Descriptor() ([]byte, []int) {
	return file_variable_proto_rawDescGZIP(), []int{0}
}

func (m *VariableValue) GetValue() isVariableValue_Value {
	if m != nil {
		return m.Value
	}
	return nil
}

func (x *VariableValue) GetJsonObj() string {
	if x, ok := x.GetValue().(*VariableValue_JsonObj); ok {
		return x.JsonObj
	}
	return ""
}

func (x *VariableValue) GetJsonArr() string {
	if x, ok := x.GetValue().(*VariableValue_JsonArr); ok {
		return x.JsonArr
	}
	return ""
}

func (x *VariableValue) GetDouble() float64 {
	if x, ok := x.GetValue().(*VariableValue_Double); ok {
		return x.Double
	}
	return 0
}

func (x *VariableValue) GetBool() bool {
	if x, ok := x.GetValue().(*VariableValue_Bool); ok {
		return x.Bool
	}
	return false
}

func (x *VariableValue) GetStr() string {
	if x, ok := x.GetValue().(*VariableValue_Str); ok {
		return x.Str
	}
	return ""
}

func (x *VariableValue) GetInt() int64 {
	if x, ok := x.GetValue().(*VariableValue_Int); ok {
		return x.Int
	}
	return 0
}

func (x *VariableValue) GetBytes() []byte {
	if x, ok := x.GetValue().(*VariableValue_Bytes); ok {
		return x.Bytes
	}
	return nil
}

type isVariableValue_Value interface {
	isVariableValue_Value()
}

type VariableValue_JsonObj struct {
	// A String representing a serialized json object.
	JsonObj string `protobuf:"bytes,2,opt,name=json_obj,json=jsonObj,proto3,oneof"`
}

type VariableValue_JsonArr struct {
	// A String representing a serialized json list.
	JsonArr string `protobuf:"bytes,3,opt,name=json_arr,json=jsonArr,proto3,oneof"`
}

type VariableValue_Double struct {
	// A 64-bit floating point number.
	Double float64 `protobuf:"fixed64,4,opt,name=double,proto3,oneof"`
}

type VariableValue_Bool struct {
	// A boolean.
	Bool bool `protobuf:"varint,5,opt,name=bool,proto3,oneof"`
}

type VariableValue_Str struct {
	// A string.
	Str string `protobuf:"bytes,6,opt,name=str,proto3,oneof"`
}

type VariableValue_Int struct {
	// The `INT` variable type is stored as a 64-bit integer. The
	// `INT` can be cast to a `DOUBLE`.
	Int int64 `protobuf:"varint,7,opt,name=int,proto3,oneof"`
}

type VariableValue_Bytes struct {
	// An arbitrary String of bytes.
	Bytes []byte `protobuf:"bytes,8,opt,name=bytes,proto3,oneof"`
}

func (*VariableValue_JsonObj) isVariableValue_Value() {}

func (*VariableValue_JsonArr) isVariableValue_Value() {}

func (*VariableValue_Double) isVariableValue_Value() {}

func (*VariableValue_Bool) isVariableValue_Value() {}

func (*VariableValue_Str) isVariableValue_Value() {}

func (*VariableValue_Int) isVariableValue_Value() {}

func (*VariableValue_Bytes) isVariableValue_Value() {}

// A Variable is an instance of a variable assigned to a WfRun.
type Variable struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// ID of this Variable. Note that the VariableId contains the relevant
	// WfRunId inside it, the threadRunNumber, and the name of the Variabe.
	Id *VariableId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The value of this Variable.
	Value *VariableValue `protobuf:"bytes,2,opt,name=value,proto3" json:"value,omitempty"`
	// When the Variable was created.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,3,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	// The ID of the WfSpec that this Variable belongs to.
	WfSpecId *WfSpecId `protobuf:"bytes,4,opt,name=wf_spec_id,json=wfSpecId,proto3" json:"wf_spec_id,omitempty"`
	// Marks a variable to show masked values
	Masked bool `protobuf:"varint,5,opt,name=masked,proto3" json:"masked,omitempty"`
}

func (x *Variable) Reset() {
	*x = Variable{}
	if protoimpl.UnsafeEnabled {
		mi := &file_variable_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Variable) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Variable) ProtoMessage() {}

func (x *Variable) ProtoReflect() protoreflect.Message {
	mi := &file_variable_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Variable.ProtoReflect.Descriptor instead.
func (*Variable) Descriptor() ([]byte, []int) {
	return file_variable_proto_rawDescGZIP(), []int{1}
}

func (x *Variable) GetId() *VariableId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *Variable) GetValue() *VariableValue {
	if x != nil {
		return x.Value
	}
	return nil
}

func (x *Variable) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *Variable) GetWfSpecId() *WfSpecId {
	if x != nil {
		return x.WfSpecId
	}
	return nil
}

func (x *Variable) GetMasked() bool {
	if x != nil {
		return x.Masked
	}
	return false
}

var File_variable_proto protoreflect.FileDescriptor

var file_variable_proto_rawDesc = []byte{
	0x0a, 0x0e, 0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a, 0x1f, 0x67,
	0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74,
	0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0f,
	0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22,
	0xc8, 0x01, 0x0a, 0x0d, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75,
	0x65, 0x12, 0x1b, 0x0a, 0x08, 0x6a, 0x73, 0x6f, 0x6e, 0x5f, 0x6f, 0x62, 0x6a, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x09, 0x48, 0x00, 0x52, 0x07, 0x6a, 0x73, 0x6f, 0x6e, 0x4f, 0x62, 0x6a, 0x12, 0x1b,
	0x0a, 0x08, 0x6a, 0x73, 0x6f, 0x6e, 0x5f, 0x61, 0x72, 0x72, 0x18, 0x03, 0x20, 0x01, 0x28, 0x09,
	0x48, 0x00, 0x52, 0x07, 0x6a, 0x73, 0x6f, 0x6e, 0x41, 0x72, 0x72, 0x12, 0x18, 0x0a, 0x06, 0x64,
	0x6f, 0x75, 0x62, 0x6c, 0x65, 0x18, 0x04, 0x20, 0x01, 0x28, 0x01, 0x48, 0x00, 0x52, 0x06, 0x64,
	0x6f, 0x75, 0x62, 0x6c, 0x65, 0x12, 0x14, 0x0a, 0x04, 0x62, 0x6f, 0x6f, 0x6c, 0x18, 0x05, 0x20,
	0x01, 0x28, 0x08, 0x48, 0x00, 0x52, 0x04, 0x62, 0x6f, 0x6f, 0x6c, 0x12, 0x12, 0x0a, 0x03, 0x73,
	0x74, 0x72, 0x18, 0x06, 0x20, 0x01, 0x28, 0x09, 0x48, 0x00, 0x52, 0x03, 0x73, 0x74, 0x72, 0x12,
	0x12, 0x0a, 0x03, 0x69, 0x6e, 0x74, 0x18, 0x07, 0x20, 0x01, 0x28, 0x03, 0x48, 0x00, 0x52, 0x03,
	0x69, 0x6e, 0x74, 0x12, 0x16, 0x0a, 0x05, 0x62, 0x79, 0x74, 0x65, 0x73, 0x18, 0x08, 0x20, 0x01,
	0x28, 0x0c, 0x48, 0x00, 0x52, 0x05, 0x62, 0x79, 0x74, 0x65, 0x73, 0x42, 0x07, 0x0a, 0x05, 0x76,
	0x61, 0x6c, 0x75, 0x65, 0x4a, 0x04, 0x08, 0x01, 0x10, 0x02, 0x22, 0xed, 0x01, 0x0a, 0x08, 0x56,
	0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x12, 0x27, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20,
	0x01, 0x28, 0x0b, 0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64,
	0x12, 0x30, 0x0a, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32,
	0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x56, 0x61,
	0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x52, 0x05, 0x76, 0x61, 0x6c,
	0x75, 0x65, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x5f, 0x61, 0x74,
	0x18, 0x03, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61,
	0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x41, 0x74, 0x12, 0x33, 0x0a,
	0x0a, 0x77, 0x66, 0x5f, 0x73, 0x70, 0x65, 0x63, 0x5f, 0x69, 0x64, 0x18, 0x04, 0x20, 0x01, 0x28,
	0x0b, 0x32, 0x15, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e,
	0x57, 0x66, 0x53, 0x70, 0x65, 0x63, 0x49, 0x64, 0x52, 0x08, 0x77, 0x66, 0x53, 0x70, 0x65, 0x63,
	0x49, 0x64, 0x12, 0x16, 0x0a, 0x06, 0x6d, 0x61, 0x73, 0x6b, 0x65, 0x64, 0x18, 0x05, 0x20, 0x01,
	0x28, 0x08, 0x52, 0x06, 0x6d, 0x61, 0x73, 0x6b, 0x65, 0x64, 0x42, 0x4d, 0x0a, 0x1f, 0x69, 0x6f,
	0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b,
	0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a,
	0x09, 0x2e, 0x3b, 0x6c, 0x68, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0xaa, 0x02, 0x1c, 0x4c, 0x69, 0x74,
	0x74, 0x6c, 0x65, 0x48, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53, 0x64, 0x6b, 0x2e, 0x43, 0x6f, 0x6d,
	0x6d, 0x6f, 0x6e, 0x2e, 0x50, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f,
	0x33,
}

var (
	file_variable_proto_rawDescOnce sync.Once
	file_variable_proto_rawDescData = file_variable_proto_rawDesc
)

func file_variable_proto_rawDescGZIP() []byte {
	file_variable_proto_rawDescOnce.Do(func() {
		file_variable_proto_rawDescData = protoimpl.X.CompressGZIP(file_variable_proto_rawDescData)
	})
	return file_variable_proto_rawDescData
}

var file_variable_proto_msgTypes = make([]protoimpl.MessageInfo, 2)
var file_variable_proto_goTypes = []interface{}{
	(*VariableValue)(nil),         // 0: littlehorse.VariableValue
	(*Variable)(nil),              // 1: littlehorse.Variable
	(*VariableId)(nil),            // 2: littlehorse.VariableId
	(*timestamppb.Timestamp)(nil), // 3: google.protobuf.Timestamp
	(*WfSpecId)(nil),              // 4: littlehorse.WfSpecId
}
var file_variable_proto_depIdxs = []int32{
	2, // 0: littlehorse.Variable.id:type_name -> littlehorse.VariableId
	0, // 1: littlehorse.Variable.value:type_name -> littlehorse.VariableValue
	3, // 2: littlehorse.Variable.created_at:type_name -> google.protobuf.Timestamp
	4, // 3: littlehorse.Variable.wf_spec_id:type_name -> littlehorse.WfSpecId
	4, // [4:4] is the sub-list for method output_type
	4, // [4:4] is the sub-list for method input_type
	4, // [4:4] is the sub-list for extension type_name
	4, // [4:4] is the sub-list for extension extendee
	0, // [0:4] is the sub-list for field type_name
}

func init() { file_variable_proto_init() }
func file_variable_proto_init() {
	if File_variable_proto != nil {
		return
	}
	file_object_id_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_variable_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*VariableValue); i {
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
		file_variable_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Variable); i {
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
	file_variable_proto_msgTypes[0].OneofWrappers = []interface{}{
		(*VariableValue_JsonObj)(nil),
		(*VariableValue_JsonArr)(nil),
		(*VariableValue_Double)(nil),
		(*VariableValue_Bool)(nil),
		(*VariableValue_Str)(nil),
		(*VariableValue_Int)(nil),
		(*VariableValue_Bytes)(nil),
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_variable_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   2,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_variable_proto_goTypes,
		DependencyIndexes: file_variable_proto_depIdxs,
		MessageInfos:      file_variable_proto_msgTypes,
	}.Build()
	File_variable_proto = out.File
	file_variable_proto_rawDesc = nil
	file_variable_proto_goTypes = nil
	file_variable_proto_depIdxs = nil
}
