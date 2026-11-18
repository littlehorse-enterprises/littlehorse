//! Configurable gRPC transport with per-call authentication and OAuth refresh.
use crate::{
    auth::{OAuthConfig, TokenProvider},
    proto::little_horse_client::LittleHorseClient,
};
use std::{
    collections::HashMap,
    future::Future,
    pin::Pin,
    task::{Context, Poll},
    time::Duration,
};
use tonic::{
    body::Body,
    transport::{Channel, ClientTlsConfig, Endpoint},
};
use tower::Service;
pub type ClientError = Box<dyn std::error::Error + Send + Sync>;

#[derive(Clone)]
pub struct ClientConfig {
    pub endpoint: String,
    pub tenant_id: Option<String>,
    pub bearer_token: Option<String>,
    pub oauth: Option<OAuthConfig>,
    pub tls: Option<ClientTlsConfig>,
    pub connect_timeout: Duration,
    pub request_timeout: Duration,
    pub keepalive_interval: Duration,
    pub keepalive_timeout: Duration,
    pub inflight_tasks: usize,
}
impl Default for ClientConfig {
    fn default() -> Self {
        Self {
            endpoint: "http://localhost:2023".into(),
            tenant_id: None,
            bearer_token: None,
            oauth: None,
            tls: None,
            connect_timeout: Duration::from_secs(10),
            request_timeout: Duration::from_secs(30),
            keepalive_interval: Duration::from_secs(30),
            keepalive_timeout: Duration::from_secs(10),
            inflight_tasks: 8,
        }
    }
}
impl std::fmt::Debug for ClientConfig {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        formatter
            .debug_struct("ClientConfig")
            .field("endpoint", &self.endpoint)
            .field("tenant_id", &self.tenant_id)
            .field(
                "bearer_token",
                &self.bearer_token.as_ref().map(|_| "[redacted]"),
            )
            .field("oauth", &self.oauth)
            .finish_non_exhaustive()
    }
}
impl ClientConfig {
    pub fn from_env() -> Result<Self, ClientError> {
        Self::from_properties(std::env::vars().collect())
    }
    /// Read Java-style key=value / key:value properties; environment values override the file.
    pub fn from_file(path: impl AsRef<std::path::Path>) -> Result<Self, ClientError> {
        let text = std::fs::read_to_string(path)?;
        let mut properties = HashMap::new();
        for line in text
            .lines()
            .map(str::trim)
            .filter(|line| !line.is_empty() && !line.starts_with(['#', '!']))
        {
            let (key, value) = line
                .split_once(['=', ':'])
                .ok_or("expected key=value property")?;
            properties.insert(key.trim().into(), value.trim().into());
        }
        properties.extend(std::env::vars().filter(|(key, _)| key.starts_with("LHC_")));
        Self::from_properties(properties)
    }
    /// Build from explicit settings without consulting process-global state.
    pub fn from_properties(properties: HashMap<String, String>) -> Result<Self, ClientError> {
        let get = |key: &str| properties.get(key).cloned();
        let host = get("LHC_API_HOST").unwrap_or_else(|| "localhost".into());
        let port = get("LHC_API_PORT")
            .unwrap_or_else(|| "2023".into())
            .parse::<u16>()?;
        let encrypted = match get("LHC_API_PROTOCOL").as_deref().unwrap_or("PLAINTEXT") {
            "PLAINTEXT" => false,
            "TLS" => true,
            _ => return Err("LHC_API_PROTOCOL must be PLAINTEXT or TLS".into()),
        };
        let mut config = Self {
            endpoint: format!(
                "{}://{host}:{port}",
                if encrypted { "https" } else { "http" }
            ),
            tenant_id: get("LHC_TENANT_ID"),
            bearer_token: get("LHC_BEARER_TOKEN"),
            ..Default::default()
        };
        if let Some(value) = get("LHC_GRPC_KEEPALIVE_TIME_MS") {
            config.keepalive_interval = Duration::from_millis(value.parse()?);
        }
        if let Some(value) = get("LHC_GRPC_KEEPALIVE_TIMEOUT_MS") {
            config.keepalive_timeout = Duration::from_millis(value.parse()?);
        }
        if let Some(value) = get("LHC_INFLIGHT_TASKS") {
            config.inflight_tasks = value.parse()?;
        }
        if config.inflight_tasks == 0 {
            return Err("LHC_INFLIGHT_TASKS must be positive".into());
        }
        if encrypted {
            let mut tls = ClientTlsConfig::new().with_native_roots();
            if let Some(path) = get("LHC_CA_CERT") {
                tls = tls.ca_certificate(tonic::transport::Certificate::from_pem(std::fs::read(
                    path,
                )?));
            }
            match (get("LHC_CLIENT_CERT"), get("LHC_CLIENT_KEY")) {
                (Some(cert), Some(key)) => {
                    tls = tls.identity(tonic::transport::Identity::from_pem(
                        std::fs::read(cert)?,
                        std::fs::read(key)?,
                    ))
                }
                (None, None) => {}
                _ => return Err("both LHC_CLIENT_CERT and LHC_CLIENT_KEY must be provided".into()),
            }
            config.tls = Some(tls);
        }
        match (
            get("LHC_OAUTH_CLIENT_ID"),
            get("LHC_OAUTH_CLIENT_SECRET"),
            get("LHC_OAUTH_ACCESS_TOKEN_URL"),
        ) {
            (Some(client_id), Some(client_secret), Some(token_url)) => {
                config.oauth = Some(OAuthConfig {
                    client_id,
                    client_secret,
                    token_url,
                    scope: get("LHC_OAUTH_SCOPE"),
                })
            }
            (None, None, None) => {}
            _ => {
                return Err("OAuth client id, secret, and token URL must all be configured".into());
            }
        }
        Ok(config)
    }
    pub async fn connect(self) -> Result<Client, ClientError> {
        if self.oauth.is_some() && self.bearer_token.is_some() {
            return Err("configure OAuth or a static bearer token, not both".into());
        }
        let mut endpoint = Endpoint::from_shared(self.endpoint.clone())?
            .connect_timeout(self.connect_timeout)
            .timeout(self.request_timeout)
            .http2_keep_alive_interval(self.keepalive_interval)
            .keep_alive_timeout(self.keepalive_timeout);
        if let Some(tls) = self.tls {
            endpoint = endpoint.tls_config(tls)?;
        } else if self.endpoint.starts_with("https://") {
            endpoint = endpoint.tls_config(ClientTlsConfig::new().with_native_roots())?;
        }
        let mut bearer = self
            .bearer_token
            .map(|value| format!("Bearer {value}").parse::<http::HeaderValue>())
            .transpose()?;
        if let Some(header) = &mut bearer {
            header.set_sensitive(true);
        }
        Ok(LittleHorseClient::new(AuthChannel {
            channel: endpoint.connect().await?,
            tenant: self.tenant_id.map(|value| value.parse()).transpose()?,
            bearer,
            oauth: self.oauth.map(TokenProvider::new).transpose()?,
        }))
    }
    /// Preserve TLS, tenant, and credentials while targeting a host advertised by registration.
    pub fn for_host(&self, host: &crate::proto::LhHostInfo) -> Result<Self, ClientError> {
        let port = u16::try_from(host.port)?;
        let mut config = self.clone();
        let scheme = if self.endpoint.starts_with("https://") {
            "https"
        } else {
            "http"
        };
        let address = if host.host.contains(':') && !host.host.starts_with('[') {
            format!("[{}]", host.host)
        } else {
            host.host.clone()
        };
        config.endpoint = format!("{scheme}://{address}:{port}");
        Ok(config)
    }
}
#[derive(Clone)]
pub struct AuthChannel {
    channel: Channel,
    tenant: Option<http::HeaderValue>,
    bearer: Option<http::HeaderValue>,
    oauth: Option<TokenProvider>,
}
impl Service<http::Request<Body>> for AuthChannel {
    type Response = http::Response<Body>;
    type Error = ClientError;
    type Future = Pin<Box<dyn Future<Output = Result<Self::Response, Self::Error>> + Send>>;
    fn poll_ready(&mut self, context: &mut Context<'_>) -> Poll<Result<(), Self::Error>> {
        self.channel.poll_ready(context).map_err(Into::into)
    }
    fn call(&mut self, mut request: http::Request<Body>) -> Self::Future {
        let replacement = self.channel.clone();
        let mut channel = std::mem::replace(&mut self.channel, replacement);
        let tenant = self.tenant.clone();
        let bearer = self.bearer.clone();
        let oauth = self.oauth.clone();
        Box::pin(async move {
            if let Some(tenant) = tenant {
                request.headers_mut().insert("tenantid", tenant);
            }
            let authorization = match oauth {
                Some(provider) => Some(provider.header().await?),
                None => bearer,
            };
            if let Some(authorization) = authorization {
                request
                    .headers_mut()
                    .insert(http::header::AUTHORIZATION, authorization);
            }
            Ok(channel.call(request).await?)
        })
    }
}
pub type Client = LittleHorseClient<AuthChannel>;
