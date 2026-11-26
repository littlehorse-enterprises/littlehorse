//! Waits for a counter to reach zero; each interrupt event decrements it.
use littlehorse::{
    ClientConfig, LittleHorseValue, Workflow,
    proto::{PutExternalEventDefRequest, ReturnType, VariableMutationType},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-wait-for-condition", |wf| {
        let counter = wf.declare_int("counter").with_default(2);
        wf.interrupt("rust-subtract", "subtract-handler", |_| {
            counter.mutate(VariableMutationType::Subtract, 1);
        });
        wf.wait_for_condition(counter.equals(0));
        wf.return_value(&counter);
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
            name: "rust-subtract".into(),
            content_type: Some(ReturnType {
                return_type: Some(bool::type_definition()),
            }),
            ..Default::default()
        })
        .await?;
    workflow()?.register(&mut client).await?;
    println!(
        "Registered rust-wait-for-condition. It has no task workers, so nothing else needs to run."
    );
    Ok(())
}
