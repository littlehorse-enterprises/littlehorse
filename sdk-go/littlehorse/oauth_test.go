package littlehorse_test

import (
	"context"
	"encoding/json"
	"encoding/pem"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"runtime"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"golang.org/x/oauth2"
	"google.golang.org/grpc/credentials/oauth"
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

// newAuthServer starts a TLS server whose certificate is not trusted by the system,
// and returns it with the path to its CA certificate in PEM format and a count of token requests.
func newAuthServer(t *testing.T) (*httptest.Server, string, *atomic.Int32) {
	tokenRequests := &atomic.Int32{}
	mux := http.NewServeMux()
	server := httptest.NewUnstartedServer(mux)
	mux.HandleFunc("/.well-known/openid-configuration", func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]string{
			"issuer":                 server.URL,
			"authorization_endpoint": server.URL + "/auth",
			"token_endpoint":         server.URL + "/token",
			"jwks_uri":               server.URL + "/jwks",
		})
	})
	mux.HandleFunc("/token", func(w http.ResponseWriter, r *http.Request) {
		tokenRequests.Add(1)
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]any{
			"access_token": "new-access-token",
			"token_type":   "Bearer",
			"expires_in":   3600,
		})
	})
	server.StartTLS()
	t.Cleanup(server.Close)

	caPath := filepath.Join(t.TempDir(), "ca.crt")
	caPEM := pem.EncodeToMemory(&pem.Block{Type: "CERTIFICATE", Bytes: server.Certificate().Raw})
	require.NoError(t, os.WriteFile(caPath, caPEM, 0o600))
	return server, caPath, tokenRequests
}

func saveCredentials(t *testing.T, token *oauth2.Token) string {
	credentialsPath := filepath.Join(t.TempDir(), "littlehorse.credentials")
	require.NoError(t, littlehorse.SaveToken(credentialsPath, token))
	return credentialsPath
}

func readCredentials(t *testing.T, credentialsPath string) *oauth2.Token {
	data, err := os.ReadFile(credentialsPath)
	require.NoError(t, err)
	token := &oauth2.Token{}
	require.NoError(t, json.Unmarshal(data, token))
	return token
}

// assertFileMode does nothing on Windows, which has no Unix file modes.
func assertFileMode(t *testing.T, path string, mode os.FileMode) {
	if runtime.GOOS == "windows" {
		return
	}
	info, err := os.Stat(path)
	require.NoError(t, err)
	assert.Equal(t, mode, info.Mode().Perm())
}

func TestClientCredentialsTrustsOauthCaCert(t *testing.T) {
	server, caPath, _ := newAuthServer(t)
	config := littlehorse.OauthConfig{
		ClientId:         "worker",
		ClientSecret:     "secret",
		TokenEndpointUrl: server.URL + "/token",
		CaCert:           caPath,
	}

	token, err := config.GetTokenSource().(oauth.TokenSource).Token()

	require.NoError(t, err)
	assert.Equal(t, "new-access-token", token.AccessToken)
}

func TestClientCredentialsRejectsUnknownCa(t *testing.T) {
	server, _, _ := newAuthServer(t)
	config := littlehorse.OauthConfig{
		ClientId:         "worker",
		ClientSecret:     "secret",
		TokenEndpointUrl: server.URL + "/token",
	}

	_, err := config.GetTokenSource().(oauth.TokenSource).Token()

	assert.ErrorContains(t, err, "certificate")
}

func TestAuthorizationCodeDiscoveryAndRefreshTrustOauthCaCert(t *testing.T) {
	server, caPath, _ := newAuthServer(t)
	config := littlehorse.OauthConfig{
		ClientId:   "lhctl",
		AuthServer: server.URL,
		CredentialsLocation: saveCredentials(t, &oauth2.Token{
			AccessToken:  "expired-access-token",
			TokenType:    "Bearer",
			RefreshToken: "refresh-token",
			Expiry:       time.Now().Add(-time.Hour),
		}),
		CaCert: caPath,
	}

	metadata, err := config.GetTokenSource().GetRequestMetadata(context.Background())

	require.NoError(t, err)
	assert.Equal(t, "Bearer new-access-token", metadata["authorization"])
}

func TestWithHTTPClientFailsOnInvalidCaCert(t *testing.T) {
	caPath := filepath.Join(t.TempDir(), "ca.crt")
	require.NoError(t, os.WriteFile(caPath, []byte("not a certificate"), 0o600))
	config := littlehorse.OauthConfig{CaCert: caPath}

	_, err := config.WithHTTPClient(context.Background())

	assert.ErrorContains(t, err, littlehorse.OAUTH_CA_CERT_KEY)
}

func TestWithHTTPClientKeepsContextWithoutCaCert(t *testing.T) {
	ctx := context.Background()
	config := littlehorse.OauthConfig{}

	result, err := config.WithHTTPClient(ctx)

	require.NoError(t, err)
	assert.Equal(t, ctx, result)
}

