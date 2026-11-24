package internal

import (
	"context"
	"crypto/rand"
	"crypto/rsa"
	"encoding/json"
	"errors"
	"net"
	"net/http"
	"net/http/httptest"
	"net/url"
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"testing"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"github.com/go-jose/go-jose/v4"
	"golang.org/x/oauth2"
)

// fakeProvider is an OIDC provider whose token endpoint only issues tokens for a
// code_verifier that matches the code_challenge of the authorization request.
type fakeProvider struct {
	server    *httptest.Server
	key       *rsa.PrivateKey
	challenge string
	nonce     string
	atHash    string
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
	claims := map[string]any{
		"iss": p.server.URL, "aud": "lhctl", "sub": "user", "nonce": p.nonce, "iat": now, "exp": now + 300,
	}
	if p.atHash != "" {
		claims["at_hash"] = p.atHash
	}
	payload, _ := json.Marshal(claims)
	signed, err := signer.Sign(payload)
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

func newTestSession(t *testing.T) (*loginSession, *fakeProvider, context.Context) {
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
	session, err := newLoginSession(provider, config, filepath.Join(t.TempDir(), "credentials.json"))
	if err != nil {
		t.Fatal(err)
	}
	idp.challenge = oauth2.S256ChallengeFromVerifier(session.verifier)
	idp.nonce = session.nonce
	return session, idp, ctx
}

func serve(ctx context.Context, session *loginSession, method, target string) *httptest.ResponseRecorder {
	recorder := httptest.NewRecorder()
	newLoginMux(ctx, session).ServeHTTP(recorder, httptest.NewRequest(method, target, nil))
	return recorder
}

func callbackURL(session *loginSession, params url.Values) string {
	params.Set("state", session.state)
	return "/callback?" + params.Encode()
}

// loginResult returns the login result, and false if the login is still waiting.
func loginResult(session *loginSession) (error, bool) {
	select {
	case err := <-session.result:
		return err, true
	default:
		return nil, false
	}
}

func TestHomeRedirectsWithPKCEChallengeAndNoCookies(t *testing.T) {
	session, _, ctx := newTestSession(t)

	response := serve(ctx, session, http.MethodGet, "/")

	if response.Code != http.StatusFound {
		t.Fatalf("status = %d, want %d", response.Code, http.StatusFound)
	}
	location, err := url.Parse(response.Header().Get("Location"))
	if err != nil {
		t.Fatalf("invalid redirect location: %v", err)
	}
	query := location.Query()
	want := map[string]string{
		"code_challenge":        oauth2.S256ChallengeFromVerifier(session.verifier),
		"code_challenge_method": "S256",
		"state":                 session.state,
		"nonce":                 session.nonce,
	}
	for key, value := range want {
		if got := query.Get(key); got != value {
			t.Errorf("%s = %q, want %q", key, got, value)
		}
	}
	if cookies := response.Header().Values("Set-Cookie"); len(cookies) > 0 {
		t.Errorf("home set cookies %q, want none", cookies)
	}
}

func TestLoginExchangesCodeWithPKCEVerifier(t *testing.T) {
	session, idp, ctx := newTestSession(t)

	response := serve(ctx, session, http.MethodGet, callbackURL(session, url.Values{"code": {"test-code"}}))

	if err, done := loginResult(session); !done || err != nil {
		t.Fatalf("login result = %v (done %v), want success", err, done)
	}
	if response.Code != http.StatusOK {
		t.Errorf("callback status = %d, want %d", response.Code, http.StatusOK)
	}
	if !reflect.DeepEqual(idp.verifiers, []string{session.verifier}) {
		t.Errorf("token endpoint received verifiers %q, want [%q]", idp.verifiers, session.verifier)
	}
	data, err := os.ReadFile(session.credentialsLocation)
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

func TestCallbackWithoutTheLoginStateKeepsWaiting(t *testing.T) {
	session, idp, ctx := newTestSession(t)

	for _, target := range []string{"/callback", "/callback?code=test-code&state=wrong"} {
		if response := serve(ctx, session, http.MethodGet, target); response.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want %d", target, response.Code, http.StatusBadRequest)
		}
	}
	if err, done := loginResult(session); done {
		t.Fatalf("login finished with %v, want it to keep waiting", err)
	}
	if len(idp.verifiers) != 0 {
		t.Errorf("token endpoint was called %d times, want 0", len(idp.verifiers))
	}

	serve(ctx, session, http.MethodGet, callbackURL(session, url.Values{"code": {"test-code"}}))
	if err, done := loginResult(session); !done || err != nil {
		t.Fatalf("login result = %v (done %v), want success", err, done)
	}
}

func TestCallbackFailures(t *testing.T) {
	tests := []struct {
		name    string
		params  url.Values
		prepare func(*fakeProvider)
		want    string
	}{
		{
			name:   "provider error",
			params: url.Values{"error": {"access_denied"}, "error_description": {"User denied access"}},
			want:   "login failed: access_denied: User denied access",
		},
		{
			name:   "missing code",
			params: url.Values{},
			want:   "no authorization code",
		},
		{
			name:   "another issuer",
			params: url.Values{"code": {"test-code"}, "iss": {"https://idp.example.com"}},
			want:   "came from issuer",
		},
		{
			name:    "wrong PKCE verifier",
			params:  url.Values{"code": {"test-code"}},
			prepare: func(idp *fakeProvider) { idp.challenge = oauth2.S256ChallengeFromVerifier(oauth2.GenerateVerifier()) },
			want:    "invalid_grant",
		},
		{
			name:    "wrong nonce",
			params:  url.Values{"code": {"test-code"}},
			prepare: func(idp *fakeProvider) { idp.nonce = "another-nonce" },
			want:    "nonce did not match",
		},
		{
			name:    "access token not bound to the ID token",
			params:  url.Values{"code": {"test-code"}},
			prepare: func(idp *fakeProvider) { idp.atHash = "not-the-hash" },
			want:    "access token does not match",
		},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			session, idp, ctx := newTestSession(t)
			if test.prepare != nil {
				test.prepare(idp)
			}

			response := serve(ctx, session, http.MethodGet, callbackURL(session, test.params))

			err, done := loginResult(session)
			if !done || err == nil || !strings.Contains(err.Error(), test.want) {
				t.Fatalf("login result = %v (done %v), want an error containing %q", err, done, test.want)
			}
			if response.Code != http.StatusBadRequest {
				t.Errorf("callback status = %d, want %d", response.Code, http.StatusBadRequest)
			}
			if _, statErr := os.Stat(session.credentialsLocation); !errors.Is(statErr, os.ErrNotExist) {
				t.Errorf("credentials were written after a failed login")
			}
		})
	}
}

