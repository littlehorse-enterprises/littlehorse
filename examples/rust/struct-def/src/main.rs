//! Derives nested StructDefs, looks up a vehicle owner, and passes the typed result
//! to a second task.
use littlehorse::{ClientConfig, LittleHorseValue, Workflow, worker::Worker};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;

#[derive(LittleHorseValue)]
#[lh(name = "rust-ticket-address")]
struct Address {
    city: String,
}

#[derive(LittleHorseValue)]
#[lh(name = "rust-ticket-person")]
struct Person {
    name: String,
    address: Address,
}

#[derive(LittleHorseValue)]
#[lh(name = "rust-parking-ticket")]
struct ParkingTicket {
    make: String,
    model: String,
    #[lh(rename = "licensePlate")]
    license_plate: String,
}

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("rust-struct-def", |wf| {
        let ticket = wf
            .declare_struct::<ParkingTicket>("ticket")
            .with_default(ParkingTicket {
                make: "Toyota".into(),
                model: "Camry".into(),
                license_plate: "ABC-123".into(),
            });
        let owner = wf.execute("rust-ticket-owner", &ticket);
        let result = wf.execute("rust-mail-ticket", &owner);
        wf.return_value(&result);
    })
}

fn owner_worker() -> Worker {
    Worker::typed("rust-ticket-owner", |ticket: ParkingTicket, _| async move {
        println!("Looking up {}", ticket.license_plate);
        Ok(Person {
            name: "Obi-Wan Kenobi".into(),
            address: Address {
                city: "Anchorhead".into(),
            },
        })
    })
}

fn mail_worker() -> Worker {
    Worker::typed("rust-mail-ticket", |person: Person, _| async move {
        // Simulated mail operation using synthetic example data.
        Ok(format!(
            "Ticket sent to {} in {}",
            person.name, person.address.city
        ))
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

    // register_typed also registers the StructDefs each task uses.
    let owner = owner_worker();
    let mail = mail_worker();
    owner
        .register_typed::<ParkingTicket, Person>(&mut client, "ticket")
        .await?;
    mail.register_typed::<Person, String>(&mut client, "person")
        .await?;
    workflow()?.register(&mut client).await?;
    println!("Registered rust-struct-def. Press Ctrl-C to stop the workers.");

    littlehorse::worker::start(config, [owner, mail]).await?;
    Ok(())
}
