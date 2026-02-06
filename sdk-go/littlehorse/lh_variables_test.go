package littlehorse_test

import (
	"reflect"
	"testing"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/stretchr/testify/assert"
)

func TestStrToVarValWithStr(t *testing.T) {
	result, err := littlehorse.StrToVarVal("1234", lhproto.VariableType_INT)
	assert.Nil(t, err)
	assert.Equal(t, int64(1234), result.GetInt())

	result, err = littlehorse.StrToVarVal("1234", lhproto.VariableType_STR)
	assert.Nil(t, err)
	assert.Equal(t, "1234", result.GetStr())

	_, err = littlehorse.StrToVarVal("not-an-int", lhproto.VariableType_INT)
	assert.NotNil(t, err)

	result, _ = littlehorse.StrToVarVal("true", lhproto.VariableType_BOOL)
	assert.True(t, result.GetBool())

	result, _ = littlehorse.StrToVarVal("false", lhproto.VariableType_BOOL)
	assert.False(t, result.GetBool())
}

func TestReflectTypeToLHVarType(t *testing.T) {
	testMap := map[reflect.Type]lhproto.VariableType{
		reflect.TypeOf(""):                lhproto.VariableType_STR,
		reflect.TypeOf(time.Time{}):       lhproto.VariableType_TIMESTAMP,
		reflect.TypeOf(lhproto.WfRunId{}): lhproto.VariableType_WF_RUN_ID,
		reflect.TypeOf(1):                 lhproto.VariableType_INT,
		reflect.TypeOf(true):              lhproto.VariableType_BOOL,
		reflect.TypeOf([]int{}):           lhproto.VariableType_JSON_ARR,
		reflect.TypeOf(map[string]int{}):  lhproto.VariableType_JSON_OBJ,
		reflect.TypeOf(struct{}{}):        lhproto.VariableType_JSON_OBJ,
	}

	for key := range testMap {
		result := littlehorse.ReflectTypeToVarType(key)
		assert.Equal(t, testMap[key], result)
	}
}
