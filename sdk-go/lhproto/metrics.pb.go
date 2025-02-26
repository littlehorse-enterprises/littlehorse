// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v4.23.4
// source: metrics.proto

package lhproto

import (
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	durationpb "google.golang.org/protobuf/types/known/durationpb"
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

type MetricSpec struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id            *MetricSpecId          `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	CreatedAt     *timestamppb.Timestamp `protobuf:"bytes,2,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	WindowLengths []*durationpb.Duration `protobuf:"bytes,3,rep,name=window_lengths,json=windowLengths,proto3" json:"window_lengths,omitempty"`
}

func (x *MetricSpec) Reset() {
	*x = MetricSpec{}
	if protoimpl.UnsafeEnabled {
		mi := &file_metrics_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *MetricSpec) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*MetricSpec) ProtoMessage() {}

func (x *MetricSpec) ProtoReflect() protoreflect.Message {
	mi := &file_metrics_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use MetricSpec.ProtoReflect.Descriptor instead.
func (*MetricSpec) Descriptor() ([]byte, []int) {
	return file_metrics_proto_rawDescGZIP(), []int{0}
}

func (x *MetricSpec) GetId() *MetricSpecId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *MetricSpec) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *MetricSpec) GetWindowLengths() []*durationpb.Duration {
	if x != nil {
		return x.WindowLengths
	}
	return nil
}

type PartitionMetric struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id            *PartitionMetricId         `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	CreatedAt     *timestamppb.Timestamp     `protobuf:"bytes,2,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	ActiveWindows []*PartitionWindowedMetric `protobuf:"bytes,3,rep,name=active_windows,json=activeWindows,proto3" json:"active_windows,omitempty"`
	WindowLength  *durationpb.Duration       `protobuf:"bytes,4,opt,name=window_length,json=windowLength,proto3" json:"window_length,omitempty"`
}

func (x *PartitionMetric) Reset() {
	*x = PartitionMetric{}
	if protoimpl.UnsafeEnabled {
		mi := &file_metrics_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *PartitionMetric) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*PartitionMetric) ProtoMessage() {}

func (x *PartitionMetric) ProtoReflect() protoreflect.Message {
	mi := &file_metrics_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use PartitionMetric.ProtoReflect.Descriptor instead.
func (*PartitionMetric) Descriptor() ([]byte, []int) {
	return file_metrics_proto_rawDescGZIP(), []int{1}
}

func (x *PartitionMetric) GetId() *PartitionMetricId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *PartitionMetric) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *PartitionMetric) GetActiveWindows() []*PartitionWindowedMetric {
	if x != nil {
		return x.ActiveWindows
	}
	return nil
}

func (x *PartitionMetric) GetWindowLength() *durationpb.Duration {
	if x != nil {
		return x.WindowLength
	}
	return nil
}

type PartitionWindowedMetric struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Value           float64                `protobuf:"fixed64,1,opt,name=value,proto3" json:"value,omitempty"`
	WindowStart     *timestamppb.Timestamp `protobuf:"bytes,2,opt,name=window_start,json=windowStart,proto3" json:"window_start,omitempty"`
	NumberOfSamples int64                  `protobuf:"varint,3,opt,name=number_of_samples,json=numberOfSamples,proto3" json:"number_of_samples,omitempty"`
}

func (x *PartitionWindowedMetric) Reset() {
	*x = PartitionWindowedMetric{}
	if protoimpl.UnsafeEnabled {
		mi := &file_metrics_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *PartitionWindowedMetric) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*PartitionWindowedMetric) ProtoMessage() {}

