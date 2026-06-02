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
