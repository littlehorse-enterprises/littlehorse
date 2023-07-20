package auth

import (
	"context"
	"encoding/json"
	"log"
	"os"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/clientcredentials"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/oauth"
)

type OauthFlow int64

const (
	AuthorizationCode OauthFlow = iota
	ClientCredentials
	Undefined
)

type OauthConfig struct {
	ClientId            string
	ClientSecret        string
	AuthServer          string
	CallbackPort        int32
	CredentialsLocation string
}

func (oauthConfig *OauthConfig) IsEnabled() bool {
	if oauthConfig.ClientId != "" && oauthConfig.AuthServer != "" {
		return true
	}
	return false
}

// Implicit configurations instead of explicitly set the desired flow
// ClientCredentials needs ClientId and ClientSecret
// AuthorizationCode only needs ClientId
// AuthServer is always mandatory
func (oauthConfig *OauthConfig) DeduceFlow() OauthFlow {
	if oauthConfig.ClientId != "" && oauthConfig.AuthServer != "" && oauthConfig.ClientSecret != "" {
		return ClientCredentials
	}

	if oauthConfig.ClientId != "" && oauthConfig.AuthServer != "" {
		return AuthorizationCode
	}

	return Undefined
}

func (oauthConfig *OauthConfig) IsClientCredentials() bool {
	return oauthConfig.DeduceFlow() == ClientCredentials
}

func (oauthConfig *OauthConfig) IsAuthorizationCode() bool {
	return oauthConfig.DeduceFlow() == AuthorizationCode
}

type FileTokenSource struct {
	CredentialsLocation string
	OauthConfig         oauth2.Config
}

// GetRequestMetadata gets the request metadata as a map from a TokenSource.
func (fileTokenSource FileTokenSource) GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error) {
	byteValue, err := os.ReadFile(fileTokenSource.CredentialsLocation)
	if err != nil {
		log.Fatal("OAuth2 is enabled but it was not possible to load the credentials: ", err, ". Run 'lhctl login'.")
	}

	token := &oauth2.Token{}
	err = json.Unmarshal(byteValue, &token)
	if err != nil {
		log.Fatal(err)
	}

	if token.Expiry.Before(time.Now()) {
		token, err = fileTokenSource.OauthConfig.TokenSource(ctx, token).Token()
		if err != nil {
			log.Fatal(err)
		}

		data, err := json.MarshalIndent(token, "", "    ")
		if err != nil {
			log.Fatal(err)
		}

		err = os.WriteFile(fileTokenSource.CredentialsLocation, data, 0644)

		if err != nil {
			log.Fatal(err)
		}
	}

	return map[string]string{
		"authorization": token.Type() + " " + token.AccessToken,
	}, nil
}

// RequireTransportSecurity indicates whether the credentials requires transport security.
func (fileTokenSource FileTokenSource) RequireTransportSecurity() bool {
	return true
}

// Returns an object that implements:
//
//	type PerRPCCredentials interface {
//			GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error)
//			RequireTransportSecurity() bool
//	}
func (oauthConfig *OauthConfig) GetTokenSource() credentials.PerRPCCredentials {
	provider, err := oidc.NewProvider(context.TODO(), oauthConfig.AuthServer)
	if err != nil {
		log.Fatal(err)
	}

	switch oauthConfig.DeduceFlow() {
	case AuthorizationCode:
		oauth2Config := oauth2.Config{
			ClientID: oauthConfig.ClientId,
			Endpoint: provider.Endpoint(),
			Scopes:   []string{oidc.ScopeOpenID},
		}
		return FileTokenSource{CredentialsLocation: oauthConfig.CredentialsLocation, OauthConfig: oauth2Config}
	default:
		config := &clientcredentials.Config{
			ClientID:     oauthConfig.ClientId,
			ClientSecret: oauthConfig.ClientSecret,
			Scopes:       []string{oidc.ScopeOpenID},
			TokenURL:     provider.Endpoint().TokenURL,
		}
		return oauth.TokenSource{TokenSource: oauth2.ReuseTokenSource(nil, config.TokenSource(context.TODO()))}
	}
}
