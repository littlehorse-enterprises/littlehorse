//! A minimal greeting workflow:
//! 1. Declare a "name" variable of type String.
//! 2. Pass that variable into the "rust-basic-greet" task and return its output.
use littlehorse::{ClientConfig, Workflow, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-basic", |wf| {
        let name = wf.declare_str("name").with_default("world");
        let greeting = wf.execute("rust-basic-greet", &name);
        wf.return_value(&greeting);
    })
}

fn worker() -> Worker {
    Worker::typed("rust-basic-greet", |name: String, context| async move {
        let greeting = format!("Hello, {name}!");
        context.log(&greeting);
        println!("{greeting}");
        Ok(greeting)
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
    println!("Registered rust-basic. Press Ctrl-C to stop the worker.");

    littlehorse::worker::start(config, [worker]).await?;
    Ok(())
}
