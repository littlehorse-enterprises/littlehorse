package common_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/stretchr/testify/assert"
)

func TestStrToVarValWithStr(t *testing.T) {
	result, err := common.StrToVarVal("1234", model.VariableType_INT)
	assert.Nil(t, err)
	assert.Equal(t, int64(1234), result.GetInt())

	result, err = common.StrToVarVal("1234", model.VariableType_STR)
	assert.Nil(t, err)
	assert.Equal(t, "1234", result.GetStr())

	_, err = common.StrToVarVal("not-an-int", model.VariableType_INT)
	assert.NotNil(t, err)

	result, _ = common.StrToVarVal("true", model.VariableType_BOOL)
	assert.True(t, result.GetBool())

	result, _ = common.StrToVarVal("false", model.VariableType_BOOL)
	assert.False(t, result.GetBool())
}
