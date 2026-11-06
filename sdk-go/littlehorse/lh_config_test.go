package littlehorse_test

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestNewConfigFromEnvEnablesResourceExhaustedRetryByDefault(t *testing.T) {
	t.Setenv("LHC_GRPC_RESOURCE_EXHAUSTED_RETRY", "")

	config := littlehorse.NewConfigFromEnv()

	assert.True(t, config.GrpcResourceExhaustedRetry)
	assert.Len(t, config.UnaryInterceptors, 1)
}

func TestNewConfigFromEnvCanDisableResourceExhaustedRetry(t *testing.T) {
	t.Setenv("LHC_GRPC_RESOURCE_EXHAUSTED_RETRY", "false")

	config := littlehorse.NewConfigFromEnv()

	assert.False(t, config.GrpcResourceExhaustedRetry)
	assert.Nil(t, config.UnaryInterceptors)
}

func TestNewConfigFromPropsCanDisableResourceExhaustedRetry(t *testing.T) {
	tempDir := t.TempDir()
	propsPath := filepath.Join(tempDir, "littlehorse.config")
	require.NoError(t, os.WriteFile(propsPath, []byte("LHC_GRPC_RESOURCE_EXHAUSTED_RETRY=false\n"), 0o600))

	config, err := littlehorse.NewConfigFromProps(propsPath)

	require.NoError(t, err)
	assert.False(t, config.GrpcResourceExhaustedRetry)
	assert.Nil(t, config.UnaryInterceptors)
}

func TestNewConfigFromPropsReadsOauthCallbackPort(t *testing.T) {
	propsPath := filepath.Join(t.TempDir(), "littlehorse.config")
	props := "LHC_OAUTH_CALLBACK_PORT=30000\nLHW_NUM_WORKER_THREADS=8\n"
	require.NoError(t, os.WriteFile(propsPath, []byte(props), 0o600))

	config, err := littlehorse.NewConfigFromProps(propsPath)

	require.NoError(t, err)
	assert.Equal(t, int32(30000), config.OauthConfig.CallbackPort)
}

func TestNewConfigFromPropsUsesDefaultOauthCallbackPort(t *testing.T) {
	propsPath := filepath.Join(t.TempDir(), "littlehorse.config")
	require.NoError(t, os.WriteFile(propsPath, []byte("LHW_NUM_WORKER_THREADS=8\n"), 0o600))

	config, err := littlehorse.NewConfigFromProps(propsPath)

	require.NoError(t, err)
	assert.Equal(t, int32(littlehorse.DEFAULT_OAUTH_CALLBACK_PORT), config.OauthConfig.CallbackPort)
}
