//! Spawns a child thread with an input, waits for it, and returns the variable it set.
use littlehorse::{ClientConfig, Workflow, proto::WaitForThreadsStrategy, worker::Worker};
use std::{collections::HashMap, path::PathBuf};

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-child-thread", |wf| {
        let result = wf.declare_str("result");
        let child = wf.spawn(
            "greeting-child",
            HashMap::from([("name".into(), "Ada".into())]),
            |child| {
                let name = child.declare_str("name");
                let output = child.execute("rust-child-greet", &name);
                result.assign(&output);
            },
        );
        wf.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
        wf.return_value(&result);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-child-greet", |name: String, _| async move {
        Ok(format!("Hello from child, {name}!"))
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
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-child-wf. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
