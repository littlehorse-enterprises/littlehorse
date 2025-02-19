package littlehorse_test

import (
	"testing"

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
