package auth_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/auth"
	"github.com/stretchr/testify/assert"
)

func TestIfOauthIsEnableForAuthorizationCode(t *testing.T) {
	oauthConfig := auth.OauthConfig{
		ClientId:   "myclientid",
		AuthServer: "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), auth.AuthorizationCode)
	assert.True(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}

func TestIfOauthIsEnableForClientCredentials(t *testing.T) {
	oauthConfig := auth.OauthConfig{
		ClientId:         "myclientid",
		ClientSecret:     "myclientsecret",
		TokenEndpointUrl: "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), auth.ClientCredentials)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.True(t, oauthConfig.IsClientCredentials())
}

func TestGetUndefinedFlowIfConfigsAreEmpty(t *testing.T) {
	oauthConfig := auth.OauthConfig{}
	assert.False(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), auth.Undefined)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}