func (x *PartitionWindowedMetric) ProtoReflect() protoreflect.Message {
	mi := &file_metrics_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use PartitionWindowedMetric.ProtoReflect.Descriptor instead.
func (*PartitionWindowedMetric) Descriptor() ([]byte, []int) {
	return file_metrics_proto_rawDescGZIP(), []int{2}
}

func (x *PartitionWindowedMetric) GetValue() float64 {
	if x != nil {
		return x.Value
	}
	return 0
}

func (x *PartitionWindowedMetric) GetWindowStart() *timestamppb.Timestamp {
	if x != nil {
		return x.WindowStart
	}
	return nil
}

func (x *PartitionWindowedMetric) GetNumberOfSamples() int64 {
	if x != nil {
		return x.NumberOfSamples
	}
	return 0
}

type PartitionMetricId struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id       *MetricSpecId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	TenantId *TenantId     `protobuf:"bytes,2,opt,name=tenant_id,json=tenantId,proto3" json:"tenant_id,omitempty"`
}

func (x *PartitionMetricId) Reset() {
	*x = PartitionMetricId{}
	if protoimpl.UnsafeEnabled {
		mi := &file_metrics_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *PartitionMetricId) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*PartitionMetricId) ProtoMessage() {}

func (x *PartitionMetricId) ProtoReflect() protoreflect.Message {
	mi := &file_metrics_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use PartitionMetricId.ProtoReflect.Descriptor instead.
func (*PartitionMetricId) Descriptor() ([]byte, []int) {
	return file_metrics_proto_rawDescGZIP(), []int{3}
}

func (x *PartitionMetricId) GetId() *MetricSpecId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *PartitionMetricId) GetTenantId() *TenantId {
	if x != nil {
		return x.TenantId
	}
	return nil
}

