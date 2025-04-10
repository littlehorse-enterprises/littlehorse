// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v4.23.4
// source: common_enums.proto

package lhproto

import (
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

// Status used for WfRun, ThreadRun, and NodeRun
type LHStatus int32

const (
	// The entity is starting.
	LHStatus_STARTING LHStatus = 0
	// The entity is running.
	LHStatus_RUNNING LHStatus = 1
	// The entity is completed. This is a terminal state.
	LHStatus_COMPLETED LHStatus = 2
	// The entity will move to `HALTED` as soon as all preconditions for halting are
	// satisfied.
	LHStatus_HALTING LHStatus = 3
	// The entity is halted, either by user intervention or by the workflow scheduler.
	LHStatus_HALTED LHStatus = 4
	// The entity has failed due to a technical failure, such as a type conversion error,
	// a task timeout due to network failure, or a task worker crash.
	LHStatus_ERROR LHStatus = 5
	// The entity has failed due to a business exception defined by the user.
	LHStatus_EXCEPTION LHStatus = 6
)

// Enum value maps for LHStatus.
var (
	LHStatus_name = map[int32]string{
		0: "STARTING",
		1: "RUNNING",
		2: "COMPLETED",
		3: "HALTING",
		4: "HALTED",
		5: "ERROR",
		6: "EXCEPTION",
	}
	LHStatus_value = map[string]int32{
		"STARTING":  0,
		"RUNNING":   1,
		"COMPLETED": 2,
		"HALTING":   3,
		"HALTED":    4,
		"ERROR":     5,
		"EXCEPTION": 6,
	}
)

func (x LHStatus) Enum() *LHStatus {
	p := new(LHStatus)
	*p = x
	return p
}

func (x LHStatus) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (LHStatus) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[0].Descriptor()
}

func (LHStatus) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[0]
}

func (x LHStatus) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use LHStatus.Descriptor instead.
func (LHStatus) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{0}
}

// Status of a Metadata Object, such as WfSpec or TaskDef
type MetadataStatus int32

const (
	// ACTIVE means the object can be used.
	MetadataStatus_ACTIVE MetadataStatus = 0
	// An ARCHIVED WfSpec can no longer be used to create new WfRun's, but
	// existing WfRun's will be allowed to run to completion.
	MetadataStatus_ARCHIVED MetadataStatus = 1
	// A TERMINATING WfSpec is actively deleting all running WfRun's, and will
	// self-destruct once all of its child WfRun's are terminated.
	MetadataStatus_TERMINATING MetadataStatus = 2
)

// Enum value maps for MetadataStatus.
var (
	MetadataStatus_name = map[int32]string{
		0: "ACTIVE",
		1: "ARCHIVED",
		2: "TERMINATING",
	}
	MetadataStatus_value = map[string]int32{
		"ACTIVE":      0,
		"ARCHIVED":    1,
		"TERMINATING": 2,
	}
)

func (x MetadataStatus) Enum() *MetadataStatus {
	p := new(MetadataStatus)
	*p = x
	return p
}

func (x MetadataStatus) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (MetadataStatus) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[1].Descriptor()
}

func (MetadataStatus) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[1]
}

func (x MetadataStatus) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use MetadataStatus.Descriptor instead.
func (MetadataStatus) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{1}
}

// Status of a TaskRun.
type TaskStatus int32

