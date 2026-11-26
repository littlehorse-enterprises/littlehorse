//! Assigns a task result back to a workflow variable and passes it to another task.
use littlehorse::{ClientConfig, Workflow, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-mutation", |wf| {
        let name = wf.declare_str("name").with_default("Peter");
        let bitten = wf.execute("rust-spider-bite", &name);
        name.assign(&bitten);
        let news = wf.execute("rust-daily-bugle", &name);
        wf.return_value(&news);
    })
}

fn spider_bite_worker() -> Worker {
    Worker::typed("rust-spider-bite", |name: String, _| async move {
        Ok(if matches!(name.as_str(), "Peter" | "Miles" | "Gwen") {
            format!("{name} transforms into Spider-Man!")
        } else {
            format!("{name} remains the same.")
        })
    })
}

fn daily_bugle_worker() -> Worker {
    Worker::typed("rust-daily-bugle", |news: String, _| async move {
        println!("{news}");
        Ok(news)
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

    let spider_bite = spider_bite_worker();
    let daily_bugle = daily_bugle_worker();
    spider_bite
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    daily_bugle
        .register_typed::<String, String>(&mut client, "news")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-mutation. Press Ctrl-C to stop the workers.");

    littlehorse::worker::start(config, [spider_bite, daily_bugle]).await?;
    Ok(())
}
