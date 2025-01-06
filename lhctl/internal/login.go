package internal

import (
	"context"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/coreos/go-oidc/v3/oidc"
	"github.com/pkg/browser"
	"github.com/spf13/cobra"
	"golang.org/x/oauth2"
)

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
	ctx := requestContext(cmd)

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

	mux := http.NewServeMux()
	localServer := &http.Server{
		Addr:    fmt.Sprintf(":%d", lhConfig.OauthConfig.CallbackPort),
		Handler: mux,
	}

	mux.HandleFunc("/", handleHomeRequest(oauth2Config))
	mux.HandleFunc("/callback", handleCallbackRequest(ctx, localServer, &lhConfig, oauthProvider, oauth2Config))

	time.AfterFunc(time.Minute, func() {
		Fatal("Timeout")
	})

	time.AfterFunc(time.Second, func() {
		browser.OpenURL(fmt.Sprintf("http://127.0.0.1:%d/", lhConfig.OauthConfig.CallbackPort))
	})

	fmt.Printf("Listening on http://127.0.0.1:%d/\n", lhConfig.OauthConfig.CallbackPort)

	err = localServer.ListenAndServe()
	if err != nil && err != http.ErrServerClosed {
		Fatal(err)
	}
	fmt.Println("Login Successful!")
}

func randString(nByte int) (string, error) {
	b := make([]byte, nByte)
	if _, err := io.ReadFull(rand.Reader, b); err != nil {
		return "", err
	}
	return base64.RawURLEncoding.EncodeToString(b), nil
}

func setCallbackCookie(w http.ResponseWriter, r *http.Request, name, value string) {
	c := &http.Cookie{
		Name:     name,
		Value:    value,
		MaxAge:   int(time.Hour.Seconds()),
		Secure:   r.TLS != nil,
		HttpOnly: true,
	}
	http.SetCookie(w, c)
}

func handleHomeRequest(oauth2Config oauth2.Config) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		state, err := randString(16)
		if err != nil {
			http.Error(w, "Internal error", http.StatusInternalServerError)
			return
		}
		nonce, err := randString(16)
		if err != nil {
			http.Error(w, "Internal error", http.StatusInternalServerError)
			return
		}
		setCallbackCookie(w, r, "state", state)
		setCallbackCookie(w, r, "nonce", nonce)
		http.Redirect(w, r, oauth2Config.AuthCodeURL(state, oidc.Nonce(nonce)), http.StatusFound)
	}
}

func handleCallbackRequest(ctx context.Context, localServer *http.Server, lhConfig *littlehorse.LHConfig, oauthProvider *oidc.Provider, oauth2Config oauth2.Config) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		state, err := r.Cookie("state")
		if err != nil {
			Fatal("State not found")
		}
		if r.URL.Query().Get("state") != state.Value {
			Fatal("State did not match")
		}

		oauth2Token, err := oauth2Config.Exchange(ctx, r.URL.Query().Get("code"))
		if err != nil {
			Fatal("Failed to exchange token: " + err.Error())
		}
		rawIDToken, ok := oauth2Token.Extra("id_token").(string)
		if !ok {
			Fatal("No id_token field in oauth2 token.")
		}

		oidcConfig := &oidc.Config{
			ClientID: oauth2Config.ClientID,
		}
		verifier := oauthProvider.Verifier(oidcConfig)
		idToken, err := verifier.Verify(ctx, rawIDToken)
		if err != nil {
			Fatal("Failed to verify ID Token: " + err.Error())
		}

		nonce, err := r.Cookie("nonce")
		if err != nil {
			Fatal("nonce not found")
		}
		if idToken.Nonce != nonce.Value {
			Fatal("nonce did not match")
		}

		data, err := json.MarshalIndent(oauth2Token, "", "    ")
		if err != nil {
			Fatal(err.Error())
		}

		err = os.WriteFile(lhConfig.OauthConfig.CredentialsLocation, data, 0644)

		if err != nil {
			Fatal(err.Error())
		}

		javascript := []byte(`
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
		w.Write(javascript)

		time.AfterFunc(time.Second, func() {
			localServer.Close()
		})
	}
}

func Fatal(v ...any) {
	fmt.Fprint(os.Stderr, "Fatal: ")
	fmt.Fprintln(os.Stderr, v...)
	os.Exit(1)
}
