//! Handles an event in an interrupt thread while the main thread is waiting.
use littlehorse::{
    ClientConfig, LittleHorseValue, Workflow,
    proto::{PutExternalEventDefRequest, ReturnType, VariableMutationType},
    worker::Worker,
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-interrupt-handler", |wf| {
        let count = wf.declare_int("interrupt-count").with_default(0);
        wf.interrupt("rust-interrupt", "on-interrupt", |handler| {
            handler.execute("rust-interrupt-notify", "Interrupted!");
            count.mutate(VariableMutationType::Add, 1);
        });
        wf.wait_for_condition(count.greater_than(0));
        wf.wait_for_event("rust-interrupt-finish").timeout(60);
        wf.return_value(&count);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-interrupt-notify", |message: String, _| async move {
        println!("{message}");
        Ok(message)
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

    for (name, content_type) in [
        ("rust-interrupt", String::type_definition()),
        ("rust-interrupt-finish", bool::type_definition()),
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
    let worker = worker();
    worker
        .register_typed::<String, String>(&mut client, "message")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-interrupt-handler. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