func TestCallbackCompletesTheLoginOnlyOnce(t *testing.T) {
	session, _, ctx := newTestSession(t)
	target := callbackURL(session, url.Values{"code": {"test-code"}})

	serve(ctx, session, http.MethodGet, target)
	second := serve(ctx, session, http.MethodGet, target)

	if second.Code != http.StatusConflict {
		t.Errorf("second callback status = %d, want %d", second.Code, http.StatusConflict)
	}
}

func TestLoginServerOnlyServesLoginRoutes(t *testing.T) {
	session, _, ctx := newTestSession(t)

	if response := serve(ctx, session, http.MethodGet, "/favicon.ico"); response.Code != http.StatusNotFound {
		t.Errorf("GET /favicon.ico status = %d, want %d", response.Code, http.StatusNotFound)
	}
	if response := serve(ctx, session, http.MethodPost, "/"); response.Code != http.StatusMethodNotAllowed {
		t.Errorf("POST / status = %d, want %d", response.Code, http.StatusMethodNotAllowed)
	}
}

func TestListenLoopbackOnlyListensOnLocalhost(t *testing.T) {
	listener, err := listenLoopback(0)
	if err != nil {
		t.Fatal(err)
	}
	defer listener.Close()

	if ip := listener.Addr().(*net.TCPAddr).IP; !ip.Equal(net.IPv4(127, 0, 0, 1)) {
		t.Errorf("listening on %s, want 127.0.0.1", ip)
	}
}
