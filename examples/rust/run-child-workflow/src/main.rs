//! Registers a separate child WfSpec, invokes it from a parent, and returns its result.
use littlehorse::{ClientConfig, Workflow, worker::Worker};
use std::{collections::HashMap, path::PathBuf};

type Error = Box<dyn std::error::Error + Send + Sync>;

fn child_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-greeting-child-workflow", |wf| {
        let name = wf.declare_str("name").required();
        let output = wf.execute("rust-child-workflow-greet", &name);
        wf.return_value(&output);
    })
}

fn parent_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-run-child-workflow", |wf| {
        let child = wf.run_workflow(
            "rust-greeting-child-workflow",
            0,
            HashMap::from([("name".into(), "Ada".into())]),
            None,
        );
        let output = wf.wait_for_child(&child);
        // An explicit result type also works with v1.3.1's output inference.
        let greeting = wf.declare_str("greeting");
        greeting.assign(&output);
        wf.return_value(&greeting);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-child-workflow-greet", |name: String, _| async move {
        Ok(format!("Hello from child workflow, {name}!"))
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
    // Register the child first so the parent can reference it.
    child_workflow()?.register(&mut client).await?;
    parent_workflow()?.register(&mut client).await?;
    println!("Registered rust-run-child-workflow. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
