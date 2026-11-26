//! Extracts JSON paths from a JSON object variable and passes them to two tasks.
use littlehorse::{ClientConfig, Expression, Workflow, to_value, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

fn workflow() -> Result<Workflow, Error> {
    let person =
        to_value(&serde_json::json!({"name":"Ada","car":{"brand":"Toyota","model":"Camry"}}))?;
    Ok(Workflow::define("rust-json", |wf| {
        let person = wf.declare_json_obj("person").default_value(person);
        wf.execute("rust-json-greet", [person.json_path("$.name")]);
        let car = wf.execute(
            "rust-json-car",
            [Expression::format(
                "You drive a {0} {1}.",
                [
                    person.json_path("$.car.brand"),
                    person.json_path("$.car.model"),
                ],
            )],
        );
        wf.return_value(&car);
    })?)
}

fn greet_worker() -> Worker {
    Worker::typed("rust-json-greet", |name: String, _| async move {
        println!("Hello, {name}!");
        Ok(name)
    })
}

fn car_worker() -> Worker {
    Worker::typed("rust-json-car", |description: String, _| async move {
        println!("{description}");
        Ok(description)
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

    let greet = greet_worker();
    let car = car_worker();
    greet
        .register_typed::<String, String>(&mut client, "name")
        .await?;
    car.register_typed::<String, String>(&mut client, "description")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-json. Press Ctrl-C to stop the workers.");

    littlehorse::worker::start(config, [greet, car]).await?;
    Ok(())
}
