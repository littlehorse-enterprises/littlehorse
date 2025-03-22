// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.31.0
// 	protoc        v6.30.1
// source: acls.proto

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

// Defines a resource type for ACL's.
type ACLResource int32

const (
	// Refers to `WfSpec` and `WfRun`
	ACLResource_ACL_WORKFLOW ACLResource = 0
	// Refers to `TaskDef` and `TaskRun`
	ACLResource_ACL_TASK ACLResource = 1
	// Refers to `ExternalEventDef` and `ExternalEvent`
	ACLResource_ACL_EXTERNAL_EVENT ACLResource = 2
	// Refers to `UserTaskDef` and `UserTaskRun`
	ACLResource_ACL_USER_TASK ACLResource = 3
	// Refers to the `Principal` resource. Currently, the `ACL_PRINCIPAL` permission is only
	// valid in the `global_acls` field of the `Principal`. A `Principal` who only has access
	// to a specific Tenant cannot create othe Principals because a Principal is scoped
	// to the Cluster, and not to a Tenant.
	ACLResource_ACL_PRINCIPAL ACLResource = 4
	// Refers to the `Tenant` resource. The `ACL_TENANT` permission is only valid in the
	// `global_acls` field of the `Principal`. This is because the `Tenant` resource is
	// cluster-scoped.
	ACLResource_ACL_TENANT ACLResource = 5
	// Refers to all resources. In the `global_acls` field, this includes `Principal` and `Tenant`
	// resources. In the `per_tenant_acls` field, this does not include `Principal` and `Tenant` since
	// those are cluster-scoped resources.
	ACLResource_ACL_ALL_RESOURCES ACLResource = 6
	// Refers to the `TaskWorkerGroup` associated with a TaskDef
	ACLResource_ACL_TASK_WORKER_GROUP ACLResource = 7
	// Refers to `WorkflowEventDef` and `WorkflowEvent`
	ACLResource_ACL_WORKFLOW_EVENT ACLResource = 8
)

// Enum value maps for ACLResource.
var (
	ACLResource_name = map[int32]string{
		0: "ACL_WORKFLOW",
		1: "ACL_TASK",
		2: "ACL_EXTERNAL_EVENT",
		3: "ACL_USER_TASK",
		4: "ACL_PRINCIPAL",
		5: "ACL_TENANT",
		6: "ACL_ALL_RESOURCES",
		7: "ACL_TASK_WORKER_GROUP",
		8: "ACL_WORKFLOW_EVENT",
	}
	ACLResource_value = map[string]int32{
		"ACL_WORKFLOW":          0,
		"ACL_TASK":              1,
		"ACL_EXTERNAL_EVENT":    2,
		"ACL_USER_TASK":         3,
		"ACL_PRINCIPAL":         4,
		"ACL_TENANT":            5,
		"ACL_ALL_RESOURCES":     6,
		"ACL_TASK_WORKER_GROUP": 7,
		"ACL_WORKFLOW_EVENT":    8,
	}
)

func (x ACLResource) Enum() *ACLResource {
	p := new(ACLResource)
	*p = x
	return p
}

func (x ACLResource) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (ACLResource) Descriptor() protoreflect.EnumDescriptor {
	return file_acls_proto_enumTypes[0].Descriptor()
}

func (ACLResource) Type() protoreflect.EnumType {
	return &file_acls_proto_enumTypes[0]
}

func (x ACLResource) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use ACLResource.Descriptor instead.
func (ACLResource) EnumDescriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{0}
}

// Describes an Action that can be taken over a specific set of resources.
type ACLAction int32

const (
	// Allows all RPC's that start with `Get`, `List`, and `Search` in relation to the
	// metadata (eg. `TaskDef` for `ACL_TASK`) or run data (eg. `TaskRun` for `ACL_TASK`)
	ACLAction_READ ACLAction = 0
	// Allows RPC's that are needed for mutating the _runs_ of the resource. For
	// example, `RUN` over `ACL_TASK` allows the `ReportTask` and `PollTask` RPC's,
	// and `RUN` over `ACL_WORKFLOW` allows the `RunWf`, `DeleteWfRun`, `StopWfRun`,
	// and `ResumeWfRun` RPC's.
	ACLAction_RUN ACLAction = 1
	// Allows mutating metadata. For example, `WRITE_METADATA` over `ACL_WORKFLOW` allows
	// mutating `WfSpec`s, and `WRITE_METADATA` over `ACL_TASK` allows mutating `TaskDef`s.
	ACLAction_WRITE_METADATA ACLAction = 2
	// Allows all actions related to a resource.
	ACLAction_ALL_ACTIONS ACLAction = 3
)

