//! Retries a technical task failure, using the per-run attempt number.
use littlehorse::{
    ClientConfig, Workflow,
    worker::{TaskFailure, Worker},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-retries", |wf| {
        let output = wf
            .execute("rust-retry-task", "retry me")
            .with_retries(2)
            .timeout(10);
        wf.return_value(&output);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-retry-task", |_: String, context| async move {
        let attempt = context.task.attempt_number;
        println!("Attempt {attempt}");
        if attempt == 0 {
            return Err(TaskFailure::Error("simulated transient failure".into()));
        }
        Ok(format!("Succeeded on attempt {attempt}"))
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
        .register_typed::<String, String>(&mut client, "message")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-retries. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
