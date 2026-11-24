package internal

import (
	"context"
	"crypto/rand"
	"crypto/subtle"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"os"
	"sync/atomic"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"github.com/pkg/browser"
	"github.com/spf13/cobra"
	"golang.org/x/oauth2"
)

const loginTimeout = time.Minute

var loginCmd = &cobra.Command{
	Use:   "login",
	Short: "OAuth2 login.",
	Run:   run,
}

func init() {
	rootCmd.AddCommand(loginCmd)
}

func run(cmd *cobra.Command, args []string) {

	lhConfig := getGlobalConfig(cmd)

	if !lhConfig.OauthConfig.IsEnabled() {
		Fatal("OAuth configs not found")
	}

	if lhConfig.OauthConfig.IsClientCredentials() {
		Fatal("OAuth Client Credentials flow not allowed")
	}

	fmt.Println("Starting OAuth2 PKCE authorization flow")

	// https://github.com/coreos/go-oidc/blob/v3/example/idtoken/app.go
	ctx, err := lhConfig.OauthConfig.WithHTTPClient(requestContext(cmd))
	if err != nil {
		Fatal(err)
	}

	oauthProvider, err := oidc.NewProvider(ctx, lhConfig.OauthConfig.AuthServer)
	if err != nil {
		Fatal(err)
	}

	oauth2Config := oauth2.Config{
		ClientID:    lhConfig.OauthConfig.ClientId,
		RedirectURL: fmt.Sprintf("http://127.0.0.1:%d/callback", lhConfig.OauthConfig.CallbackPort),
		Endpoint:    oauthProvider.Endpoint(),
		Scopes:      []string{oidc.ScopeOpenID},
	}

	session, err := newLoginSession(oauthProvider, oauth2Config, lhConfig.OauthConfig.CredentialsLocation)
	if err != nil {
		Fatal(err)
	}

	listener, err := listenLoopback(lhConfig.OauthConfig.CallbackPort)
	if err != nil {
		Fatal(err)
	}
	localServer := &http.Server{
		Handler:           newLoginMux(ctx, session),
		ReadHeaderTimeout: 10 * time.Second,
	}
	serveErr := make(chan error, 1)
	go func() { serveErr <- localServer.Serve(listener) }()

	loginURL := fmt.Sprintf("http://127.0.0.1:%d/", lhConfig.OauthConfig.CallbackPort)
	fmt.Printf("Listening on %s\n", loginURL)
	if err := browser.OpenURL(loginURL); err != nil {
		fmt.Printf("Open %s in your browser to log in\n", loginURL)
	}

	select {
	case err = <-session.result:
	case err = <-serveErr:
	case <-time.After(loginTimeout):
		err = errors.New("timed out waiting for the login to finish")
	}

	// Lets the callback finish sending its page before the process exits.
	shutdownCtx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()
	localServer.Shutdown(shutdownCtx)

	if err != nil {
		Fatal(err)
	}
	fmt.Println("Login Successful!")
}

// listenLoopback listens only on 127.0.0.1, the host in the redirect URI.
func listenLoopback(port int32) (net.Listener, error) {
	return net.Listen("tcp", fmt.Sprintf("127.0.0.1:%d", port))
}

func newLoginMux(ctx context.Context, session *loginSession) *http.ServeMux {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /{$}", session.handleHome)
	mux.HandleFunc("GET /callback", session.handleCallback(ctx))
	return mux
}

func randString(nByte int) (string, error) {
	b := make([]byte, nByte)
	if _, err := io.ReadFull(rand.Reader, b); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(b), nil
}

// loginSession holds the values of one login. They stay in this process, so no
// other site or local service can read or set them.
type loginSession struct {
	provider            *oidc.Provider
	issuer              string
	issRequired         bool
	config              oauth2.Config
	credentialsLocation string
	state               string
	nonce               string
	verifier            string
	claimed             atomic.Bool
	result              chan error
}

func newLoginSession(provider *oidc.Provider, config oauth2.Config, credentialsLocation string) (*loginSession, error) {
	var discovery struct {
		Issuer      string `json:"issuer"`
		IssRequired bool   `json:"authorization_response_iss_parameter_supported"`
	}
	if err := provider.Claims(&discovery); err != nil {
		return nil, err
	}
	state, err := randString(16)
	if err != nil {
		return nil, err
	}
	nonce, err := randString(16)
	if err != nil {
		return nil, err
	}
	return &loginSession{
		provider:            provider,
		issuer:              discovery.Issuer,
		issRequired:         discovery.IssRequired,
		config:              config,
		credentialsLocation: credentialsLocation,
		state:               state,
		nonce:               nonce,
		verifier:            oauth2.GenerateVerifier(),
		result:              make(chan error, 1),
	}, nil
}