const (
	// Scheduled in the Task Queue but not yet picked up by a Task Worker.
	TaskStatus_TASK_SCHEDULED TaskStatus = 0
	// Picked up by a Task Worker, but not yet reported or timed out.
	TaskStatus_TASK_RUNNING TaskStatus = 1
	// Successfully completed.
	TaskStatus_TASK_SUCCESS TaskStatus = 2
	// Task Worker reported a technical failure while attempting to execute the TaskRun
	TaskStatus_TASK_FAILED TaskStatus = 3
	// Task Worker did not report a result in time.
	TaskStatus_TASK_TIMEOUT TaskStatus = 4
	// Task Worker reported that it was unable to serialize the output of the TaskRun.
	TaskStatus_TASK_OUTPUT_SERIALIZING_ERROR TaskStatus = 5
	// Task Worker was unable to deserialize the input variables into appropriate language-specific
	// objects to pass into the Task Function
	TaskStatus_TASK_INPUT_VAR_SUB_ERROR TaskStatus = 6
	// Task Function business logic determined that there was a business exception.
	TaskStatus_TASK_EXCEPTION TaskStatus = 8
	// Refers to a TaskAttempt that is not yet scheduled. This happens when using retries
	// with an ExponentialBackoffRetryPolicy: the TaskAttempt isn't supposed to be scheduled
	// until it "matures", but it does already exist.
	TaskStatus_TASK_PENDING TaskStatus = 9
)

// Enum value maps for TaskStatus.
var (
	TaskStatus_name = map[int32]string{
		0: "TASK_SCHEDULED",
		1: "TASK_RUNNING",
		2: "TASK_SUCCESS",
		3: "TASK_FAILED",
		4: "TASK_TIMEOUT",
		5: "TASK_OUTPUT_SERIALIZING_ERROR",
		6: "TASK_INPUT_VAR_SUB_ERROR",
		8: "TASK_EXCEPTION",
		9: "TASK_PENDING",
	}
	TaskStatus_value = map[string]int32{
		"TASK_SCHEDULED":                0,
		"TASK_RUNNING":                  1,
		"TASK_SUCCESS":                  2,
		"TASK_FAILED":                   3,
		"TASK_TIMEOUT":                  4,
		"TASK_OUTPUT_SERIALIZING_ERROR": 5,
		"TASK_INPUT_VAR_SUB_ERROR":      6,
		"TASK_EXCEPTION":                8,
		"TASK_PENDING":                  9,
	}
)

func (x TaskStatus) Enum() *TaskStatus {
	p := new(TaskStatus)
	*p = x
	return p
}

func (x TaskStatus) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (TaskStatus) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[2].Descriptor()
}

func (TaskStatus) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[2]
}

func (x TaskStatus) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use TaskStatus.Descriptor instead.
func (TaskStatus) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{2}
}

// Metrics
type MetricsWindowLength int32

const (
	MetricsWindowLength_MINUTES_5 MetricsWindowLength = 0
	MetricsWindowLength_HOURS_2   MetricsWindowLength = 1
	MetricsWindowLength_DAYS_1    MetricsWindowLength = 2
)

// Enum value maps for MetricsWindowLength.
var (
	MetricsWindowLength_name = map[int32]string{
		0: "MINUTES_5",
		1: "HOURS_2",
		2: "DAYS_1",
	}
	MetricsWindowLength_value = map[string]int32{
		"MINUTES_5": 0,
		"HOURS_2":   1,
		"DAYS_1":    2,
	}
)

func (x MetricsWindowLength) Enum() *MetricsWindowLength {
	p := new(MetricsWindowLength)
	*p = x
	return p
}

func (x MetricsWindowLength) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (MetricsWindowLength) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[3].Descriptor()
}

func (MetricsWindowLength) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[3]
}

func (x MetricsWindowLength) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use MetricsWindowLength.Descriptor instead.
func (MetricsWindowLength) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{3}
}

// Specifies a primitive type in LittleHorse.
type VariableType int32

const (
	// An object represented as a json string. <br/>
	//
	// The `JSON_OBJ` variable allows you to store complex objects in the JSON format.
	// When using the Java and GoLang SDK's, the `JSON_OBJ` variable type is often
	// used transparently to the user. For example, the Java Task Worker SDK can
	// inspect your method signature and automatically deserialize an input variable
	// into a POJO.
	VariableType_JSON_OBJ VariableType = 0
	// The `JSON_ARR` variable allows you to store collections of objects as a JSON
	// array. The behavior is similar to the `JSON_OBJ` variable type.
	VariableType_JSON_ARR VariableType = 1
	// The `DOUBLE` variable type is a 64-bit floating point number. It can
	// be cast to an `INT`.
	VariableType_DOUBLE VariableType = 2
	// Boolean denotes a simple boolean switch.
	VariableType_BOOL VariableType = 3
	// The `STR` variable type is stored as a String. `INT`, `DOUBLE`,
	// and `BOOL` variables can be cast to a `STR`.
	VariableType_STR VariableType = 4
	// The `INT` variable type is stored as a 64-bit integer. The
	// `INT` can be cast to a `DOUBLE`.
	VariableType_INT VariableType = 5
	// The `BYTES` variable type allows you to store an arbitrary byte string.
	VariableType_BYTES VariableType = 6
)

