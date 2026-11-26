//! Handles technical task errors and named business exceptions separately.
use littlehorse::{
    ClientConfig, Workflow,
    proto::{LhErrorType, LhTaskException},
    worker::{TaskFailure, Worker},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-error-handling", |wf| {
        let kind = wf.declare_str("kind").with_default("technical");
        let result = wf.declare_str("result");
        let failed = wf.execute("rust-fail-task", &kind);
        wf.handle_error(&failed, LhErrorType::TaskError, |_| {
            result.assign("technical error handled");
        });
        wf.handle_exception(&failed, "declined", |_| {
            result.assign("business exception handled");
        });
        wf.return_value(&result);
    })
}

fn worker() -> Worker {
    Worker::typed::<String, String, _, _>("rust-fail-task", |kind: String, _| async move {
        if kind == "business" {
            Err(TaskFailure::Exception(Box::new(LhTaskException {
                name: "declined".into(),
                message: "simulated business rejection".into(),
                ..Default::default()
            })))
        } else {
            Err(TaskFailure::Error("simulated technical failure".into()))
        }
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

    let worker = worker();
    worker
        .register_typed::<String, String>(&mut client, "kind")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-error-handling. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
