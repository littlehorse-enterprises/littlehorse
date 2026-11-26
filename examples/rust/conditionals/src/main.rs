//! Recommends a donut below 10 donuts and a salad otherwise, using a workflow if/else.
use littlehorse::{ClientConfig, Workflow, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-conditionals", |wf| {
        let donuts = wf.declare_int("number-of-donuts").with_default(3);
        let message = wf.declare_str("recommendation");
        wf.do_if(donuts.less_than(10), |_| {
            message.assign("Have another donut!");
        })
        .do_else(|_| {
            message.assign("Have a salad!");
        });
        let output = wf.execute("rust-conditionals-recommend", &message);
        wf.return_value(&output);
    })
}

fn worker() -> Worker {
    Worker::typed(
        "rust-conditionals-recommend",
        |message: String, _| async move {
            println!("{message}");
            Ok(message)
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
        .register_typed::<String, String>(&mut client, "message")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-conditionals. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