// Enum value maps for VariableType.
var (
	VariableType_name = map[int32]string{
		0: "JSON_OBJ",
		1: "JSON_ARR",
		2: "DOUBLE",
		3: "BOOL",
		4: "STR",
		5: "INT",
		6: "BYTES",
	}
	VariableType_value = map[string]int32{
		"JSON_OBJ": 0,
		"JSON_ARR": 1,
		"DOUBLE":   2,
		"BOOL":     3,
		"STR":      4,
		"INT":      5,
		"BYTES":    6,
	}
)

func (x VariableType) Enum() *VariableType {
	p := new(VariableType)
	*p = x
	return p
}

func (x VariableType) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (VariableType) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[4].Descriptor()
}

func (VariableType) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[4]
}

func (x VariableType) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use VariableType.Descriptor instead.
func (VariableType) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{4}
}

// This enum is all of the types of technical failure that can occur in a WfRun.
type LHErrorType int32

const (
	// A child ThreadRun failed with a technical ERROR.
	LHErrorType_CHILD_FAILURE LHErrorType = 0
	// Failed substituting input variables into a NodeRun.
	LHErrorType_VAR_SUB_ERROR LHErrorType = 1
	// Failed mutating variables after a NodeRun successfully completed.
	LHErrorType_VAR_MUTATION_ERROR LHErrorType = 2
	// A UserTaskRun was cancelled (EVOLVING: this will become a Business EXCEPTION)
	LHErrorType_USER_TASK_CANCELLED LHErrorType = 3
	// A NodeRun failed due to a timeout.
	LHErrorType_TIMEOUT LHErrorType = 4
	// A TaskRun failed due to an unexpected error.
	LHErrorType_TASK_FAILURE LHErrorType = 5
	// Wrapper for VAR_SUB_ERROR and VAR_MUTATION_ERROR
	LHErrorType_VAR_ERROR LHErrorType = 6
	// Wrapper for TASK_FALIURE and TIMEOUT
	LHErrorType_TASK_ERROR LHErrorType = 7
	// An unexpected LittleHorse Internal error occurred. This is not expected to happen.
	LHErrorType_INTERNAL_ERROR LHErrorType = 8
)

// Enum value maps for LHErrorType.
var (
	LHErrorType_name = map[int32]string{
		0: "CHILD_FAILURE",
		1: "VAR_SUB_ERROR",
		2: "VAR_MUTATION_ERROR",
		3: "USER_TASK_CANCELLED",
		4: "TIMEOUT",
		5: "TASK_FAILURE",
		6: "VAR_ERROR",
		7: "TASK_ERROR",
		8: "INTERNAL_ERROR",
	}
	LHErrorType_value = map[string]int32{
		"CHILD_FAILURE":       0,
		"VAR_SUB_ERROR":       1,
		"VAR_MUTATION_ERROR":  2,
		"USER_TASK_CANCELLED": 3,
		"TIMEOUT":             4,
		"TASK_FAILURE":        5,
		"VAR_ERROR":           6,
		"TASK_ERROR":          7,
		"INTERNAL_ERROR":      8,
	}
)

func (x LHErrorType) Enum() *LHErrorType {
	p := new(LHErrorType)
	*p = x
	return p
}

func (x LHErrorType) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (LHErrorType) Descriptor() protoreflect.EnumDescriptor {
	return file_common_enums_proto_enumTypes[5].Descriptor()
}

