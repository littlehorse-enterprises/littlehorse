// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: common_wfspec.proto

package io.littlehorse.sdk.common.proto;

public final class CommonWfspec {
  private CommonWfspec() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableAssignment_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableAssignment_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableAssignment_FormatString_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableAssignment_FormatString_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableMutation_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableMutation_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableMutation_NodeOutputSource_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableMutation_NodeOutputSource_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_VariableDef_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_VariableDef_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UTActionTrigger_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UTActionTrigger_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UTActionTrigger_UTACancel_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UTActionTrigger_UTACancel_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UTActionTrigger_UTATask_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UTActionTrigger_UTATask_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_UTActionTrigger_UTAReassign_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_UTActionTrigger_UTAReassign_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_ExponentialBackoffRetryPolicy_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_littlehorse_TaskNode_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_TaskNode_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023common_wfspec.proto\022\013littlehorse\032\022comm" +
      "on_enums.proto\032\016variable.proto\032\017object_i" +
      "d.proto\"\311\002\n\022VariableAssignment\022\026\n\tjson_p" +
      "ath\030\001 \001(\tH\001\210\001\001\022\027\n\rvariable_name\030\002 \001(\tH\000\022" +
      "3\n\rliteral_value\030\003 \001(\0132\032.littlehorse.Var" +
      "iableValueH\000\022E\n\rformat_string\030\004 \001(\0132,.li" +
      "ttlehorse.VariableAssignment.FormatStrin" +
      "gH\000\032n\n\014FormatString\022/\n\006format\030\001 \001(\0132\037.li" +
      "ttlehorse.VariableAssignment\022-\n\004args\030\002 \003" +
      "(\0132\037.littlehorse.VariableAssignmentB\010\n\006s" +
      "ourceB\014\n\n_json_path\"\205\003\n\020VariableMutation" +
      "\022\020\n\010lhs_name\030\001 \001(\t\022\032\n\rlhs_json_path\030\002 \001(" +
      "\tH\001\210\001\001\0224\n\toperation\030\003 \001(\0162!.littlehorse." +
      "VariableMutationType\022:\n\017source_variable\030" +
      "\004 \001(\0132\037.littlehorse.VariableAssignmentH\000" +
      "\0223\n\rliteral_value\030\005 \001(\0132\032.littlehorse.Va" +
      "riableValueH\000\022E\n\013node_output\030\006 \001(\0132..lit" +
      "tlehorse.VariableMutation.NodeOutputSour" +
      "ceH\000\0326\n\020NodeOutputSource\022\025\n\010jsonpath\030\n \001" +
      "(\tH\000\210\001\001B\013\n\t_jsonpathB\013\n\trhs_valueB\020\n\016_lh" +
      "s_json_path\"\216\001\n\013VariableDef\022\'\n\004type\030\001 \001(" +
      "\0162\031.littlehorse.VariableType\022\014\n\004name\030\002 \001" +
      "(\t\0226\n\rdefault_value\030\003 \001(\0132\032.littlehorse." +
      "VariableValueH\000\210\001\001B\020\n\016_default_value\"\357\004\n" +
      "\017UTActionTrigger\0224\n\004task\030\001 \001(\0132$.littleh" +
      "orse.UTActionTrigger.UTATaskH\000\0228\n\006cancel" +
      "\030\002 \001(\0132&.littlehorse.UTActionTrigger.UTA" +
      "CancelH\000\022<\n\010reassign\030\003 \001(\0132(.littlehorse" +
      ".UTActionTrigger.UTAReassignH\000\0226\n\rdelay_" +
      "seconds\030\005 \001(\0132\037.littlehorse.VariableAssi" +
      "gnment\0221\n\004hook\030\006 \001(\0162#.littlehorse.UTAct" +
      "ionTrigger.UTHook\032\013\n\tUTACancel\032`\n\007UTATas" +
      "k\022#\n\004task\030\001 \001(\0132\025.littlehorse.TaskNode\0220" +
      "\n\tmutations\030\002 \003(\0132\035.littlehorse.Variable" +
      "Mutation\032\231\001\n\013UTAReassign\0225\n\007user_id\030\001 \001(" +
      "\0132\037.littlehorse.VariableAssignmentH\000\210\001\001\022" +
      "8\n\nuser_group\030\002 \001(\0132\037.littlehorse.Variab" +
      "leAssignmentH\001\210\001\001B\n\n\010_user_idB\r\n\013_user_g" +
      "roup\".\n\006UTHook\022\016\n\nON_ARRIVAL\020\000\022\024\n\020ON_TAS" +
      "K_ASSIGNED\020\001B\010\n\006action\"c\n\035ExponentialBac" +
      "koffRetryPolicy\022\030\n\020base_interval_ms\030\001 \001(" +
      "\005\022\024\n\014max_delay_ms\030\002 \001(\003\022\022\n\nmultiplier\030\003 " +
      "\001(\002\"\311\002\n\010TaskNode\022-\n\013task_def_id\030\001 \001(\0132\026." +
      "littlehorse.TaskDefIdH\000\0227\n\014dynamic_task\030" +
      "\006 \001(\0132\037.littlehorse.VariableAssignmentH\000" +
      "\022\027\n\017timeout_seconds\030\002 \001(\005\022\017\n\007retries\030\003 \001" +
      "(\005\022L\n\023exponential_backoff\030\005 \001(\0132*.little" +
      "horse.ExponentialBackoffRetryPolicyH\001\210\001\001" +
      "\0222\n\tvariables\030\004 \003(\0132\037.littlehorse.Variab" +
      "leAssignmentB\021\n\017task_to_executeB\026\n\024_expo" +
      "nential_backoff*\230\001\n\024VariableMutationType" +
      "\022\n\n\006ASSIGN\020\000\022\007\n\003ADD\020\001\022\n\n\006EXTEND\020\002\022\014\n\010SUB" +
      "TRACT\020\003\022\014\n\010MULTIPLY\020\004\022\n\n\006DIVIDE\020\005\022\025\n\021REM" +
      "OVE_IF_PRESENT\020\006\022\020\n\014REMOVE_INDEX\020\007\022\016\n\nRE" +
      "MOVE_KEY\020\010*\204\001\n\nComparator\022\r\n\tLESS_THAN\020\000" +
      "\022\020\n\014GREATER_THAN\020\001\022\020\n\014LESS_THAN_EQ\020\002\022\023\n\017" +
      "GREATER_THAN_EQ\020\003\022\n\n\006EQUALS\020\004\022\016\n\nNOT_EQU" +
      "ALS\020\005\022\006\n\002IN\020\006\022\n\n\006NOT_IN\020\007BG\n\037io.littleho" +
      "rse.sdk.common.protoP\001Z\007.;model\252\002\030Little" +
      "Horse.Common.Protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
          io.littlehorse.sdk.common.proto.ObjectId.getDescriptor(),
        });
    internal_static_littlehorse_VariableAssignment_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_littlehorse_VariableAssignment_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableAssignment_descriptor,
        new java.lang.String[] { "JsonPath", "VariableName", "LiteralValue", "FormatString", "Source", "JsonPath", });
    internal_static_littlehorse_VariableAssignment_FormatString_descriptor =
      internal_static_littlehorse_VariableAssignment_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_VariableAssignment_FormatString_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableAssignment_FormatString_descriptor,
        new java.lang.String[] { "Format", "Args", });
    internal_static_littlehorse_VariableMutation_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_littlehorse_VariableMutation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableMutation_descriptor,
        new java.lang.String[] { "LhsName", "LhsJsonPath", "Operation", "SourceVariable", "LiteralValue", "NodeOutput", "RhsValue", "LhsJsonPath", });
    internal_static_littlehorse_VariableMutation_NodeOutputSource_descriptor =
      internal_static_littlehorse_VariableMutation_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_VariableMutation_NodeOutputSource_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableMutation_NodeOutputSource_descriptor,
        new java.lang.String[] { "Jsonpath", "Jsonpath", });
    internal_static_littlehorse_VariableDef_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_littlehorse_VariableDef_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_VariableDef_descriptor,
        new java.lang.String[] { "Type", "Name", "DefaultValue", "DefaultValue", });
    internal_static_littlehorse_UTActionTrigger_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_UTActionTrigger_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UTActionTrigger_descriptor,
        new java.lang.String[] { "Task", "Cancel", "Reassign", "DelaySeconds", "Hook", "Action", });
    internal_static_littlehorse_UTActionTrigger_UTACancel_descriptor =
      internal_static_littlehorse_UTActionTrigger_descriptor.getNestedTypes().get(0);
    internal_static_littlehorse_UTActionTrigger_UTACancel_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UTActionTrigger_UTACancel_descriptor,
        new java.lang.String[] { });
    internal_static_littlehorse_UTActionTrigger_UTATask_descriptor =
      internal_static_littlehorse_UTActionTrigger_descriptor.getNestedTypes().get(1);
    internal_static_littlehorse_UTActionTrigger_UTATask_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UTActionTrigger_UTATask_descriptor,
        new java.lang.String[] { "Task", "Mutations", });
    internal_static_littlehorse_UTActionTrigger_UTAReassign_descriptor =
      internal_static_littlehorse_UTActionTrigger_descriptor.getNestedTypes().get(2);
    internal_static_littlehorse_UTActionTrigger_UTAReassign_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_UTActionTrigger_UTAReassign_descriptor,
        new java.lang.String[] { "UserId", "UserGroup", "UserId", "UserGroup", });
    internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_littlehorse_ExponentialBackoffRetryPolicy_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_ExponentialBackoffRetryPolicy_descriptor,
        new java.lang.String[] { "BaseIntervalMs", "MaxDelayMs", "Multiplier", });
    internal_static_littlehorse_TaskNode_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_TaskNode_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskNode_descriptor,
        new java.lang.String[] { "TaskDefId", "DynamicTask", "TimeoutSeconds", "Retries", "ExponentialBackoff", "Variables", "TaskToExecute", "ExponentialBackoff", });
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
    io.littlehorse.sdk.common.proto.ObjectId.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
