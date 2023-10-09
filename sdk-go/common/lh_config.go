package common

import (
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"os"
	"strconv"
	"strings"

	"github.com/google/uuid"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/auth"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/magiconair/properties"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/insecure"
)

const (
	API_HOST_KEY     = "LHC_API_HOST"
	API_PORT_KEY     = "LHC_API_PORT"
	API_PROTOCOL_KEY = "LHC_API_PROTOCOL"
	CLIENT_ID_KEY    = "LHC_CLIENT_ID"

	CERT_FILE_KEY    = "LHC_CLIENT_CERT"
	KEY_FILE_KEY     = "LHC_CLIENT_KEY"
	CA_CERT_FILE_KEY = "LHC_CA_CERT"

	NUM_WORKER_THREADS_KEY      = "LHW_NUM_WORKER_THREADS"
	TASK_WORKER_VERSION_KEY     = "LHW_TASK_WORKER_VERSION"
	SERVER_CONNECT_LISTENER_KEY = "LHW_SERVER_CONNECT_LISTENER"

	OAUTH_CLIENT_ID_KEY     = "LHC_OAUTH_CLIENT_ID"
	OAUTH_CLIENT_SECRET_KEY = "LHC_OAUTH_CLIENT_SECRET"
	OAUTH_SERVER_URL        = "LHC_OAUTH_SERVER_URL"
	OAUTH_ACCESS_TOKEN_URL  = "LHC_OAUTH_ACCESS_TOKEN_URL"

	OAUTH_CALLBACK_PORT_KEY        = "LHC_OAUTH_CALLBACK_PORT"
	OAUTH_CREDENTIALS_LOCATION_KEY = "LHC_OAUTH_CREDENTIALS_LOCATION"

	DEFAULT_LISTENER            = "PLAIN"
	DEFAULT_OAUTH_CALLBACK_PORT = 25242
	DEFAULT_PROTOCOL            = "PLAINTEXT"
	TLS_PROTOCOL                = "TLS"
)

type LHConfig struct {
	ApiHost     string
	ApiProtocol string
	ApiPort     string
	ClientId    string
	CertFile    *string
	KeyFile     *string
	CaCert      *string

	NumWorkerThreads      int32
	TaskWorkerVersion     string
	ServerConnectListener string

	clients  map[string]*model.LHPublicApiClient
	channels map[string]*grpc.ClientConn

	OauthConfig *auth.OauthConfig
}

func (config *LHConfig) GetGrpcConn(url string) (*grpc.ClientConn, error) {

	if config.channels[url] == nil {
		var opts []grpc.DialOption
		apiUrl := config.ApiHost + ":" + config.ApiPort

		if config.ApiProtocol == DEFAULT_PROTOCOL {
			opts = append(opts, grpc.WithTransportCredentials(insecure.NewCredentials()))
		} else {
			var transportCredentials credentials.TransportCredentials
			if config.CertFile == nil && config.KeyFile == nil {
				transportCredentials = loadTLS(config.CaCert)
			} else {
				transportCredentials = loadMTLS(config.CaCert, *config.CertFile, *config.KeyFile)
			}

			if config.OauthConfig.IsEnabled() {
				// https://github.com/grpc/grpc-go/blob/master/Documentation/grpc-auth-support.md
				opts = append(opts, grpc.WithPerRPCCredentials(config.OauthConfig.GetTokenSource()), grpc.WithTransportCredentials(transportCredentials))
			} else {
				opts = append(opts, grpc.WithTransportCredentials(transportCredentials))
			}
		}

		conn, err := grpc.Dial(apiUrl, opts...)
		if err != nil {
			return nil, err
		}
		config.channels[url] = conn
	}
	return config.channels[url], nil
}

func (l *LHConfig) GetGrpcClient() (*model.LHPublicApiClient, error) {
	url := l.ApiHost + ":" + l.ApiPort
	if l.ApiProtocol != DEFAULT_PROTOCOL && l.ApiProtocol != TLS_PROTOCOL {
		return nil, fmt.Errorf("invalid protocol: %s", l.ApiProtocol)
	}
	return l.GetGrpcClientForHost(url)
}

func (l *LHConfig) GetGrpcClientForHost(url string) (*model.LHPublicApiClient, error) {
	if l.clients[url] == nil {
		conn, err := l.GetGrpcConn(l.ApiHost + ":" + l.ApiPort)
		if err != nil {
			return nil, err
		}
		temp := model.NewLHPublicApiClient(conn)
		l.clients[url] = &temp
	}

	return l.clients[url], nil
}

