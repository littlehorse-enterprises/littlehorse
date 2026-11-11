package littlehorse

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
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
	TokenEndpointUrl    string
	CallbackPort        int32
	CredentialsLocation string
	CaCert              string
}

func (oauthConfig *OauthConfig) IsEnabled() bool {
	return oauthConfig.DeduceFlow() != Undefined
}

// Implicit configurations instead of explicitly set the desired flow
// ClientCredentials needs ClientId and ClientSecret
// AuthorizationCode only needs ClientId
// AuthServer is always mandatory
func (oauthConfig *OauthConfig) DeduceFlow() OauthFlow {
	if oauthConfig.ClientId != "" && oauthConfig.TokenEndpointUrl != "" && oauthConfig.ClientSecret != "" {
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

// WithHTTPClient returns a context whose HTTP calls to the authorization server also trust CaCert.
func (oauthConfig *OauthConfig) WithHTTPClient(ctx context.Context) (context.Context, error) {
	client, err := oauthConfig.httpClient()
	if err != nil || client == nil {
		return ctx, err
	}
	return oidc.ClientContext(ctx, client), nil
}

// httpClient returns nil when no CaCert is set, so the default client is used.
func (oauthConfig *OauthConfig) httpClient() (*http.Client, error) {
	if oauthConfig.CaCert == "" {
		return nil, nil
	}

	ca, err := os.ReadFile(oauthConfig.CaCert)
	if err != nil {
		return nil, fmt.Errorf("can't read %s: %w", OAUTH_CA_CERT_KEY, err)
	}

	pool, err := x509.SystemCertPool()
	if err != nil {
		pool = x509.NewCertPool()
	}
	if !pool.AppendCertsFromPEM(ca) {
		return nil, fmt.Errorf("invalid %s: no PEM certificates found in %s", OAUTH_CA_CERT_KEY, oauthConfig.CaCert)
	}

	transport := http.DefaultTransport.(*http.Transport).Clone()
	transport.TLSClientConfig = &tls.Config{RootCAs: pool, MinVersion: tls.VersionTLS12}
	return &http.Client{Transport: transport}, nil
}

type FileTokenSource struct {
	CredentialsLocation string
	OauthConfig         oauth2.Config
	httpClient          *http.Client
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
		if fileTokenSource.httpClient != nil {
			ctx = oidc.ClientContext(ctx, fileTokenSource.httpClient)
		}
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
	client, err := oauthConfig.httpClient()
	if err != nil {
		log.Fatal(err)
	}
	ctx := context.TODO()
	if client != nil {
		ctx = oidc.ClientContext(ctx, client)
	}

	switch oauthConfig.DeduceFlow() {
	case AuthorizationCode:
		provider, err := oidc.NewProvider(ctx, oauthConfig.AuthServer)
		if err != nil {
			log.Fatal(err)
		}
		oauth2Config := oauth2.Config{
			ClientID: oauthConfig.ClientId,
			Endpoint: provider.Endpoint(),
			Scopes:   []string{oidc.ScopeOpenID},
		}
		return FileTokenSource{CredentialsLocation: oauthConfig.CredentialsLocation, OauthConfig: oauth2Config, httpClient: client}
	default:
		config := &clientcredentials.Config{
			ClientID:     oauthConfig.ClientId,
			ClientSecret: oauthConfig.ClientSecret,
			TokenURL:     oauthConfig.TokenEndpointUrl,
		}
		return oauth.TokenSource{TokenSource: oauth2.ReuseTokenSource(nil, config.TokenSource(ctx))}
	}
}
