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
    internal_static_littlehorse_JsonIndex_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_littlehorse_JsonIndex_fieldAccessorTable;
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
      "on_enums.proto\032\016variable.proto\"\311\002\n\022Varia" +
      "bleAssignment\022\026\n\tjson_path\030\001 \001(\tH\001\210\001\001\022\027\n" +
      "\rvariable_name\030\002 \001(\tH\000\0223\n\rliteral_value\030" +
      "\003 \001(\0132\032.littlehorse.VariableValueH\000\022E\n\rf" +
      "ormat_string\030\004 \001(\0132,.littlehorse.Variabl" +
      "eAssignment.FormatStringH\000\032n\n\014FormatStri" +
      "ng\022/\n\006format\030\001 \001(\0132\037.littlehorse.Variabl" +
      "eAssignment\022-\n\004args\030\002 \003(\0132\037.littlehorse." +
      "VariableAssignmentB\010\n\006sourceB\014\n\n_json_pa" +
      "th\"\205\003\n\020VariableMutation\022\020\n\010lhs_name\030\001 \001(" +
      "\t\022\032\n\rlhs_json_path\030\002 \001(\tH\001\210\001\001\0224\n\toperati" +
      "on\030\003 \001(\0162!.littlehorse.VariableMutationT" +
      "ype\022:\n\017source_variable\030\004 \001(\0132\037.littlehor" +
      "se.VariableAssignmentH\000\0223\n\rliteral_value" +
      "\030\005 \001(\0132\032.littlehorse.VariableValueH\000\022E\n\013" +
      "node_output\030\006 \001(\0132..littlehorse.Variable" +
      "Mutation.NodeOutputSourceH\000\0326\n\020NodeOutpu" +
      "tSource\022\025\n\010jsonpath\030\n \001(\tH\000\210\001\001B\013\n\t_jsonp" +
      "athB\013\n\trhs_valueB\020\n\016_lhs_json_path\"\371\001\n\013V" +
      "ariableDef\022\'\n\004type\030\001 \001(\0162\031.littlehorse.V" +
      "ariableType\022\014\n\004name\030\002 \001(\t\022/\n\nindex_type\030" +
      "\003 \001(\0162\026.littlehorse.IndexTypeH\000\210\001\001\022,\n\014js" +
      "on_indexes\030\004 \003(\0132\026.littlehorse.JsonIndex" +
      "\0221\n\rdefault_value\030\005 \001(\0132\032.littlehorse.Va" +
      "riableValue\022\022\n\npersistent\030\006 \001(\010B\r\n\013_inde" +
      "x_type\"E\n\tJsonIndex\022\014\n\004path\030\001 \001(\t\022*\n\nind" +
      "ex_type\030\002 \001(\0162\026.littlehorse.IndexType\"\357\004" +
      "\n\017UTActionTrigger\0224\n\004task\030\001 \001(\0132$.little" +
      "horse.UTActionTrigger.UTATaskH\000\0228\n\006cance" +
      "l\030\002 \001(\0132&.littlehorse.UTActionTrigger.UT" +
      "ACancelH\000\022<\n\010reassign\030\003 \001(\0132(.littlehors" +
      "e.UTActionTrigger.UTAReassignH\000\0226\n\rdelay" +
      "_seconds\030\005 \001(\0132\037.littlehorse.VariableAss" +
      "ignment\0221\n\004hook\030\006 \001(\0162#.littlehorse.UTAc" +
      "tionTrigger.UTHook\032\013\n\tUTACancel\032`\n\007UTATa" +
      "sk\022#\n\004task\030\001 \001(\0132\025.littlehorse.TaskNode\022" +
      "0\n\tmutations\030\002 \003(\0132\035.littlehorse.Variabl" +
      "eMutation\032\231\001\n\013UTAReassign\0225\n\007user_id\030\001 \001" +
      "(\0132\037.littlehorse.VariableAssignmentH\000\210\001\001" +
      "\0228\n\nuser_group\030\002 \001(\0132\037.littlehorse.Varia" +
      "bleAssignmentH\001\210\001\001B\n\n\010_user_idB\r\n\013_user_" +
      "group\".\n\006UTHook\022\016\n\nON_ARRIVAL\020\000\022\024\n\020ON_TA" +
      "SK_ASSIGNED\020\001B\010\n\006action\"\177\n\010TaskNode\022\025\n\rt" +
      "ask_def_name\030\001 \001(\t\022\027\n\017timeout_seconds\030\002 " +
      "\001(\005\022\017\n\007retries\030\003 \001(\005\0222\n\tvariables\030\004 \003(\0132" +
      "\037.littlehorse.VariableAssignment*\230\001\n\024Var" +
      "iableMutationType\022\n\n\006ASSIGN\020\000\022\007\n\003ADD\020\001\022\n" +
      "\n\006EXTEND\020\002\022\014\n\010SUBTRACT\020\003\022\014\n\010MULTIPLY\020\004\022\n" +
      "\n\006DIVIDE\020\005\022\025\n\021REMOVE_IF_PRESENT\020\006\022\020\n\014REM" +
      "OVE_INDEX\020\007\022\016\n\nREMOVE_KEY\020\010*.\n\tIndexType" +
      "\022\017\n\013LOCAL_INDEX\020\000\022\020\n\014REMOTE_INDEX\020\001*\204\001\n\n" +
      "Comparator\022\r\n\tLESS_THAN\020\000\022\020\n\014GREATER_THA" +
      "N\020\001\022\020\n\014LESS_THAN_EQ\020\002\022\023\n\017GREATER_THAN_EQ" +
      "\020\003\022\n\n\006EQUALS\020\004\022\016\n\nNOT_EQUALS\020\005\022\006\n\002IN\020\006\022\n" +
      "\n\006NOT_IN\020\007BG\n\037io.littlehorse.sdk.common." +
      "protoP\001Z\007.;model\252\002\030LittleHorse.Common.Pr" +
      "otob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor(),
          io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor(),
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
        new java.lang.String[] { "Type", "Name", "IndexType", "JsonIndexes", "DefaultValue", "Persistent", "IndexType", });
    internal_static_littlehorse_JsonIndex_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_littlehorse_JsonIndex_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_JsonIndex_descriptor,
        new java.lang.String[] { "Path", "IndexType", });
    internal_static_littlehorse_UTActionTrigger_descriptor =
      getDescriptor().getMessageTypes().get(4);
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
    internal_static_littlehorse_TaskNode_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_littlehorse_TaskNode_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_littlehorse_TaskNode_descriptor,
        new java.lang.String[] { "TaskDefName", "TimeoutSeconds", "Retries", "Variables", });
    io.littlehorse.sdk.common.proto.CommonEnums.getDescriptor();
    io.littlehorse.sdk.common.proto.VariableOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
