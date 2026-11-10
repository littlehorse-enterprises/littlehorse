package internal

import (
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"

	"golang.org/x/oauth2"
)

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
