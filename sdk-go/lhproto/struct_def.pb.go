// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v4.23.4
// source: struct_def.proto

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

// A `StructDef` is a versioned metadata object (tenant-scoped) inside LittleHorse
// that defines the structure and content of a variable value. It allows strong typing.
type StructDef struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The id of the `Schema`. This includes the version.
	Id *StructDefId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// Optionally description of the schema.
	Description *string `protobuf:"bytes,2,opt,name=description,proto3,oneof" json:"description,omitempty"`
	// When the StructDef was created.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,3,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	// The `StructDef` defines the actual structure of any `Struct` using this `InlineStructDeff`.
	StructDef *InlineStructDef `protobuf:"bytes,4,opt,name=struct_def,json=structDef,proto3" json:"struct_def,omitempty"`
}

func (x *StructDef) Reset() {
	*x = StructDef{}
	if protoimpl.UnsafeEnabled {
		mi := &file_struct_def_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *StructDef) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*StructDef) ProtoMessage() {}

func (x *StructDef) ProtoReflect() protoreflect.Message {
	mi := &file_struct_def_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use StructDef.ProtoReflect.Descriptor instead.
func (*StructDef) Descriptor() ([]byte, []int) {
	return file_struct_def_proto_rawDescGZIP(), []int{0}
}

func (x *StructDef) GetId() *StructDefId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *StructDef) GetDescription() string {
	if x != nil && x.Description != nil {
		return *x.Description
	}
	return ""
}

func (x *StructDef) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *StructDef) GetStructDef() *InlineStructDef {
	if x != nil {
		return x.StructDef
	}
	return nil
}