// Metric value for a given MetricId
type Metric struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// Unique id of the metric value
	Id *MetricId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// Types that are assignable to Value:
	//	*Metric_Count
	//	*Metric_LatencyAvg
	Value isMetric_Value `protobuf_oneof:"value"`
	// Indicates when the metric was created
	CreatedAt         *timestamppb.Timestamp `protobuf:"bytes,4,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	ValuePerPartition map[int32]float64      `protobuf:"bytes,5,rep,name=value_per_partition,json=valuePerPartition,proto3" json:"value_per_partition,omitempty" protobuf_key:"varint,1,opt,name=key,proto3" protobuf_val:"fixed64,2,opt,name=value,proto3"`
}

func (x *Metric) Reset() {
	*x = Metric{}
	if protoimpl.UnsafeEnabled {
		mi := &file_metrics_proto_msgTypes[4]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Metric) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Metric) ProtoMessage() {}

func (x *Metric) ProtoReflect() protoreflect.Message {
	mi := &file_metrics_proto_msgTypes[4]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Metric.ProtoReflect.Descriptor instead.
func (*Metric) Descriptor() ([]byte, []int) {
	return file_metrics_proto_rawDescGZIP(), []int{4}
}

func (x *Metric) GetId() *MetricId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (m *Metric) GetValue() isMetric_Value {
	if m != nil {
		return m.Value
	}
	return nil
}

func (x *Metric) GetCount() int64 {
	if x, ok := x.GetValue().(*Metric_Count); ok {
		return x.Count
	}
	return 0
}

func (x *Metric) GetLatencyAvg() int64 {
	if x, ok := x.GetValue().(*Metric_LatencyAvg); ok {
		return x.LatencyAvg
	}
	return 0
}

func (x *Metric) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *Metric) GetValuePerPartition() map[int32]float64 {
	if x != nil {
		return x.ValuePerPartition
	}
	return nil
}

type isMetric_Value interface {
	isMetric_Value()
}

type Metric_Count struct {
	// represents the value for a count-based metric
	Count int64 `protobuf:"varint,2,opt,name=count,proto3,oneof"`
}

type Metric_LatencyAvg struct {
	// represents the average latency for a latency-based metric
	LatencyAvg int64 `protobuf:"varint,3,opt,name=latency_avg,json=latencyAvg,proto3,oneof"`
}

func (*Metric_Count) isMetric_Value() {}

func (*Metric_LatencyAvg) isMetric_Value() {}

var File_metrics_proto protoreflect.FileDescriptor

var file_metrics_proto_rawDesc = []byte{
	0x0a, 0x0d, 0x6d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x73, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12,
	0x0b, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a, 0x1f, 0x67, 0x6f,
	0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74, 0x69,
	0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x1e, 0x67,
	0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x64,
	0x75, 0x72, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0f, 0x6f,
	0x62, 0x6a, 0x65, 0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0xb4,
	0x01, 0x0a, 0x0a, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x53, 0x70, 0x65, 0x63, 0x12, 0x29, 0x0a,
	0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x19, 0x2e, 0x6c, 0x69, 0x74, 0x74,
	0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x53, 0x70,
	0x65, 0x63, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61,
	0x74, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67,
	0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54,
	0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65,
	0x64, 0x41, 0x74, 0x12, 0x40, 0x0a, 0x0e, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f, 0x6c, 0x65,
	0x6e, 0x67, 0x74, 0x68, 0x73, 0x18, 0x03, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x19, 0x2e, 0x67, 0x6f,
	0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x44, 0x75,
	0x72, 0x61, 0x74, 0x69, 0x6f, 0x6e, 0x52, 0x0d, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x4c, 0x65,
	0x6e, 0x67, 0x74, 0x68, 0x73, 0x22, 0x89, 0x02, 0x0a, 0x0f, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74,
	0x69, 0x6f, 0x6e, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x12, 0x2e, 0x0a, 0x02, 0x69, 0x64, 0x18,
	0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1e, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f,
	0x72, 0x73, 0x65, 0x2e, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x4d, 0x65, 0x74,
	0x72, 0x69, 0x63, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65,
	0x61, 0x74, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e,
	0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e,
	0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74,
	0x65, 0x64, 0x41, 0x74, 0x12, 0x4b, 0x0a, 0x0e, 0x61, 0x63, 0x74, 0x69, 0x76, 0x65, 0x5f, 0x77,
	0x69, 0x6e, 0x64, 0x6f, 0x77, 0x73, 0x18, 0x03, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x24, 0x2e, 0x6c,
	0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x50, 0x61, 0x72, 0x74, 0x69,
	0x74, 0x69, 0x6f, 0x6e, 0x57, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x65, 0x64, 0x4d, 0x65, 0x74, 0x72,
	0x69, 0x63, 0x52, 0x0d, 0x61, 0x63, 0x74, 0x69, 0x76, 0x65, 0x57, 0x69, 0x6e, 0x64, 0x6f, 0x77,
	0x73, 0x12, 0x3e, 0x0a, 0x0d, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f, 0x6c, 0x65, 0x6e, 0x67,
	0x74, 0x68, 0x18, 0x04, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x19, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c,
	0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x44, 0x75, 0x72, 0x61, 0x74,
	0x69, 0x6f, 0x6e, 0x52, 0x0c, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x4c, 0x65, 0x6e, 0x67, 0x74,
	0x68, 0x22, 0x9a, 0x01, 0x0a, 0x17, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x57,
	0x69, 0x6e, 0x64, 0x6f, 0x77, 0x65, 0x64, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x12, 0x14, 0x0a,
	0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x01, 0x20, 0x01, 0x28, 0x01, 0x52, 0x05, 0x76, 0x61,
	0x6c, 0x75, 0x65, 0x12, 0x3d, 0x0a, 0x0c, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x5f, 0x73, 0x74,
	0x61, 0x72, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67,
	0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65,
	0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x0b, 0x77, 0x69, 0x6e, 0x64, 0x6f, 0x77, 0x53, 0x74, 0x61,
	0x72, 0x74, 0x12, 0x2a, 0x0a, 0x11, 0x6e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x5f, 0x6f, 0x66, 0x5f,
	0x73, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x73, 0x18, 0x03, 0x20, 0x01, 0x28, 0x03, 0x52, 0x0f, 0x6e,
	0x75, 0x6d, 0x62, 0x65, 0x72, 0x4f, 0x66, 0x53, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x73, 0x22, 0x72,
	0x0a, 0x11, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x4d, 0x65, 0x74, 0x72, 0x69,
	0x63, 0x49, 0x64, 0x12, 0x29, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32,
	0x19, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d, 0x65,
	0x74, 0x72, 0x69, 0x63, 0x53, 0x70, 0x65, 0x63, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x32,
	0x0a, 0x09, 0x74, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x5f, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28,
	0x0b, 0x32, 0x15, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e,
	0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x49, 0x64, 0x52, 0x08, 0x74, 0x65, 0x6e, 0x61, 0x6e, 0x74,
	0x49, 0x64, 0x22, 0xd0, 0x02, 0x0a, 0x06, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x12, 0x25, 0x0a,
	0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x15, 0x2e, 0x6c, 0x69, 0x74, 0x74,
	0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x49, 0x64,
	0x52, 0x02, 0x69, 0x64, 0x12, 0x16, 0x0a, 0x05, 0x63, 0x6f, 0x75, 0x6e, 0x74, 0x18, 0x02, 0x20,
	0x01, 0x28, 0x03, 0x48, 0x00, 0x52, 0x05, 0x63, 0x6f, 0x75, 0x6e, 0x74, 0x12, 0x21, 0x0a, 0x0b,
	0x6c, 0x61, 0x74, 0x65, 0x6e, 0x63, 0x79, 0x5f, 0x61, 0x76, 0x67, 0x18, 0x03, 0x20, 0x01, 0x28,
	0x03, 0x48, 0x00, 0x52, 0x0a, 0x6c, 0x61, 0x74, 0x65, 0x6e, 0x63, 0x79, 0x41, 0x76, 0x67, 0x12,
	0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x04, 0x20,
	0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f,
	0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52,
	0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x41, 0x74, 0x12, 0x5a, 0x0a, 0x13, 0x76, 0x61,
	0x6c, 0x75, 0x65, 0x5f, 0x70, 0x65, 0x72, 0x5f, 0x70, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f,
	0x6e, 0x18, 0x05, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x2a, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65,
	0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x4d, 0x65, 0x74, 0x72, 0x69, 0x63, 0x2e, 0x56, 0x61, 0x6c,
	0x75, 0x65, 0x50, 0x65, 0x72, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x45, 0x6e,
	0x74, 0x72, 0x79, 0x52, 0x11, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x50, 0x65, 0x72, 0x50, 0x61, 0x72,
	0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x1a, 0x44, 0x0a, 0x16, 0x56, 0x61, 0x6c, 0x75, 0x65, 0x50,
	0x65, 0x72, 0x50, 0x61, 0x72, 0x74, 0x69, 0x74, 0x69, 0x6f, 0x6e, 0x45, 0x6e, 0x74, 0x72, 0x79,
	0x12, 0x10, 0x0a, 0x03, 0x6b, 0x65, 0x79, 0x18, 0x01, 0x20, 0x01, 0x28, 0x05, 0x52, 0x03, 0x6b,
	0x65, 0x79, 0x12, 0x14, 0x0a, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28,
	0x01, 0x52, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x3a, 0x02, 0x38, 0x01, 0x42, 0x07, 0x0a, 0x05,
	0x76, 0x61, 0x6c, 0x75, 0x65, 0x42, 0x4d, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74,
	0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d,
	0x6f, 0x6e, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x09, 0x2e, 0x3b, 0x6c, 0x68,
	0x70, 0x72, 0x6f, 0x74, 0x6f, 0xaa, 0x02, 0x1c, 0x4c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x48, 0x6f,
	0x72, 0x73, 0x65, 0x2e, 0x53, 0x64, 0x6b, 0x2e, 0x43, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x50,
	0x72, 0x6f, 0x74, 0x6f, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_metrics_proto_rawDescOnce sync.Once
	file_metrics_proto_rawDescData = file_metrics_proto_rawDesc
)

func file_metrics_proto_rawDescGZIP() []byte {
	file_metrics_proto_rawDescOnce.Do(func() {
		file_metrics_proto_rawDescData = protoimpl.X.CompressGZIP(file_metrics_proto_rawDescData)
	})
	return file_metrics_proto_rawDescData
}

var file_metrics_proto_msgTypes = make([]protoimpl.MessageInfo, 6)
var file_metrics_proto_goTypes = []interface{}{
	(*MetricSpec)(nil),              // 0: littlehorse.MetricSpec
	(*PartitionMetric)(nil),         // 1: littlehorse.PartitionMetric
	(*PartitionWindowedMetric)(nil), // 2: littlehorse.PartitionWindowedMetric
	(*PartitionMetricId)(nil),       // 3: littlehorse.PartitionMetricId
	(*Metric)(nil),                  // 4: littlehorse.Metric
	nil,                             // 5: littlehorse.Metric.ValuePerPartitionEntry
	(*MetricSpecId)(nil),            // 6: littlehorse.MetricSpecId
	(*timestamppb.Timestamp)(nil),   // 7: google.protobuf.Timestamp
	(*durationpb.Duration)(nil),     // 8: google.protobuf.Duration
	(*TenantId)(nil),                // 9: littlehorse.TenantId
	(*MetricId)(nil),                // 10: littlehorse.MetricId
}
var file_metrics_proto_depIdxs = []int32{
	6,  // 0: littlehorse.MetricSpec.id:type_name -> littlehorse.MetricSpecId
	7,  // 1: littlehorse.MetricSpec.created_at:type_name -> google.protobuf.Timestamp
	8,  // 2: littlehorse.MetricSpec.window_lengths:type_name -> google.protobuf.Duration
	3,  // 3: littlehorse.PartitionMetric.id:type_name -> littlehorse.PartitionMetricId
	7,  // 4: littlehorse.PartitionMetric.created_at:type_name -> google.protobuf.Timestamp
	2,  // 5: littlehorse.PartitionMetric.active_windows:type_name -> littlehorse.PartitionWindowedMetric
	8,  // 6: littlehorse.PartitionMetric.window_length:type_name -> google.protobuf.Duration
	7,  // 7: littlehorse.PartitionWindowedMetric.window_start:type_name -> google.protobuf.Timestamp
	6,  // 8: littlehorse.PartitionMetricId.id:type_name -> littlehorse.MetricSpecId
	9,  // 9: littlehorse.PartitionMetricId.tenant_id:type_name -> littlehorse.TenantId
	10, // 10: littlehorse.Metric.id:type_name -> littlehorse.MetricId
	7,  // 11: littlehorse.Metric.created_at:type_name -> google.protobuf.Timestamp
	5,  // 12: littlehorse.Metric.value_per_partition:type_name -> littlehorse.Metric.ValuePerPartitionEntry
	13, // [13:13] is the sub-list for method output_type
	13, // [13:13] is the sub-list for method input_type
	13, // [13:13] is the sub-list for extension type_name
	13, // [13:13] is the sub-list for extension extendee
	0,  // [0:13] is the sub-list for field type_name
}

func init() { file_metrics_proto_init() }
func file_metrics_proto_init() {
	if File_metrics_proto != nil {
		return
	}
	file_object_id_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_metrics_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*MetricSpec); i {
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
		file_metrics_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*PartitionMetric); i {
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
		file_metrics_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*PartitionWindowedMetric); i {
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
		file_metrics_proto_msgTypes[3].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*PartitionMetricId); i {
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
		file_metrics_proto_msgTypes[4].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Metric); i {
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
	file_metrics_proto_msgTypes[4].OneofWrappers = []interface{}{
		(*Metric_Count)(nil),
		(*Metric_LatencyAvg)(nil),
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_metrics_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   6,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_metrics_proto_goTypes,
		DependencyIndexes: file_metrics_proto_depIdxs,
		MessageInfos:      file_metrics_proto_msgTypes,
	}.Build()
	File_metrics_proto = out.File
	file_metrics_proto_rawDesc = nil
	file_metrics_proto_goTypes = nil
	file_metrics_proto_depIdxs = nil
}
