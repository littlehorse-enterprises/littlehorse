package internal

import (
	"context"
	"crypto/rand"
	"crypto/rsa"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"reflect"
	"testing"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"github.com/go-jose/go-jose/v4"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"golang.org/x/oauth2"
)

// fakeProvider is an OIDC provider whose token endpoint only issues tokens for a
// code_verifier that matches the code_challenge of the authorization request.
type fakeProvider struct {
	server    *httptest.Server
	key       *rsa.PrivateKey
	challenge string
	nonce     string
	verifiers []string
}

func newFakeProvider(t *testing.T) *fakeProvider {
	key, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		t.Fatal(err)
	}
	p := &fakeProvider{key: key}

	mux := http.NewServeMux()
	mux.HandleFunc("/.well-known/openid-configuration", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]any{
			"issuer":                                p.server.URL,
			"authorization_endpoint":                p.server.URL + "/auth",
			"token_endpoint":                        p.server.URL + "/token",
			"jwks_uri":                              p.server.URL + "/jwks",
			"id_token_signing_alg_values_supported": []string{"RS256"},
		})
	})
	mux.HandleFunc("/jwks", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, jose.JSONWebKeySet{Keys: []jose.JSONWebKey{
			{Key: &key.PublicKey, KeyID: "test", Algorithm: string(jose.RS256), Use: "sig"},
		}})
	})
	mux.HandleFunc("/token", p.token)

	p.server = httptest.NewServer(mux)
	t.Cleanup(p.server.Close)
	return p
}

func (p *fakeProvider) token(w http.ResponseWriter, r *http.Request) {
	if err := r.ParseForm(); err != nil {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid_request"})
		return
	}
	verifier := r.PostForm.Get("code_verifier")
	p.verifiers = append(p.verifiers, verifier)
	if r.PostForm.Get("code") != "test-code" || oauth2.S256ChallengeFromVerifier(verifier) != p.challenge {
		writeJSON(w, http.StatusBadRequest, map[string]string{"error": "invalid_grant"})
		return
	}

	signer, err := jose.NewSigner(jose.SigningKey{Algorithm: jose.RS256, Key: jose.JSONWebKey{Key: p.key, KeyID: "test"}}, nil)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}
	now := time.Now().Unix()
	claims, _ := json.Marshal(map[string]any{
		"iss": p.server.URL, "aud": "lhctl", "sub": "user", "nonce": p.nonce, "iat": now, "exp": now + 300,
	})
	signed, err := signer.Sign(claims)
	if err != nil {
		writeJSON(w, http.StatusInternalServerError, map[string]string{"error": err.Error()})
		return
	}
	idToken, _ := signed.CompactSerialize()

	writeJSON(w, http.StatusOK, map[string]any{
		"access_token":  "test-access-token",
		"refresh_token": "test-refresh-token",
		"token_type":    "Bearer",
		"expires_in":    300,
		"id_token":      idToken,
	})
}

func writeJSON(w http.ResponseWriter, status int, body any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(body)
}

func TestHomeRequestSendsPKCEChallenge(t *testing.T) {
	config := oauth2.Config{
		ClientID:    "lhctl",
		RedirectURL: "http://127.0.0.1:25242/callback",
		Endpoint:    oauth2.Endpoint{AuthURL: "https://idp.example.com/auth"},
	}
	verifier := oauth2.GenerateVerifier()
	recorder := httptest.NewRecorder()

	handleHomeRequest(config, verifier)(recorder, httptest.NewRequest(http.MethodGet, "/", nil))

	location, err := url.Parse(recorder.Header().Get("Location"))
	if err != nil {
		t.Fatalf("invalid redirect location: %v", err)
	}
	query := location.Query()
	if got, want := query.Get("code_challenge"), oauth2.S256ChallengeFromVerifier(verifier); got != want {
		t.Errorf("code_challenge = %q, want %q", got, want)
	}
	if got := query.Get("code_challenge_method"); got != "S256" {
		t.Errorf("code_challenge_method = %q, want S256", got)
	}
}

func TestLoginExchangesCodeWithPKCEVerifier(t *testing.T) {
	idp := newFakeProvider(t)
	ctx := context.Background()
	provider, err := oidc.NewProvider(ctx, idp.server.URL)
	if err != nil {
		t.Fatal(err)
	}
	config := oauth2.Config{
		ClientID:    "lhctl",
		RedirectURL: "http://127.0.0.1:25242/callback",
		Endpoint:    provider.Endpoint(),
		Scopes:      []string{oidc.ScopeOpenID},
	}
	verifier := oauth2.GenerateVerifier()

	home := httptest.NewRecorder()
	handleHomeRequest(config, verifier)(home, httptest.NewRequest(http.MethodGet, "/", nil))
	authURL, err := url.Parse(home.Header().Get("Location"))
	if err != nil {
		t.Fatalf("invalid redirect location: %v", err)
	}
	idp.challenge = authURL.Query().Get("code_challenge")
	idp.nonce = authURL.Query().Get("nonce")

	callback := httptest.NewRequest(http.MethodGet,
		"/callback?code=test-code&state="+url.QueryEscape(authURL.Query().Get("state")), nil)
	for _, cookie := range home.Result().Cookies() {
		callback.AddCookie(cookie)
	}
	credentials := filepath.Join(t.TempDir(), "credentials.json")
	lhConfig := &littlehorse.LHConfig{OauthConfig: &littlehorse.OauthConfig{CredentialsLocation: credentials}}
	response := httptest.NewRecorder()

	// A wrong or missing verifier makes the token endpoint fail, and the handler exits the test process.
	handleCallbackRequest(ctx, &http.Server{}, lhConfig, provider, config, verifier)(response, callback)

	if !reflect.DeepEqual(idp.verifiers, []string{verifier}) {
		t.Errorf("token endpoint received verifiers %q, want [%q]", idp.verifiers, verifier)
	}
	if response.Code != http.StatusOK {
		t.Errorf("callback status = %d, want %d", response.Code, http.StatusOK)
	}
	data, err := os.ReadFile(credentials)
	if err != nil {
		t.Fatalf("credentials were not written: %v", err)
	}
	var token oauth2.Token
	if err := json.Unmarshal(data, &token); err != nil {
		t.Fatalf("invalid credentials file: %v", err)
	}
	if token.AccessToken != "test-access-token" || token.RefreshToken != "test-refresh-token" {
		t.Errorf("credentials = %+v, want the tokens from the provider", token)
	}
}
