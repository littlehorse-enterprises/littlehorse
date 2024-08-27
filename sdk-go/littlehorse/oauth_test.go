package littlehorse_test

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestIfOauthIsEnableForAuthorizationCode(t *testing.T) {
	oauthConfig := littlehorse.OauthConfig{
		ClientId:   "myclientid",
		AuthServer: "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), littlehorse.AuthorizationCode)
	assert.True(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}

func TestIfOauthIsEnableForClientCredentials(t *testing.T) {
	oauthConfig := littlehorse.OauthConfig{
		ClientId:         "myclientid",
		ClientSecret:     "myclientsecret",
		TokenEndpointUrl: "myclientserver",
	}

	assert.True(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), littlehorse.ClientCredentials)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.True(t, oauthConfig.IsClientCredentials())
}

func TestGetUndefinedFlowIfConfigsAreEmpty(t *testing.T) {
	oauthConfig := littlehorse.OauthConfig{}
	assert.False(t, oauthConfig.IsEnabled())
	assert.Equal(t, oauthConfig.DeduceFlow(), littlehorse.Undefined)
	assert.False(t, oauthConfig.IsAuthorizationCode())
	assert.False(t, oauthConfig.IsClientCredentials())
}
