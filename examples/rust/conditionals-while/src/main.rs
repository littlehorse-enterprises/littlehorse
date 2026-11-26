//! Eats donuts in a workflow loop until the counter reaches zero.
use littlehorse::{ClientConfig, Workflow, proto::VariableMutationType, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-conditionals-while", |wf| {
        let donuts = wf.declare_int("number-of-donuts").with_default(3);
        wf.while_loop(donuts.greater_than(0), |wf| {
            donuts.mutate(VariableMutationType::Subtract, 1);
            wf.execute("rust-eating-donut", &donuts);
        });
        wf.return_value(&donuts);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-eating-donut", |remaining: i64, _| async move {
        println!("Donuts left: {remaining}");
        Ok(remaining)
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
        .register_typed::<i64, i64>(&mut client, "remaining")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-conditionals-while. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
