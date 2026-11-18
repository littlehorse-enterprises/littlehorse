//! OAuth client credentials with synchronized, demand-driven token refresh.
use std::{
    sync::Arc,
    time::{Duration, Instant},
};
use tokio::sync::Mutex;

#[derive(Clone)]
pub struct OAuthConfig {
    pub token_url: String,
    pub client_id: String,
    pub client_secret: String,
    pub scope: Option<String>,
}
impl std::fmt::Debug for OAuthConfig {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        formatter
            .debug_struct("OAuthConfig")
            .field("token_url", &self.token_url)
            .field("client_id", &self.client_id)
            .field("client_secret", &"[redacted]")
            .finish_non_exhaustive()
    }
}
struct Token {
    header: http::HeaderValue,
    expires: Instant,
}
#[derive(Clone)]
pub(crate) struct TokenProvider {
    config: OAuthConfig,
    http: reqwest::Client,
    token: Arc<Mutex<Option<Token>>>,
}
impl TokenProvider {
    pub fn new(config: OAuthConfig) -> Result<Self, reqwest::Error> {
        Ok(Self {
            config,
            http: reqwest::Client::builder()
                .timeout(Duration::from_secs(15))
                .redirect(reqwest::redirect::Policy::none())
                .build()?,
            token: Arc::new(Mutex::new(None)),
        })
    }
    pub async fn header(
        &self,
    ) -> Result<http::HeaderValue, Box<dyn std::error::Error + Send + Sync>> {
        let mut token = self.token.lock().await;
        if let Some(token) = token
            .as_ref()
            .filter(|token| token.expires > Instant::now())
        {
            return Ok(token.header.clone());
        }
        let mut form = vec![("grant_type", "client_credentials")];
        if let Some(scope) = &self.config.scope {
            form.push(("scope", scope));
        }
        let response = self
            .http
            .post(&self.config.token_url)
            .basic_auth(&self.config.client_id, Some(&self.config.client_secret))
            .form(&form)
            .send()
            .await?;
        // Never include the response body or credential-bearing URL in an auth error.
        if !response.status().is_success() {
            return Err(format!("OAuth token endpoint returned HTTP {}", response.status()).into());
        }
        #[derive(serde::Deserialize)]
        struct Response {
            access_token: String,
            expires_in: Option<u64>,
            token_type: Option<String>,
        }
        let response: Response = response.json().await?;
        if response
            .token_type
            .as_ref()
            .is_some_and(|kind| !kind.eq_ignore_ascii_case("bearer"))
        {
            return Err("OAuth token type must be Bearer".into());
        }
        let mut header: http::HeaderValue = format!("Bearer {}", response.access_token).parse()?;
        header.set_sensitive(true);
        let lifetime = response.expires_in.unwrap_or(60).max(1);
        let refresh_after =
            Duration::from_secs(lifetime.saturating_sub((lifetime / 10).clamp(1, 30)));
        *token = Some(Token {
            header: header.clone(),
            expires: Instant::now() + refresh_after,
        });
        Ok(header)
    }
}