// An `InlineStructDef` is the actual representation of the Schema.
type InlineStructDef struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The fields in this schema.
	Fields map[string]*StructFieldDef `protobuf:"bytes,1,rep,name=fields,proto3" json:"fields,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
}

func (x *InlineStructDef) Reset() {
	*x = InlineStructDef{}
	if protoimpl.UnsafeEnabled {
		mi := &file_struct_def_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *InlineStructDef) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*InlineStructDef) ProtoMessage() {}

func (x *InlineStructDef) ProtoReflect() protoreflect.Message {
	mi := &file_struct_def_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use InlineStructDef.ProtoReflect.Descriptor instead.
func (*InlineStructDef) Descriptor() ([]byte, []int) {
	return file_struct_def_proto_rawDescGZIP(), []int{1}
}

func (x *InlineStructDef) GetFields() map[string]*StructFieldDef {
	if x != nil {
		return x.Fields
	}
	return nil
}

// A `SchemaFieldDef` defines a field inside a `StructDef`.
type StructFieldDef struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The type of the field.
	FieldType *TypeDefinition `protobuf:"bytes,1,opt,name=field_type,json=fieldType,proto3" json:"field_type,omitempty"`
	// The default value of the field, which should match the Field Type. If not
	// provided, then the field is treated as required.
	DefaultValue *VariableValue `protobuf:"bytes,2,opt,name=default_value,json=defaultValue,proto3,oneof" json:"default_value,omitempty"`
}

func (x *StructFieldDef) Reset() {
	*x = StructFieldDef{}
	if protoimpl.UnsafeEnabled {
		mi := &file_struct_def_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *StructFieldDef) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*StructFieldDef) ProtoMessage() {}

func (x *StructFieldDef) ProtoReflect() protoreflect.Message {
	mi := &file_struct_def_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use StructFieldDef.ProtoReflect.Descriptor instead.
func (*StructFieldDef) Descriptor() ([]byte, []int) {
	return file_struct_def_proto_rawDescGZIP(), []int{2}
}

func (x *StructFieldDef) GetFieldType() *TypeDefinition {
	if x != nil {
		return x.FieldType
	}
	return nil
}

func (x *StructFieldDef) GetDefaultValue() *VariableValue {
	if x != nil {
		return x.DefaultValue
	}
	return nil
}

var File_struct_def_proto protoreflect.FileDescriptor

var file_struct_def_proto_rawDesc = []byte{
	0x0a, 0x10, 0x73, 0x74, 0x72, 0x75, 0x63, 0x74, 0x5f, 0x64, 0x65, 0x66, 0x2e, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a,
	0x13, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x5f, 0x77, 0x66, 0x73, 0x70, 0x65, 0x63, 0x2e, 0x70,
	0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0f, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0e, 0x76, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x1f, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0xe4, 0x01, 0x0a, 0x09, 0x53, 0x74, 0x72, 0x75, 0x63,
	0x74, 0x44, 0x65, 0x66, 0x12, 0x28, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b,
	0x32, 0x18, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53,
	0x74, 0x72, 0x75, 0x63, 0x74, 0x44, 0x65, 0x66, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x25,
	0x0a, 0x0b, 0x64, 0x65, 0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x69, 0x6f, 0x6e, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x09, 0x48, 0x00, 0x52, 0x0b, 0x64, 0x65, 0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x69,
	0x6f, 0x6e, 0x88, 0x01, 0x01, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64,
	0x5f, 0x61, 0x74, 0x18, 0x03, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67,
	0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65,
	0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x41, 0x74,
	0x12, 0x3b, 0x0a, 0x0a, 0x73, 0x74, 0x72, 0x75, 0x63, 0x74, 0x5f, 0x64, 0x65, 0x66, 0x18, 0x04,
	0x20, 0x01, 0x28, 0x0b, 0x32, 0x1c, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72,
	0x73, 0x65, 0x2e, 0x49, 0x6e, 0x6c, 0x69, 0x6e, 0x65, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x44,
	0x65, 0x66, 0x52, 0x09, 0x73, 0x74, 0x72, 0x75, 0x63, 0x74, 0x44, 0x65, 0x66, 0x42, 0x0e, 0x0a,
	0x0c, 0x5f, 0x64, 0x65, 0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x69, 0x6f, 0x6e, 0x22, 0xab, 0x01,
	0x0a, 0x0f, 0x49, 0x6e, 0x6c, 0x69, 0x6e, 0x65, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x44, 0x65,
	0x66, 0x12, 0x40, 0x0a, 0x06, 0x66, 0x69, 0x65, 0x6c, 0x64, 0x73, 0x18, 0x01, 0x20, 0x03, 0x28,
	0x0b, 0x32, 0x28, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e,
	0x49, 0x6e, 0x6c, 0x69, 0x6e, 0x65, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x44, 0x65, 0x66, 0x2e,
	0x46, 0x69, 0x65, 0x6c, 0x64, 0x73, 0x45, 0x6e, 0x74, 0x72, 0x79, 0x52, 0x06, 0x66, 0x69, 0x65,
	0x6c, 0x64, 0x73, 0x1a, 0x56, 0x0a, 0x0b, 0x46, 0x69, 0x65, 0x6c, 0x64, 0x73, 0x45, 0x6e, 0x74,
	0x72, 0x79, 0x12, 0x10, 0x0a, 0x03, 0x6b, 0x65, 0x79, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52,
	0x03, 0x6b, 0x65, 0x79, 0x12, 0x31, 0x0a, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x0b, 0x32, 0x1b, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x46, 0x69, 0x65, 0x6c, 0x64, 0x44, 0x65, 0x66,
	0x52, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x3a, 0x02, 0x38, 0x01, 0x22, 0xa4, 0x01, 0x0a, 0x0e,
	0x53, 0x74, 0x72, 0x75, 0x63, 0x74, 0x46, 0x69, 0x65, 0x6c, 0x64, 0x44, 0x65, 0x66, 0x12, 0x3a,
	0x0a, 0x0a, 0x66, 0x69, 0x65, 0x6c, 0x64, 0x5f, 0x74, 0x79, 0x70, 0x65, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x0b, 0x32, 0x1b, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65,
	0x2e, 0x54, 0x79, 0x70, 0x65, 0x44, 0x65, 0x66, 0x69, 0x6e, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x52,
	0x09, 0x66, 0x69, 0x65, 0x6c, 0x64, 0x54, 0x79, 0x70, 0x65, 0x12, 0x44, 0x0a, 0x0d, 0x64, 0x65,
	0x66, 0x61, 0x75, 0x6c, 0x74, 0x5f, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28,
	0x0b, 0x32, 0x1a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e,
	0x56, 0x61, 0x72, 0x69, 0x61, 0x62, 0x6c, 0x65, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x48, 0x00, 0x52,
	0x0c, 0x64, 0x65, 0x66, 0x61, 0x75, 0x6c, 0x74, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x88, 0x01, 0x01,
	0x42, 0x10, 0x0a, 0x0e, 0x5f, 0x64, 0x65, 0x66, 0x61, 0x75, 0x6c, 0x74, 0x5f, 0x76, 0x61, 0x6c,
	0x75, 0x65, 0x42, 0x4d, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68,
	0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x09, 0x2e, 0x3b, 0x6c, 0x68, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0xaa, 0x02, 0x1c, 0x4c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x48, 0x6f, 0x72, 0x73, 0x65,
	0x2e, 0x53, 0x64, 0x6b, 0x2e, 0x43, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x50, 0x72, 0x6f, 0x74,
	0x6f, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_struct_def_proto_rawDescOnce sync.Once
	file_struct_def_proto_rawDescData = file_struct_def_proto_rawDesc
)

func file_struct_def_proto_rawDescGZIP() []byte {
	file_struct_def_proto_rawDescOnce.Do(func() {
		file_struct_def_proto_rawDescData = protoimpl.X.CompressGZIP(file_struct_def_proto_rawDescData)
	})
	return file_struct_def_proto_rawDescData
}

var file_struct_def_proto_msgTypes = make([]protoimpl.MessageInfo, 4)
var file_struct_def_proto_goTypes = []interface{}{
	(*StructDef)(nil),             // 0: littlehorse.StructDef
	(*InlineStructDef)(nil),       // 1: littlehorse.InlineStructDef
	(*StructFieldDef)(nil),        // 2: littlehorse.StructFieldDef
	nil,                           // 3: littlehorse.InlineStructDef.FieldsEntry
	(*StructDefId)(nil),           // 4: littlehorse.StructDefId
	(*timestamppb.Timestamp)(nil), // 5: google.protobuf.Timestamp
	(*TypeDefinition)(nil),        // 6: littlehorse.TypeDefinition
	(*VariableValue)(nil),         // 7: littlehorse.VariableValue
}
var file_struct_def_proto_depIdxs = []int32{
	4, // 0: littlehorse.StructDef.id:type_name -> littlehorse.StructDefId
	5, // 1: littlehorse.StructDef.created_at:type_name -> google.protobuf.Timestamp
	1, // 2: littlehorse.StructDef.struct_def:type_name -> littlehorse.InlineStructDef
	3, // 3: littlehorse.InlineStructDef.fields:type_name -> littlehorse.InlineStructDef.FieldsEntry
	6, // 4: littlehorse.StructFieldDef.field_type:type_name -> littlehorse.TypeDefinition
	7, // 5: littlehorse.StructFieldDef.default_value:type_name -> littlehorse.VariableValue
	2, // 6: littlehorse.InlineStructDef.FieldsEntry.value:type_name -> littlehorse.StructFieldDef
	7, // [7:7] is the sub-list for method output_type
	7, // [7:7] is the sub-list for method input_type
	7, // [7:7] is the sub-list for extension type_name
	7, // [7:7] is the sub-list for extension extendee
	0, // [0:7] is the sub-list for field type_name
}

func init() { file_struct_def_proto_init() }
func file_struct_def_proto_init() {
	if File_struct_def_proto != nil {
		return
	}
	file_common_wfspec_proto_init()
	file_object_id_proto_init()
	file_variable_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_struct_def_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*StructDef); i {
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
		file_struct_def_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*InlineStructDef); i {
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
		file_struct_def_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*StructFieldDef); i {
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
	file_struct_def_proto_msgTypes[0].OneofWrappers = []interface{}{}
	file_struct_def_proto_msgTypes[2].OneofWrappers = []interface{}{}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_struct_def_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   4,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_struct_def_proto_goTypes,
		DependencyIndexes: file_struct_def_proto_depIdxs,
		MessageInfos:      file_struct_def_proto_msgTypes,
	}.Build()
	File_struct_def_proto = out.File
	file_struct_def_proto_rawDesc = nil
	file_struct_def_proto_goTypes = nil
	file_struct_def_proto_depIdxs = nil
}