func (LHErrorType) Type() protoreflect.EnumType {
	return &file_common_enums_proto_enumTypes[5]
}

func (x LHErrorType) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use LHErrorType.Descriptor instead.
func (LHErrorType) EnumDescriptor() ([]byte, []int) {
	return file_common_enums_proto_rawDescGZIP(), []int{5}
}

var File_common_enums_proto protoreflect.FileDescriptor

var file_common_enums_proto_rawDesc = []byte{
	0x0a, 0x12, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x5f, 0x65, 0x6e, 0x75, 0x6d, 0x73, 0x2e, 0x70,
	0x72, 0x6f, 0x74, 0x6f, 0x12, 0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2a, 0x67, 0x0a, 0x08, 0x4c, 0x48, 0x53, 0x74, 0x61, 0x74, 0x75, 0x73, 0x12, 0x0c, 0x0a,
	0x08, 0x53, 0x54, 0x41, 0x52, 0x54, 0x49, 0x4e, 0x47, 0x10, 0x00, 0x12, 0x0b, 0x0a, 0x07, 0x52,
	0x55, 0x4e, 0x4e, 0x49, 0x4e, 0x47, 0x10, 0x01, 0x12, 0x0d, 0x0a, 0x09, 0x43, 0x4f, 0x4d, 0x50,
	0x4c, 0x45, 0x54, 0x45, 0x44, 0x10, 0x02, 0x12, 0x0b, 0x0a, 0x07, 0x48, 0x41, 0x4c, 0x54, 0x49,
	0x4e, 0x47, 0x10, 0x03, 0x12, 0x0a, 0x0a, 0x06, 0x48, 0x41, 0x4c, 0x54, 0x45, 0x44, 0x10, 0x04,
	0x12, 0x09, 0x0a, 0x05, 0x45, 0x52, 0x52, 0x4f, 0x52, 0x10, 0x05, 0x12, 0x0d, 0x0a, 0x09, 0x45,
	0x58, 0x43, 0x45, 0x50, 0x54, 0x49, 0x4f, 0x4e, 0x10, 0x06, 0x2a, 0x3b, 0x0a, 0x0e, 0x4d, 0x65,
	0x74, 0x61, 0x64, 0x61, 0x74, 0x61, 0x53, 0x74, 0x61, 0x74, 0x75, 0x73, 0x12, 0x0a, 0x0a, 0x06,
	0x41, 0x43, 0x54, 0x49, 0x56, 0x45, 0x10, 0x00, 0x12, 0x0c, 0x0a, 0x08, 0x41, 0x52, 0x43, 0x48,
	0x49, 0x56, 0x45, 0x44, 0x10, 0x01, 0x12, 0x0f, 0x0a, 0x0b, 0x54, 0x45, 0x52, 0x4d, 0x49, 0x4e,
	0x41, 0x54, 0x49, 0x4e, 0x47, 0x10, 0x02, 0x2a, 0xce, 0x01, 0x0a, 0x0a, 0x54, 0x61, 0x73, 0x6b,
	0x53, 0x74, 0x61, 0x74, 0x75, 0x73, 0x12, 0x12, 0x0a, 0x0e, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x53,
	0x43, 0x48, 0x45, 0x44, 0x55, 0x4c, 0x45, 0x44, 0x10, 0x00, 0x12, 0x10, 0x0a, 0x0c, 0x54, 0x41,
	0x53, 0x4b, 0x5f, 0x52, 0x55, 0x4e, 0x4e, 0x49, 0x4e, 0x47, 0x10, 0x01, 0x12, 0x10, 0x0a, 0x0c,
	0x54, 0x41, 0x53, 0x4b, 0x5f, 0x53, 0x55, 0x43, 0x43, 0x45, 0x53, 0x53, 0x10, 0x02, 0x12, 0x0f,
	0x0a, 0x0b, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x46, 0x41, 0x49, 0x4c, 0x45, 0x44, 0x10, 0x03, 0x12,
	0x10, 0x0a, 0x0c, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x54, 0x49, 0x4d, 0x45, 0x4f, 0x55, 0x54, 0x10,
	0x04, 0x12, 0x21, 0x0a, 0x1d, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x4f, 0x55, 0x54, 0x50, 0x55, 0x54,
	0x5f, 0x53, 0x45, 0x52, 0x49, 0x41, 0x4c, 0x49, 0x5a, 0x49, 0x4e, 0x47, 0x5f, 0x45, 0x52, 0x52,
	0x4f, 0x52, 0x10, 0x05, 0x12, 0x1c, 0x0a, 0x18, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x49, 0x4e, 0x50,
	0x55, 0x54, 0x5f, 0x56, 0x41, 0x52, 0x5f, 0x53, 0x55, 0x42, 0x5f, 0x45, 0x52, 0x52, 0x4f, 0x52,
	0x10, 0x06, 0x12, 0x12, 0x0a, 0x0e, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x45, 0x58, 0x43, 0x45, 0x50,
	0x54, 0x49, 0x4f, 0x4e, 0x10, 0x08, 0x12, 0x10, 0x0a, 0x0c, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x50,
	0x45, 0x4e, 0x44, 0x49, 0x4e, 0x47, 0x10, 0x09, 0x2a, 0x3d, 0x0a, 0x13, 0x4d, 0x65, 0x74, 0x72,
	0x69, 0x63, 0x73, 0x57, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x4c, 0x65, 0x6e, 0x67, 0x74, 0x68, 0x12,
	0x0d, 0x0a, 0x09, 0x4d, 0x49, 0x4e, 0x55, 0x54, 0x45, 0x53, 0x5f, 0x35, 0x10, 0x00, 0x12, 0x0b,
	0x0a, 0x07, 0x48, 0x4f, 0x55, 0x52, 0x53, 0x5f, 0x32, 0x10, 0x01, 0x12, 0x0a, 0x0a, 0x06, 0x44,
	0x41, 0x59, 0x53, 0x5f, 0x31, 0x10, 0x02, 0x2a, 0x5d, 0x0a, 0x0c, 0x56, 0x61, 0x72, 0x69, 0x61,
	0x62, 0x6c, 0x65, 0x54, 0x79, 0x70, 0x65, 0x12, 0x0c, 0x0a, 0x08, 0x4a, 0x53, 0x4f, 0x4e, 0x5f,
	0x4f, 0x42, 0x4a, 0x10, 0x00, 0x12, 0x0c, 0x0a, 0x08, 0x4a, 0x53, 0x4f, 0x4e, 0x5f, 0x41, 0x52,
	0x52, 0x10, 0x01, 0x12, 0x0a, 0x0a, 0x06, 0x44, 0x4f, 0x55, 0x42, 0x4c, 0x45, 0x10, 0x02, 0x12,
	0x08, 0x0a, 0x04, 0x42, 0x4f, 0x4f, 0x4c, 0x10, 0x03, 0x12, 0x07, 0x0a, 0x03, 0x53, 0x54, 0x52,
	0x10, 0x04, 0x12, 0x07, 0x0a, 0x03, 0x49, 0x4e, 0x54, 0x10, 0x05, 0x12, 0x09, 0x0a, 0x05, 0x42,
	0x59, 0x54, 0x45, 0x53, 0x10, 0x06, 0x2a, 0xb6, 0x01, 0x0a, 0x0b, 0x4c, 0x48, 0x45, 0x72, 0x72,
	0x6f, 0x72, 0x54, 0x79, 0x70, 0x65, 0x12, 0x11, 0x0a, 0x0d, 0x43, 0x48, 0x49, 0x4c, 0x44, 0x5f,
	0x46, 0x41, 0x49, 0x4c, 0x55, 0x52, 0x45, 0x10, 0x00, 0x12, 0x11, 0x0a, 0x0d, 0x56, 0x41, 0x52,
	0x5f, 0x53, 0x55, 0x42, 0x5f, 0x45, 0x52, 0x52, 0x4f, 0x52, 0x10, 0x01, 0x12, 0x16, 0x0a, 0x12,
	0x56, 0x41, 0x52, 0x5f, 0x4d, 0x55, 0x54, 0x41, 0x54, 0x49, 0x4f, 0x4e, 0x5f, 0x45, 0x52, 0x52,
	0x4f, 0x52, 0x10, 0x02, 0x12, 0x17, 0x0a, 0x13, 0x55, 0x53, 0x45, 0x52, 0x5f, 0x54, 0x41, 0x53,
	0x4b, 0x5f, 0x43, 0x41, 0x4e, 0x43, 0x45, 0x4c, 0x4c, 0x45, 0x44, 0x10, 0x03, 0x12, 0x0b, 0x0a,
	0x07, 0x54, 0x49, 0x4d, 0x45, 0x4f, 0x55, 0x54, 0x10, 0x04, 0x12, 0x10, 0x0a, 0x0c, 0x54, 0x41,
	0x53, 0x4b, 0x5f, 0x46, 0x41, 0x49, 0x4c, 0x55, 0x52, 0x45, 0x10, 0x05, 0x12, 0x0d, 0x0a, 0x09,
	0x56, 0x41, 0x52, 0x5f, 0x45, 0x52, 0x52, 0x4f, 0x52, 0x10, 0x06, 0x12, 0x0e, 0x0a, 0x0a, 0x54,
	0x41, 0x53, 0x4b, 0x5f, 0x45, 0x52, 0x52, 0x4f, 0x52, 0x10, 0x07, 0x12, 0x12, 0x0a, 0x0e, 0x49,
	0x4e, 0x54, 0x45, 0x52, 0x4e, 0x41, 0x4c, 0x5f, 0x45, 0x52, 0x52, 0x4f, 0x52, 0x10, 0x08, 0x42,
	0x4d, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x50, 0x01, 0x5a, 0x09, 0x2e, 0x3b, 0x6c, 0x68, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0xaa,
	0x02, 0x1c, 0x4c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x48, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53, 0x64,
	0x6b, 0x2e, 0x43, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x50, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x06,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_common_enums_proto_rawDescOnce sync.Once
	file_common_enums_proto_rawDescData = file_common_enums_proto_rawDesc
)