// Enum value maps for ACLAction.
var (
	ACLAction_name = map[int32]string{
		0: "READ",
		1: "RUN",
		2: "WRITE_METADATA",
		3: "ALL_ACTIONS",
	}
	ACLAction_value = map[string]int32{
		"READ":           0,
		"RUN":            1,
		"WRITE_METADATA": 2,
		"ALL_ACTIONS":    3,
	}
)

func (x ACLAction) Enum() *ACLAction {
	p := new(ACLAction)
	*p = x
	return p
}

func (x ACLAction) String() string {
	return protoimpl.X.EnumStringOf(x.Descriptor(), protoreflect.EnumNumber(x))
}

func (ACLAction) Descriptor() protoreflect.EnumDescriptor {
	return file_acls_proto_enumTypes[1].Descriptor()
}

func (ACLAction) Type() protoreflect.EnumType {
	return &file_acls_proto_enumTypes[1]
}

func (x ACLAction) Number() protoreflect.EnumNumber {
	return protoreflect.EnumNumber(x)
}

// Deprecated: Use ACLAction.Descriptor instead.
func (ACLAction) EnumDescriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{1}
}

// A Principal represents the identity of a client of LittleHorse, whether human or
// machine. The ACL's on the Principal control what actions the client is allowed
// to take.
//
// A Principal is not scoped to a Tenant; rather, a Principal is scoped to the Cluster
// and may have access to one or more Tenants.
type Principal struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the Principal. In OAuth for human users, this is the user_id. In
	// OAuth for machine clients, this is the Client ID.
	//
	// mTLS for Principal identification is not yet implemented.
	Id *PrincipalId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The time at which the Principal was created.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,2,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
	// Maps a Tenant ID to a list of ACL's that the Principal has permission to
	// execute *within that Tenant*.
	PerTenantAcls map[string]*ServerACLs `protobuf:"bytes,3,rep,name=per_tenant_acls,json=perTenantAcls,proto3" json:"per_tenant_acls,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
	// Sets permissions that this Principal has *for any Tenant* in the LH Cluster.
	GlobalAcls *ServerACLs `protobuf:"bytes,4,opt,name=global_acls,json=globalAcls,proto3" json:"global_acls,omitempty"`
}

func (x *Principal) Reset() {
	*x = Principal{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Principal) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Principal) ProtoMessage() {}

func (x *Principal) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Principal.ProtoReflect.Descriptor instead.
func (*Principal) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{0}
}

func (x *Principal) GetId() *PrincipalId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *Principal) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

func (x *Principal) GetPerTenantAcls() map[string]*ServerACLs {
	if x != nil {
		return x.PerTenantAcls
	}
	return nil
}

func (x *Principal) GetGlobalAcls() *ServerACLs {
	if x != nil {
		return x.GlobalAcls
	}
	return nil
}

// A Tenant is a logically isolated environment within LittleHorse. All workflows and
// associated data (WfSpec, WfRun, TaskDef, TaskRun, NodeRun, etc) are scoped to within
// a Tenant.
//
// Future versions will include quotas on a per-Tenant basis.
type Tenant struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the Tenant.
	Id *TenantId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The time at which the Tenant was created.
	CreatedAt *timestamppb.Timestamp `protobuf:"bytes,2,opt,name=created_at,json=createdAt,proto3" json:"created_at,omitempty"`
}

func (x *Tenant) Reset() {
	*x = Tenant{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *Tenant) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*Tenant) ProtoMessage() {}

func (x *Tenant) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use Tenant.ProtoReflect.Descriptor instead.
func (*Tenant) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{1}
}

func (x *Tenant) GetId() *TenantId {
	if x != nil {
		return x.Id
	}
	return nil
}

func (x *Tenant) GetCreatedAt() *timestamppb.Timestamp {
	if x != nil {
		return x.CreatedAt
	}
	return nil
}

// List of ACL's for LittleHorse
type ServerACLs struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The associated ACL's
	Acls []*ServerACL `protobuf:"bytes,1,rep,name=acls,proto3" json:"acls,omitempty"`
}

func (x *ServerACLs) Reset() {
	*x = ServerACLs{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *ServerACLs) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*ServerACLs) ProtoMessage() {}

func (x *ServerACLs) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use ServerACLs.ProtoReflect.Descriptor instead.
func (*ServerACLs) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{2}
}

func (x *ServerACLs) GetAcls() []*ServerACL {
	if x != nil {
		return x.Acls
	}
	return nil
}

// Represents a specific set of permissions over a specific set of objects
// in a Tenant. This is a *positive* permission.
type ServerACL struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The resource types over which permission is granted.
	Resources []ACLResource `protobuf:"varint,1,rep,packed,name=resources,proto3,enum=littlehorse.ACLResource" json:"resources,omitempty"`
	// The actions that are permitted.
	AllowedActions []ACLAction `protobuf:"varint,2,rep,packed,name=allowed_actions,json=allowedActions,proto3,enum=littlehorse.ACLAction" json:"allowed_actions,omitempty"`
	// Types that are assignable to ResourceFilter:
	//	*ServerACL_Name
	//	*ServerACL_Prefix
	ResourceFilter isServerACL_ResourceFilter `protobuf_oneof:"resource_filter"`
}

func (x *ServerACL) Reset() {
	*x = ServerACL{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[3]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *ServerACL) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*ServerACL) ProtoMessage() {}

func (x *ServerACL) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[3]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use ServerACL.ProtoReflect.Descriptor instead.
func (*ServerACL) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{3}
}

func (x *ServerACL) GetResources() []ACLResource {
	if x != nil {
		return x.Resources
	}
	return nil
}

func (x *ServerACL) GetAllowedActions() []ACLAction {
	if x != nil {
		return x.AllowedActions
	}
	return nil
}

func (m *ServerACL) GetResourceFilter() isServerACL_ResourceFilter {
	if m != nil {
		return m.ResourceFilter
	}
	return nil
}

func (x *ServerACL) GetName() string {
	if x, ok := x.GetResourceFilter().(*ServerACL_Name); ok {
		return x.Name
	}
	return ""
}

func (x *ServerACL) GetPrefix() string {
	if x, ok := x.GetResourceFilter().(*ServerACL_Prefix); ok {
		return x.Prefix
	}
	return ""
}

type isServerACL_ResourceFilter interface {
	isServerACL_ResourceFilter()
}

type ServerACL_Name struct {
	// If set, then only the resources with this exact name are allowed. For example,
	// the `READ` and `RUN` `allowed_actions` over `ACL_TASK` with `name` == `my-task`
	// allows a Task Worker to only execute the `my-task` TaskDef.
	//
	// If `name` and `prefix` are unset, then the ACL applies to all resources of the
	// specified types.
	Name string `protobuf:"bytes,3,opt,name=name,proto3,oneof"`
}

type ServerACL_Prefix struct {
	// If set, then only the resources whose names match this prefix are allowed.
	//
	// If `name` and `prefix` are unset, then the ACL applies to all resources of the
	// specified types.
	Prefix string `protobuf:"bytes,4,opt,name=prefix,proto3,oneof"`
}

func (*ServerACL_Name) isServerACL_ResourceFilter() {}

func (*ServerACL_Prefix) isServerACL_ResourceFilter() {}

// Creates or updates a Principal. If this request would remove admin privileges from the
// last admin principal (i.e. `ALL_ACTIONS` over `ACL_ALL_RESOURCES` in the `global_acls`),
// then the RPC throws `FAILED_PRECONDITION`.
type PutPrincipalRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the Principal that we are creating.
	Id string `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
	// The per-tenant ACL's for the Principal
	PerTenantAcls map[string]*ServerACLs `protobuf:"bytes,2,rep,name=per_tenant_acls,json=perTenantAcls,proto3" json:"per_tenant_acls,omitempty" protobuf_key:"bytes,1,opt,name=key,proto3" protobuf_val:"bytes,2,opt,name=value,proto3"`
	// The ACL's for the principal in all tenants
	GlobalAcls *ServerACLs `protobuf:"bytes,3,opt,name=global_acls,json=globalAcls,proto3" json:"global_acls,omitempty"`
	// If this is set to false and a `Principal` with the same `id` already exists *and*
	// has different ACL's configured, then the RPC throws `ALREADY_EXISTS`.
	//
	// If this is set to `true`, then the RPC will override hte
	Overwrite bool `protobuf:"varint,5,opt,name=overwrite,proto3" json:"overwrite,omitempty"`
}