func TestSaveTokenReplacesFileReadableOnlyByOwner(t *testing.T) {
	dir := t.TempDir()
	credentialsPath := filepath.Join(dir, "littlehorse.credentials")
	require.NoError(t, os.WriteFile(credentialsPath, []byte("old"), 0o644))

	err := littlehorse.SaveToken(credentialsPath, &oauth2.Token{AccessToken: "access-token"})

	require.NoError(t, err)
	assert.Equal(t, "access-token", readCredentials(t, credentialsPath).AccessToken)
	assertFileMode(t, credentialsPath, 0o600)
	entries, err := os.ReadDir(dir)
	require.NoError(t, err)
	assert.Len(t, entries, 1, "the temporary file must not be left behind")
}

func TestSaveTokenCreatesMissingDirectory(t *testing.T) {
	dir := filepath.Join(t.TempDir(), "missing")
	credentialsPath := filepath.Join(dir, "littlehorse.credentials")

	err := littlehorse.SaveToken(credentialsPath, &oauth2.Token{AccessToken: "access-token"})

	require.NoError(t, err)
	assert.Equal(t, "access-token", readCredentials(t, credentialsPath).AccessToken)
	assertFileMode(t, dir, 0o700)
}

func TestAuthorizationCodeDoesNotRefreshTokenWithoutExpiry(t *testing.T) {
	server, caPath, tokenRequests := newAuthServer(t)
	config := littlehorse.OauthConfig{
		ClientId:            "lhctl",
		AuthServer:          server.URL,
		CredentialsLocation: saveCredentials(t, &oauth2.Token{AccessToken: "access-token", TokenType: "Bearer", RefreshToken: "refresh-token"}),
		CaCert:              caPath,
	}
	tokenSource := config.GetTokenSource()

	first, err := tokenSource.GetRequestMetadata(context.Background())
	require.NoError(t, err)
	second, err := tokenSource.GetRequestMetadata(context.Background())
	require.NoError(t, err)

	assert.Equal(t, "Bearer access-token", first["authorization"])
	assert.Equal(t, "Bearer access-token", second["authorization"])
	assert.Equal(t, int32(0), tokenRequests.Load())
}

func TestAuthorizationCodeRefreshesOnceBeforeExpiry(t *testing.T) {
	server, caPath, tokenRequests := newAuthServer(t)
	credentialsPath := saveCredentials(t, &oauth2.Token{
		AccessToken:  "expiring-access-token",
		TokenType:    "Bearer",
		RefreshToken: "refresh-token",
		Expiry:       time.Now().Add(5 * time.Second),
	})
	config := littlehorse.OauthConfig{
		ClientId:            "lhctl",
		AuthServer:          server.URL,
		CredentialsLocation: credentialsPath,
		CaCert:              caPath,
	}
	tokenSource := config.GetTokenSource()

	var wg sync.WaitGroup
	for range 10 {
		wg.Add(1)
		go func() {
			defer wg.Done()
			metadata, err := tokenSource.GetRequestMetadata(context.Background())
			assert.NoError(t, err)
			assert.Equal(t, "Bearer new-access-token", metadata["authorization"])
		}()
	}
	wg.Wait()

	assert.Equal(t, int32(1), tokenRequests.Load())
	assert.Equal(t, "new-access-token", readCredentials(t, credentialsPath).AccessToken)
	assertFileMode(t, credentialsPath, 0o600)
}

func TestAuthorizationCodePicksUpNewLogin(t *testing.T) {
	server, caPath, _ := newAuthServer(t)
	credentialsPath := saveCredentials(t, &oauth2.Token{AccessToken: "first-access-token", TokenType: "Bearer"})
	config := littlehorse.OauthConfig{
		ClientId:            "lhctl",
		AuthServer:          server.URL,
		CredentialsLocation: credentialsPath,
		CaCert:              caPath,
	}
	tokenSource := config.GetTokenSource()
	_, err := tokenSource.GetRequestMetadata(context.Background())
	require.NoError(t, err)
	require.NoError(t, littlehorse.SaveToken(credentialsPath, &oauth2.Token{AccessToken: "second-access-token", TokenType: "Bearer"}))

	metadata, err := tokenSource.GetRequestMetadata(context.Background())

	require.NoError(t, err)
	assert.Equal(t, "Bearer second-access-token", metadata["authorization"])
}

func TestAuthorizationCodeRestrictsReadableCredentials(t *testing.T) {
	server, caPath, _ := newAuthServer(t)
	credentialsPath := saveCredentials(t, &oauth2.Token{AccessToken: "access-token", TokenType: "Bearer"})
	require.NoError(t, os.Chmod(credentialsPath, 0o644))
	config := littlehorse.OauthConfig{
		ClientId:            "lhctl",
		AuthServer:          server.URL,
		CredentialsLocation: credentialsPath,
		CaCert:              caPath,
	}

	_, err := config.GetTokenSource().GetRequestMetadata(context.Background())

	require.NoError(t, err)
	assertFileMode(t, credentialsPath, 0o600)
}
