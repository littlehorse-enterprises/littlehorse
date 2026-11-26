//! Assigns a human approval to alice and returns the submitted boolean.
use littlehorse::{ClientConfig, Workflow, schema::UserTaskSchema};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-user-tasks", |wf| {
        let answer = wf
            .assign_user_task_to_user("rust-review-request", "alice")
            .with_notes("Approve this request?");
        let approved = wf.declare_bool("approved");
        approved.assign(answer.json_path("$.approved"));
        wf.return_value(&approved);
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
        .put_user_task_def(
            UserTaskSchema::new("rust-review-request")
                .field(UserTaskSchema::typed_field::<bool>("approved", true)?)
                .compile()?,
        )
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-user-tasks. It has no task workers, so nothing else needs to run.");
    Ok(())
}
