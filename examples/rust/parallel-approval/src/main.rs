//! Fans out one human approval thread per reviewer, then joins all threads.
use littlehorse::{
    ClientConfig, Workflow,
    proto::{VariableMutationType, WaitForThreadsStrategy},
    schema::UserTaskSchema,
    to_value,
};
use std::{collections::HashMap, path::PathBuf};

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, Error> {
    let reviewers = to_value(&serde_json::json!(["alice", "bob"]))?;
    Ok(Workflow::define("rust-parallel-approval", |wf| {
        let reviewers = wf.declare_json_arr("reviewers").default_value(reviewers);
        let rejections = wf.declare_int("rejections").with_default(0);
        let approvals = wf.for_each("one-approval", &reviewers, HashMap::new(), |child| {
            // INPUT is the foreach element supplied by LittleHorse.
            let reviewer = child.declare_str("INPUT");
            let answer = child
                .assign_user_task_to_user("rust-parallel-review", &reviewer)
                .with_notes("Approve this item?");
            child.do_if(answer.json_path("$.approved").equals(false), |_| {
                rejections.mutate(VariableMutationType::Add, 1);
            });
        });
        wf.wait_for_thread_list(&approvals, WaitForThreadsStrategy::WaitForAll);
        wf.return_value(rejections.equals(0));
    })?)
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
            UserTaskSchema::new("rust-parallel-review")
                .field(UserTaskSchema::typed_field::<bool>("approved", true)?)
                .compile()?,
        )
        .await?;
    workflow()?.register(&mut client).await?;
    println!(
        "Registered rust-parallel-approval. It has no task workers, so nothing else needs to run."
    );
    Ok(())
}