func (x *PutPrincipalRequest) Reset() {
	*x = PutPrincipalRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[4]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *PutPrincipalRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*PutPrincipalRequest) ProtoMessage() {}

func (x *PutPrincipalRequest) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[4]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use PutPrincipalRequest.ProtoReflect.Descriptor instead.
func (*PutPrincipalRequest) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{4}
}

func (x *PutPrincipalRequest) GetId() string {
	if x != nil {
		return x.Id
	}
	return ""
}

func (x *PutPrincipalRequest) GetPerTenantAcls() map[string]*ServerACLs {
	if x != nil {
		return x.PerTenantAcls
	}
	return nil
}

func (x *PutPrincipalRequest) GetGlobalAcls() *ServerACLs {
	if x != nil {
		return x.GlobalAcls
	}
	return nil
}

func (x *PutPrincipalRequest) GetOverwrite() bool {
	if x != nil {
		return x.Overwrite
	}
	return false
}

// Deletes a `Principal`. Fails with `FAILED_PRECONDITION` if the specified `Principal` is the last
// admin `Principal`.
type DeletePrincipalRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	// The ID of the `Principal` to delete.
	Id *PrincipalId `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
}

func (x *DeletePrincipalRequest) Reset() {
	*x = DeletePrincipalRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[5]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *DeletePrincipalRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*DeletePrincipalRequest) ProtoMessage() {}

func (x *DeletePrincipalRequest) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[5]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use DeletePrincipalRequest.ProtoReflect.Descriptor instead.
func (*DeletePrincipalRequest) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{5}
}

func (x *DeletePrincipalRequest) GetId() *PrincipalId {
	if x != nil {
		return x.Id
	}
	return nil
}

type PutTenantRequest struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	Id string `protobuf:"bytes,1,opt,name=id,proto3" json:"id,omitempty"`
}

func (x *PutTenantRequest) Reset() {
	*x = PutTenantRequest{}
	if protoimpl.UnsafeEnabled {
		mi := &file_acls_proto_msgTypes[6]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *PutTenantRequest) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*PutTenantRequest) ProtoMessage() {}

func (x *PutTenantRequest) ProtoReflect() protoreflect.Message {
	mi := &file_acls_proto_msgTypes[6]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use PutTenantRequest.ProtoReflect.Descriptor instead.
func (*PutTenantRequest) Descriptor() ([]byte, []int) {
	return file_acls_proto_rawDescGZIP(), []int{6}
}

func (x *PutTenantRequest) GetId() string {
	if x != nil {
		return x.Id
	}
	return ""
}

var File_acls_proto protoreflect.FileDescriptor

var file_acls_proto_rawDesc = []byte{
	0x0a, 0x0a, 0x61, 0x63, 0x6c, 0x73, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x12, 0x0b, 0x6c, 0x69,
	0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x1a, 0x1f, 0x67, 0x6f, 0x6f, 0x67, 0x6c,
	0x65, 0x2f, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2f, 0x74, 0x69, 0x6d, 0x65, 0x73,
	0x74, 0x61, 0x6d, 0x70, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x1a, 0x0f, 0x6f, 0x62, 0x6a, 0x65,
	0x63, 0x74, 0x5f, 0x69, 0x64, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x22, 0xd8, 0x02, 0x0a, 0x09,
	0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70, 0x61, 0x6c, 0x12, 0x28, 0x0a, 0x02, 0x69, 0x64, 0x18,
	0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x18, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f,
	0x72, 0x73, 0x65, 0x2e, 0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70, 0x61, 0x6c, 0x49, 0x64, 0x52,
	0x02, 0x69, 0x64, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x5f, 0x61,
	0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f, 0x6f, 0x67, 0x6c, 0x65,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74,
	0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64, 0x41, 0x74, 0x12, 0x51,
	0x0a, 0x0f, 0x70, 0x65, 0x72, 0x5f, 0x74, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x5f, 0x61, 0x63, 0x6c,
	0x73, 0x18, 0x03, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x29, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65,
	0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70, 0x61, 0x6c, 0x2e,
	0x50, 0x65, 0x72, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x41, 0x63, 0x6c, 0x73, 0x45, 0x6e, 0x74,
	0x72, 0x79, 0x52, 0x0d, 0x70, 0x65, 0x72, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x41, 0x63, 0x6c,
	0x73, 0x12, 0x38, 0x0a, 0x0b, 0x67, 0x6c, 0x6f, 0x62, 0x61, 0x6c, 0x5f, 0x61, 0x63, 0x6c, 0x73,
	0x18, 0x04, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68,
	0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x73, 0x52,
	0x0a, 0x67, 0x6c, 0x6f, 0x62, 0x61, 0x6c, 0x41, 0x63, 0x6c, 0x73, 0x1a, 0x59, 0x0a, 0x12, 0x50,
	0x65, 0x72, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x41, 0x63, 0x6c, 0x73, 0x45, 0x6e, 0x74, 0x72,
	0x79, 0x12, 0x10, 0x0a, 0x03, 0x6b, 0x65, 0x79, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x03,
	0x6b, 0x65, 0x79, 0x12, 0x2d, 0x0a, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20, 0x01,
	0x28, 0x0b, 0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65,
	0x2e, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x73, 0x52, 0x05, 0x76, 0x61, 0x6c,
	0x75, 0x65, 0x3a, 0x02, 0x38, 0x01, 0x22, 0x6a, 0x0a, 0x06, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74,
	0x12, 0x25, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x15, 0x2e, 0x6c,
	0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x54, 0x65, 0x6e, 0x61, 0x6e,
	0x74, 0x49, 0x64, 0x52, 0x02, 0x69, 0x64, 0x12, 0x39, 0x0a, 0x0a, 0x63, 0x72, 0x65, 0x61, 0x74,
	0x65, 0x64, 0x5f, 0x61, 0x74, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x1a, 0x2e, 0x67, 0x6f,
	0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x62, 0x75, 0x66, 0x2e, 0x54, 0x69,
	0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70, 0x52, 0x09, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x64,
	0x41, 0x74, 0x22, 0x38, 0x0a, 0x0a, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x73,
	0x12, 0x2a, 0x0a, 0x04, 0x61, 0x63, 0x6c, 0x73, 0x18, 0x01, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x16,
	0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53, 0x65, 0x72,
	0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x52, 0x04, 0x61, 0x63, 0x6c, 0x73, 0x22, 0xc7, 0x01, 0x0a,
	0x09, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x12, 0x36, 0x0a, 0x09, 0x72, 0x65,
	0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x73, 0x18, 0x01, 0x20, 0x03, 0x28, 0x0e, 0x32, 0x18, 0x2e,
	0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x41, 0x43, 0x4c, 0x52,
	0x65, 0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x52, 0x09, 0x72, 0x65, 0x73, 0x6f, 0x75, 0x72, 0x63,
	0x65, 0x73, 0x12, 0x3f, 0x0a, 0x0f, 0x61, 0x6c, 0x6c, 0x6f, 0x77, 0x65, 0x64, 0x5f, 0x61, 0x63,
	0x74, 0x69, 0x6f, 0x6e, 0x73, 0x18, 0x02, 0x20, 0x03, 0x28, 0x0e, 0x32, 0x16, 0x2e, 0x6c, 0x69,
	0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x41, 0x43, 0x4c, 0x41, 0x63, 0x74,
	0x69, 0x6f, 0x6e, 0x52, 0x0e, 0x61, 0x6c, 0x6c, 0x6f, 0x77, 0x65, 0x64, 0x41, 0x63, 0x74, 0x69,
	0x6f, 0x6e, 0x73, 0x12, 0x14, 0x0a, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x18, 0x03, 0x20, 0x01, 0x28,
	0x09, 0x48, 0x00, 0x52, 0x04, 0x6e, 0x61, 0x6d, 0x65, 0x12, 0x18, 0x0a, 0x06, 0x70, 0x72, 0x65,
	0x66, 0x69, 0x78, 0x18, 0x04, 0x20, 0x01, 0x28, 0x09, 0x48, 0x00, 0x52, 0x06, 0x70, 0x72, 0x65,
	0x66, 0x69, 0x78, 0x42, 0x11, 0x0a, 0x0f, 0x72, 0x65, 0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x5f,
	0x66, 0x69, 0x6c, 0x74, 0x65, 0x72, 0x22, 0xb5, 0x02, 0x0a, 0x13, 0x50, 0x75, 0x74, 0x50, 0x72,
	0x69, 0x6e, 0x63, 0x69, 0x70, 0x61, 0x6c, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x12, 0x0e,
	0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x02, 0x69, 0x64, 0x12, 0x5b,
	0x0a, 0x0f, 0x70, 0x65, 0x72, 0x5f, 0x74, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x5f, 0x61, 0x63, 0x6c,
	0x73, 0x18, 0x02, 0x20, 0x03, 0x28, 0x0b, 0x32, 0x33, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65,
	0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x50, 0x75, 0x74, 0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70,
	0x61, 0x6c, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x2e, 0x50, 0x65, 0x72, 0x54, 0x65, 0x6e,
	0x61, 0x6e, 0x74, 0x41, 0x63, 0x6c, 0x73, 0x45, 0x6e, 0x74, 0x72, 0x79, 0x52, 0x0d, 0x70, 0x65,
	0x72, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x41, 0x63, 0x6c, 0x73, 0x12, 0x38, 0x0a, 0x0b, 0x67,
	0x6c, 0x6f, 0x62, 0x61, 0x6c, 0x5f, 0x61, 0x63, 0x6c, 0x73, 0x18, 0x03, 0x20, 0x01, 0x28, 0x0b,
	0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53,
	0x65, 0x72, 0x76, 0x65, 0x72, 0x41, 0x43, 0x4c, 0x73, 0x52, 0x0a, 0x67, 0x6c, 0x6f, 0x62, 0x61,
	0x6c, 0x41, 0x63, 0x6c, 0x73, 0x12, 0x1c, 0x0a, 0x09, 0x6f, 0x76, 0x65, 0x72, 0x77, 0x72, 0x69,
	0x74, 0x65, 0x18, 0x05, 0x20, 0x01, 0x28, 0x08, 0x52, 0x09, 0x6f, 0x76, 0x65, 0x72, 0x77, 0x72,
	0x69, 0x74, 0x65, 0x1a, 0x59, 0x0a, 0x12, 0x50, 0x65, 0x72, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74,
	0x41, 0x63, 0x6c, 0x73, 0x45, 0x6e, 0x74, 0x72, 0x79, 0x12, 0x10, 0x0a, 0x03, 0x6b, 0x65, 0x79,
	0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x03, 0x6b, 0x65, 0x79, 0x12, 0x2d, 0x0a, 0x05, 0x76,
	0x61, 0x6c, 0x75, 0x65, 0x18, 0x02, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x17, 0x2e, 0x6c, 0x69, 0x74,
	0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x53, 0x65, 0x72, 0x76, 0x65, 0x72, 0x41,
	0x43, 0x4c, 0x73, 0x52, 0x05, 0x76, 0x61, 0x6c, 0x75, 0x65, 0x3a, 0x02, 0x38, 0x01, 0x22, 0x42,
	0x0a, 0x16, 0x44, 0x65, 0x6c, 0x65, 0x74, 0x65, 0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70, 0x61,
	0x6c, 0x52, 0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x12, 0x28, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01,
	0x20, 0x01, 0x28, 0x0b, 0x32, 0x18, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x68, 0x6f, 0x72,
	0x73, 0x65, 0x2e, 0x50, 0x72, 0x69, 0x6e, 0x63, 0x69, 0x70, 0x61, 0x6c, 0x49, 0x64, 0x52, 0x02,
	0x69, 0x64, 0x22, 0x22, 0x0a, 0x10, 0x50, 0x75, 0x74, 0x54, 0x65, 0x6e, 0x61, 0x6e, 0x74, 0x52,
	0x65, 0x71, 0x75, 0x65, 0x73, 0x74, 0x12, 0x0e, 0x0a, 0x02, 0x69, 0x64, 0x18, 0x01, 0x20, 0x01,
	0x28, 0x09, 0x52, 0x02, 0x69, 0x64, 0x2a, 0xc5, 0x01, 0x0a, 0x0b, 0x41, 0x43, 0x4c, 0x52, 0x65,
	0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x12, 0x10, 0x0a, 0x0c, 0x41, 0x43, 0x4c, 0x5f, 0x57, 0x4f,
	0x52, 0x4b, 0x46, 0x4c, 0x4f, 0x57, 0x10, 0x00, 0x12, 0x0c, 0x0a, 0x08, 0x41, 0x43, 0x4c, 0x5f,
	0x54, 0x41, 0x53, 0x4b, 0x10, 0x01, 0x12, 0x16, 0x0a, 0x12, 0x41, 0x43, 0x4c, 0x5f, 0x45, 0x58,
	0x54, 0x45, 0x52, 0x4e, 0x41, 0x4c, 0x5f, 0x45, 0x56, 0x45, 0x4e, 0x54, 0x10, 0x02, 0x12, 0x11,
	0x0a, 0x0d, 0x41, 0x43, 0x4c, 0x5f, 0x55, 0x53, 0x45, 0x52, 0x5f, 0x54, 0x41, 0x53, 0x4b, 0x10,
	0x03, 0x12, 0x11, 0x0a, 0x0d, 0x41, 0x43, 0x4c, 0x5f, 0x50, 0x52, 0x49, 0x4e, 0x43, 0x49, 0x50,
	0x41, 0x4c, 0x10, 0x04, 0x12, 0x0e, 0x0a, 0x0a, 0x41, 0x43, 0x4c, 0x5f, 0x54, 0x45, 0x4e, 0x41,
	0x4e, 0x54, 0x10, 0x05, 0x12, 0x15, 0x0a, 0x11, 0x41, 0x43, 0x4c, 0x5f, 0x41, 0x4c, 0x4c, 0x5f,
	0x52, 0x45, 0x53, 0x4f, 0x55, 0x52, 0x43, 0x45, 0x53, 0x10, 0x06, 0x12, 0x19, 0x0a, 0x15, 0x41,
	0x43, 0x4c, 0x5f, 0x54, 0x41, 0x53, 0x4b, 0x5f, 0x57, 0x4f, 0x52, 0x4b, 0x45, 0x52, 0x5f, 0x47,
	0x52, 0x4f, 0x55, 0x50, 0x10, 0x07, 0x12, 0x16, 0x0a, 0x12, 0x41, 0x43, 0x4c, 0x5f, 0x57, 0x4f,
	0x52, 0x4b, 0x46, 0x4c, 0x4f, 0x57, 0x5f, 0x45, 0x56, 0x45, 0x4e, 0x54, 0x10, 0x08, 0x2a, 0x43,
	0x0a, 0x09, 0x41, 0x43, 0x4c, 0x41, 0x63, 0x74, 0x69, 0x6f, 0x6e, 0x12, 0x08, 0x0a, 0x04, 0x52,
	0x45, 0x41, 0x44, 0x10, 0x00, 0x12, 0x07, 0x0a, 0x03, 0x52, 0x55, 0x4e, 0x10, 0x01, 0x12, 0x12,
	0x0a, 0x0e, 0x57, 0x52, 0x49, 0x54, 0x45, 0x5f, 0x4d, 0x45, 0x54, 0x41, 0x44, 0x41, 0x54, 0x41,
	0x10, 0x02, 0x12, 0x0f, 0x0a, 0x0b, 0x41, 0x4c, 0x4c, 0x5f, 0x41, 0x43, 0x54, 0x49, 0x4f, 0x4e,
	0x53, 0x10, 0x03, 0x42, 0x4d, 0x0a, 0x1f, 0x69, 0x6f, 0x2e, 0x6c, 0x69, 0x74, 0x74, 0x6c, 0x65,
	0x68, 0x6f, 0x72, 0x73, 0x65, 0x2e, 0x73, 0x64, 0x6b, 0x2e, 0x63, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e,
	0x2e, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x50, 0x01, 0x5a, 0x09, 0x2e, 0x3b, 0x6c, 0x68, 0x70, 0x72,
	0x6f, 0x74, 0x6f, 0xaa, 0x02, 0x1c, 0x4c, 0x69, 0x74, 0x74, 0x6c, 0x65, 0x48, 0x6f, 0x72, 0x73,
	0x65, 0x2e, 0x53, 0x64, 0x6b, 0x2e, 0x43, 0x6f, 0x6d, 0x6d, 0x6f, 0x6e, 0x2e, 0x50, 0x72, 0x6f,
	0x74, 0x6f, 0x62, 0x06, 0x70, 0x72, 0x6f, 0x74, 0x6f, 0x33,
}

var (
	file_acls_proto_rawDescOnce sync.Once
	file_acls_proto_rawDescData = file_acls_proto_rawDesc
)

func file_acls_proto_rawDescGZIP() []byte {
	file_acls_proto_rawDescOnce.Do(func() {
		file_acls_proto_rawDescData = protoimpl.X.CompressGZIP(file_acls_proto_rawDescData)
	})
	return file_acls_proto_rawDescData
}

var file_acls_proto_enumTypes = make([]protoimpl.EnumInfo, 2)
var file_acls_proto_msgTypes = make([]protoimpl.MessageInfo, 9)
var file_acls_proto_goTypes = []interface{}{
	(ACLResource)(0),               // 0: littlehorse.ACLResource
	(ACLAction)(0),                 // 1: littlehorse.ACLAction
	(*Principal)(nil),              // 2: littlehorse.Principal
	(*Tenant)(nil),                 // 3: littlehorse.Tenant
	(*ServerACLs)(nil),             // 4: littlehorse.ServerACLs
	(*ServerACL)(nil),              // 5: littlehorse.ServerACL
	(*PutPrincipalRequest)(nil),    // 6: littlehorse.PutPrincipalRequest
	(*DeletePrincipalRequest)(nil), // 7: littlehorse.DeletePrincipalRequest
	(*PutTenantRequest)(nil),       // 8: littlehorse.PutTenantRequest
	nil,                            // 9: littlehorse.Principal.PerTenantAclsEntry
	nil,                            // 10: littlehorse.PutPrincipalRequest.PerTenantAclsEntry
	(*PrincipalId)(nil),            // 11: littlehorse.PrincipalId
	(*timestamppb.Timestamp)(nil),  // 12: google.protobuf.Timestamp
	(*TenantId)(nil),               // 13: littlehorse.TenantId
}
var file_acls_proto_depIdxs = []int32{
	11, // 0: littlehorse.Principal.id:type_name -> littlehorse.PrincipalId
	12, // 1: littlehorse.Principal.created_at:type_name -> google.protobuf.Timestamp
	9,  // 2: littlehorse.Principal.per_tenant_acls:type_name -> littlehorse.Principal.PerTenantAclsEntry
	4,  // 3: littlehorse.Principal.global_acls:type_name -> littlehorse.ServerACLs
	13, // 4: littlehorse.Tenant.id:type_name -> littlehorse.TenantId
	12, // 5: littlehorse.Tenant.created_at:type_name -> google.protobuf.Timestamp
	5,  // 6: littlehorse.ServerACLs.acls:type_name -> littlehorse.ServerACL
	0,  // 7: littlehorse.ServerACL.resources:type_name -> littlehorse.ACLResource
	1,  // 8: littlehorse.ServerACL.allowed_actions:type_name -> littlehorse.ACLAction
	10, // 9: littlehorse.PutPrincipalRequest.per_tenant_acls:type_name -> littlehorse.PutPrincipalRequest.PerTenantAclsEntry
	4,  // 10: littlehorse.PutPrincipalRequest.global_acls:type_name -> littlehorse.ServerACLs
	11, // 11: littlehorse.DeletePrincipalRequest.id:type_name -> littlehorse.PrincipalId
	4,  // 12: littlehorse.Principal.PerTenantAclsEntry.value:type_name -> littlehorse.ServerACLs
	4,  // 13: littlehorse.PutPrincipalRequest.PerTenantAclsEntry.value:type_name -> littlehorse.ServerACLs
	14, // [14:14] is the sub-list for method output_type
	14, // [14:14] is the sub-list for method input_type
	14, // [14:14] is the sub-list for extension type_name
	14, // [14:14] is the sub-list for extension extendee
	0,  // [0:14] is the sub-list for field type_name
}

func init() { file_acls_proto_init() }
func file_acls_proto_init() {
	if File_acls_proto != nil {
		return
	}
	file_object_id_proto_init()
	if !protoimpl.UnsafeEnabled {
		file_acls_proto_msgTypes[0].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Principal); i {
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
		file_acls_proto_msgTypes[1].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*Tenant); i {
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
		file_acls_proto_msgTypes[2].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*ServerACLs); i {
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
		file_acls_proto_msgTypes[3].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*ServerACL); i {
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
		file_acls_proto_msgTypes[4].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*PutPrincipalRequest); i {
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
		file_acls_proto_msgTypes[5].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*DeletePrincipalRequest); i {
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
		file_acls_proto_msgTypes[6].Exporter = func(v interface{}, i int) interface{} {
			switch v := v.(*PutTenantRequest); i {
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
	file_acls_proto_msgTypes[3].OneofWrappers = []interface{}{
		(*ServerACL_Name)(nil),
		(*ServerACL_Prefix)(nil),
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_acls_proto_rawDesc,
			NumEnums:      2,
			NumMessages:   9,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_acls_proto_goTypes,
		DependencyIndexes: file_acls_proto_depIdxs,
		EnumInfos:         file_acls_proto_enumTypes,
		MessageInfos:      file_acls_proto_msgTypes,
	}.Build()
	File_acls_proto = out.File
	file_acls_proto_rawDesc = nil
	file_acls_proto_goTypes = nil
	file_acls_proto_depIdxs = nil
}
