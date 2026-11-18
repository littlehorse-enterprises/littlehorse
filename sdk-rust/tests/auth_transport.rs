use littlehorse::{
    ClientConfig,
    auth::OAuthConfig,
    proto::{
        little_horse_server::{LittleHorse, LittleHorseServer},
        *,
    },
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use tokio_stream::wrappers::TcpListenerStream;
use tonic::{Request, Response, Status};
struct EchoAuth;
#[tonic::async_trait]
impl LittleHorse for EchoAuth {
    async fn whoami(&self, request: Request<()>) -> Result<Response<Principal>, Status> {
        let token = request
            .metadata()
            .get("authorization")
            .and_then(|token| token.to_str().ok())
            .unwrap_or("none");
        Ok(Response::new(Principal {
            id: Some(PrincipalId { id: token.into() }),
            ..Default::default()
        }))
    }
}
#[tokio::test]
async fn oauth_tokens_are_cached_and_refreshed_before_expiry() {
    for lifetime in [3600, 1] {
        let requests = Arc::new(AtomicUsize::new(0));
        let token_listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let token_address = token_listener.local_addr().unwrap();
        let app = axum::Router::new().route("/token", axum::routing::post({
            let requests = requests.clone();
            move |headers: axum::http::HeaderMap, axum::Form(form): axum::Form<std::collections::HashMap<String, String>>| {
                let requests = requests.clone(); async move {
                    assert_eq!(form["grant_type"], "client_credentials");
                    assert_eq!(headers["authorization"], "Basic Y2xpZW50OnNlY3JldA==");
                    let count = requests.fetch_add(1, Ordering::SeqCst) + 1;
                    axum::Json(serde_json::json!({"access_token": format!("token-{count}"), "expires_in": lifetime, "token_type": "Bearer"}))
                }
            }
        }));
        let token_server = tokio::spawn(async {
            axum::serve(token_listener, app).await.unwrap();
        });
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let address = listener.local_addr().unwrap();
        let server = tokio::spawn(
            tonic::transport::Server::builder()
                .add_service(LittleHorseServer::new(EchoAuth))
                .serve_with_incoming(TcpListenerStream::new(listener)),
        );
        let mut client = ClientConfig {
            endpoint: format!("http://{address}"),
            oauth: Some(OAuthConfig {
                token_url: format!("http://{token_address}/token"),
                client_id: "client".into(),
                client_secret: "secret".into(),
                scope: None,
            }),
            ..Default::default()
        }
        .connect()
        .await
        .unwrap();
        assert_eq!(
            client.whoami(()).await.unwrap().into_inner().id.unwrap().id,
            "Bearer token-1"
        );
        let second = client.whoami(()).await.unwrap().into_inner().id.unwrap().id;
        if lifetime == 3600 {
            assert_eq!(second, "Bearer token-1");
            assert_eq!(requests.load(Ordering::SeqCst), 1);
        } else {
            assert_eq!(second, "Bearer token-2");
            assert_eq!(requests.load(Ordering::SeqCst), 2);
        }
        server.abort();
        token_server.abort();
    }
}
#[tokio::test]
async fn tls_and_mutual_tls_verify_custom_certificate_authorities() {
    use rcgen::{
        BasicConstraints, CertificateParams, ExtendedKeyUsagePurpose, IsCa, KeyPair,
        KeyUsagePurpose,
    };
    let ca_key = KeyPair::generate().unwrap();
    let mut ca_params = CertificateParams::new(vec![]).unwrap();
    ca_params.is_ca = IsCa::Ca(BasicConstraints::Unconstrained);
    ca_params.key_usages = vec![
        KeyUsagePurpose::KeyCertSign,
        KeyUsagePurpose::DigitalSignature,
        KeyUsagePurpose::CrlSign,
    ];
    let ca = ca_params.self_signed(&ca_key).unwrap();
    let server_key = KeyPair::generate().unwrap();
    let mut server_params = CertificateParams::new(vec!["localhost".into()]).unwrap();
    server_params.extended_key_usages = vec![ExtendedKeyUsagePurpose::ServerAuth];
    let server_cert = server_params.signed_by(&server_key, &ca, &ca_key).unwrap();
    let client_key = KeyPair::generate().unwrap();
    let mut client_params = CertificateParams::new(vec!["client".into()]).unwrap();
    client_params.extended_key_usages = vec![ExtendedKeyUsagePurpose::ClientAuth];
    let client_cert = client_params.signed_by(&client_key, &ca, &ca_key).unwrap();
    for mutual in [false, true] {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let address = listener.local_addr().unwrap();
        let mut server_tls = tonic::transport::ServerTlsConfig::new().identity(
            tonic::transport::Identity::from_pem(server_cert.pem(), server_key.serialize_pem()),
        );
        if mutual {
            server_tls =
                server_tls.client_ca_root(tonic::transport::Certificate::from_pem(ca.pem()));
        }
        let server = tokio::spawn(
            tonic::transport::Server::builder()
                .tls_config(server_tls)
                .unwrap()
                .add_service(LittleHorseServer::new(EchoAuth))
                .serve_with_incoming(TcpListenerStream::new(listener)),
        );
        let mut tls = tonic::transport::ClientTlsConfig::new()
            .domain_name("localhost")
            .ca_certificate(tonic::transport::Certificate::from_pem(ca.pem()));
        if mutual {
            tls = tls.identity(tonic::transport::Identity::from_pem(
                client_cert.pem(),
                client_key.serialize_pem(),
            ));
        }
        let mut client = ClientConfig {
            endpoint: format!("https://{address}"),
            tls: Some(tls),
            ..Default::default()
        }
        .connect()
        .await
        .unwrap();
        assert!(client.whoami(()).await.is_ok());
        let untrusted = ClientConfig {
            endpoint: format!("https://{address}"),
            ..Default::default()
        }
        .connect()
        .await;
        assert!(untrusted.is_err(), "untrusted CA must be rejected");
        server.abort();
    }
}