func file_common_enums_proto_rawDescGZIP() []byte {
	file_common_enums_proto_rawDescOnce.Do(func() {
		file_common_enums_proto_rawDescData = protoimpl.X.CompressGZIP(file_common_enums_proto_rawDescData)
	})
	return file_common_enums_proto_rawDescData
}

var file_common_enums_proto_enumTypes = make([]protoimpl.EnumInfo, 6)
var file_common_enums_proto_goTypes = []interface{}{
	(LHStatus)(0),            // 0: littlehorse.LHStatus
	(MetadataStatus)(0),      // 1: littlehorse.MetadataStatus
	(TaskStatus)(0),          // 2: littlehorse.TaskStatus
	(MetricsWindowLength)(0), // 3: littlehorse.MetricsWindowLength
	(VariableType)(0),        // 4: littlehorse.VariableType
	(LHErrorType)(0),         // 5: littlehorse.LHErrorType
}
var file_common_enums_proto_depIdxs = []int32{
	0, // [0:0] is the sub-list for method output_type
	0, // [0:0] is the sub-list for method input_type
	0, // [0:0] is the sub-list for extension type_name
	0, // [0:0] is the sub-list for extension extendee
	0, // [0:0] is the sub-list for field type_name
}

func init() { file_common_enums_proto_init() }
func file_common_enums_proto_init() {
	if File_common_enums_proto != nil {
		return
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_common_enums_proto_rawDesc,
			NumEnums:      6,
			NumMessages:   0,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_common_enums_proto_goTypes,
		DependencyIndexes: file_common_enums_proto_depIdxs,
		EnumInfos:         file_common_enums_proto_enumTypes,
	}.Build()
	File_common_enums_proto = out.File
	file_common_enums_proto_rawDesc = nil
	file_common_enums_proto_goTypes = nil
	file_common_enums_proto_depIdxs = nil
}
