//! Registers two inbound event types and an outbound workflow event, then a workflow
//! that waits for both events and emits a summary.
use littlehorse::{
    ClientConfig, Expression, LittleHorseValue, Workflow,
    proto::{PutExternalEventDefRequest, PutWorkflowEventDefRequest, ReturnType},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-event-registration", |wf| {
        let name = wf.wait_for_event("rust-summary-name").timeout(60);
        let age = wf.wait_for_event("rust-summary-age").timeout(60);
        let summary = Expression::format("Summary: {0}, age {1}", (&name, &age));
        wf.throw_event("rust-summary-ready", summary.clone());
        wf.return_value(summary);
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

    for (name, content_type) in [
        ("rust-summary-name", String::type_definition()),
        ("rust-summary-age", i64::type_definition()),
    ] {
        client
            .put_external_event_def(PutExternalEventDefRequest {
                name: name.into(),
                content_type: Some(ReturnType {
                    return_type: Some(content_type),
                }),
                ..Default::default()
            })
            .await?;
    }
    client
        .put_workflow_event_def(PutWorkflowEventDefRequest {
            name: "rust-summary-ready".into(),
            content_type: Some(ReturnType {
                return_type: Some(String::type_definition()),
            }),
        })
        .await?;
    workflow()?.register(&mut client).await?;
    println!(
        "Registered rust-event-registration. It has no task workers, so nothing else needs to run."
    );
    Ok(())
}
