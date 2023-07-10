package auth

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestIfOauthIsEnableForAuthorizationCode(t *testing.T) {
	oauthConfig := OauthConfig{
		ClientId:   "myclientid",
		AuthServer: "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), AuthorizationCode)
	assert.True(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}

func TestIfOauthIsEnableForClientCredentials(t *testing.T) {
	oauthConfig := OauthConfig{
		ClientId:     "myclientid",
		ClientSecret: "myclientsecret",
		AuthServer:   "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), ClientCredentials)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.True(t, oauthConfig.IsClientCredentials())
}

func GetUndefinedFlowIfConfigsAreEmpty(t *testing.T) {
	oauthConfig := OauthConfig{}
	assert.False(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), Undefined)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}
