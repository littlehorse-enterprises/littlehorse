package littlehorse

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/magiconair/properties"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/keepalive"
)

const (
	API_HOST_KEY     = "LHC_API_HOST"
	API_PORT_KEY     = "LHC_API_PORT"
	API_PROTOCOL_KEY = "LHC_API_PROTOCOL"

	TENANT_ID_HEADER = "tenantid"
	TENANT_ID_KEY    = "LHC_TENANT_ID"

	CERT_FILE_KEY    = "LHC_CLIENT_CERT"
	KEY_FILE_KEY     = "LHC_CLIENT_KEY"
	CA_CERT_FILE_KEY = "LHC_CA_CERT"

	NUM_WORKER_THREADS_KEY  = "LHW_NUM_WORKER_THREADS"
	TASK_WORKER_VERSION_KEY = "LHW_TASK_WORKER_VERSION"
	TASK_WORKER_ID_KEY      = "LHW_TASK_WORKER_ID"

	OAUTH_CLIENT_ID_KEY     = "LHC_OAUTH_CLIENT_ID"
	OAUTH_CLIENT_SECRET_KEY = "LHC_OAUTH_CLIENT_SECRET"
	OAUTH_SERVER_URL        = "LHC_OAUTH_SERVER_URL"
	OAUTH_ACCESS_TOKEN_URL  = "LHC_OAUTH_ACCESS_TOKEN_URL"

	OAUTH_CALLBACK_PORT_KEY        = "LHC_OAUTH_CALLBACK_PORT"
	OAUTH_CREDENTIALS_LOCATION_KEY = "LHC_OAUTH_CREDENTIALS_LOCATION"

	GRPC_KEEPALIVE_TIME_KEY    = "LHC_GRPC_KEEPALIVE_TIME_MS"
	GRPC_KEEPALIVE_TIMEOUT_KEY = "LHC_GRPC_KEEPALIVE_TIMEOUT_MS"

	DEFAULT_OAUTH_CALLBACK_PORT = 25242
	DEFAULT_PROTOCOL            = "PLAINTEXT"
	TLS_PROTOCOL                = "TLS"
)

type LHConfig struct {
	ApiHost      string
	ApiProtocol  string
	ApiPort      string
	TaskWorkerId string
	CertFile     *string
	KeyFile      *string
	CaCert       *string
	TenantId     *string

	NumWorkerThreads  int32
	TaskWorkerVersion string

	GrpcKeepaliveTimeMs    int64
	GrpcKeepaliveTimeoutMs int64

	clients  map[string]*lhproto.LittleHorseClient
	channels map[string]*grpc.ClientConn

	OauthConfig *OauthConfig
}

