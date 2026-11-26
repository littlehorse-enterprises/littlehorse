//! Waits for an external event addressed to a specific workflow run, then greets its content.
use littlehorse::{
    ClientConfig, LittleHorseValue, Workflow,
    proto::{PutExternalEventDefRequest, ReturnType},
    worker::Worker,
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-external-event", |wf| {
        let name = wf.wait_for_event("rust-name-received").timeout(60);
        let result = wf.execute("rust-event-greet", &name);
        wf.return_value(&result);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-event-greet", |name: String, _| async move {
        Ok(format!("Hello, {name}!"))
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
    let config = config()?;
    let mut client = config.clone().connect().await?;

    client
        .put_external_event_def(PutExternalEventDefRequest {
            name: "rust-name-received".into(),
            content_type: Some(ReturnType {
                return_type: Some(String::type_definition()),
            }),
            ..Default::default()
        })
        .await?;
    let worker = worker();
    worker
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-external-event. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
