use littlehorse::{
    Client, ClientConfig, EventOptions, LittleHorseValue, TaskOptions, Variable, Workflow,
    dsl::FailureFilter,
    proto::{
        CorrelatedEventConfig, LhErrorType, PutExternalEventDefRequest, ReturnType, VariableDef,
        WfSpec,
    },
    worker::{TaskFailure, Worker},
};
use std::path::PathBuf;
use tokio::{sync::watch, task::JoinSet};

type Error = Box<dyn std::error::Error + Send + Sync>;
const VERIFY_IDENTITY: &str = "verify-identity";
const NOTIFY_VERIFIED: &str = "notify-customer-verified";
const NOTIFY_NOT_VERIFIED: &str = "notify-customer-not-verified";
const IDENTITY_VERIFIED: &str = "identity-verified";

fn workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define("quickstart", |thread| {
        let full_name = thread.declare(
            Variable::typed::<String>("full-name")
                .searchable()
                .required(),
        );
        let email = thread.declare(Variable::typed::<String>("email").searchable().required());
        let ssn = thread.declare(Variable::typed::<i64>("ssn").masked().required());
        let identity_verified =
            thread.declare(Variable::typed::<bool>(IDENTITY_VERIFIED).searchable());
        thread.execute_with(
            TaskOptions::new(
                VERIFY_IDENTITY,
                [full_name.expression(), email.expression(), ssn.expression()],
            )
            .retries(3),
        );
        let result = thread.wait_for_event(
            EventOptions::new(IDENTITY_VERIFIED)
                .timeout(300.into())
                .correlation(email.expression(), false),
        );
        thread.handle(
            &result,
            FailureFilter::Error(Some(LhErrorType::Timeout)),
            |handler| {
                handler.execute(
                    NOTIFY_NOT_VERIFIED,
                    [full_name.expression(), email.expression()],
                );
                handler.fail(
                    "customer-not-verified",
                    "Unable to verify customer identity in time.",
                    None,
                );
                Ok(())
            },
        )?;
        thread.assign(&identity_verified, result.expression());
        thread.if_else(
            identity_verified.expression().equals(true),
            |thread| {
                thread.execute(
                    NOTIFY_VERIFIED,
                    [full_name.expression(), email.expression()],
                );
                Ok(())
            },
            |thread| {
                thread.execute(
                    NOTIFY_NOT_VERIFIED,
                    [full_name.expression(), email.expression()],
                );
                Ok(())
            },
        )?;
        Ok(())
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

fn start_workers(
    workers: Vec<Worker>,
    config: ClientConfig,
    shutdown: watch::Receiver<bool>,
) -> JoinSet<Result<(), littlehorse::worker::WorkerError>> {
    let mut tasks = JoinSet::new();
    for worker in workers {
        let config = config.clone();
        let shutdown = shutdown.clone();
        tasks.spawn(async move { worker.run_cluster(config, shutdown).await });
    }
    tasks
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let config = config()?;
    let mut client = config.clone().connect().await?;
    let workers = workers();
    register(&mut client, &workers).await?;
    let (shutdown, receiver) = watch::channel(false);
    let mut tasks = start_workers(workers, config, receiver);
    println!("Quickstart registered. Task workers running; press Ctrl-C to stop.");
    let result: Result<(), Error> = tokio::select! {
        signal = tokio::signal::ctrl_c() => signal.map_err(Into::into),
        result = tasks.join_next() => match result {
            Some(Ok(Err(error))) => Err(error.into()),
            Some(Err(error)) => Err(error.into()),
            _ => Err("a task worker stopped unexpectedly".into()),
        },
    };
    shutdown.send_replace(true);
    while let Some(worker) = tasks.join_next().await {
        worker??;
    }
    result
}

#[cfg(test)]
mod tests;
