//! A parent workflow exposes a public variable; a separately started child workflow
//! inherits and reads it.
use littlehorse::{
    ClientConfig, LittleHorseValue, Workflow,
    proto::{PutExternalEventDefRequest, ReturnType, WfRunVariableAccessLevel},
    worker::Worker,
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn parent_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-hierarchical-workflow", |wf| {
        let name = wf
            .declare_str("input-name")
            .with_default("Ada")
            .access(WfRunVariableAccessLevel::PublicVar);
        wf.wait_for_event("rust-hierarchy-finished").timeout(60);
        wf.return_value(&name);
    })
}

fn child_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Ok(Workflow::define("rust-hierarchical-child", |wf| {
        let name = wf
            .declare_str("input-name")
            .access(WfRunVariableAccessLevel::InheritedVar);
        let output = wf.execute("rust-hierarchy-greet", &name);
        wf.return_value(&output);
    })?
    .parent("rust-hierarchical-workflow", 0))
}

fn worker() -> Worker {
    Worker::typed("rust-hierarchy-greet", |name: String, _| async move {
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
            name: "rust-hierarchy-finished".into(),
            content_type: Some(ReturnType {
                return_type: Some(bool::type_definition()),
            }),
            ..Default::default()
        })
        .await?;
    let worker = worker();
    worker
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    // Register the parent first; the child inherits its public variable.
    parent_workflow()?.register(&mut client).await?;
    child_workflow()?.register(&mut client).await?;
    println!("Registered rust-hierarchical-workflow. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
