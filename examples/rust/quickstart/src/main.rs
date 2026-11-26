use littlehorse::{
    Client, ClientConfig, LittleHorseValue, Workflow,
    proto::{
        CorrelatedEventConfig, LhErrorType, PutExternalEventDefRequest, ReturnType, VariableDef,
        WfSpec,
    },
    worker::{TaskFailure, Worker},
};
use std::path::PathBuf;

type Error = Box<dyn std::error::Error + Send + Sync>;
const VERIFY_IDENTITY: &str = "verify-identity";
const NOTIFY_VERIFIED: &str = "notify-customer-verified";
const NOTIFY_NOT_VERIFIED: &str = "notify-customer-not-verified";
const IDENTITY_VERIFIED: &str = "identity-verified";

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("quickstart", |wf| {
        let full_name = wf.declare_str("full-name").searchable().required();
        let email = wf.declare_str("email").searchable().required();
        let ssn = wf.declare_int("ssn").masked().required();
        let identity_verified = wf.declare_bool(IDENTITY_VERIFIED).searchable();
        wf.execute(VERIFY_IDENTITY, (&full_name, &email, &ssn))
            .with_retries(3);
        let result = wf
            .wait_for_event(IDENTITY_VERIFIED)
            .timeout(300)
            .with_correlation_id(&email);
        wf.handle_error(&result, LhErrorType::Timeout, |handler| {
            handler.execute(NOTIFY_NOT_VERIFIED, (&full_name, &email));
            handler.fail(
                "customer-not-verified",
                "Unable to verify customer identity in time.",
                None,
            );
        });
        identity_verified.assign(&result);
        wf.do_if(identity_verified.equals(true), |wf| {
            wf.execute(NOTIFY_VERIFIED, (&full_name, &email));
        })
        .do_else(|wf| {
            wf.execute(NOTIFY_NOT_VERIFIED, (&full_name, &email));
        });
    })
}

fn workers() -> Vec<Worker> {
    let verify_identity = Worker::new(VERIFY_IDENTITY, |context| async move {
        let full_name: String = context.input(0)?;
        let email: String = context.input(1)?;
        let _ssn: i64 = context.input(2)?;
        // Simulate submitting to an identity service; the callback arrives as a correlated event.
        let message =
            format!("[verify-identity] Verification request submitted for {full_name} <{email}>");
        context.log(&message);
        println!("{message}");
        ().to_lh()
            .map_err(|error| TaskFailure::Output(error.to_string()))
    });
    let [notify_verified, notify_not_verified] = [
        (NOTIFY_VERIFIED, "Identity confirmed"),
        (NOTIFY_NOT_VERIFIED, "Identity could not be confirmed"),
    ]
    .map(|(task_name, outcome)| {
        Worker::new(task_name, move |context| async move {
            let full_name: String = context.input(0)?;
            let email: String = context.input(1)?;
            let message = format!("[{task_name}] {outcome} for {full_name} <{email}>");
            context.log(&message);
            println!("{message}");
            message
                .to_lh()
                .map_err(|error| TaskFailure::Output(error.to_string()))
        })
    });
    vec![verify_identity, notify_verified, notify_not_verified]
}

async fn register(client: &mut Client, workers: &[Worker]) -> Result<WfSpec, Error> {
    client
        .put_external_event_def(PutExternalEventDefRequest {
            name: IDENTITY_VERIFIED.into(),
            content_type: Some(ReturnType {
                return_type: Some(bool::type_definition()),
            }),
            correlated_event_config: Some(CorrelatedEventConfig {
                delete_after_first_correlation: true,
                ..Default::default()
            }),
            ..Default::default()
        })
        .await?;
    for worker in workers {
        let mut inputs = vec![
            VariableDef {
                name: "full-name".into(),
                type_def: Some(String::type_definition()),
                ..Default::default()
            },
            VariableDef {
                name: "email".into(),
                type_def: Some(String::type_definition()),
                ..Default::default()
            },
        ];
        let output = if worker.task_name == VERIFY_IDENTITY {
            inputs.push(VariableDef {
                name: "ssn".into(),
                type_def: Some(i64::type_definition()),
                ..Default::default()
            });
            None
        } else {
            Some(String::type_definition())
        };
        worker.register(client, inputs, output).await?;
    }
    workflow()?.register(client).await
}

fn config() -> Result<ClientConfig, Error> {
    let path = std::env::var_os("HOME")
        .or_else(|| std::env::var_os("USERPROFILE"))
        .map(PathBuf::from)
        .map(|home_directory| home_directory.join(".config/littlehorse.config"));
    match path.filter(|path| path.is_file()) {
        Some(path) => ClientConfig::from_file(path),
        None => ClientConfig::from_env(),
    }
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let config = config()?;
    let mut client = config.clone().connect().await?;
    let workers = workers();
    register(&mut client, &workers).await?;
    println!("Quickstart registered. Task workers running; press Ctrl-C to stop.");
    littlehorse::worker::start(config, workers).await?;
    Ok(())
}
