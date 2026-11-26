//! Waits for a callback correlated by a business key instead of the workflow run ID.
use littlehorse::{
    ClientConfig, LittleHorseValue, Workflow,
    proto::{CorrelatedEventConfig, PutExternalEventDefRequest, ReturnType},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-correlated-event", |wf| {
        let key = wf.declare_str("order-id").with_default("order-123");
        let paid = wf
            .wait_for_event("rust-order-paid")
            .with_correlation_id(&key)
            .timeout(60);
        wf.return_value(&paid);
    })
}

fn config() -> Result<ClientConfig, Error> {
    let path = std::env::var_os("HOME")
        .or_else(|| std::env::var_os("USERPROFILE"))
        .map(|home| PathBuf::from(home).join(".config/littlehorse.config"));
    match path.filter(|path| path.is_file()) {
        Some(path) => ClientConfig::from_file(path),
        None => ClientConfig::from_env(),
    }
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let mut client = config()?.connect().await?;

    client
        .put_external_event_def(PutExternalEventDefRequest {
            name: "rust-order-paid".into(),
            content_type: Some(ReturnType {
                return_type: Some(bool::type_definition()),
            }),
            correlated_event_config: Some(CorrelatedEventConfig {
                delete_after_first_correlation: true,
                ..Default::default()
            }),
            ..Default::default()
        })
        .await?;
    workflow()?.register(&mut client).await?;
    println!(
        "Registered rust-correlated-event. It has no task workers, so nothing else needs to run."
    );
    Ok(())
}