func (config *LHConfig) GetGrpcConn(url string) (*grpc.ClientConn, error) {

	if config.channels[url] == nil {
		var opts []grpc.DialOption
		apiUrl := config.ApiHost + ":" + config.ApiPort

		opts = append(opts, grpc.WithKeepaliveParams(
			keepalive.ClientParameters{
				Time:                time.Duration(config.GrpcKeepaliveTimeMs) * time.Millisecond,
				Timeout:             time.Duration(config.GrpcKeepaliveTimeoutMs) * time.Millisecond,
				PermitWithoutStream: true,
			},
		))
		opts = append(opts, grpc.WithPerRPCCredentials(&tenantIdHeaderCreds{TenantId: config.TenantId}))

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

func (l *LHConfig) GetGrpcClient() (*lhproto.LittleHorseClient, error) {
	url := l.ApiHost + ":" + l.ApiPort
	if l.ApiProtocol != DEFAULT_PROTOCOL && l.ApiProtocol != TLS_PROTOCOL {
		return nil, fmt.Errorf("invalid protocol: %s", l.ApiProtocol)
	}
	return l.GetGrpcClientForHost(url)
}

func (l *LHConfig) GetGrpcClientForHost(url string) (*lhproto.LittleHorseClient, error) {
	if l.clients[url] == nil {
		conn, err := l.GetGrpcConn(l.ApiHost + ":" + l.ApiPort)
		if err != nil {
			return nil, err
		}
		temp := lhproto.NewLittleHorseClient(conn)
		l.clients[url] = &temp
	}

	return l.clients[url], nil
}

func NewConfigFromEnv() *LHConfig {

	return &LHConfig{
		ApiHost:      getEnvOrDefault(API_HOST_KEY, "localhost"),
		ApiProtocol:  getEnvOrDefault(API_PROTOCOL_KEY, DEFAULT_PROTOCOL),
		ApiPort:      getEnvOrDefault(API_PORT_KEY, "2023"),
		TaskWorkerId: getEnvOrDefault(TASK_WORKER_ID_KEY, "worker-"+generateRandomWorkerId()),

		CertFile: stringPtr(os.Getenv(CERT_FILE_KEY)),
		KeyFile:  stringPtr(os.Getenv(KEY_FILE_KEY)),
		CaCert:   stringPtr(os.Getenv(CA_CERT_FILE_KEY)),
		TenantId: stringPtr(os.Getenv(TENANT_ID_KEY)),

		NumWorkerThreads:  int32FromEnv(NUM_WORKER_THREADS_KEY, 8),
		TaskWorkerVersion: os.Getenv(TASK_WORKER_VERSION_KEY),

		GrpcKeepaliveTimeMs:    int64FromEnv(GRPC_KEEPALIVE_TIME_KEY, 45000),
		GrpcKeepaliveTimeoutMs: int64FromEnv(GRPC_KEEPALIVE_TIMEOUT_KEY, 5000),

		clients:  make(map[string]*lhproto.LittleHorseClient),
		channels: make(map[string]*grpc.ClientConn),

		OauthConfig: &OauthConfig{
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
		ApiHost:      p.GetString(API_HOST_KEY, "localhost"),
		ApiProtocol:  p.GetString(API_PROTOCOL_KEY, DEFAULT_PROTOCOL),
		ApiPort:      p.GetString(API_PORT_KEY, "2023"),
		TaskWorkerId: p.GetString(TASK_WORKER_ID_KEY, "worker-"+generateRandomWorkerId()),

		CertFile: stringPtr(p.GetString(CERT_FILE_KEY, "")),
		KeyFile:  stringPtr(p.GetString(KEY_FILE_KEY, "")),
		CaCert:   stringPtr(p.GetString(CA_CERT_FILE_KEY, "")),
		TenantId: stringPtr(p.GetString(TENANT_ID_KEY, "")),

		NumWorkerThreads:  int32FromProp(p, NUM_WORKER_THREADS_KEY, 8),
		TaskWorkerVersion: p.GetString(TASK_WORKER_VERSION_KEY, ""),

		GrpcKeepaliveTimeMs:    int64FromProp(p, GRPC_KEEPALIVE_TIME_KEY, 45000),
		GrpcKeepaliveTimeoutMs: int64FromProp(p, GRPC_KEEPALIVE_TIMEOUT_KEY, 5000),

		clients:  make(map[string]*lhproto.LittleHorseClient),
		channels: make(map[string]*grpc.ClientConn),

		OauthConfig: &OauthConfig{
			ClientId:            p.GetString(OAUTH_CLIENT_ID_KEY, ""),
			ClientSecret:        p.GetString(OAUTH_CLIENT_SECRET_KEY, ""),
			AuthServer:          p.GetString(OAUTH_SERVER_URL, ""),
			TokenEndpointUrl:    p.GetString(OAUTH_ACCESS_TOKEN_URL, ""),
			CallbackPort:        int32FromProp(p, NUM_WORKER_THREADS_KEY, DEFAULT_OAUTH_CALLBACK_PORT),
			CredentialsLocation: p.GetString(OAUTH_CREDENTIALS_LOCATION_KEY, homeDir("/.config/littlehorse.credentials")),
		},
	}, nil
}

func generateRandomWorkerId() string {
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

func int64FromEnv(key string, defaultVal int64) int64 {
	val, err := strconv.Atoi(os.Getenv(key))
	if err != nil {
		return defaultVal
	}
	return int64(val)
}

func int32FromEnv(key string, defaultVal int32) int32 {
	val, err := strconv.Atoi(os.Getenv(key))
	if err != nil {
		return defaultVal
	}
	return int32(val)
}

func int64FromProp(p *properties.Properties, key string, defaultVal int64) int64 {
	val := p.GetInt64(key, int64(defaultVal))
	return int64(val)
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

type tenantIdHeaderCreds struct {
	TenantId *string
}

func (c *tenantIdHeaderCreds) GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error) {
	if c == nil || c.TenantId == nil {
		return map[string]string{}, nil
	}
	return map[string]string{TENANT_ID_HEADER: *c.TenantId}, nil
}

func (c *tenantIdHeaderCreds) RequireTransportSecurity() bool {
	return false
}
