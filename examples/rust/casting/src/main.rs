//! Casts a string input to a double, then casts the double task output to an integer.
use littlehorse::{ClientConfig, LittleHorseValue, Workflow, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-casting", |wf| {
        let text = wf.declare_str("string-number").with_default("3.14");
        let discounted = wf.execute("rust-casting-discount", [text.cast(f64::type_definition())]);
        let doubled = wf.execute(
            "rust-casting-double",
            [discounted.cast(i64::type_definition())],
        );
        wf.return_value(&doubled);
    })
}

fn discount_worker() -> Worker {
    Worker::typed("rust-casting-discount", |value: f64, _| async move {
        Ok(value * 0.9)
    })
}

fn double_worker() -> Worker {
    Worker::typed("rust-casting-double", |value: i64, _| async move {
        Ok(value * 2)
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

    let discount = discount_worker();
    let double = double_worker();
    discount
        .register_typed::<f64, f64>(&mut client, "value")
        .await?;
    double
        .register_typed::<i64, i64>(&mut client, "value")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-casting. Press Ctrl-C to stop the workers.");

    littlehorse::worker::start(config, [discount, double]).await?;
    Ok(())
}
