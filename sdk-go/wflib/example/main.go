package main

import (
	"fmt"
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/wflib"
	"google.golang.org/protobuf/encoding/protojson"
)

/*
func conditionWf(t *wflib.ThreadBuilder) {
	output, err := t.ExecuteTask("task1")

	t.IfElse(output.GreaterThan(5), func() {
		// todo
		t.ExecuteTask("task2")
	}, func() {
		t.ExecuteTask("task3")
	})

	t.If(output.LessThan(5), func() {

	})
}

type Car struct {
	Make  string
	Model string
}

func inputVarsStuff(t *wflib.ThreadBuilder) {
	bronco := &Car{Make: "Ford", Model: "Bronco"}
	colorado := &Car{Make: "Chevrolet", Model: "Colorado"}
	myBytes := []byte("hello")

	cars := make([]*Car, 2)

	cars[0] = bronco
	cars[1] = colorado

	t.AddVariable("my_int", 65321)
	myVar := t.AddVariable("my_json_obj", bronco)
	t.AddVariable("my_json_arr", cars)
	t.AddVariable("my_bytes", myBytes)
	t.Execute("task1", map[string]interface{}{
		"myVar":        myVar.JsonPath("$.make"),
		"literalThing": "hello there!",
	})

	myVar.Mutate(model.VariableMutationTypePb_ASSIGN, map[string]string{"foo": "bar"})
}

func simpleConditionals(t *wflib.ThreadBuilder) {
	myVar := t.AddVariable("my_var", 10)
	// myOtherVar := t.AddVariable("my_other_var", 11)

	t.Execute("task1", nil)

	t.DoIf(t.Condition(myVar, model.ComparatorPb_EQUALS, 10), func() {
		// t.DoIf(t.Condition(myOtherVar, model.ComparatorPb_IN, []int{10, 11}), func() {
		t.Execute("task1", nil)
		// })
	})

	t.Execute("task1", nil)
}

func simpleIfElse(t *wflib.ThreadBuilder) {
	t.DoIfElse(t.Condition(5, model.ComparatorPb_LESS_THAN, 10),
		func() {
			t.Execute("task1", nil)
		}, func() {
			t.Execute("task2", nil)
		},
	)
}

func simpleWf(t *wflib.ThreadBuilder) {
	t.Execute("hi", nil)
	t.Execute("bye", nil)
}

func simpleThreads(t *wflib.ThreadBuilder) {
	thr := t.SpawnThread(simpleWf, nil)
	t.Execute("task1", nil)
	t.WaitForThread(thr)
}
*/

func simpleConditionals(t *wflib.ThreadBuilder) {
	myVar := t.AddVariable("my_var", 10)

	t.Execute("task1", nil)

	t.DoIf(t.Condition(myVar, model.ComparatorPb_EQUALS, 10), func(ifBody *wflib.ThreadBuilder) {
		ifBody.Execute("task2")
	})

	t.Execute("task3", nil)
}

func main() {
	wf := wflib.LHWorkflow{
		Name:             "simple-wf",
		EntrypointThread: simpleConditionals,
	}

	wfSpec, err := wf.Compile()

	if err != nil {
		log.Fatal(err)
	}

	jsonBytes, err := protojson.MarshalOptions{
		EmitUnpopulated: true,
	}.Marshal(wfSpec)

	if err != nil {
		log.Fatal(err)
	}

	fmt.Println(string(jsonBytes))
}
