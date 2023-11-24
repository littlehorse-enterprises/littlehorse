package common

import (
	"fmt"
	"log"
	"reflect"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

func GetInputVarDefs(w *model.WfSpec) map[string]*model.VariableDef {
	// First, find the entrypoint threadspec
	tspec := w.ThreadSpecs[w.EntrypointThreadName]

	if tspec == nil {
		panic("Not possible.")
	}

	out := make(map[string]*model.VariableDef)

	for _, vd := range tspec.VariableDefs {
		out[vd.VarDef.Name] = vd.VarDef
	}

	return out
}

func GetWfRunIdFromTaskSource(source *model.TaskRunSource) *string {
	switch s := source.TaskRunSource.(type) {
	case *model.TaskRunSource_UserTaskTrigger:
		return &s.UserTaskTrigger.NodeRunId.WfRunId
	case *model.TaskRunSource_TaskNode:
		return &s.TaskNode.NodeRunId.WfRunId
	}
	return nil
}

func PrintResp(resp proto.Message, err error) {
	if err != nil {
		log.Fatal(err)
	}

	b, err := protojson.MarshalOptions{
		Indent:          "  ",
		EmitUnpopulated: true,
	}.Marshal(resp)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println(string(b))
}

func PrintProto(p proto.Message) {
	b, _ := protojson.MarshalOptions{
		Indent:          "  ",
		EmitUnpopulated: true,
	}.Marshal(p)
	fmt.Println(string(b))
}

func GetIsPtrAndType(thing reflect.Type) (bool, reflect.Kind) {
	if thing.Kind() == reflect.Ptr {
		return true, thing.Elem().Kind()
	} else {
		return false, thing.Kind()
	}
}

func formatInt32(n int32) string {
	return strconv.FormatInt(int64(n), 10)
}

func derefString(s *string) string {
	if s != nil {
		return *s
	}
	return ""
}
