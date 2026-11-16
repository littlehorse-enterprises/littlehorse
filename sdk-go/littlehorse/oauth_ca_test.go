package littlehorse_test

import (
	"context"
	"encoding/json"
	"encoding/pem"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"golang.org/x/oauth2"
	"google.golang.org/grpc/credentials/oauth"
)

// newAuthServer starts a TLS server whose certificate is not trusted by the system,
// and returns it with the path to its CA certificate in PEM format.
func newAuthServer(t *testing.T) (*httptest.Server, string) {
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
	return server, caPath
}

func TestClientCredentialsTrustsOauthCaCert(t *testing.T) {
	server, caPath := newAuthServer(t)
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
	server, _ := newAuthServer(t)
	config := littlehorse.OauthConfig{
		ClientId:         "worker",
		ClientSecret:     "secret",
		TokenEndpointUrl: server.URL + "/token",
	}

	_, err := config.GetTokenSource().(oauth.TokenSource).Token()

	assert.ErrorContains(t, err, "certificate")
}

func TestAuthorizationCodeDiscoveryAndRefreshTrustOauthCaCert(t *testing.T) {
	server, caPath := newAuthServer(t)
	credentialsPath := filepath.Join(t.TempDir(), "littlehorse.credentials")
	expired, err := json.Marshal(&oauth2.Token{
		AccessToken:  "expired-access-token",
		TokenType:    "Bearer",
		RefreshToken: "refresh-token",
		Expiry:       time.Now().Add(-time.Hour),
	})
	require.NoError(t, err)
	require.NoError(t, os.WriteFile(credentialsPath, expired, 0o600))
	config := littlehorse.OauthConfig{
		ClientId:            "lhctl",
		AuthServer:          server.URL,
		CredentialsLocation: credentialsPath,
		CaCert:              caPath,
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