func NewConfigFromEnv() *LHConfig {
	serverConnectListener := os.Getenv(SERVER_CONNECT_LISTENER_KEY)
	if serverConnectListener == "" {
		serverConnectListener = DEFAULT_LISTENER
	}

	return &LHConfig{
		ApiHost:     getEnvOrDefault(API_HOST_KEY, "localhost"),
		ApiProtocol: getEnvOrDefault(API_PROTOCOL_KEY, DEFAULT_PROTOCOL),
		ApiPort:     getEnvOrDefault(API_PORT_KEY, "2023"),
		ClientId:    getEnvOrDefault(CLIENT_ID_KEY, "client-"+generateRandomClientId()),

		CertFile: stringPtr(os.Getenv(CERT_FILE_KEY)),
		KeyFile:  stringPtr(os.Getenv(KEY_FILE_KEY)),
		CaCert:   stringPtr(os.Getenv(CA_CERT_FILE_KEY)),

		NumWorkerThreads:      int32FromEnv(NUM_WORKER_THREADS_KEY, 8),
		TaskWorkerVersion:     os.Getenv(TASK_WORKER_VERSION_KEY),
		ServerConnectListener: getEnvOrDefault(SERVER_CONNECT_LISTENER_KEY, DEFAULT_LISTENER),

		clients:  make(map[string]*model.LHPublicApiClient),
		channels: make(map[string]*grpc.ClientConn),

		OauthConfig: &auth.OauthConfig{
			ClientId:            getEnvOrDefault(OAUTH_CLIENT_ID_KEY, ""),
			ClientSecret:        getEnvOrDefault(OAUTH_CLIENT_SECRET_KEY, ""),
			AuthServer:          getEnvOrDefault(OAUTH_SERVER_URL, ""),
			TokenEndpointUrl:    getEnvOrDefault(OAUTH_ACCESS_TOKEN_URL, ""),
			CallbackPort:        int32FromEnv(OAUTH_CALLBACK_PORT_KEY, DEFAULT_OAUTH_CALLBACK_PORT),
			CredentialsLocation: getEnvOrDefault(OAUTH_CREDENTIALS_LOCATION_KEY, homeDir("/.config/littlehorse.credentials")),
		},
	}
}

func NewConfigFromProps(filePath string) (*LHConfig, error) {
	p, err := properties.LoadFile(filePath, properties.UTF8)
	if err != nil {
		return nil, err
	}

	return &LHConfig{
		ApiHost:     p.GetString(API_HOST_KEY, "localhost"),
		ApiProtocol: p.GetString(API_PROTOCOL_KEY, DEFAULT_PROTOCOL),
		ApiPort:     p.GetString(API_PORT_KEY, "2023"),
		ClientId:    p.GetString(CLIENT_ID_KEY, "client-"+generateRandomClientId()),

		CertFile: stringPtr(p.GetString(CERT_FILE_KEY, "")),
		KeyFile:  stringPtr(p.GetString(KEY_FILE_KEY, "")),
		CaCert:   stringPtr(p.GetString(CA_CERT_FILE_KEY, "")),

		NumWorkerThreads:      int32FromProp(p, NUM_WORKER_THREADS_KEY, 8),
		TaskWorkerVersion:     p.GetString(TASK_WORKER_VERSION_KEY, ""),
		ServerConnectListener: p.GetString(SERVER_CONNECT_LISTENER_KEY, DEFAULT_LISTENER),

		clients:  make(map[string]*model.LHPublicApiClient),
		channels: make(map[string]*grpc.ClientConn),

		OauthConfig: &auth.OauthConfig{
			ClientId:            p.GetString(OAUTH_CLIENT_ID_KEY, ""),
			ClientSecret:        p.GetString(OAUTH_CLIENT_SECRET_KEY, ""),
			AuthServer:          p.GetString(OAUTH_SERVER_URL, ""),
			TokenEndpointUrl:    p.GetString(OAUTH_ACCESS_TOKEN_URL, ""),
			CallbackPort:        int32FromProp(p, NUM_WORKER_THREADS_KEY, DEFAULT_OAUTH_CALLBACK_PORT),
			CredentialsLocation: p.GetString(OAUTH_CREDENTIALS_LOCATION_KEY, homeDir("/.config/littlehorse.credentials")),
		},
	}, nil
}

func generateRandomClientId() string {
	uuid := uuid.NewString()
	return strings.ReplaceAll(uuid, "-", "")
}

func homeDir(append string) string {
	dirname, err := os.UserHomeDir()
	if err != nil {
		panic("can't reach home dir")
	}
	return dirname + append
}

func stringPtr(s string) *string {
	if s == "" {
		return nil
	}
	return &s
}

func int32FromEnv(key string, defaultVal int32) int32 {
	val, err := strconv.Atoi(os.Getenv(key))
	if err != nil {
		return defaultVal
	}
	return int32(val)
}

func int32FromProp(p *properties.Properties, key string, defaultVal int32) int32 {
	val := p.GetInt(key, int(defaultVal))
	return int32(val)
}

func getEnvOrDefault(key, defaultVal string) string {
	out := os.Getenv(key)
	if out == "" {
		return defaultVal
	}
	return out
}

func loadTLS(caCertFileName *string) credentials.TransportCredentials {
	tlsConfig := &tls.Config{}

	if caCertFileName != nil {
		capool := x509.NewCertPool()
		ca, err := os.ReadFile(*caCertFileName)
		if err != nil {
			panic("can't read ca file")
		}

		if !capool.AppendCertsFromPEM(ca) {
			panic("invalid CA file")
		}

		tlsConfig = &tls.Config{
			RootCAs: capool,
		}
	}

	return credentials.NewTLS(tlsConfig)
}

func loadMTLS(caCertFileName *string, certFileName string, keyFileName string) credentials.TransportCredentials {
	certificate, err := tls.LoadX509KeyPair(certFileName, keyFileName)
	if err != nil {
		panic("Load client certification failed: " + err.Error())
	}

	tlsConfig := &tls.Config{Certificates: []tls.Certificate{certificate}}

	if caCertFileName != nil {
		capool := x509.NewCertPool()
		ca, err := os.ReadFile(*caCertFileName)
		if err != nil {
			panic("can't read ca file")
		}

		if !capool.AppendCertsFromPEM(ca) {
			panic("invalid CA file")
		}

		tlsConfig = &tls.Config{
			Certificates: []tls.Certificate{certificate},
			RootCAs:      capool,
		}
	}

	return credentials.NewTLS(tlsConfig)
}
