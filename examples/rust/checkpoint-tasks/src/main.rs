//! A task that checkpoints its first step, fails on the first attempt, and replays the
//! checkpointed result on the retry instead of executing that step again.
use littlehorse::{
    ClientConfig, Workflow,
    worker::{TaskFailure, Worker},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-checkpoint-tasks", |wf| {
        let output = wf.execute("rust-checkpoint-greet", "Ada").with_retries(1);
        wf.return_value(&output);
    })
}

fn worker() -> Worker {
    Worker::typed(
        "rust-checkpoint-greet",
        |name: String, mut context| async move {
            let first = context
                .checkpoint_typed(|| async {
                    println!("Executing first checkpoint");
                    Ok(format!("hello {name}"))
                })
                .await?;
            if context.task.attempt_number == 0 {
                return Err(TaskFailure::Error("failure after persistence".into()));
            }
            let second = context
                .checkpoint_typed(|| async { Ok(", second checkpoint complete".to_string()) })
                .await?;
            Ok(first + &second)
        },
    )
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
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-checkpoint-tasks. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