func (s *loginSession) handleHome(w http.ResponseWriter, r *http.Request) {
	http.Redirect(w, r, s.config.AuthCodeURL(s.state, oidc.Nonce(s.nonce), oauth2.S256ChallengeOption(s.verifier)), http.StatusFound)
}

// handleCallback answers requests that don't belong to this login with an error
// and keeps waiting. The first callback with the right state ends the login.
func (s *loginSession) handleCallback(ctx context.Context) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		query := r.URL.Query()
		if subtle.ConstantTimeCompare([]byte(query.Get("state")), []byte(s.state)) != 1 {
			http.Error(w, "Invalid login state", http.StatusBadRequest)
			return
		}
		if !s.claimed.CompareAndSwap(false, true) {
			http.Error(w, "This login has already finished", http.StatusConflict)
			return
		}

		err := s.completeLogin(ctx, query)
		if err != nil {
			http.Error(w, "Login failed. Check the terminal for details.", http.StatusBadRequest)
		} else {
			w.Write(loginSuccessPage)
		}
		s.result <- err
	}
}

func (s *loginSession) completeLogin(ctx context.Context, query url.Values) error {
	// RFC 9207: the response must come from this login's issuer, and must name it
	// if the provider's discovery says it always does.
	iss := query.Get("iss")
	if iss == "" && s.issRequired {
		return errors.New("login response has no iss parameter, but the provider always sends it")
	}
	if iss != "" && iss != s.issuer {
		return fmt.Errorf("login response came from issuer %q, expected %q", iss, s.issuer)
	}
	if loginError := query.Get("error"); loginError != "" {
		if description := query.Get("error_description"); description != "" {
			return fmt.Errorf("login failed: %s: %s", loginError, description)
		}
		return fmt.Errorf("login failed: %s", loginError)
	}
	code := query.Get("code")
	if code == "" {
		return errors.New("login response has no authorization code")
	}

	oauth2Token, err := s.config.Exchange(ctx, code, oauth2.VerifierOption(s.verifier))
	if err != nil {
		return fmt.Errorf("failed to exchange token: %w", err)
	}
	rawIDToken, ok := oauth2Token.Extra("id_token").(string)
	if !ok {
		return errors.New("no id_token field in oauth2 token")
	}

	idToken, err := s.provider.Verifier(&oidc.Config{ClientID: s.config.ClientID}).Verify(ctx, rawIDToken)
	if err != nil {
		return fmt.Errorf("failed to verify ID token: %w", err)
	}
	if subtle.ConstantTimeCompare([]byte(idToken.Nonce), []byte(s.nonce)) != 1 {
		return errors.New("nonce did not match")
	}
	// go-oidc fails when at_hash is missing, and providers may omit it.
	if idToken.AccessTokenHash != "" {
		if err := idToken.VerifyAccessToken(oauth2Token.AccessToken); err != nil {
			return fmt.Errorf("access token does not match the ID token: %w", err)
		}
	}

	data, err := json.MarshalIndent(oauth2Token, "", "    ")
	if err != nil {
		return err
	}
	return os.WriteFile(s.credentialsLocation, data, 0644)
}

var loginSuccessPage = []byte(`
		<!DOCTYPE html>
		<html>
			<head>
				<style>
					.title {
						margin: 25px;
						font-family: Arial, Helvetica, sans-serif;
						font-weight: bold;
					}
					.text {
						font-family: Arial, Helvetica, sans-serif;
					}
					img {
						width: 60px;
						float: left;
					}
				</style>
			</head>
			<body>
				<div><img src="https://littlehorse.io/img/logo.jpg"></div>
				<div class="title">LittleHorse</div>
				<p class="text">Login Successful!, You can close this window now.</p>
			</body>
		</html>
		`)

func Fatal(v ...any) {
	fmt.Fprint(os.Stderr, "Fatal: ")
	fmt.Fprintln(os.Stderr, v...)
	os.Exit(1)
}
