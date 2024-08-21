package littlehorse_test

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestStrToVarValWithStr(t *testing.T) {
	result, err := littlehorse.StrToVarVal("1234", model.VariableType_INT)
	assert.Nil(t, err)
	assert.Equal(t, int64(1234), result.GetInt())

	result, err = littlehorse.StrToVarVal("1234", model.VariableType_STR)
	assert.Nil(t, err)
	assert.Equal(t, "1234", result.GetStr())

	_, err = littlehorse.StrToVarVal("not-an-int", model.VariableType_INT)
	assert.NotNil(t, err)

	result, _ = littlehorse.StrToVarVal("true", model.VariableType_BOOL)
	assert.True(t, result.GetBool())

	result, _ = littlehorse.StrToVarVal("false", model.VariableType_BOOL)
	assert.False(t, result.GetBool())
}
